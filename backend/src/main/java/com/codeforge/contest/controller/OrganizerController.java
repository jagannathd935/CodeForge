package com.codeforge.contest.controller;

import com.codeforge.common.ApiResponse;
import com.codeforge.contest.dto.ContestAnalyticsDto;
import com.codeforge.contest.dto.ContestCreateRequest;
import com.codeforge.contest.dto.ContestDetailDto;
import com.codeforge.contest.dto.ContestDto;
import com.codeforge.contest.service.ContestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/organizer")
@PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
public class OrganizerController {

    private final ContestService contestService;

    public OrganizerController(ContestService contestService) {
        this.contestService = contestService;
    }

    @GetMapping("/contests")
    public ResponseEntity<ApiResponse<List<ContestDto>>> getMyContests(Authentication authentication) {
        List<ContestDto> contests = contestService.getContestsByOrganizer(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(contests));
    }

    @PostMapping("/contests")
    public ResponseEntity<ApiResponse<ContestDetailDto>> createContest(
            @Valid @RequestBody ContestCreateRequest request,
            Authentication authentication) {
        ContestDetailDto created = contestService.createContest(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Contest created successfully", created));
    }

    @PutMapping("/contests/{id}")
    public ResponseEntity<ApiResponse<ContestDetailDto>> updateContest(
            @PathVariable Long id,
            @Valid @RequestBody ContestCreateRequest request,
            Authentication authentication) {
        ContestDetailDto updated = contestService.updateContest(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Contest updated successfully", updated));
    }

    @DeleteMapping("/contests/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContest(
            @PathVariable Long id,
            Authentication authentication) {
        contestService.deleteContest(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Contest deleted successfully", null));
    }

    @PatchMapping("/contests/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Status is required"));
        }
        contestService.updateContestStatus(id, status.toUpperCase(), authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Contest status updated to " + status.toUpperCase(), status.toUpperCase()));
    }

    @GetMapping("/contests/{id}/analytics")
    public ResponseEntity<ApiResponse<ContestAnalyticsDto>> getContestAnalytics(@PathVariable Long id) {
        ContestAnalyticsDto analytics = contestService.getContestAnalytics(id);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }
}
