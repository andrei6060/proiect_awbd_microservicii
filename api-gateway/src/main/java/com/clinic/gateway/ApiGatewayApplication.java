package com.clinic.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Cloud Gateway (reactive, Netty). Single external entry point on port
 * 8080 that routes /api/v1/** to the backing services via Eureka (lb://...),
 * adding rate limiting, request logging, correlation ids and CORS.
 */
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
