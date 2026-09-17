package com.ARL.ApiRateLimiter.protectedapi;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ProtectedApi {

    @Id
    private String id;

    private String targetUrl;

    private String apiKey;

    @Column(name = "request_limit")
    private int limit;

    private int windowSeconds;

    // Required by JPA
    public ProtectedApi() {
    }

    public ProtectedApi(
            String id,
            String targetUrl,
            String apiKey,
            int limit,
            int windowSeconds) {

        this.id = id;
        this.targetUrl = targetUrl;
        this.apiKey = apiKey;
        this.limit = limit;
        this.windowSeconds = windowSeconds;
    }

    public String getId() {
        return id;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public int getLimit() {
        return limit;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }
}