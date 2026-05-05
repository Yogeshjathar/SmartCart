# Operations Runbook

## Scope

This document covers local setup, runtime dependencies, environment shape, observability endpoints, and common troubleshooting paths.

## Runtime components

### Infrastructure from `docker-compose.infra.yml`

- PostgreSQL
- MongoDB
- Redis
- Kafka
- AKHQ
- Prometheus
- Grafana
- Zipkin

### Business services from `docker-compose.services.yml`

- discovery-server
- api-gateway
- auth-service
- user-service
- product-service
- inventory-service
- order-service
- payment-service
- notification-service

## Environment

Core values from `.env`:

- PostgreSQL user/password: `postgres` / `postgres`
- databases:
  - `user_db`
  - `inventory_db`
  - `order_db`
  - `payment_db`
  - `product_db`
  - `notification_db`
- gateway port: `8080`
- Eureka port: `8761`
- Prometheus port: `9095`
- Grafana port: `3000`
- Kafka port: `9092`

## Start and stop

### Start all

```bash
make up
```

### Start infra only

```bash
make infra-up
```

### Start service containers only

```bash
make services-up
```

### Follow logs

```bash
make logs
```

### Stop all

```bash
make down
```

## Local service ports

| Component | Port |
|---|---|
| API Gateway | 8080 |
| Auth Service | 8081 |
| User Service | 8082 |
| Product Service | 8083 |
| Inventory Service | 8084 |
| Order Service | 8085 |
| Payment Service | 8086 |
| Notification Service | 8087 |
| Eureka | 8761 |
| Frontend dev server | 5173 |
| Kafka | 9092 |
| AKHQ | 9191 |
| Prometheus | 9095 |
| Grafana | 3000 |
| Zipkin | 9411 |

## Observability endpoints

### Human-facing

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Eureka: `http://localhost:8761`
- AKHQ: `http://localhost:9191`
- Prometheus: `http://localhost:9095`
- Grafana: `http://localhost:3000`
- Zipkin: `http://localhost:9411`

### Actuator

Most services expose actuator endpoints under:

- `/actuator/health`
- `/actuator/info`
- `/actuator/prometheus`
- `/actuator/metrics`
- `/actuator/loggers`

## Smoke test path

Recommended validation order after boot:

1. Open Eureka and confirm service registration
2. Open gateway Swagger UI
3. Register a user
4. Log in and capture JWT
5. Create or fetch products
6. Add inventory for a product
7. Place an order
8. Inspect payment, workflow, and notification results
9. Inspect Kafka topics in AKHQ
10. Inspect traces in Zipkin

## Troubleshooting

## Problem: service does not appear in Eureka

Check:

- discovery-server health
- service `SPRING_PROFILES_ACTIVE`
- `EUREKA_SERVER_URL`
- container startup logs

## Problem: protected endpoints return 401

Check:

- valid bearer token present
- `auth-service` JWKS endpoint reachable
- gateway and service security config
- token expiry

## Problem: order stays in `CREATED`

Check:

- Kafka is running
- `order-created` topic exists or auto-creation is enabled
- `inventory-service` consumer startup logs
- inventory exists for requested products

## Problem: order moves to `FAILED`

Check:

- inventory availability
- payment simulation result
- event consumer logs in inventory, payment, and order services

## Problem: no notifications visible

Check:

- MongoDB connectivity for notification service
- Kafka consumers in notification service
- notification repository logs

## Current operational gaps

- no DLQ handling
- no centralized log sink configured here
- no automated deployment pipeline
- Redis is provisioned but not yet wired into application behavior
