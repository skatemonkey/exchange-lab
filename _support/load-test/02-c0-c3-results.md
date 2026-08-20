# C0-C3 Load-Test Results

> Test method: [C0-C3 Load-Test Strategy](01-c0-c3-strategy.md)

The previous C1-C3 measurements were discarded because they tested older single-application implementations rather than the three-service configurations defined in the proposal. Only the C0 result remains valid until C1-C3 are retested.

## 1. Test Environment

| Field | Value |
|---|---|
| Test date | C0: 19 August 2026; C1-C2: 20 August 2026; C3: pending |
| Machine and operating system | Windows 11 Enterprise 10.0.26200; AMD Ryzen 7 7800X3D; 16 logical processors; 31.1 GB RAM |
| Java and JVM settings | OpenJDK 26.0.1; default JVM settings |
| k6 version | 2.2.0 |
| Warm-up period | C1-C2: 10-second readiness delay after all health checks; no traffic warm-up |
| Startup readiness | Finance, match, and exchange health endpoints must pass before k6 starts |
| Measurement period | 30 seconds per run |
| Drain period | C0: 0 seconds; rebuilt C1-C3: 5 seconds |
| Completion measurement | C0: synchronous accepted responses; rebuilt C1-C3: `finance.settlements.completed` before drain |
| Application restart | All three application services restart before every measured C1-C3 rate; MySQL and Kafka remain stable between rates |
| Kafka health check | Lightweight TCP check; the previous Java CLI health check was removed before valid C1 measurements |
| C2-C3 startup state | Match service rebuilds the in-memory order book from MySQL |
| C3 startup state | Redis is flushed before startup; finance service then preloads availability from MySQL |
| k6 script | `k6/02-tps-benchmark.js` |
| Verification files | `seed.sql`, `verify.sql`, and C3 `verify-redis.ps1` |

## 2. Configuration Versions

| Configuration | Implementation commit | Main difference | Status |
|---|---|---|---|
| C0 | `567b63b` | Synchronous processing with MySQL | Tested |
| C1 | `0739e88` | Three-service Kafka pipeline with database matching | Tested |
| C2 | `edd50d6` | C1 with in-memory order matching | Tested |
| C3 | `fb57885` | C2 with Redis reservation and startup preload | Awaiting retest |

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

The verified C0 score is **25 TPS**. The three confirmation runs produced a median completed throughput of **25.03 TPS** and a median p95 latency of **21.01 ms**.

## 4. C1 Results

### Rate Search

The test started at higher rates, but those exploratory runs were discarded after the Kafka health check was found to consume substantial CPU. With the corrected environment, 30 TPS passed while 35 and 40 TPS overloaded the database matcher.

| Test | Target TPS | Accepted TPS | Completed TPS | Completion ratio | p95 latency | Completed during drain | Unfinished after drain | Kafka lag (order / trade) | Errors / drops | SQL checks | Decision |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| Initial rate | 30 | 30.03 | 30.00 | 99.89% | 19.06 ms | 1 | 0 | 0 / 0 | 0 / 0 | 5/5 pass | Pass |
| Initial rate | 40 | 40.00 | 12.47 | 31.17% | 33.07 ms | 177 | 649 | 700 / 2 | 0 / 0 | 5/5 pass | Fail |
| Smaller step | 35 | 35.03 | 18.30 | 52.24% | 33.83 ms | 194 | 308 | 605 / 1 | 0 / 0 | 5/5 pass | Fail |

### Final TPS Confirmation

| Test | Target TPS | Accepted TPS | Completed TPS | Completion ratio | p95 latency | Completed during drain | Unfinished after drain | Kafka lag (order / trade) | Errors / drops | SQL checks | Decision |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| Confirmation 1 | 30 | 30.00 | 30.00 | 100.00% | 18.12 ms | 0 | 0 | 0 / 0 | 0 / 0 | 5/5 pass | Pass |
| Confirmation 2 | 30 | 30.03 | 30.00 | 99.89% | 19.78 ms | 1 | 0 | 0 / 0 | 0 / 0 | 5/5 pass | Pass |

### Conclusion

The verified C1 score is **30 TPS**. The two confirmation runs produced a median completed throughput of **30.00 TPS** and a median p95 latency of **18.95 ms**. At 35 TPS, order intake remained responsive but the database-backed matcher accumulated unfinished Kafka work.

