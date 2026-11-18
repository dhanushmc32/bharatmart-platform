# Phase 2A — System Architecture (`02-system-architecture.md`)

## 1. Phase Name
Phase 2: Architecture & Development Environment Setup (Part A — System Architecture).

## 2. Objective
Establish the modular monolith package architecture, component communication boundaries, externalized configuration strategy, and containerized runtime topology before writing database migrations or business logic.

## 3. What We Are Building
- Complete project workspace skeleton (`bharatmart-platform/`).
- Maven project (`backend/pom.xml`) with Spring Boot 3.3.5, Java 21, Apache Camel 4.8.1, Flyway, MySQL Connector/J, JJWT, and Springdoc OpenAPI.
- Vite + React 18 + TypeScript + Tailwind CSS frontend skeleton (`frontend/`).
- Multi-stage `Dockerfile` tuned for 512 MB RAM free-tier cloud containers.

## 4. Why We Are Building It
Starting with a clean modular monolith and externalized configuration prevents spaghetti coupling, ensures zero hardcoded secrets, and allows smooth future migration to microservices, AWS, and Kubernetes.

## 5. Technologies Being Used
Java 21, Spring Boot 3.3.5, Apache Camel 4.8.1, React 18, TypeScript 5.6, Vite 5.4, Tailwind CSS 3.4, Docker (optional).

## 6. What Each Technology Does
- **Spring Boot + Maven:** Manages backend dependencies, embedded Tomcat server, and modular dependency injection.
- **Apache Camel:** Provides Enterprise Integration Patterns (EIP) for XML routing, XSD validation, and retries.
- **React + Vite + TypeScript:** Compiles and serves the strongly typed single-page frontend.
- **Docker:** Packages the backend into a predictable Alpine Linux JRE 21 container.

## 7. Architecture and Workflow
The frontend communicates with `/api/v1/*` REST controllers over HTTPS/JSON. Backend modules (`auth`, `customer`, `store`, `product`, `inventory`, `cart`, `order`, `payment`, `notification`) interact via service interfaces or via Apache Camel XML routes (`integration`).

## 8. Folder Structure
- `bharatmart-platform/.devcontainer/` — Cloud IDE & Docker Compose setup.
- `bharatmart-platform/backend/` — Java 21 Spring Boot modular monolith.
- `bharatmart-platform/frontend/` — React + TypeScript SPA.
- `bharatmart-platform/docs/` — Phase-by-phase documentation.

## 9. Files Created or Modified
- `backend/pom.xml`
- `backend/Dockerfile`
- `backend/src/main/java/com/bharatmart/BharatMartApplication.java`
- `backend/src/main/resources/application.yml`
- `frontend/package.json`, `frontend/tsconfig.json`, `frontend/vite.config.ts`, `frontend/tailwind.config.js`, `frontend/index.html`, `frontend/src/App.tsx`, `frontend/src/main.tsx`, `frontend/src/index.css`

## 10. Explanation of Every Important File
- **`backend/pom.xml`:** Declares the Spring Boot 3.3.5 parent, Camel 4.8.1 BOM, `flyway-mysql` (required for Flyway 10 + MySQL 8), JJWT `0.12.6`, and Springdoc OpenAPI.
- **`backend/Dockerfile`:** Uses a 2-stage build (Maven builder → `eclipse-temurin:21-jre-alpine` non-root runtime) with `-Xmx300m -XX:+UseSerialGC` so the JVM stays safely under Render's 512 MB RAM ceiling.
- **`application.yml`:** Externalizes `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, and `ALLOWED_ORIGINS` using `${ENV_VAR:default}` syntax.

## 11. Explanation of the Code
`BharatMartApplication.java` is annotated with `@SpringBootApplication`, enabling component scanning across all `com.bharatmart.*` modules, Spring Boot auto-configuration, and Camel context initialization. `App.tsx` renders a Tailwind-styled confirmation shell ready for routing in Phase 6.

## 12. How the Components Communicate
- **Browser ↔ Backend:** Axios sends JSON over HTTP/HTTPS to Spring Boot port `8080`.
- **Backend ↔ Database:** HikariCP connection pool (`maximum-pool-size: 5` to respect free-tier DB connection limits) connects via JDBC to MySQL/TiDB.

## 13. Database Changes
None in Phase 2 (`spring.flyway.enabled=true` is configured and ready for Phase 3 SQL migrations).

## 14. APIs Introduced
Spring Boot Actuator health endpoint (`/actuator/health`) and Swagger UI (`/swagger-ui.html`) are enabled.

## 15. Configuration Changes
- `open-in-view: false` set in JPA to prevent lazy-loading anti-patterns in controllers.
- `hibernate.ddl-auto: validate` enforced so Flyway—not Hibernate—owns schema changes.

## 16. Commands to Execute
```bash
# Backend compile check
cd backend && mvn clean compile

# Frontend dependency install & build check
cd ../frontend && npm install && npm run build
```

## 17. Expected Output
- `mvn clean compile` exits with `BUILD SUCCESS`.
- `npm run build` outputs production bundle into `frontend/dist/` with zero TypeScript errors.

## 18. Testing Instructions
Start the backend (`mvn spring-boot:run`) once a database is connected in Phase 3, and verify `GET http://localhost:8080/actuator/health` returns `{"status":"UP"}`.

## 19. Common Errors and Solutions
- **`FlywayException: Unsupported Database: MySQL 8.x`:** Ensure `org.flywaydb:flyway-mysql` is included alongside `flyway-core` (already included in our `pom.xml`).
- **Out-of-Memory (Exit Code 137) in 512 MB container:** Ensure `JAVA_OPTS` includes `-Xmx300m -XX:MaxMetaspaceSize=128m`.

## 20. Security Considerations
- No secrets are committed; `.env` is listed in `.gitignore`.
- `Dockerfile` runs as a non-root `spring` user.

## 21. What We Achieved
Created a clean, production-structured Maven + Vite/React project skeleton with externalized configuration and memory-tuned containerization.

## 22. Completion Checklist
- [x] Folder structure created
- [x] `backend/pom.xml` and `application.yml` configured
- [x] `backend/Dockerfile` created
- [x] `frontend/` Vite + React + TypeScript + Tailwind skeleton created

## 23. What Comes Next
Phase 3: MySQL Database Implementation (Flyway `V1__schema.sql` with all 23 tables, indexes, constraints, and `V2__seed_data.sql` for Indian states, cities, and stores).
