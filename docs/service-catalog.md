# Service Catalog

## Scope

This document lists each deployable service, what it owns, what it depends on, and how it participates in the SmartCart platform.

## `discovery-server`

### Responsibility

- Eureka registry for service discovery

### Depends on

- none outside Spring runtime

### Provides

- service registry
- service lookup for gateway and backend services

## `api-gateway`

### Responsibility

- external HTTP entry point
- route mapping to downstream services
- JWT validation for protected routes
- Swagger aggregation
- correlation ID propagation

### Depends on

- Eureka
- Auth service JWKS endpoint

### Public routes

- `/api/v1/auth/**`
- `/api/v1/users/**`
- `/swagger-ui/**`
- `/swagger-ui.html`
- `/v3/api-docs/**`
- `/actuator/**`

## `auth-service`

### Responsibility

- authenticate users
- issue RSA-signed JWT access tokens
- expose JWKS public key material

### Depends on

- `user-service` via Feign
- Eureka

### Owns data

- no standalone business database in the current implementation
- key material is loaded from resource files

## `user-service`

### Responsibility

- register new users
- return user auth details by email
- publish `USER_CREATED`

### Depends on

- PostgreSQL
- Kafka
- Eureka

### Owns data

- users

## `product-service`

### Responsibility

- create products
- fetch product by ID
- list all products

### Depends on

- MongoDB
- Eureka

### Owns data

- products

## `inventory-service`

### Responsibility

- add stock
- reserve stock
- release stock
- confirm stock
- react to order and payment workflow events

### Depends on

- PostgreSQL
- Kafka
- Eureka

### Owns data

- inventory records

## `order-service`

### Responsibility

- create orders
- expose order history and workflow status
- cancel orders
- persist workflow metadata
- publish order events
- react to inventory and payment outcome events

### Depends on

- PostgreSQL
- Kafka
- Eureka

### Owns data

- orders
- order items

## `payment-service`

### Responsibility

- create payment records for reserved orders
- simulate gateway success or failure
- publish payment outcome events

### Depends on

- PostgreSQL
- Kafka
- Eureka

### Owns data

- payments

## `notification-service`

### Responsibility

- create notification records from business events
- simulate delivery
- expose notifications by order and user

### Depends on

- MongoDB
- Kafka
- Eureka

### Owns data

- notifications

## `smartcart-common`

### Responsibility

- shared events
- Kafka topic definitions
- API response model
- global exceptions
- tracing utilities
- logging and observability support
- Swagger and Jackson support

### Consumers

- all backend services

## Cross-service dependency map

| Service | Direct synchronous dependency | Asynchronous dependency |
|---|---|---|
| `api-gateway` | all routed services | none |
| `auth-service` | `user-service` | none |
| `user-service` | none | Kafka publisher |
| `product-service` | none | none in current code |
| `inventory-service` | none | consumes order/payment events, publishes inventory events |
| `order-service` | none in current active workflow | publishes order events, consumes inventory/payment events |
| `payment-service` | none | consumes inventory events, publishes payment events |
| `notification-service` | none | consumes order/payment events |

## Notes

- Redis exists in infrastructure but is not currently used by business services.
- The service boundaries are intentionally narrower than a full production commerce platform.
- Each service is independently deployable and aligned to a single core domain concern.
