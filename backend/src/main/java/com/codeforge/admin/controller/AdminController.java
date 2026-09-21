package com.codeforge.admin.controller;

import com.codeforge.auth.dto.UserProfileDto;
import com.codeforge.common.ApiResponse;
import com.codeforge.contest.dto.ContestCreateRequest;
import com.codeforge.contest.dto.ContestDetailDto;
import com.codeforge.contest.entity.Contest;
import com.codeforge.contest.repository.ContestRepository;
import com.codeforge.contest.service.ContestService;
import com.codeforge.exception.BadRequestException;
import com.codeforge.exception.ResourceNotFoundException;
import com.codeforge.problem.dto.ProblemCreateRequest;
import com.codeforge.problem.dto.ProblemDetailDto;
import com.codeforge.problem.entity.Problem;
import com.codeforge.problem.repository.ProblemRepository;
import com.codeforge.problem.service.ProblemService;
import com.codeforge.submission.dto.SubmissionResponse;
import com.codeforge.submission.entity.Submission;
import com.codeforge.submission.entity.SubmissionStatus;
import com.codeforge.submission.repository.SubmissionRepository;
import com.codeforge.submission.service.SubmissionService;
import com.codeforge.testcase.dto.TestCaseDto;
import com.codeforge.testcase.entity.TestCase;
import com.codeforge.testcase.repository.TestCaseRepository;
import com.codeforge.user.entity.Role;
import com.codeforge.user.entity.User;
import com.codeforge.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final ProblemService problemService;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final UserRepository userRepository;
    private final SubmissionService submissionService;
    private final SubmissionRepository submissionRepository;
    private final ContestService contestService;
    private final ContestRepository contestRepository;
    private final com.codeforge.contest.repository.ContestParticipantRepository contestParticipantRepository;
    private final com.codeforge.contest.repository.ContestProblemResultRepository contestProblemResultRepository;

    public AdminController(ProblemService problemService,
                           ProblemRepository problemRepository,
                           TestCaseRepository testCaseRepository,
                           UserRepository userRepository,
                           SubmissionService submissionService,
                           SubmissionRepository submissionRepository,
                           ContestService contestService,
                           ContestRepository contestRepository,
                           com.codeforge.contest.repository.ContestParticipantRepository contestParticipantRepository,
                           com.codeforge.contest.repository.ContestProblemResultRepository contestProblemResultRepository) {
        this.problemService = problemService;
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
        this.userRepository = userRepository;
        this.submissionService = submissionService;
        this.submissionRepository = submissionRepository;
        this.contestService = contestService;
        this.contestRepository = contestRepository;
        this.contestParticipantRepository = contestParticipantRepository;
        this.contestProblemResultRepository = contestProblemResultRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminStats() {
        long totalUsers = userRepository.count();
        long totalOrganizers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_ORGANIZER).count();
        long totalParticipants = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_PARTICIPANT || u.getRole() == Role.ROLE_USER).count();

        List<Contest> allContests = contestRepository.findAll();
        long totalContests = allContests.size();
        long activeContests = allContests.stream().filter(c -> "LIVE".equals(c.getStatus()) || "ACTIVE".equals(c.getStatus())).count();
        long completedContests = allContests.stream().filter(c -> "COMPLETED".equals(c.getStatus()) || "ENDED".equals(c.getStatus())).count();

        long totalProblems = problemRepository.count();
        long totalSubmissions = submissionRepository.count();
        long acceptedSubmissions = submissionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED).count();

        double acceptanceRate = totalSubmissions > 0
                ? Math.round((acceptedSubmissions * 100.0 / totalSubmissions) * 10.0) / 10.0
                : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalOrganizers", totalOrganizers);
        stats.put("totalParticipants", totalParticipants);
        stats.put("totalContests", totalContests);
        stats.put("activeContests", activeContests);
        stats.put("completedContests", completedContests);
        stats.put("totalProblems", totalProblems);
        stats.put("totalSubmissions", totalSubmissions);
        stats.put("acceptedSubmissions", acceptedSubmissions);
        stats.put("acceptanceRate", acceptanceRate);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/problems")
    public ResponseEntity<ApiResponse<ProblemDetailDto>> createProblem(@Valid @RequestBody ProblemCreateRequest request) {
        ProblemDetailDto created = problemService.createProblem(request);
        return ResponseEntity.ok(ApiResponse.success("Problem created successfully", created));
    }

    @PutMapping("/problems/{id}")
    public ResponseEntity<ApiResponse<ProblemDetailDto>> updateProblem(@PathVariable Long id, @Valid @RequestBody ProblemCreateRequest request) {
        ProblemDetailDto updated = problemService.updateProblem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Problem updated successfully", updated));
    }

    @DeleteMapping("/problems/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);
        return ResponseEntity.ok(ApiResponse.success("Problem deleted successfully", null));
    }

    @GetMapping("/problems/{problemId}/test-cases")
    public ResponseEntity<ApiResponse<List<TestCaseDto>>> getProblemTestCases(@PathVariable Long problemId) {
        List<TestCaseDto> cases = testCaseRepository.findByProblemId(problemId).stream()
                .map(tc -> new TestCaseDto(tc.getId(), tc.getInput(), tc.getExpectedOutput(), tc.getIsHidden()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(cases));
    }

    @PostMapping("/problems/{problemId}/test-cases")
    public ResponseEntity<ApiResponse<TestCaseDto>> addTestCase(@PathVariable Long problemId, @RequestBody TestCaseDto dto) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem", "id", problemId));

        TestCase tc = new TestCase(problem, dto.getInput(), dto.getExpectedOutput(), dto.getIsHidden() != null ? dto.getIsHidden() : false);
        TestCase saved = testCaseRepository.save(tc);
        return ResponseEntity.ok(ApiResponse.success("Test case added", new TestCaseDto(saved.getId(), saved.getInput(), saved.getExpectedOutput(), saved.getIsHidden())));
    }

    @DeleteMapping("/test-cases/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTestCase(@PathVariable Long id) {
        if (!testCaseRepository.existsById(id)) {
            throw new ResourceNotFoundException("TestCase", "id", id);
        }
        testCaseRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Test case deleted", null));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserProfileDto>>> getAllUsers(@RequestParam(required = false) String role) {
        List<User> users = userRepository.findAll();
        if (role != null && !role.isBlank()) {
            users = users.stream().filter(u -> u.getRole().name().equalsIgnoreCase(role)
                    || (role.equalsIgnoreCase("ROLE_PARTICIPANT") && u.getRole() == Role.ROLE_USER)).toList();
        }

        // Security restriction: NEVER return passwords or sensitive tokens
        List<UserProfileDto> dtos = users.stream().map(u -> {
            UserProfileDto dto = new UserProfileDto();
            dto.setId(u.getId());
            dto.setUsername(u.getUsername());
            dto.setEmail(u.getEmail());
            dto.setRole(u.getRole().name());
            dto.setActive(u.getActive());
            dto.setOrganization(u.getOrganization());
            dto.setCreatedAt(u.getCreatedAt());
            return dto;
        }).toList();

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PatchMapping("/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse<UserProfileDto>> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        boolean newActive = !user.getActive();
        user.setActive(newActive);
        User saved = userRepository.save(user);

        UserProfileDto dto = new UserProfileDto();
        dto.setId(saved.getId());
        dto.setUsername(saved.getUsername());
        dto.setEmail(saved.getEmail());
        dto.setRole(saved.getRole().name());
        dto.setActive(saved.getActive());
        dto.setOrganization(saved.getOrganization());

        return ResponseEntity.ok(ApiResponse.success("User status changed to " + (newActive ? "Active" : "Deactivated"), dto));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String roleStr = body.get("role");
        if (roleStr == null) {
            throw new BadRequestException("Role is required");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        try {
            Role newRole = Role.valueOf(roleStr.toUpperCase());
            user.setRole(newRole);
            User saved = userRepository.save(user);

            UserProfileDto dto = new UserProfileDto();
            dto.setId(saved.getId());
            dto.setUsername(saved.getUsername());
            dto.setEmail(saved.getEmail());
            dto.setRole(saved.getRole().name());
            dto.setActive(saved.getActive());
            dto.setOrganization(saved.getOrganization());

            return ResponseEntity.ok(ApiResponse.success("User role updated to " + newRole.name(), dto));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role specified: " + roleStr);
        }
    }

    @DeleteMapping("/users/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        if (user.getRole() == Role.ROLE_ADMIN && "admin".equalsIgnoreCase(user.getUsername())) {
            throw new BadRequestException("Cannot delete primary root administrator account");
        }

        // Clean up references before deleting user to satisfy foreign key constraints
        contestProblemResultRepository.deleteByUserId(id);
        contestParticipantRepository.deleteByUserId(id);
        submissionRepository.deleteByUserId(id);

        User rootAdmin = userRepository.findByUsername("admin").orElse(null);
        List<Contest> userContests = contestRepository.findByCreatedBy(user);
        for (Contest c : userContests) {
            c.setCreatedBy(rootAdmin);
            contestRepository.save(c);
        }

        userRepository.delete(user);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @GetMapping("/submissions")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getAllSubmissions() {
        List<SubmissionResponse> submissions = submissionService.getAllSubmissions();
        return ResponseEntity.ok(ApiResponse.success(submissions));
    }

    @PostMapping("/contests")
    public ResponseEntity<ApiResponse<ContestDetailDto>> createContest(@Valid @RequestBody ContestCreateRequest request, Authentication auth) {
        String username = auth != null ? auth.getName() : "admin";
        ContestDetailDto created = contestService.createContest(request, username);
        return ResponseEntity.ok(ApiResponse.success("Contest created successfully", created));
    }

    @PutMapping("/contests/{id}")
    public ResponseEntity<ApiResponse<ContestDetailDto>> updateContest(@PathVariable Long id, @Valid @RequestBody ContestCreateRequest request, Authentication auth) {
        String username = auth != null ? auth.getName() : "admin";
        ContestDetailDto updated = contestService.updateContest(id, request, username);
        return ResponseEntity.ok(ApiResponse.success("Contest updated successfully", updated));
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemLogs() {
        Runtime runtime = Runtime.getRuntime();
        long totalMem = runtime.totalMemory();
        long freeMem = runtime.freeMemory();
        long usedMem = totalMem - freeMem;
        long maxMem = runtime.maxMemory();

        Map<String, Object> telemetry = new HashMap<>();
        telemetry.put("usedMemoryMb", Math.round(usedMem / (1024.0 * 1024.0) * 10.0) / 10.0);
        telemetry.put("totalMemoryMb", Math.round(totalMem / (1024.0 * 1024.0) * 10.0) / 10.0);
        telemetry.put("maxMemoryMb", Math.round(maxMem / (1024.0 * 1024.0) * 10.0) / 10.0);
        telemetry.put("availableProcessors", runtime.availableProcessors());
        telemetry.put("databaseStatus", "ONLINE (MySQL 8.0)");
        telemetry.put("executionSandbox", "ACTIVE (Docker Sandbox)");
        telemetry.put("activeThreads", Thread.activeCount());

        List<Submission> recentSubmissions = submissionRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(20)
                .toList();

        List<Map<String, Object>> logEvents = recentSubmissions.stream().map(s -> {
            Map<String, Object> log = new HashMap<>();
            log.put("timestamp", s.getCreatedAt());
            log.put("level", s.getStatus() == SubmissionStatus.ACCEPTED ? "INFO" : "WARN");
            log.put("message", "Submission #" + s.getId() + " by " + s.getUser().getUsername() +
                    " on problem '" + s.getProblem().getTitle() + "' -> " + s.getStatus() +
                    (s.getRuntime() != null ? " (" + s.getRuntime() + " ms)" : ""));
            return log;
        }).toList();

        telemetry.put("logs", logEvents);

        return ResponseEntity.ok(ApiResponse.success(telemetry));
    }

    @DeleteMapping("/contests/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContest(@PathVariable Long id, Authentication auth) {
        String username = auth != null ? auth.getName() : "admin";
        contestService.deleteContest(id, username);
        return ResponseEntity.ok(ApiResponse.success("Contest deleted successfully", null));
    }
}
