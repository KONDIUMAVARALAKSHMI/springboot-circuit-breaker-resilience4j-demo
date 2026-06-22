package com.uma.circuitbreaker.controller;

import com.uma.circuitbreaker.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock external service controller that simulates failures, successes,
 * or alternating behaviors for testing circuit breaker patterns.
 */
@RestController
@RequestMapping("/mock")
public class MockExternalServiceController {

    private static final Logger logger = LoggerFactory.getLogger(MockExternalServiceController.class);

    private enum MockMode {
        SUCCESS,
        FAILURE,
        ALTERNATING
    }

    private MockMode currentMode = MockMode.SUCCESS;
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    /**
     * GET /mock/users/{id}
     * Simulates retrieving a user. Behaves according to the current selected mode.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getMockUser(@PathVariable String id) {
        int count = requestCounter.incrementAndGet();
        logger.info("Mock service call #{} for userId={}. Current mode: {}", count, id, currentMode);

        boolean shouldFail = false;
        if (currentMode == MockMode.FAILURE) {
            shouldFail = true;
        } else if (currentMode == MockMode.ALTERNATING) {
            // Alternating mode: fail on even requests, succeed on odd requests
            shouldFail = (count % 2 == 0);
        }

        if (shouldFail) {
            logger.warn("Mock service returning 500 Internal Server Error for userId={}", id);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Simulated mock service failure");
        }

        logger.info("Mock service returning 200 OK for userId={}", id);
        return ResponseEntity.ok(new UserDTO(
                id,
                "Real User " + id,
                "user" + id + "@example.com"
        ));
    }

    /**
     * POST /mock/failure?enabled=true
     * Configures the mock service to always return errors.
     */
    @PostMapping("/failure")
    public ResponseEntity<String> setFailureMode(@RequestParam(defaultValue = "true") boolean enabled) {
        if (enabled) {
            currentMode = MockMode.FAILURE;
        } else {
            currentMode = MockMode.SUCCESS;
        }
        logger.info("Mock service mode set to: {}", currentMode);
        return ResponseEntity.ok("Failure mode set to " + enabled + " (current mode: " + currentMode + ")");
    }

    /**
     * POST /mock/success?enabled=true
     * Configures the mock service to always return success.
     */
    @PostMapping("/success")
    public ResponseEntity<String> setSuccessMode(@RequestParam(defaultValue = "true") boolean enabled) {
        if (enabled) {
            currentMode = MockMode.SUCCESS;
        } else {
            currentMode = MockMode.FAILURE;
        }
        logger.info("Mock service mode set to: {}", currentMode);
        return ResponseEntity.ok("Success mode set to " + enabled + " (current mode: " + currentMode + ")");
    }

    /**
     * POST /mock/reset
     * Resets the mock service to alternating (50% failure) mode and resets the call counter.
     */
    @PostMapping("/reset")
    public ResponseEntity<String> resetBehavior() {
        currentMode = MockMode.ALTERNATING;
        requestCounter.set(0);
        logger.info("Mock service reset to ALTERNATING mode");
        return ResponseEntity.ok("Mock service reset to ALTERNATING mode");
    }

    /**
     * GET /mock/status
     * Returns the current status/mode of the mock service.
     */
    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("Current mock service mode: " + currentMode);
    }
}
