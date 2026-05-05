# SmartCart Frontend

This is the React + Vite frontend for SmartCart. It is a developer-facing UI that exercises the current backend capabilities through the API Gateway.

## Supported user flows

- register
- login
- browse catalog
- fetch inventory per product
- add to cart
- checkout
- view order history
- view workflow state
- view payment state
- view notifications
- create product as admin
- add inventory as admin

## Runtime model

- dev server port: `5173`
- API base: `/api/v1`
- Vite proxy target: `http://localhost:8080`

## Run locally

```bash
npm install
npm run dev
```

## Assumptions

- backend stack is already running
- gateway is reachable on `http://localhost:8080`
- JWT returned by `auth-service` is used for protected requests

## Important implementation notes

- session is stored client-side
- cart is stored in `localStorage`
- admin UI visibility depends on the JWT role claim
- the UI reflects current backend contracts; it is not a generic storefront shell
