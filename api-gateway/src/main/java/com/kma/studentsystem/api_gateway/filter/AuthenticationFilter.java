package com.kma.studentsystem.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/users/login",
            "/api/users/register",
            "/api/users/verify-email",
            "/api/users/reset-password",
            "/api/users/confirm-password-reset",
            "/health",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        log.info("Processing request: {} {}", request.getMethod(), path);
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            log.info("Public endpoint accessed: {}", path);
            return chain.filter(exchange);
        }
        
        // Check for Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid authorization header for path: {}", path);
            return handleUnauthorized(exchange);
        }
        
        // Extract token
        String token = authHeader.substring(7);
        
        // Validate token (in a real implementation, you would validate the JWT token)
        if (!isValidToken(token)) {
            log.warn("Invalid token for path: {}", path);
            return handleUnauthorized(exchange);
        }
        
        // Add user info to request headers
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", extractUserIdFromToken(token))
                .header("X-User-Role", extractUserRoleFromToken(token))
                .build();
        
        log.info("Request authenticated successfully for path: {}", path);
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }
    
    private boolean isValidToken(String token) {
        // In a real implementation, you would validate the JWT token
        // For demo purposes, we'll accept any non-empty token
        return token != null && !token.isEmpty() && !token.equals("invalid");
    }
    
    private String extractUserIdFromToken(String token) {
        // In a real implementation, you would extract user ID from JWT token
        // For demo purposes, we'll return a default user ID
        return "user123";
    }
    
    private String extractUserRoleFromToken(String token) {
        // In a real implementation, you would extract user role from JWT token
        // For demo purposes, we'll return a default role
        return "STUDENT";
    }
    
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\":\"Unauthorized\",\"message\":\"Missing or invalid authentication token\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
    
    @Override
    public int getOrder() {
        return -1; // High priority
    }
}
