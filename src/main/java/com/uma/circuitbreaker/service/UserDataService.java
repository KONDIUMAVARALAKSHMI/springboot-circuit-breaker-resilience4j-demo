package com.uma.circuitbreaker.service;

import com.uma.circuitbreaker.dto.UserDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service that fetches user data from an external API,
 * protected by a Resilience4j Circuit Breaker.
 */
@Service
public class UserDataService {

    private static final Logger logger = LoggerFactory.getLogger(UserDataService.class);

    private static final String MOCK_SERVICE_URL = "http://localhost:8080/mock/users/{id}";

    private final RestTemplate restTemplate;

    // Constructor injection (best practice)
    public UserDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetch user data from external service with circuit breaker protection.
     */
    @CircuitBreaker(name = "userService", fallbackMethod = "getFallbackUserData")
    public UserDTO fetchUser(String id) {

        logger.info("Calling external service for userId={}", id);

        ResponseEntity<UserDTO> response =
                restTemplate.getForEntity(MOCK_SERVICE_URL, UserDTO.class, id);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException(
                    "External service failed with status: " + response.getStatusCode()
            );
        }

        UserDTO user = response.getBody();

        logger.info("Successfully fetched user: {}", user);

        return user;
    }

    /**
     * Fallback method for circuit breaker.
     */
    public UserDTO getFallbackUserData(String id, Throwable throwable) {

        logger.warn("Fallback triggered for userId={}. Error: {}",
                id,
                throwable != null ? throwable.getMessage() : "Unknown error"
        );

        return new UserDTO(
                "default-id",
                "Default User",
                "default@example.com"
        );
    }
}