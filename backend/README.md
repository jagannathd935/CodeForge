# CodeForge Backend ⚡
### Enterprise-Grade REST API & Online Judge Execution Engine

![Java](https://img.shields.io/badge/Java-17%20%2F%2025-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.3-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-JJWT%200.12.6-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)

The **CodeForge Backend** is a high-throughput, modular monolithic service built with **Spring Boot 3.3**, **Spring Security 6**, and **Hibernate / MySQL 8.0**. It powers the automated judging, timed ICPC-style contests, user management, and multi-language sandbox execution for CodeForge.

---

## 📑 Table of Contents

- [Architectural Overview](#-architectural-overview)
- [Tech Stack & Dependencies](#-tech-stack--dependencies)
- [Getting Started & Running](#-getting-started--running)
- [Default Demo Accounts](#-default-demo-accounts)
- [Complete REST API Catalog](#-complete-rest-api-catalog)
  - [1. Health & Telemetry](#1-health--telemetry)
  - [2. Authentication (`/api/auth`)](#2-authentication-apiauth)
  - [3. Users & Profile (`/api/users`)](#3-users--profile-apiusers)
  - [4. Problems & Topics (`/api/problems`, `/api/topics`)](#4-problems--topics-apiproblems-apitopics)
  - [5. Code Execution & Submissions (`/api/submissions`)](#5-code-execution--submissions-apisubmissions)
  - [6. Contests & Leaderboards (`/api/contests`)](#6-contests--leaderboards-apicontests)
  - [7. Organizer Studio (`/api/organizer`)](#7-organizer-studio-apiorganizer)
  - [8. Admin Management & Telemetry (`/api/admin`)](#8-admin-management--telemetry-apiadmin)
- [Execution Sandbox & Security Architecture](#-execution-sandbox--security-architecture)
- [Error Handling & API Response Format](#-error-handling--api-response-format)

---

## 🏛️ Architectural Overview

```
                      ┌────────────────────────────────────────┐
                      │        Vite + React Frontend           │
                      └───────────────────┬────────────────────┘
                                          │ JSON / REST (Axios)
                                          ▼
                      ┌────────────────────────────────────────┐
                      │         Spring Security Filter         │
                      │       (JwtAuthenticationFilter)        │
                      └───────────────────┬────────────────────┘
                                          │
            ┌─────────────────────────────┼─────────────────────────────┐
            ▼                             ▼                             ▼
  ┌───────────────────┐         ┌───────────────────┐         ┌───────────────────┐
  │ Problem Controller│         │Submission / Judge │         │ Contest Controller│
  │  & Topic Service  │         │  Execution Engine │         │  & Standings Svc  │
  └─────────┬─────────┘         └─────────┬─────────┘         └─────────┬─────────┘
            │                             │                             │
            ▼                             ▼                             ▼
  ┌───────────────────────────────────────────────────────────────────────────────┐
  │                           Spring Data JPA / Hibernate                         │
  └───────────────────────────────────────┬───────────────────────────────────────┘
                                          │
                                          ▼
                                ┌───────────────────┐
                                │     MySQL 8.0     │
                                │   (codeforge_db)  │
                                └───────────────────┘
```

---

## 🛠️ Tech Stack & Dependencies

| Component | Technology | Version | Description |
| :--- | :--- | :--- | :--- |
| **Framework** | Spring Boot | `3.3.4` | Core MVC, Dependency Injection & Web Server |
| **Language** | Java | `17` / `25` | Modern Java runtime with virtual thread support |
| **Persistence** | Spring Data JPA / Hibernate | `6.5` | ORM, Criteria queries, and connection pooling |
| **Connection Pool** | HikariCP | `5.1.0` | Ultra-low latency database connection pool |
| **Database** | MySQL Server | `8.0` | Relational storage (InnoDB storage engine) |
| **Security** | Spring Security | `6.3` | RBAC method security (`@PreAuthorize`) |
| **Token Auth** | JJWT | `0.12.6` | Stateless HMAC-SHA256 JWT tokens |
| **Sandbox Execution** | Local / Docker Engine | — | Sandboxed process runner with redirected file I/O |

---

## 🚀 Getting Started & Running

### Prerequisites
- **JDK 17 or 25** (`java -version`, `javac -version`)
- **Apache Maven 3.9+** (`mvn -v`)
- **MySQL 8.0 Server** running on port `3307` or `3306`

### Running via Terminal
```bash
# 1. Navigate to backend directory
cd backend

# 2. Launch Spring Boot application
mvn spring-boot:run
```

### Custom Database Configuration
Database parameters can be set via environment variables or in `application.yml`:
```powershell
$env:MYSQL_PORT="3307"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD=""
mvn spring-boot:run
```

---

## 👥 Default Demo Accounts

CodeForge seeds initial accounts on first startup:

| Account | Username | Password | Role | Permissions |
| :--- | :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | `ROLE_ADMIN` | Full control, problem authoring, test case manager, telemetry |
| **Organizer** | `organizer` | `organizer123` | `ROLE_ORGANIZER` | Contest studio, manage contests, author problems |
| **Participant** | `john_doe` | `user123` | `ROLE_USER` | Solve problems, run/submit code, view profile & history |

---

## 📡 Complete REST API Catalog

> [!NOTE]
> All endpoints are prefixed with `/api`. Authenticated endpoints require the header:
> `Authorization: Bearer <your_jwt_token>`

---

### 1. Health & Telemetry

#### `GET /api/health`
Checks server readiness, uptime, and database connectivity.
- **Access**: Public
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "CodeForge backend foundation is operational",
  "data": {
    "database": "Connected",
    "service": "CodeForge Backend API",
    "status": "UP",
    "registeredUsers": 5
  },
  "timestamp": "2026-09-21T20:00:00"
}
```

---

### 2. Authentication (`/api/auth`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Register a new user account |
| `POST` | `/api/auth/login` | Public | Authenticate user & retrieve JWT token |

#### `POST /api/auth/login`
**Request Body**:
```json
{
  "usernameOrEmail": "john_doe",
  "password": "user123"
}
```
**Response**: `200 OK`
```json
{
  "success": true,
  "message": "User logged in successfully",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJqb2huX2RvZSIs...",
    "tokenType": "Bearer",
    "id": 3,
    "username": "john_doe",
    "email": "john@codeforge.dev",
    "role": "ROLE_USER"
  }
}
```

---

### 3. Users & Profile (`/api/users`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/users/me` | Authenticated | Get current user's profile |
| `GET` | `/api/users/me/stats` | Authenticated | Get user stats (solved breakdown, streak, total submissions) |
| `PUT` | `/api/users/me` | Authenticated | Update user profile details (avatar) |
| `GET` | `/api/users/leaderboard` | Public | Global user leaderboard ranked by points and solved count |

#### `GET /api/users/me/stats`
**Response**: `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 3,
    "username": "john_doe",
    "email": "john@codeforge.dev",
    "role": "ROLE_USER",
    "problemsSolved": 14,
    "easySolved": 8,
    "mediumSolved": 5,
    "hardSolved": 1,
    "totalSubmissions": 29,
    "dayStreak": 4
  }
}
```

---

### 4. Problems & Topics (`/api/problems`, `/api/topics`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/problems` | Public | List all problems with filters (`difficulty`, `topic`, `search`) |
| `GET` | `/api/problems/{id}` | Public | Get problem details and public sample test cases |
| `GET` | `/api/problems/slug/{slug}` | Public | Get problem details by slug |
| `GET` | `/api/topics` | Public | List all problem topic categories |
| `POST` | `/api/problems` | `ADMIN`, `ORGANIZER` | Create a new DSA problem |
| `PUT` | `/api/problems/{id}` | `ADMIN`, `ORGANIZER` | Update existing problem |
| `DELETE` | `/api/problems/{id}` | `ADMIN`, `ORGANIZER` | Delete problem |
| `GET` | `/api/problems/{problemId}/test-cases` | `ADMIN`, `ORGANIZER` | View all test cases (hidden & sample) |
| `POST` | `/api/problems/{problemId}/test-cases` | `ADMIN`, `ORGANIZER` | Add a test case with `isHidden` toggle |
| `DELETE` | `/api/test-cases/{id}` | `ADMIN`, `ORGANIZER` | Delete a test case by ID |

#### `POST /api/problems`
**Request Body**:
```json
{
  "title": "Merge Two Sorted Lists",
  "difficulty": "EASY",
  "description": "Merge two sorted linked lists and return it as a sorted list.",
  "constraints": "The number of nodes in both lists is in the range [0, 50].",
  "inputFormat": "Two lines containing space-separated integers",
  "outputFormat": "Single line of merged space-separated integers",
  "timeLimit": 1000,
  "memoryLimit": 256,
  "starterCode": "public class Solution { ... }",
  "topics": ["Linked List", "Recursion"],
  "testCases": [
    { "input": "1 2 4\n1 3 4", "expectedOutput": "1 1 2 3 4 4", "isHidden": false },
    { "input": "\n0", "expectedOutput": "0", "isHidden": true }
  ]
}
```

---

### 5. Code Execution & Submissions (`/api/submissions`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/submissions/run` | Authenticated | Execute code against sample or custom stdin inputs |
| `POST` | `/api/submissions` | Authenticated | Submit code for grading against all hidden test cases |
| `GET` | `/api/submissions/{id}` | Authenticated | Get detailed submission evaluation report |
| `GET` | `/api/submissions` or `/my` | Authenticated | Get current user's submission history |
| `GET` | `/api/submissions/problem/{id}`| Authenticated | Get current user's submissions for specific problem |

#### `POST /api/submissions/run` (Interactive Test)
**Request Body**:
```json
{
  "problemId": 1,
  "language": "PYTHON",
  "code": "import sys\n...",
  "customInput": "4\n2 7 11 15\n9"
}
```
**Response**: `200 OK`
```json
{
  "success": true,
  "data": {
    "status": "ACCEPTED",
    "input": "4\n2 7 11 15\n9",
    "expectedOutput": "0 1",
    "actualOutput": "0 1",
    "runtime": 172,
    "memory": 38744,
    "errorMessage": null
  }
}
```

#### `POST /api/submissions` (Full Solution Evaluation)
**Request Body**:
```json
{
  "problemId": 1,
  "language": "JAVA",
  "code": "import java.util.*;\npublic class Solution { ... }",
  "contestId": 1
}
```
**Response**: `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 108,
    "problemId": 1,
    "problemTitle": "Two Sum",
    "contestId": 1,
    "contestTitle": "CodeForge Winter Cup 2026",
    "language": "JAVA",
    "status": "ACCEPTED",
    "runtime": 139,
    "memory": 38678,
    "testsPassed": 4,
    "totalTests": 4,
    "errorMessage": null,
    "createdAt": "2026-09-21T20:06:21"
  }
}
```

---

### 6. Contests & Leaderboards (`/api/contests`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/contests` | Public | List all contests (Live, Upcoming, Past) |
| `GET` | `/api/contests/{id}` | Public | Get contest details, problem roster & user enrollment |
| `GET` | `/api/contests/slug/{slug}` | Public | Get contest by slug |
| `POST` | `/api/contests` | `ADMIN`, `ORGANIZER` | Create a new contest |
| `PUT` | `/api/contests/{id}` | `ADMIN`, `ORGANIZER` | Update contest details |
| `DELETE`| `/api/contests/{id}` | `ADMIN`, `ORGANIZER` | Delete a contest |
| `POST` | `/api/contests/{id}/register` | Authenticated | Register current user in contest |
| `GET` | `/api/contests/{id}/leaderboard` | Public | Live ICPC scoreboard (rank, score, penalty) |
| `GET` | `/api/contests/{id}/results` | Public | Final contest standings |
| `GET` | `/api/contests/{id}/analytics` | Public | Contest participation & solve analytics |

#### `GET /api/contests/{id}/leaderboard`
**Response**: `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "rank": 1,
      "username": "sarah_coder",
      "score": 300,
      "penaltyMinutes": 48,
      "problemsSolved": 3,
      "problemResults": {
        "1": { "status": "ACCEPTED", "points": 100, "attempts": 1, "solveTimeMinutes": 12 },
        "2": { "status": "ACCEPTED", "points": 100, "attempts": 1, "solveTimeMinutes": 24 },
        "3": { "status": "ACCEPTED", "points": 100, "attempts": 2, "solveTimeMinutes": 32 }
      }
    }
  ]
}
```

---

### 7. Organizer Studio (`/api/organizer`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/organizer/contests` | `ROLE_ORGANIZER` | List contests owned by the current organizer |
| `POST` | `/api/organizer/contests` | `ROLE_ORGANIZER` | Create contest with organizer ownership |
| `PUT` | `/api/organizer/contests/{id}` | `ROLE_ORGANIZER` | Update contest details |
| `DELETE`| `/api/organizer/contests/{id}`| `ROLE_ORGANIZER` | Delete contest |

---

### 8. Admin Management & Telemetry (`/api/admin`)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/admin/stats` | `ROLE_ADMIN` | Platform KPIs (users, contests, problems, submissions, acceptance rate) |
| `GET` | `/api/admin/users` | `ROLE_ADMIN` | User list with role filter (`?role=ROLE_PARTICIPANT`) |
| `PATCH`| `/api/admin/users/{id}/toggle-status` | `ROLE_ADMIN` | Toggle user active / deactivated |
| `PATCH`| `/api/admin/users/{id}/role` | `ROLE_ADMIN` | Change user role (`ROLE_USER`, `ROLE_ORGANIZER`, `ROLE_ADMIN`) |
| `DELETE`| `/api/admin/users/{id}` | `ROLE_ADMIN` | Delete user and cascade clean dependencies |
| `GET` | `/api/admin/submissions` | `ROLE_ADMIN` | Global submission audit trail |
| `GET` | `/api/admin/logs` | `ROLE_ADMIN` | Real-time JVM memory, MySQL status & execution logs |

#### `GET /api/admin/logs` (Live Telemetry)
**Response**: `200 OK`
```json
{
  "success": true,
  "data": {
    "usedMemoryMb": 184.2,
    "totalMemoryMb": 384.0,
    "maxMemoryMb": 4096.0,
    "availableProcessors": 8,
    "activeThreads": 32,
    "databaseStatus": "ONLINE (MySQL 8.0)",
    "executionSandbox": "ACTIVE (Docker Sandbox)",
    "logs": [
      {
        "timestamp": "2026-09-21T20:10:06",
        "level": "INFO",
        "message": "Submission #2 by john_doe on problem 'Two Sum' -> ACCEPTED (170 ms)"
      }
    ]
  }
}
```

---

## 🔒 Execution Sandbox & Security Architecture

1. **Static AST & Regex Sanitizer (`CodeSanitizer.java`)**:
   - Inspects student source code prior to compile phase.
   - Forbids destructive system calls:
     - `Runtime.getRuntime()`, `ProcessBuilder`, `System.exit()`, socket reflection in Java.
     - `os.system`, `subprocess`, `__import__`, `eval()`, `exec()` in Python.
     - `system()`, `popen()`, `fork()`, `kill()` in C/C++.
2. **Pipe Buffer Deadlock Elimination**:
   - Child process `stdin`, `stdout`, and `stderr` are redirected to dedicated temporary files on disk (`stdin.txt`, `stdout.txt`, `stderr.txt`).
   - Prevents OS pipe saturation on long stdout outputs.
3. **Execution Guardrails**:
   - Strict compile timeout: `10000 ms`.
   - Per-problem runtime watchdog: `1000 ms` to `5000 ms`.
   - Hard memory limit: `256 MB` heap.
   - Test run isolation: Each evaluation creates an ephemeral run directory (`run_<timestamp>_<uuid>`) which is deleted upon completion.

---

## 📦 Error Handling & API Response Format

All API responses follow a unified response envelope:

```json
{
  "success": true,
  "message": "Operation description",
  "data": { ... },
  "timestamp": "2026-09-21T20:15:00"
}
```

When an error occurs (e.g. `400 Bad Request`, `401 Unauthorized`, `404 Not Found`, `500 Internal Error`), the `GlobalExceptionHandler` produces standard problem payloads:

```json
{
  "success": false,
  "message": "Resource not found: Problem with id 999",
  "data": null,
  "timestamp": "2026-09-21T20:15:00"
}
```
