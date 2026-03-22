package com.imperialpedia.api.controller;

import com.imperialpedia.api.configuration.AppData;
import com.imperialpedia.api.dto.InfoResponse;
import com.imperialpedia.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.ZoneId;

@Tag(name = "Default", description = "System Information")
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class DefaultController {

    private final long startTime = System.currentTimeMillis();

    private final AppData appData;

    @Operation(summary = "Get API Info", description = "Fetch API System Information")
    @GetMapping
    public ApiResponse<InfoResponse> info(){

        String uptime = getUptime();

        InfoResponse infoResponse = new InfoResponse(
                appData.getName(),
                appData.getVersion(),
                appData.getDescription(),
                appData.getEnvironment(),
                appData.getStatus(),
                uptime,
                ZoneId.systemDefault().toString()
        );

        return ApiResponse.success(infoResponse);
    }

    private String getUptime(){
        long dif = System.currentTimeMillis() - startTime;

        long seconds = dif/1000;
        long minutes = seconds/60;
        long hours = minutes/60;

        return hours + "h " + (minutes % 60) + "m " + seconds + "s";
    }
}
