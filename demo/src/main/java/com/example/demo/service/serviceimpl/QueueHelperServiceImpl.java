package com.example.demo.service.serviceimpl;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.example.demo.dto.QueueMessageBody;
import com.example.demo.entity.BasicDetails;
import com.example.demo.entity.EmployeesRequest;
import com.example.demo.exception.IntegrationException;
import com.example.demo.service.QueueHelperService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
@Service
@RequiredArgsConstructor
public class QueueHelperServiceImpl implements QueueHelperService {
    private final ServiceBusClientBuilder serviceBusClientBuilder;
    @Value("${servicebus.queue.sample}")
    private String sampleQueueName;
    @Value("${servicebus.queue.employeeprocess}")
    private String employeeProcessQueueName;
    private static final Logger log = LoggerFactory.getLogger(QueueHelperServiceImpl.class);

    @Override
    public void sendEmployeeDetailsToQueue(BasicDetails basicDetails) {
        sendMessageToQueue(sampleQueueName,basicDetails);
    }

    @Override
    public void sendStatusMessageToQueue(EmployeesRequest employeesRequest, String key) {
        QueueMessageBody messageBody = new QueueMessageBody();
        messageBody.setEmployees(employeesRequest.getEmployees());
        messageBody.setIsProcessed(employeesRequest.getIsProcessed());
        messageBody.setRedisStatusKey(key);
        sendMessageToQueue(employeeProcessQueueName,messageBody);
    }
    public void sendMessageToQueue(String queueName, Object payload){
        try (ServiceBusSenderClient sender = serviceBusClientBuilder
                .sender()
                .queueName(queueName)
                .buildClient()) {
            ObjectMapper mapper = new ObjectMapper();

            String jsonString = mapper.writeValueAsString(payload);
            log.debug("Compact JSON to publish ({}): {}", queueName, jsonString);
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(jsonString.getBytes(StandardCharsets.UTF_8));
            sender.sendMessage(serviceBusMessage);
        }catch (JsonProcessingException e) {
            throw new IntegrationException("Failed to serialize message body for Service Bus.", e);
        } catch (ServiceBusException e) {
            throw new IntegrationException("Failed to publish to Service Bus queue: " + employeeProcessQueueName, e);
        }

    }
}
