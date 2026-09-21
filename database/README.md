# CodeForge Database Setup Guide

This folder contains the complete relational schema and initial seed data for **CodeForge**.

## Files in this Directory

| File | Purpose |
| :--- | :--- |
| `schema.sql` | Creates the `codeforge_db` database, 6 tables, relational foreign keys, and indexes. |
| `seed_data.sql` | Inserts default users (`admin`, `john_doe`), 12 topics, 10 educational DSA problems, and public/hidden test cases. |
| `setup_database.sql` | Master script that runs both `schema.sql` and `seed_data.sql`. |

---

## How to Run & Import into MySQL

### Option 1: Using Windows PowerShell / Terminal (Recommended)

1. Open PowerShell and navigate to the database directory:
   ```powershell
   cd "C:\Users\Jagannath Dalei\OneDrive\Desktop\CodeForge\database"
   ```

2. Run the MySQL command (enter your MySQL root password when prompted):
   ```powershell
   & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < setup_database.sql
   ```
   *(Or if `mysql` is already on your system PATH: `mysql -u root -p < setup_database.sql`)*

---

### Option 2: Using MySQL Workbench

1. Launch **MySQL Workbench**.
2. Connect to your local MySQL instance (`localhost:3306`).
3. Click **File -> Open SQL Script...**
4. Select `schema.sql` and click the **Lightning Bolt (Execute)** button.
5. Next, open `seed_data.sql` and click the **Lightning Bolt (Execute)** button.
6. Refresh the **Schemas** navigator on the left to inspect `codeforge_db`.

---

## Verification Queries

After executing the script, verify your setup using these SQL queries:

```sql
USE codeforge_db;

-- 1. Check all tables
SHOW TABLES;
-- Expected: 6 tables (users, problems, topics, problem_topics, test_cases, submissions)

-- 2. Verify seeded users
SELECT id, username, email, role, created_at FROM users;
-- Expected: 2 users (admin, john_doe)

-- 3. Verify problems & difficulties
SELECT id, title, difficulty, time_limit, memory_limit FROM problems;
-- Expected: 10 rows (5 EASY, 4 MEDIUM, 1 HARD)

-- 4. Verify test cases count per problem
SELECT problem_id, 
       SUM(CASE WHEN is_hidden = 0 THEN 1 ELSE 0 END) AS public_cases,
       SUM(CASE WHEN is_hidden = 1 THEN 1 ELSE 0 END) AS hidden_cases,
       COUNT(*) AS total_cases
FROM test_cases
GROUP BY problem_id;
-- Expected: 10 rows with 2 public cases and 2-3 hidden cases each
```

---

## Default Accounts

| Username | Password | Role |
| :--- | :--- | :--- |
| `admin` | `admin123` | `ROLE_ADMIN` |
| `john_doe` | `user123` | `ROLE_USER` |
