package com.cts.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.PostConstruct;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import java.util.List;


import org.springframework.boot.autoconfigure.AutoConfiguration;

@AutoConfiguration
public class SwaggerConfig {
    private static final List<String> INTERNAL_HEADERS =
            List.of("X-User-Id", "X-Original-Path", "X-Http-Method");


    @PostConstruct
    public void init() {
        System.out.println("COMMON SWAGGER CONFIG LOADED");
    }


    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // THIS LINE ENABLES AUTHORIZE BUTTON
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))

                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .servers(List.of(
                        new Server().url("http://localhost:8080")
                ));
    }

    @Bean
    public OperationCustomizer hideInternalHeaders() {
        return (Operation operation, org.springframework.web.method.HandlerMethod handlerMethod) -> {

            if (operation.getParameters() != null) {
                operation.getParameters().removeIf(p ->
                        INTERNAL_HEADERS.contains(p.getName())
                );
            }

            return operation;
        };
    }

}