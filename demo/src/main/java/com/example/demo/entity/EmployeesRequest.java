package com.example.demo.entity;

import com.example.demo.dto.CreateEmployeeRequest;
import lombok.Data;

import java.util.List;

@Data
public class EmployeesRequest {
    List<CreateEmployeeRequest> employees;
    Boolean isProcessed;

    String statusUrl;
}
