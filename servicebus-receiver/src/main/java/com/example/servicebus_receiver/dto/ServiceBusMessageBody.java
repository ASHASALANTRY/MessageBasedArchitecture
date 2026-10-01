package com.example.servicebus_receiver.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ServiceBusMessageBody implements Serializable {
    List<CreateEmployeeRequest> employees;
    Boolean isProcessed;
    String redisStatusKey;
}
