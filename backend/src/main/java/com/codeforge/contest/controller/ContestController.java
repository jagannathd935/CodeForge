package com.codeforge.contest.controller;

import com.codeforge.common.ApiResponse;
import com.codeforge.contest.dto.*;
import com.codeforge.contest.service.ContestService;
import com.codeforge.user.entity.Role;
import com.codeforge.user.entity.User;
import com.codeforge.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contests")
public class ContestController {

    private final ContestService contestService;
    private final UserRepository userRepository;

    public ContestController(ContestService contestService, UserRepository userRepository) {
        this.contestService = contestService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContestDto>>> getAllContests() {
        List<ContestDto> contests = contestService.getAllContests();
        return ResponseEntity.ok(ApiResponse.success(contests));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContestDetailDto>> createContest(
            @Valid @RequestBody ContestCreateRequest request,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Must be logged in to create a contest"));
        }
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null || (user.getRole() != Role.ROLE_ADMIN && user.getRole() != Role.ROLE_ORGANIZER)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access Denied: Only administrators or contest organizers can create contests"));
        }
        ContestDetailDto created = contestService.createContest(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Contest created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ContestDetailDto>> updateContest(
            @PathVariable Long id,
            @Valid @RequestBody ContestCreateRequest request,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Must be logged in to update a contest"));
        }
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null || (user.getRole() != Role.ROLE_ADMIN && user.getRole() != Role.ROLE_ORGANIZER)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access Denied: Only administrators or contest organizers can update contests"));
        }
        ContestDetailDto updated = contestService.updateContest(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Contest updated successfully", updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContestDetailDto>> getContestById(
            @PathVariable Long id,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        ContestDetailDto contest = contestService.getContestById(id, username);
        return ResponseEntity.ok(ApiResponse.success(contest));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ContestDetailDto>> getContestBySlug(
            @PathVariable String slug,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        ContestDetailDto contest = contestService.getContestBySlug(slug, username);
        return ResponseEntity.ok(ApiResponse.success(contest));
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<ApiResponse<String>> registerForContest(
            @PathVariable Long id,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Must be logged in to register"));
        }
        contestService.registerUserForContest(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Successfully registered for contest", "REGISTERED"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteContest(
            @PathVariable Long id,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Must be logged in to delete a contest"));
        }
        contestService.deleteContest(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Contest deleted successfully", "DELETED"));
    }

    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<ApiResponse<List<ContestStandingDto>>> getContestLeaderboard(@PathVariable Long id) {
        List<ContestStandingDto> standings = contestService.getContestLeaderboard(id);
        return ResponseEntity.ok(ApiResponse.success(standings));
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<ApiResponse<ContestResultDto>> getContestResults(@PathVariable Long id) {
        ContestResultDto results = contestService.getContestResults(id);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/{id}/analytics")
    public ResponseEntity<ApiResponse<ContestAnalyticsDto>> getContestAnalytics(@PathVariable Long id) {
        ContestAnalyticsDto analytics = contestService.getContestAnalytics(id);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }
}
