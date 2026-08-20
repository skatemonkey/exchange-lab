# C0-C3 Load-Test Results

> Test method: [C0-C3 Load-Test Strategy](01-c0-c3-strategy.md)

## 1. Test Environment

| Field | Value |
|---|---|
| Test date | C0: 19 August 2026; C1-C2: 20 August 2026 |
| Machine and operating system | Windows 11 Enterprise 10.0.26200; AMD Ryzen 7 7800X3D; 16 logical processors; 31.1 GB RAM |
| Java and JVM settings | OpenJDK 26.0.1; default JVM settings |
| k6 version | 2.2.0 |
| Warm-up period | Corrected C1-C2 runs: none; every measured rate used a fresh application process |
| Startup readiness | Corrected C1-C2 runs: 10-second startup window; application and Kafka consumer ready before k6 started |
| Measurement period | 30 seconds per run |
| Drain period | C0: 0 seconds; asynchronous C1-C2: 5 seconds |
| Completion measurement | C0: synchronous accepted responses; C1-C2: `exchange.orders.completed` after successful Kafka processing |
| Application restart | Corrected C1-C2 runs: required before every rate; C2 rebuilt the in-memory order book during startup |
| k6 script | `k6/02-tps-benchmark.js` |
| Seed and verification files | `seed.sql` and `verify.sql` |

## 2. Configuration Versions

| Configuration | Git commit | Main difference | Status |
|---|---|---|---|
| C0 | `567b63b` | Synchronous processing with MySQL | Tested |
| C1 | `5f248cc` | Adds Kafka-based sequential processing | Retested with restart before every rate |
| C2 | `e5f84d1` | Adds in-memory order matching | Retested with restart before every rate |
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

Every rate used a newly started application process and freshly seeded database. The initial rates located the processing limit, and smaller steps narrowed it. The 55 TPS run fell below the 98% completion rule, so 50 TPS became the highest candidate.

| Test | Target TPS | Accepted TPS | Completed TPS | Completion ratio | p95 latency | Completed during drain | Unfinished after drain | Kafka lag | Errors / drops | SQL checks | Decision |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| Initial rate | 10 | 10.03 | 10.00 | 99.67% | 9.40 ms | 1 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Initial rate | 20 | 20.03 | 20.00 | 99.83% | 8.02 ms | 1 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Initial rate | 40 | 40.03 | 40.00 | 99.92% | 7.56 ms | 1 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Initial rate | 80 | 80.00 | 59.50 | 74.38% | 6.92 ms | 330 | 285 | 262 | 0 / 0 | 5/5 pass | Fail |
| Smaller step | 60 | 60.03 | 56.37 | 93.89% | 7.29 ms | 110 | 0 | 0 | 0 / 0 | 5/5 pass | Fail |
| Smaller step | 50 | 50.03 | 49.93 | 99.80% | 7.11 ms | 3 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Smaller step | 55 | 55.00 | 53.13 | 96.61% | 6.89 ms | 56 | 0 | 0 | 0 / 0 | 5/5 pass | Fail |

### Final TPS Confirmation

| Test | Target TPS | Accepted TPS | Completed TPS | Completion ratio | p95 latency | Completed during drain | Unfinished after drain | Kafka lag | Errors / drops | SQL checks | Decision |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| Confirmation 1 | 50 | 50.03 | 49.97 | 99.87% | 7.27 ms | 2 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Confirmation 2 | 50 | 50.03 | 49.97 | 99.87% | 7.04 ms | 2 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Confirmation 3 | 50 | 50.00 | 49.93 | 99.87% | 7.12 ms | 2 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |

### Conclusion

Under the corrected restart-before-every-rate protocol, the verified C1 score is **50 TPS**. The three confirmation runs produced a median accepted throughput of **50.03 TPS**, a median completed throughput of **49.97 TPS**, and a median p95 API latency of **7.12 ms**.

## 5. C2 Results

### Rate Search

