# API Contracts

## Scope

This document describes the current implemented HTTP contracts exposed through the API Gateway. It focuses on source-of-truth behavior rather than aspirational future endpoints.

## Conventions

### Gateway base URL

`/api/v1`

### Authentication

- Public:
  - `POST /api/v1/auth/login`
  - `POST /api/v1/users/register`
  - `GET /api/v1/users?email=...`
- Protected:
  - most catalog, inventory, order, payment, and notification routes

### Standard response shape

Most business services use the shared `ApiResponse<T>` envelope:

```json
{
  "success": true,
  "message": "Products fetched successfully",
  "data": [],
  "timestamp": "2026-05-04T09:00:00Z",
  "traceId": "..."
}
```

Exceptions:

- `auth-service` login returns `AuthResponse` directly
- some read endpoints such as order and payment controllers currently return entities directly instead of `ApiResponse`

### Error handling

Shared exception handling is provided by `smartcart-common`. Business exceptions are mapped to consistent HTTP status codes with `message`, `errorCode`, and `traceId`.

## Auth service

### `POST /api/v1/auth/login`

Authenticates a user by email and password.

Request:

```json
{
  "email": "user@example.com",
  "password": "StrongPassword@123"
}
```

Response:

```json
{
  "accessToken": "jwt",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### `GET /.well-known/jwks.json`

Exposes RSA public key metadata for JWT validation.

## User service

### `POST /api/v1/users/register`

Registers a new user.

Request:

```json
{
  "firstName": "Yogesh",
  "lastName": "Jathar",
  "phoneNo": "9999999999",
  "email": "user@example.com",
  "password": "StrongPassword@123",
  "role": "ROLE_USER"
}
```

Response:

- `200 OK`
- wrapped in `ApiResponse`

### `GET /api/v1/users?email={email}`

Returns user authentication details by email.

Current usage:

- internal login-time lookup by `auth-service`

Response:

- `200 OK`
- returns `Optional<UserAuthDetails>`

## Product service

### `POST /api/v1/products`

Creates a product.

Protected route.

Request:

```json
{
  "name": "Running Shoes",
  "description": "Lightweight running shoes",
  "price": 2500,
  "currency": "USD",
  "category": "Footwear",
  "brand": "Nike"
}
```

### `GET /api/v1/products`

Returns all products.

### `GET /api/v1/products/{id}`

Returns a single product by ID.

## Inventory service

### `POST /api/v1/inventory`

Adds stock for a product.

Request:

```json
{
  "productId": "product-id",
  "quantity": 10,
  "warehouseLocation": "WH-01"
}
```

### `GET /api/v1/inventory/{productId}`

Returns inventory for a product.

### `POST /api/v1/inventory/reserve/{productId}?quantity={n}`

Reserves stock for a product.

Primary caller:

- internal business workflow

### `POST /api/v1/inventory/release/{productId}?quantity={n}`

Releases reserved stock.

### `POST /api/v1/inventory/confirm/{productId}?quantity={n}`

Confirms reserved stock after payment success.

## Order service

### `POST /api/v1/orders`

Creates an order.

Request:

```json
{
  "userId": "user-uuid",
  "currency": "USD",
  "items": [
    {
      "productId": "product-id",
      "quantity": 2,
      "price": 2500
    }
  ]
}
```

Response:

- `200 OK`
- current controller returns the created `Order` entity directly

### `GET /api/v1/orders/{orderId}`

Returns order details.

### `GET /api/v1/orders/user/{userId}`

Returns all orders for a user.

### `GET /api/v1/orders/{orderId}/workflow`

Returns workflow metadata for an order.

### `PUT /api/v1/orders/{orderId}/cancel`

Cancels an order if it is still cancellable.

Response:

- wrapped in `ApiResponse<Order>`

## Payment service

### `GET /api/v1/payments/order/{orderId}`

Returns payment record for the specified order.

Current note:

- payment creation is event-driven
- there is no public `POST /payments` endpoint in the current codebase

## Notification service

### `GET /api/v1/notifications/order/{orderId}`

Returns notifications for an order.

### `GET /api/v1/notifications/user/{userId}`

Returns notifications for a user.

## Explicitly not implemented yet

These are common commerce capabilities but are not current gateway contracts:

- refresh token flow
- logout endpoint
- user profile CRUD
- address management
- product update/delete/search pagination
- payment webhook endpoint
- manual notification creation endpoint
- persisted idempotency key support
