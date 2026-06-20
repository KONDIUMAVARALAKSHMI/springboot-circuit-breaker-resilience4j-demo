package com.uma.circuitbreaker.controller;

import com.uma.circuitbreaker.dto.UserDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mock")
public class MockUserController {

    @GetMapping("/users/{id}")
    public UserDTO getMockUser(@PathVariable String id) {

        return new UserDTO(
                id,
                "Real User " + id,
                "user" + id + "@example.com"
        );
    }
}