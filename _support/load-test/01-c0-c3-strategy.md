# C0-C3 Load-Test Strategy

## 1. Goal

Measure the highest sustainable completed TPS of C0, C1, C2, and C3 under the same workload and test conditions.

## 2. Configurations

| ID | Configuration |
|---|---|
| C0 | Synchronous processing with MySQL |
| C1 | C0 with Kafka-based sequential processing |
| C2 | C1 with in-memory order matching |
| C3 | C2 with Redis reservation |

Each configuration must use a recorded Git commit and the same API, seed data, workload, timing, metrics, and SQL checks.

## 3. Test Process

1. Reset the required infrastructure and load the controlled seed data.
2. Start the selected configuration, check its health, and complete the fixed warm-up period.
3. Run the k6 workload at increasing target rates. Measure completed TPS before the drain period and unfinished work after draining.
4. Run the SQL verification and record the result immediately in [C0-C3 Load-Test Results](02-c0-c3-results.md).

Start at a low rate, increase the rate until the first failure, and then test smaller increments between the last pass and first failure. Repeat the highest passing rate three times.

## 4. Pass Rules

A rate passes only when completed TPS keeps pace with accepted TPS, no requests or k6 iterations fail, unfinished work does not continue growing, and every SQL check passes.
