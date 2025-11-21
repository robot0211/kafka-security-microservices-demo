package com.kma.studentsystem.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Student Service Routes
                .route("student-service-get", r -> r
                        .path("/api/students/**")
                        .and().method(HttpMethod.GET)
                        .uri("lb://student-service"))
                
                .route("student-service-post", r -> r
                        .path("/api/students/**")
                        .and().method(HttpMethod.POST)
                        .uri("lb://student-service"))
                
                .route("student-service-put", r -> r
                        .path("/api/students/**")
                        .and().method(HttpMethod.PUT)
                        .uri("lb://student-service"))
                
                .route("student-service-delete", r -> r
                        .path("/api/students/**")
                        .and().method(HttpMethod.DELETE)
                        .uri("lb://student-service"))
                
                // Course Service Routes
                .route("course-service-get", r -> r
                        .path("/api/courses/**")
                        .and().method(HttpMethod.GET)
                        .uri("lb://course-service"))
                
                .route("course-service-post", r -> r
                        .path("/api/courses/**")
                        .and().method(HttpMethod.POST)
                        .uri("lb://course-service"))
                
                .route("course-service-put", r -> r
                        .path("/api/courses/**")
                        .and().method(HttpMethod.PUT)
                        .uri("lb://course-service"))
                
                .route("course-service-delete", r -> r
                        .path("/api/courses/**")
                        .and().method(HttpMethod.DELETE)
                        .uri("lb://course-service"))
                
                // Grade Service Routes
                .route("grade-service-get", r -> r
                        .path("/api/grades/**")
                        .and().method(HttpMethod.GET)
                        .uri("lb://grade-service"))
                
                .route("grade-service-post", r -> r
                        .path("/api/grades/**")
                        .and().method(HttpMethod.POST)
                        .uri("lb://grade-service"))
                
                .route("grade-service-put", r -> r
                        .path("/api/grades/**")
                        .and().method(HttpMethod.PUT)
                        .uri("lb://grade-service"))
                
                .route("grade-service-delete", r -> r
                        .path("/api/grades/**")
                        .and().method(HttpMethod.DELETE)
                        .uri("lb://grade-service"))
                
                // Enrollment Service Routes
                .route("enrollment-service-get", r -> r
                        .path("/api/enrollments/**")
                        .and().method(HttpMethod.GET)
                        .uri("lb://enrollment-service"))
                
                .route("enrollment-service-post", r -> r
                        .path("/api/enrollments/**")
                        .and().method(HttpMethod.POST)
                        .uri("lb://enrollment-service"))
                
                .route("enrollment-service-put", r -> r
                        .path("/api/enrollments/**")
                        .and().method(HttpMethod.PUT)
                        .uri("lb://enrollment-service"))
                
                .route("enrollment-service-delete", r -> r
                        .path("/api/enrollments/**")
                        .and().method(HttpMethod.DELETE)
                        .uri("lb://enrollment-service"))
                
                // Notification Service Routes
                .route("notification-service-get", r -> r
                        .path("/api/notifications/**")
                        .and().method(HttpMethod.GET)
                        .uri("lb://notification-service"))
                
                .route("notification-service-post", r -> r
                        .path("/api/notifications/**")
                        .and().method(HttpMethod.POST)
                        .uri("lb://notification-service"))
                
                .route("notification-service-put", r -> r
                        .path("/api/notifications/**")
                        .and().method(HttpMethod.PUT)
                        .uri("lb://notification-service"))
                
                .route("notification-service-delete", r -> r
                        .path("/api/notifications/**")
                        .and().method(HttpMethod.DELETE)
                        .uri("lb://notification-service"))
                
                // Identity Service Routes
                .route("identity-service-get", r -> r
                        .path("/api/users/**")
                        .and().method(HttpMethod.GET)
                        .uri("lb://identity-service"))
                
                .route("identity-service-post", r -> r
                        .path("/api/users/**")
                        .and().method(HttpMethod.POST)
                        .uri("lb://identity-service"))
                
                .route("identity-service-put", r -> r
                        .path("/api/users/**")
                        .and().method(HttpMethod.PUT)
                        .uri("lb://identity-service"))
                
                .route("identity-service-delete", r -> r
                        .path("/api/users/**")
                        .and().method(HttpMethod.DELETE)
                        .uri("lb://identity-service"))
                
                // Health Check Routes
                .route("health-check", r -> r
                        .path("/health/**")
                        .uri("lb://student-service"))
                
                .build();
    }
}