Every rate used a newly started application process and freshly seeded database. The initial rates located the processing limit, and smaller steps narrowed it. At 55 TPS and above, completed processing did not keep pace with accepted orders. Therefore, 50 TPS became the highest candidate.

| Test | Target TPS | Accepted TPS | Completed TPS | Completion ratio | p95 latency | Completed during drain | Unfinished after drain | Kafka lag | Errors / drops | SQL checks | Decision |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| Initial rate | 10 | 10.00 | 10.00 | 100.00% | 8.72 ms | 0 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Initial rate | 20 | 20.03 | 20.00 | 99.83% | 7.63 ms | 1 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Initial rate | 40 | 40.03 | 40.00 | 99.92% | 7.32 ms | 1 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Initial rate | 80 | 80.00 | 67.33 | 84.17% | 6.93 ms | 349 | 31 | 0 | 0 / 0 | 5/5 pass | Fail |
| Smaller step | 60 | 60.00 | 53.33 | 88.89% | 7.23 ms | 200 | 0 | 0 | 0 / 0 | 5/5 pass | Fail |
| Smaller step | 50 | 50.00 | 49.93 | 99.87% | 6.86 ms | 2 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Smaller step | 55 | 55.03 | 48.80 | 88.67% | 6.88 ms | 187 | 0 | 0 | 0 / 0 | 5/5 pass | Fail |

### Final TPS Confirmation

| Test | Target TPS | Accepted TPS | Completed TPS | Completion ratio | p95 latency | Completed during drain | Unfinished after drain | Kafka lag | Errors / drops | SQL checks | Decision |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| Confirmation 1 | 50 | 50.00 | 49.93 | 99.87% | 6.87 ms | 2 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Confirmation 2 | 50 | 50.03 | 49.97 | 99.87% | 6.90 ms | 2 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |
| Confirmation 3 | 50 | 50.03 | 49.97 | 99.87% | 6.89 ms | 2 | 0 | 0 | 0 / 0 | 5/5 pass | Pass |

### Conclusion

Under the corrected restart-before-every-rate protocol, the verified C2 score is **50 TPS**. The three confirmation runs produced a median accepted throughput of **50.03 TPS**, a median completed throughput of **49.97 TPS**, and a median p95 API latency of **6.89 ms**. C2 matched C1's corrected sustainable target but did not increase it.

## 6. C3 Results

| Target TPS | Accepted TPS | Completed TPS | p95 latency | Errors / dropped iterations | Unfinished work after drain | SQL checks | Decision |
|---:|---:|---:|---:|---|---|---|---|
| Pending | Pending | Pending | Pending | Pending | Pending | Pending | Pending |

## 7. Overall Comparison

| Configuration | Highest passing target TPS | Median completed TPS | Change from previous configuration | Finding |
|---|---:|---:|---:|---|
| C0 | 25 | 25.03 | Baseline | Verified at 5-TPS boundary resolution |
| C1 | 50 | 49.97 | Not directly comparable until C0 uses the corrected protocol | Corrected result using a fresh application process for every rate |
| C2 | 50 | 49.97 | 0.0% from C1 target TPS | In-memory matching matched C1's sustainable rate but did not raise the 5-TPS boundary |
| C3 | Pending | Pending | Pending | Pending |

## 8. Invalid Runs and Notes

| Configuration | Target TPS | Problem | Action |
|---|---:|---|---|
| C0 | 5 TPS warm-up | Two request failures caused by MySQL deadlocks during cold start | Discarded as planned and reseeded before formal testing |
| C0 | 40 TPS | Four requests failed and two conservation checks failed | Marked as failed; narrowed the boundary |
| C0 | 30 TPS | All requests completed, but cash and stock conservation checks failed | Marked as failed; reduced the rate to 25 TPS |
| C1 | 55-80 TPS | Completed processing did not keep pace with accepted orders under the corrected restart protocol | Set the highest verified target to 50 TPS |
| C2 | 55-80 TPS | Completed processing did not keep pace with accepted orders; the 80 TPS run retained 31 in-flight orders after the fixed drain | Set the highest verified target to 50 TPS |
