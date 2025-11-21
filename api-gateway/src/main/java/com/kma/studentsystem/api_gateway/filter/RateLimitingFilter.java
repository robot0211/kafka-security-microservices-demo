package com.kma.studentsystem.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final int MAX_REQUESTS_PER_HOUR = 1000;
    
    private final ConcurrentHashMap<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String clientIp = getClientIp(request);
        String userAgent = request.getHeaders().getFirst("User-Agent");
        String key = clientIp + ":" + userAgent;
        
        RequestCounter counter = requestCounters.computeIfAbsent(key, k -> new RequestCounter());
        
        LocalDateTime now = LocalDateTime.now();
        
        // Check minute rate limit
        if (counter.getMinuteCount(now) >= MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded for client: {} (minute limit)", clientIp);
            return handleRateLimitExceeded(exchange, "Rate limit exceeded. Maximum 100 requests per minute.");
        }
        
        // Check hour rate limit
        if (counter.getHourCount(now) >= MAX_REQUESTS_PER_HOUR) {
            log.warn("Rate limit exceeded for client: {} (hour limit)", clientIp);
            return handleRateLimitExceeded(exchange, "Rate limit exceeded. Maximum 1000 requests per hour.");
        }
        
        // Increment counters
        counter.increment(now);
        
        log.debug("Request allowed for client: {} (minute: {}/{}, hour: {}/{})", 
                clientIp, counter.getMinuteCount(now), MAX_REQUESTS_PER_MINUTE, 
                counter.getHourCount(now), MAX_REQUESTS_PER_HOUR);
        
        return chain.filter(exchange);
    }
    
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
    
    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("Retry-After", "60");
        
        String body = String.format("{\"error\":\"Rate Limit Exceeded\",\"message\":\"%s\"}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
    
    @Override
    public int getOrder() {
        return 0; // Before AuthenticationFilter
    }
    
    private static class RequestCounter {
        private final AtomicInteger minuteCount = new AtomicInteger(0);
        private final AtomicInteger hourCount = new AtomicInteger(0);
        private LocalDateTime minuteStart = LocalDateTime.now().withSecond(0).withNano(0);
        private LocalDateTime hourStart = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        
        public void increment(LocalDateTime now) {
            LocalDateTime currentMinute = now.withSecond(0).withNano(0);
            LocalDateTime currentHour = now.withMinute(0).withSecond(0).withNano(0);
            
            // Reset minute counter if new minute
            if (!currentMinute.equals(minuteStart)) {
                minuteCount.set(0);
                minuteStart = currentMinute;
            }
            
            // Reset hour counter if new hour
            if (!currentHour.equals(hourStart)) {
                hourCount.set(0);
                hourStart = currentHour;
            }
            
            minuteCount.incrementAndGet();
            hourCount.incrementAndGet();
        }
        
        public int getMinuteCount(LocalDateTime now) {
            LocalDateTime currentMinute = now.withSecond(0).withNano(0);
            if (!currentMinute.equals(minuteStart)) {
                return 0;
            }
            return minuteCount.get();
        }
        
        public int getHourCount(LocalDateTime now) {
            LocalDateTime currentHour = now.withMinute(0).withSecond(0).withNano(0);
            if (!currentHour.equals(hourStart)) {
                return 0;
            }
            return hourCount.get();
        }
    }
}
