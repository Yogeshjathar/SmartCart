# SmartCart – Service Contracts
**Architecture Style:** Domain-Oriented Microservices  
**Version:** v1  
**Gateway Base URL:** `/api/v1`  
**Communication:** REST (Sync), Events (Async – Kafka Future)  
**Authentication:** JWT Bearer Token  
**Design Goal:** Horizontally scalable, fault tolerant, low latency system

---

# 1. Global API Standards

## 1.1 Headers (Mandatory Across Services)

| Header | Required | Purpose |
|--------|----------|----------|
| Authorization | Yes | `Bearer <JWT>` |
| X-Request-Id | Yes | Distributed tracing |
| Idempotency-Key | Required for POST (orders, payments) | Prevent duplicate processing |
| X-Service-Name | Internal | Observability |

---

## 1.2 API Versioning Strategy

- URI Versioning: `/api/v1`
- Breaking changes → `/api/v2`
- Backward compatibility supported for minimum 6 months

---

## 1.3 Standard Success Response

```json
{
  "data": {},
  "message": "Success"
}
```

---

## 1.4 Standard Error Response

```json
{
  "timestamp": "2026-02-12T10:00:00Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "path": "/api/v1/orders",
  "requestId": "uuid"
}
```

---

## 1.5 Idempotency Rules

- Required for:
    - Order creation
    - Payment initiation
- Server stores `Idempotency-Key` with response
- Duplicate key → return previous response

---

## 1.6 Non-Functional Guarantees

| Requirement | Strategy |
|-------------|----------|
| 99.9% Availability | Horizontal scaling |
| <200ms Read Latency | Indexing + caching |
| Fault Tolerance | Circuit breaker |
| High Traffic Spikes | Auto-scaling |
| Observability | Metrics + Logs + Tracing |
| No Shared DB | Database-per-service |

---

# 2. Auth Service

**Responsibility:** Authentication & Token Issuance  
**Base Path:** `/auth`

---

## 2.1 Register User

**POST** `/auth/register`

### Request
```json
{
  "email": "user@example.com",
  "password": "StrongPassword@123",
  "role": "CUSTOMER"
}
```

### Response (201)
```json
{
  "data": {
    "userId": "uuid"
  },
  "message": "User registered successfully"
}
```

### Constraints
- Email unique
- Password hashed (BCrypt)
- 409 if email exists

---

## 2.2 Login

**POST** `/auth/login`

### Request
```json
{
  "email": "user@example.com",
  "password": "StrongPassword@123"
}
```

### Response
```json
{
  "data": {
    "accessToken": "jwt-token",
    "refreshToken": "refresh-token",
    "expiresIn": 900
  }
}
```

### Token Design
JWT contains:
- userId
- role
- issuedAt
- expiry

Short-lived access token (15 mins).

---

# 3. User Service

**Responsibility:** Profile & Address Management  
**Base Path:** `/users`  
**Database:** user_profile, user_address

---

## 3.1 Get Profile

**GET** `/users/{userId}`

### Response
```json
{
  "data": {
    "userId": "uuid",
    "name": "Yogesh",
    "email": "user@example.com",
    "phone": "9999999999",
    "addresses": []
  }
}
```

---

## 3.2 Add Address

**POST** `/users/{userId}/addresses`

```json
{
  "street": "MG Road",
  "city": "Bangalore",
  "state": "Karnataka",
  "zipCode": "560001",
  "country": "India"
}
```

---

## 3.3 Update Profile

**PUT** `/users/{userId}`

Uses optimistic locking (version field).

---

# 4. Product Service

**Responsibility:** Product Catalog & Reviews  
**Base Path:** `/products`  
**Database:** product, category, review

---

## 4.1 Create Product (Admin)

**POST** `/products`

```json
{
  "name": "Nike Shoes",
  "description": "Running shoes",
  "price": 2500,
  "category": "Footwear"
}
```

---

## 4.2 Get Product

**GET** `/products/{productId}`

```json
{
  "data": {
    "productId": "uuid",
    "name": "Nike Shoes",
    "price": 2500,
    "available": true
  }
}
```

---

## 4.3 Search Products

**GET**
```
/products?keyword=shoes&minPrice=1000&maxPrice=5000&page=0&size=20&sort=price,asc
```

### Scalability Notes
- Indexed DB columns
- Pagination mandatory
- Future: ElasticSearch integration

---

## 4.4 Add Review

**POST** `/products/{productId}/reviews`

