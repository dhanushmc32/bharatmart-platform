# India-Wide Multi-Store E-Commerce Platform — Master Project Documentation

**Document ID:** `00-master-project-documentation.md`

**Project Name:** BharatMart (India-Wide Multi-Store E-Commerce & Enterprise XML Integration Platform)

**Phase:** Phase 1 — Requirements & Master Documentation

**Budget Constraint:** ₹0 (100% Free & Open-Source Stack, No Credit Card Required)

**Payment Policy:** Cash on Delivery (COD) Only

## 1. Project Introduction

**BharatMart** is an enterprise-grade, India-wide multi-store e-commerce platform designed to serve customers across multiple Indian states and cities through a network of physical/regional stores. Unlike a simple single-vendor shopping website, this platform models real-world retail operations:

* Every physical or fulfillment store has a unique hierarchical store code (for example, `IN-KA-BLR-STORE001` for Bengaluru, Karnataka; `IN-KA-MYS-STORE002` for Mysuru, Karnataka; `IN-TN-CHE-STORE003` for Chennai, Tamil Nadu; `IN-MH-MUM-STORE004` for Mumbai, Maharashtra).

* Product availability, pricing/stock, and order fulfillment are tracked **per store and per SKU**, preventing overselling through database transactions and pessimistic/optimistic locking.

* Internal subsystems communicate using **industry-standard XML messages** orchestrated by **Apache Camel**, complete with XSD schema validation, XXE (XML External Entity) attack protection, correlation IDs, retry policies, dead-letter handling, and a dedicated **Middleware Monitoring Dashboard**.

* All payments are strictly **Cash on Delivery (COD)**, tracked from order placement through cash collection and final settlement, while the payment module is cleanly abstracted behind interfaces for future extensibility.

* The entire architecture is built on a **₹0 budget** using free, open-source technologies and card-free cloud tiers, while remaining cleanly structured as a **modular monolith** ready for future migration to **AWS**, **Kubernetes**, and **open-source AI agents (e.g., Ollama)**.

## 2. Project Objectives

1. **Build a Production-Structured Multi-Store E-Commerce Platform:** Support multi-state, multi-city, and multi-store shopping across India with store-specific inventory and order routing.

2. **Master Enterprise Integration:** Implement asynchronous-style and synchronous XML-based system-to-system communication using Apache Camel, XSD validation, acknowledgements, retries, and correlation tracking.

3. **Enforce Strict Security & Authentication:** Implement Spring Security with BCrypt password hashing, JWT access/refresh token lifecycle, token revocation, role-based access control (`CUSTOMER`, `STORE_MANAGER`, `ADMIN`), login/logout audit trails, and XXE-hardened XML parsing.

4. **Implement Strict COD-Only Payments:** Build a complete Cash on Delivery lifecycle (`PENDING` → `CASH_COLLECTED` → `PAID`, plus `FAILED` / `CANCELLED`) with zero online payment gateway dependencies.

5. **Maintain ₹0 Cost:** Ensure every tool, database, IDE, library, and cloud hosting option works without paid licenses or mandatory credit card registration.

6. **Future-Proof Architecture:** Design clean module boundaries for microservice extraction, externalized configuration for AWS/Kubernetes deployment, and decoupled APIs for future open-source AI agents.

## 3. Business Requirements

| ID | Requirement | Description | 
| ----- | ----- | ----- | 
| **BR-01** | **Pan-India Store Network** | Support stores organized by Indian State and City, each identified by a unique code (`IN-<STATE>-<CITY>-STORE<NNN>`). | 
| **BR-02** | **Store-Wise Inventory** | A product SKU can have different stock levels across different stores. Customers select or are matched to a store to check real-time availability. | 
| **BR-03** | **Cash on Delivery Only** | Customers can only pay via Cash on Delivery. No credit/debit cards, UPI, net banking, wallets, or third-party payment gateways are permitted. | 
| **BR-04** | **Reliable Order & Stock Sync** | Placing or cancelling an order must reliably reserve or release store inventory via validated XML messages with full auditability. | 
| **BR-05** | **Role-Segregated Operations** | Customers shop and track orders; Store Managers manage their assigned store's inventory, orders, and COD collections; Admins oversee all stores, users, roles, payments, and middleware health. | 
| **BR-06** | **Zero-Budget Feasibility** | Development, testing, database hosting, and initial cloud deployment must cost ₹0 and require no credit card. | 

## 4. Functional Requirements

### 4.1 Customer Portal

* **Account Management:** Registration, login (email or username), logout with token invalidation, forgot/reset password, change password, profile management, and multi-address book (Indian states, cities, PIN codes).

* **Catalog & Store Discovery:** Browse categories, search products, view product details, and check stock availability across stores/cities.

* **Cart & Checkout:** Add/update/remove items per store, validate stock before checkout, select shipping address, select **Cash on Delivery (COD)**, and place order.

* **Order Tracking:** View order history, itemized receipts, shipment status, COD payment status, and order timeline.

### 4.2 Store Manager Dashboard

* View assigned store details (`IN-KA-BLR-STORE001`, etc.).

* Manage store-level product availability and SKU inventory quantities.

* Receive low-stock alerts when stock drops below a configured threshold.

* Process incoming store orders (confirm, pack, dispatch, mark delivered).

* Update COD cash collection status (`CASH_COLLECTED` → `PAID`) upon delivery agent settlement.

* View store-level sales and COD collection reports.

### 4.3 Admin Dashboard

* Global KPIs: total stores, customers, products, orders, and sales.

* Register and manage States, Cities, and Stores.

* Manage global Product catalog, Categories, and SKUs.

* Manage Users, Roles (`CUSTOMER`, `STORE_MANAGER`, `ADMIN`), and account statuses.

* Monitor COD payments across all stores.

* Monitor system health and access the **Middleware Monitoring Dashboard**.

### 4.4 Middleware Monitoring Dashboard

* View all XML messages with Message ID, Correlation ID, Source/Destination System, Message Type, Timestamp, Status (`CREATED`, `SENT`, `RECEIVED`, `PROCESSING`, `SUCCESS`, `FAILED`, `RETRYING`, `DEAD_LETTER`), Retry Count, and Error Details.

* Search by Message ID, Correlation ID, or Order ID; filter by status.

* Inspect raw XML request and response payloads (with sensitive fields masked).

* View step-by-step processing history and trigger safe manual retries for `FAILED` / `DEAD_LETTER` messages.

