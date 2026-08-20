# C0-C3 Load-Test Strategy

## 1. Goal

Measure the highest sustainable completed TPS of C0, C1, C2, and C3 under the same workload and test conditions.

## 2. Configurations

| ID | Branch | Configuration | Required services |
|---|---|---|---|
| C0 | `codex/benchmark-c0` | Synchronous processing with MySQL | MySQL |
| C1 | `codex/benchmark-c1` | C0 with Kafka-based sequential processing | MySQL and Kafka |
| C2 | `codex/benchmark-c2` | C1 with in-memory order matching | MySQL and Kafka |
| C3 | `codex/benchmark-c3` | C2 with Redis reservation | MySQL, Kafka, and Redis |

Each configuration must use a recorded Git commit and the same API, seed data, workload, timing, metrics, and SQL checks.

## 3. Test Process

1. Switch to the configuration branch and start its Docker Compose services. Before every measured rate, stop the application, confirm zero Kafka backlog where applicable, reset and seed MySQL, reset Redis where applicable, start a new application process, and wait for the application and Kafka consumer to become ready. Never reuse an application process between measured rates.
2. Run `02-tps-benchmark.js` at `10`, `20`, `40`, `80`, `160` TPS until the first failure:

   ```powershell
   k6 run -e RATE=20 -e DURATION=30s .\_support\load-test\k6\02-tps-benchmark.js
   ```

   For C0, add `-e METRICS_MODE=sync -e DRAIN_SECONDS=0` because every accepted response has already completed processing.

3. Test smaller increments between the last pass and first failure, then repeat the highest passing rate three times. Apply the complete stop, reseed, restart, and readiness sequence before every repetition.
4. After every run, allow the fixed drain period, execute `verify.sql`, and reject any invalid run.
5. Record and commit the verified result on the configuration branch first. Then return to `v3` and copy the finalized result into [C0-C3 Load-Test Results](02-c0-c3-results.md).

## 4. Pass Rules

A rate passes only when no requests or k6 iterations fail and every SQL check passes. For asynchronous C1-C3, at least 98% of accepted orders must complete during the 30-second measurement period, and the fixed drain must leave zero unfinished orders and zero Kafka lag.