## 5. C2 Results

### Rate Search

C2 passed its first 40 TPS run, but a fresh 40 TPS repetition failed the 98% completion rule. Therefore, 40 TPS was rejected and the candidate was reduced to 35 TPS.

| Test | Target TPS | Accepted TPS | Completed TPS | Completion ratio | p95 latency | Completed during drain | Unfinished after drain | Kafka lag (order / trade) | Errors / drops | SQL checks | Decision |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| Initial rate | 40 | 40.03 | 40.00 | 99.92% | 25.96 ms | 1 | 0 | 0 / 0 | 0 / 0 | 5/5 pass | Pass |
| Higher rate | 50 | 50.03 | 29.27 | 58.49% | 17.15 ms | 321 | 302 | 297 / 2 | 0 / 0 | 5/5 pass | Fail |
| Smaller step | 45 | 44.97 | 40.77 | 90.66% | 18.02 ms | 126 | 0 | 0 / 0 | 1 / 0 | 5/5 pass | Fail |
| Repeatability check | 40 | 40.00 | 29.47 | 73.67% | 18.27 ms | 316 | 0 | 0 / 0 | 0 / 0 | 5/5 pass | Fail |
| Revised candidate | 35 | 35.00 | 35.00 | 100.00% | 18.68 ms | 0 | 0 | 0 / 0 | 0 / 0 | 5/5 pass | Pass |

### Final TPS Confirmation

| Test | Target TPS | Accepted TPS | Completed TPS | Completion ratio | p95 latency | Completed during drain | Unfinished after drain | Kafka lag (order / trade) | Errors / drops | SQL checks | Decision |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| Confirmation 1 | 35 | 35.00 | 35.00 | 100.00% | 18.96 ms | 0 | 0 | 0 / 0 | 0 / 0 | 5/5 pass | Pass |
| Confirmation 2 | 35 | 35.03 | 35.00 | 99.90% | 18.34 ms | 1 | 0 | 0 / 0 | 0 / 0 | 5/5 pass | Pass |

### Conclusion

The verified C2 score is **35 TPS**. The two confirmation runs produced a median completed throughput of **35.00 TPS** and a median p95 latency of **18.65 ms**. In-memory matching raised repeatable throughput above C1, but the database-backed reservation and settlement work still limited the complete pipeline.

## 6. C3 Results

**Pending.** Retest C2 after adding Redis reservation and startup preloading.

| Target TPS | Accepted TPS | Completed TPS | p95 latency | Errors / dropped iterations | Unfinished work after drain | SQL / Redis checks | Decision |
|---:|---:|---:|---:|---|---|---|---|
| Pending | Pending | Pending | Pending | Pending | Pending | Pending | Pending |

## 7. Overall Comparison

| Configuration | Highest passing target TPS | Median completed TPS | Change from previous configuration | Finding |
|---|---:|---:|---:|---|
| C0 | 25 | 25.03 | Baseline | Existing synchronous baseline |
| C1 | 30 | 30.00 | +19.86% | Kafka improves intake isolation, but database matching becomes the bottleneck above 30 TPS |
| C2 | 35 | 35.00 | +16.67% | In-memory matching improves throughput, while finance database work becomes the next bottleneck |
| C3 | Pending | Pending | Pending | Awaiting multi-stage retest |

## 8. Invalid Runs and Notes

| Configuration | Target TPS | Problem | Action |
|---|---:|---|---|
| C0 | 5 TPS warm-up | Two request failures caused by MySQL deadlocks during cold start | Discarded as planned and reseeded before formal testing |
| C0 | 40 TPS | Four requests failed and two conservation checks failed | Marked as failed; narrowed the boundary |
| C0 | 30 TPS | All requests completed, but cash and stock conservation checks failed | Marked as failed; reduced the rate to 25 TPS |
| C1 | Exploratory runs | Kafka's original Java CLI health check consumed roughly 70-90% CPU and distorted throughput | Discarded; replaced it with a lightweight TCP health check before formal testing |
| C1 | Container-restart trials | Restarting Kafka before every rate introduced log-recovery work during measurement startup | Discarded; kept infrastructure stable and restarted only the application services as defined by the strategy |

The removed C1-C3 values remain available only on the `codex/benchmark-c1-monolith`, `codex/benchmark-c2-monolith`, and `codex/benchmark-c3-monolith` archival branches. They are not part of the formal comparison.
