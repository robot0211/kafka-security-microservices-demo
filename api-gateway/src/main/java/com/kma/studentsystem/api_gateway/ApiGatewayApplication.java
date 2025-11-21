package com.kma.studentsystem.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
 

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        System.out.println("🚀 API Gateway started successfully!");
        System.out.println("🌐 API Gateway: http://localhost:8080");
    }

    
}