
---

## 1. Authentication & Authorization

| Method | Endpoint | Description | Request Body | Response |
|--------|---------|------------|-------------|---------|
| POST | `/auth/register` | Register a new user | `{ "name": "string", "email": "string", "password": "string", "role": "string" }` | `{ "user": {...}, "token": "JWT" }` |
| POST | `/auth/login` | Login user and generate JWT | `{ "email": "string", "password": "string" }` | `{ "user": {...}, "token": "JWT" }` |
| POST | `/auth/refresh-token` | Refresh expired JWT | `{ "refreshToken": "string" }` | `{ "token": "JWT" }` |
| POST | `/auth/logout` | Logout user (invalidate token) | `{ "token": "string" }` | `{ "message": "Logged out successfully" }` |

---

## 2. User Management

| Method | Endpoint | Description | Request Body / Params | Response |
|--------|---------|------------|---------------------|---------|
| GET | `/` | List all users (Admin only) | `?page=1&size=20&sort=name,asc` | Paginated list of users |
| GET | `/{id}` | Get user details | Path param: `id` | User object |
| PUT | `/{id}` | Update user info | `{ "name": "string", "email": "string", "role": "string" }` | Updated user object |
| PATCH | `/{id}/status` | Activate / deactivate user | `{ "status": "ACTIVE" | "INACTIVE" }` | Success message |
| DELETE | `/{id}` | Delete user | Path param: `id` | `{ "message": "User deleted successfully" }` |

---

## 3. Role & Permission Management

| Method | Endpoint | Description | Request Body | Response |
|--------|---------|------------|-------------|---------|
| GET | `/roles` | List all roles | - | List of roles |
| POST | `/roles` | Create a new role | `{ "name": "string", "permissions": ["string"] }` | Role object |
| PUT | `/roles/{id}` | Update role | `{ "name": "string", "permissions": ["string"] }` | Updated role object |
| DELETE | `/roles/{id}` | Delete role | Path param: `id` | `{ "message": "Role deleted successfully" }` |

---

## 4. User Profile / Self-Service

| Method | Endpoint | Description | Request Body | Response |
|--------|---------|------------|-------------|---------|
| GET | `/profile` | Get logged-in user profile | JWT in header | User object |
| PUT | `/profile` | Update profile | `{ "name": "string", "email": "string", "phone": "string" }` | Updated profile |
| PATCH | `/profile/password` | Change password | `{ "oldPassword": "string", "newPassword": "string" }` | `{ "message": "Password updated successfully" }` |
| GET | `/profile/orders` | View user orders | JWT in header | List of orders |

---

## 5. Security & Verification

| Method | Endpoint | Description | Request Body | Response |
|--------|---------|------------|-------------|---------|
| POST | `/auth/forgot-password` | Send password reset link | `{ "email": "string" }` | `{ "message": "Password reset link sent" }` |
| POST | `/auth/reset-password` | Reset password | `{ "token": "string", "newPassword": "string" }` | `{ "message": "Password reset successfully" }` |
| POST | `/auth/verify-email` | Verify email | `{ "token": "string" }` | `{ "message": "Email verified successfully" }` |

---

## Notes & Best Practices

1. **JWT-based authentication** for stateless APIs.
2. **Role-based access control** for admin vs normal users.
3. **Pagination & filtering** for endpoints returning lists (`/users`).
4. **Audit logs** for sensitive actions: password changes, user deletion, role updates.
5. **Idempotency** for POST/PUT endpoints to prevent duplicate operations.
6. All endpoints should respond with proper **HTTP status codes**:
    - `200 OK` – Success
    - `201 Created` – Resource created
    - `400 Bad Request` – Validation error
    - `401 Unauthorized` – Invalid token / not logged in
    - `403 Forbidden` – Access denied
    - `404 Not Found` – Resource not found
    - `500 Internal Server Error` – Unexpected error

