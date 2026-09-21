-- =====================================================================
-- CodeForge: Initial Seed Data
-- Version: 1.0 (MVP)
-- Description: Pre-populates topics, initial users (admin + student),
--              10 educational DSA problems, and associated test cases.
-- =====================================================================

USE codeforge_db;

-- ---------------------------------------------------------------------
-- 1. Seed Users
-- Passwords:
-- admin / admin123  -> $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a
-- john_doe / user123 -> $2a$10$wT8K8U1yHn/vO0z8C31kpeKzZ1vLg61sQoD.pM.3jW0YtXyEwM8m2
-- ---------------------------------------------------------------------
INSERT INTO users (id, username, email, password, role) VALUES
(1, 'admin', 'admin@codeforge.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'ROLE_ADMIN'),
(2, 'john_doe', 'john@codeforge.com', '$2a$10$wT8K8U1yHn/vO0z8C31kpeKzZ1vLg61sQoD.pM.3jW0YtXyEwM8m2', 'ROLE_USER');

-- ---------------------------------------------------------------------
-- 2. Seed Topics
-- ---------------------------------------------------------------------
INSERT INTO topics (id, name) VALUES
(1, 'Arrays'),
(2, 'Strings'),
(3, 'HashMap'),
(4, 'Linked List'),
(5, 'Stack'),
(6, 'Queue'),
(7, 'Trees'),
(8, 'Graphs'),
(9, 'Dynamic Programming'),
(10, 'Binary Search'),
(11, 'Two Pointers'),
(12, 'Sorting');

-- ---------------------------------------------------------------------
-- 3. Seed 10 Educational DSA Problems
-- ---------------------------------------------------------------------

-- Problem 1: Two Sum (EASY)
INSERT INTO problems (id, title, slug, description, difficulty, constraints, input_format, output_format, starter_code, time_limit, memory_limit)
VALUES (
1,
'Two Sum',
'two-sum',
'Given an array of integers `nums` and an integer `target`, return the 0-based indices of the two numbers such that they add up to `target`.\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice. Print the indices separated by a space in ascending order.',
'EASY',
'2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9\n-10^9 <= target <= 10^9\nOnly one valid answer exists.',
'Line 1: An integer n (array size)\nLine 2: n space-separated integers representing the array\nLine 3: An integer target',
'Print the two indices separated by a space.',
'import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextInt()) return;
        int n = sc.nextInt();
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = sc.nextInt();
        }
        int target = sc.nextInt();

        // Write your logic here
        
    }
}',
1000,
256
);

-- Problem 2: Reverse String (EASY)
INSERT INTO problems (id, title, slug, description, difficulty, constraints, input_format, output_format, starter_code, time_limit, memory_limit)
VALUES (
2,
'Reverse String',
'reverse-string',
'Write a program that takes a string as input and outputs the reversed string.',
'EASY',
'1 <= s.length <= 10^5\ns consists of printable ASCII characters.',
'Line 1: A single string s',
'Print the reversed string.',
'import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextLine()) return;
        String s = sc.nextLine();

        // Write your logic here
        
    }
}',
1000,
256
);

-- Problem 3: Palindrome Check (EASY)
INSERT INTO problems (id, title, slug, description, difficulty, constraints, input_format, output_format, starter_code, time_limit, memory_limit)
VALUES (
3,
'Palindrome Check',
'palindrome-check',
'A phrase is a palindrome if, after converting all uppercase letters into lowercase letters and removing all non-alphanumeric characters, it reads the same forward and backward. Alphanumeric characters include letters and numbers.\n\nPrint `true` if it is a palindrome, or `false` otherwise.',
'EASY',
'1 <= s.length <= 2 * 10^5\ns consists only of printable ASCII characters.',
'Line 1: A single string s',
'Print true or false.',
'import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextLine()) return;
        String s = sc.nextLine();

        // Write your logic here
        
    }
}',
1000,
256
);

