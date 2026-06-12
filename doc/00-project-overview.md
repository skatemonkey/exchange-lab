# Exchange Lab

> **Table of Contents**
>
> - [1. Overview](#1-overview)
> - [2. Things I Want To Explore](#2-things-i-want-to-explore)
> - [3. Success Metrics](#3-success-metrics)
> - [4. Project Phases](#4-project-phases)

## 1. Overview

Exchange Lab is an exchange simulator for practicing trading platform engineering with Java, Spring Boot, Kafka, and distributed backend systems. It uses domain-driven design to model how orders are placed, matched, turned into trades, and reflected in balances, positions, and market data.

## 2. Things I Want To Explore

The project should help me explore the core logic of an exchange:

1. Basic trading behavior: users can buy and sell an asset.
2. Order book behavior: buy and sell orders are stored, prioritized, and matched.
3. Order matching: incoming orders can match against existing orders and produce trades.
4. Market making: understand how liquidity can be provided through buy and sell quotes.
5. Simulated market behavior: an automatic process keeps generating buy and sell activity so prices can move over time.
   - The simulation should start with a fixed market state, such as a set number of traders, total cash, and total asset supply, so trades move money/assets between users instead of creating them randomly.
6. Live price movement: users can observe the simulated asset price moving as orders are matched.
7. Performance visualization: use local dashboards to observe both technical performance and market simulation behavior.
   - Technical performance: server load, API latency, throughput, Kafka lag, memory usage, error rate, and recovery after traffic spikes.
   - Market performance: current price, price movement, trading volume, active traders, order book state, and executed trades.
8. Regular stock/trading concepts: orders, trades, prices, quantities, balances, and positions.

## 3. Success Metrics

The project should eventually demonstrate:

1. High concurrency: many users can place simulated orders at the same time.
2. High-throughput streaming: many order, trade, and market-data events can flow through Kafka.
3. Event-driven backend: services communicate through events instead of only direct REST calls.
4. Trading platform domain: the project models orders, trades, balances, market data, and order lifecycle.
5. Reliability: the system can handle retries, service restarts, duplicate events, and consumer lag.

## 4. Project Phases

### 4.1 Phase 1: Basic Limit Order Matching

Phase 1 focuses on the smallest useful market engine behavior: an automated simulation submits buy and sell limit orders for seeded traders, the system matches them through an order book, executes trades, and updates account state.

Concrete requirements:

- Simulation state
  - Start with seeded trader accounts.
  - Track trader cash balances.
  - Track trader stock positions.
- Order generation
  - Run an automated order simulation.
  - Generate buy and sell limit orders for multiple traders.
- Market engine
  - Store unmatched orders in an order book.
  - Match orders when buy price crosses sell price.
  - Create trade records for matched orders.
- Settlement
  - Update cash balances and stock positions after trades.
  - Track order status and filled quantity.
