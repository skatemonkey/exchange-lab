# Order Flow

## 1. Overview

The current `POST /api/orders/limit` API accepts a limit order and queues it
through Kafka. Phase 6 changes the matching engine so active orders are matched
from an in-memory order book instead of database queries or Redis.

The target flow is categorized into six stages:

1. Create order.
2. Queue order through Kafka.
3. Reserve cash or stock.
4. Match against in-memory order book.
5. Record order and trade result.
6. Settle cash and stock.

## 2. Pseudocode Flow

Marker: `[DB read]` reads from database, `[DB write]` writes to database,
`[Kafka]` uses Kafka, and `[Memory]` uses in-process memory.

```text
# Server startup
[DB read] load active open orders
    active means ACCEPTED or PARTIALLY_FILLED with remaining quantity > 0

[Memory] rebuild order books by symbol
    buy side: highest price first
    sell side: lowest price first
    same price level: oldest order first

# 1. Create order
receive limit order request
validate trader, symbol, side, price, and quantity
create incoming order event

# 2. Queue order
[Kafka] publish accepted order event
return 202 Accepted

# 3. Reserve cash/stock
[Kafka] matching worker consumes accepted order event
recreate incoming order from event

if incoming order is BUY:
    [DB read] load trader account
    required_cash = limit_price * quantity
    require available cash >= required_cash
    reserve required_cash
    [DB write] save trader account

if incoming order is SELL:
    [DB read] load stock position
    required_stock = quantity
    require available stock >= required_stock
    reserve required_stock
    [DB write] save stock position

# 4. Match against in-memory order book
[Memory] get order book for incoming order symbol

if incoming order is BUY:
    [Memory] read sell side from lowest price to highest price
    stop when best sell price > incoming buy limit price

if incoming order is SELL:
    [Memory] read buy side from highest price to lowest price
    stop when best buy price < incoming sell limit price

for each matching resting order:
    if incoming order has no remaining quantity:
        stop matching

    trade quantity = min(incoming remaining quantity, resting remaining quantity)
    trade price = resting order limit price

    create trade result
    reduce incoming order remaining quantity
    reduce resting order remaining quantity

    if resting order is fully filled:
        [Memory] remove resting order from order book

    if resting price level is empty:
        [Memory] remove price level from order book

if incoming order still has remaining quantity:
    [Memory] add incoming order to its own side of the order book

# 5. Record order/trade result
[DB write] save incoming order
[DB write] save updated resting orders
[DB write] save created trades

# 6. Settle cash and stock
for each created trade:
    [DB read] load affected trader accounts
    [DB read] load affected stock positions

    buyer pays cash
    seller receives cash
    buyer receives stock
    seller delivers stock
    reduce/release reserved cash or stock

[DB write] save updated trader accounts
[DB write] save updated stock positions
```

## 3. In-Memory Order Book Rule

- Memory stores only active open orders.
  - Active means waiting to match or partially filled.
  - Fully filled or cancelled orders are removed from memory.
- DB stores durable order history.
  - On startup, memory can be rebuilt from active DB orders.
- One order book is owned per symbol.
  - Example: `ACME` has its own buy side and sell side.
- Buy side priority:
  - Higher price first.
  - Same price uses FIFO order.
- Sell side priority:
  - Lower price first.
  - Same price uses FIFO order.

## 4. Current Reserve Rule

- Reservation is still stored in DB columns for now.
  - Buy reserves `trader_accounts.reserved_cash`.
  - Sell reserves `stock_positions.reserved_quantity`.
- Buy example:
  - Cash balance is `1000`.
  - Buy order reserves `300`.
  - DB becomes `cash_balance = 1000`, `reserved_cash = 300`.
- Sell example:
  - Stock quantity is `10`.
  - Sell order reserves `6`.
  - DB becomes `quantity = 10`, `reserved_quantity = 6`.

## 5. Known Problems

1. Kafka consumer must be idempotent.
   - Duplicate order events must not create duplicate matches or duplicate DB
     updates.
2. In-memory order book recovery must be reliable.
   - On startup, active DB orders must rebuild memory in correct price-time
     order.
3. Matching ownership is not finalized.
   - Long term direction is one matching worker owns one symbol or one symbol
     partition to avoid two workers matching the same order book.
4. Memory loss is expected on restart.
   - This is acceptable only because DB remains the durable recovery source.
5. Reservation timing is still simplified.
   - The API returns `202` after Kafka publish, while reserve failure can happen
     later in the consumer. A later phase may move fast reservation before
     publish.
