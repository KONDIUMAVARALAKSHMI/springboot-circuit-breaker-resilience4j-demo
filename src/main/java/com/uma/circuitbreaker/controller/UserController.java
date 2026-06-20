package com.uma.circuitbreaker.controller;

import com.uma.circuitbreaker.dto.UserDTO;
import com.uma.circuitbreaker.service.UserDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing the /api/users endpoint.
 * Circuit breaker logic is handled inside UserDataService.
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserDataService userDataService;

    // Constructor injection (recommended over @Autowired field injection)
    public UserController(UserDataService userDataService) {
        this.userDataService = userDataService;
    }

    /**
     * GET /api/users/{id}
     * Fetch user data via service layer (with circuit breaker protection).
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String id) {

        logger.info("Received request for userId={}", id);

        UserDTO user = userDataService.fetchUser(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }
}