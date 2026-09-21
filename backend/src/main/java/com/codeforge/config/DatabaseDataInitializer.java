package com.codeforge.config;

import com.codeforge.contest.entity.Contest;
import com.codeforge.contest.entity.ContestParticipant;
import com.codeforge.contest.entity.ContestProblem;
import com.codeforge.contest.entity.ContestProblemResult;
import com.codeforge.contest.repository.ContestParticipantRepository;
import com.codeforge.contest.repository.ContestProblemRepository;
import com.codeforge.contest.repository.ContestProblemResultRepository;
import com.codeforge.contest.repository.ContestRepository;
import com.codeforge.problem.entity.Difficulty;
import com.codeforge.problem.entity.Problem;
import com.codeforge.problem.repository.ProblemRepository;
import com.codeforge.submission.entity.Submission;
import com.codeforge.submission.entity.SubmissionStatus;
import com.codeforge.submission.repository.SubmissionRepository;
import com.codeforge.testcase.entity.TestCase;
import com.codeforge.testcase.repository.TestCaseRepository;
import com.codeforge.topic.entity.Topic;
import com.codeforge.topic.repository.TopicRepository;
import com.codeforge.user.entity.Role;
import com.codeforge.user.entity.User;
import com.codeforge.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class DatabaseDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final PasswordEncoder passwordEncoder;
    private final ContestRepository contestRepository;
    private final ContestProblemRepository contestProblemRepository;
    private final ContestParticipantRepository contestParticipantRepository;
    private final ContestProblemResultRepository contestProblemResultRepository;
    private final SubmissionRepository submissionRepository;

    public DatabaseDataInitializer(UserRepository userRepository,
                                   TopicRepository topicRepository,
                                   ProblemRepository problemRepository,
                                   TestCaseRepository testCaseRepository,
                                   PasswordEncoder passwordEncoder,
                                   ContestRepository contestRepository,
                                   ContestProblemRepository contestProblemRepository,
                                   ContestParticipantRepository contestParticipantRepository,
                                   ContestProblemResultRepository contestProblemResultRepository,
                                   SubmissionRepository submissionRepository) {
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
        this.passwordEncoder = passwordEncoder;
        this.contestRepository = contestRepository;
        this.contestProblemRepository = contestProblemRepository;
        this.contestParticipantRepository = contestParticipantRepository;
        this.contestProblemResultRepository = contestProblemResultRepository;
        this.submissionRepository = submissionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedTopics();
        seedProblemsAndTestCases();
        seedContestsAndSampleResults();
    }

    private void seedUsers() {
        // Admin
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", "admin@codeforge.com", passwordEncoder.encode("admin123"), Role.ROLE_ADMIN, "CodeForge Tech");
            userRepository.save(admin);
            System.out.println(">> Initialized default admin account: admin / admin123");
        }

        // Contest Organizer
        if (!userRepository.existsByUsername("organizer")) {
            User organizer = new User("organizer", "organizer@codeforge.com", passwordEncoder.encode("organizer123"), Role.ROLE_ORGANIZER, "MIT ACM Chapter");
            userRepository.save(organizer);
            System.out.println(">> Initialized contest organizer account: organizer / organizer123");
        }

        // Participant 1
        if (!userRepository.existsByUsername("john_doe")) {
            User user1 = new User("john_doe", "john@codeforge.com", passwordEncoder.encode("user123"), Role.ROLE_PARTICIPANT, "Stanford CS");
            userRepository.save(user1);
            System.out.println(">> Initialized participant account: john_doe / user123");
        }

        // Participant 2
        if (!userRepository.existsByUsername("sarah_coder")) {
            User user2 = new User("sarah_coder", "sarah@codeforge.com", passwordEncoder.encode("user123"), Role.ROLE_PARTICIPANT, "Carnegie Mellon Univ");
            userRepository.save(user2);
            System.out.println(">> Initialized participant account: sarah_coder / user123");
        }

        // Participant 3
        if (!userRepository.existsByUsername("alex_dev")) {
            User user3 = new User("alex_dev", "alex@codeforge.com", passwordEncoder.encode("user123"), Role.ROLE_PARTICIPANT, "Berkeley EECS");
            userRepository.save(user3);
            System.out.println(">> Initialized participant account: alex_dev / user123");
        }
    }

    private void seedTopics() {
        List<String> defaultTopics = Arrays.asList(
                "Arrays", "Strings", "HashMap", "Linked List", "Stack",
                "Queue", "Trees", "Graphs", "Dynamic Programming",
                "Binary Search", "Two Pointers", "Sorting"
        );

        for (String topicName : defaultTopics) {
            if (!topicRepository.existsByName(topicName)) {
                topicRepository.save(new Topic(topicName));
            }
        }
    }

    private void seedProblemsAndTestCases() {
        if (problemRepository.count() > 0) {
            return;
        }

        System.out.println(">> Seeding educational DSA problems and test cases into database...");

        // Problem 1: Two Sum
        createProblem(
                "Two Sum", "two-sum",
                "Given an array of integers `nums` and an integer `target`, return the 0-based indices of the two numbers such that they add up to `target`.\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice. Print the indices separated by a space in ascending order.",
                Difficulty.EASY,
                "2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9\n-10^9 <= target <= 10^9\nOnly one valid answer exists.",
                "Line 1: An integer n (array size)\nLine 2: n space-separated integers representing the array\nLine 3: An integer target",
                "Print the two indices separated by a space.",
                "import java.util.*;\n\npublic class Solution {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        if (!sc.hasNextInt()) return;\n        int n = sc.nextInt();\n        int[] nums = new int[n];\n        for (int i = 0; i < n; i++) {\n            nums[i] = sc.nextInt();\n        }\n        int target = sc.nextInt();\n\n        // Write your logic here\n        \n    }\n}",
                1000, 256,
                Arrays.asList("Arrays", "HashMap"),
                Arrays.asList(
                        new TestCaseRecord("4\n2 7 11 15\n9", "0 1", false),
                        new TestCaseRecord("3\n3 2 4\n6", "1 2", false),
                        new TestCaseRecord("2\n3 3\n6", "0 1", true),
                        new TestCaseRecord("4\n-1 -2 -3 -4\n-6", "1 3", true)
                )
        );

        // Problem 2: Reverse String
        createProblem(
                "Reverse String", "reverse-string",
                "Write a program that takes a string as input and outputs the reversed string.",
                Difficulty.EASY,
                "1 <= s.length <= 10^5\ns consists of printable ASCII characters.",
                "Line 1: A single string s",
                "Print the reversed string.",
                "import java.util.*;\n\npublic class Solution {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        if (!sc.hasNextLine()) return;\n        String s = sc.nextLine();\n\n        // Write your logic here\n        \n    }\n}",
                1000, 256,
                Arrays.asList("Strings", "Two Pointers"),
                Arrays.asList(
                        new TestCaseRecord("hello", "olleh", false),
                        new TestCaseRecord("CodeForge", "egroFedoC", false),
                        new TestCaseRecord("a", "a", true),
                        new TestCaseRecord("12345 67890", "09876 54321", true)
                )
        );

        // Problem 3: Palindrome Check
        createProblem(
                "Palindrome Check", "palindrome-check",
                "A phrase is a palindrome if, after converting all uppercase letters into lowercase letters and removing all non-alphanumeric characters, it reads the same forward and backward. Alphanumeric characters include letters and numbers.\n\nPrint `true` if it is a palindrome, or `false` otherwise.",
                Difficulty.EASY,
                "1 <= s.length <= 2 * 10^5\ns consists only of printable ASCII characters.",
                "Line 1: A single string s",
                "Print true or false.",
                "import java.util.*;\n\npublic class Solution {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        if (!sc.hasNextLine()) return;\n        String s = sc.nextLine();\n\n        // Write your logic here\n        \n    }\n}",
                1000, 256,
                Arrays.asList("Strings", "Two Pointers"),
                Arrays.asList(
                        new TestCaseRecord("racecar", "true", false),
                        new TestCaseRecord("hello", "false", false),
                        new TestCaseRecord("A man a plan a canal Panama", "true", true),
                        new TestCaseRecord("0P", "false", true)
                )
        );

        // Problem 4: Maximum Element in Array
        createProblem(
                "Maximum Element in Array", "maximum-element-in-array",
                "Given an array of integers, find and print the maximum value present in the array.",
                Difficulty.EASY,
                "1 <= n <= 10^5\n-10^9 <= nums[i] <= 10^9",
                "Line 1: An integer n\nLine 2: n space-separated integers",
                "Print the single maximum integer.",
                "import java.util.*;\n\npublic class Solution {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        if (!sc.hasNextInt()) return;\n        int n = sc.nextInt();\n        int[] nums = new int[n];\n        for (int i = 0; i < n; i++) {\n            nums[i] = sc.nextInt();\n        }\n\n        // Write your logic here\n        \n    }\n}",
                1000, 256,
                Arrays.asList("Arrays"),
                Arrays.asList(
                        new TestCaseRecord("5\n1 8 3 9 2", "9", false),
                        new TestCaseRecord("3\n-5 -2 -10", "-2", false),
                        new TestCaseRecord("1\n42", "42", true),
                        new TestCaseRecord("6\n100 100 50 100 20 90", "100", true)
                )
        );

        // Problem 5: Binary Search
        createProblem(
                "Binary Search", "binary-search",
                "Given an array of integers `nums` which is sorted in ascending order, and an integer `target`, write a function to search `target` in `nums`. If `target` exists, then print its 0-based index. Otherwise, print `-1`. You must write an algorithm with O(log n) runtime complexity.",
                Difficulty.EASY,
                "1 <= nums.length <= 10^4\n-10^4 < nums[i], target < 10^4\nAll the integers in nums are unique.\nnums is sorted in ascending order.",
                "Line 1: An integer n\nLine 2: n space-separated sorted integers\nLine 3: An integer target",
                "Print the index of target or -1.",
                "import java.util.*;\n\npublic class Solution {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        if (!sc.hasNextInt()) return;\n        int n = sc.nextInt();\n        int[] nums = new int[n];\n        for (int i = 0; i < n; i++) {\n            nums[i] = sc.nextInt();\n        }\n        int target = sc.nextInt();\n\n        // Write your logic here\n        \n    }\n}",
                1000, 256,
                Arrays.asList("Arrays", "Binary Search"),
                Arrays.asList(
                        new TestCaseRecord("6\n-1 0 3 5 9 12\n9", "4", false),
                        new TestCaseRecord("6\n-1 0 3 5 9 12\n2", "-1", false),
                        new TestCaseRecord("1\n5\n5", "0", true),
                        new TestCaseRecord("5\n2 4 6 8 10\n10", "4", true)
                )
        );

        // Problem 6: Merge Intervals
        createProblem(
                "Merge Intervals", "merge-intervals",
                "Given an array of intervals where intervals[i] = [start_i, end_i], merge all overlapping intervals, and print the resulting non-overlapping intervals in ascending order by start time.",
                Difficulty.MEDIUM,
                "1 <= intervals.length <= 10^4\n0 <= start_i <= end_i <= 10^4",
                "Line 1: An integer n (number of intervals)\nNext n lines: Two space-separated integers representing start and end",
                "Print each merged interval on a new line as two space-separated integers.",
                "import java.util.*;\n\npublic class Solution {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        if (!sc.hasNextInt()) return;\n        int n = sc.nextInt();\n        int[][] intervals = new int[n][2];\n        for (int i = 0; i < n; i++) {\n            intervals[i][0] = sc.nextInt();\n            intervals[i][1] = sc.nextInt();\n        }\n\n        // Write your logic here\n        \n    }\n}",
                1500, 256,
                Arrays.asList("Arrays", "Sorting"),
                Arrays.asList(
                        new TestCaseRecord("4\n1 3\n2 6\n8 10\n15 18", "1 6\n8 10\n15 18", false),
                        new TestCaseRecord("2\n1 4\n4 5", "1 5", false),
                        new TestCaseRecord("3\n1 2\n3 4\n5 6", "1 2\n3 4\n5 6", true),
                        new TestCaseRecord("2\n1 10\n2 5", "1 10", true)
                )
        );

        System.out.println(">> Successfully initialized DSA problems and test cases.");
    }

    private void seedContestsAndSampleResults() {
        if (contestRepository.count() > 0) {
            return;
        }

        System.out.println(">> Seeding realistic live, completed, and upcoming contests with per-problem solve times...");

        User organizer = userRepository.findByUsername("organizer").orElse(null);
        User john = userRepository.findByUsername("john_doe").orElse(null);
        User sarah = userRepository.findByUsername("sarah_coder").orElse(null);
        User alex = userRepository.findByUsername("alex_dev").orElse(null);

        List<Problem> problems = problemRepository.findAll();
        if (problems.size() < 4) return;

        LocalDateTime now = LocalDateTime.now();

        // 1. LIVE CONTEST: CodeForge Winter Cup 2026
        Contest liveContest = new Contest(
                "CodeForge Winter Cup 2026",
                "winter-cup-2026",
                "Annual inter-collegiate coding contest. Solve algorithmic challenges, beat the clock, and rise to the top of the campus standings.",
                now.minusMinutes(35),
                now.plusMinutes(85),
                120
        );
        liveContest.setCreatedBy(organizer);
        liveContest.setRules("1. Individual participation only.\n2. Submissions evaluated against hidden test cases.\n3. Each wrong submission adds 20 minutes penalty upon problem solve.\n4. Time limit strictly enforced.");
        liveContest.setPenaltyMinutesPerWrong(20);
        liveContest = contestRepository.save(liveContest);

        // Assign problems to live contest
        contestProblemRepository.save(new ContestProblem(liveContest, problems.get(0), 100, 1));
        contestProblemRepository.save(new ContestProblem(liveContest, problems.get(1), 100, 2));
        contestProblemRepository.save(new ContestProblem(liveContest, problems.get(4), 100, 3));
        contestProblemRepository.save(new ContestProblem(liveContest, problems.get(5), 150, 4));

        // Enroll participants into live contest
        if (john != null) {
            ContestParticipant pJohn = new ContestParticipant(liveContest, john);
            pJohn.setScore(100);
            pJohn.setPenaltyMinutes(18);
            pJohn.setProblemsSolved(1);
            pJohn.setTotalSolveTimeSeconds(18 * 60L + 42L);
            pJohn.setLastAcceptedAt(now.minusMinutes(16));
            contestParticipantRepository.save(pJohn);

            // Per-Problem Result for John: Two Sum solved at 18m 42s with 2 attempts
            ContestProblemResult r1 = new ContestProblemResult(liveContest, john, problems.get(0));
            r1.setStatus("ACCEPTED");
            r1.setAttemptsCount(2);
            r1.setWrongAttemptsCount(1);
            r1.setSolveTimeSeconds(18 * 60L + 42L);
            r1.setFirstSubmissionTime(now.minusMinutes(30));
            r1.setAcceptedSubmissionTime(now.minusMinutes(16));
            r1.setScore(100);
            r1.setPenaltyMinutes(38); // 18m solve + 20m penalty
            contestProblemResultRepository.save(r1);
        }

        if (sarah != null) {
            ContestParticipant pSarah = new ContestParticipant(liveContest, sarah);
            pSarah.setScore(200);
            pSarah.setPenaltyMinutes(25);
            pSarah.setProblemsSolved(2);
            pSarah.setTotalSolveTimeSeconds(25 * 60L + 12L);
            pSarah.setLastAcceptedAt(now.minusMinutes(10));
            contestParticipantRepository.save(pSarah);

            // Per-Problem Result for Sarah: Two Sum solved at 10m 12s, Reverse String at 15m 00s
            ContestProblemResult r1 = new ContestProblemResult(liveContest, sarah, problems.get(0));
            r1.setStatus("ACCEPTED");
            r1.setAttemptsCount(1);
            r1.setWrongAttemptsCount(0);
            r1.setSolveTimeSeconds(10 * 60L + 12L);
            r1.setFirstSubmissionTime(now.minusMinutes(25));
            r1.setAcceptedSubmissionTime(now.minusMinutes(25));
            r1.setScore(100);
            r1.setPenaltyMinutes(10);
            contestProblemResultRepository.save(r1);

            ContestProblemResult r2 = new ContestProblemResult(liveContest, sarah, problems.get(1));
            r2.setStatus("ACCEPTED");
            r2.setAttemptsCount(1);
            r2.setWrongAttemptsCount(0);
            r2.setSolveTimeSeconds(15 * 60L);
            r2.setFirstSubmissionTime(now.minusMinutes(10));
            r2.setAcceptedSubmissionTime(now.minusMinutes(10));
            r2.setScore(100);
            r2.setPenaltyMinutes(15);
            contestProblemResultRepository.save(r2);
        }

        // 2. COMPLETED CONTEST: CodeForge Grand Prix - Fall Edition (Demonstrating Section 9 & 10)
        Contest completedContest = new Contest(
                "CodeForge Grand Prix - Fall Edition",
                "fall-grand-prix",
                "Official collegiate championship tournament. Featuring complete results breakdown, solve-time metrics, and winner determination.",
                now.minusDays(2).minusHours(2),
                now.minusDays(2),
                120
        );
        completedContest.setCreatedBy(organizer);
        completedContest.setStatus("COMPLETED");
        completedContest.setRules("1. Three algorithmic challenges.\n2. Strict solve time tracking.\n3. Automatic tie breaking based on earliest final AC timestamp.");
        completedContest = contestRepository.save(completedContest);

        Problem pA = problems.get(0); // Two Sum
        Problem pB = problems.get(2); // Palindrome Check
        Problem pC = problems.get(4); // Binary Search

        contestProblemRepository.save(new ContestProblem(completedContest, pA, 100, 1));
        contestProblemRepository.save(new ContestProblem(completedContest, pB, 100, 2));
        contestProblemRepository.save(new ContestProblem(completedContest, pC, 100, 3));

        LocalDateTime contestStart = completedContest.getStartTime();

        // Sarah - Winner (Rank 1): Solved 3/3 problems
        if (sarah != null) {
            ContestParticipant pSarah = new ContestParticipant(completedContest, sarah);
            pSarah.setProblemsSolved(3);
            pSarah.setScore(300);
            pSarah.setPenaltyMinutes(44);
            pSarah.setTotalSolveTimeSeconds(44 * 60L + 5L);
            pSarah.setLastAcceptedAt(contestStart.plusMinutes(21).plusSeconds(5));
            contestParticipantRepository.save(pSarah);

            // Two Sum: 8m 42s, 2 attempts
            ContestProblemResult rA = new ContestProblemResult(completedContest, sarah, pA);
            rA.setStatus("ACCEPTED");
            rA.setAttemptsCount(2);
            rA.setWrongAttemptsCount(1);
            rA.setSolveTimeSeconds(8 * 60L + 42L);
            rA.setScore(100);
            rA.setPenaltyMinutes(28); // 8m + 20m
            contestProblemResultRepository.save(rA);

            // Palindrome: 14m 18s, 1 attempt
            ContestProblemResult rB = new ContestProblemResult(completedContest, sarah, pB);
            rB.setStatus("ACCEPTED");
            rB.setAttemptsCount(1);
            rB.setWrongAttemptsCount(0);
            rB.setSolveTimeSeconds(14 * 60L + 18L);
            rB.setScore(100);
            rB.setPenaltyMinutes(14);
            contestProblemResultRepository.save(rB);

            // Binary Search: 21m 05s, 1 attempt
            ContestProblemResult rC = new ContestProblemResult(completedContest, sarah, pC);
            rC.setStatus("ACCEPTED");
            rC.setAttemptsCount(1);
            rC.setWrongAttemptsCount(0);
            rC.setSolveTimeSeconds(21 * 60L + 5L);
            rC.setScore(100);
            rC.setPenaltyMinutes(21);
            contestProblemResultRepository.save(rC);
        }

        // John - Rank 2: Solved 2/3 problems
        if (john != null) {
            ContestParticipant pJohn = new ContestParticipant(completedContest, john);
            pJohn.setProblemsSolved(2);
            pJohn.setScore(200);
            pJohn.setPenaltyMinutes(69);
            pJohn.setTotalSolveTimeSeconds(29 * 60L + 50L);
            pJohn.setLastAcceptedAt(contestStart.plusMinutes(18).plusSeconds(42));
            contestParticipantRepository.save(pJohn);

            // Two Sum: 18m 42s, 3 attempts (matching example in prompt Section 9!)
            ContestProblemResult rA = new ContestProblemResult(completedContest, john, pA);
            rA.setStatus("ACCEPTED");
            rA.setAttemptsCount(3);
            rA.setWrongAttemptsCount(2);
            rA.setSolveTimeSeconds(18 * 60L + 42L);
            rA.setScore(100);
            rA.setPenaltyMinutes(58); // 18m + 40m
            contestProblemResultRepository.save(rA);

            // Palindrome: 11m 08s, 1 attempt
            ContestProblemResult rB = new ContestProblemResult(completedContest, john, pB);
            rB.setStatus("ACCEPTED");
            rB.setAttemptsCount(1);
            rB.setWrongAttemptsCount(0);
            rB.setSolveTimeSeconds(11 * 60L + 8L);
            rB.setScore(100);
            rB.setPenaltyMinutes(11);
            contestProblemResultRepository.save(rB);

            // Binary Search: Unsolved, 4 attempts, score 0
            ContestProblemResult rC = new ContestProblemResult(completedContest, john, pC);
            rC.setStatus("WRONG_ANSWER");
            rC.setAttemptsCount(4);
            rC.setWrongAttemptsCount(4);
            rC.setFormattedSolveTime("—");
            rC.setScore(0);
            rC.setPenaltyMinutes(0);
            contestProblemResultRepository.save(rC);
        }

        // Alex - Rank 3: Solved 1/3 problems
        if (alex != null) {
            ContestParticipant pAlex = new ContestParticipant(completedContest, alex);
            pAlex.setProblemsSolved(1);
            pAlex.setScore(100);
            pAlex.setPenaltyMinutes(35);
            pAlex.setTotalSolveTimeSeconds(15 * 60L + 30L);
            pAlex.setLastAcceptedAt(contestStart.plusMinutes(15).plusSeconds(30));
            contestParticipantRepository.save(pAlex);

            // Two Sum: 15m 30s, 2 attempts
            ContestProblemResult rA = new ContestProblemResult(completedContest, alex, pA);
            rA.setStatus("ACCEPTED");
            rA.setAttemptsCount(2);
            rA.setWrongAttemptsCount(1);
            rA.setSolveTimeSeconds(15 * 60L + 30L);
            rA.setScore(100);
            rA.setPenaltyMinutes(35);
            contestProblemResultRepository.save(rA);

            // Palindrome: Unsolved, 2 attempts
            ContestProblemResult rB = new ContestProblemResult(completedContest, alex, pB);
            rB.setStatus("WRONG_ANSWER");
            rB.setAttemptsCount(2);
            rB.setWrongAttemptsCount(2);
            rB.setFormattedSolveTime("—");
            rB.setScore(0);
            contestProblemResultRepository.save(rB);

            // Binary Search: Unsolved, 0 attempts
            ContestProblemResult rC = new ContestProblemResult(completedContest, alex, pC);
            rC.setStatus("UNSOLVED");
            rC.setAttemptsCount(0);
            rC.setFormattedSolveTime("—");
            rC.setScore(0);
            contestProblemResultRepository.save(rC);
        }

        // 3. UPCOMING CONTEST: Inter-College Algorithmic Brawl
        Contest upcomingContest = new Contest(
                "Inter-College Algorithmic Brawl",
                "inter-college-brawl",
                "Flagship collegiate algorithmic programming battle. Problems focus on Graph Algorithms, Binary Search, and Two Pointers.",
                now.plusDays(2).withHour(18).withMinute(0),
                now.plusDays(2).withHour(20).withMinute(0),
                120
        );
        upcomingContest.setCreatedBy(organizer);
        upcomingContest.setStatus("UPCOMING");
        upcomingContest.setRules("1. Individual division collegiate competition.\n2. Standard CodeForge timing and penalty rules apply.\n3. Problems revealed automatically when countdown reaches zero.");
        contestRepository.save(upcomingContest);

        System.out.println(">> Successfully initialized Live, Completed, and Upcoming contests with per-problem solve times.");
    }

    private void createProblem(
            String title, String slug, String desc, Difficulty difficulty,
            String constraints, String inputFmt, String outputFmt, String starterCode,
            int timeLimit, int memoryLimit, List<String> topicNames,
            List<TestCaseRecord> testCases
    ) {
        Problem problem = new Problem();
        problem.setTitle(title);
        problem.setSlug(slug);
        problem.setDescription(desc);
        problem.setDifficulty(difficulty);
        problem.setConstraints(constraints);
        problem.setInputFormat(inputFmt);
        problem.setOutputFormat(outputFmt);
        problem.setStarterCode(starterCode);
        problem.setTimeLimit(timeLimit);
        problem.setMemoryLimit(memoryLimit);

        Set<Topic> topics = new HashSet<>();
        for (String topicName : topicNames) {
            topicRepository.findByName(topicName).ifPresent(topics::add);
        }
        problem.setTopics(topics);

        Problem savedProblem = problemRepository.save(problem);

        for (TestCaseRecord record : testCases) {
            TestCase tc = new TestCase(savedProblem, record.input, record.expectedOutput, record.isHidden);
            testCaseRepository.save(tc);
        }
    }

    private static class TestCaseRecord {
        final String input;
        final String expectedOutput;
        final boolean isHidden;

        TestCaseRecord(String input, String expectedOutput, boolean isHidden) {
            this.input = input;
            this.expectedOutput = expectedOutput;
            this.isHidden = isHidden;
        }
    }
}
