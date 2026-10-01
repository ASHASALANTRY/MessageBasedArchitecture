package com.example.servicebus_receiver.config;


import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Configuration
public class ServiceBusConfig {
    @Value("${spring.cloud.azure.servicebus.connection-string}")
    private String serviceBusConnection;
    @Value("${servicebus.sender.namespace}")
    private String namespace;
    @Value("${spring.profiles.active}")
    private String profile;

    @Bean
    ServiceBusClientBuilder serviceBusConfiguration() {
        // create a token using the default Azure credential
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                .build();

        if (namespace == null || namespace.isBlank()) {
            throw new IllegalStateException("service bus namespace not found");
        }
        if (profile.equals("local"))
            return new ServiceBusClientBuilder().connectionString(serviceBusConnection);

        return new ServiceBusClientBuilder().fullyQualifiedNamespace(namespace)
                .credential(credential);

    }
    @Bean(destroyMethod = "close")
        // ensures it closes on app shutdown
    ServiceBusSenderClient processedSender(ServiceBusClientBuilder builder) {
        return builder.sender().queueName("processedqueue").buildClient();
    }
}
