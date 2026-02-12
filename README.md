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

- Java 17+
- Spring Boot
- Spring Cloud (Eureka, OpenFeign)
- Spring Security
- Maven (Multi-module)
- Lombok
- Micrometer + Prometheus
- Docker (Planned)
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

> Note: In production, individual services are not accessed directly. All external traffic flows through the API Gateway.
---

## 📌 Future Improvements

- Distributed tracing
- Centralized logging
- CI/CD pipeline
- Docker Compose setup
- Kubernetes deployment
- Resilience4j circuit breaker
- API documentation (OpenAPI)

---

## 👨‍💻 Author

SmartCart - Learning-focused enterprise-grade architecture project.
