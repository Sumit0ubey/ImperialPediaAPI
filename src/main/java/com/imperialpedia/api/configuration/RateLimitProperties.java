package com.imperialpedia.api.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    private List<String> skippedPathPrefixes = new ArrayList<>(List.of("/swagger-ui", "/v3/api-docs"));
    private List<String> skippedPathExacts = new ArrayList<>(List.of("/favicon.ico"));
    private DefaultRule defaultRule = new DefaultRule();
    private List<Rule> rules = new ArrayList<>();

    @Data
    public static class DefaultRule {
        private String group = "default";
        private long capacity = 15;
        private Duration window = Duration.ofMinutes(1);
    }

    @Data
    public static class Rule {
        private MatchType type = MatchType.PREFIX;
        private String pattern;
        private String group;
        private long capacity = 15;
        private Duration window = Duration.ofMinutes(1);
    }

    public enum MatchType {
        EXACT,
        PREFIX,
        REGEX
    }
}

