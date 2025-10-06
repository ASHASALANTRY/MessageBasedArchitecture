package com.example.demo.service;

import com.example.demo.dto.StatusResponse;
import com.example.demo.entity.BasicDetails;
import com.example.demo.entity.EmployeesRequest;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeService {
    public BasicDetails getEmployeeDetails(UUID employeeId);

    public EmployeesRequest addEmployeeDetails(EmployeesRequest employeesRequest);

    public StatusResponse getStatus(String key);
}
