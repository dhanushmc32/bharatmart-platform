# BharatMart — India-Wide Multi-Store E-Commerce Platform

Complete end-to-end platform built on a **₹0 budget** with **Java 21 + Spring Boot 3**, **Apache Camel (XML + XSD + XXE protection)**, **MySQL 8 / TiDB Cloud**, **React + TypeScript + Tailwind**, and **COD-only payments**.

## Fastest Way to Run (₹0, Zero Local Admin Rights — GitHub Codespaces)
1. Push this `bharatmart-platform` folder to a free GitHub repository.
2. Click **Code → Codespaces → Create codespace on main**.
3. Wait ~2 minutes for Java 21, Maven, Node 20, and MySQL 8.0 to start automatically.
4. In the Codespaces terminal:
```bash
cp .env.example .env

# Start Backend (runs Flyway V1 & V2 migrations automatically)
cd backend && mvn spring-boot:run
```
5. Open a **second terminal** in Codespaces and start the Frontend:
```bash
cd frontend && npm install && npm run dev
```
6. Open the forwarded port `5173` in your browser!
   - **Customer login:** `customer@bharatmart.in` / `Password@123`
   - **Store Manager login:** `manager.blr@bharatmart.in` / `Password@123`
   - **Admin & XML Monitor login:** `admin@bharatmart.in` / `Password@123`
   - **Swagger API Docs:** `http://localhost:8080/swagger-ui.html`

## Run Backend Tests
```bash
cd backend && mvn test
```
