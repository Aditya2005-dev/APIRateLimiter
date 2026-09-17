package com.ARL.ApiRateLimiter.protectedapi;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProtectedApiService {

    private final ProtectedApiRepository protectedApiRepository;

    public ProtectedApiService(
            ProtectedApiRepository protectedApiRepository) {

        this.protectedApiRepository = protectedApiRepository;
    }

    public ProtectedApi create(
            String targetUrl,
            String apiKey,
            int limit,
            int windowSeconds) {

        String id = UUID.randomUUID()
                .toString()
                .substring(0, 8);

        ProtectedApi protectedApi =
                new ProtectedApi(
                        id,
                        targetUrl,
                        apiKey,
                        limit,
                        windowSeconds
                );

        return protectedApiRepository.save(protectedApi);
    }

    public ProtectedApi getById(String id) {

        return protectedApiRepository
                .findById(id)
                .orElse(null);
    }
}