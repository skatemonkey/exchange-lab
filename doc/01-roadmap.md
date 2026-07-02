# Roadmap

> **Table of Contents**
>
> - [1. Overview](#1-overview)
> - [2. Long-Term Direction](#2-long-term-direction)
> - [3. Development Roadmap](#3-development-roadmap)

## 1. Overview

This file tracks where the project is going next.

## 2. Long-Term Direction

As the project grows, the architecture should mature step by step instead of
jumping directly into complexity.

Future learning and implementation areas include:

| Status | Area |
|---|---|
| 🟡 | Domain-driven design |
| ⚪ | Hexagonal architecture |
| ⚪ | Order matching and settlement correctness |
| ⚪ | High-concurrency request handling |
| ⚪ | Kafka for event streaming |
| ⚪ | Redis for caching or fast coordination use cases |
| ⚪ | Spring ecosystem tools such as Spring Cloud Gateway |
| ⚪ | Spring Cloud Alibaba tools such as Nacos and Sentinel |
| ⚪ | Infrastructure tools such as Nginx |
| ⚪ | Prometheus and Grafana for metrics and dashboards |
| ⚪ | Loki and Alloy for log collection and search |
| ⚪ | Apache SkyWalking for distributed tracing and observability |
| ⚪ | Seata for distributed transaction learning |
| ⚪ | Spring Batch for batch processing |
| ⚪ | ElasticJob for distributed job scheduling |
| ⚪ | Load testing and performance profiling |
| ⚪ | JVM optimization |

The important idea is progression: begin with controller-service-repository,
understand the pain points, then introduce each advanced concept when it solves a
real problem in the trading platform.

## 3. Development Roadmap

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
