package com.codeforge.submission.controller;

import com.codeforge.common.ApiResponse;
import com.codeforge.submission.dto.RunCodeRequest;
import com.codeforge.submission.dto.RunCodeResponse;
import com.codeforge.submission.dto.SubmissionRequest;
import com.codeforge.submission.dto.SubmissionResponse;
import com.codeforge.submission.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitCode(
            @Valid @RequestBody SubmissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        SubmissionResponse response = submissionService.submitCode(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Code evaluated successfully", response));
    }

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<RunCodeResponse>> runSampleCode(
            @Valid @RequestBody RunCodeRequest request) {

        RunCodeResponse response = submissionService.runSampleCode(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmissionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        SubmissionResponse response = submissionService.getSubmissionById(id, userDetails != null ? userDetails.getUsername() : null);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping({"", "/my"})
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getMySubmissions(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<SubmissionResponse> responses = submissionService.getUserSubmissions(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/problem/{problemId}")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getProblemSubmissions(
            @PathVariable Long problemId,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<SubmissionResponse> responses = submissionService.getProblemSubmissions(problemId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
