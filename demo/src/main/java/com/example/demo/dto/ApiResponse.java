package com.example.demo.dto;

import lombok.Data;

@Data
public class ApiResponse {
    Response response;
    String errorMessage;
}
