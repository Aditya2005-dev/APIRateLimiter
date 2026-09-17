package com.ARL.ApiRateLimiter.protectedapi;

import com.ARL.ApiRateLimiter.controller.RateLimitRequest;
import com.ARL.ApiRateLimiter.proxy.ProxyService;
import com.ARL.ApiRateLimiter.ratelimiter.FixedWindowRateLimiter;
import com.ARL.ApiRateLimiter.ratelimiter.RateLimitResult;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.util.StringUtils;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProtectedApiController {

    private final ProtectedApiService protectedApiService;
    private final FixedWindowRateLimiter rateLimiter;
    private final ProxyService proxyService;

    public ProtectedApiController(
            ProtectedApiService protectedApiService,
            FixedWindowRateLimiter rateLimiter,
            ProxyService proxyService) {

        this.protectedApiService = protectedApiService;
        this.rateLimiter = rateLimiter;
        this.proxyService = proxyService;
    }

    @PostMapping("/protected/create")
    public String createProtectedApi(
            @RequestBody RateLimitRequest request) {

        ProtectedApi protectedApi =
                protectedApiService.create(
                        request.getTargetUrl(),
                        request.getKey(),
                        request.getLimit(),
                        request.getWindowSeconds()
                );

        return "http://localhost:8080/api/v1/proxy/"
                + protectedApi.getId();
    }

    @RequestMapping(
            value = "/v1/proxy/{id}",
            method = {
                    RequestMethod.GET,
                    RequestMethod.POST,
                    RequestMethod.PUT,
                    RequestMethod.PATCH,
                    RequestMethod.DELETE
            }
    )
    public ResponseEntity<?> proxyApi(
            @PathVariable String id,
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body) {

        // Find protected API
        ProtectedApi protectedApi =
                protectedApiService.getById(id);

        if (protectedApi == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Protected API not found");
        }

        // Check rate limit
        RateLimitResult result =
                rateLimiter.checkLimit(
                        protectedApi.getId()
                                + ":"
                                + protectedApi.getApiKey(),
                        protectedApi.getLimit(),
                        protectedApi.getWindowSeconds()
                );

        // Debug information
        System.out.println(
                "METHOD: " + request.getMethod()
        );

        System.out.println(
                "ALLOWED: " + result.isAllowed()
                        + " | LIMIT: " + result.getLimit()
                        + " | REMAINING: " + result.getRemaining()
        );

        // Rate limit exceeded
        if (!result.isAllowed()) {

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(
                            "X-RateLimit-Limit",
                            String.valueOf(result.getLimit())
                    )
                    .header(
                            "X-RateLimit-Remaining",
                            String.valueOf(result.getRemaining())
                    )
                    .header(
                            "X-RateLimit-Reset",
                            String.valueOf(
                                    result.getResetAfterSeconds()
                            )
                    )
                    .body(result);
        }

        // Forward selected headers
        HttpHeaders headers = new HttpHeaders();

        String contentType =
                request.getHeader("Content-Type");

        String authorization =
                request.getHeader("Authorization");

        String accept =
                request.getHeader("Accept");

        if (StringUtils.hasText(contentType)) {
            headers.set("Content-Type", contentType);
        }

        if (StringUtils.hasText(authorization)) {
            headers.set("Authorization", authorization);
        }

        if (StringUtils.hasText(accept)) {
            headers.set("Accept", accept);
        }

        // Forward request to target API
        ResponseEntity<String> targetResponse =
                proxyService.forwardRequest(
                        HttpMethod.valueOf(request.getMethod()),
                        protectedApi.getTargetUrl(),
                        body,
                        headers
                );

        // Return target API response
        return ResponseEntity
                .status(targetResponse.getStatusCode())
                .header(
                        "X-RateLimit-Limit",
                        String.valueOf(result.getLimit())
                )
                .header(
                        "X-RateLimit-Remaining",
                        String.valueOf(result.getRemaining())
                )
                .header(
                        "X-RateLimit-Reset",
                        String.valueOf(
                                result.getResetAfterSeconds()
                        )
                )
                .body(targetResponse.getBody());
    }
}