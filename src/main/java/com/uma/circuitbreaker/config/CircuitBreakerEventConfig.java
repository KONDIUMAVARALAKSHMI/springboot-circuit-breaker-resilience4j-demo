package com.uma.circuitbreaker.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerEventConfig {

    private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerEventConfig.class);

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @PostConstruct
    public void registerEventListeners() {
        CircuitBreaker userServiceCB = circuitBreakerRegistry.circuitBreaker("userService");

        // Log every state transition
        userServiceCB.getEventPublisher()
            .onStateTransition(event -> {
                logger.info("CircuitBreaker 'userService' changed state from {} to {}",
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState());
                System.out.println("[CIRCUIT BREAKER] State transition: "
                        + event.getStateTransition().getFromState()
                        + " -> " + event.getStateTransition().getToState());
            })
            .onError(event -> logger.warn("CircuitBreaker 'userService' recorded a failure: {}",
                    event.getThrowable().getMessage()))
            .onSuccess(event -> logger.debug("CircuitBreaker 'userService' recorded a success"))
            .onCallNotPermitted(event -> logger.warn("CircuitBreaker 'userService' did not permit a call (circuit is OPEN)"))
            .onReset(event -> logger.info("CircuitBreaker 'userService' was reset to CLOSED state"));
    }
}
