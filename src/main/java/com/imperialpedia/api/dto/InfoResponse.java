package com.imperialpedia.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Schema for System API Info")
public class InfoResponse {

    @Schema(description = "API Name", example = "api")
    private String name;

    @Schema(description = "API version", example = "1.0")
    private String version;

    @Schema(description = "About API", example = "This is an API")
    private String description;

    @Schema(description = "Current Environment", example = "PROD")
    private String environment;

    @Schema(description = "API Status", example = "UP")
    private String status;

    @Schema(description = "Server Uptime", example = "45h 31m 41s")
    private String uptime;

    @Schema(description = "Server Region", example = "US")
    private String serverTimeZone;
}
