# Order Flow

## 1. Overview

The current `POST /api/orders/limit` API is synchronous, but the target high-concurrency flow is categorized into six stages:

1. Create order.
2. Reserve cash/stock (Redis lazy load).
3. Queue order (Kafka).
4. Match against Redis order book.
5. Record order/trade result.
6. Settle cash and stock.

## 2. Pseudocode Flow

Marker: `[DB read]` reads from database, `[DB write]` writes to database, `[Redis]` uses Redis, `[Kafka]` uses Kafka.

```text
# Server startup
[DB read] load active open orders
[Redis] preload active open orders into Redis order book

# 1. Create order
receive limit order request
validate trader, symbol, side, price, and quantity
create incoming order

# 2. Reserve cash/stock (Redis lazy load)
# Redis stores available amount only:
# cash:available:{traderId} = cash_balance - reserved_cash
# stock:available:{traderId}:{symbol} = quantity - reserved_quantity

if incoming order is BUY:
    if Redis cash key is missing:
        [DB read] load trader account
        [Redis] set available cash = cash_balance - reserved_cash

    required_cash = limit_price * quantity
    [Redis] require cash:available:{traderId} >= required_cash
    [Redis] cash:available:{traderId} -= required_cash
    attach reserved cash amount to Kafka event

if incoming order is SELL:
    if Redis stock key is missing:
        [DB read] load stock position
        [Redis] set available stock = quantity - reserved_quantity

    required_stock = quantity
    [Redis] require stock:available:{traderId}:{symbol} >= required_stock
    [Redis] stock:available:{traderId}:{symbol} -= required_stock
    attach reserved stock quantity to Kafka event

# 3. Queue order (Kafka)
[Kafka] publish accepted order event

# 4. Match against Redis order book
[Kafka] matching worker consumes accepted order event
[Redis] load opposite active orders that can match incoming order

for each matching order:
    if incoming order has no remaining quantity:
        stop matching

    trade quantity = min(incoming remaining quantity, matching remaining quantity)
    trade price = matching order limit price

    create trade result
    reduce incoming order remaining quantity
    reduce matching order remaining quantity

# 5. Record order/trade result
[DB write] save incoming order
[DB write] save updated matching orders
[DB write] save created trades

[Redis] add incoming order to order book if it still has remaining quantity
[Redis] update partially filled matching orders
[Redis] remove fully filled matching orders from order book

# 6. Settle cash and stock
for each created trade:
    [DB read] load affected trader accounts
    [DB read] load affected stock positions

    buyer pays cash
    seller receives cash
    buyer receives stock
    seller delivers stock
    reduce/release reserved cash or stock

    [Redis] seller cash:available:{sellerId} += trade amount
    [Redis] buyer stock:available:{buyerId}:{symbol} += trade quantity

[DB write] save updated trader accounts
[DB write] save updated stock positions
```

## 3. Redis Order Book Rule

- Redis stores only active open orders.
  - Active means waiting to match or partially filled.
  - Fully filled or cancelled orders are removed from Redis.
- DB stores the permanent order history.
  - On startup, Redis can be rebuilt from active DB orders.

## 4. Redis Reserve Rule

- Redis stores only available cash/stock.
  - Buy example: `cash:available:T1 = 1000`, buy order reserves `300`, Redis becomes `700`.
  - Sell example: `stock:available:T1:ACME = 10`, sell order reserves `6`, Redis becomes `4`.
- Kafka event carries the reserved amount.
  - Buy event carries `reservedCash = 300`.
  - Sell event carries `reservedQuantity = 6`.
- DB later records the real columns.
  - Buy not matched: `cash_balance = 1000`, `reserved_cash = 300`.
  - Buy fully matched: `cash_balance = 700`, `reserved_cash = 0`.
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
4. Redis order book structure is not finalized.
   - Need to decide exact keys for bid/ask sorted sets and full order data.
5. Matching ownership is not finalized.
   - Long term direction is one matching worker owns one symbol or one symbol
     partition to avoid two workers matching the same order.
