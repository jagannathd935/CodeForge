# CodeForge ⚡
### Modern Competitive Programming & Online Judge Platform

![CodeForge Platform](https://img.shields.io/badge/Platform-CodeForge-3b82f6?style=for-the-badge&logo=codeforces&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-18.3-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-5.4-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Java](https://img.shields.io/badge/Java-17%20%2F%2025-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)

**CodeForge** is a full-featured, developer-grade competitive programming and automated online judge platform built with **Spring Boot**, **React**, **Vite**, **MySQL 8.0**, and the **Monaco Editor**. Designed with an authentic, clean, minimalist aesthetic (inspired by LeetCode and GitHub), CodeForge offers seamless dark/light theme switching, multi-language sandbox execution, live contest judging, submission auditing, and role-based administration.

---

## 📑 Table of Contents

- [Key Features](#-key-features)
- [Architecture & Tech Stack](#-architecture--tech-stack)
- [Project Directory Structure](#-project-directory-structure)
- [Quick Start & Setup](#-quick-start--setup)
  - [Prerequisites](#prerequisites)
  - [1. Database Configuration](#1-database-configuration)
  - [2. Launch Spring Boot Backend](#2-launch-spring-boot-backend)
  - [3. Launch React Frontend](#3-launch-react-frontend)
  - [One-Click Start Scripts](#one-click-start-scripts)
- [Default Demo Accounts](#-default-demo-accounts)
- [Core Platform Interfaces](#-core-platform-interfaces)
- [Execution Sandbox & Security](#-execution-sandbox--security)
- [REST API Reference](#-rest-api-reference)
- [License](#-license)

---

## ✨ Key Features

### 🖥️ 1. Modern Code Editor Workspace
- **Powered by Monaco Editor**: The same engine that powers VS Code, featuring syntax highlighting, line numbering, auto-formatting, and smooth scrolling.
- **Dynamic Dark & Light Themes**: Real-time theme synchronization between the platform and Monaco (`vs-dark` and `light`).
- **Multi-Language Support**:
  - **Java** (OpenJDK 17 / 25)
  - **Python** (Python 3.10+)
  - **C++** (GCC / C++17)
  - **C** (GCC)
  - **JavaScript** (Node.js)
- **Zero Code-Bleed State Management**: Isolated local caching per language and problem ID; switching languages instantly loads the target language's clean boilerplate without race conditions.

### ⚡ 2. High-Performance Sandboxed Execution Engine
- **Deadlock-Free Execution**: OS-level redirected file I/O prevents process pipe buffer deadlocks on large stdout/stderr streams.
- **Run vs Submit Evaluation**:
  - **Run Code**: Instantly tests code against public sample test cases or arbitrary custom stdin inputs.
  - **Submit Solution**: Evaluates code against full hidden test suites with strict time limits (e.g. 1000 ms) and memory limits (256 MB).
- **Rich Verdict Diagnostics**: Clear feedback on `ACCEPTED`, `WRONG_ANSWER`, `COMPILATION_ERROR`, `TIME_LIMIT_EXCEEDED`, and `RUNTIME_ERROR` with line-by-line compiler error logs and diff comparisons.

### 🏆 3. Timed Contests & Dynamic Leaderboards
- **Live Contests**: Real-time countdown clock, enrolled participant rosters, and per-problem scoring.
- **Penalty Tracking**: Standard ICPC-style penalty calculation: `penalty = solveTimeMinutes + (wrongAttempts * penaltyPerWrong)`.
- **Auto-Grading**: Submissions within an active contest automatically update participant standings.

### 🛡️ 4. Role-Based Access Control (RBAC)
- **Admin**: Full problem lifecycle management, hidden test case authoring, user management, and live operational telemetry (`/api/admin/logs`).
- **Organizer**: Create contests, assign problems and points, and monitor live submissions.
- **Participant**: Solve problems, test custom inputs, track streak, and view detailed submission histories.

### 📊 5. Submission History & Code Inspection
- Submissions table tracking Problem, Language, Status, Runtime, Memory, and Timestamp.
- Interactive code modal allowing one-click code inspection and copying of previously submitted solutions.

---

## 🏛️ Architecture & Tech Stack

```
                        ┌────────────────────────┐
                        │   Vite + React 18 UI   │
                        │  (Tailwind + Monaco)   │
                        └───────────┬────────────┘
                                    │ HTTP / REST (Axios)
                                    ▼
                        ┌────────────────────────┐
                        │  Spring Boot 3.3 REST  │
                        │  (Spring Security+JWT) │
                        └─────┬────────────┬─────┘
                              │            │
            JPA / Hibernate   │            │ Process / Sandbox
                              ▼            ▼
                     ┌─────────────┐  ┌─────────────────────────┐
                     │  MySQL 8.0  │  │  Local / Docker Sandbox │
                     │  Database   │  │  (Java, Python, C++, C)│
                     └─────────────┘  └─────────────────────────┘
```

| Layer | Technologies |
| :--- | :--- |
| **Frontend Framework** | React 18, Vite 5, Tailwind CSS |
| **Code Editor** | `@monaco-editor/react` (VS Code engine) |
| **Icons & UI** | Lucide React, Custom Theme Engine (Dark/Light) |
| **Backend Framework** | Spring Boot 3.3.4, Java 17 / 25 |
| **Security & Auth** | Spring Security 6, JJWT (HMAC-SHA256), BCrypt |
| **Data Persistence** | Spring Data JPA, Hibernate, HikariCP |
| **Relational Database**| MySQL 8.0 (InnoDB) |
| **Execution Compilers**| `javac` 25, `g++` 6.3+ (C++17), `python` 3.12, `node` |

---

## 📂 Project Directory Structure

```
CodeForge/
├── backend/                        # Spring Boot 3 Backend
│   ├── src/main/java/com/codeforge/
│   │   ├── auth/                   # JWT Auth, UserDetailsService, AuthController
│   │   ├── common/                 # Global ApiResponse, Exception handlers
│   │   ├── config/                 # SecurityConfig, CorsConfig, DatabaseInitializer
│   │   ├── contest/                # Contest entities, controllers, standings service
│   │   ├── execution/              # JudgeService, LocalSandboxExecutor, CodeSanitizer
│   │   ├── problem/                # Problem CRUD, Difficulty, Acceptance service
│   │   ├── submission/             # Submission DTOs, controllers, run/eval services
│   │   ├── testcase/               # TestCase entity and repository
│   │   └── user/                   # User entity, roles (ADMIN, ORGANIZER, USER)
│   ├── src/main/resources/
│   │   └── application.yml         # MySQL & sandbox configuration
│   └── pom.xml                     # Maven dependencies
├── frontend/                       # React 18 + Vite Frontend
│   ├── src/
│   │   ├── api/                    # Axios interceptors with JWT Bearer token injection
│   │   ├── components/             # Navbar, Badges, Modals, StatCards
│   │   ├── context/                # AuthContext (roles, tokens), ThemeContext (dark/light)
│   │   ├── pages/                  # 6 Core MVP views:
│   │   │   ├── LoginPage.jsx       # Clean auth form with Dennis Ritchie quote card
│   │   │   ├── RegisterPage.jsx    # Registration
│   │   │   ├── ProblemListPage.jsx # Problem Bank with search, filters & acceptance rates
│   │   │   ├── ProblemDetailPage.jsx # Monaco Editor split pane, custom input & console
│   │   │   ├── ContestsPage.jsx    # Live, upcoming, and completed contests
│   │   │   ├── SubmissionsPage.jsx # Submission table & code viewer modal
│   │   │   ├── ProfilePage.jsx     # User statistics, streak, and difficulty breakdown
│   │   │   ├── AdminDashboardPage.jsx # Problem authoring, user manager, and system telemetry
│   │   │   └── OrganizerDashboardPage.jsx # Contest studio and problem manager
│   │   ├── index.css               # Tailwind directives & high-contrast light mode overrides
│   │   └── App.jsx                 # Client-side routing & protected route guards
│   ├── package.json
│   └── vite.config.js
├── database/                       # Database scripts & schemas
│   └── setup_database.sql
├── docs/                           # Documentation
│   ├── api.md                      # REST API specification
│   └── setup.md                    # Setup and Viva presentation guide
├── run.ps1                         # One-click PowerShell launcher
├── run.bat                         # One-click Command Prompt launcher
└── README.md
```

---

## 🚀 Quick Start & Setup

### Prerequisites

Ensure you have the following installed on your machine:
- **Java JDK 17 or higher** (`java -version`, `javac -version`)
- **Node.js 18+ and npm** (`node -v`, `npm -v`)
- **Apache Maven 3.9+** (`mvn -v`)
- **MySQL 8.0 Server**
- *(Optional for C++ / Python)*: `g++` (MinGW) and `python` 3.10+ in system `PATH`.

---

### 1. Database Configuration

1. Launch MySQL Server (default port: `3307` or `3306`).
2. If running a dedicated instance on port 3307:
   ```powershell
   & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --datadir=".\mysql-data" --port=3307 --console
   ```
3. The application automatically creates the database `codeforge_db` and seeds initial problems, accounts, and contests on first startup via `DatabaseDataInitializer.java`.

> [!NOTE]
> Database credentials can be customized in [`backend/src/main/resources/application.yml`](backend/src/main/resources/application.yml) via the environment variables `MYSQL_PORT`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.

---

### 2. Launch Spring Boot Backend

In your terminal, navigate to `backend/` and run:

```powershell
cd backend
mvn spring-boot:run
```

The Spring Boot backend will start on **`http://localhost:8080`**.
Verify health by visiting: `http://localhost:8080/api/health`

---

### 3. Launch React Frontend

In a new terminal window, navigate to `frontend/` and start the Vite dev server:

```powershell
cd frontend
npm install
npm run dev
```

The React frontend will be accessible at: **`http://localhost:5173`**

---

### One-Click Start Scripts

To start both services simultaneously on Windows, run the provided root launcher:

```powershell
.\run.ps1
```
*Or double-click `run.bat`.*

---

## 👥 Default Demo Accounts

On startup, CodeForge automatically initializes test accounts for each role:

| Role | Username | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Full access, Problem Bank authoring, User management, System Logs & JVM telemetry |
| **Organizer** | `organizer` | `organizer123` | Contest Studio, Problem authoring, Contest management |
| **Participant** | `john_doe` | `user123` | Solve problems, Run/Submit in Monaco, Track streak & profile |

---

## 🖥️ Core Platform Interfaces

### 1. Authentication (`/login`, `/register`)
- Clean split card: credentials input on the left, high-contrast Dennis Ritchie quote card on the right.
- Persistent login sessions with JWT Bearer tokens in `localStorage`.

### 2. Problem Bank (`/problems`)
- Search by problem title, topic, or keyword.
- Filter by difficulty: `All`, `Easy`, `Medium`, `Hard`.
- Acceptance rate calculations and indicator for already-solved challenges.

### 3. Problem Workspace & Code Editor (`/problems/:id`)
- **Split-Pane Layout**: Description, sample test cases, constraints, topics, editorial, and past submissions on the left; code editor on the right.
- **Monaco Toolbar**: Language selector, Reset template button, Fullscreen toggle, `Run`, and `Submit`.
- **Bottom Drawer**: Switchable sample test cases (`Case 1`, `Case 2`), `+ Custom Input` with editable stdin textarea, and expandable test verdict console.

### 4. Submission History (`/submissions`)
- Chronological table of all user submissions showing Problem title, Language, Status badge, Runtime in ms, Memory in MB, and timestamp.
- Code preview modal with syntax highlighting and copy button.

### 5. Profile & Statistics (`/profile`)
- User avatar circle with initials.
- Total Problems Solved, Total Submissions, and Day Streak.
- Visual difficulty progress bars (`Easy`, `Medium`, `Hard`).

### 6. Admin Panel (`/admin`)
- Two-tab Problem Authoring Form: Problem Details & Test Case Builder.
- User management table with role editing.
- Live system telemetry displaying JVM memory consumption, MySQL database connectivity, and sandbox engine status.

---

## 🔒 Execution Sandbox & Security

CodeForge takes code execution security seriously:

1. **Static Analysis & Sanitization (`CodeSanitizer.java`)**:
   - Inspects incoming source code before execution for prohibited tokens:
     - **Java**: Disallows `ProcessBuilder`, `Runtime.getRuntime()`, `System.exit()`, reflection, and raw sockets.
     - **Python**: Disallows `os.system`, `subprocess`, `__import__`, `eval()`, `exec()`, `shutil`.
     - **C / C++**: Disallows `system()`, `popen()`, `fork()`, `kill()`, `execvp()`.
2. **Resource Ceilings**:
   - Hard execution timeout watchdog (default 1000–5000 ms).
   - Memory ceiling (default 256 MB heap).
3. **Information Leak Protection**:
   - Public APIs (`GET /api/problems/{id}`) only return test cases where `isHidden = false`.
   - Hidden edge cases are only queried internally during full evaluation.

---

## 📡 REST API Reference

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Register new participant account |
| `POST` | `/api/auth/login` | Public | Authenticate user and obtain JWT Bearer token |
| `GET` | `/api/problems` | Public | List all problems with pagination and filters |
| `GET` | `/api/problems/{id}` | Public | Get problem details and public sample test cases |
| `POST` | `/api/submissions/run` | Authenticated | Execute code against sample or custom stdin inputs |
| `POST` | `/api/submissions` | Authenticated | Submit code for grading against all hidden test cases |
| `GET` | `/api/submissions/my` | Authenticated | Get current user's submission history |
| `GET` | `/api/users/me/stats` | Authenticated | Get user profile statistics and solve breakdown |
| `GET` | `/api/contests` | Public | List active, upcoming, and past contests |
| `POST` | `/api/admin/problems` | `ROLE_ADMIN` | Create new DSA problem with test cases |
| `GET` | `/api/admin/logs` | `ROLE_ADMIN` | Real-time system telemetry and operational logs |

*For complete request and response schemas, see [docs/api.md](docs/api.md).*

---

## 📄 License

This project is developed for educational, academic, and competitive programming purposes. Distributed under the **MIT License**.
