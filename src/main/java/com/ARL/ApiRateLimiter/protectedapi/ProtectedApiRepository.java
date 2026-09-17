package com.ARL.ApiRateLimiter.protectedapi;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProtectedApiRepository
        extends JpaRepository<ProtectedApi, String> {
}