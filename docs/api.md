# CodeForge REST API Specification

Base URL: `http://localhost:8080/api`

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
    "password": "password123"
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
      "role": "ROLE_USER"
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

---

## 2. Problem Endpoints

### Get All Problems
- **Method**: `GET`
- **URL**: `/api/problems`
- **Access**: Public (Authenticated users get `isSolved` flag populated)
- **Query Params**:
  - `difficulty`: `EASY` | `MEDIUM` | `HARD`
  - `topic`: `Arrays` | `Strings` | etc.
  - `search`: search term string
- **Response**: `200 OK`

### Get Problem by ID
- **Method**: `GET`
- **URL**: `/api/problems/{id}`
- **Access**: Public
- **Security Note**: Strictly exposes public sample test cases (`is_hidden = false`). Hidden test cases are never returned.

---

## 3. Submission & Execution Endpoints

### Submit Code for Evaluation
- **Method**: `POST`
- **URL**: `/api/submissions`
- **Access**: Authenticated (`Authorization: Bearer <token>`)
- **Request Body**:
  ```json
  {
    "problemId": 1,
    "language": "JAVA",
    "code": "import java.util.*;\npublic class Solution { ... }"
  }
  ```
- **Response**: `200 OK`
  ```json
  {
    "success": true,
    "message": "Code evaluated successfully",
    "data": {
      "id": 42,
      "problemId": 1,
      "problemTitle": "Two Sum",
      "language": "JAVA",
      "status": "ACCEPTED",
      "runtime": 14,
      "memory": 38400,
      "testsPassed": 4,
      "totalTests": 4,
      "errorMessage": null
    }
  }
  ```

### Run Code (Sample Case)
- **Method**: `POST`
- **URL**: `/api/submissions/run`
- **Access**: Public / Authenticated
- **Request Body**:
  ```json
  {
    "problemId": 1,
    "code": "import java.util.*; ...",
    "customInput": "4\n2 7 11 15\n9"
  }
  ```

---

## 4. User Profile Endpoints

### Get Current User Profile & Statistics
- **Method**: `GET`
- **URL**: `/api/users/me/stats`
- **Access**: Authenticated (`Authorization: Bearer <token>`)
- **Response**: `200 OK`

---

## 5. Admin Endpoints

### Create Problem
- **Method**: `POST`
- **URL**: `/api/admin/problems`
- **Access**: `ROLE_ADMIN` only
- **Request Body**:
  ```json
  {
    "title": "Valid Parentheses",
    "difficulty": "EASY",
    "description": "Given a string s containing just characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.",
    "topics": ["Stack", "Strings"],
    "timeLimit": 1000,
    "memoryLimit": 256,
    "testCases": [
      { "input": "()", "expectedOutput": "true", "isHidden": false },
      { "input": "([)]", "expectedOutput": "false", "isHidden": true }
    ]
  }
  ```

### Delete Problem
- **Method**: `DELETE`
- **URL**: `/api/admin/problems/{id}`
- **Access**: `ROLE_ADMIN` only
