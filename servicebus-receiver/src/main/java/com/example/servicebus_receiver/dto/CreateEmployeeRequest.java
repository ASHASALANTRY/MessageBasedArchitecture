package com.example.servicebus_receiver.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class CreateEmployeeRequest {

    private String firstName;

    private String lastName;

    private Character gender; // Could be replaced with Enum later

    private Timestamp dob;

    private String email;

    private String phone;
    private Boolean isProcessed;
}
