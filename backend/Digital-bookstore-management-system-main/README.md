# 📚 Digital Bookstore Management System

> A scalable, enterprise-grade Digital Bookstore platform built using **Java 21, Spring Boot, and Spring Cloud Microservices**. The project demonstrates modern backend engineering practices including distributed system design, centralized authentication, service discovery, fault tolerance, clean architecture, and comprehensive testing.

---

## 📖 Overview

The Digital Bookstore Management System is a distributed e-commerce backend that simulates a real-world online bookstore. Instead of building a monolithic application, the system follows a **Microservices Architecture**, where every business capability is implemented as an independent service.

Each microservice owns its own business logic and database interactions while communicating through REST APIs. The architecture is designed for scalability, maintainability, and fault tolerance using Spring Cloud components.

---

# 🏗️ Architecture

```
                           +-------------------+
                           |      Client       |
                           +---------+---------+
                                     |
                                     |
                           +---------v---------+
                           |    API Gateway    |
                           +---------+---------+
                                     |
              --------------------------------------------------
             |          |          |          |                 |
             |          |          |          |                 |
      +------v----+ +---v----+ +---v----+ +---v----+ +----------v---------+
      | User      | | Auth   | |Catalog | | Order  | | Review Service     |
      | Service   | | Server | |Service | |Service | |                    |
      +------+----+ +----+---+ +----+---+ +----+---+ +----------+---------+
             |           |           |          |                 |
             ------------------------------------------------------
                              |
                      +-------v--------+
                      |   MySQL DB     |
                      +----------------+

                    +----------------------+
                    | Eureka Server        |
                    +----------------------+

                    +----------------------+
                    | Config Server        |
                    +----------------------+
```

---

# 🚀 Features

## User Management

- User Registration
- Secure Login
- JWT Token Generation
- User Profile Management
- Password Encryption
- Role Management

---

## Authentication & Authorization

- JWT-based Authentication
- Stateless Security
- Role-Based Access Control (RBAC)
- Token Expiration
- Secure REST APIs
- Authentication centralized inside Auth Service

---

## Catalog Management

- Add Books
- Update Books
- Delete Books
- Browse Books
- Search Books by
  - Title
  - Author
  - Category
- Pagination
- Sorting

---

## Order Management

- Place Orders
- View Orders
- Order History
- Business Validation

---

## Review Management

- Add Reviews
- View Reviews
- Book Rating Management

---

## Cloud Components

- API Gateway
- Eureka Service Discovery
- Centralized Configuration Server

---

## Fault Tolerance

Implemented using **Resilience4j**

- Circuit Breaker
- Retry
- Time Limiter

---

## API Documentation

- Swagger UI
- OpenAPI Specification

---

## Monitoring

Spring Boot Actuator

- Health Endpoint
- Metrics
- Info Endpoint

---

# 🧩 Microservices

## 1. User Service

Responsible for

- User Registration
- User Profile
- JWT Authentication
- User Database

---

## 2. Authentication Service

Responsible for

- Authentication
- Authorization
- RBAC
- JWT Validation
- Security

---

## 3. Catalog Service

Responsible for

- Books
- Authors
- Categories
- Search
- Pagination
- Sorting

---

## 4. Order Service

Responsible for

- Order Placement
- Order Validation
- Order History

---

## 5. Review Service

Responsible for

- Reviews
- Ratings
- User Feedback

---

## 6. API Gateway

Provides

- Request Routing
- Centralized Entry Point
- Security
- Load Distribution

---

## 7. Eureka Server

Provides

- Service Discovery
- Dynamic Registration

---

## 8. Config Server

Provides

- Centralized Configuration
- Externalized Properties

---

# 🛠 Technology Stack

## Backend

- Java 21
- Spring Boot
- Spring MVC
- Spring Data JPA
- Spring Security

---

## Microservices

- Spring Cloud
- Eureka Server
- API Gateway
- Config Server

---

## Database

- MySQL
- Hibernate
- JPA

---

## Security

- JWT
- RBAC

---

## Fault Tolerance

- Resilience4j
  - Circuit Breaker
  - Retry
  - Time Limiter

---

## Testing

- JUnit 5
- Mockito
- MockMvc
- MockMvcBuilders

---

## Documentation

- Swagger UI
- OpenAPI

---

## Code Quality

- SonarQube

---

## Build Tool

- Maven

---

# 🏛 Project Structure

```
digital-bookstore/

│
├── api-gateway
│
├── auth-service
│
├── user-service
│
├── catalog-service
│
├── order-service
│
├── review-service
│
├── eureka-server
│
├── config-server
│
└── README.md
```

---

# 🗄 Database Design

The project uses **Spring Data JPA** with Hibernate ORM.

Implemented relationships include

- One-to-One
- One-to-Many
- Many-to-One
- Many-to-Many

Also used

- @Embedded
- @Embeddable
- @Enumerated

---

# 🧠 Software Engineering Practices

The project follows several enterprise software engineering principles.

## SOLID Principles

- Single Responsibility Principle
- Open Closed Principle
- Liskov Substitution Principle
- Interface Segregation Principle
- Dependency Inversion Principle

---

## Layered Architecture

```
Controller
      ↓
Service Interface
      ↓
Service Implementation
      ↓
Repository
      ↓
Database
```

---

## Programming to Interfaces

Business logic is written against interfaces rather than concrete implementations, making the application easier to test, maintain, and extend.

---

## Dependency Injection

Used constructor injection with Spring IoC.

Common annotations include

- @Service
- @Repository
- @RestController
- @Component
- @Configuration

---

## DTO Pattern

The application uses DTOs (Java Records) for

- Request Objects
- Response Objects

Benefits

- Loose Coupling
- API Stability
- Security
- Cleaner Serialization

---

## Validation

Used Jakarta Bean Validation

Examples

- @NotBlank
- @NotNull
- @Size
- @Email
- @Valid

---

## Transaction Management

Used

```
@Transactional
```

to maintain database consistency.

---

# 🧪 Testing

Implemented testing across multiple layers.

## Repository Layer

- JUnit 5

## Service Layer

- JUnit 5
- Mockito

## Controller Layer

- MockMvc
- MockMvcBuilders
- Mockito

Testing includes

- Unit Tests
- Service Tests
- REST Controller Tests

---

# 📑 API Documentation

Swagger UI is integrated for interactive API testing.

Provides

- Endpoint Documentation
- Request Models
- Response Models
- Error Responses

---

# ⚡ Resilience

The application improves reliability using Resilience4j.

Implemented patterns

- Circuit Breaker
- Retry
- Time Limiter

This prevents cascading failures when dependent services become unavailable.

---

# 📊 Code Quality

Code quality is continuously evaluated using SonarQube.

Checks include

- Bugs
- Vulnerabilities
- Code Smells
- Complexity
- Test Coverage
- Maintainability

---

# 🔐 Security

Security implementation includes

- JWT Authentication
- Stateless Sessions
- Role-Based Access Control
- Token Validation
- Secure REST APIs

---

# 🚀 Future Enhancements

- Docker & Docker Compose
- Kubernetes Deployment
- Redis Caching
- Apache Kafka Event Streaming
- Elasticsearch
- Distributed Tracing
- CI/CD Pipeline
- Prometheus & Grafana Monitoring
- Centralized Logging (ELK Stack)
- OAuth2 / OpenID Connect
- Payment Gateway Integration

---

# 👨‍💻 Author

**Kushal Das**

Backend Developer | Java | Spring Boot | Microservices | REST APIs | MySQL
