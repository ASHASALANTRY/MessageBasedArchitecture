package com.example.demo.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@Configuration
public class SwaggerConfig {
    @Autowired
    private SwaggerConfigProperties swaggerConfigProperties;

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title(swaggerConfigProperties.getOpenApi().getTitle())
                        .description(swaggerConfigProperties.getOpenApi().getDescription())
                        .version(swaggerConfigProperties.getOpenApi().getVersion())
                        .license(new License().name(swaggerConfigProperties.getLicense().getName())
                                .url(swaggerConfigProperties.getLicense().getUrl())))
                .components(new Components()
                        .addSecuritySchemes("Authorization",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")
                                        .bearerFormat("JWT")))

                .security(List.of(new SecurityRequirement().addList("Authorization")));
    }
}
