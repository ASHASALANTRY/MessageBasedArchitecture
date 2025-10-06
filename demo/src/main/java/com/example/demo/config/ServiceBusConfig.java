package com.example.demo.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.example.demo.exception.IntegrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class ServiceBusConfig {
    @Value("${servicebus.connection}")
    private String serviceBusConnection;
    @Value("${servicebus.namespace}")
    private String namespace;
    @Value("${spring.profiles.active}")
    private String profile;

    @Bean
    ServiceBusClientBuilder serviceBusConfiguration() {
        // create a token using the default Azure credential
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                .build();
        if (namespace == null || namespace.isBlank()) {
            throw new IntegrationException("service bus namespace not found");
        }
        if (profile.equals("local"))
            return new ServiceBusClientBuilder().connectionString(serviceBusConnection);
        return new ServiceBusClientBuilder().fullyQualifiedNamespace(namespace)
                .credential(credential);

    }

}
