package com.imperialpedia.api.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppData {

    private String Name;
    private String Description;
    private String version;
    private String environment;
    private String status;

    private final String swaggerDescription = "API documentation for ImperialPedia";
}
