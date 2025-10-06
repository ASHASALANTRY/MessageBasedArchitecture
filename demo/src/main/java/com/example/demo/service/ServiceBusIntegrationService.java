package com.example.demo.service;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.Request;
import com.example.demo.dto.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;

import java.util.List;

public interface ServiceBusIntegrationService {
    public ResponseEntity<ApiResponse>  externalApiCall(String city) throws JsonProcessingException;
//    public List<String> peek(int max) ;

    }
