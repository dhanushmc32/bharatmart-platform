# Phase 4 — Spring Boot Backend Development (`05-backend-development.md`)

## 1. Phase Name
Phase 4: Spring Boot Backend Development.

## 2. Objective
Implement the core Spring Boot modular monolith backend: centralized exception handling, standard API response wrappers, and full Controller → Service → Repository → Entity layers for Store, Product, Inventory, Cart, Order, and COD Payment initialization.

## 3. What We Are Building
- `common/` — `ApiResponse`, `ApiErrorResponse`, `ResourceNotFoundException`, `BusinessException`, `InsufficientStockException`, `GlobalExceptionHandler`.
- `store/` — State, City, and Store CRUD with automatic `IN-<STATE>-<CITY>-STORE###` code generation.
- `product/` — Category, Product, and SKU search/browsing.
- `inventory/` — Store-wise inventory queries, low-stock alerts, stock updates, and pessimistic-locked `reserveStock` / `releaseStock` with `inventory_history` auditing.
- `cart/` — Store-scoped shopping cart add/view/remove operations.
- `order/` & `payment/` — Transactional COD order placement, order cancellation with stock release, and `PaymentProcessor` strategy (`CodPaymentProcessor`).

## 4. Why We Are Building It
Separating modules into clean layers with DTOs prevents internal database entities from leaking in APIs, while pessimistic row locking (`SELECT ... FOR UPDATE`) prevents race-condition overselling.

## 5. Technologies Being Used
Java 21, Spring Boot 3.3.5, Spring Web MVC, Spring Data JPA, Hibernate, Jakarta Bean Validation, Lombok, Springdoc OpenAPI.

## 6. What Each Technology Does
- **Spring Web MVC:** Routes `/api/v1/*` JSON HTTP requests to `@RestController` classes.
- **Spring Data JPA / Hibernate:** Maps Java `@Entity` classes to MySQL tables and manages pessimistic locks.
- **Jakarta Validation:** Validates `@Valid` DTO fields (e.g., 6-digit Indian PIN code regex) before business logic runs.
- **`@RestControllerAdvice`:** Catches exceptions globally and returns uniform `ApiErrorResponse` JSON.

## 7. Architecture and Workflow
1. Customer queries `/api/v1/stores` and `/api/v1/products`.
2. Customer adds SKUs to a store-scoped cart via `/api/v1/cart/items`.
3. Customer calls `POST /api/v1/orders` with `paymentMethod: "COD"`.
4. `OrderService.placeOrder` starts a `@Transactional` boundary, locks inventory rows, deducts `available_qty`, increments `reserved_qty`, creates `orders` + `order_items` + `payments (COD/PENDING)`, and clears the cart.

## 8. Folder Structure
`backend/src/main/java/com/bharatmart/`
- `common/` (DTOs & exceptions)
- `config/` (`SecurityConfig` with CORS)
- `store/`, `product/`, `inventory/`, `cart/`, `order/`, `payment/`

## 9. Files Created or Modified
Created 35+ Java classes across `common`, `config`, `store`, `product`, `inventory`, `cart`, `order`, and `payment`.

## 10. Explanation of Every Important File
- **`GlobalExceptionHandler.java`:** Maps `ResourceNotFoundException` → `404`, `InsufficientStockException` → `409 Conflict`, `BusinessException` → `400`, and validation failures → `400` with field-level error maps.
- **`InventoryRepository.java`:** Uses `@Lock(LockModeType.PESSIMISTIC_WRITE)` on `findByStoreIdAndSkuIdForUpdate` so concurrent checkouts serialize safely at the database row level.
- **`CodPaymentProcessor.java`:** Implements `PaymentProcessor` for `PaymentMethod.COD` only, keeping future payment methods decoupled.

## 11. Explanation of the Code
All DTOs use immutable Java 21 `record` types. `OrderService` rejects any `PaymentMethod` other than `COD`, computes totals server-side from `SkuEntity.sellingPrice` (never trusting client-sent prices), and generates a unique `correlationId` (`corr-<UUID>`) ready for Phase 7 Apache Camel XML tracing.

## 12. How the Components Communicate
`OrderService` calls `InventoryService.reserveStock(...)` and `PaymentProcessor.initializePayment(...)` through Spring-injected interfaces, never directly touching another module's repositories.

## 13. Database Changes
None (maps cleanly to `V1__schema.sql` tables created in Phase 3).

## 14. APIs Introduced
- `GET/POST /api/v1/stores`, `GET /api/v1/stores/{storeCode}`
- `GET /api/v1/categories`, `GET /api/v1/products`, `GET /api/v1/products/{id}`
- `GET/PUT /api/v1/stores/{storeCode}/inventory`, `GET /api/v1/stores/{storeCode}/inventory/low-stock`
- `GET/POST/DELETE /api/v1/cart`
- `GET/POST /api/v1/orders`, `POST /api/v1/orders/{orderNumber}/cancel`

## 15. Configuration Changes
Added `config/SecurityConfig.java` with CORS enabled for `ALLOWED_ORIGINS` (`http://localhost:5173`).

## 16. Commands to Execute
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

## 17. Expected Output
Application starts on port `8080`, Swagger UI is live at `http://localhost:8080/swagger-ui.html`, and all `/api/v1/*` endpoints respond with `ApiResponse` JSON.

## 18. Testing Instructions
1. `GET http://localhost:8080/api/v1/stores` → returns 4 seeded Indian stores.
2. `POST http://localhost:8080/api/v1/cart/items` with `{"storeCode":"IN-KA-BLR-STORE001","skuId":1,"quantity":2}`.
3. `POST http://localhost:8080/api/v1/orders` with `{"storeCode":"IN-KA-BLR-STORE001","shippingAddress":"Indiranagar, Bengaluru 560038","paymentMethod":"COD"}` → returns `201 Created` with `status: "CONFIRMED"` and `paymentStatus: "PENDING"`.

## 19. Common Errors and Solutions
- **409 Conflict (`INSUFFICIENT_STOCK`):** Requested quantity exceeds `available_qty` at that store.
- **400 Validation Error (`Invalid Indian 6-digit PIN code`):** Ensure PIN code is 6 digits and does not start with `0`.

## 20. Security Considerations
- Prices and totals are always calculated server-side from database SKUs.
- Phase 4 uses a temporary `X-User-Id` header and permit-all `SecurityConfig`; Phase 5 replaces this with full JWT authentication and RBAC.

## 21. What We Achieved
Built the complete, transactional, multi-store Spring Boot REST backend with COD-only checkout and overselling protection.

## 22. Completion Checklist
- [x] Common response wrappers & global exception handler
- [x] Store, Product, Inventory, Cart, Order, and COD Payment modules implemented
- [x] Pessimistic locking on stock reservation

## 23. What Comes Next
Phase 5: User Authentication and Account Management (Spring Security 6, BCrypt, JWT access + refresh tokens, `token_revocations` logout, `login_history` audit, forgot/reset password, and RBAC for `CUSTOMER`, `STORE_MANAGER`, `ADMIN`).