## 5. Non-Functional Requirements

1. **Security:** Passwords hashed with BCrypt (cost factor 10+); JWT signed with HMAC-SHA256/RSA; XXE disabled on all XML parsers; SQL injection prevented via JPA parameterized queries; sensitive data (passwords, tokens, full addresses/phones where restricted) masked in logs and XML monitoring views.

2. **Data Integrity & Concurrency:** Order placement and inventory reservation run inside ACID database transactions (`@Transactional`) using pessimistic write locks (`SELECT ... FOR UPDATE`) or optimistic `@Version` checks to eliminate overselling under concurrent requests.

3. **Idempotency:** XML message consumers check `message_id` / `correlation_id` before processing so duplicate deliveries or retries never double-deduct stock.

4. **Performance & Resource Efficiency:** Backend tuned to run within 512 MB RAM on free cloud containers (`-Xmx300m`) while supporting sub-500ms API responses for catalog/cart operations under normal load.

5. **Maintainability & Modularity:** Clean layered architecture (Controller → Service → Repository → Entity, with DTO mapping) and strict module boundaries for future microservice extraction.

6. **Portability:** All secrets, database URLs, and CORS origins externalized via environment variables (`application.yml` + `.env`).

## 6. Technology Stack and Selection Justification

Every technology below is **free and open-source** (or offers a permanently free, no-credit-card tier for hosting).

