# Order Flow

## 1. Overview

The current `POST /api/orders/limit` API is synchronous, but the target high-concurrency flow is categorized into six stages:

1. Create order.
2. Reserve cash/stock (Redis).
3. Queue order (Kafka).
4. Match against order book.
5. Record order/trade result.
6. Settle cash and stock.

## 2. Pseudocode Flow

Marker: `[DB read]` reads from database, `[DB write]` writes to database, `[Redis]` uses Redis, `[Kafka]` uses Kafka.

```text
# 1. Create order
receive limit order request
validate trader, symbol, side, price, and quantity
create incoming order

# 2. Reserve cash/stock (Redis)
if incoming order is BUY:
    [Redis] atomically reserve cash = limit price * quantity

if incoming order is SELL:
    [Redis] atomically reserve stock = quantity

# 3. Queue order (Kafka)
[Kafka] publish accepted order event

# 4. Match against order book
[Kafka] matching worker consumes accepted order event
[DB read] load opposite orders that can match incoming order

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

## 3. Known Problems

1. Current Stage 2 saves too early.
   - The current code reserves cash/stock and writes it immediately.
   - If the order matches, the same account or stock position may be loaded and
     saved again during settlement.
   - Cleaner direction: reserve through Redis first, then write final state to
     DB after matching and settlement.
2. Matching creates trade results before recording them.
   - This is acceptable, but `Record order/trade result` should mean saving the
     result, not creating it from scratch.
3. Matching currently loads all matchable opposite orders.
   - This works for learning.
   - Later, it can be wasteful if many orders match but only a few are needed.
4. Concurrency is not solved yet.
   - Two requests can still try to match the same resting order at the same time.
   - This is the biggest real correctness problem for high concurrency.
5. The whole API is synchronous.
   - This is fine for the current learning phase.
   - The target flow moves matching work behind Kafka so the API only validates,
     pre-reserves, queues, and returns.
