package com.example.demo.config;


import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.util.Base64;

@Configuration
public class DbConfig {
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver}")
    private String dbDriver;
    @Value("${datasource.scope}")
    private String scope;
    @Value("${aad.identity.name}")
    private String managedIdentityName;
    @Value("${spring.profiles.active}")

    private String profile;
    private static final Logger log = LoggerFactory.getLogger(DbConfig.class);

    @Bean
    @Primary
    public DataSource dataSource() {
        TokenCredential credential;
        if (!profile.equals("local")) {
            credential = new DefaultAzureCredentialBuilder().build(); // or VisualStudioCodeCredential()

            AccessToken token = credential.getToken(new TokenRequestContext().addScopes(scope)).block();
            String[] parts = token.getToken().split("\\.");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            log.info(">>>> Building DataSource bean (stderr) <<<<");
            log.info(token.getToken());
            log.info(payloadJson);
            // Use MI in Azure
            return DataSourceBuilder.create()
                    .driverClassName(dbDriver)
                    .url(dbUrl)
                    .username(dbUsername)
                    .password(token.getToken())
                    .build();
        } else {
            return DataSourceBuilder.create()
                    .driverClassName(dbDriver)
                    .url(dbUrl)
                    .username(dbUsername)
                    .password(dbPassword)
                    .build();
        }
    }

    @Bean
    public JpaTransactionManager transactionManager() {
        return new JpaTransactionManager();
    }
}

