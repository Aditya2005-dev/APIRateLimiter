package com.ARL.ApiRateLimiter.proxy;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ProxyService {

    private final RestClient restClient;

    public ProxyService() {
        this.restClient = RestClient.create();
    }

    public ResponseEntity<String> forwardRequest(
            HttpMethod method,
            String targetUrl,
            byte[] body,
            HttpHeaders headers) {

        return restClient
                .method(method)
                .uri(targetUrl)
                .headers(requestHeaders -> {

                    if (headers != null) {
                        requestHeaders.putAll(headers);
                    }

                })
                .body(body != null ? body : new byte[0])
                .retrieve()
                .toEntity(String.class);
    }
}