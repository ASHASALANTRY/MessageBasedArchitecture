# 🧩 Message-Based Architecture (Spring Boot + Azure)

This project demonstrates an **event-driven employee Information system** built on **Azure** using **Spring Boot**, **Azure Service Bus**, **PostgreSQL**, and **Redis**. 
It acts as the **sender/producer** service in a distributed message-based system.
All services except Redis use **Azure Managed Identity** for secure, passwordless authentication.  
The REST API is hosted on an **Azure Web App** protected via **platform-based Azure AD authentication**, ensuring that only authenticated users or service principals can access the endpoints.

---

## 🏗️ System Architecture Overview

The system implements a **message-based asynchronous architecture** designed for scalability, reliability, and loose coupling between producer and consumer components.

![System Architecture](./demo/systemdiagram.jpg)

---

### 🧭 Diagram Legend – How the Flow Maps to the Code

| Diagram Component | Description | Code Component |
|--------------------|--------------|----------------|
| 🖥️ **add-employee** | Client initiates a request to add the information of new employees | `EmployeeController.addEmployee()` |
| 🔷 **employeeprocessqueue** | Azure Service Bus Queue receiving employee messages | Defined in `ServiceBusConfig.java` |
| ⚙️ **Processor Service** | send messages to employee process queue and update cache with processing status, return redis status url | `QueueHelperServiceImpl` + `CacheHelperService` |
| ⚙️ **Receiver Application** | 	Consumes employee process queue messages, creates records in DB, update cache with processed/failed status | `SericeBusListener` |
| 🗄️ **PostgreSQL DB** | Stores employee master data | `BasicDetailRepository` + `BasicDetails` entity |
| 📬 **processedqueue** | Queue for downstream system notifications | Defined in `Receiver Application` |
| 💾 **Redis Cache** | Tracks transient processing statuses (processing/processed/failed) | `CacheHelperServiceImpl` + `RedisConfig.java` + `Receiver Application` |

---

### 🔄 Processing Flow

1. **API Request**
   - The client calls `POST /add-employee` (protected by Azure AD).
   - Request payload is mapped to `CreateEmployeeRequest`.
   - API publishes the message to `employeeprocessqueue`.
   - Status is immediately cached in Redis as `"processing"`.

2. **Queue Consumption**
   - `Service Bus Receiver` listens for new messages.
   - The payload is processed and persisted in PostgreSQL via `BasicDetailRepository`.
   - If successful, Redis is updated as `"processed"`; otherwise `"failed"`.

3. **Event Propagation**
   - A success event is published to `processedqueue` for downstream consumers.
   - This enables other systems (reporting, audit, HR sync) to react asynchronously.

4. **Status Tracking**
   - Clients can query the employee status via `/get-status?key=`.
   - This key is provided by /add-employee API in response.
   - Redis acts as a lightweight, fast-access layer for intermediate status updates.

---

## ⚙️ Codebase Overview

| Package | Purpose |
|----------|----------|
| `controller` | Contains `EmployeeController` for REST APIs |
| `service` | contains interfaces. Declares core business contracts (`EmployeeService`, `CacheHelperService`, etc.) |
| `service.serviceimpl` | contains implementations (business logic + infra integration) |
| `config` | Azure and database configurations (`ServiceBusConfig`, `DbConfig`, `RedisConfig`, `SwaggerConfig`) |
| `dto` | Data Transfer Objects for requests/responses[ API Contracts between client and controller] |
| `entity` | JPA entities mapped to PostgreSQL tables |
| `exception` | Centralized exception handling using `GlobalExceptionHandler` and some of the custom exceptions |
| `Repository` | Spring Data repositories for DB persistence |

---

## ☁️ Deployment & Technology Stack

| Layer | Technology | Description |
|--------|-------------|-------------|
| **API Layer** | Spring Boot (Java 21) | Exposes REST endpoints (`/add-employee`, `/get-status?key=?`) |
| **Messaging** | Azure Service Bus Queues | Handles asynchronous message flow |
| **Persistence** | Azure PostgreSQL | Secure data storage with token-based authentication |
| **Cache** | Azure Redis Cache | Temporary cache for intermediate status tracking |
| **Security** | Azure Managed Identity + Azure AD | Passwordless authentication and API protection |
| **Docs** | Swagger UI | Auto-generated interactive documentation |

---

## 🔐 Managed Identity Integration

The project leverages **Azure Managed Identity** across all components (except Redis) to eliminate secrets from the codebase.

| Component | Authentication Method | Notes |
|------------|-----------------------|--------|
| **Azure Web App** | System-assigned Managed Identity + Azure AD Auth | Protects API using Azure’s built-in authentication |
| **Azure Service Bus** | Managed Identity via `DefaultAzureCredential` | Used for send/receive operations |
| **Azure PostgreSQL** | Managed Identity via token-based JDBC | Secure database access without credentials |
| **Azure Redis Cache** | Key-based authentication | Managed Identity available only in Premium tier |

**Configuration Example:**
```properties
# PostgreSQL (Managed Identity)
spring.datasource.url=jdbc:postgresql://<server>.postgres.database.azure.com:5432/<db>
spring.datasource.username=<managed_identity_user>
spring.datasource.authentication=ActiveDirectoryManagedIdentity

# Azure Service Bus
spring.cloud.azure.servicebus.namespace=<namespace>
spring.cloud.azure.servicebus.credential.managed-identity-enabled=true

# Redis (key-based)
spring.data.redis.host=<redis-name>.redis.cache.windows.net
spring.data.redis.port=6380
spring.data.redis.password=${REDIS_ACCESS_KEY}
spring.data.redis.ssl=true
