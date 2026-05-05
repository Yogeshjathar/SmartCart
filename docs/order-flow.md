# Order Flow

## Scope

This document describes the current implemented order workflow across `order-service`, `inventory-service`, `payment-service`, and `notification-service`.

## Workflow summary

SmartCart uses an event-driven checkout flow. The order record is created synchronously, but stock reservation, payment outcome, and user notification are progressed asynchronously through Kafka events.

## Status model

### Order status

- `CREATED`
- `RESERVED`
- `CONFIRMED`
- `FAILED`
- `CANCELLED`

### Payment status

- `NOT_STARTED`
- `INITIATED`
- `SUCCESS`
- `FAILED`

## Happy path

### Step 1. Create order

Client sends `POST /api/v1/orders` through the gateway.

`order-service`:

- validates and maps the request
- creates `Order` and `OrderItem` records
- sets workflow metadata such as correlation ID and trace ID
- stores initial state:
  - order status = `CREATED`
  - payment status = `NOT_STARTED`
- publishes `ORDER_CREATED`

### Step 2. Reserve inventory

`inventory-service` listens to `ORDER_CREATED`.

For each order item it:

- loads inventory by `productId`
- verifies `availableQuantity >= requested quantity`
- decrements available stock
- increments reserved stock

If all items are reserved successfully:

- publish `INVENTORY_RESERVED`

If any item fails:

- compensate any already-reserved items for the same order
- publish `INVENTORY_RESERVATION_FAILED`

### Step 3. Initiate and process payment

`payment-service` listens to `INVENTORY_RESERVED`.

It:

- checks whether a payment already exists for the order
- creates a payment record in `PROCESSING`
- simulates gateway success with an 80% success rate
- publishes either:
  - `PAYMENT_SUCCESS`
  - `PAYMENT_FAILED`

### Step 4. Update order workflow state

`order-service` listens to:

- `INVENTORY_RESERVED`
- `INVENTORY_RESERVATION_FAILED`
- `PAYMENT_SUCCESS`
- `PAYMENT_FAILED`

State transitions:

| Event | Order status | Payment status |
|---|---|---|
| `INVENTORY_RESERVED` | `RESERVED` | `INITIATED` |
| `INVENTORY_RESERVATION_FAILED` | `FAILED` | `NOT_STARTED` |
| `PAYMENT_SUCCESS` | `CONFIRMED` | `SUCCESS` |
| `PAYMENT_FAILED` | `FAILED` | `FAILED` |

### Step 5. Finalize inventory

`inventory-service` also listens to payment outcome events.

On `PAYMENT_SUCCESS`:

- reserved stock is confirmed
- reserved quantity is reduced permanently

On `PAYMENT_FAILED`:

- reserved stock is released
- available quantity is restored

### Step 6. Notify the user

`notification-service` listens to:

- `ORDER_CREATED`
- `PAYMENT_SUCCESS`
- `PAYMENT_FAILED`

It creates MongoDB notification records and simulates sending.

## Cancellation flow

Client sends `PUT /api/v1/orders/{orderId}/cancel`.

`order-service`:

- rejects cancellation for `CONFIRMED` or `FAILED` orders
- returns immediately for already cancelled orders
- marks order `CANCELLED`
- publishes `ORDER_CANCELLED`

`inventory-service` listens to `ORDER_CANCELLED` and releases reserved stock for all order items.

## Traceability model

The `Order` entity stores workflow metadata:

- `correlationId`
- `traceId`
- `lastSpanId`
- `lastEventId`
- `lastEventType`
- `lastEventSource`
- `workflowUpdatedAt`

This allows the UI and developers to inspect the latest workflow state from the order itself.

## Failure behavior

### Inventory reservation failure

- order becomes `FAILED`
- payment is never started

### Payment failure

- order becomes `FAILED`
- inventory is released
- notification is still attempted

### Notification failure

- notification record is marked `FAILED`
- order is not rolled back

## Current implementation notes

- The workflow is asynchronous and event-driven now, not “future Kafka”
- There is no DLQ or explicit retry policy yet
- Payment is simulated, not integrated with a real provider
- Inventory reservation is implemented per item with compensation if a later item fails
