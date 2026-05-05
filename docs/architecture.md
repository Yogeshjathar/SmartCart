# SmartCart Architecture

## Purpose

SmartCart is a domain-oriented microservices platform for core e-commerce flows: user registration, authentication, catalog browsing, inventory reservation, order placement, payment processing, and notification delivery.

## System topology

```text
React Frontend
    |
    v
API Gateway
    |
    +--> Auth Service
    +--> User Service
    +--> Product Service
    +--> Inventory Service
    +--> Order Service
    +--> Payment Service
    +--> Notification Service

All backend services
    |
    +--> Eureka Discovery Server
    +--> Kafka
    +--> Service-owned datastores
```

## Architectural decisions

### 1. API Gateway as the single public entry point

`api-gateway` is the external edge for HTTP traffic. It centralizes:

- route mapping
- JWT enforcement for protected routes
- correlation ID propagation
- aggregated Swagger documentation

### 2. Service discovery through Eureka

Services register with `discovery-server` and are resolved by logical name such as `lb://order-service`. This avoids hardcoding inter-service addresses and aligns the system with containerized deployment patterns.

### 3. Database per service

Each service owns its own persistence layer:

- PostgreSQL for transactional services
- MongoDB for document-oriented services

No service reads or writes another service's database directly.

### 4. Mixed sync/async communication

SmartCart intentionally uses both:

- synchronous HTTP for edge requests and auth lookup
- asynchronous Kafka events for distributed order workflow

This keeps user-facing flows simple while allowing the checkout path to be eventually consistent across multiple services.

### 5. Stateless security model

`auth-service` signs RSA-backed JWTs and exposes a JWKS endpoint. Downstream services validate tokens as OAuth2 resource servers using the public key. No shared symmetric secret is required across services.

### 6. Observability as a first-class concern

The platform propagates trace and correlation metadata across HTTP and Kafka. Metrics, tracing, structured logging, and common exception handling are implemented through `smartcart-common`.

## Communication model

## Synchronous paths

- frontend -> gateway
- gateway -> business services
- `auth-service` -> `user-service` for login-time user lookup

## Asynchronous paths

- `ORDER_CREATED`
- `ORDER_CANCELLED`
- `INVENTORY_RESERVED`
- `INVENTORY_RESERVATION_FAILED`
- `PAYMENT_SUCCESS`
- `PAYMENT_FAILED`
- `USER_CREATED`

## Bounded contexts

| Domain | Owning service |
|---|---|
| Authentication | `auth-service` |
| User identity and registration | `user-service` |
| Catalog | `product-service` |
| Stock and reservation | `inventory-service` |
| Order lifecycle | `order-service` |
| Payment execution state | `payment-service` |
| User notifications | `notification-service` |

## Runtime dependencies

| Dependency | Used by |
|---|---|
| Eureka | all backend services |
| Kafka | user, inventory, order, payment, notification |
| PostgreSQL | user, inventory, order, payment |
| MongoDB | product, notification |
| Prometheus / Grafana / Zipkin | observability |

## Current state vs future state

Current implementation already includes:

- gateway routing
- discovery
- JWT/JWKS security
- event-driven checkout workflow
- frontend integration
- observability support

Planned but not yet fully implemented:

- persisted idempotency handling
- retry and DLQ strategy
- richer product search and admin operations
- production-grade payment provider integration
