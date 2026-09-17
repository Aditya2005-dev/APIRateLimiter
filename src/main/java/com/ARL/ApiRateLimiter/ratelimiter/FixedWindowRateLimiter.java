package com.ARL.ApiRateLimiter.ratelimiter;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class FixedWindowRateLimiter implements RateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;

    public FixedWindowRateLimiter(
            RedisTemplate<String, Object> redisTemplate) {

        this.redisTemplate = redisTemplate;
    }

    @Override
    public RateLimitResult checkLimit(
            String key,
            int limit,
            int windowSeconds) {

        String redisKey = "rate_limit:" + key;

        Long currentCount =
                redisTemplate.opsForValue().increment(redisKey);

        // Temporary debugging
        System.out.println(
                "RATE LIMIT KEY: " + redisKey
        );

        System.out.println(
                "CURRENT COUNT: " + currentCount
                        + " / LIMIT: " + limit
        );

        if (currentCount != null && currentCount == 1) {

            redisTemplate.expire(
                    redisKey,
                    Duration.ofSeconds(windowSeconds)
            );
        }

        Long ttl = redisTemplate.getExpire(
                redisKey,
                TimeUnit.SECONDS
        );

        long remaining = Math.max(
                0,
                limit - (currentCount == null ? 0 : currentCount)
        );

        boolean allowed =
                currentCount != null && currentCount <= limit;

        return new RateLimitResult(
                allowed,
                limit,
                remaining,
                ttl
        );
    }
}