# SmartCart Frontend

React frontend for the actual SmartCart application.

## Run

```bash
npm install
npm run dev
```

## Environment

- `VITE_API_BASE=/api/v1`

By default Vite proxies `/api` to `http://localhost:8080`, so the UI calls the API Gateway locally.

## Included flows

- login
- user registration
- product catalog
- cart and checkout
- order history
- workflow and payment status lookup
- notifications
- admin product creation
- admin inventory updates
