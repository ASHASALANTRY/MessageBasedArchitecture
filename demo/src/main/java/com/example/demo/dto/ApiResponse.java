package com.example.demo.dto;

import lombok.Data;

@Data
public class ApiResponse {
    Object Data;
    Exception Error;
}