```json
{
  "userId": "uuid",
  "rating": 5,
  "comment": "Excellent product"
}
```

Idempotent by `(userId, productId)`.

---

# 5. Inventory Service

**Responsibility:** Stock Control & Reservation  
**Base Path:** `/inventory`  
**Critical for preventing overselling**

---

## 5.1 Check Stock

**GET** `/inventory/{productId}`

```json
{
  "data": {
    "productId": "uuid",
    "availableQuantity": 120
  }
}
```

---

## 5.2 Reserve Stock

**POST** `/inventory/reserve`

```json
{
  "orderId": "uuid",
  "productId": "uuid",
  "quantity": 2
}
```

### Response
```json
{
  "data": {
    "reservationId": "uuid",
    "status": "RESERVED"
  }
}
```

### Rules
- Atomic stock deduction
- Reservation expires in 10 minutes
- Uses row-level locking or distributed lock

---

## 5.3 Release Stock

**POST** `/inventory/release`

```json
{
  "orderId": "uuid"
}
```

---

# 6. Order Service

**Responsibility:** Order Lifecycle  
**Base Path:** `/orders`  
**Database:** orders, order_items

---

## 6.1 Create Order

**POST** `/orders`

Header:
```
Idempotency-Key: unique-key
```

```json
{
  "userId": "uuid",
  "items": [
    {
      "productId": "uuid",
      "quantity": 2
    }
  ],
  "addressId": "uuid"
}
```

### Response
```json
{
  "data": {
    "orderId": "uuid",
    "status": "CREATED"
  }
}
```

---

## 6.2 Get Orders by User

**GET**
```
/orders?userId=uuid&page=0&size=10&sort=createdAt,desc
```

Pagination required.

---

## 6.3 Update Order Status (Internal)

**PATCH** `/orders/{orderId}/status`

```json
{
  "status": "PAID"
}
```

### Order States
```
CREATED → RESERVED → PAID → SHIPPED → DELIVERED
                  ↘ FAILED
```

---

# 7. Payment Service

**Responsibility:** Payment Processing  
**Base Path:** `/payments`

---

## 7.1 Initiate Payment

**POST** `/payments`

Header:
```
Idempotency-Key: unique-key
```

```json
{
  "orderId": "uuid",
  "amount": 2500,
  "paymentMethod": "UPI"
}
```

### Response
```json
{
  "data": {
    "paymentId": "uuid",
    "status": "PENDING"
  }
}
```

---

## 7.2 Payment Webhook

**POST** `/payments/webhook`

```json
{
  "paymentId": "uuid",
  "status": "SUCCESS"
}
```

---

# 8. Notification Service

**Responsibility:** Email & SMS  
**Base Path:** `/notifications`

---

## 8.1 Send Notification (Internal)

**POST** `/notifications`

```json
{
  "userId": "uuid",
  "type": "ORDER_CONFIRMATION",
  "message": "Your order has been placed successfully"
}
```

Asynchronous processing recommended.

---

# 9. Event Contracts (Future Kafka Integration)

---

## 9.1 ORDER_CREATED

```json
{
  "eventType": "ORDER_CREATED",
  "orderId": "uuid",
  "userId": "uuid",
  "items": []
}
```

---

## 9.2 PAYMENT_SUCCESS

```json
{
  "eventType": "PAYMENT_SUCCESS",
  "orderId": "uuid"
}
```

---

## 9.3 PAYMENT_FAILED

```json
{
  "eventType": "PAYMENT_FAILED",
  "orderId": "uuid",
  "reason": "INSUFFICIENT_FUNDS"
}
```

---

# 10. Scalability & Resilience Considerations

| Concern | Strategy |
|----------|----------|
| Duplicate Order Requests | Idempotency-Key |
| Payment Retry | Event-based reconciliation |
| Inventory Oversell | Atomic reservation |
| Service Failure | Circuit breaker |
| High Read Traffic | Caching layer |
| DB Slowdown | Read replicas |
| Notification Failure | Retry + DLQ |

---

# 11. Observability Standards

- Metrics: Prometheus
- Logs: Structured JSON
- Tracing: Distributed tracing with requestId
- Health Checks: `/actuator/health`

---

# 12. Security Standards

- JWT-based stateless auth
- Role-based access control
- HTTPS enforced
- Rate limiting at Gateway
- Input validation at service level

---

# Summary

SmartCart contracts are:

- Versioned
- Idempotent
- Stateless
- Horizontally scalable
- Database-per-service compliant
- Observability-ready
- Designed for event-driven evolution

