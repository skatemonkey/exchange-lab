# Order Flow

## 1. Overview

The current `POST /api/orders/limit` API is synchronous and categorized into five stages:

1. Create order.
2. Reserve cash/stock.
3. Match against order book.
4. Record order/trade result.
5. Settle cash and stock.

## 2. Pseudocode Flow

Marker: `[DB read]` reads from database, `[DB write]` writes to database.

```text
# 1. Create order
receive limit order request
validate trader, symbol, side, price, and quantity
create incoming order

# 2. Reserve cash/stock
if incoming order is BUY:
    [DB read] load buyer trader account
    reserve cash = limit price * quantity
    [DB write] save buyer trader account

if incoming order is SELL:
    [DB read] load seller stock position
    reserve stock = quantity
    [DB write] save seller stock position

# 3. Match against order book
[DB read] load opposite orders that can match incoming order

for each matching order:
    if incoming order has no remaining quantity:
        stop matching

    trade quantity = min(incoming remaining quantity, matching remaining quantity)
    trade price = matching order limit price

    create trade result
    reduce incoming order remaining quantity
    reduce matching order remaining quantity

# 4. Record order/trade result
[DB write] save incoming order
[DB write] save updated matching orders
[DB write] save created trades

# 5. Settle cash and stock
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
