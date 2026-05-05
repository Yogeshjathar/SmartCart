# Data Model

## Scope

This document describes the currently implemented persistence model. Types and field names are aligned to the code, not to future design ideas.

## PostgreSQL-backed services

## User service

### Table: `users`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | generated primary key |
| `first_name` | string | mapped from `firstName` |
| `last_name` | string | mapped from `lastName` |
| `phone_no` | string | unique |
| `email` | string | unique |
| `password` | string | BCrypt hash |
| `role` | string | e.g. `ROLE_USER`, `ROLE_ADMIN` |
| `status` | string | currently `ACTIVE` by default |
| `created_at` | timestamp with timezone | set on insert |

## Inventory service

### Table: `inventory`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | generated primary key |
| `product_id` | string | references product by ID only, no FK |
| `available_quantity` | integer | stock ready to reserve |
| `reserved_quantity` | integer | temporarily held stock |
| `warehouse_location` | string | optional |
| `last_updated` | timestamp | last mutation time |

## Order service

### Table: `orders`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | generated primary key |
| `user_id` | string | user reference |
| `status` | enum string | `CREATED`, `RESERVED`, `CONFIRMED`, `FAILED`, `CANCELLED` |
| `payment_status` | enum string | `NOT_STARTED`, `INITIATED`, `SUCCESS`, `FAILED` |
| `total_amount` | decimal | derived from order items |
| `currency` | string | order currency |
| `correlation_id` | string | workflow correlation |
| `trace_id` | string | trace context |
| `last_span_id` | string | latest consumer/producer span |
| `last_event_id` | string | latest event applied |
| `last_event_type` | string | latest event type |
| `last_event_source` | string | latest source service |
| `workflow_updated_at` | timestamp | last workflow mutation |
| `created_at` | timestamp | created time |
| `updated_at` | timestamp | updated time |

### Table: `order_items`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | generated primary key |
| `order_id` | UUID | parent order |
| `product_id` | string | product reference |
| `quantity` | integer | requested quantity |
| `price` | decimal | price snapshot from request |
| `subtotal` | decimal | `price * quantity` |

## Payment service

### Table: `payments`

| Field | Type | Notes |
|---|---|---|
| `id` | UUID | generated primary key |
| `order_id` | UUID | order reference |
| `amount` | decimal | payment amount |
| `currency` | string | payment currency |
| `status` | enum string | `PROCESSING`, `SUCCESS`, `FAILED` |
| `transaction_reference` | string | synthetic transaction ID |
| `created_at` | timestamp | insert time |
| `updated_at` | timestamp | latest update time |

## MongoDB-backed services

## Product service

### Collection: `products`

| Field | Type | Notes |
|---|---|---|
| `id` | string | Mongo document ID |
| `name` | string | product name |
| `description` | string | product description |
| `price` | decimal | selling price |
| `currency` | string | e.g. `USD` |
| `category` | string | optional category |
| `brand` | string | optional brand |
| `status` | enum | `ACTIVE`, `INACTIVE` |
| `createdAt` | instant | created time |
| `updatedAt` | instant | updated time |

## Notification service

### Collection: `notifications`

| Field | Type | Notes |
|---|---|---|
| `id` | string | Mongo document ID |
| `orderId` | UUID | order reference |
| `userId` | string | user reference |
| `type` | enum | business notification type |
| `channel` | enum | `EMAIL`, `SMS`, `PUSH` |
| `status` | enum | `PENDING`, `SENT`, `FAILED` |
| `message` | string | notification body |
| `recipient` | string | resolved delivery target |
| `createdAt` | instant | insert time |
| `sentAt` | instant | set when simulated send succeeds |
| `updatedAt` | instant | latest change |

## Data ownership rules

- no cross-service foreign keys exist
- references between services are stored as IDs only
- each service evolves its schema independently
- cross-service consistency is handled through APIs and events, not shared tables
