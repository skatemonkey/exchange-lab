# Exchange Lab

> **Table of Contents**
>
> - [1. Overview](#1-overview)
> - [2. Long-Term Direction](#2-long-term-direction)
> - [3. Development Roadmap](#3-development-roadmap)

## 1. Overview

Exchange Lab is a backend engineering practice project for building a stock
trading platform from the ground up.

The goal is to build a robust backend that can eventually handle serious
concurrency, from thousands of requests per second toward much larger traffic
loads. The project starts small, but the direction is intentionally ambitious:
learn how a trading backend can grow from a familiar Spring Boot application
into a system that uses stronger domain modeling, distributed systems patterns,
streaming, caching, observability, and performance tuning.

The first business capability is intentionally narrow: support limit buy orders
and limit sell orders. A user should be able to submit a limit order, the system
should decide whether it can be accepted, and later the platform should match
compatible buy and sell orders into trades.

## 2. Long-Term Direction

As the project grows, the architecture should mature step by step instead of
jumping directly into complexity.

Future learning and implementation areas include:

| Status | Area |
|---|---|
| 🟢 | Domain-driven design |
| ⚪ | Hexagonal architecture |
| 🟡 | Order matching and settlement correctness |
| ⚪ | High-concurrency request handling |
| ⚪ | Kafka for event streaming |
| ⚪ | Redis for caching or fast coordination use cases |
| 🟢 | Spring ecosystem tools such as Spring Cloud Gateway |
| 🟢 | Spring Cloud Alibaba tools such as Nacos and Sentinel |
| ⚪ | Infrastructure tools such as Nginx |
| 🟢 | Prometheus and Grafana for metrics and dashboards |
| ⚪ | Loki and Alloy for log collection and search |
| 🟢 | Apache SkyWalking for distributed tracing and observability |
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

Phase 1 covers one vertical slice: accept limit buy and limit sell requests,
validate the basic input, store the order, and return a clear response.

Status: completed at commit `505bb9a`.

The simple baseline model is now in place: controller, service, repository,
entities, Postgres Docker setup, and Flyway schema migration. The next phase is
to rewrite and grow this code in a domain-driven design style.

### 🟢 Phase 2: First Domain-Driven Design Pass

> Reorganize the first limit order flow into a clearer DDD-style structure.

Status: completed for now at commit `4ad67a7`.

Phase 2 converts the phase 1 controller-service-repository baseline into a first
DDD-oriented version. This is a learning checkpoint, not a final architecture.

Current phase 2 results include:

- `presentation`: controller and API DTOs.
- `application`: order placement use case and application-level loading/saving
  helpers.
- `domain`: order, order book, portfolio, settlement, trades, accounts,
  positions, and repository contracts.
- `infrastructure`: JPA entities, DAOs, custom queries, and repository
  implementations.

### 🟢 Phase 3: Spring Cloud Alibaba Infrastructure Foundation

> Add common Spring Cloud Alibaba infrastructure around the monolith first.

Phase 3 keeps the project as a monolith, but starts introducing infrastructure
patterns that are useful before splitting into multiple services.

Status: completed for now at commit `1db0958`.

Current phase 3 results include:

1. Nacos service discovery for `exchange-app` and `exchange-gateway`.
2. Sentinel dashboard integration for API protection learning.
3. Spring Cloud Gateway as a separate module and entry point.
4. Gateway routing from `/api/**` to the `exchange-lab` backend service.

The project is still a modular monolith plus gateway. The backend has not been
split into multiple business microservices yet.

### 🟢 Phase 4: Metrics And Dashboards

> Add a whole-system overview using Prometheus and Grafana.

Phase 4 focuses on metrics before tracing. The goal is to stop checking each
tool one by one and start seeing the system from one dashboard.

Status: completed for now.

Current phase 4 results include:

1. Prometheus for collecting metrics.
2. Grafana for dashboards.
3. Actuator metrics from `exchange-app` and `exchange-gateway`.
4. Basic dashboard panels for service health, request rate, errors, latency,
   CPU, and memory.

### 🟢 Phase 5: Distributed Tracing

> Add request tracing and service topology with Apache SkyWalking.

Phase 5 focuses on tracing after the metrics dashboard exists.

Status: completed for now.

Current phase 5 results include:

1. SkyWalking OAP backend, UI, and BanyanDB storage.
2. Micrometer tracing from `exchange-app` and `exchange-gateway`, exporting
   Zipkin-format traces into SkyWalking OAP.
3. Zipkin-compatible collection and query ports exposed through SkyWalking OAP.
4. Optional `bootRun` tracing switch for `exchange-app` and
   `exchange-gateway`.
5. Trace requests from gateway to backend.
6. Use traces to inspect slow requests and service relationships.
