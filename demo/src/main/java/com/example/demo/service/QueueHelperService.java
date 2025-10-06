package com.example.demo.service;

import com.example.demo.dto.EmployeesRequest;

public interface QueueHelperService {
    void sendStatusMessageToQueue(EmployeesRequest employeesRequest, String key);
}