-- Problem 4: Maximum Element in Array (EASY)
INSERT INTO problems (id, title, slug, description, difficulty, constraints, input_format, output_format, starter_code, time_limit, memory_limit)
VALUES (
4,
'Maximum Element in Array',
'maximum-element-in-array',
'Given an array of integers, find and print the maximum value present in the array.',
'EASY',
'1 <= n <= 10^5\n-10^9 <= nums[i] <= 10^9',
'Line 1: An integer n\nLine 2: n space-separated integers',
'Print the single maximum integer.',
'import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextInt()) return;
        int n = sc.nextInt();
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = sc.nextInt();
        }

        // Write your logic here
        
    }
}',
1000,
256
);

-- Problem 5: Binary Search (EASY)
INSERT INTO problems (id, title, slug, description, difficulty, constraints, input_format, output_format, starter_code, time_limit, memory_limit)
VALUES (
5,
'Binary Search',
'binary-search',
'Given an array of integers `nums` which is sorted in ascending order, and an integer `target`, write a function to search `target` in `nums`. If `target` exists, then print its 0-based index. Otherwise, print `-1`. You must write an algorithm with O(log n) runtime complexity.',
'EASY',
'1 <= nums.length <= 10^4\n-10^4 < nums[i], target < 10^4\nAll the integers in nums are unique.\nnums is sorted in ascending order.',
'Line 1: An integer n\nLine 2: n space-separated sorted integers\nLine 3: An integer target',
'Print the index of target or -1.',
'import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextInt()) return;
        int n = sc.nextInt();
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = sc.nextInt();
        }
        int target = sc.nextInt();

        // Write your logic here
        
    }
}',
1000,
256
);

-- Problem 6: Merge Intervals (MEDIUM)
INSERT INTO problems (id, title, slug, description, difficulty, constraints, input_format, output_format, starter_code, time_limit, memory_limit)
VALUES (
6,
'Merge Intervals',
'merge-intervals',
'Given an array of intervals where intervals[i] = [start_i, end_i], merge all overlapping intervals, and print the resulting non-overlapping intervals in ascending order by start time.',
'MEDIUM',
'1 <= intervals.length <= 10^4\n0 <= start_i <= end_i <= 10^4',
'Line 1: An integer n (number of intervals)\nNext n lines: Two space-separated integers representing start and end',
'Print each merged interval on a new line as two space-separated integers.',
'import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextInt()) return;
        int n = sc.nextInt();
        int[][] intervals = new int[n][2];
        for (int i = 0; i < n; i++) {
            intervals[i][0] = sc.nextInt();
            intervals[i][1] = sc.nextInt();
        }

        // Write your logic here
        
    }
}',
1500,
256
);

-- Problem 7: Longest Substring Without Repeating Characters (MEDIUM)
INSERT INTO problems (id, title, slug, description, difficulty, constraints, input_format, output_format, starter_code, time_limit, memory_limit)
VALUES (
7,
'Longest Substring Without Repeating Characters',
'longest-substring-without-repeating-characters',
'Given a string `s`, find the length of the longest substring without repeating characters.',
'MEDIUM',
'0 <= s.length <= 5 * 10^4\ns consists of English letters, digits, symbols and spaces.',
'Line 1: A single string s',
'Print the integer length of the longest substring without duplicate characters.',
'import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s = sc.hasNextLine() ? sc.nextLine() : "";

        // Write your logic here
        
    }
}',
1000,
256
);

