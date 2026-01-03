package com.inguzdev.gitly.controller;

import java.util.HashMap;
import java.util.Map;

import com.inguzdev.gitly.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        System.out.println("Health check endpoint called.");
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "Gitly API");
        healthData.put("version", "1.0.0");

        ApiResponse<Map<String, Object>> response = ApiResponse.success(
                "Application is healthy and running",
                healthData);

        return ResponseEntity.ok(response);
    }
}
