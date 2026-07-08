# Roadmap

> **Table of Contents**
>
> - [1. Overview](#1-overview)
> - [2. Long-Term Direction](#2-long-term-direction)
> - [3. Development Roadmap](#3-development-roadmap)
> - [4. Related Docs](#4-related-docs)
> - [5. Open Problems](#5-open-problems)

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
| 🟢 | Redis for caching or fast coordination use cases |
| ⚪ | MyBatis or MyBatis-Plus for optimized MySQL access |
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

### 🟢 Phase 7: Redis-Based Reservation

> Add Redis as the fast reservation layer for cash and stock before accepted
> orders are published to Kafka.

1. Infra: add Redis Docker/config/dependency. Done.
2. Redis reservation: store, check, and deduct available cash/stock. Done.
3. Flow change: reserve in Redis before Kafka publish, and carry reserved amount
   in the event. Done.
4. DB sync: consumer updates MySQL so DB eventually matches Redis. Done.

Status: completed at commit `1e02bfd`.

### 🟢 Phase 8: Split Into Exchange, Finance, and Match Services

> Split the current app into small services while keeping the same order flow
> and infrastructure.

Flow:

`exchange-service -> finance-service reserve -> Kafka -> match-service -> trade event -> finance-service settle`

- `exchange-service`: receive order requests and publish accepted orders.
- `finance-service`: reserve cash/stock in Redis and settle trades.
- `match-service`: consume order events, keep the in-memory order book, and match
  trades.

General steps:

1. Create multi-module Gradle structure: `common`, `exchange-service`,
   `finance-service`, and `match-service`. Done.
2. Move shared events and DTOs into `common`. Done.
3. Move order API and Kafka publishing into `exchange-service`. Done.
4. Move Redis reservation and finance APIs into `finance-service`. Done.
5. Add OpenFeign so `exchange-service` can call `finance-service`. Done.
6. Move Kafka consumer and in-memory order book matching into `match-service`. Done.
7. Publish matched trade events and settle them in `finance-service`. Done.
8. Start all services and rerun tests/k6. Done.

Status: completed.

### ⚪ Phase 9: End-to-End TPS Measurement

> Measure real throughput for the full order flow, not only HTTP acceptance.

Main metric:

`end-to-end TPS = completed settlements per second`

Stage metrics:

- Intake TPS: orders accepted by `exchange-service`.
- Reservation TPS: cash/stock reservations completed by `finance-service`.
- Match TPS: orders/trades processed by `match-service`.
- Settlement TPS: trades settled by `finance-service`.

General steps:

1. Add metrics for intake, reservation, matching, and settlement.
2. Update k6/reporting to separate API TPS from end-to-end TPS.
3. Run local benchmark first.
4. Run server benchmark later for resume-quality numbers.

Status: not started.

## 4. Related Docs

- [Performance / TPS Tracking](02-performance.md)

## 5. Open Problems

This section tracks miscellaneous problems discovered while building the system.

- Kafka consumer idempotency: if an order event is processed but the Kafka
  offset is not committed before a crash or restart, Kafka may redeliver the
  same event. The consumer should avoid creating duplicate trades or applying
  settlement twice for the same `orderId`.