| Technology | What It Is | Why We Are Using It & Problem It Solves | How It Communicates | Free? | Future Upgrade Path | 
| ----- | ----- | ----- | ----- | ----- | ----- | 
| **React.js + TypeScript** | Component-based UI library with static typing | Builds a responsive SPA for Customer, Store Manager, Admin, and Middleware dashboards; TypeScript catches API contract bugs at compile time. | Sends HTTP/JSON requests via Axios to Spring Boot REST APIs. | Yes (MIT License) | Upgrade to Next.js SSR for SEO or split into micro-frontends. | 
| **Tailwind CSS / Bootstrap** | Utility-first / component CSS framework | Rapidly builds clean, mobile-responsive layouts without writing thousands of lines of custom CSS. | Compiled into static CSS bundled with React. | Yes (MIT License) | Design system tokens or custom component library. | 
| **Axios** | Promise-based HTTP client | Simplifies API calls, attaches JWT `Authorization: Bearer` headers via interceptors, and handles automatic 401 token refresh. | Communicates with Spring Boot `/api/v1/*` endpoints over HTTPS. | Yes (MIT License) | Replace or wrap with TanStack Query (React Query) for caching. | 
| **Java 21 (LTS)** | Long-Term Support Java runtime | Provides modern language features (records, pattern matching, virtual threads) and rock-solid enterprise ecosystem support. | Executes Spring Boot and Apache Camel inside the JVM. | Yes (GPLv2+CE / Eclipse Temurin) | Enable Java 21 Virtual Threads (`spring.threads.virtual.enabled=true`) for high concurrency. | 
| **Spring Boot 3.x (MVC, Security, Data JPA)** | Opinionated Java enterprise framework | Accelerates REST API creation, handles JWT/RBAC security, and maps Java objects to MySQL tables via Hibernate ORM. | Exposes REST/JSON to React; uses JDBC to talk to MySQL; invokes Camel `ProducerTemplate`. | Yes (Apache 2.0) | Split modular monolith packages into independent Spring Boot microservices. | 
| **MySQL Community Edition / TiDB Cloud Starter** | Relational SQL database / MySQL-compatible serverless DB | Stores normalized relational data with ACID transactions and row-level locking to prevent inventory overselling. | Communicates with Spring Boot via MySQL Connector/J over TCP/TLS (port 3306/4000). | Yes (GPLv2; [TiDB Cloud Starter](https://docs.pingcap.com/tidbcloud/serverless-limitations/) is \$0/mo, no card) | Migrate to Amazon RDS for MySQL (Multi-AZ) with read replicas. | 
| **Flyway** | Versioned database migration tool | Tracks schema changes (`V1__init.sql`, `V2__seed.sql`) automatically on startup so every environment stays in sync. | Runs SQL scripts directly against MySQL via JDBC at Spring Boot startup. | Yes (Apache 2.0) | Integrate into CI/CD pipelines (GitHub Actions) before app rollout. | 
| **Apache Camel** | Enterprise Integration Patterns (EIP) framework | Orchestrates XML message routing, XSD validation, retries, acknowledgements, and Dead Letter Channels between modules. | Embedded in Spring Boot; routes `direct:`/`seda:` endpoints and updates `xml_messages` in MySQL. | Yes (Apache 2.0) | Swap `direct:`/`seda:` endpoints to `rabbitmq:` or `aws-sqs:` without changing business logic. | 
| **RabbitMQ (Optional Evaluation)** | Open-source AMQP message broker | Decouples message producers and consumers with durable disk-backed queues if traffic scales beyond in-process Camel routes. | Connects to Apache Camel via `camel-rabbitmq` / Spring AMQP. | Yes (MPL 2.0) | Replace with Amazon MQ (managed RabbitMQ) or AWS SQS in cloud migration. | 
| **Swagger / OpenAPI (Springdoc)** | Interactive API documentation | Auto-generates live API docs and a browser test UI from Spring controllers. | Served at `/swagger-ui.html` by Spring Boot. | Yes (Apache 2.0) | Export OpenAPI spec to API Gateway (AWS API Gateway / Kong). | 
| **JUnit 5 & Mockito** | Java unit & integration testing tools | Verifies business logic, security rules, and Camel XML routes automatically before deployment. | Runs via Maven (`mvn test`) in IDE or GitHub Actions. | Yes (EPL 2.0 / MIT) | Add Testcontainers (MySQL + RabbitMQ) in CI/CD. | 
| **VS Code / IntelliJ IDEA CE / GitHub Codespaces** | Code editors & cloud IDE | Provides zero-cost coding environments; [GitHub Codespaces](https://github.com/features/codespaces) offers 120 free core hours/month in browser without local admin rights. | Pushes code to GitHub; runs Maven/Node/Docker in cloud container. | Yes (Free tiers / OSS) | Self-hosted runners or GitHub Team/Enterprise if scaling a team. | 

## 7. Complete System Architecture

```
flowchart TB
    subgraph Client["Frontend Layer (React + TypeScript + Tailwind + Axios)"]
        CP["Customer Portal"]
        SM["Store Manager Dashboard"]
        AD["Admin Dashboard"]
        MM["Middleware Monitor UI"]
    end

    subgraph Backend["Spring Boot 3 Modular Monolith (Java 21)"]
        SEC["Spring Security + JWT Filter + RBAC"]
        API["REST Controllers (/api/v1/*)"]

        subgraph Modules["Business Modules"]
            M_CUST["Customer Module"]
            M_PROD["Product & Catalog Module"]
            M_STORE["Store Module"]
            M_INV["Inventory Module"]
            M_ORD["Order Module"]
            M_PAY["Payment Module (COD Only)"]
            M_NOTIF["Notification Module"]
        end

        subgraph Integration["Apache Camel XML Integration Engine"]
            CAMEL_IN["Order / Stock XML Producer"]
            XSD["XSD Validator (XXE-Safe)"]
            ROUTER["Content-Based Router & Retry/DLQ"]
            ACK["Acknowledgement & Audit Handler"]
        end
    end

    subgraph Data["Data & Messaging Layer"]
        DB[("MySQL 8 / TiDB Cloud\n(Flyway Managed)")]
        MQ[("Optional: RabbitMQ\n(Future Async Broker)")]
        AI["Future AI Service\n(Ollama / Python FastAPI)"]
    end

    Client -->|HTTPS + JSON + JWT| SEC
    SEC --> API
    API --> Modules
    M_ORD -->|Produce Order XML| CAMEL_IN
    CAMEL_IN --> XSD --> ROUTER
    ROUTER -->|Reserve Stock XML| M_INV
    M_INV -->|Ack / Error XML| ACK
    ACK -->|Update Order & Log| M_ORD
    Modules --> DB
    Integration -->|Persist Message & History| DB
    ROUTER -.->|Future Broker Swap| MQ
    AI -.->|Controlled REST API + JWT| API

```

## 8. High-Level Design (HLD)

1. **Presentation Tier:** A single-page React + TypeScript application served statically from **Cloudflare Pages** (or Netlify/Vercel). It enforces client-side route guards (`CUSTOMER`, `STORE_MANAGER`, `ADMIN`) and communicates exclusively via HTTPS REST/JSON with JWT Bearer tokens.

2. **Application & Security Tier:** A Spring Boot 3 application running on Java 21. Every request passes through CORS validation, the `JwtAuthenticationFilter` (which also checks the token revocation table), and `@PreAuthorize` role checks before reaching modular services.

3. **Integration Tier (Apache Camel):** Internal state transitions that cross bounded contexts—such as Order Creation → Inventory Reservation, Order Cancellation → Stock Release, and COD Collection → Payment Confirmation—are serialized into **XML**, validated against **XSD schemas**, routed through Apache Camel with a `CorrelationId`, and logged in `xml_messages` and `message_processing_history`.

4. **Persistence Tier:** MySQL Community Edition (locally/Codespaces) or TiDB Cloud Starter (cloud) managed by Flyway migrations. All stock mutations use pessimistic row locking (`FOR UPDATE`) inside database transactions.

## 9. Low-Level Design (LLD)

### 9.1 Package & Layering Pattern (Per Module)

Each business module (`customer`, `product`, `store`, `inventory`, `order`, `payment`, `notification`) follows strict layering:

* `controller/` — REST endpoints, `@Valid` request validation, OpenAPI annotations, returns `ResponseEntity<ApiResponse<T>>`.

* `dto/` — Immutable Java `record` classes for Request and Response payloads (never exposing JPA `@Entity` classes directly).

* `service/` — Interface + `@Service` implementation containing business rules and `@Transactional` boundaries.

* `repository/` — Spring Data JPA interfaces (`JpaRepository<Entity, Long>`) with custom `@Lock(LockModeType.PESSIMISTIC_WRITE)` queries where concurrency matters.

* `entity/` — JPA entities mapped to Flyway-managed tables with audit timestamps (`createdAt`, `updatedAt`).

* `mapper/` — Clean DTO ↔ Entity conversion methods.

### 9.2 Key Concurrency & Idempotency Design

* **Preventing Overselling:** When reserving stock for `(store_id, sku_id)`, `InventoryRepository.findByStoreIdAndSkuIdForUpdate(...)` acquires a database row lock until the transaction commits or rolls back.

* **Idempotent XML Processing:** Before Camel processes an inbound XML message, it checks `xml_messages.message_id`. If status is already `SUCCESS`, it immediately returns the cached acknowledgement without re-executing inventory deduction.

## 10. Complete Application Folder Structure

```
bharatmart-platform/
├── .devcontainer/
│   ├── devcontainer.json              # GitHub Codespaces (Java 21, Node 20, MySQL 8)
│   └── docker-compose.yml             # Codespaces / optional local MySQL + RabbitMQ
├── docs/
│   ├── 00-master-project-documentation.md
│   ├── 01-requirements.md
│   ├── 02-system-architecture.md
│   ├── 03-technology-setup.md
│   ├── 04-database-design.md
│   ├── 05-backend-development.md
│   ├── 06-frontend-development.md
│   ├── 07-xml-integration.md
│   ├── 08-middleware-monitoring.md
│   ├── 09-authentication-security.md
│   ├── 10-cod-payment-workflow.md
│   ├── 11-system-integration.md
│   ├── 12-testing.md
│   ├── 13-deployment.md
│   ├── 14-aws-migration.md
│   ├── 15-kubernetes-migration.md
│   └── 16-ai-integration.md
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/bharatmart/
│       │   │   ├── BharatMartApplication.java
│       │   │   ├── common/            # ApiResponse, GlobalExceptionHandler, MaskingUtil
│       │   │   ├── config/            # SecurityConfig, CorsConfig, OpenApiConfig, CamelConfig
│       │   │   ├── auth/              # AuthController, JwtService, TokenRevocation, LoginAudit
│       │   │   ├── customer/          # Profile, Address, State/City lookup
│       │   │   ├── store/             # Store CRUD, StoreCode generator (IN-KA-BLR-STORE001)
│       │   │   ├── product/           # Category, Product, SKU management
│       │   │   ├── inventory/         # StoreInventory, InventoryHistory, LowStock alerts
│       │   │   ├── cart/              # ShoppingCart, CartItem
│       │   │   ├── order/             # Order, OrderItem, Shipment, OrderStateMachine
│       │   │   ├── payment/           # PaymentStrategy interface, CodPaymentService
│       │   │   ├── notification/      # Event-driven email/in-app notification stub
│       │   │   └── integration/       # Camel routes, XXE-safe XML parser, XSD validator, Monitor API
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── db/migration/      # V1__schema.sql, V2__seed_india_stores.sql
│       │       └── xsd/               # bharatmart-envelope.xsd, order-create.xsd, etc.
│       └── test/java/com/bharatmart/  # JUnit 5, Mockito, CamelTestSupport suites
└── frontend/
    ├── package.json
    ├── tsconfig.json
    ├── tailwind.config.js
    └── src/
        ├── api/                       # Axios instance + JWT refresh interceptor
        ├── context/                   # AuthContext, CartContext, StoreContext
        ├── components/                # Reusable UI, Navbar, ProtectedRoute, StatusBadge
        ├── pages/
        │   ├── customer/              # Home, Catalog, ProductDetail, Cart, CheckoutCOD, Orders
        │   ├── store/                 # StoreDashboard, StoreInventory, StoreOrders, CodCollection
        │   ├── admin/                 # AdminDashboard, StoreReg, UserRoles, GlobalPayments
        │   └── middleware/            # XmlMessageMonitor, MessageDetailModal, RetryLogs
        └── types/                     # TypeScript interfaces matching backend DTOs

```

## 11. Database ER Diagram

```
erDiagram
    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : assigned_to
    USERS ||--o{ LOGIN_HISTORY : logs
    USERS ||--o{ TOKEN_REVOCATIONS : revokes
    USERS ||--o| CUSTOMER_PROFILES : owns
    CUSTOMER_PROFILES ||--o{ ADDRESSES : has
    STATES ||--o{ CITIES : contains
    CITIES ||--o{ ADDRESSES : locates
    STATES ||--o{ STORES : hosts
    CITIES ||--o{ STORES : hosts
    STORES ||--o{ INVENTORY : stocks
    CATEGORIES ||--o{ PRODUCTS : groups
    PRODUCTS ||--o{ SKUS : has_variants
    SKUS ||--o{ INVENTORY : tracked_in
    INVENTORY ||--o{ INVENTORY_HISTORY : audits
    USERS ||--o{ SHOPPING_CARTS : owns
    STORES ||--o{ SHOPPING_CARTS : scoped_to
    SHOPPING_CARTS ||--o{ CART_ITEMS : contains
    SKUS ||--o{ CART_ITEMS : references
    USERS ||--o{ ORDERS : places
    STORES ||--o{ ORDERS : fulfills
    ORDERS ||--o{ ORDER_ITEMS : contains
    ORDERS ||--o| PAYMENTS : paid_via
    ORDERS ||--o| SHIPMENTS : shipped_via
    XML_MESSAGES ||--o{ MESSAGE_PROCESSING_HISTORY : tracks

```

## 12. Database Schema

All 23 core tables are managed by Flyway (`V1__schema.sql`) using `BIGINT AUTO_INCREMENT` primary keys, explicit `FOREIGN KEY` constraints, composite `UNIQUE` constraints, and targeted indexes.

| Domain | Table Name | Key Columns & Purpose | Key Constraints / Indexes | 
| ----- | ----- | ----- | ----- | 
| **Auth** | `users` | `id`, `username`, `email`, `password_hash`, `account_status` (`ACTIVE`, `LOCKED`, `DISABLED`), `failed_attempts` | `UNIQUE(username)`, `UNIQUE(email)` | 
| **Auth** | `roles` | `id`, `name` (`CUSTOMER`, `STORE_MANAGER`, `ADMIN`) | `UNIQUE(name)` | 
| **Auth** | `user_roles` | `user_id`, `role_id` | Composite PK `(user_id, role_id)` | 
| **Auth** | `login_history` | `id`, `user_id`, `login_at`, `logout_at`, `token_jti`, `status` (`SUCCESS`, `FAILED`), `ip_address` | `INDEX(user_id, login_at)` | 
| **Auth** | `token_revocations` | `id`, `token_jti`, `user_id`, `revoked_at`, `expires_at`, `reason` | `UNIQUE(token_jti)`, `INDEX(expires_at)` | 
| **Geo** | `states` | `id`, `code` (`KA`, `TN`, `MH`), `name` | `UNIQUE(code)` | 
| **Geo** | `cities` | `id`, `state_id`, `code` (`BLR`, `MYS`, `CHE`, `MUM`), `name` | `UNIQUE(state_id, code)` | 
| **Customer** | `customer_profiles` | `id`, `user_id`, `full_name`, `phone`, `preferred_store_id` | `UNIQUE(user_id)` | 
| **Customer** | `addresses` | `id`, `customer_id`, `line1`, `line2`, `city_id`, `state_id`, `pincode`, `is_default` | `INDEX(customer_id)` | 
| **Store** | `stores` | `id`, `store_code` (`IN-KA-BLR-STORE001`), `name`, `state_id`, `city_id`, `manager_user_id`, `is_active` | `UNIQUE(store_code)` | 
| **Catalog** | `categories` | `id`, `name`, `slug`, `parent_id` | `UNIQUE(slug)` | 
| **Catalog** | `products` | `id`, `category_id`, `name`, `description`, `brand`, `is_active` | `INDEX(category_id, name)` | 
| **Catalog** | `skus` | `id`, `product_id`, `sku_code`, `variant_name`, `mrp`, `selling_price` | `UNIQUE(sku_code)` | 
| **Inventory** | `inventory` | `id`, `store_id`, `sku_id`, `available_qty`, `reserved_qty`, `reorder_level`, `version` | `UNIQUE(store_id, sku_id)` | 
| **Inventory** | `inventory_history` | `id`, `inventory_id`, `change_qty`, `reason` (`ORDER_RESERVE`, `ORDER_CANCEL`, `RESTOCK`), `reference_id`, `created_at` | `INDEX(inventory_id, created_at)` | 
| **Cart** | `shopping_carts` | `id`, `user_id`, `store_id`, `updated_at` | `UNIQUE(user_id, store_id)` | 
| **Cart** | `cart_items` | `id`, `cart_id`, `sku_id`, `quantity` | `UNIQUE(cart_id, sku_id)` | 
| **Order** | `orders` | `id`, `order_number`, `user_id`, `store_id`, `shipping_address_snapshot`, `total_amount`, `status`, `correlation_id` | `UNIQUE(order_number)`, `INDEX(user_id)`, `INDEX(store_id)` | 
| **Order** | `order_items` | `id`, `order_id`, `sku_id`, `sku_code_snapshot`, `quantity`, `unit_price`, `line_total` | `INDEX(order_id)` | 
| **Payment** | `payments` | `id`, `order_id`, `payment_method` (`COD`), `amount`, `status` (`PENDING`, `CASH_COLLECTED`, `PAID`, `FAILED`, `CANCELLED`), `collected_by`, `collected_at` | `UNIQUE(order_id)` | 
| **Shipping** | `shipments` | `id`, `order_id`, `tracking_number`, `carrier_name`, `status`, `dispatched_at`, `delivered_at` | `UNIQUE(order_id)` | 
| **Middleware** | `xml_messages` | `id`, `message_id`, `correlation_id`, `order_id`, `source_system`, `destination_system`, `message_type`, `status`, `retry_count`, `request_xml`, `response_xml`, `error_details`, `created_at`, `updated_at` | `UNIQUE(message_id)`, `INDEX(correlation_id)`, `INDEX(status)`, `INDEX(order_id)` | 
| **Middleware** | `message_processing_history` | `id`, `xml_message_id`, `status`, `attempt_number`, `notes`, `logged_at` | `INDEX(xml_message_id, logged_at)` | 

## 13. API Specifications (Versioned `/api/v1`)

| Module | Method & Endpoint | Role | Purpose | 
| ----- | ----- | ----- | ----- | 
| **Auth** | `POST /api/v1/auth/register` | Public | Register new `CUSTOMER` account | 
| **Auth** | `POST /api/v1/auth/login` | Public | Authenticate via email/username; issue JWT + Refresh token | 
| **Auth** | `POST /api/v1/auth/refresh` | Public | Rotate refresh token and issue new access JWT | 
| **Auth** | `POST /api/v1/auth/logout` | Authenticated | Revoke token JTI and record `logout_at` | 
| **Auth** | `POST /api/v1/auth/forgot-password` / `reset-password` | Public | Generate reset token and update password | 
| **Customer** | `GET/PUT /api/v1/customers/me` | `CUSTOMER` | View/update profile and manage addresses | 
| **Geo/Store** | `GET /api/v1/stores` | Public | List active stores filtered by state/city | 
| **Geo/Store** | `POST /api/v1/admin/stores` | `ADMIN` | Register new store (`IN-XX-XXX-STORE###`) | 
| **Catalog** | `GET /api/v1/products` / `GET /api/v1/products/{id}` | Public | Search/browse products and store-wise availability | 
| **Inventory** | `PUT /api/v1/store/inventory/{skuId}` | `STORE_MANAGER` | Update store stock and trigger stock-update XML | 
| **Cart** | `GET/POST/DELETE /api/v1/cart/items` | `CUSTOMER` | Manage store-scoped shopping cart | 
| **Order** | `POST /api/v1/orders` | `CUSTOMER` | Place COD order, trigger Camel XML order-creation flow | 
| **Order** | `GET /api/v1/orders/{orderNumber}` | `CUSTOMER` / `STORE_MANAGER` | Track order status, shipment, and COD status | 
| **Order** | `POST /api/v1/orders/{orderNumber}/cancel` | `CUSTOMER` / `STORE_MANAGER` | Cancel order and release inventory via XML | 
| **Payment** | `PATCH /api/v1/store/payments/{orderId}/cod` | `STORE_MANAGER` / `ADMIN` | Update COD status (`CASH_COLLECTED` / `PAID` / `FAILED`) | 
| **Monitor** | `GET /api/v1/admin/middleware/messages` | `ADMIN` | Search/filter XML messages by status, Order ID, Correlation ID | 
| **Monitor** | `POST /api/v1/admin/middleware/messages/{id}/retry` | `ADMIN` | Safely re-dispatch a `FAILED` or `DEAD_LETTER` XML message | 

## 14. XML Message Architecture

Every inter-module XML message uses a unified **BharatMart Envelope** containing:

* `<MessageId>`: UUIDv4 unique per message (used for idempotency).

* `<CorrelationId>`: UUIDv4 shared across an entire business transaction (e.g., Order Creation → Inventory Reservation → Acknowledgement).

* `<SourceSystem>` & `<DestinationSystem>`: e.g., `ORDER_SERVICE`, `INVENTORY_SERVICE`, `PAYMENT_SERVICE`.

* `<MessageType>`: `ORDER_CREATE`, `INVENTORY_RESERVE`, `INVENTORY_CONFIRM`, `ORDER_CANCEL`, `STOCK_UPDATE`, `COD_PAYMENT_CONFIRM`, `ERROR_RESPONSE`.

* `<Timestamp>`: ISO-8601 UTC/IST timestamp (`2026-11-18T14:00:00+05:30`).

* `<Payload>`: Strongly typed XSD-validated business payload.

**XXE Protection:** All `DocumentBuilderFactory`, `SAXParserFactory`, and `SchemaFactory` instances disable `disallow-doctype-decl`, `external-general-entities`, and `external-parameter-entities` before parsing any XML.

## 15. XML Sample Messages

### 15.1 Order Creation (`ORDER_CREATE`)

```
<?xml version="1.0" encoding="UTF-8"?>
<BharatMartMessage xmlns="http://bharatmart.in/schema/integration/v1">
    <Header>
        <MessageId>msg-8f14e45f-ea3c-4a12-9b11-001122334455</MessageId>
        <CorrelationId>corr-99a8b7c6-1234-4567-890a-bcdef1234567</CorrelationId>
        <SourceSystem>ORDER_SERVICE</SourceSystem>
        <DestinationSystem>INVENTORY_SERVICE</DestinationSystem>
        <MessageType>ORDER_CREATE</MessageType>
        <Timestamp>2026-11-18T14:05:00+05:30</Timestamp>
    </Header>
    <Payload>
        <OrderCreate>
            <OrderNumber>ORD-2026-000101</OrderNumber>
            <StoreCode>IN-KA-BLR-STORE001</StoreCode>
            <PaymentMethod>COD</PaymentMethod>
            <Items>
                <Item>
                    <SkuCode>SKU-ELEC-1001</SkuCode>
                    <Quantity>2</Quantity>
                </Item>
            </Items>
        </OrderCreate>
    </Payload>
</BharatMartMessage>

```

### 15.2 Inventory Reservation (`INVENTORY_RESERVE`) & Confirmation (`INVENTORY_CONFIRM`)

```
<!-- INVENTORY_RESERVE (Camel -> Inventory Module) -->
<BharatMartMessage xmlns="http://bharatmart.in/schema/integration/v1">
    <Header>
        <MessageId>msg-11223344-5566-7788-99aa-bbccddeeff00</MessageId>
        <CorrelationId>corr-99a8b7c6-1234-4567-890a-bcdef1234567</CorrelationId>
        <SourceSystem>CAMEL_MIDDLEWARE</SourceSystem>
        <DestinationSystem>INVENTORY_SERVICE</DestinationSystem>
        <MessageType>INVENTORY_RESERVE</MessageType>
        <Timestamp>2026-11-18T14:05:01+05:30</Timestamp>
    </Header>
    <Payload>
        <InventoryReserve>
            <OrderNumber>ORD-2026-000101</OrderNumber>
            <StoreCode>IN-KA-BLR-STORE001</StoreCode>
            <Items><Item><SkuCode>SKU-ELEC-1001</SkuCode><Quantity>2</Quantity></Item></Items>
        </InventoryReserve>
    </Payload>
</BharatMartMessage>

<!-- INVENTORY_CONFIRM (Inventory Module -> Camel -> Order Module) -->
<BharatMartMessage xmlns="http://bharatmart.in/schema/integration/v1">
    <Header>
        <MessageId>msg-22334455-6677-8899-aabb-ccddeeff0011</MessageId>
        <CorrelationId>corr-99a8b7c6-1234-4567-890a-bcdef1234567</CorrelationId>
        <SourceSystem>INVENTORY_SERVICE</SourceSystem>
        <DestinationSystem>ORDER_SERVICE</DestinationSystem>
        <MessageType>INVENTORY_CONFIRM</MessageType>
        <Timestamp>2026-11-18T14:05:02+05:30</Timestamp>
    </Header>
    <Payload>
        <InventoryConfirm>
            <OrderNumber>ORD-2026-000101</OrderNumber>
            <StoreCode>IN-KA-BLR-STORE001</StoreCode>
            <ReservationStatus>RESERVED</ReservationStatus>
        </InventoryConfirm>
    </Payload>
</BharatMartMessage>

```

### 15.3 Order Cancellation, Stock Update, COD Confirmation & Error Response

```
<!-- ORDER_CANCEL -->
<Payload><OrderCancel><OrderNumber>ORD-2026-000101</OrderNumber><StoreCode>IN-KA-BLR-STORE001</StoreCode><Reason>CUSTOMER_CANCELLED</Reason></OrderCancel></Payload>

<!-- STOCK_UPDATE -->
<Payload><StockUpdate><StoreCode>IN-KA-BLR-STORE001</StoreCode><SkuCode>SKU-ELEC-1001</SkuCode><NewQuantity>48</NewQuantity></StockUpdate></Payload>

<!-- COD_PAYMENT_CONFIRM -->
<Payload><CodPaymentConfirm><OrderNumber>ORD-2026-000101</OrderNumber><Amount>1499.00</Amount><Currency>INR</Currency><PaymentStatus>PAID</PaymentStatus><CollectedAt>2026-11-20T16:30:00+05:30</CollectedAt></CodPaymentConfirm></Payload>

<!-- ERROR_RESPONSE -->
<Payload><ErrorResponse><OriginalMessageId>msg-8f14e45f-...</OriginalMessageId><ErrorCode>INSUFFICIENT_STOCK</ErrorCode><ErrorMessage>Requested 2 units of SKU-ELEC-1001, available 0</ErrorMessage></ErrorResponse></Payload>

```

*(Each payload above is wrapped inside the standard `<BharatMartMessage><Header>...</Header><Payload>...</Payload></BharatMartMessage>` envelope.)*

## 16. XSD Specifications

All XML messages are validated against `src/main/resources/xsd/bharatmart-envelope.xsd`:

* Enforces `targetNamespace="http://bharatmart.in/schema/integration/v1"` and `elementFormDefault="qualified"`.

* Validates `MessageType` as an `xs:enumeration` of the 7 allowed types.

* Validates `StoreCode` with regex pattern `IN-[A-Z]{2}-[A-Z]{3}-STORE[0-9]{3,5}`.

* Validates `Quantity` as `xs:positiveInteger` and `Amount` as `xs:decimal` (`fractionDigits=2`).

## 17. Apache Camel Integration Design

* **Endpoints:** Uses `direct:order-outbound` and `seda:integration-bus` (in-memory async queue) in the modular monolith.

* **XSD Validation:** Camel's `validator:xsd/bharatmart-envelope.xsd` validates every message before routing.

* **Error Handling & Retries:** Configured with `onException(Exception.class)` using exponential backoff (up to 3 retries: 1s, 2s, 4s), updating `xml_messages.status = 'RETRYING'` and `retry_count` on each attempt. Exhausted messages move to `direct:dead-letter-channel`, setting `status = 'DEAD_LETTER'`.

* **Optional RabbitMQ Evaluation:** RabbitMQ (MPL 2.0) is **optional** in Phase 1–11 because Apache Camel `seda:` handles asynchronous routing in-process at zero memory overhead (critical for Render's 512 MB RAM limit). In Phase 13+ or local Docker Compose, swapping `seda:integration-bus` to `rabbitmq:bharatmart.exchange` requires only changing the Camel endpoint URI.

## 18. Message-Flow Diagrams

```
sequenceDiagram
    autonumber
    actor C as Customer
    participant API as Order Service
    participant DB as MySQL / TiDB
    participant Camel as Apache Camel + XSD
    participant Inv as Inventory Service

    C->>API: POST /api/v1/orders (COD)
    API->>DB: Save Order (PENDING_CONFIRMATION)
    API->>Camel: Send ORDER_CREATE XML (CorrelationId)
    Camel->>DB: Log xml_messages (CREATED -> PROCESSING)
    Camel->>Camel: Validate against XSD (XXE-Safe)
    Camel->>Inv: Route INVENTORY_RESERVE XML
    Inv->>DB: SELECT ... FOR UPDATE & Reserve Stock
    alt Stock Available
        Inv-->>Camel: Return INVENTORY_CONFIRM XML
        Camel->>DB: Log SUCCESS & Update Order (CONFIRMED)
        API-->>C: 201 Created (Order Confirmed - COD)
    else Out of Stock / Error
        Inv-->>Camel: Return ERROR_RESPONSE XML
        Camel->>DB: Retry (up to 3x) or mark FAILED/DEAD_LETTER
        Camel->>DB: Update Order (CANCELLED_OUT_OF_STOCK)
        API-->>C: 409 Conflict (Insufficient Stock)
    end

```

## 19. Authentication and Authorization Architecture

* **Spring Security 6 Filter Chain:** Stateless session policy (`SessionCreationPolicy.STATELESS`).

* **Password Security:** `BCryptPasswordEncoder` (strength 10). Never logged or returned in DTOs.

* **JWT Strategy:**

  * Short-lived **Access Token** (15 minutes) containing `sub` (username), `userId`, `roles`, and `jti` (unique token ID).

  * Longer-lived **Refresh Token** (7 days) stored securely and rotated on `/api/v1/auth/refresh`.

* **Logout & Revocation:** Calling `/api/v1/auth/logout` inserts the token's `jti` into `token_revocations` and updates `login_history.logout_at`. `JwtAuthenticationFilter` rejects any revoked `jti`.

* **Account Protection:** Tracks `failed_attempts` and locks account after 5 consecutive failures.

* **Role Matrix:**

  * `CUSTOMER`: Browse, manage own cart/profile/addresses, place/cancel/track own orders.

  * `STORE_MANAGER`: Manage inventory, orders, and COD collections for their assigned `store_id` only.

  * `ADMIN`: Full access across all stores, users, roles, global payments, and Middleware Monitoring.

## 20. COD Payment Workflow

1. Customer adds products from a selected store to cart and proceeds to checkout.

2. Payment method is fixed to **Cash on Delivery (`COD`)** — no card, UPI, or gateway UI exists.

3. Backend validates stock and creates `orders` + `payments` (`payment_method = 'COD'`, `status = 'PENDING'`).

4. Inventory is reserved via Camel XML workflow; order moves to `CONFIRMED`.

5. Store Manager packs and dispatches order (`SHIPPED` / `OUT_FOR_DELIVERY`).

6. Delivery agent collects cash upon delivery; Store Manager marks `CASH_COLLECTED`, then `PAID` once cash is reconciled, triggering `COD_PAYMENT_CONFIRM` XML.

7. **Future Extensibility:** `PaymentService` delegates to `PaymentProcessor` interface (`supports(PaymentMethod)`). Currently only `CodPaymentProcessor` is registered; future gateways can be added as new beans without modifying `OrderService`.

## 21. Frontend Page Architecture

* **Public & Customer Pages:** Home (State/City/Store selector), Product Listing & Search, Product Detail (store stock badge), Cart, COD Checkout, Order History & Live Stepper Tracking, Profile & Address Book, Login/Register/Forgot Password.

* **Store Manager Pages:** Store Overview KPIs, Store SKU Inventory & Low-Stock Alerts, Store Order Fulfillment Queue, COD Cash Collection Ledger.

* **Admin Pages:** Platform KPIs, Store Registration, Global Catalog/SKU Manager, User & Role Management, Global COD Payment Monitor, **Middleware XML Monitoring Console** (search, status badges, XML request/response modal, retry button).

## 22. Backend Module Architecture

Implemented as a **Modular Monolith** inside `com.bharatmart.*`:

1. **Customer Management** (`customer`, `auth`)

2. **Product Management** (`product`)

3. **Store Management** (`store`)

4. **Inventory Management** (`inventory`)

5. **Order Management** (`order`, `cart`)

6. **Payment Management** (`payment` — COD-only)

7. **Notification System** (`notification` — Spring ApplicationEvents)

8. **Integration & Monitoring** (`integration` — Apache Camel)

Modules communicate via **Java service interfaces** or **Camel XML messages**—never by directly injecting another module's `Repository`—making future extraction into microservices straightforward.

## 23. Security Architecture

* **Input Validation:** Jakarta Bean Validation (`@NotBlank`, `@Positive`, `@Pattern`) on all DTOs.

* **SQL Injection Prevention:** 100% Spring Data JPA / parameterized JPQL & native queries.

* **CORS:** Restricted to configured frontend origin(s) via `ALLOWED_ORIGINS` environment variable.

* **XXE Prevention:** Explicit `XMLConstants.FEATURE_SECURE_PROCESSING` and `disallow-doctype-decl = true`.

* **Data Masking:** `MaskingUtil` strips or masks passwords, JWTs, and sensitive PII before writing application logs or storing `xml_messages` audit views.

## 24. Free Development Environment Setup

To satisfy the **no local admin rights** and **no mandatory localhost** constraints:

1. **Primary Option — GitHub Codespaces (Free Tier, Browser-Based):**

   * [GitHub Codespaces](https://github.com/features/codespaces) provides **120 core hours/month** (60 hours on a 2-core container) and **15 GB storage** at **\$0/month with no credit card required**.

   * A `.devcontainer/devcontainer.json` file will provision Java 21, Maven, Node.js 20, and MySQL 8 automatically inside your browser (VS Code Web).

2. **Alternative Option — Local Portable Tools (No Admin Required):**

   * VS Code (User Installer / Portable ZIP), Eclipse Temurin Java 21 (ZIP), Node.js (ZIP), and Maven (ZIP) can run in user space, connecting to **TiDB Cloud Starter** (\$0, no credit card) so you don't even need to install MySQL locally.

## 25. Deployment Architecture (₹0 Cloud Stack)

Because **100% free, always-on, card-free Java + MySQL hosting does not exist in a single provider**, we combine three specialized free tiers that require **no credit card**:

| Layer | Provider | Free Tier Allowance (Verified 2026) | Credit Card Required? | Limitations & Workarounds | 
| ----- | ----- | ----- | ----- | ----- | 
| **Frontend (React)** | [**Cloudflare Pages**](https://developers.cloudflare.com/pages/platform/limits/) | Unlimited bandwidth, 500 builds/month, 20,000 files/site ([Pricing](https://www.cloudflare.com/en-in/plans/developer-platform/)) | **No** | 1 concurrent build at a time; static SPA only (ideal for React). | 
| **Backend (Spring Boot + Camel)** | [**Render Free Web Service**](https://render.com/docs/free) | 512 MB RAM, 0.1 CPU, 750 instance hours/month, 5 GB bandwidth ([Docker Hosting](https://render.com/hosting/docker)) | **No** | Spins down after 15 mins idle (\~30–60s cold start). Tune JVM with `-Xmx300m -XX:MaxMetaspaceSize=128m`. | 
| **Database (MySQL-Compatible)** | [**TiDB Cloud Starter**](https://docs.pingcap.com/tidbcloud/serverless-limitations/) | 5 GiB row storage + 5 GiB columnar + 50M Request Units/mo per cluster (up to 5 clusters) | **No** (when spending limit = \$0) | Serverless MySQL 8.0 compatible; stored procedures/triggers are unsupported ([MySQL Compatibility](https://docs.pingcap.com/tidbcloud/mysql-compatibility/)), which our JPA/Camel architecture avoids. | 

*Note on alternatives:* **Koyeb** introduced a credit card + \$29 pre-auth hold in Feb 2026; **Aiven** retired its free MySQL tier; **Oracle Cloud Always Free** offers generous permanent VMs *only after* credit/debit card verification. Therefore, **Cloudflare Pages + Render Free + TiDB Cloud Starter** is the cleanest genuinely ₹0, card-free cloud stack.

## 26. Testing Strategy

* **Unit Tests (JUnit 5 + Mockito):** Service-layer business rules, COD state transitions, JWT generation/validation, store code formatting.

* **Integration Tests (`@SpringBootTest` + MockMvc):** Full controller → service → DB transactions, concurrency tests for inventory locking, RBAC 401/403 checks.

* **Camel Route Tests (`CamelTestSupport`):** Verify valid XML passes XSD, invalid/XXE XML is rejected, and retries/DLQ trigger on simulated failures.

* **Frontend & API E2E:** React Testing Library for UI components + Postman collection for end-to-end checkout, COD settlement, and middleware retry flows.

## 27. Error-Handling Strategy

* **REST API:** Centralized `@RestControllerAdvice` returning a consistent `ApiErrorResponse` (`timestamp`, `status`, `errorCode`, `message`, `path`, `correlationId`). Never leaks stack traces in production.

* **Apache Camel:** `onException` handlers capture XSD/processing errors, log sanitized details to `message_processing_history`, retry up to 3 times with exponential backoff, and route exhausted messages to `DEAD_LETTER` for admin inspection and manual replay.

## 28. AWS Migration Plan

When ready to scale beyond ₹0:

* **Frontend:** S3 + Amazon CloudFront (replacing Cloudflare Pages).

* **Backend:** Docker container on **Amazon ECS (Fargate)** or **Amazon EKS** behind an Application Load Balancer.

* **Database:** **Amazon RDS for MySQL** (Multi-AZ) with Flyway migrations running in CI/CD.

* **Messaging:** Swap Camel `seda:` endpoints to **Amazon MQ (RabbitMQ)** or **Amazon SQS**.

* **Observability:** Amazon CloudWatch Logs + X-Ray distributed tracing using `CorrelationId`.

## 29. Kubernetes Migration Plan

* **Containerization:** Multi-stage `Dockerfile` (Maven build → Eclipse Temurin 21 JRE Alpine).

* **Manifests:** `Deployment` (with `livenessProbe` / `readinessProbe` on `/actuator/health`), `Service`, `ConfigMap` (non-secret configs), `Secret` (DB/JWT credentials), `HorizontalPodAutoscaler` (CPU/memory scaling), and `Ingress` (TLS termination).

* **Target Cluster:** Local `minikube`/`kind` for testing, then **Amazon EKS** for production.

## 30. Future AI Integration Architecture

* **Decoupled AI Service:** A standalone Python (FastAPI) or Java service communicating with BharatMart **strictly via authenticated REST APIs** (`/api/v1/products`, `/api/v1/orders/{id}`) using a scoped service token—never direct database writes.

* **Zero-Cost Local/Open-Source AI:** Powered by [**Ollama**](https://ollama.com/) running open-weight models (e.g., Llama 3 / Qwen / Mistral) for natural-language product search, shopping assistant, order tracking Q&A, and demand/replenishment forecasting from `inventory_history`.

## 31. Project Execution Roadmap

 1. **Phase 1:** Requirements & Master Documentation *(Current Phase)*

 2. **Phase 2:** Architecture & Free Development Environment Setup (`.devcontainer`, project skeletons, `02` & `03` docs)

 3. **Phase 3:** MySQL Database Implementation (Flyway migrations, ER verification, seed data, `04` doc)

 4. **Phase 4:** Spring Boot Backend Development (Core modules, REST APIs, `05` doc)

 5. **Phase 5:** User Authentication & Account Management (Spring Security, JWT, RBAC, audit logs, `09` doc)

 6. **Phase 6:** React Frontend Development (Customer, Store Manager, Admin UIs, `06` doc)

 7. **Phase 7:** Apache Camel & XML Integration (XSDs, XXE protection, Camel routes, `07` doc)

 8. **Phase 8:** Middleware Message Monitoring Dashboard (Backend + React UI, `08` doc)

 9. **Phase 9:** COD Payment Implementation (COD lifecycle, reporting, extensibility, `10` doc)

10. **Phase 10:** Complete End-to-End Integration (`11` doc)

11. **Phase 11:** Unit, Integration & E2E Testing (`12` doc)

12. **Phase 12:** Free Cloud Deployment (`13` doc)

13. **Phase 13:** AWS & Kubernetes Migration Planning (`14` & `15` docs)

14. **Phase 14:** Future AI Agent Integration (`16` doc)

## 32. Glossary of Technical Terms

* **SKU (Stock Keeping Unit):** A unique code identifying a specific product variant (e.g., `SKU-ELEC-1001` for a specific phone color/storage).

* **Modular Monolith:** A single deployable application internally divided into strict, independent business modules so it is simple to run now and easy to split into microservices later.

* **JWT (JSON Web Token):** A digitally signed token sent in HTTP headers so the server can verify who the user is without storing session state in server memory.

* **COD (Cash on Delivery):** Payment collected in physical cash when the order is delivered to the customer.

* **XML & XSD:** XML is a structured tag-based data format; XSD (XML Schema Definition) is the rulebook that validates whether an XML message has the exact required fields and data types.

* **XXE (XML External Entity) Attack:** A security vulnerability where malicious XML tricks a parser into reading local server files; prevented by disabling external entity resolution.

* **Apache Camel:** An integration framework that routes, validates, transforms, and retries messages between systems using Enterprise Integration Patterns.

* **Correlation ID:** A unique tracking ID attached to every message in a transaction so engineers can trace one order's entire journey across systems.

* **Dead Letter Channel (DLQ):** A holding area for messages that still fail after all automatic retries, allowing admins to inspect and replay them safely.

* **Flyway:** A tool that versions SQL scripts so database tables are created and updated reliably across every environment.