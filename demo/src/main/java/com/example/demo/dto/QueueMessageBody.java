package com.example.demo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
public class QueueMessageBody implements Serializable {
    List<CreateEmployeeRequest> employees;
    Boolean isProcessed;
    String redisStatusKey;
}
