package com.imperialpedia.api.configuration;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:3000"));
    private List<String> allowedOriginPatterns = new ArrayList<>();
    private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    private List<String> allowedHeaders = new ArrayList<>(List.of("*"));
    private List<String> exposedHeaders = new ArrayList<>(List.of("X-Rate-Limit-Remaining", "Retry-After"));
    private boolean allowCredentials = true;
    private long maxAge = 3600;

    @PostConstruct
    void validate() {
        if (allowCredentials && containsWildcard(allowedOrigins)) {
            throw new IllegalStateException("Invalid CORS configuration: wildcard '*' is not allowed in cors.allowed-origins when credentials are enabled.");
        }

        if (allowCredentials && containsWildcard(allowedOriginPatterns)) {
            throw new IllegalStateException("Invalid CORS configuration: wildcard '*' is not allowed in cors.allowed-origin-patterns when credentials are enabled.");
        }
    }

    private boolean containsWildcard(List<String> values) {
        return values.stream().map(String::trim).anyMatch("*"::equals);
    }
}

