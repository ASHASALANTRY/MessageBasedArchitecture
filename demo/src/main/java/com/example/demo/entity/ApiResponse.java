package com.example.demo.entity;

import lombok.Data;

@Data
public class ApiResponse {
    Object Data;
    Exception Error;
}
