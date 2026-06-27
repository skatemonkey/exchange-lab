# Exchange Lab

> **Table of Contents**
>
> - [1. Overview](#1-overview)
> - [2. Starting Point](#2-starting-point)
> - [3. Long-Term Direction](#3-long-term-direction)

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

## 2. Starting Point

The first implementation should use the structure that is already familiar:

- Controller
- Service
- Repository

At the beginning, the service layer can hold most of the business logic. This is
deliberate. The project should start from a simple three-layer Spring Boot
baseline, then evolve only when the code starts to need stronger boundaries.

Initial scope:

- One order API.
- Limit buy and limit sell only.
- Simple request validation.
- Simple service-layer business logic.
- Simple persistence when the database is introduced.
- No Kafka, Redis, gateway, service discovery, or advanced architecture on day
  one.

## 3. Long-Term Direction

As the project grows, the architecture should mature step by step instead of
jumping directly into complexity.

Future learning and implementation areas include:

- Domain-driven design.
- Hexagonal architecture.
- Order matching and settlement correctness.
- High-concurrency request handling.
- Kafka for event streaming.
- Redis for caching or fast coordination use cases.
- Spring ecosystem tools such as Spring Cloud Gateway.
- Infrastructure tools such as Nginx and Nacos.
- Observability, metrics, tracing, and dashboards.
- Load testing and performance profiling.
- JVM optimization.

The important idea is progression: begin with controller-service-repository,
understand the pain points, then introduce each advanced concept when it solves a
real problem in the trading platform.
