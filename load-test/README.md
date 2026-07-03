# Baseline Load Test

## 1. Scenario

- Stock symbol: `ACME`
- Buyer accounts: `100`
  - Each buyer starts with `100000` cash.
- Seller accounts: `100`
  - Each seller starts with `0` cash and `1000` ACME.
- Initial total cash: `10000000`
- Initial total ACME quantity: `100000`
- Initial order book: empty.
- k6 sends random buy/sell limit orders.
  - Price: `100`
  - Quantity: `1`

## 2. Goal

Test the current synchronous DB flow before adding Redis and Kafka.

After the load test:

- Total cash should still be `10000000`.
- Total ACME quantity should still be `100000`.
- No account should have negative cash.
- No stock position should have negative quantity.
- Reserved cash/stock should not exceed owned cash/stock.

## 3. Commands

Start Postgres:

```powershell
docker compose up -d postgres
```

Seed baseline data:

```powershell
Get-Content .\load-test\seed.sql | docker exec -i exchange-lab-postgres psql -U exchange_lab -d exchange_lab
```

Start the app:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-26.0.1"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat bootRun
```

Run k6:

```powershell
k6 run .\load-test\limit-orders.js
```

Verify DB totals:

```powershell
Get-Content .\load-test\verify.sql | docker exec -i exchange-lab-postgres psql -U exchange_lab -d exchange_lab
```
