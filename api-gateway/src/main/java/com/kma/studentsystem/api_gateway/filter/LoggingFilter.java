package com.kma.studentsystem.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = java.util.UUID.randomUUID().toString();
        
        // Add request ID to headers
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Request-ID", requestId)
                .build();
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        log.info("=== REQUEST START ===");
        log.info("Request ID: {}", requestId);
        log.info("Timestamp: {}", timestamp);
        log.info("Method: {}", request.getMethod());
        log.info("Path: {}", request.getURI().getPath());
        log.info("Query Params: {}", request.getURI().getQuery());
        log.info("Headers: {}", request.getHeaders());
        log.info("Remote Address: {}", request.getRemoteAddress());
        log.info("User Agent: {}", request.getHeaders().getFirst("User-Agent"));
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    String responseTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    
                    log.info("=== RESPONSE END ===");
                    log.info("Request ID: {}", requestId);
                    log.info("Response Timestamp: {}", responseTimestamp);
                    log.info("Status Code: {}", response.getStatusCode());
                    log.info("Response Headers: {}", response.getHeaders());
                    log.info("==================");
                }));
    }
    
    @Override
    public int getOrder() {
        return 1; // After AuthenticationFilter
    }
}
