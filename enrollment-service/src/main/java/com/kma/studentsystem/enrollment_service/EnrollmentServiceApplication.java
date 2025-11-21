package com.kma.studentsystem.enrollment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class EnrollmentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnrollmentServiceApplication.class, args);
		System.out.println("🚀 Enrollment Service started successfully!");
		System.out.println("📊 Enrollment Service: http://localhost:8084");
	}

}
