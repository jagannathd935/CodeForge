package com.codeforge.common;

import com.codeforge.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final UserRepository userRepository;

    public HealthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "CodeForge Backend API");
        status.put("database", "Connected");

        try {
            long userCount = userRepository.count();
            status.put("registeredUsers", userCount);
        } catch (Exception e) {
            status.put("database", "Error: " + e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.success("CodeForge backend foundation is operational", status));
    }
}
