# Phase 3 — MySQL Database Implementation (`04-database-design.md`)

## 1. Phase Name
Phase 3: MySQL Database Implementation.

## 2. Objective
Implement the complete 23-table relational database schema and initial India-wide seed data using Flyway migrations (`V1__schema.sql` and `V2__seed_data.sql`), supporting multi-store inventory concurrency and XML middleware auditing.

## 3. What We Are Building
- `V1__schema.sql` — All 23 normalized tables with primary keys, foreign keys, unique constraints, check constraints, and performance indexes.
- `V2__seed_data.sql` — Roles, demo accounts, 5 Indian states, 6 cities, 4 stores (`IN-KA-BLR-STORE001`, `IN-KA-MYS-STORE002`, `IN-TN-CHE-STORE003`, `IN-MH-MUM-STORE004`), sample catalog, and store-wise stock.

## 4. Why We Are Building It
A solid relational schema prevents data corruption, enforces referential integrity, and guarantees that two customers cannot buy the last unit of a SKU at the same store simultaneously.

## 5. Technologies Being Used
MySQL 8.0 Community Edition (or TiDB Cloud Starter), Flyway, InnoDB Storage Engine.

## 6. What Each Technology Does
- **MySQL / TiDB:** Stores relational data with ACID transactions and row-level locking.
- **Flyway:** Executes `V1__schema.sql` and `V2__seed_data.sql` in exact version order at Spring Boot startup and records checksums in `flyway_schema_history`.

## 7. Architecture and Workflow
Spring Boot boots → Flyway connects via JDBC → checks `flyway_schema_history` → applies `V1` and `V2` → Hibernate validates JPA entities against the live tables (`ddl-auto: validate`).

## 8. Folder Structure
- `backend/src/main/resources/db/migration/V1__schema.sql`
- `backend/src/main/resources/db/migration/V2__seed_data.sql`

## 9. Files Created or Modified
- `backend/src/main/resources/db/migration/V1__schema.sql`
- `backend/src/main/resources/db/migration/V2__seed_data.sql`

## 10. Explanation of Every Important File
- **`V1__schema.sql`:** Groups tables into Auth (`users`, `roles`, `user_roles`, `login_history`, `token_revocations`), Geography/Stores (`states`, `cities`, `stores`), Customer (`customer_profiles`, `addresses`), Catalog (`categories`, `products`, `skus`), Inventory (`inventory`, `inventory_history`), Commerce (`shopping_carts`, `cart_items`, `orders`, `order_items`, `payments`, `shipments`), and XML Middleware (`xml_messages`, `message_processing_history`).
- **`V2__seed_data.sql`:** Seeds realistic Indian retail data and includes one low-stock SKU (`SKU-ELEC-1002` at `IN-KA-BLR-STORE001` with qty `6 < reorder_level 10`) to demonstrate low-stock alerts later.

## 11. Explanation of the Code
- **Concurrency Control:** `inventory` has both `version` (for optimistic locking) and `CHECK (available_qty >= 0 AND reserved_qty >= 0)` so stock can never go negative even at the SQL engine level.
- **COD Enforcement:** `payments` includes `CONSTRAINT chk_payments_method CHECK (payment_method = 'COD')` to strictly enforce the COD-only business rule at the database layer.
- **TiDB Compatibility:** No stored procedures or database triggers are used, ensuring 100% compatibility with both MySQL 8.0 and TiDB Cloud Starter.

## 12. How the Components Communicate
All backend modules read/write through Spring Data JPA repositories backed by this schema.

## 13. Database Changes
Created all 23 tables and seeded initial data.

## 14. APIs Introduced
None (database layer only; REST endpoints are built in Phases 4–5).

## 15. Configuration Changes
None beyond `spring.flyway.enabled=true` already set in `application.yml`.

## 16. Commands to Execute
```bash
# In GitHub Codespaces (MySQL container running on localhost:3306)
cd backend
mvn spring-boot:run
```

## 17. Expected Output
Flyway logs:
`Successfully applied 2 migrations to schema 'bharatmart'`
followed by `Started BharatMartApplication`.

## 18. Testing Instructions
Verify tables and seed data in MySQL:
```sql
SHOW TABLES;
SELECT store_code, name FROM stores;
SELECT s.store_code, k.sku_code, i.available_qty FROM inventory i
JOIN stores s ON i.store_id = s.id
JOIN skus k ON i.sku_id = k.id;
```

## 19. Common Errors and Solutions
- **Flyway Checksum Mismatch:** Never edit an already-applied `V1` or `V2` script; always create `V3__...sql` for future changes (or drop/recreate dev DB during initial setup).
- **Access Denied for `bharatmart_user`:** Ensure `.env` matches `docker-compose.yml` credentials.

## 20. Security Considerations
- Demo users in `V2__seed_data.sql` use a BCrypt hash (`Password@123` for local dev only—must be changed before public deployment).
- Sensitive fields (`password_hash`, `reset_token`) are never exposed in API DTOs or XML logs.

## 21. What We Achieved
Implemented a complete, normalized, concurrency-safe 23-table MySQL schema with Flyway migrations and India-wide sample data.

## 22. Completion Checklist
- [x] `V1__schema.sql` created with 23 tables, FKs, indexes, and constraints
- [x] `V2__seed_data.sql` created with Indian states, cities, 4 stores, roles, and inventory
- [x] COD-only check constraint and non-negative inventory constraint added

## 23. What Comes Next
Phase 4: Spring Boot Backend Development (Common DTOs, centralized exception handling, Entities, Repositories, Services, and REST Controllers for Store, Product, Inventory, Cart, and Order modules).
