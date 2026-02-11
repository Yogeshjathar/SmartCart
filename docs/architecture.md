# SmartCart – System Architecture

## 1. Architecture Style

SmartCart follows a Domain-Oriented Microservices Architecture
with the following characteristics:

- Service Discovery (Eureka)
- API Gateway (Spring Cloud Gateway)
- Independent Deployable Services
- Database per Service
- Event-Driven Communication (Future: Kafka)
- Stateless Authentication (JWT)
- Horizontal Scalability

---

## 2. High-Level Flow

Client
↓
API Gateway
↓
Discovery Server
↓
Microservices:
- Auth Service
- User Service
- Product Service
- Inventory Service
- Order Service
- Payment Service
- Notification Service

---

## 3. Key Design Principles

- SOLID principles inside each service
- Database-per-service pattern
- No shared database
- Inter-service communication via REST (initially)
- Async events for critical workflows (future enhancement)
- Idempotent APIs
- Stateless services for scalability

---

## 4. Functional

- User authentication & authorization 
- Product catalog (search, filter)
- Cart & checkout 
- Order management
- Payment processing
- Inventory management 
- Notifications 
- Reviews & ratings

---

## 4. Non-Functional Requirements

- Handle 10M requests/day (~115 req/sec average, spikes much higher)
- Low latency (<200ms for reads)
- High availability (99.9%+)
- Fault tolerant 
- Horizontally scalable 
- Secure 
- Observability-ready (metrics, logs, tracing)
