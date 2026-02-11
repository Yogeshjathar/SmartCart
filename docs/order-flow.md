# SmartCart – Order Flow

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

## Failure Scenarios

- Inventory failure → Order cancelled.
- Payment failure → Inventory released.
- Notification failure → Logged but order not rolled back.

---

## Future Improvement

- Replace synchronous calls with Kafka events.
- Implement Saga Pattern for distributed transactions.
