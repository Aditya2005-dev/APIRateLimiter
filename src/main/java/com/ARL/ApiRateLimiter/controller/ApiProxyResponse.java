package com.ARL.ApiRateLimiter.controller;

import com.ARL.ApiRateLimiter.ratelimiter.RateLimitResult;

public class ApiProxyResponse {

    private final RateLimitResult rateLimit;
    private final String targetResponse;

    public ApiProxyResponse(
            RateLimitResult rateLimit,
            String targetResponse) {

        this.rateLimit = rateLimit;
        this.targetResponse = targetResponse;
    }

    public RateLimitResult getRateLimit() {
        return rateLimit;
    }

    public String getTargetResponse() {
        return targetResponse;
    }
}