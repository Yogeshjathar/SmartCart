# 🛒 SmartCart Microservices Platform

SmartCart is a scalable, cloud-native e-commerce platform built using Spring Boot and Spring Cloud microservices architecture.

---

## 🏗 Architecture Overview

SmartCart follows a distributed microservices architecture:

- Discovery Server (Eureka)
- API Gateway
- Auth Service
- User Service
- Product Service
- Inventory Service
- Order Service
- Payment Service
- Notification Service
- Shared Common Library

---

## ⚙️ Tech Stack

- Java 21+
- Spring Boot
- Spring Cloud (Eureka, OpenFeign)
- Spring Security
- Maven (Multi-module)
- Lombok
- Micrometer + Prometheus
- Grafana
- OpenTelemetry + zipkin
- JUnit + Mockito
- Docker
- Kubernetes (Future-ready)

---

## 📁 Project Structure

smartcart-parent  
├── smartcart-common  
├── discovery-server  
├── api-gateway  
├── auth-service  
├── user-service  
├── product-service  
├── inventory-service  
├── order-service  
├── payment-service  
└── notification-service

---

## Features include:
- User registration and authentication (JWT)
- Product catalog management
- Inventory tracking
- Order processing
- Payment integration (mocked)
- Notification system (email/SMS)
- Centralized API documentation (Swagger)
- Service discovery (Eureka)
- Inter-service communication (OpenFeign)
- Centralized logging
- Monitoring and tracing (Micrometer, Prometheus, Grafana, OpenTelemetry)

---

## 🚀 Running Locally

1. Build all modules:
   `mvn clean install`

2. Start services in order:
    - discovery-server
    - api-gateway
    - other services


3. Access:

   Eureka Dashboard → http://localhost:8761  
   Gateway → http://localhost:8080

   Auth Service → http://localhost:8081  
   User Service → http://localhost:8082  
   Product Service → http://localhost:8083  
   Inventory Service → http://localhost:8084  
   Order Service → http://localhost:8085  
   Payment Service → http://localhost:8086  
   Notification Service → http://localhost:8087


4. Monitoring:

   - Prometheus → http://localhost:9090
   - Grafana → http://localhost:3000
   - zipkin → http://localhost:9411

> Note: In production, individual services are not accessed directly. All external traffic flows through the API Gateway.
---

# SmartCart API Documentation

## Overview

SmartCart is built using a **microservices architecture**, where each service exposes its own REST APIs.  
To simplify API discovery and improve the developer experience, we implemented **centralized API documentation using Swagger (OpenAPI)**.

Instead of hosting Swagger UI separately for every service, all API specifications are **aggregated through the API Gateway**, allowing developers to access documentation from a **single interface**.

This approach is widely used in **large-scale distributed systems** to manage APIs efficiently.

---

## API Documentation Strategy

Each microservice generates its own **OpenAPI specification**, which is then aggregated by the **API Gateway**.

### API Definitions

Each service exposes its OpenAPI specification through the API Gateway.

| Service | OpenAPI Endpoint |
|--------|------------------|
| Auth Service | http://localhost:8080/v3/api-docs/auth-service |
| User Service | http://localhost:8080/v3/api-docs/user-service |
| Product Service | http://localhost:8080/v3/api-docs/product-service |
| Order Service | http://localhost:8080/v3/api-docs/order-service |
| Inventory Service | http://localhost:8080/v3/api-docs/inventory-service |
| Payment Service | http://localhost:8080/v3/api-docs/payment-service |
| Notification Service | http://localhost:8080/v3/api-docs/notification-service |

### Swagger UI

Access the unified Swagger UI: http://localhost:8080/swagger-ui.html

---

### Start SmartCart
#### To start the SmartCart platform using Docker Compose, run the following command in the terminal:
```bash
docker compose -f docker-compose.infra.yml -f docker-compose.services.yml up -d
```

You can also run using Makefile:
```bash
make up
make logs
make down
```
### Then to start all services without infrastructure components (like Prometheus, Grafana, zipkin), run:
```bash
docker compose -f docker-compose.services.yml up -d
```

---
## 📌 Future Improvements

- CI/CD pipeline
- Kubernetes deployment
- Resilience4j circuit breaker, rate limiting, and retry mechanisms

## 👨‍💻 Author

SmartCart - Learning-focused enterprise-grade architecture project.
