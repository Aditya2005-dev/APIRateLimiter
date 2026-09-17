package com.ARL.ApiRateLimiter.ratelimiter;

public class RateLimitResult {

    private final boolean allowed;
    private final long limit;
    private final long remaining;
    private final long resetAfterSeconds;

    public RateLimitResult(
            boolean allowed,
            long limit,
            long remaining,
            long resetAfterSeconds) {

        this.allowed = allowed;
        this.limit = limit;
        this.remaining = remaining;
        this.resetAfterSeconds = resetAfterSeconds;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public long getLimit() {
        return limit;
    }

    public long getRemaining() {
        return remaining;
    }

    public long getResetAfterSeconds() {
        return resetAfterSeconds;
    }
}