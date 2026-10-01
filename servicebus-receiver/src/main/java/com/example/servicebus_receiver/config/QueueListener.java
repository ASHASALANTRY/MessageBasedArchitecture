package com.example.servicebus_receiver.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.messaging.servicebus.implementation.core.annotation.ServiceBusListener;
import com.azure.spring.messaging.servicebus.support.ServiceBusMessageHeaders;
import com.example.servicebus_receiver.dto.ServiceBusMessageBody;
import com.example.servicebus_receiver.dto.ServiceBusProcessedMsg;
import com.example.servicebus_receiver.model.BasicDetails;
import com.example.servicebus_receiver.repository.BasicDetailsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class QueueListener {
    private final BasicDetailsRepository basicDetailsRepository;

    private final StatefulRedisConnection<String, String> connection;
    private final ServiceBusSenderClient serviceBusClient;
    private static final Logger log = LoggerFactory.getLogger(QueueListener.class);

    // Set SERVICEBUS_QUEUE as an App Setting in Azure, or hardcode the queue name here
    @ServiceBusListener(destination = "employeeprocessqueue")
    public void handle(String body, @Headers Map<String, Object> headers) throws Exception {

        String messageId = (String) headers.getOrDefault(ServiceBusMessageHeaders.MESSAGE_ID, "n/a");
        ObjectMapper objectMapper = new ObjectMapper();
        ServiceBusMessageBody serviceBusMessageBody = objectMapper.readValue(body, ServiceBusMessageBody.class);

        serviceBusMessageBody.getEmployees().stream().forEach(x -> {
            try {

                BasicDetails basicDetails = basicDetailsRepository.save(BasicDetails.builder().firstName(x.getFirstName())
                        .dob(x.getDob()).lastName(x.getLastName()).phone(x.getPhone()).
                        email(x.getEmail()).gender(x.getGender()).
                        isProcessed(Boolean.TRUE).build());
                //update processed queue with employee id of processed records
                ServiceBusProcessedMsg serviceBusProcessedMsg = ServiceBusProcessedMsg.builder().employeeId(basicDetails.getEmployeeId()).isProcessed(basicDetails.getIsProcessed()).build();
                String jsonString = "";
                jsonString = objectMapper.writeValueAsString(serviceBusProcessedMsg);
                ServiceBusMessage serviceBusMessage = new ServiceBusMessage(jsonString);
                serviceBusClient.sendMessage(serviceBusMessage);

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                String result = connection.sync().set(serviceBusMessageBody.getRedisStatusKey(), "failed", SetArgs.Builder.xx());
                if (result == null) {
                    throw new RuntimeException("Redis key does not exist");
                }
                log.warn("Some thing went wrong while updating record", e.getMessage());

            }
        });
        //Update Redis Cache
        String result = connection.sync().set(serviceBusMessageBody.getRedisStatusKey(), "processed", SetArgs.Builder.xx());
        if (result == null) {
            throw new RuntimeException("Redis key does not exist");
        }
    }

}
