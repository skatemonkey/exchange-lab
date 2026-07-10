# Exchange Lab: High-Concurrency Backend Engineering

> **Ultimate goal:** Build and prove a transaction-processing backend that
> sustains high concurrency while preserving business correctness, predictable
> latency, and recoverability.

> **Table of Contents**
>
> - [1. Purpose: High Concurrency](#1-purpose-high-concurrency)
> - [2. Current Scope](#2-current-scope)
> - [3. Current System](#3-current-system)

## 1. Purpose: High Concurrency

Exchange Lab is a backend engineering practice project that uses a stock
trading platform to learn how to build a backend that can sustain high
concurrency. The exchange is the learning vehicle, while the engineering goal
applies to other transaction-processing systems as well.

High concurrency is not a secondary optimization added after the business
features are complete; it is the ultimate purpose of this project. Architecture,
infrastructure, and performance decisions should help the system process more
concurrent transactions safely and predictably.

Success means increasing sustainable end-to-end throughput while keeping data
correct, latency predictable, backlog controlled, and failures recoverable.
Progress should be demonstrated through repeatable load tests and measurements.

## 2. Current Scope

The first business capability is intentionally narrow: support limit buy orders
and limit sell orders. A user should be able to submit a limit order, the system
should decide whether it can be accepted, and later the platform should match
compatible buy and sell orders into trades.

## 3. Current System

- Current API: `POST /api/orders/limit`
  - Allows users to submit buy or sell limit orders: [Order Flow](03-order-flow.md).
- Current services:
  - `exchange-service`: accepts limit orders and publishes accepted orders.
  - `finance-service`: reserves and settles trader cash and stock.
  - `match-service`: maintains in-memory order books and matches trades.
- Current tables:
  - `trader_accounts`: trader cash balance and reserved cash.
    - Columns: `trader_id`, `cash_balance`, `reserved_cash`
  - `stock_positions`: trader stock quantity and reserved stock.
    - Columns: `position_id`, `trader_id`, `symbol`, `quantity`,
      `reserved_quantity`
  - `orders`: buy/sell limit orders and order status.
    - Columns: `order_id`, `trader_id`, `symbol`, `side`, `limit_price`,
      `quantity`, `remaining_quantity`, `status`, `created_at`
  - `trades`: executed trade records.
    - Columns: `trade_id`, `buy_order_id`, `sell_order_id`,
      `buyer_trader_id`, `seller_trader_id`, `symbol`, `price`, `quantity`,
      `created_at`
