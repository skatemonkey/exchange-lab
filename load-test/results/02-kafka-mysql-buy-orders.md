# 02 Kafka MySQL Buy Orders

## 1. Setup

- Date: `2026-07-06`
- App version: `d5d80ab`
- Database: MySQL 8.4
- Kafka topic: `orders.submitted`
- Seed file: [seed.sql](../seed.sql)
- k6 script: [01-buy-orders.js](../k6/01-buy-orders.js)
- Verification file: [verify.sql](../verify.sql)
- k6 run method: Docker `grafana/k6`

## 2. k6 Result

- Virtual users: `20`
- Duration: `30s`
- Total requests: `10100`
- HTTP failure rate: `0%`
- Successful status checks: `10100 / 10100`
- Average request duration: `8.92ms`
- Throughput: `336.29 req/s`

## 3. Kafka Drain

- Consumer group: `exchange-lab-order-matcher`
- Topic partition: `orders.submitted-0`
- Final current offset: `20301`
- Final log end offset: `20301`
- Final lag: `0`

## 4. Verification Result

| Check | Expected | Actual | Pass |
|---|---:|---:|---|
| Total cash | `10000000000.00000000` | `10000000000.00000000` | Yes |
| Total ACME quantity | `5000000.00000000` | `5000000.00000000` | Yes |
| Invalid cash rows | `0` | `0` | Yes |
| Invalid stock rows | `0` | `0` | Yes |
| Invalid order rows | `0` | `0` | Yes |

Additional summary:

| Metric | Value |
|---|---:|
| Buy orders | `10100` |
| Sell orders | `50` |
| Trades | `10100` |
| Traded quantity | `10100.00000000` |
| Traded value | `1010000.0000000000000000` |

Order status after processing:

| Status | Count |
|---|---:|
| ACCEPTED | `48` |
| FILLED | `10101` |
| PARTIALLY_FILLED | `1` |

## 5. Conclusion

The Kafka-based flow kept market totals correct under the same buy-order load
scenario that previously broke the synchronous DB flow.

The important difference is that matching is now handled by a single Kafka
consumer for the single `orders.submitted` partition, so the same resting sell
order was not matched concurrently by multiple requests.

This verifies the current Phase 5 goal for the single-app, single-partition
learning setup. Remaining production-style concerns still include idempotent
consumer handling, retry behavior, and partition ownership by symbol.
