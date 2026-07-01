# Local Development Notes

> **Table of Contents**
>
> - [1. URLs](#1-urls)
> - [2. Startup](#2-startup)
> - [3. Login Notes](#3-login-notes)
> - [4. Observability Folder](#4-observability-folder)
> - [5. SkyWalking Tracing](#5-skywalking-tracing)
> - [6. Kafka Event Flow](#6-kafka-event-flow)
> - [7. Load Testing](#7-load-testing)

## 1. URLs

| Tool | URL | Purpose |
|---|---|---|
| Exchange app | http://localhost:8080 | Backend service |
| Exchange gateway | http://localhost:9000 | API entry point |
| Place limit order API | http://localhost:9000/api/orders/limit | Gateway route to the backend order API |
| Kafka bootstrap server | localhost:9092 | Local Kafka broker |
| Nacos console | http://localhost:18080 | Service discovery |
| Sentinel dashboard | http://localhost:8858 | Flow control and API protection |
| Prometheus | http://localhost:9090 | Metrics collection |
| Prometheus targets | http://localhost:9090/targets | Scrape target health |
| Grafana | http://localhost:3000 | Metrics dashboards |
| Grafana Exchange Lab Overview | http://localhost:3000/d/exchange-lab-overview/exchange-lab-overview | Provisioned local dashboard |
| SkyWalking UI | http://localhost:18086 | Distributed tracing dashboard |
| SkyWalking OAP HTTP API | http://localhost:12800 | SkyWalking backend HTTP port |
| SkyWalking OAP gRPC API | localhost:11800 | Native SkyWalking trace collection port |
| SkyWalking Zipkin receiver | http://localhost:9411 | Zipkin-format trace collection exposed by OAP |
| SkyWalking Zipkin query API | http://localhost:9412/zipkin | Zipkin-compatible trace query endpoint exposed by OAP |
| SkyWalking BanyanDB HTTP API | http://localhost:17913 | SkyWalking trace storage HTTP port |

Useful Actuator URLs:

| App | URL |
|---|---|
| Exchange app health | http://localhost:8080/actuator/health |
| Exchange app Prometheus metrics | http://localhost:8080/actuator/prometheus |
| Exchange gateway health | http://localhost:9000/actuator/health |
| Exchange gateway Prometheus metrics | http://localhost:9000/actuator/prometheus |

Grafana dashboard:

| Dashboard | Location |
|---|---|
| Exchange Lab Overview | `Exchange Lab / Exchange Lab Overview` |

## 2. Startup

Start infrastructure first:

```powershell
docker compose up -d
```

Start only Kafka:

```powershell
docker compose up -d kafka
```

Check Kafka:

```powershell
docker exec exchange-lab-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

Run the backend app:

```powershell
.\gradlew.bat :exchange-app:bootRun
```

Run the gateway:

```powershell
.\gradlew.bat :exchange-gateway:bootRun
```

Run the backend app with SkyWalking tracing:

```powershell
.\gradlew.bat :exchange-app:bootRun -Pskywalking.enabled=true
```

Run the gateway with SkyWalking tracing:

```powershell
.\gradlew.bat :exchange-gateway:bootRun -Pskywalking.enabled=true
```

## 3. Login Notes

| Tool | Username | Password |
|---|---|---|
| Nacos console | `nacos` | `1234` |
| Sentinel dashboard | `sentinel` | `sentinel` |
| Grafana | `admin` | `admin` |

These credentials are only for local development.

## 4. Observability Folder

The `observability/` folder stores local monitoring configuration.

It currently contains:

1. `observability/prometheus/prometheus.yml`: tells Prometheus which targets to scrape.
2. `observability/grafana/provisioning/datasources/prometheus.yml`: connects Grafana to Prometheus.
3. `observability/grafana/provisioning/dashboards/dashboards.yml`: tells Grafana where to load dashboards from.
4. `observability/grafana/dashboards/exchange-lab-overview.json`: the local Exchange Lab dashboard.

This folder is not business logic. It is infrastructure configuration for the
local Prometheus and Grafana setup.

## 5. SkyWalking Tracing

SkyWalking shows request traces across services.

For local development:

1. `skywalking-banyandb` stores trace data.
2. `skywalking-oap` is the backend that receives traces.
3. `skywalking-ui` is the browser dashboard.
4. `-Pskywalking.enabled=true` turns on Micrometer tracing when running
   `bootRun`.

The app reports Zipkin-format traces to SkyWalking OAP at
`http://localhost:9411/api/v2/spans`.

SkyWalking also exposes a Zipkin-compatible query API at
`http://localhost:9412/zipkin`.

In tracing, the backend appears as `exchange-lab` and the gateway appears as
`exchange-gateway`.

## 6. Kafka Event Flow

Current topic:

```text
orders.accepted
```

Current flow:

```text
OrderController
-> PlaceLimitOrderUseCase
-> save accepted order and reserve portfolio
-> publish OrderAcceptedEvent
-> orders.accepted topic
-> OrderAcceptedKafkaConsumer
-> MatchAcceptedOrderUseCase
-> match, settle, and save trades
```

## 7. Load Testing

The `load-testing/` folder stores local performance testing files.

It currently contains:

1. `load-testing/seed/phase7-seed.sql`: resets and seeds traders, stock
   positions, and resting sell orders.
2. `load-testing/k6/place-limit-orders.js`: sends limit buy orders through
   `exchange-gateway`.

Detailed load test overview:

```text
load-testing/README.md
```

Seed the database:

```powershell
Get-Content .\load-testing\seed\phase7-seed.sql | docker exec -i exchange-lab-postgres psql -U exchange_lab -d exchange_lab
```

Run the default k6 test:

```powershell
$env:BASE_URL = "http://localhost:9000"
k6 run .\load-testing\k6\place-limit-orders.js
```

Verify the database after k6:

```powershell
Get-Content .\load-testing\verify\phase7-verify.sql | docker exec -i exchange-lab-postgres psql -U exchange_lab -d exchange_lab
```

Run a higher-rate test:

```powershell
$env:BASE_URL = "http://localhost:9000"
$env:TARGET_RATE = "200"
$env:DURATION = "2m"
$env:MAX_VUS = "500"
k6 run .\load-testing\k6\place-limit-orders.js
```

Useful k6 environment variables:

| Variable | Default | Meaning |
|---|---|---|
| `BASE_URL` | `http://localhost:9000` | Gateway URL |
| `TARGET_RATE` | `50` | Target requests per second |
| `DURATION` | `1m` | Main test duration |
| `BUYER_COUNT` | `1000` | Number of seeded buyer accounts |
| `MAX_VUS` | `200` | Maximum virtual users |
| `P95_MS` | `2000` | p95 latency threshold |
| `MAX_ERROR_RATE` | `0.20` | Maximum allowed request failure rate |
