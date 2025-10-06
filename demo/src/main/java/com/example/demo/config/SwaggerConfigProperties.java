package com.example.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "swagger", ignoreUnknownFields = false)
@Data
public class SwaggerConfigProperties {

    private OpenApi openApi = new OpenApi();
    private License license = new License();
    private Component component = new Component();
    private String groupName = "Tenant";

    @Data
    public static class OpenApi {
        private String title = "SpringShop API";
        private String description = "Spring shop sample application";
        private String version = "v3.1.0";
    }

    @Data
    public static class License {
        private String name = "Apache 2.0";
        private String url = "http://springdoc.org";
    }

    @Data
    public static class Component {
        private String securitySchemes = "Authorization";
        private String scheme = "bearer";
        private String bearerFormat = "JWT";
        private String securityReq = "Authorization";
    }
}
