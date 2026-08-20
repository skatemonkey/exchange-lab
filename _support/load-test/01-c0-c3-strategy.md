# C0-C3 Load-Test Strategy

## 1. Goal

Measure the highest sustainable completed TPS of C0, C1, C2, and C3 under the same workload and test conditions.

## 2. Configurations

| ID | Branch | Configuration | Application processes | Infrastructure |
|---|---|---|---|---|
| C0 | `codex/benchmark-c0` | Synchronous processing with MySQL | Single baseline application | MySQL |
| C1 | `codex/benchmark-c1` | Kafka pipeline with database matching | Exchange, match, and finance services | MySQL and Kafka |
| C2 | `codex/benchmark-c2` | C1 with in-memory order matching | Exchange, match, and finance services | MySQL and Kafka |
| C3 | `v3` | C2 with Redis reservation | Exchange, match, and finance services | MySQL, Kafka, and Redis |

Each configuration must use a recorded Git commit and the same API, seed data, workload, timing, metrics, and SQL checks.

## 3. Test Process

1. Switch to the configuration branch and start its Docker Compose infrastructure. Keep MySQL and Kafka running between measured rates. Before every rate, stop every application process and wait until both Kafka consumer groups have no active members. Confirm zero Kafka lag, reset and seed MySQL, and flush Redis for C3.
2. Start the single C0 application or, for C1-C3, start `finance-service`, `match-service`, and `exchange-service`. C3 preloads Redis during finance startup, while C2 and C3 rebuild the in-memory order book during match startup. For C1-C3, health endpoints alone are not sufficient: wait until both Kafka consumer groups have assigned partitions and zero lag, then allow the fixed 10-second readiness delay before k6. Never reuse any application process between measured rates.
3. Run `02-tps-benchmark.js` at `10`, `20`, `40`, `80`, `160` TPS until the first failure:

   ```powershell
   k6 run -e RATE=20 -e DURATION=30s .\_support\load-test\k6\02-tps-benchmark.js
   ```

   For C0, add `-e METRICS_MODE=sync -e DRAIN_SECONDS=0` because every accepted response has already completed processing.

4. Test smaller increments between the last pass and first failure, then repeat the highest passing rate twice. Apply the complete stop, reset, restart, and readiness sequence before every repetition.
5. After every run, allow the fixed drain period and execute `verify.sql`. For C3, also execute `verify-redis.ps1` to compare Redis availability with MySQL. Reject any invalid run.
6. Record and commit the verified result on the configuration branch first. Then return to `v3` and copy the finalized result into [C0-C3 Load-Test Results](02-c0-c3-results.md).

For C3 on `v3`, the automated runner applies this complete reset, restart, consumer-readiness, test, verification, and shutdown cycle:

```powershell
.\_support\load-test\run-c3-rate.ps1 -Rate 55 -Run 1
```

## 4. Pass Rules

A rate passes only when no requests or k6 iterations fail and every SQL check passes. C3 must also pass every Redis consistency check. For asynchronous C1-C3, at least 98% of accepted orders must settle during the 30-second measurement period, and the fixed drain must leave zero unfinished orders and zero lag in both Kafka consumer groups.
