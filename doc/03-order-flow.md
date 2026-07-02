# Order Flow

## 1. Overview

The current `POST /api/orders/limit` API is synchronous and categorized into five stages:

1. Create order.
2. Reserve cash/stock.
3. Match against order book.
4. Record order/trade result.
5. Settle cash and stock.
