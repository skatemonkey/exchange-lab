# 01 Sync DB Buy Orders

## 1. Setup

- Date: `2026-07-03`
- App version: `230d6cc`
- Seed file: [seed.sql](../seed.sql)
- k6 script: [01-buy-orders.js](../k6/01-buy-orders.js)
- Verification file: [verify.sql](../verify.sql)
- k6 run method: Docker `grafana/k6`

## 2. k6 Result

- Virtual users: `20`
- Duration: `30s`
- Total requests: `9038`
- HTTP failure rate: `0%`
- Successful status checks: `9038 / 9038`
- Average request duration: `15.79ms`
- Throughput: `300.65 req/s`

## 3. Verification Result

| Check | Expected | Actual | Pass |
|---|---:|---:|---|
| Total cash | `10000000000.00000000` | `9999386600.00000000` | No |
| Total ACME quantity | `5000000.00000000` | `5006268.00000000` | No |
| Invalid cash rows | `0` | `0` | Yes |
| Invalid stock rows | `0` | `0` | Yes |
| Invalid order rows | `0` | `0` | Yes |

## 4. Conclusion

The API handled all requests successfully, but the market totals became wrong.

Most likely problem:

- Multiple concurrent requests matched the same sell order.
- Buyer stock increased more than seller stock decreased.
- This confirms the current synchronous DB matching flow is not concurrency safe.
