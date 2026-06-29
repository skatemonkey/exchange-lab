# Local Development Notes

## Nacos Console

- URL: http://localhost:18080
- Username: `nacos`
- Password: `1234`

This is only for local development.

## Sentinel Dashboard

- URL: http://localhost:8858
- Username: `sentinel`
- Password: `sentinel`

This is only for local development.

## Spring Boot Apps

- Exchange app URL: http://localhost:8080
- Exchange gateway URL: http://localhost:9000

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
