# CodeForge: Complete Setup & College Viva Guide

## 1. Quick Start Guide

### Step 1: Database Setup
1. Ensure MySQL Server 8.0 is running on port 3306.
2. Open PowerShell and run:
   ```powershell
   Get-Content "database\setup_database.sql" | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
   ```
   *(Enter your MySQL root password when prompted)*

---

### Step 2: Launch Spring Boot Backend
1. Open the `backend/` folder in **IntelliJ IDEA** or **VS Code**.
2. Run `CodeForgeApplication.java`.
   *(Or in terminal: `mvn spring-boot:run`)*
3. Backend starts at `http://localhost:8080`.
4. Verify by visiting `http://localhost:8080/api/health`.

---

### Step 3: Launch React Frontend
1. Open terminal in the `frontend/` directory:
   ```powershell
   cd frontend
   npm run dev
   ```
2. Open `http://localhost:5173` in your browser.

---

## 2. Default Accounts for Demonstration

| Account Type | Username | Password | Accessible Features |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` | Full access, Admin Panel, Create/Delete Problems, Test Cases, View Users |
| **Student User** | `john_doe` | `user123` | Browse Problems, Code in Monaco, Run/Submit, View Profile & History |

---

## 3. College Viva / Presentation Q&A

### Q1: Why did you choose a Modular Monolith instead of Microservices?
> "For Version 1 of an online judge, a modular monolith minimizes deployment complexity, avoids distributed transaction overhead, and keeps latency between the submission controller and judge engine at zero. The code is still separated into domain packages (auth, problem, submission, execution) so it can be broken into microservices in the future if needed."

### Q2: How is user-submitted code kept safe from crashing the server?
> "CodeForge never executes arbitrary student code inside the Spring Boot JVM. Execution occurs in an isolated sandbox with hard resource limits: strict CPU quotas (0.5 cores), memory ceiling (256 MB), execution timeout watchdog (5000 ms), non-root unprivileged user, and completely disabled network access (`--network none`)."

### Q3: How do hidden test cases work?
> "Test cases have an `is_hidden` boolean column. The public problem API (`GET /api/problems/{id}`) specifically filters for `is_hidden = false` and returns only sample test cases for the problem description. When a user submits code, the backend judge queries all test cases directly from the database to evaluate correctness without ever leaking the edge-case inputs to the client."

### Q4: How is authentication handled?
> "Stateless JSON Web Tokens (JWT) using the JJWT library with HMAC-SHA256 signatures. Passwords are never stored in plaintext; they are hashed using the BCrypt algorithm with a cost factor of 10."
