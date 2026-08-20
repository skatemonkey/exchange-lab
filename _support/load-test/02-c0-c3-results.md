# C0-C3 Load-Test Results

> Test method: [C0-C3 Load-Test Strategy](01-c0-c3-strategy.md)

## 1. Test Environment

| Field | Value |
|---|---|
| Test date | C0: 19 August 2026; C1: 20 August 2026 |
| Machine and operating system | Windows 11 Enterprise 10.0.26200; AMD Ryzen 7 7800X3D; 16 logical processors; 31.1 GB RAM |
| Java and JVM settings | OpenJDK 26.0.1; default JVM settings |
| k6 version | 2.2.0 |
| Warm-up period | 10 seconds at 5 TPS; discarded, then database reseeded |
| Measurement period | 30 seconds per run |
| Drain period | C0: 0 seconds; asynchronous C1: 5 seconds |
| Completion measurement | C0: synchronous accepted responses; C1: `exchange.orders.completed` after successful Kafka processing |
| k6 script | `k6/02-tps-benchmark.js` |
| Seed and verification files | `seed.sql` and `verify.sql` |

## 2. Configuration Versions

| Configuration | Git commit | Main difference | Status |
|---|---|---|---|
| C0 | `567b63b` | Synchronous processing with MySQL | Tested |
| C1 | `ad15ba4` | Adds Kafka-based sequential processing | Tested |
| C2 | Pending | Adds in-memory order matching | Pending |
| C3 | Pending | Adds Redis reservation | Pending |

## 3. C0 Results

### Rate Search

The initial rates located the limit, and smaller steps narrowed it. The 30 TPS run failed its SQL checks, so 25 TPS became the highest candidate.

| Test | Target TPS | Accepted TPS | Completed TPS | Completion ratio | p95 latency | Completed during drain | Unfinished after drain | Kafka lag | Errors / drops | SQL checks | Decision |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| Initial rate | 10 | 10.03 | 10.03 | 100.00% | 22.84 ms | 0 | 0 | N/A | 0 / 0 | 5/5 pass | Pass |
| Initial rate | 20 | 20.00 | 20.00 | 100.00% | 22.01 ms | 0 | 0 | N/A | 0 / 0 | 5/5 pass | Pass |
| Initial rate | 40 | 39.90 | 39.90 | 100.00% | 21.75 ms | 0 | 0 | N/A | 4 / 0 | 3/5 pass | Fail |
| Smaller step | 30 | 30.00 | 30.00 | 100.00% | 19.78 ms | 0 | 0 | N/A | 0 / 0 | 3/5 pass | Fail |
| Smaller step | 25 | 25.00 | 25.00 | 100.00% | 19.85 ms | 0 | 0 | N/A | 0 / 0 | 5/5 pass | Pass |

### Final TPS Confirmation

| Test | Target TPS | Accepted TPS | Completed TPS | Completion ratio | p95 latency | Completed during drain | Unfinished after drain | Kafka lag | Errors / drops | SQL checks | Decision |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| Confirmation 1 | 25 | 25.00 | 25.00 | 100.00% | 21.01 ms | 0 | 0 | N/A | 0 / 0 | 5/5 pass | Pass |
| Confirmation 2 | 25 | 25.03 | 25.03 | 100.00% | 20.49 ms | 0 | 0 | N/A | 0 / 0 | 5/5 pass | Pass |
| Confirmation 3 | 25 | 25.03 | 25.03 | 100.00% | 22.07 ms | 0 | 0 | N/A | 0 / 0 | 5/5 pass | Pass |

### Conclusion

The verified C0 score is **25 TPS**. The three confirmation runs produced a median completed throughput of **25.03 TPS** and a median p95 latency of **21.01 ms**. Because C0 is synchronous, each accepted response represents completed processing and no drain backlog exists.

## 4. C1 Results

### Rate Search

