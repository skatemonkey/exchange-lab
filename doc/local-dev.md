# Local Development Notes

> **Table of Contents**
>
> - [1. URLs](#1-urls)
> - [2. Startup](#2-startup)
> - [3. Login Notes](#3-login-notes)
> - [4. Observability Folder](#4-observability-folder)

## 1. URLs

| Tool | URL | Purpose |
|---|---|---|
| Exchange app | http://localhost:8080 | Backend service |
| Exchange gateway | http://localhost:9000 | API entry point |
| Place limit order API | http://localhost:9000/api/orders/limit | Gateway route to the backend order API |
| Nacos console | http://localhost:18080 | Service discovery |
| Sentinel dashboard | http://localhost:8858 | Flow control and API protection |
| Prometheus | http://localhost:9090 | Metrics collection |
| Prometheus targets | http://localhost:9090/targets | Scrape target health |
| Grafana | http://localhost:3000 | Metrics dashboards |
| Grafana Exchange Lab Overview | http://localhost:3000/d/exchange-lab-overview/exchange-lab-overview | Provisioned local dashboard |

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

Run the backend app:

```powershell
.\gradlew.bat :exchange-app:bootRun
```

Run the gateway:

```powershell
.\gradlew.bat :exchange-gateway:bootRun
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
