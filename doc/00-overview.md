# Exchange Lab

> **Table of Contents**
>
> - [1. Overview](#1-overview)
> - [2. Project Details](#2-project-details)

## 1. Overview

Exchange Lab is a backend engineering practice project for building a stock
trading platform from the ground up.

The first business capability is intentionally narrow: support limit buy orders
and limit sell orders. A user should be able to submit a limit order, the system
should decide whether it can be accepted, and later the platform should match
compatible buy and sell orders into trades.

The long-term goal is to grow this from a simple Spring Boot backend into a
system that can teach stronger domain modeling, concurrency, messaging,
observability, and performance tuning.

## 2. Project Details

- Current API: `POST /api/orders/limit`
  - Allows users to submit buy or sell limit orders: [Order Flow](03-order-flow.md).
- Current tables:
  - `trader_accounts`: trader cash balance and reserved cash.
  - `stock_positions`: trader stock quantity and reserved stock.
  - `orders`: buy/sell limit orders and order status.
  - `trades`: executed trade records.
