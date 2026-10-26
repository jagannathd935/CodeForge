# CodeForge REST API Specification

Base URL: `http://localhost:8080/api`

All responses follow the unified response format:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

Error responses:
```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2026-10-07T22:30:00"
}
```

---

## 1. Authentication Endpoints

### Register User
- **Method**: `POST`
- **URL**: `/api/auth/register`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "confirmPassword": "password123",
    "role": "ROLE_PARTICIPANT",
    "organization": "MIT"
  }
  ```
- **Response**: `200 OK`
  ```json
  {
    "success": true,
    "message": "User registered successfully",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "tokenType": "Bearer",
      "id": 2,
      "username": "john_doe",
      "email": "john@example.com",
      "role": "ROLE_PARTICIPANT",
      "organization": "MIT"
    }
  }
  ```

### Login User
- **Method**: `POST`
- **URL**: `/api/auth/login`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "usernameOrEmail": "admin",
    "password": "admin123"
  }
  ```
- **Response**: `200 OK`

### Logout User
- **Method**: `POST`
- **URL**: `/api/auth/logout`
- **Access**: Authenticated (`Authorization: Bearer <token>`)
- **Response**: `200 OK`
  ```json
  {
    "success": true,
    "message": "User successfully logged out",
    "data": null
  }
  ```

### Get Authenticated User Token Info
- **Method**: `GET`
- **URL**: `/api/auth/me`
- **Access**: Authenticated (`Authorization: Bearer <token>`)
- **Response**: `200 OK`
  ```json
  {
    "success": true,
    "message": "Authentication verified",
    "data": {
      "id": 1,
      "username": "admin",
      "email": "admin@codeforge.com",
      "role": "ROLE_ADMIN",
      "organization": "CodeForge Tech"
    }
  }
  ```

---

## 2. Problem Endpoints

### Get All Problems
- **Method**: `GET`
- **URL**: `/api/problems`
- **Access**: Public (Authenticated users receive dynamic `isSolved` flag)
- **Query Params**:
  - `page`: default `0`
  - `size`: default `20`
  - `difficulty`: `EASY` | `MEDIUM` | `HARD`
  - `topic`: e.g. `Arrays`, `Strings`, `Dynamic Programming`
  - `search`: search query keyword
- **Response**: `200 OK`

### Search Problems
- **Method**: `GET`
- **URL**: `/api/problems/search`
- **Access**: Public
- **Query Params**:
  - `query` / `keyword`: Search title and description
- **Response**: `200 OK`

### Get Problem by ID
- **Method**: `GET`
- **URL**: `/api/problems/{id}`
- **Access**: Public
- **Security Constraint**: Hidden test cases (`is_hidden = TRUE`) are strictly omitted. Only public sample test cases are returned.
- **Response**: `200 OK`

### Get Problem Submission History
- **Method**: `GET`
- **URL**: `/api/problems/{id}/submissions`
- **Access**: Authenticated (`Authorization: Bearer <token>`)
- **Response**: `200 OK`

---

## 3. Submission & Execution Endpoints

### Run Code (Test Sandbox)
- **Method**: `POST`
- **URL**: `/api/submissions/run`
- **Access**: Public / Authenticated
- **Request Body**:
  ```json
  {
    "problemId": 1,
    "language": "JAVA",
    "code": "import java.util.*; ...",
    "customInput": "4\n2 7 11 15\n9"
  }
  ```
- **Response**: `200 OK`
  ```json
  {
    "success": true,
    "data": {
      "status": "ACCEPTED",
      "input": "4\n2 7 11 15\n9",
      "expectedOutput": "0 1",
      "actualOutput": "0 1",
      "runtime": 22,
      "memory": 32100,
      "errorMessage": null
    }
  }
  ```

### Submit Solution for Judging
- **Method**: `POST`
- **URL**: `/api/submissions`
- **Access**: Authenticated (`Authorization: Bearer <token>`)
- **Request Body**:
  ```json
  {
    "problemId": 1,
    "language": "PYTHON",
    "code": "print('0 1')",
    "contestId": null
  }
  ```
- **Response**: `200 OK`
  ```json
  {
    "success": true,
    "message": "Code evaluated successfully",
    "data": {
      "id": 105,
      "problemId": 1,
      "problemTitle": "Two Sum",
      "language": "PYTHON",
      "status": "ACCEPTED",
      "runtime": 35,
      "memory": 18200,
      "testsPassed": 4,
      "totalTests": 4,
      "errorMessage": null
    }
  }
  ```

### Get My Submissions
- **Method**: `GET`
- **URL**: `/api/submissions/my`
- **Access**: Authenticated (`Authorization: Bearer <token>`)
- **Response**: `200 OK`

### Get Submission by ID
- **Method**: `GET`
- **URL**: `/api/submissions/{id}`
- **Access**: Authenticated (Owner or Admin)
- **Response**: `200 OK`

---

## 4. User Profile & Statistics Endpoints

