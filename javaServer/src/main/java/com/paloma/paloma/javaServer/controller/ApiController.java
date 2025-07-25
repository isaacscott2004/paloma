package com.paloma.paloma.javaServer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "Paloma Mental Health API");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Paloma Mental Health API");
        response.put("version", "1.0.0");
        response.put("description", "REST API for mental health tracking application");
        response.put("endpoints", Map.of(
            "users", "/api/users",
            "checkins", "/api/checkins", 
            "medications", "/api/medications",
            "health", "/api/health"
        ));
        return ResponseEntity.ok(response);
    }
}