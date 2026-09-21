-- =====================================================================
-- CodeForge: Database Schema Definition
-- Version: 1.0 (MVP)
-- Target RDBMS: MySQL 8.0+
-- Description: Complete schema for Users, Problems, Topics, Test Cases,
--              and Submissions with strict relational constraints and indexing.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS codeforge_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE codeforge_db;

-- Temporarily disable foreign key constraints for clean re-creation
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS submissions;
DROP TABLE IF EXISTS test_cases;
DROP TABLE IF EXISTS problem_topics;
DROP TABLE IF EXISTS topics;
DROP TABLE IF EXISTS problems;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- Table: users
-- Purpose: Authentication and user profile management
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed password',
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_PARTICIPANT' COMMENT 'ROLE_PARTICIPANT, ROLE_ORGANIZER, ROLE_ADMIN',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: problems
-- Purpose: Coding problems catalogue, descriptions, constraints, and starter code
-- ---------------------------------------------------------------------
CREATE TABLE problems (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    slug VARCHAR(150) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL,
    constraints TEXT,
    input_format TEXT,
    output_format TEXT,
    starter_code TEXT COMMENT 'Initial Java boilerplate loaded into Monaco Editor',
    time_limit INT NOT NULL DEFAULT 1000 COMMENT 'Execution time limit in milliseconds',
    memory_limit INT NOT NULL DEFAULT 256 COMMENT 'Execution memory limit in megabytes',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_problems_difficulty (difficulty),
    INDEX idx_problems_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: topics
-- Purpose: DSA categories / tags (e.g., Arrays, Strings, Dynamic Programming)
-- ---------------------------------------------------------------------
CREATE TABLE topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: problem_topics
-- Purpose: Many-to-Many junction table connecting problems to topics
-- ---------------------------------------------------------------------
CREATE TABLE problem_topics (
    problem_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    PRIMARY KEY (problem_id, topic_id),
    CONSTRAINT fk_pt_problem FOREIGN KEY (problem_id) REFERENCES problems(id) ON DELETE CASCADE,
    CONSTRAINT fk_pt_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE,
    INDEX idx_pt_problem (problem_id),
    INDEX idx_pt_topic (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: test_cases
-- Purpose: Automated judging test cases. Public cases are visible as samples;
--          hidden cases are evaluated strictly during judging.
-- ---------------------------------------------------------------------
CREATE TABLE test_cases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    input TEXT NOT NULL,
    expected_output TEXT NOT NULL,
    is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tc_problem FOREIGN KEY (problem_id) REFERENCES problems(id) ON DELETE CASCADE,
    INDEX idx_tc_problem (problem_id),
    INDEX idx_tc_is_hidden (is_hidden)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: submissions
-- Purpose: Historical record of user submissions and automated judge verdicts
-- ---------------------------------------------------------------------
CREATE TABLE submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    contest_id BIGINT DEFAULT NULL,
    language VARCHAR(30) NOT NULL DEFAULT 'JAVA',
    code MEDIUMTEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT 'ACCEPTED, WRONG_ANSWER, COMPILATION_ERROR, TIME_LIMIT_EXCEEDED, RUNTIME_ERROR',
    runtime INT DEFAULT NULL COMMENT 'Execution time in milliseconds',
    memory INT DEFAULT NULL COMMENT 'Peak memory usage in kilobytes',
    tests_passed INT NOT NULL DEFAULT 0,
    total_tests INT NOT NULL DEFAULT 0,
    error_message TEXT DEFAULT NULL COMMENT 'Compiler stderr or runtime exception stack trace',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sub_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_sub_problem FOREIGN KEY (problem_id) REFERENCES problems(id) ON DELETE CASCADE,
    CONSTRAINT fk_sub_contest FOREIGN KEY (contest_id) REFERENCES contests(id) ON DELETE SET NULL,
    INDEX idx_sub_user (user_id),
    INDEX idx_sub_problem (problem_id),
    INDEX idx_sub_contest (contest_id),
    INDEX idx_sub_status (status),
    INDEX idx_sub_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: contests
-- Purpose: Scheduled, active, and completed competitive coding contests
-- ---------------------------------------------------------------------
CREATE TABLE contests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    slug VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    rules TEXT,
    status VARCHAR(30) DEFAULT NULL COMMENT 'DRAFT, UPCOMING, LIVE, COMPLETED, CANCELLED',
    created_by_user_id BIGINT DEFAULT NULL,
    penalty_per_wrong INT NOT NULL DEFAULT 20,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 120,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_contests_creator FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_contests_start (start_time),
    INDEX idx_contests_slug (slug),
    INDEX idx_contests_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: contest_problems
-- Purpose: Problems assigned to a specific contest with custom score weighting
-- ---------------------------------------------------------------------
CREATE TABLE contest_problems (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contest_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    points INT NOT NULL DEFAULT 100,
    order_index INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_cp_contest FOREIGN KEY (contest_id) REFERENCES contests(id) ON DELETE CASCADE,
    CONSTRAINT fk_cp_problem FOREIGN KEY (problem_id) REFERENCES problems(id) ON DELETE CASCADE,
    UNIQUE KEY uq_contest_problem (contest_id, problem_id),
    INDEX idx_cp_contest (contest_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: contest_participants
-- Purpose: Registration and live leaderboard scores for contest participants
-- ---------------------------------------------------------------------
CREATE TABLE contest_participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contest_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    score INT NOT NULL DEFAULT 0,
    penalty_minutes INT NOT NULL DEFAULT 0,
    problems_solved INT NOT NULL DEFAULT 0,
    total_solve_time_seconds BIGINT NOT NULL DEFAULT 0,
    last_accepted_at DATETIME DEFAULT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_part_contest FOREIGN KEY (contest_id) REFERENCES contests(id) ON DELETE CASCADE,
    CONSTRAINT fk_part_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uq_contest_user (contest_id, user_id),
    INDEX idx_part_contest_score (contest_id, problems_solved DESC, score DESC, penalty_minutes ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- Table: contest_problem_results
-- Purpose: Per-problem solve time tracking, attempts, score, and penalty per participant
-- ---------------------------------------------------------------------
CREATE TABLE contest_problem_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contest_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'UNSOLVED' COMMENT 'ACCEPTED, WRONG_ANSWER, UNSOLVED',
    attempts_count INT NOT NULL DEFAULT 0,
    wrong_attempts_count INT NOT NULL DEFAULT 0,
    first_submission_time DATETIME DEFAULT NULL,
    accepted_submission_time DATETIME DEFAULT NULL,
    solve_time_seconds BIGINT DEFAULT NULL,
    formatted_solve_time VARCHAR(30) NOT NULL DEFAULT '—',
    score INT NOT NULL DEFAULT 0,
    penalty_minutes INT NOT NULL DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cpr_contest FOREIGN KEY (contest_id) REFERENCES contests(id) ON DELETE CASCADE,
    CONSTRAINT fk_cpr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cpr_problem FOREIGN KEY (problem_id) REFERENCES problems(id) ON DELETE CASCADE,
    UNIQUE KEY uq_cpr_contest_user_problem (contest_id, user_id, problem_id),
    INDEX idx_cpr_contest_user (contest_id, user_id),
    INDEX idx_cpr_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
