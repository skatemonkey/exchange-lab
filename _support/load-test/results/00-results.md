# Load Test Results

| Version | Result File | App Version | k6 Script | Verification | Summary |
|---|---|---|---|---|---|
| 1 | [01-sync-db-buy-orders.md](01-sync-db-buy-orders.md) | `230d6cc` | [01-buy-orders.js](../k6/01-buy-orders.js) | Failed | HTTP passed, but cash/stock totals became wrong. |
| 2 | [02-kafka-mysql-buy-orders.md](02-kafka-mysql-buy-orders.md) | `d5d80ab` | [01-buy-orders.js](../k6/01-buy-orders.js) | Passed | Kafka single-consumer processing kept cash/stock totals correct. |
