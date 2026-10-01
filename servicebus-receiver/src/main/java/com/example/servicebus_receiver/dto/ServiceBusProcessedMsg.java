package com.example.servicebus_receiver.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;
@Data
@Builder
public class ServiceBusProcessedMsg {
    UUID employeeId;
    Boolean isProcessed;
}
