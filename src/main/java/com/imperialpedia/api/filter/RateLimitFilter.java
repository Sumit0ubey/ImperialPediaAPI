package com.imperialpedia.api.filter;

import com.imperialpedia.api.configuration.RateLimitProperties;
import com.imperialpedia.api.exception.TooManyRequestsException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_RATE_LIMIT_REMAINING = "X-Rate-Limit-Remaining";
    private static final String X_RATE_LIMIT_LIMIT = "X-Rate-Limit-Limit";

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, Pattern> regexPatternCache = new ConcurrentHashMap<>();
    private final RateLimitProperties rateLimitProperties;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        for (String prefix : rateLimitProperties.getSkippedPathPrefixes()) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }

        for (String exactPath : rateLimitProperties.getSkippedPathExacts()) {
            if (path.equals(exactPath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String clientKey = resolveClientKey(request);
            String path = request.getRequestURI();
            RateRule rule = resolveRateRule(path);

            Bucket bucket = buckets.computeIfAbsent(
                    buildBucketKey(clientKey, rule.group()),
                    key -> createBucket(rule)
            );

            if (!bucket.tryConsume(1)) {
                throw new TooManyRequestsException("Too many requests. Please try again later.");
            }

            response.setHeader(X_RATE_LIMIT_REMAINING, String.valueOf(bucket.getAvailableTokens()));
            response.setHeader(X_RATE_LIMIT_LIMIT, String.valueOf(rule.capacity()));
            filterChain.doFilter(request, response);

        } catch (TooManyRequestsException ex) {
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }

    private RateRule resolveRateRule(String path) {
        for (RateLimitProperties.Rule configuredRule : safeRules()) {
            if (matches(path, configuredRule)) {
                return new RateRule(
                        configuredRule.getGroup(),
                        configuredRule.getCapacity(),
                        configuredRule.getWindow()
                );
            }
        }

        RateLimitProperties.DefaultRule defaultRule = rateLimitProperties.getDefaultRule();
        return new RateRule(defaultRule.getGroup(), defaultRule.getCapacity(), defaultRule.getWindow());
    }

    private Bucket createBucket(RateRule rule) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(rule.capacity())
                        .refillGreedy(rule.capacity(), rule.window())
                        .build())
                .build();
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader(X_FORWARDED_FOR);

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private String buildBucketKey(String clientKey, String group) {
        return clientKey + ":" + group;
    }

    private List<RateLimitProperties.Rule> safeRules() {
        List<RateLimitProperties.Rule> rules = rateLimitProperties.getRules();
        if (rules == null) {
            return List.of();
        }

        return rules;
    }

    private boolean matches(String path, RateLimitProperties.Rule rule) {
        if (rule == null || rule.getType() == null || rule.getPattern() == null || rule.getPattern().isBlank()) {
            return false;
        }

        return switch (rule.getType()) {
            case EXACT -> path.equals(rule.getPattern());
            case PREFIX -> path.startsWith(rule.getPattern());
            case REGEX -> regexPatternCache
                    .computeIfAbsent(rule.getPattern(), Pattern::compile)
                    .matcher(path)
                    .matches();
        };
    }

    private record RateRule(String group, long capacity, Duration window) {}
}
