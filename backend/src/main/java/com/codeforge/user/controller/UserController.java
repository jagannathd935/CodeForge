package com.codeforge.user.controller;

import com.codeforge.auth.dto.UserProfileDto;
import com.codeforge.common.ApiResponse;
import com.codeforge.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserProfileDto profile = userService.getCurrentUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/me/stats")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserStats(@AuthenticationPrincipal UserDetails userDetails) {
        UserProfileDto profile = userService.getCurrentUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @org.springframework.web.bind.annotation.PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String avatarUrl = body.get("avatarUrl");
        UserProfileDto updated = userService.updateUserProfile(userDetails.getUsername(), avatarUrl);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<java.util.List<com.codeforge.user.dto.LeaderboardDto>>> getLeaderboard() {
        java.util.List<com.codeforge.user.dto.LeaderboardDto> leaderboard = userService.getGlobalLeaderboard();
        return ResponseEntity.ok(ApiResponse.success(leaderboard));
    }
}
