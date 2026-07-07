# Roadmap

> **Table of Contents**
>
> - [1. Overview](#1-overview)
> - [2. Long-Term Direction](#2-long-term-direction)
> - [3. Development Roadmap](#3-development-roadmap)
> - [4. Open Problems](#4-open-problems)

## 1. Overview

This file tracks where the project is going next.

## 2. Long-Term Direction

As the project grows, the architecture should mature step by step instead of
jumping directly into complexity.

Future learning and implementation areas include:

| Status | Area |
|---|---|
| 🟢 | Domain-driven design |
| ⚪ | Hexagonal architecture |
| ⚪ | Order matching and settlement correctness |
| 🟢 | In-memory order book using `TreeMap` or `ConcurrentSkipListMap` for active orders |
| ⚪ | High-concurrency request handling |
| 🟢 | Kafka for event streaming |
| 🟡 | Redis for caching or fast coordination use cases |
| ⚪ | Spring ecosystem tools such as Spring Cloud Gateway |
| ⚪ | Spring Cloud Alibaba tools such as Nacos and Sentinel |
| ⚪ | Infrastructure tools such as Nginx |
| ⚪ | Prometheus and Grafana for metrics and dashboards |
| ⚪ | Loki and Alloy for log collection and search |
| ⚪ | Apache SkyWalking for distributed tracing and observability |
| ⚪ | Seata for distributed transaction learning |
| ⚪ | Spring Batch for batch processing |
| ⚪ | ElasticJob for distributed job scheduling |
| 🟢 | Load testing and performance profiling |
| ⚪ | JVM optimization |

The important idea is progression: begin with controller-service-repository,
understand the pain points, then introduce each advanced concept when it solves a
real problem in the trading platform.

## 3. Development Roadmap

Status:

- 🟢 completed
- 🟡 in progress or revisiting
- ⚪ not started

### 🟢 Phase 1: Controller-Service-Repository Baseline

> Build the first limit order API using the familiar Spring Boot
> controller-service-repository structure.

Status: completed at commit `505bb9a`.

### 🟢 Phase 2: First Domain-Driven Design Pass

> Reorganize the first limit order flow into a clearer DDD-style structure.

- `presentation`: controller and API DTOs.
- `application`: order placement use case and application-level loading/saving
  helpers.
- `domain`: order, order book, portfolio, settlement, trades, accounts,
  positions, and repository contracts.
- `infrastructure`: JPA entities, DAOs, custom queries, and repository
  implementations.

Status: completed for now at commit `4ad67a7`.

### 🟢 Phase 3: Order Flow and Simple Domain Model Review

> Re-understand the full buy/sell limit order flow and simplify the code so the
> use case is easier to follow.

- Documented the order flow as five stages: receive and validate order, check
  and reserve asset, match against order book, save result, and return response.
- Reworked the use case so the five stages are visible directly in the code.
- Kept a simple DDD style with domain objects such as `Order`, `Trade`,
  `TraderAccount`, and `StockPosition`.
- Removed confusing wrapper objects while learning, such as `Portfolio`,
  `OrderBook`, and `MatchResult`.
- Current limitation: the use case still contains a lot of workflow/business
  logic. This is acceptable for now while the domain model is still being
  understood.

Status: completed for now at commit `02caa20`.

### 🟢 Phase 4: Baseline Load Testing

> Add a repeatable k6 load test and verify whether the current synchronous DB
> flow keeps cash and stock totals correct.

- Added seed data, k6 script, and verification SQL.
- Ran baseline test and found the concurrency bug:
  [result](../load-test/results/01-sync-db-buy-orders.md).

Status: completed for now at commit `e38d72a`.

### 🟢 Phase 5: Kafka-Based Order Processing

> Move order matching behind Kafka so matching can be processed sequentially per
> symbol before adding Redis reservation.

- API publishes order events. Done.
- Kafka consumer performs matching. Done.
- Rerun the same k6 verification. Done:
  [result](../load-test/results/02-kafka-mysql-buy-orders.md).

Sub-phases:

- 5.1 Add Kafka infrastructure: docker-compose, Spring dependency, application config, and one order topic. Done.
- 5.2 Publish order event: HTTP request publishes an order event and returns `202`. Done.
- 5.3 Consume order event: Kafka consumer calls the processing use case for matching and settlement. Done.
- 5.4 Verify result: rerun the same seed, k6 script, and verification SQL. Done.

Status: completed.

### 🟢 Phase 6: In-Memory Order Book Matching

> Rework matching so the active order book is held in memory instead of using
> database queries as the matching engine.

Current problem:

- The Kafka consumer now processes orders sequentially, but matching still loads
  active opposite-side orders from the database.
- That is acceptable for proving the Kafka flow, but it is not the real order
  book design.

Target direction:

- Build an in-memory order book for active orders. Done.
- Keep buy and sell sides ordered by price-time priority. Done.
- Use the database as durable history and recovery source, not as the live
  matching data structure. Done.
- Persist order/trade/account/position results after matching. Done.

Status: completed at commit `bbdc0bb`.

### 🟡 Phase 7: Redis-Based Reservation

> Add Redis as the fast reservation layer for cash and stock before accepted
> orders are published to Kafka.

1. Infra: add Redis Docker/config/dependency. Done.
2. Redis reservation: store, check, and deduct available cash/stock.
3. Flow change: reserve in Redis before Kafka publish, and carry reserved amount
   in the event.
4. DB sync: consumer updates MySQL so DB eventually matches Redis.

## 4. Open Problems

This section tracks miscellaneous problems discovered while building the system.

- Kafka consumer idempotency: if an order event is processed but the Kafka
  offset is not committed before a crash or restart, Kafka may redeliver the
  same event. The consumer should avoid creating duplicate trades or applying
  settlement twice for the same `orderId`.
