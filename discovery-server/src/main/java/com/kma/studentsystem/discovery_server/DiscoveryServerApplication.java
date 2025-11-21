package com.kma.studentsystem.discovery_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
        System.out.println("🚀 Eureka Discovery Server started successfully!");
        System.out.println("🔍 Eureka Dashboard: http://localhost:8761");
    }
}