-- Problem 8: Linked List Cycle (MEDIUM)
INSERT INTO problems (id, title, slug, description, difficulty, constraints, input_format, output_format, starter_code, time_limit, memory_limit)
VALUES (
8,
'Linked List Cycle',
'linked-list-cycle',
'Given the head of a linked list represented as an array of values and an integer `pos` representing the 0-based index of the node that the tail connects to (-1 if no cycle), determine if the linked list contains a cycle. Print `true` if there is a cycle, else `false`.',
'MEDIUM',
'0 <= number of nodes <= 10^4\n-10^5 <= Node.val <= 10^5\npos is -1 or a valid index in the linked list.',
'Line 1: An integer n (number of nodes)\nLine 2: n space-separated values\nLine 3: An integer pos (-1 for no cycle)',
'Print true if cycle exists, otherwise false.',
'import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextInt()) {
            System.out.println("false");
            return;
        }
        int n = sc.nextInt();
        int[] vals = new int[n];
        for (int i = 0; i < n; i++) {
            vals[i] = sc.nextInt();
        }
        int pos = sc.nextInt();

        // Print true if pos != -1 and n > 0, otherwise false
        if (pos >= 0 && pos < n) {
            System.out.println("true");
        } else {
            System.out.println("false");
        }
    }
}',
1000,
256
);

-- Problem 9: Binary Tree Level Order Traversal (MEDIUM)
INSERT INTO problems (id, title, slug, description, difficulty, constraints, input_format, output_format, starter_code, time_limit, memory_limit)
VALUES (
9,
'Binary Tree Level Order Traversal',
'binary-tree-level-order-traversal',
'Given the root of a binary tree represented as an array in breadth-first order (using "null" for missing nodes), print the level-order traversal of its nodes values level by level, separated by newlines.',
'MEDIUM',
'0 <= number of nodes <= 2000\n-1000 <= Node.val <= 1000',
'Line 1: Space-separated node values in breadth-first sequence (or "null")',
'Print each level values on a separate line, space-separated.',
'import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextLine()) return;
        String line = sc.nextLine().trim();
        if (line.isEmpty() || line.equals("null")) return;

        // Write your logic here
        
    }
}',
1000,
256
);

-- Problem 10: Longest Increasing Subsequence (HARD)
INSERT INTO problems (id, title, slug, description, difficulty, constraints, input_format, output_format, starter_code, time_limit, memory_limit)
VALUES (
10,
'Longest Increasing Subsequence',
'longest-increasing-subsequence',
'Given an integer array `nums`, return the length of the longest strictly increasing subsequence.\n\nA subsequence is a sequence that can be derived from an array by deleting some or no elements without changing the order of the remaining elements.',
'HARD',
'1 <= nums.length <= 2500\n-10^4 <= nums[i] <= 10^4\nAim for O(n log n) time complexity.',
'Line 1: An integer n\nLine 2: n space-separated integers',
'Print a single integer: the length of the longest strictly increasing subsequence.',
'import java.util.*;

public class Solution {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextInt()) return;
        int n = sc.nextInt();
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = sc.nextInt();
        }

        // Write your logic here
        
    }
}',
2000,
256
);

-- ---------------------------------------------------------------------
-- 4. Link Problems to Topics (problem_topics)
-- ---------------------------------------------------------------------
-- Problem 1: Two Sum -> Arrays (1), HashMap (3)
INSERT INTO problem_topics (problem_id, topic_id) VALUES (1, 1), (1, 3);

-- Problem 2: Reverse String -> Strings (2), Two Pointers (11)
INSERT INTO problem_topics (problem_id, topic_id) VALUES (2, 2), (2, 11);

-- Problem 3: Palindrome Check -> Strings (2), Two Pointers (11)
INSERT INTO problem_topics (problem_id, topic_id) VALUES (3, 2), (3, 11);

-- Problem 4: Maximum Element -> Arrays (1)
INSERT INTO problem_topics (problem_id, topic_id) VALUES (4, 1);

-- Problem 5: Binary Search -> Arrays (1), Binary Search (10)
INSERT INTO problem_topics (problem_id, topic_id) VALUES (5, 1), (5, 10);

-- Problem 6: Merge Intervals -> Arrays (1), Sorting (12)
INSERT INTO problem_topics (problem_id, topic_id) VALUES (6, 1), (6, 12);

