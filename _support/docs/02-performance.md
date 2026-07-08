# Performance / TPS Tracking

> **Table of Contents**
>
> - [1. Overview](#1-overview)
> - [2. TPS Definitions](#2-tps-definitions)
> - [3. Benchmark Results](#3-benchmark-results)

## 1. Overview

This file tracks measured TPS results from this point forward.

## 2. TPS Definitions

| Metric | Meaning |
|---|---|
| End-to-end TPS | Completed settlements per second. |
| Intake TPS | Orders accepted by `exchange-service` per second. |
| Reservation TPS | Cash/stock reservations completed by `finance-service` per second. |
| Match TPS | Orders/trades processed by `match-service` per second. |
| Settlement TPS | Trades settled by `finance-service` per second. |

## 3. Benchmark Results

| Version | Date | Target TPS | End-to-End TPS | Intake TPS | Reservation TPS | Match TPS | Settlement TPS | p95 Latency | Kafka Lag | Error Rate | Result | Notes |
|---|---|---:|---:|---:|---:|---:|---:|---:|---|---:|---|---|
| v1 | 2026-07-08 | 100 | 100.03 | 100.03 | 100.03 | 100.03 | 100.03 | 18.45ms | 0 | 0.00% | Passed | Target-limited baseline. 3001/3001 accepted. SQL correctness passed. |
| v2 | 2026-07-08 | 110 | 110.03 | 110.03 | 110.03 | 110.03 | 110.03 | 19.19ms | 0 | 0.00% | Passed | Clean raised baseline. 3301/3301 accepted. SQL correctness passed. |
| v3 | 2026-07-08 | 150 | 111.17 | 150.03 | 150.03 | 111.20 | 111.17 | 18.80ms | Not captured; backlog visible from counters | 0.00% | Fell behind | HTTP and reservation kept up, but `match-service` did not finish all orders within the drain window. |
| v4 | 2026-07-08 | 200 | 110.60 | 200.00 | 200.00 | 110.60 | 110.60 | 18.63ms | `orders.submitted`: 1988, `trades.matched`: 2 | 0.00% | Fell behind | Intake/reservation stayed clean, but `match-service` lag grew. |
| v5 | 2026-07-08 | 500 | ~226.73 | 495.67 | 495.67 | ~226.77 | ~226.73 | 116.84ms | `orders.submitted`: 8056, `trades.matched`: 2 | 0.06% | Failed stress run | 14870 accepted, 9 failed checks, 122 dropped iterations. k6 teardown timed out before printing the normal counter report. |
