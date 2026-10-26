# CodeForge: Complete Setup & Local Deployment Guide

---

## 1. Prerequisites

Before running CodeForge, ensure the following tools are installed:
- **Java Development Kit (JDK)**: Version 17+ (Tested on Java 17 and Java 25)
- **Apache Maven**: Version 3.8+ (`mvn -version`)
- **Node.js & npm**: Node.js 18+ & npm 9+ (`node -v`, `npm -v`)
- **MySQL Server**: 8.0+
- **Compilers / Interpreters** (for local code judge):
  - Java: `javac`
  - Python: `python` / `python3`
  - C/C++: `gcc` / `g++` (MinGW on Windows, build-essential on Linux)
  - JavaScript: `node`

---

## 2. Quick Start (Step-by-Step)

### Step 1: Database Initialization
1. Ensure your MySQL Server is running (default port 3306 or 3307).
2. Execute the setup script in PowerShell or Terminal:
   ```powershell
   Get-Content "database\setup_database.sql" | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
   ```
   *(Or run `schema.sql` followed by `seed_data.sql`)*

### Step 2: Configure Environment Variables
Copy `.env.example` to `.env` or set system environment variables:
```bash
MYSQL_HOST=localhost
MYSQL_PORT=3307
MYSQL_DATABASE=codeforge_db
MYSQL_USERNAME=root
MYSQL_PASSWORD=
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION=86400000
DOCKER_ENABLED=false
EXECUTION_TIMEOUT=5000
MEMORY_LIMIT=256
```

### Step 3: Run Spring Boot Backend
```powershell
cd backend
mvn spring-boot:run
```
- Server URL: `http://localhost:8080`
- Health check: `http://localhost:8080/api/health`

### Step 4: Run React Frontend
In a new terminal window:
```powershell
cd frontend
npm install
npm run dev
```
- Client URL: `http://localhost:5173`

---

## 3. Docker Deployment (Single Command)

If Docker & Docker Compose are available on your system:
```bash
docker compose up --build
```
This launches:
- **MySQL 8.0** on port 3307 with automated schema and seed data mounting
- **Spring Boot 3.3.4** backend on port 8080 (includes JDK 17, GCC, Python 3, Node runtime)
- **React Vite Frontend** on port 5173 served via Nginx with automated API reverse proxy

To stop the containers:
```bash
docker compose down
```

---

## 4. Default Seed Accounts

The platform seeds accounts for all three user roles:

| Role | Username | Password | Accessible Features |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin` | `admin123` | Full access, Admin Dashboard, Problem & Test Case CRUD, User Role Management, Submissions Monitor |
| **ORGANIZER** | `organizer` | `organizer123` | Organizer Dashboard, Create/Edit Contests, Add Contest Problems, View Submissions & Leaderboards |
| **PARTICIPANT** | `john_doe` | `user123` | Browse Problems, Monaco Editor, Run & Submit Code, View History, Real-time Profile & Stats, Contests |

---

## 5. College Viva & Technical Architecture Q&A

### Q1: Why did you choose a Modular Monolith architecture?
> "For CodeForge, a modular monolith minimizes deployment overhead, eliminates distributed transaction complexities, and delivers near-zero latency between the submission controller and judge engine. The backend is partitioned into domain packages (`auth`, `user`, `problem`, `submission`, `execution`, `contest`, `admin`), which ensures high cohesion and loose coupling. When scaling requirements demand it, these bounded contexts can seamlessly transition into microservices."

### Q2: How does the isolated judge prevent malicious student code execution?
> "CodeForge implements defense-in-depth:
> 1. **Static Analysis & Sanitization**: `CodeSanitizer` scans incoming code for forbidden calls (e.g. `Runtime.getRuntime()`, `ProcessBuilder`, `system()`, `fork()`, `exec()`, `os.system`).
> 2. **Sandbox Isolation**: Execution runs in Docker containers (or process-isolated workers) with hard CPU quotas (0.5 cores), restricted memory ceilings (256 MB), timeout watchdog guards (5000 ms), and blocked network egress (`--network none`).
> 3. **Non-privileged Execution**: Runs under a restricted unprivileged user with write access restricted only to a temporary directory that is purged immediately after evaluation."

### Q3: How is the security of hidden test cases guaranteed?
> "Test cases have an `is_hidden` boolean column. The public problem API (`GET /api/problems/{id}`) specifically filters for `is_hidden = false` and returns only sample test cases for problem rendering. When a participant submits their solution, the backend judge queries all test cases directly from MySQL to evaluate algorithmic correctness without ever leaking the edge-case inputs or expected outputs to the client."

### Q4: How is authentication and authorization enforced?
> "Authentication uses stateless JSON Web Tokens (JWT) signed using HMAC-SHA256 with custom claims containing the user ID, username, and role. Passwords are never stored in plaintext; they are hashed using the BCrypt algorithm with a cost factor of 10. Role-based authorization is enforced at the Spring Security filter chain and method-level using `@PreAuthorize`."

### Q5: How is contest penalty calculated?
> "CodeForge uses the standard ICPC scoring policy:
> $\text{Penalty} = \text{Solve Time (minutes since contest start)} + (20 \times \text{Number of Wrong Attempts before Accepted})$.
> If a problem is never accepted, wrong attempts do not count toward the participant's penalty."