-- Problem 7: Longest Substring -> Strings (2), HashMap (3)
INSERT INTO problem_topics (problem_id, topic_id) VALUES (7, 2), (7, 3);

-- Problem 8: Linked List Cycle -> Linked List (4), Two Pointers (11)
INSERT INTO problem_topics (problem_id, topic_id) VALUES (8, 4), (8, 11);

-- Problem 9: Binary Tree Traversal -> Trees (7), Queue (6)
INSERT INTO problem_topics (problem_id, topic_id) VALUES (9, 7), (9, 6);

-- Problem 10: Longest Increasing Subsequence -> Dynamic Programming (9), Binary Search (10)
INSERT INTO problem_topics (problem_id, topic_id) VALUES (10, 9), (10, 10);

-- ---------------------------------------------------------------------
-- 5. Seed Test Cases (Public Samples & Hidden Judge Cases)
-- ---------------------------------------------------------------------

-- Problem 1: Two Sum
-- Public 1
INSERT INTO test_cases (problem_id, input, expected_output, is_hidden) VALUES
(1, '4\n2 7 11 15\n9', '0 1', FALSE),
-- Public 2
(1, '3\n3 2 4\n6', '1 2', FALSE),
-- Hidden 1
(1, '2\n3 3\n6', '0 1', TRUE),
-- Hidden 2 (Negative values)
(1, '4\n-1 -2 -3 -4\n-6', '1 3', TRUE);

-- Problem 2: Reverse String
-- Public 1
INSERT INTO test_cases (problem_id, input, expected_output, is_hidden) VALUES
(2, 'hello', 'olleh', FALSE),
-- Public 2
(2, 'CodeForge', 'egroFedoC', FALSE),
-- Hidden 1
(2, 'a', 'a', TRUE),
-- Hidden 2
(2, '12345 67890', '09876 54321', TRUE);

-- Problem 3: Palindrome Check
-- Public 1
INSERT INTO test_cases (problem_id, input, expected_output, is_hidden) VALUES
(3, 'racecar', 'true', FALSE),
-- Public 2
(3, 'hello', 'false', FALSE),
-- Hidden 1 (Ignore non-alphanumeric & case)
(3, 'A man a plan a canal Panama', 'true', TRUE),
-- Hidden 2
(3, '0P', 'false', TRUE);

-- Problem 4: Maximum Element in Array
-- Public 1
INSERT INTO test_cases (problem_id, input, expected_output, is_hidden) VALUES
(4, '5\n1 8 3 9 2', '9', FALSE),
-- Public 2
(4, '3\n-5 -2 -10', '-2', FALSE),
-- Hidden 1
(4, '1\n42', '42', TRUE),
-- Hidden 2
(4, '6\n100 100 50 100 20 90', '100', TRUE);

-- Problem 5: Binary Search
-- Public 1
INSERT INTO test_cases (problem_id, input, expected_output, is_hidden) VALUES
(5, '6\n-1 0 3 5 9 12\n9', '4', FALSE),
-- Public 2
(5, '6\n-1 0 3 5 9 12\n2', '-1', FALSE),
-- Hidden 1
(5, '1\n5\n5', '0', TRUE),
-- Hidden 2
(5, '5\n2 4 6 8 10\n10', '4', TRUE);

-- Problem 6: Merge Intervals
-- Public 1
INSERT INTO test_cases (problem_id, input, expected_output, is_hidden) VALUES
(6, '4\n1 3\n2 6\n8 10\n15 18', '1 6\n8 10\n15 18', FALSE),
-- Public 2
(6, '2\n1 4\n4 5', '1 5', FALSE),
-- Hidden 1
(6, '3\n1 2\n3 4\n5 6', '1 2\n3 4\n5 6', TRUE),
-- Hidden 2
(6, '2\n1 10\n2 5', '1 10', TRUE);

