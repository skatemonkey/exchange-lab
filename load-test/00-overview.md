# Load Test

## 1. Scenario

Test scenario:

- Stock: `ACME`
  - Total supply: `5,000,000`
  - Seller-owned units: `5,000,000`
  - Buyer-owned units: `0`
  - Units recorded as reserved in sell orders: `500,000`
  - Seller-owned idle units: `4,500,000`
- Buyers: `100`
  - Each buyer owns: `100,000,000` cash
  - Each buyer owns: `0 ACME`
- Sellers: `50`
  - Each seller owns: `0` cash
  - Each seller owns: `100,000 ACME`
- Initial order book:
  - `50` sell orders
  - Each sell order has: `10,000 ACME`
  - Sell order price is `100`
- k6 action: buyers submit `BUY ACME` orders, quantity `1`

## 2. Verification

`verify.sql` checks whether the market data still balances after the load test.

- Total cash remains `10000000000`.
- Total ACME quantity remains `5000000`.
- No trader account has negative cash.
- No trader account has reserved cash greater than cash balance.
- No stock position has negative quantity.
- No stock position has reserved quantity greater than quantity.
- No order has invalid remaining quantity or status.

Previous run results are tracked in [Load Test Results](results/00-results.md).

## 3. Commands

1. Start Postgres.

   ```powershell
   docker compose up -d postgres
   ```

2. Create tables.

   ```powershell
   Get-Content .\database\schema.sql | docker exec -i exchange-lab-postgres psql -U exchange_lab -d exchange_lab
   ```

   Or run [schema.sql](../database/schema.sql) manually in a DataGrip SQL console.

3. Seed baseline data.

   ```powershell
   Get-Content .\load-test\seed.sql | docker exec -i exchange-lab-postgres psql -U exchange_lab -d exchange_lab
   ```

   Or run [seed.sql](seed.sql) manually in a DataGrip SQL console.

4. Start the app.

   ```powershell
   $env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-26.0.1"
   $env:Path = "$env:JAVA_HOME\bin;$env:Path"
   .\gradlew.bat bootRun
   ```

5. Run k6 in another terminal.

   ```powershell
   k6 run .\load-test\k6\01-buy-orders.js
   ```

6. Verify DB totals.

   ```powershell
   Get-Content .\load-test\verify.sql | docker exec -i exchange-lab-postgres psql -U exchange_lab -d exchange_lab
   ```

   Or run [verify.sql](verify.sql) manually in a DataGrip SQL console.
