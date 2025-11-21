package com.kma.studentsystem.grade_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
public class GradeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GradeServiceApplication.class, args);
		System.out.println("🚀 Grade Service started successfully!");
		System.out.println("📊 Grade Service: http://localhost:8083");
	}

}
