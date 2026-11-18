# Phase 2B — Technology & Free Environment Setup (`03-technology-setup.md`)

## 1. Phase Name
Phase 2: Architecture & Development Environment Setup (Part B — Free Environment Setup).

## 2. Objective
Provide a ₹0, zero-admin-rights, card-free development environment using **GitHub Codespaces** (primary) or portable user-space tools + **TiDB Cloud Starter** (alternative).

## 3. What We Are Building
- `.devcontainer/devcontainer.json` — Automated cloud IDE workspace with Java 21, Maven, Node 20, and VS Code extensions.
- `.devcontainer/docker-compose.yml` — MySQL 8.0 Community Edition container + optional RabbitMQ profile.
- `.env.example` & `.gitignore` — Safe environment variable templates.

## 4. Why We Are Building It
You should not need administrator rights on your computer or paid cloud VMs to build and run an enterprise Java + React + MySQL platform.

## 5. Technologies Being Used
[GitHub Codespaces](https://github.com/features/codespaces) (120 free core hours/month, 15 GB storage, no card), Docker Compose, MySQL 8.0 Community Edition, [TiDB Cloud Starter](https://docs.pingcap.com/tidbcloud/serverless-limitations/) (optional cloud DB), VS Code Web.

## 6. What Each Technology Does
- **DevContainer (`devcontainer.json`):** Tells GitHub Codespaces to spin up a Linux container pre-loaded with Java 21, Maven, Node 20, and IDE extensions directly in your browser.
- **Docker Compose:** Runs MySQL 8.0 alongside your app container on `localhost:3306`.

## 7. Architecture and Workflow
When you open the repository in GitHub Codespaces, GitHub builds the DevContainer, starts MySQL 8.0, forwards ports `8080` (Spring Boot), `5173` (React), and `3306` (MySQL), and opens VS Code in your browser.

## 8. Folder Structure
- `.devcontainer/devcontainer.json`
- `.devcontainer/docker-compose.yml`
- `.env.example`
- `.gitignore`

## 9. Files Created or Modified
- `.devcontainer/devcontainer.json`
- `.devcontainer/docker-compose.yml`
- `.env.example`
- `.gitignore`

## 10. Explanation of Every Important File
- **`devcontainer.json`:** Installs Java 21, Maven, Node 20, and helpful VS Code extensions (`vscjava.vscode-java-pack`, `vmware.vscode-boot-dev-pack`, `bradlc.vscode-tailwindcss`, `cweijan.vscode-mysql-client2`).
- **`docker-compose.yml`:** Starts `mysql:8.0` with a healthcheck and persistent volume `mysql_data`. RabbitMQ is placed under `profiles: ["messaging"]` so it uses zero RAM unless explicitly enabled.

## 11. Explanation of the Code
`${DB_USER:-bharatmart_user}` in `docker-compose.yml` uses environment variable substitution with safe dev-only defaults, while `.env.example` documents every variable needed for local or cloud runs.

## 12. How the Components Communicate
The `app` container uses `network_mode: service:db` so Spring Boot inside Codespaces connects to MySQL at `localhost:3306` seamlessly.

## 13. Database Changes
Initializes an empty `bharatmart` MySQL 8.0 database schema ready for Flyway in Phase 3.

## 14. APIs Introduced
None (infrastructure setup only).

## 15. Configuration Changes
Copy `.env.example` to `.env` before running locally (`cp .env.example .env`).

## 16. Commands to Execute
```bash
# 1. Copy environment template
cp .env.example .env

# 2. (In GitHub Codespaces) Open repo -> Code -> Codespaces -> Create codespace on main
# 3. Verify installed versions inside Codespaces terminal:
java -version
mvn -version
node -v
```

## 17. Expected Output
- `java -version` prints `openjdk version "21.x.x"`.
- `mvn -version` prints `Apache Maven 3.9.x`.
- `node -v` prints `v20.x.x`.

## 18. Testing Instructions
Run `mysql -h 127.0.0.1 -u bharatmart_user -pbharatmart_dev_pass -e "SHOW DATABASES;"` in Codespaces to confirm `bharatmart` exists.

## 19. Common Errors and Solutions
- **Port 3306 connection refused:** Wait 10–15 seconds for MySQL container healthcheck to pass, or check `docker ps`.
- **Codespaces free quota:** Stop/suspend your codespace when not coding (`Codespaces: Stop Current Codespace`) to preserve your 120 free core hours/month.

## 20. Security Considerations
- Never commit `.env` to GitHub (enforced by `.gitignore`).
- Replace the dev `JWT_SECRET` in `.env` before any public deployment.

## 21. What We Achieved
Set up a reproducible, zero-cost, browser-based development environment requiring no local administrator privileges.

## 22. Completion Checklist
- [x] `.devcontainer/devcontainer.json` created
- [x] `.devcontainer/docker-compose.yml` created
- [x] `.env.example` and `.gitignore` created

## 23. What Comes Next
Phase 3: MySQL Database Implementation (`V1__schema.sql` and `V2__seed_data.sql` Flyway migrations + `04-database-design.md`).
