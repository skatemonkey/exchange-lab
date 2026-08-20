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

The formal comparison is defined in the [C0-C3 Load-Test Strategy](01-c0-c3-strategy.md), and measurements will be recorded in [C0-C3 Load-Test Results](02-c0-c3-results.md).

## 3. Commands

For C3 on current `v3`, use the automated runner. It stops old application processes, waits for old Kafka members to leave, resets the data, starts fresh services, waits for assigned consumer partitions, runs k6, verifies SQL and Redis, and shuts the applications down:

```powershell
.\_support\load-test\run-c3-rate.ps1 -Rate 55 -Run 1
```

The commands below are the manual equivalent.

1. Start the infrastructure required by the selected configuration. C1 and C2 use MySQL and Kafka; C3 also uses Redis.

   ```powershell
   # C1 and C2
   docker compose up -d mysql kafka

   # C3
   docker compose up -d mysql kafka redis
   ```

2. Create tables.

   ```powershell
   Get-Content .\_support\database\schema.sql | docker exec -i exchange-lab-mysql mysql -uexchange_lab -pexchange_lab exchange_lab
   ```

   Or run [schema.sql](../database/schema.sql) manually in a DataGrip MySQL console.

3. Seed baseline data.

   ```powershell
   Get-Content .\_support\load-test\seed.sql | docker exec -i exchange-lab-mysql mysql -uexchange_lab -pexchange_lab exchange_lab
   ```

   Or run [seed.sql](seed.sql) manually in a DataGrip MySQL console.

4. Start each application service in a separate PowerShell terminal. Start `finance-service` first so C3 can preload Redis, then start `match-service` so it can rebuild the order book, and finally start `exchange-service`.

   ```powershell
   $env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-26.0.1"
   $env:Path = "$env:JAVA_HOME\bin;$env:Path"
   .\gradlew.bat :finance-service:bootRun
   ```

   ```powershell
   $env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-26.0.1"
   $env:Path = "$env:JAVA_HOME\bin;$env:Path"
   .\gradlew.bat :match-service:bootRun
   ```

   ```powershell
   $env:JAVA_HOME = "$env:USERPROFILE\.jdks\openjdk-26.0.1"
   $env:Path = "$env:JAVA_HOME\bin;$env:Path"
   .\gradlew.bat :exchange-service:bootRun
   ```

   Confirm all three services are ready:

   ```powershell
   Invoke-RestMethod http://localhost:8081/actuator/health
   Invoke-RestMethod http://localhost:8082/actuator/health
   Invoke-RestMethod http://localhost:8080/actuator/health
   ```

5. Run k6 in another terminal.

   ```powershell
   k6 run -e RATE=20 -e DURATION=30s .\_support\load-test\k6\02-tps-benchmark.js
   ```

6. After the drain period, verify database totals. For C3, also verify Redis.

   ```powershell
   Get-Content .\_support\load-test\verify.sql | docker exec -i exchange-lab-mysql mysql -uexchange_lab -pexchange_lab exchange_lab
   .\_support\load-test\verify-redis.ps1
   ```

   Or run [verify.sql](verify.sql) manually in a DataGrip MySQL console.
