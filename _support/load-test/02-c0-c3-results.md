# C0-C3 Load-Test Results

> Test method: [C0-C3 Load-Test Strategy](01-c0-c3-strategy.md)

The previous C1-C3 measurements were discarded because they tested older single-application implementations rather than the three-service configurations defined in the proposal. Only the C0 result remains valid until C1-C3 are retested.

## 1. Test Environment

| Field | Value |
|---|---|
| Test date | C0: 19 August 2026; rebuilt C1-C3: pending |
| Machine and operating system | Windows 11 Enterprise 10.0.26200; AMD Ryzen 7 7800X3D; 16 logical processors; 31.1 GB RAM |
| Java and JVM settings | OpenJDK 26.0.1; default JVM settings |
| k6 version | 2.2.0 |
| Warm-up period | Pending for rebuilt C1-C3 |
| Startup readiness | Finance, match, and exchange health endpoints must pass before k6 starts |
| Measurement period | 30 seconds per run |
| Drain period | C0: 0 seconds; rebuilt C1-C3: 5 seconds |
| Completion measurement | C0: synchronous accepted responses; rebuilt C1-C3: `finance.settlements.completed` before drain |
| Application restart | All three application services must restart before every measured C1-C3 rate |
| C2-C3 startup state | Match service rebuilds the in-memory order book from MySQL |
| C3 startup state | Redis is flushed before startup; finance service then preloads availability from MySQL |
| k6 script | `k6/02-tps-benchmark.js` |
| Verification files | `seed.sql`, `verify.sql`, and C3 `verify-redis.ps1` |

## 2. Configuration Versions

| Configuration | Implementation commit | Main difference | Status |
|---|---|---|---|
| C0 | `567b63b` | Synchronous processing with MySQL | Tested |
| C1 | `0739e88` | Three-service Kafka pipeline with database matching | Awaiting retest |
| C2 | `edd50d6` | C1 with in-memory order matching | Awaiting retest |
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

**Pending.** Retest the three-service Kafka pipeline with database matching.

| Target TPS | Accepted TPS | Completed TPS | p95 latency | Errors / dropped iterations | Unfinished work after drain | SQL checks | Decision |
|---:|---:|---:|---:|---|---|---|---|
| Pending | Pending | Pending | Pending | Pending | Pending | Pending | Pending |

## 5. C2 Results

**Pending.** Retest C1 after adding the startup-rebuilt in-memory order book.

| Target TPS | Accepted TPS | Completed TPS | p95 latency | Errors / dropped iterations | Unfinished work after drain | SQL checks | Decision |
|---:|---:|---:|---:|---|---|---|---|
| Pending | Pending | Pending | Pending | Pending | Pending | Pending | Pending |

## 6. C3 Results

**Pending.** Retest C2 after adding Redis reservation and startup preloading.

| Target TPS | Accepted TPS | Completed TPS | p95 latency | Errors / dropped iterations | Unfinished work after drain | SQL / Redis checks | Decision |
|---:|---:|---:|---:|---|---|---|---|
| Pending | Pending | Pending | Pending | Pending | Pending | Pending | Pending |

## 7. Overall Comparison

| Configuration | Highest passing target TPS | Median completed TPS | Change from previous configuration | Finding |
|---|---:|---:|---:|---|
| C0 | 25 | 25.03 | Baseline | Existing synchronous baseline |
| C1 | Pending | Pending | Pending | Awaiting multi-stage retest |
| C2 | Pending | Pending | Pending | Awaiting multi-stage retest |
| C3 | Pending | Pending | Pending | Awaiting multi-stage retest |

## 8. Invalid Runs and Notes

| Configuration | Target TPS | Problem | Action |
|---|---:|---|---|
| C0 | 5 TPS warm-up | Two request failures caused by MySQL deadlocks during cold start | Discarded as planned and reseeded before formal testing |
| C0 | 40 TPS | Four requests failed and two conservation checks failed | Marked as failed; narrowed the boundary |
| C0 | 30 TPS | All requests completed, but cash and stock conservation checks failed | Marked as failed; reduced the rate to 25 TPS |

The removed C1-C3 values remain available only on the `codex/benchmark-c1-monolith`, `codex/benchmark-c2-monolith`, and `codex/benchmark-c3-monolith` archival branches. They are not part of the formal comparison.
