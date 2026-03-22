package com.imperialpedia.api.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;

import java.util.List;

@Configuration
@RequiredArgsConstructor()
public class OpenApiConfig {

    private final AppData appData;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of())
                .info(new Info()
                        .title(appData.getName())
                        .version(appData.getVersion())
                        .description(appData.getSwaggerDescription())
                );
    }
}
