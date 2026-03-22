package com.imperialpedia.api.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Use format: Bearer <token>")
                ))
                .info(new Info()
                        .title(appData.getName())
                        .version(appData.getVersion())
                        .description(appData.getSwaggerDescription() + "\n\n" +
                                "### Documentation style\n" +
                                "Each endpoint includes required/optional parameters, limits, examples, and expected responses.")
                );
    }
}
