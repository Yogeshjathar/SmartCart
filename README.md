# SmartCart

SmartCart is a multi-service e-commerce platform built with Spring Boot, Spring Cloud, Kafka, PostgreSQL, MongoDB, and React. It is designed as a working microservices foundation with a real checkout workflow, centralized authentication, service discovery, observability, and a developer-facing frontend.

## What exists today

- API Gateway with JWT enforcement for protected routes
- Eureka service discovery
- Auth service with RSA-signed JWT and JWKS endpoint
- User registration and auth lookup
- Product catalog in MongoDB
- Inventory reservation and stock confirmation in PostgreSQL
- Order workflow backed by Kafka events
- Mock payment processing
- Notification persistence and simulated delivery
- Frontend for login, catalog, cart, checkout, order tracking, and admin inventory/product actions
- Metrics, tracing, Swagger aggregation, and correlation IDs

## Architecture

External traffic enters through `api-gateway` on port `8080`.

Core backend modules:

- `discovery-server`
- `api-gateway`
- `auth-service`
- `user-service`
- `product-service`
- `inventory-service`
- `order-service`
- `payment-service`
- `notification-service`
- `smartcart-common`

Data ownership:

- PostgreSQL: `user-service`, `inventory-service`, `order-service`, `payment-service`
- MongoDB: `product-service`, `notification-service`

Workflow pattern:

1. `order-service` creates the order and publishes `ORDER_CREATED`
2. `inventory-service` reserves stock and publishes either `INVENTORY_RESERVED` or `INVENTORY_RESERVATION_FAILED`
3. `payment-service` listens for reserved inventory and publishes either `PAYMENT_SUCCESS` or `PAYMENT_FAILED`
4. `order-service` updates workflow state from those events
5. `notification-service` persists and simulates user notifications

## Tech stack

- Java 21
- Spring Boot 3.4.6
- Spring Cloud 2024.0.1
- Spring Security
- Spring Cloud Gateway
- Spring Cloud OpenFeign
- Spring Kafka
- Spring Data JPA
- Spring Data MongoDB
- Maven multi-module build
- React 18 + Vite
- Kafka, PostgreSQL, MongoDB, Redis
- Micrometer, Prometheus, Grafana, Zipkin

## Local development

### Prerequisites

- Java 21
- Maven or `mvnw`
- Docker Desktop or equivalent
- Node.js for the frontend

### Start infrastructure and services

```bash
make up
```

Useful alternatives:

```bash
make infra-up
make services-up
make logs
make down
```

### Backend entry points

- Gateway: `http://localhost:8080`
- Eureka: `http://localhost:8761`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- AKHQ: `http://localhost:9191`
- Prometheus: `http://localhost:9095`
- Grafana: `http://localhost:3000`
- Zipkin: `http://localhost:9411`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend dev server:

- UI: `http://localhost:5173`

The frontend proxies `/api` to `http://localhost:8080`.

## Main API routes through the gateway

- `POST /api/v1/users/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/products`
- `GET /api/v1/products/{id}`
- `POST /api/v1/products`
- `GET /api/v1/inventory/{productId}`
- `POST /api/v1/inventory`
- `POST /api/v1/orders`
- `GET /api/v1/orders/{orderId}`
- `GET /api/v1/orders/user/{userId}`
- `GET /api/v1/orders/{orderId}/workflow`
- `PUT /api/v1/orders/{orderId}/cancel`
- `GET /api/v1/payments/order/{orderId}`
- `GET /api/v1/notifications/user/{userId}`

## Documentation

The source-of-truth project documentation lives under [`docs/`](./docs/README.md).

Recommended reading order:

1. [`docs/architecture.md`](./docs/architecture.md)
2. [`docs/service-catalog.md`](./docs/service-catalog.md)
3. [`docs/api-contracts.md`](./docs/api-contracts.md)
4. [`docs/order-flow.md`](./docs/order-flow.md)
5. [`docs/security-authentication.md`](./docs/security-authentication.md)
6. [`docs/operations-runbook.md`](./docs/operations-runbook.md)
7. [`docs/smartcart-developer-guide.md`](./docs/smartcart-developer-guide.md)

## Current scope and known gaps

SmartCart is a strong working platform, but it is not yet production-complete.

Current gaps include:

- mock payment instead of real PSP integration
- simulated notification delivery
- no persisted idempotency-key handling
- no DLQ or retry policy for failed Kafka consumers
- limited automated tests beyond application context coverage
- product and user domains are intentionally narrower than a full commerce platform

## Direction for next work

- stronger test coverage around the order workflow
- idempotency and retry handling
- admin authorization hardening
- product search, pagination, update, and delete flows
- outbox or transactional event publishing pattern
- CI/CD and container orchestration
