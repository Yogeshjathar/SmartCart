# Security and Authentication

## Scope

This document describes the current authentication and authorization model implemented in SmartCart.

## Authentication model

SmartCart uses stateless bearer-token authentication.

### Token issuer

`auth-service`

### Signing algorithm

- RSA
- JWT signed with private key
- downstream validation through public key exposure

### Key distribution

`auth-service` exposes:

- `GET /.well-known/jwks.json`

Other services validate JWTs through the configured `jwk-set-uri`.

## Login flow

1. Client sends credentials to `POST /api/v1/auth/login`
2. `auth-service` calls `user-service` to fetch user details by email
3. Password is verified using BCrypt
4. JWT access token is generated
5. Client includes `Authorization: Bearer <token>` on protected requests

## JWT claims currently issued

| Claim | Meaning |
|---|---|
| `sub` | user ID |
| `roles` | role string from user record |
| `firstName` | first name |
| `lastName` | last name |
| `name` | display-friendly name |
| `iss` | token issuer |
| `iat` | issued time |
| `exp` | expiration time |
| `jti` | token identifier |

JWT header includes:

- `kid`

## Gateway security model

`api-gateway` uses Spring WebFlux Security with three security filter chains.

### Public routes

- `/api/v1/auth/**`
- `/api/v1/users/**`

### Open documentation and actuator routes

- `/actuator/**`
- `/swagger-ui/**`
- `/swagger-ui.html`
- `/v3/api-docs/**`

### Protected routes

All other routes require a valid JWT.

## Service-level security model

Backend services that expose protected routes are configured as OAuth2 resource servers and validate JWTs against the auth JWKS endpoint.

### Auth service

- permits login and JWKS
- uses a custom authentication filter/provider internally

### User service

- permits registration and auth lookup endpoint
- secures other routes

### Order, product, inventory, payment, notification services

- secure business routes
- generally permit Swagger and actuator endpoints

## Role usage

The code currently stores role values such as:

- `ROLE_USER`
- `ROLE_ADMIN`

Frontend uses the role claim to show admin operations. Business-route authorization is present at the security layer, but finer-grained role checks across all domains are still limited and should be strengthened.

## Correlation and trace headers

Current headers propagated across the platform include:

- `X-Correlation-Id`
- `X-Trace-Id`
- `X-Span-Id`
- `X-Aggregate-Id`
- `X-Aggregate-Type`
- `X-Event-Id`
- `X-Event-Type`
- `X-Source-Service`

These are used for diagnostics rather than end-user auth.

## Current limitations

- no refresh token flow
- no token revocation store
- no logout invalidation
- no rate limiting at the gateway yet
- no fine-grained authorization policy layer beyond current route configuration and token roles

## Why this design is still strong

- stateless auth scales well
- RSA + JWKS avoids sharing a symmetric secret with every service
- gateway and resource-server validation align with real distributed-system security patterns