-- Problem 7: Longest Substring Without Repeating Characters
-- Public 1
INSERT INTO test_cases (problem_id, input, expected_output, is_hidden) VALUES
(7, 'abcabcbb', '3', FALSE),
-- Public 2
(7, 'bbbbb', '1', FALSE),
-- Hidden 1
(7, 'pwwkew', '3', TRUE),
-- Hidden 2
(7, 'abcdef', '6', TRUE);

-- Problem 8: Linked List Cycle
-- Public 1
INSERT INTO test_cases (problem_id, input, expected_output, is_hidden) VALUES
(8, '4\n3 2 0 -4\n1', 'true', FALSE),
-- Public 2
(8, '2\n1 2\n0', 'true', FALSE),
-- Hidden 1
(8, '1\n1\n-1', 'false', TRUE);

-- Problem 9: Binary Tree Level Order Traversal
-- Public 1
INSERT INTO test_cases (problem_id, input, expected_output, is_hidden) VALUES
(9, '3 9 20 null null 15 7', '3\n9 20\n15 7', FALSE),
-- Public 2
(9, '1', '1', FALSE),
-- Hidden 1
(9, '1 2 3 4 5 null null', '1\n2 3\n4 5', TRUE);

-- Problem 10: Longest Increasing Subsequence
-- Public 1
INSERT INTO test_cases (problem_id, input, expected_output, is_hidden) VALUES
(10, '8\n10 9 2 5 3 7 101 18', '4', FALSE),
-- Public 2
(10, '6\n0 1 0 3 2 3', '4', FALSE),
-- Hidden 1
(10, '7\n7 7 7 7 7 7 7', '1', TRUE),
-- Hidden 2
(10, '5\n5 4 3 2 1', '1', TRUE);

-- ---------------------------------------------------------------------
-- 6. Seed Contests
-- ---------------------------------------------------------------------
INSERT INTO contests (id, title, slug, description, start_time, end_time, duration_minutes) VALUES
(1, 'CodeForge Spring Cup 2026', 'codeforge-spring-cup-2026', 'The premier college algorithmic contest of the semester! Solve 4 DSA problems ranging from basic array manipulation to dynamic programming.', NOW() - INTERVAL 1 HOUR, NOW() + INTERVAL 23 HOUR, 120),
(2, 'Weekly College Algorithm Sprint #1', 'weekly-college-algorithm-sprint-1', 'A quick, competitive sprint designed to sharpen problem-solving speed under timed constraints.', NOW() + INTERVAL 2 DAY, NOW() + INTERVAL 2 DAY + INTERVAL 2 HOUR, 90),
(3, 'Freshers Coding Challenge 2026', 'freshers-coding-challenge-2026', 'Introductory contest tailored for first- and second-year engineering students exploring DSA.', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 2 DAY, 120);

-- ---------------------------------------------------------------------
-- 7. Seed Contest Problems
-- ---------------------------------------------------------------------
-- Spring Cup Problems: Two Sum (100 pts), Reverse String (100 pts), Merge Intervals (200 pts), Longest Increasing Subsequence (300 pts)
INSERT INTO contest_problems (contest_id, problem_id, points, order_index) VALUES
(1, 1, 100, 1),
(1, 2, 100, 2),
(1, 6, 200, 3),
(1, 10, 300, 4),

-- Weekly Sprint: Palindrome Check (100 pts), Maximum Element (100 pts), Binary Search (150 pts)
(2, 3, 100, 1),
(2, 4, 100, 2),
(2, 5, 150, 3),

-- Freshers Challenge: Two Sum (100 pts), Palindrome Check (100 pts)
(3, 1, 100, 1),
(3, 3, 100, 2);

-- ---------------------------------------------------------------------
-- 8. Seed Contest Participants
-- ---------------------------------------------------------------------
INSERT INTO contest_participants (contest_id, user_id, score, penalty_minutes) VALUES
(1, 1, 400, 45),
(1, 2, 200, 18),
(3, 2, 200, 30);
