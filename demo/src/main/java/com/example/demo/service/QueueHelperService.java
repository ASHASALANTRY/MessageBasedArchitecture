package com.example.demo.service;

import com.example.demo.entity.BasicDetails;
import com.example.demo.entity.EmployeesRequest;

public interface QueueHelperService {
    void sendEmployeeDetailsToQueue(BasicDetails basicDetails);

    void sendStatusMessageToQueue(EmployeesRequest employeesRequest, String key);
}
