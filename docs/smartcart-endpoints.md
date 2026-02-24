# E-Commerce Microservices – API Endpoints

## 1. Product Service

| Method | Endpoint       | Purpose                | Request Flow                  |
| ------ | -------------- | ---------------------- | ----------------------------- |
| POST   | /products      | Create a new product   | Admin → Product Service → DB  |
| GET    | /products      | List all products      | Client → Product Service → DB |
| GET    | /products/{id} | Get product details    | Client → Product Service → DB |
| PUT    | /products/{id} | Update product details | Admin → Product Service → DB  |
| DELETE | /products/{id} | Soft delete product    | Admin → Product Service → DB  |

---

## 2. Inventory Service

| Method | Endpoint               | Purpose                                   | Request Flow                           |
| ------ | ---------------------- | ----------------------------------------- | -------------------------------------- |
| POST   | /inventory             | Add stock for product                     | Admin → Inventory Service → DB         |
| GET    | /inventory/{productId} | Check stock availability                  | Order Service → Inventory Service → DB |
| PUT    | /inventory/reserve     | Reserve stock during checkout             | Order Service → Inventory Service → DB |
| PUT    | /inventory/release     | Release reserved stock (payment failure)  | Order Service → Inventory Service → DB |
| PUT    | /inventory/confirm     | Confirm stock deduction (payment success) | Order Service → Inventory Service → DB |

---

## 3. Order Service

| Method | Endpoint              | Purpose           | Request Flow                |
| ------ | --------------------- | ----------------- | --------------------------- |
| POST   | /orders               | Create new order  | Client → Order Service      |
| GET    | /orders/{id}          | Get order details | Client → Order Service → DB |
| GET    | /orders/user/{userId} | Get order history | Client → Order Service → DB |
| PUT    | /orders/{id}/cancel   | Cancel an order   | Client → Order Service → DB |

Order Creation Flow:

1. Order created with status CREATED
2. Inventory reserved
3. Payment initiated
4. On success → Inventory confirmed → Order CONFIRMED → Notification sent
5. On failure → Inventory released → Order CANCELLED

---

## 4. Payment Service

| Method | Endpoint            | Purpose                            | Request Flow                      |
| ------ | ------------------- | ---------------------------------- | --------------------------------- |
| POST   | /payments/initiate  | Initiate payment process           | Order Service → Payment Service   |
| POST   | /payments/confirm   | Confirm payment (gateway callback) | Payment Gateway → Payment Service |
| GET    | /payments/{orderId} | Get payment status                 | Order Service → Payment Service   |

---

## 5. Notification Service

| Method | Endpoint                     | Purpose                    | Request Flow                       |
| ------ | ---------------------------- | -------------------------- | ---------------------------------- |
| POST   | /notifications               | Send notification manually | Service → Notification Service     |
| GET    | /notifications/user/{userId} | Get user notifications     | Client → Notification Service → DB |

Event-Driven Flow (Recommended for Production):

* Order Service publishes event
* Notification Service consumes event
* Sends Email/SMS
* Stores notification status
