# Load Testing

> **Table of Contents**
>
> - [1. Overview](#1-overview)
> - [2. Test Flow](#2-test-flow)
> - [3. Commands](#3-commands)
> - [4. Files](#4-files)

## 1. Overview

Phase 7 is for load testing the order flow. We seed a fake market, then use k6
to send many buy orders through the gateway.

Test scenario:

- Stock: `ACME`
  - Total supply: `50,000,000`
  - Seller-owned units: `50,000,000`
  - Buyer-owned units: `0`
  - Units recorded as reserved in sell orders: `50,000,000`
- Buyers: `1,000`
  - Each buyer owns: `100,000,000` cash
  - Each buyer owns: `0 ACME`
- Sellers: `500`
  - Each seller owns: `0` cash
  - Each seller owns: `100,000 ACME`
- Initial order book:
  - `500` sell orders
  - Each sell order has `100,000 ACME`
  - Sell order price is `100`
- k6 action: buyers submit `BUY ACME` orders, quantity `1`

## 2. Test Flow

```text
k6
-> exchange-gateway
-> exchange-app
-> Kafka orders.accepted
-> matching consumer
-> Postgres
```

## 3. Commands

Seed database:

```powershell
Get-Content .\load-testing\seed\phase7-seed.sql | docker exec -i exchange-lab-postgres psql -U exchange_lab -d exchange_lab
```

Run k6:

```powershell
$env:BASE_URL = "http://localhost:9000"
k6 run .\load-testing\k6\place-limit-orders.js
```

Verify database after k6:

```powershell
Get-Content .\load-testing\verify\phase7-verify.sql | docker exec -i exchange-lab-postgres psql -U exchange_lab -d exchange_lab
```

Clean run:

```powershell
Get-Content .\load-testing\seed\phase7-seed.sql | docker exec -i exchange-lab-postgres psql -U exchange_lab -d exchange_lab
$env:BASE_URL = "http://localhost:9000"
k6 run .\load-testing\k6\place-limit-orders.js
Get-Content .\load-testing\verify\phase7-verify.sql | docker exec -i exchange-lab-postgres psql -U exchange_lab -d exchange_lab
```

## 4. Files

- `seed/phase7-seed.sql`: resets and seeds the test market.
- `k6/place-limit-orders.js`: sends buy limit orders through the gateway.
- `verify/phase7-verify.sql`: checks post-test database consistency.
