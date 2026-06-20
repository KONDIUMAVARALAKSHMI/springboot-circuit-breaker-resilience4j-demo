package com.uma.circuitbreaker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CircuitBreakerMetricsDTO {

    @JsonProperty("circuitBreakerName")
    private String circuitBreakerName;

    @JsonProperty("state")
    private String state;

    @JsonProperty("failureRate")
    private String failureRate;

    @JsonProperty("numberOfBufferedCalls")
    private int numberOfBufferedCalls;

    @JsonProperty("numberOfFailedCalls")
    private int numberOfFailedCalls;

    @JsonProperty("numberOfSuccessfulCalls")
    private int numberOfSuccessfulCalls;

    @JsonProperty("numberOfNotPermittedCalls")
    private long numberOfNotPermittedCalls;

    public CircuitBreakerMetricsDTO() {}

    public String getCircuitBreakerName() { return circuitBreakerName; }
    public void setCircuitBreakerName(String circuitBreakerName) { this.circuitBreakerName = circuitBreakerName; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getFailureRate() { return failureRate; }
    public void setFailureRate(String failureRate) { this.failureRate = failureRate; }

    public int getNumberOfBufferedCalls() { return numberOfBufferedCalls; }
    public void setNumberOfBufferedCalls(int numberOfBufferedCalls) { this.numberOfBufferedCalls = numberOfBufferedCalls; }

    public int getNumberOfFailedCalls() { return numberOfFailedCalls; }
    public void setNumberOfFailedCalls(int numberOfFailedCalls) { this.numberOfFailedCalls = numberOfFailedCalls; }

    public int getNumberOfSuccessfulCalls() { return numberOfSuccessfulCalls; }
    public void setNumberOfSuccessfulCalls(int numberOfSuccessfulCalls) { this.numberOfSuccessfulCalls = numberOfSuccessfulCalls; }

    public long getNumberOfNotPermittedCalls() { return numberOfNotPermittedCalls; }
    public void setNumberOfNotPermittedCalls(long numberOfNotPermittedCalls) { this.numberOfNotPermittedCalls = numberOfNotPermittedCalls; }
}
