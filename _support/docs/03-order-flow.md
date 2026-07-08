# Order Flow

## 1. Overview

The target `POST /api/orders/limit` flow uses Redis for fast cash/stock
reservation, Kafka for queueing accepted orders, and an in-memory order book for
live matching.

Phase 6 changed the matching engine so active orders are matched from memory
instead of database queries or a Redis order book. Redis is still part of the
target design for cash/stock reservation.

The target flow is categorized into six stages:

1. Create order.
2. Reserve cash or stock in Redis.
3. Queue order through Kafka.
4. Match against in-memory order book.
5. Record order and trade result.
6. Settle cash and stock.

## 2. Pseudocode Flow

Marker: `[DB read]` reads from database, `[DB write]` writes to database,
`[Redis]` uses Redis, `[Kafka]` uses Kafka, and `[Memory]` uses in-process
memory.

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
create incoming order

# 2. Reserve cash/stock in Redis
# Redis stores available amount only:
# cash:available:{traderId} = cash_balance - reserved_cash
# stock:available:{traderId}:{symbol} = quantity - reserved_quantity

if incoming order is BUY:
    if Redis cash key is missing:
        [DB read] load trader account
        [Redis] set available cash = cash_balance - reserved_cash

    required_cash = limit_price * quantity
    [Redis] atomically require cash:available:{traderId} >= required_cash
    [Redis] cash:available:{traderId} -= required_cash
    attach reserved cash amount to Kafka event

if incoming order is SELL:
    if Redis stock key is missing:
        [DB read] load stock position
        [Redis] set available stock = quantity - reserved_quantity

    required_stock = quantity
    [Redis] atomically require stock:available:{traderId}:{symbol} >= required_stock
    [Redis] stock:available:{traderId}:{symbol} -= required_stock
    attach reserved stock quantity to Kafka event

# 3. Queue order through Kafka
[Kafka] publish accepted order event
return 202 Accepted

# 4. Match against in-memory order book
[Kafka] matching worker consumes accepted order event
recreate incoming order from event
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
    reduce/release reserved cash or stock in DB

    [Redis] seller cash:available:{sellerId} += trade amount
    [Redis] buyer stock:available:{buyerId}:{symbol} += trade quantity
    [Redis] buyer unused reserved cash returns to available cash if trade price < buy limit price

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

## 4. Redis Reserve Rule

- Redis stores only available cash/stock.
  - Buy example: `cash:available:T1 = 1000`, buy order reserves `300`, Redis
    becomes `700`.
  - Sell example: `stock:available:T1:ACME = 10`, sell order reserves `6`,
    Redis becomes `4`.
- Kafka event carries the reserved amount.
  - Buy event carries `reservedCash = 300`.
  - Sell event carries `reservedQuantity = 6`.
- DB later records the real durable columns.
  - Buy not matched: `cash_balance = 1000`, `reserved_cash = 300`.
  - Buy fully matched at limit price: `cash_balance = 700`, `reserved_cash = 0`.
  - Sell not matched: `quantity = 10`, `reserved_quantity = 6`.
  - Sell fully matched: `quantity = 4`, `reserved_quantity = 0`.

## 5. Known Problems

1. Redis reserve must be atomic.
   - Do not `GET` in Redis, calculate in Java, then `SET` back.
   - Use Lua script or another atomic Redis operation to check and deduct in one
     step.
2. Kafka publish failure needs rollback.
   - If Redis reserve succeeds but Kafka publish fails, Redis must restore the
     reserved cash/stock.
3. Kafka consumer must be idempotent.
   - Duplicate order events must not create duplicate matches or duplicate DB
     updates.
   - Use a key like `processed:order:{orderId}`.
4. In-memory order book recovery must be reliable.
   - On startup, active DB orders must rebuild memory in correct price-time
     order.
5. Matching ownership is not finalized.
   - Long term direction is one matching worker owns one symbol or one symbol
     partition to avoid two workers matching the same order book.
6. Reservation timing is not implemented yet.
   - Current code still reserves cash/stock in MySQL inside the Kafka consumer.
   - Target flow reserves in Redis before publishing to Kafka.
