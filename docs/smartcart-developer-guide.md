# SmartCart Developer Guide

## Purpose

This guide is for engineers joining the project, presenting it in interviews, or taking ownership of future development. It complements the source-of-truth architecture and contract docs by connecting the codebase to the business story, tradeoffs, and roadmap.

## One-paragraph explanation

SmartCart is a microservices-based e-commerce platform where authentication, users, products, inventory, orders, payments, and notifications are implemented as separate Spring Boot services behind a Spring Cloud Gateway. Checkout is modeled as an event-driven workflow using Kafka, state is isolated per service, and the platform includes JWT/JWKS security, observability, and a React frontend that exercises the full flow.

## How to explain the project in an interview

Use this order:

1. Problem
2. Architecture
3. Workflow
4. Security
5. Tradeoffs
6. What you would improve next

### Problem statement

SmartCart models the backend of a commerce platform where a single user action, placing an order, crosses multiple bounded contexts:

- identity
- catalog
- inventory
- order lifecycle
- payment
- notification

This is exactly the kind of flow where service decomposition and eventual consistency become meaningful.

### Architecture statement

The platform is split by business capability, uses a gateway as the only public entry point, uses Eureka for service discovery, uses JWT for stateless auth, and uses Kafka to coordinate the checkout workflow.

### Workflow statement

The most important flow is:

1. create order
2. reserve stock
3. process payment
4. update order state
5. notify the user

This is not just CRUD. It is a distributed workflow with compensation and eventual consistency.

### Security statement

Authentication is centralized in `auth-service`. It signs RSA-backed JWTs and exposes JWKS so downstream services can validate tokens independently without sharing a secret.

### Tradeoff statement

This architecture is more complex than a monolith, but it demonstrates clean service boundaries, isolated persistence, async workflow progression, and better operational visibility.

## What is already strong in this codebase

- service boundaries are clear
- database-per-service is implemented
- JWT/JWKS flow is real, not mocked
- checkout is event-driven
- correlation and trace propagation exist
- frontend proves the APIs work end to end
- docs and operations are now structured

## What is intentionally simplified

- payment provider is mocked
- notification sending is simulated
- Redis is not yet used
- idempotency is not yet persisted
- retry and DLQ handling are not yet implemented
- tests are still shallow relative to production expectations

## What to say if asked about gaps

Say this directly:

> The project is intentionally positioned as a working microservices foundation rather than a production-finished commerce platform. The core architecture, security, workflow, and observability are real. The next phase is hardening: idempotency, retries, stronger test coverage, richer admin flows, and deployment maturity.

That answer is technically honest and shows judgment.

## Codebase reading order for a new developer

1. `README.md`
2. `docs/architecture.md`
3. `docs/service-catalog.md`
4. `docs/api-contracts.md`
5. `docs/order-flow.md`
6. `docs/security-authentication.md`
7. `docs/operations-runbook.md`
8. `smartcart-common`
9. `order-service`
10. `inventory-service`
11. `payment-service`
12. `notification-service`
13. `frontend`

## Important implementation details to remember

### Order workflow ownership

`order-service` owns the order record and exposes workflow status, but the flow advances through Kafka events from other services.

### Inventory consistency model

Inventory uses:

- available quantity
- reserved quantity

This avoids immediate oversell during checkout and allows release on failure.

### Payment model

Payment is triggered only after successful inventory reservation. The service stores a payment record and emits a final outcome event.

### Notification consistency model

Notification is side-effecting and non-transactional. Failure should not roll back an order.

### Frontend role

The frontend is a working consumer of the platform, not just a mock screen. It proves the backend contracts, JWT flow, and workflow status visibility.

## Recommended next engineering milestones

### Priority 1

- add focused tests for checkout workflow transitions
- add idempotency for order creation and payment execution
- add retry and DLQ handling for Kafka consumers
- tighten admin-only authorization

### Priority 2

- product search, pagination, update, and delete
- richer user profile capabilities
- better error contracts for direct entity-returning endpoints
- outbox pattern for stronger event publication guarantees

### Priority 3

- Redis-backed caching and idempotency
- CI/CD
- Kubernetes deployment
- production-grade payment adapter

## Interview closing summary

If you need a short close:

> SmartCart demonstrates that I can design and explain a distributed system beyond CRUD. It has secure gateway-based access, service discovery, event-driven checkout, isolated persistence, and observability. The current code is a solid engineering base, and I know exactly what the next hardening steps are.