The initial rates located the processing limit, and smaller steps narrowed it. The first 65 TPS run reached the minimum 98% completion rule, but its confirmation fell below that rule. Therefore, 65 TPS was rejected and 60 TPS became the highest verified target.

| Test | Target TPS | Accepted TPS | Completed TPS | Completion ratio | p95 latency | Completed during drain | Unfinished after drain | Kafka lag | Errors / drops | SQL checks | Decision |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| Initial rate | 10 | 10.00 | 10.00 | 100.00% | 8.14 ms | 0 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Initial rate | 20 | 20.03 | 20.00 | 99.83% | 7.48 ms | 1 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Initial rate | 40 | 40.03 | 40.00 | 99.92% | 6.75 ms | 1 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Initial rate | 80 | 80.00 | 64.27 | 80.33% | 6.40 ms | 327 | 145 | 205 | 0 / 0 | 5/5 pass | Fail |
| Smaller step | 60 | 60.03 | 59.80 | 99.61% | 6.38 ms | 7 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Smaller step | 70 | 70.00 | 64.37 | 91.95% | 6.33 ms | 169 | 0 | 0 | 0 / 0 | 5/5 pass | Fail |
| Smaller step | 65 | 65.00 | 63.70 | 98.00% | 6.35 ms | 39 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| 65 TPS confirmation | 65 | 65.00 | 63.47 | 97.64% | 6.40 ms | 46 | 0 | 0 | 0 / 0 | 5/5 pass | Fail |

### Final TPS Confirmation

| Test | Target TPS | Accepted TPS | Completed TPS | Completion ratio | p95 latency | Completed during drain | Unfinished after drain | Kafka lag | Errors / drops | SQL checks | Decision |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| Confirmation 1 | 60 | 60.00 | 59.80 | 99.67% | 6.38 ms | 6 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Confirmation 2 | 60 | 60.03 | 59.80 | 99.61% | 6.40 ms | 7 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Confirmation 3 | 60 | 60.00 | 59.73 | 99.56% | 6.37 ms | 8 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |

### Conclusion

The verified C1 score is **60 TPS**. The three confirmation runs produced a median completed throughput of **59.80 TPS** and a median p95 API latency of **6.38 ms**.

## 5. C2 Results

| Target TPS | Accepted TPS | Completed TPS | p95 latency | Errors / dropped iterations | Unfinished work after drain | SQL checks | Decision |
|---:|---:|---:|---:|---|---|---|---|
| Pending | Pending | Pending | Pending | Pending | Pending | Pending | Pending |

## 6. C3 Results

| Target TPS | Accepted TPS | Completed TPS | p95 latency | Errors / dropped iterations | Unfinished work after drain | SQL checks | Decision |
|---:|---:|---:|---:|---|---|---|---|
| Pending | Pending | Pending | Pending | Pending | Pending | Pending | Pending |

## 7. Overall Comparison

| Configuration | Highest passing target TPS | Median completed TPS | Change from previous configuration | Finding |
|---|---:|---:|---:|---|
| C0 | 25 | 25.03 | Baseline | Verified at 5-TPS boundary resolution |
| C1 | 60 | 59.80 | +140% from C0 target TPS | Kafka decoupled fast intake from sequential database processing |
| C2 | Pending | Pending | Pending | Pending |
| C3 | Pending | Pending | Pending | Pending |

## 8. Invalid Runs and Notes

| Configuration | Target TPS | Problem | Action |
|---|---:|---|---|
| C0 | 5 TPS warm-up | Two request failures caused by MySQL deadlocks during cold start | Discarded as planned and reseeded before formal testing |
| C0 | 40 TPS | Four requests failed and two conservation checks failed | Marked as failed; narrowed the boundary |
| C0 | 30 TPS | All requests completed, but cash and stock conservation checks failed | Marked as failed; reduced the rate to 25 TPS |
| C1 | 65-80 TPS | Completed processing did not reliably keep pace with accepted orders | Set the highest verified target to 60 TPS |
