# CodeForge System Architecture

## Overview

**CodeForge** is an enterprise-grade competitive programming and algorithmic evaluation platform engineered for college and university coding clubs, hackathons, and individual practice. It features a layered architecture separating concerns between web presentation, business logic, data persistence, and sandboxed code execution.

---

## 1. High-Level Architecture Diagram

```mermaid
graph TD
    Client["React 18 + Vite (Monaco Editor, Tailwind CSS)"]
    Gateway["Spring Security + JWT Auth Filter + CORS"]
    
    subgraph SpringBootBackend ["Spring Boot 3.x Backend"]
        Controllers["REST Controllers (/api/*)"]
        Services["Business Services (Auth, Problem, Contest, Submission, User)"]
        Judge["Judge Engine (JudgeService, CodeSanitizer)"]
        Repositories["Spring Data JPA Repositories"]
    end
    
    subgraph ExecutionLayer ["Execution Layer"]
        Sandbox["CodeExecutor (Docker Sandbox / Local Process Sandbox)"]
        Compilers["Compilers & Runtimes (javac, g++, gcc, python, node)"]
    end
    
    Database[("MySQL 8.0+ InnoDB Database (codeforge_db)")]

    Client -->|HTTP / JSON with Bearer JWT| Gateway
    Gateway --> Controllers
    Controllers --> Services
    Services --> Repositories
    Repositories --> Database
    Services --> Judge
    Judge --> Sandbox
    Sandbox --> Compilers
```

---

## 2. Layered Backend Design

The backend strictly conforms to the enterprise layered pattern:

$$\text{HTTP Request} \longrightarrow \text{Controller} \longrightarrow \text{Service} \longrightarrow \text{Repository} \longrightarrow \text{Entity} \longrightarrow \text{Database}$$

### 2.1 Controller Layer (`com.codeforge.*.controller`)
- Validates incoming HTTP requests via Jakarta Bean Validation (`@Valid`).
- Extracts authentication principals via `@AuthenticationPrincipal` or `Authentication`.
- Returns standard envelopes (`ApiResponse<T>`) with HTTP status codes.
- Does not contain business logic or transactional state.

### 2.2 Service Layer (`com.codeforge.*.service`)
- Enforces business rules (e.g., verifying contest status before submission, ensuring unique slugs).
- Manages transactional boundaries with `@Transactional`.
- Transforms entity graphs into Data Transfer Objects (`*Dto`).

### 2.3 Repository Layer (`com.codeforge.*.repository`)
- Extends Spring Data JPA `JpaRepository`.
- Implements custom JPQL queries with index-optimized lookups.
- Avoids N+1 query overhead through targeted joins and pagination.

### 2.4 Entity Layer (`com.codeforge.*.entity`)
- JPA entities mapped to MySQL 8 InnoDB tables.
- Strict foreign key constraints and cascade rules.
- Automatic auditing via `@PrePersist` timestamps.

---

## 3. Code Execution & Judge System

```mermaid
sequenceDiagram
    autonumber
    actor User as Participant / Monaco IDE
    participant API as SubmissionController
    participant SubSvc as SubmissionService
    participant Judge as JudgeService
    participant San as CodeSanitizer
    participant Exec as SandboxExecutor
    participant DB as MySQL Database

    User->>API: POST /api/submissions (code, lang, problemId)
    API->>SubSvc: submitCode(request, username)
    SubSvc->>DB: Save Submission (status=RUNNING)
    SubSvc->>Judge: evaluateSubmission(submission, problem, testCases)
    Judge->>San: validate(code, lang)
    alt Security Violation Found
        San-->>Judge: Forbidden token (e.g. Runtime.getRuntime)
        Judge-->>SubSvc: Set status=RUNTIME_ERROR + error details
    else Clean Code
        Judge->>Exec: compile(dir, "Solution", lang, 10000ms)
        alt Compilation Error
            Exec-->>Judge: exitCode != 0, stderr
            Judge-->>SubSvc: Set status=COMPILATION_ERROR
        else Compilation OK
            loop For each Test Case (Hidden + Public)
                Judge->>Exec: execute(dir, "Solution", lang, input, timeLimit, memLimit)
                alt Timeout
                    Judge-->>SubSvc: status=TIME_LIMIT_EXCEEDED
                else Runtime Error
                    Judge-->>SubSvc: status=RUNTIME_ERROR
                else Wrong Output
                    Judge-->>SubSvc: status=WRONG_ANSWER
                end
            end
            Judge-->>SubSvc: All Passed -> status=ACCEPTED
        end
    end
    SubSvc->>DB: Update Submission (verdict, runtime, memory)
    SubSvc-->>API: SubmissionResponse
    API-->>User: JSON Response
```

### Supported Languages & Runtimes:
1. **Java 17+** (`javac`, `java -Xmx<limit>m`)
2. **C++ (C++17)** (`g++ -O3 -std=c++17 Solution.cpp -o Solution.exe`)
3. **C (C11)** (`gcc -O3 Solution.c -o Solution.exe`)
4. **Python 3** (`python Solution.py`)
5. **JavaScript (Node.js)** (`node Solution.js`)

### Security Controls:
- **Sandbox Isolation:** Temporary per-execution directories automatically destroyed in `finally` blocks.
- **Process Boundaries:** OS process limits, timeout termination (`waitFor(timeout, TimeUnit.MILLISECONDS)` followed by `process.destroyForcibly()`).
- **CodeSanitizer Validation:** Static regex inspection blocking unauthorized process invocation, system shutdown, destructive file I/O, and socket binding.
- **Output Truncation & Normalization:** Prevents output bombing; normalizes CRLF and trailing whitespace before verification.

---

## 4. Contest Engine & ICPC Scoring

- **Contest Life Cycle:**
  - `UPCOMING`: Scheduled in the future; registrations open.
  - `LIVE`: Active tournament; problem statements unlocked; live countdown timer.
  - `COMPLETED`: Ended; final scoreboard locked.
- **ICPC Penalty Calculation:**
  $$\text{Penalty} = \text{Elapsed Solve Time (minutes)} + (\text{Wrong Attempts} \times 20\text{ minutes})$$
- **Live Leaderboard:**
  Ranked by:
  1. Primary: Problems solved (descending)
  2. Secondary: Score (descending)
  3. Tertiary: Penalty minutes (ascending)