### Get Current User Profile
- **Method**: `GET`
- **URL**: `/api/users/me`
- **Access**: Authenticated
- **Response**: `200 OK`

### Get User Coding Statistics
- **Method**: `GET`
- **URL**: `/api/users/me/stats`
- **Access**: Authenticated
- **Response**: `200 OK`
  ```json
  {
    "success": true,
    "data": {
      "id": 3,
      "username": "john_doe",
      "email": "john@codeforge.com",
      "role": "ROLE_PARTICIPANT",
      "organization": "CodeForge Tech",
      "streak": 3,
      "solvedCount": 8,
      "totalSubmissions": 14,
      "acceptedSubmissions": 10,
      "acceptanceRate": 71.4,
      "easySolved": 5,
      "mediumSolved": 2,
      "hardSolved": 1,
      "topicStats": [
        {
          "topic": "Dynamic Programming",
          "totalSubmissions": 6,
          "acceptedSubmissions": 4,
          "uniqueSolved": 2,
          "accuracy": 66.7
        }
      ],
      "weakTopics": ["Binary Search"],
      "recommendedProblems": [
        {
          "id": 4,
          "title": "Search in Rotated Sorted Array",
          "slug": "search-in-rotated-sorted-array",
          "difficulty": "MEDIUM",
          "topics": ["Binary Search", "Arrays"],
          "acceptanceRate": 39.2,
          "isSolved": false
        }
      ],
      "dailyActivity": {
        "2026-10-24": 2,
        "2026-10-25": 4,
        "2026-10-26": 2
      }
    }
  }
  ```

---

## 5. Admin Endpoints (`ROLE_ADMIN` Required)

### Create Problem
- **Method**: `POST`
- **URL**: `/api/admin/problems`
- **Request Body**:
  ```json
  {
    "title": "Climbing Stairs",
    "difficulty": "EASY",
    "description": "You are climbing a staircase...",
    "constraints": "1 <= n <= 45",
    "inputFormat": "Single integer n",
    "outputFormat": "Single integer representing distinct ways",
    "starterCode": "import java.util.*; ...",
    "timeLimit": 1000,
    "memoryLimit": 256,
    "topics": ["Dynamic Programming"],
    "testCases": [
      { "input": "2", "expectedOutput": "2", "isHidden": false },
      { "input": "3", "expectedOutput": "3", "isHidden": true }
    ]
  }
  ```

### Update Problem
- **Method**: `PUT`
- **URL**: `/api/admin/problems/{id}`

### Delete Problem
- **Method**: `DELETE`
- **URL**: `/api/admin/problems/{id}`

### Add Problem Test Cases
- **Method**: `POST`
- **URL**: `/api/admin/problems/{id}/testcases`

### Update Test Case
- **Method**: `PUT`
- **URL**: `/api/admin/testcases/{id}`

### Delete Test Case
- **Method**: `DELETE`
- **URL**: `/api/admin/testcases/{id}`

### List All Users
- **Method**: `GET`
- **URL**: `/api/admin/users`

### Update User Role / Status
- **Method**: `PUT`
- **URL**: `/api/admin/users/{id}/role`
- **Request Body**: `{ "role": "ROLE_ORGANIZER", "active": true }`

### View All Submissions
- **Method**: `GET`
- **URL**: `/api/admin/submissions`

### View System Status & Health Metrics
- **Method**: `GET`
- **URL**: `/api/admin/system-status`
- **Response**: `200 OK`
  ```json
  {
    "success": true,
    "data": {
      "backendStatus": "UP",
      "databaseStatus": "CONNECTED",
      "sandboxStatus": "OPERATIONAL",
      "jvmMemory": {
        "maxMemoryMB": 4096,
        "totalMemoryMB": 512,
        "freeMemoryMB": 320
      },
      "totalUsers": 3,
      "totalProblems": 10,
      "totalSubmissions": 18
    }
  }
  ```

---

## 6. Contest Endpoints

### List Contests
- **Method**: `GET`
- **URL**: `/api/contests`
- **Access**: Public

### Get Contest Details
- **Method**: `GET`
- **URL**: `/api/contests/{id}`
- **Access**: Public

### Create Contest
- **Method**: `POST`
- **URL**: `/api/contests`
- **Access**: `ROLE_ORGANIZER`, `ROLE_ADMIN`

### Update Contest
- **Method**: `PUT`
- **URL**: `/api/contests/{id}`
- **Access**: `ROLE_ORGANIZER`, `ROLE_ADMIN`

### Delete Contest
- **Method**: `DELETE`
- **URL**: `/api/contests/{id}`
- **Access**: `ROLE_ORGANIZER`, `ROLE_ADMIN`

### Register for Contest
- **Method**: `POST`
- **URL**: `/api/contests/{id}/register`
- **Access**: Authenticated

### Get Contest Live Leaderboard
- **Method**: `GET`
- **URL**: `/api/contests/{id}/leaderboard`
- **Access**: Public
- **Response**: `200 OK` (Rank, username, problems solved, score, ICPC penalty, per-problem solve times)
