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

| Version | Architecture | Environment | k6 Config | End-to-End TPS | Intake TPS | Reservation TPS | Match TPS | Settlement TPS | p95 Latency | Kafka Lag | Error Rate | Notes |
|---|---|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---|
| v1 | Split services with Redis reservation, Kafka, and in-memory matching | TBD | TBD | TBD | TBD | TBD | TBD | TBD | TBD | TBD | TBD | First measured baseline. |
