package com.kma.studentsystem.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    @GetMapping("/student-service")
    public ResponseEntity<Map<String, Object>> studentServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Student Service is currently unavailable. Please try again later.");
        response.put("service", "student-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    @GetMapping("/course-service")
    public ResponseEntity<Map<String, Object>> courseServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Course Service is currently unavailable. Please try again later.");
        response.put("service", "course-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    @GetMapping("/grade-service")
    public ResponseEntity<Map<String, Object>> gradeServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Grade Service is currently unavailable. Please try again later.");
        response.put("service", "grade-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    @GetMapping("/enrollment-service")
    public ResponseEntity<Map<String, Object>> enrollmentServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Enrollment Service is currently unavailable. Please try again later.");
        response.put("service", "enrollment-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    @GetMapping("/notification-service")
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Notification Service is currently unavailable. Please try again later.");
        response.put("service", "notification-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    @GetMapping("/identity-service")
    public ResponseEntity<Map<String, Object>> identityServiceFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Identity Service is currently unavailable. Please try again later.");
        response.put("service", "identity-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
