package com.example.servicebus_receiver.config;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;

import javax.sql.DataSource;
import java.util.Base64;

@Configuration
public class DbConfig {
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

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
//
//        boolean runningInAzure = System.getenv("MSI_ENDPOINT") != null
//                || System.getenv("IDENTITY_ENDPOINT") != null
//                || System.getenv("WEBSITE_INSTANCE_ID") != null;
        TokenCredential credential;
/*        if (!profile.equals("local")) {
            // Use MI in Azure
            credential = new ManagedIdentityCredentialBuilder().build();
        } else {*/
        // Use your dev identity locally
//        }
//         credential=new DefaultAzureCredentialBuilder().build();


        if (!profile.equals("local")) {
            credential = new DefaultAzureCredentialBuilder().build(); // or VisualStudioCodeCredential()

            AccessToken token=credential.getToken(new TokenRequestContext().addScopes(scope)).block();
            String[] parts = token.getToken().split("\\.");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            log.info(">>>> Building DataSource bean (stderr) <<<<");
            log.info(token.getToken());
            log.info(payloadJson);
            // Use MI in Azure
//            credential = new ManagedIdentityCredentialBuilder().build();
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
                    .password("Vipul@1234")
                    .build();
        }


    }

//    @Bean
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
//        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
//        em.setPackagesToScan("com.example.demo.entity"); // Your entity package
//        return em;
//    }

    @Bean
    public JpaTransactionManager transactionManager() {
        return new JpaTransactionManager();
    }
}

