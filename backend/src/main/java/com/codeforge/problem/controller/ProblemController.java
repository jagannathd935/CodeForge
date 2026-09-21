package com.codeforge.problem.controller;

import com.codeforge.common.ApiResponse;
import com.codeforge.exception.ResourceNotFoundException;
import com.codeforge.problem.dto.ProblemCreateRequest;
import com.codeforge.problem.dto.ProblemDetailDto;
import com.codeforge.problem.dto.ProblemListDto;
import com.codeforge.problem.entity.Problem;
import com.codeforge.problem.repository.ProblemRepository;
import com.codeforge.problem.service.ProblemService;
import com.codeforge.testcase.dto.TestCaseDto;
import com.codeforge.testcase.entity.TestCase;
import com.codeforge.testcase.repository.TestCaseRepository;
import com.codeforge.topic.entity.Topic;
import com.codeforge.topic.repository.TopicRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProblemController {

    private final ProblemService problemService;
    private final TopicRepository topicRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;

    public ProblemController(ProblemService problemService,
                             TopicRepository topicRepository,
                             ProblemRepository problemRepository,
                             TestCaseRepository testCaseRepository) {
        this.problemService = problemService;
        this.topicRepository = topicRepository;
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
    }

    @GetMapping("/problems")
    public ResponseEntity<ApiResponse<List<ProblemListDto>>> getAllProblems(
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String search,
            Authentication authentication) {

        String currentUsername = authentication != null ? authentication.getName() : null;
        List<ProblemListDto> problems = problemService.getAllProblems(difficulty, topic, search, currentUsername);
        return ResponseEntity.ok(ApiResponse.success(problems));
    }

    @GetMapping("/problems/{id}")
    public ResponseEntity<ApiResponse<ProblemDetailDto>> getProblemById(
            @PathVariable Long id,
            Authentication authentication) {

        String currentUsername = authentication != null ? authentication.getName() : null;
        ProblemDetailDto problem = problemService.getProblemById(id, currentUsername);
        return ResponseEntity.ok(ApiResponse.success(problem));
    }

    @GetMapping("/problems/slug/{slug}")
    public ResponseEntity<ApiResponse<ProblemDetailDto>> getProblemBySlug(
            @PathVariable String slug,
            Authentication authentication) {

        String currentUsername = authentication != null ? authentication.getName() : null;
        ProblemDetailDto problem = problemService.getProblemBySlug(slug, currentUsername);
        return ResponseEntity.ok(ApiResponse.success(problem));
    }

    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<String>>> getAllTopics() {
        List<String> topics = topicRepository.findAll().stream().map(Topic::getName).toList();
        return ResponseEntity.ok(ApiResponse.success(topics));
    }

    @PostMapping("/problems")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    public ResponseEntity<ApiResponse<ProblemDetailDto>> createProblem(@Valid @RequestBody ProblemCreateRequest request) {
        ProblemDetailDto created = problemService.createProblem(request);
        return ResponseEntity.ok(ApiResponse.success("Problem created successfully", created));
    }

    @PutMapping("/problems/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    public ResponseEntity<ApiResponse<ProblemDetailDto>> updateProblem(@PathVariable Long id, @Valid @RequestBody ProblemCreateRequest request) {
        ProblemDetailDto updated = problemService.updateProblem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Problem updated successfully", updated));
    }

    @DeleteMapping("/problems/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    public ResponseEntity<ApiResponse<Void>> deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);
        return ResponseEntity.ok(ApiResponse.success("Problem deleted successfully", null));
    }

    @GetMapping("/problems/{problemId}/test-cases")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    public ResponseEntity<ApiResponse<List<TestCaseDto>>> getProblemTestCases(@PathVariable Long problemId) {
        List<TestCaseDto> cases = testCaseRepository.findByProblemId(problemId).stream()
                .map(tc -> new TestCaseDto(tc.getId(), tc.getInput(), tc.getExpectedOutput(), tc.getIsHidden()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(cases));
    }

    @PostMapping("/problems/{problemId}/test-cases")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    public ResponseEntity<ApiResponse<TestCaseDto>> addTestCase(@PathVariable Long problemId, @RequestBody TestCaseDto dto) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem", "id", problemId));

        TestCase tc = new TestCase(problem, dto.getInput(), dto.getExpectedOutput(), dto.getIsHidden() != null ? dto.getIsHidden() : false);
        TestCase saved = testCaseRepository.save(tc);
        return ResponseEntity.ok(ApiResponse.success("Test case added", new TestCaseDto(saved.getId(), saved.getInput(), saved.getExpectedOutput(), saved.getIsHidden())));
    }

    @DeleteMapping("/test-cases/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_ORGANIZER')")
    public ResponseEntity<ApiResponse<Void>> deleteTestCase(@PathVariable Long id) {
        if (!testCaseRepository.existsById(id)) {
            throw new ResourceNotFoundException("TestCase", "id", id);
        }
        testCaseRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Test case deleted", null));
    }
}
