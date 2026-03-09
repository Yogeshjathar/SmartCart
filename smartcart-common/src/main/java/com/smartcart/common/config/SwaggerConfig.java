package com.smartcart.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Value("${swagger.title:SmartCart API}")
    private String title;

    @Value("${swagger.description:SmartCart Microservices API}")
    private String description;

    @Value("${swagger.version:1.0}")
    private String version;

    @Value("${swagger.server-url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(apiInfo())
                .addServersItem(new Server().url(serverUrl))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme()));

    }

    private Info apiInfo() {
        return new Info()
                .title(title)
                .description(description)
                .version(version);
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }
}