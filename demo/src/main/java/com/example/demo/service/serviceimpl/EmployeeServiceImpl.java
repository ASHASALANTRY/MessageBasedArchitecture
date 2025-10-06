package com.example.demo.service.serviceimpl;

import com.example.demo.Repository.BasicDetailRepository;
import com.example.demo.dto.StatusResponse;
import com.example.demo.entity.BasicDetails;
import com.example.demo.dto.EmployeesRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.CacheOperationException;
import com.example.demo.exception.IntegrationException;
import com.example.demo.service.CacheHelperService;
import com.example.demo.service.EmployeeService;
import com.example.demo.service.QueueHelperService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final BasicDetailRepository basicDetailRepository;
    private final QueueHelperService queueHelperService;
    private final CacheHelperService cacheHelperService;
    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);
    @Value("${redis.status.url}")
    private String statusUrl;

    @Override
    public BasicDetails getEmployeeDetails(UUID employeeId) {

        BasicDetails basicDetails = basicDetailRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new BadRequestException("Employee not found with this employeeId: " + employeeId));
        return basicDetails;
    }

    @Override
    public EmployeesRequest addEmployeeDetails(EmployeesRequest employeesRequest) {
        if (Objects.isNull(employeesRequest)) {
            throw new BadRequestException("Request body cannot be null.");
        }
        if (Objects.isNull(employeesRequest.getEmployees()) || CollectionUtils.isEmpty(employeesRequest.getEmployees())) {
            throw new BadRequestException("Employees list cannot be empty.");
        }
        try {
            final String key = UUID.randomUUID().toString().replace("-", "");
            cacheHelperService.addMessageStatusToCache(key, "sent for processing");
            employeesRequest.setIsProcessed(false);
            employeesRequest.setStatusUrl(statusUrl.concat(key));
            queueHelperService.sendStatusMessageToQueue(employeesRequest, key);
            ObjectMapper mapper = new ObjectMapper();
            return employeesRequest;
        } catch (Exception exception) {
            if (exception instanceof IntegrationException)
                throw exception;
            if (exception instanceof CacheOperationException)
                throw exception;
            throw new BadRequestException("Unexpected error while processing employee details", exception);
        }
    }

    @Override
    public StatusResponse getStatus(String key) {
        try {
            String status = cacheHelperService.getDataFromCache(key);
            StatusResponse statusResponse = new StatusResponse();
            statusResponse.setStatus(status);
            return statusResponse;
        } catch (Exception exception) {
            if (exception instanceof CacheOperationException)
                throw exception;
            throw new BadRequestException("Unexpected error while fetching data from cache", exception);
        }
    }
}
