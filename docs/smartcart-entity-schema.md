# E-Commerce Microservices – Entity Schema

## 1. Product Service

### Product

| Field       | Type                    | Description                    |
| ----------- | ----------------------- | ------------------------------ |
| id          | UUID                    | Unique product identifier      |
| name        | String                  | Product name                   |
| description | Text                    | Detailed product description   |
| price       | BigDecimal              | Selling price                  |
| currency    | String                  | Currency code (INR, USD, etc.) |
| category    | String                  | Product category               |
| brand       | String                  | Brand name                     |
| status      | ENUM (ACTIVE, INACTIVE) | Product availability status    |
| createdAt   | Instant                 | Creation timestamp             |
| updatedAt   | Instant                 | Last update timestamp          |

---

## 2. Inventory Service

### Inventory

| Field             | Type    | Description                             |
| ----------------- | ------- | --------------------------------------- |
| id                | UUID    | Unique inventory identifier             |
| productId         | UUID    | Product reference (no cross-service FK) |
| availableQuantity | Integer | Current available stock                 |
| reservedQuantity  | Integer | Quantity reserved during checkout       |
| warehouseLocation | String  | Warehouse identifier/location           |
| lastUpdated       | Instant | Last update timestamp                   |

---

## 3. Order Service

### Order

| Field         | Type                                                     | Description             |
| ------------- | -------------------------------------------------------- | ----------------------- |
| id            | UUID                                                     | Unique order identifier |
| userId        | UUID                                                     | Customer reference      |
| status        | ENUM (CREATED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED) | Order lifecycle status  |
| totalAmount   | BigDecimal                                               | Total order amount      |
| currency      | String                                                   | Currency code           |
| paymentStatus | ENUM (PENDING, SUCCESS, FAILED)                          | Payment state           |
| createdAt     | Instant                                                  | Creation timestamp      |

### OrderItem

| Field           | Type       | Description                     |
| --------------- | ---------- | ------------------------------- |
| id              | UUID       | Unique order item identifier    |
| orderId         | UUID       | Parent order reference          |
| productId       | UUID       | Product reference               |
| quantity        | Integer    | Quantity ordered                |
| priceAtPurchase | BigDecimal | Price snapshot at purchase time |

---

## 4. Payment Service

### Payment

| Field         | Type                              | Description                        |
| ------------- | --------------------------------- | ---------------------------------- |
| id            | UUID                              | Unique payment identifier          |
| orderId       | UUID                              | Order reference                    |
| transactionId | String                            | External payment gateway reference |
| amount        | BigDecimal                        | Paid amount                        |
| currency      | String                            | Currency code                      |
| paymentMethod | ENUM (CARD, UPI, NETBANKING)      | Mode of payment                    |
| status        | ENUM (INITIATED, SUCCESS, FAILED) | Payment lifecycle status           |
| createdAt     | Instant                           | Creation timestamp                 |

---

## 5. Notification Service

### Notification

| Field     | Type                | Description                    |
| --------- | ------------------- | ------------------------------ |
| id        | UUID                | Unique notification identifier |
| userId    | UUID                | Receiver reference             |
| type      | ENUM (EMAIL, SMS)   | Notification channel           |
| message   | Text                | Notification content           |
| status    | ENUM (SENT, FAILED) | Delivery status                |
| createdAt | Instant             | Creation timestamp             |
