package com.ARL.ApiRateLimiter.ratelimiter;

public interface RateLimiter {

    RateLimitResult checkLimit(
            String key,
            int limit,
            int windowSeconds
    );
}