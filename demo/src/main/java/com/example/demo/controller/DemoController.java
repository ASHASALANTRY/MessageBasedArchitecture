package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.Request;
import com.example.demo.dto.Response;
import com.example.demo.service.ServiceBusIntegrationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class DemoController {
    private final ServiceBusIntegrationService serviceBusIntegrationService;
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping(value = "/hello")
    public ResponseEntity<ApiResponse> helloController(@RequestParam(name="city") String city) throws JsonProcessingException {
        return serviceBusIntegrationService.externalApiCall(city);
    }

//    @GetMapping("/peek")
//    public List<String> peek(@RequestParam(name="max",defaultValue = "5") int max) {
//        return serviceBusIntegrationService.peek(max);
//    }
    }
