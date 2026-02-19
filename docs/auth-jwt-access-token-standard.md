# Access Token – JWT Standard (E-Commerce)

---

## Purpose
Represents authenticated user identity and authorization claims.
Used by downstream microservices for stateless validation.

---

## Example Payload

```json
{
  "sub": "b8f3e1c2-45d7-4b55-9f0a-1c2d3e4f5678",
  "roles": ["ROLE_USER"],
  "token_type": "access",
  "iss": "auth-service",
  "iat": 1718500000,
  "exp": 1718503600,
  "jti": "a1b2c3d4e5f6g7"
}

```

## Standard Claims

| Claim        | Description                                      |
|--------------|--------------------------------------------------|
| `sub`        | Unique immutable user identifier (UUID)         |
| `roles`      | Authorization roles for access control          |
| `token_type` | Identifies token category (`access`)            |
| `iss`        | Issuer (Authentication Service)                 |
| `iat`        | Issued timestamp (epoch)                        |
| `exp`        | Expiration timestamp (short-lived)              |
| `jti`        | Unique token ID (revocation support)            |

