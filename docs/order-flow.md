# SmartCart – Order Flow

## Order Lifecycle
CREATED

↓

RESERVED (Inventory reserved)

↓

PAYMENT_PENDING

↓

CONFIRMED (Payment success)

↓

COMPLETED (Later stage)

OR

FAILED / CANCELLED

## Step-by-Step Order Placement

1. User authenticates via Auth Service.
2. User places order through API Gateway.
3. Gateway validates JWT.
4. Order Service creates order (PENDING state).
5. Order Service calls Inventory Service to reserve stock.
6. Inventory confirms reservation.
7. Order Service calls Payment Service.
8. Payment confirms transaction.
9. Order status updated to CONFIRMED.
10. Notification Service sends confirmation email.

---

---

# Checkout Flow (SAGA Pattern – Orchestration)

## Step 1: Create Order

Client → Order Service → Create Order

- Order status = `CREATED`

---

## Step 2: Reserve Inventory

Order Service → Inventory Service → Reserve Stock

### If Success:
- Update order status = `RESERVED`

### If Failure:
- Update order status = `FAILED`
- Stop process

---

## Step 3: Initiate Payment

Order Service → Payment Service → Initiate Payment
- Update order status = `PAYMENT_PENDING`

### If Payment Success:
- Update order status = `CONFIRMED`
- Call Inventory Service → Confirm Stock
- Publish Order Confirmed Event
- Later → Update status = `COMPLETED`

### If Payment Failure:
- Call Inventory Service → Release Stock
- Update order status = `FAILED`
- Publish Order Failed Event
- Stop process

## Failure Scenarios

- Inventory failure → Order cancelled.
- Payment failure → Inventory released.
- Notification failure → Logged but order not rolled back.

---

## Future Improvement

- Replace synchronous calls with Kafka events.
- Implement Saga Pattern for distributed transactions.
