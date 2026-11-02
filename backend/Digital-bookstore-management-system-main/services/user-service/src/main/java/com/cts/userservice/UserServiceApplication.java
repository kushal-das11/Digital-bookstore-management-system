package com.cts.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main entry point for the User Service Spring Boot application.
 *
 * <p>{@code @SpringBootApplication} enables component scanning, auto-configuration,
 * and configuration-property support across the {@code com.cts.userservice}
 * package. {@code @EnableDiscoveryClient} registers this service with the
 * configured service-discovery registry (for example, Eureka or Consul), so it
 * can be located by other microservices and the API gateway.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

    /**
     * Boots the Spring application context and starts the embedded server.
     *
     * @param args command-line arguments passed through to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}