package com.uma.circuitbreaker.controller;

import com.uma.circuitbreaker.dto.CircuitBreakerMetricsDTO;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for querying circuit breaker state and metrics.
 */
@RestController
@RequestMapping("/api/circuit-breaker")
public class CircuitBreakerController {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerController.class);
    private static final String CB_NAME = "userService";

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * GET /api/circuit-breaker/state
     * Returns current state of the userService circuit breaker as plain text:
     * CLOSED, OPEN, or HALF_OPEN
     */
    @GetMapping("/state")
    public ResponseEntity<String> getState() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(CB_NAME);
        String state = cb.getState().name();
        logger.info("Circuit breaker '{}' state queried: {}", CB_NAME, state);
        return ResponseEntity.ok(state);
    }

    /**
     * GET /api/circuit-breaker/metrics
     * Returns simplified metrics JSON for the userService circuit breaker.
     */
    @GetMapping("/metrics")
    public ResponseEntity<CircuitBreakerMetricsDTO> getMetrics() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(CB_NAME);
        CircuitBreaker.Metrics metrics = cb.getMetrics();

        CircuitBreakerMetricsDTO dto = new CircuitBreakerMetricsDTO();
        dto.setCircuitBreakerName(CB_NAME);
        dto.setState(cb.getState().name());
        dto.setFailureRate(metrics.getFailureRate() == -1.0f
                ? "N/A (not enough calls)"
                : metrics.getFailureRate() + "%");
        dto.setNumberOfBufferedCalls(metrics.getNumberOfBufferedCalls());
        dto.setNumberOfFailedCalls(metrics.getNumberOfFailedCalls());
        dto.setNumberOfSuccessfulCalls(metrics.getNumberOfSuccessfulCalls());
        dto.setNumberOfNotPermittedCalls(metrics.getNumberOfNotPermittedCalls());

        logger.info("Circuit breaker '{}' metrics queried: state={}, failureRate={}",
                CB_NAME, cb.getState(), metrics.getFailureRate());

        return ResponseEntity.ok(dto);
    }
}
