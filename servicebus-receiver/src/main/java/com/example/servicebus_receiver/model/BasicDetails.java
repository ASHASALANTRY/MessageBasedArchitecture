package com.example.servicebus_receiver.model;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;
@Builder
@Entity
@Table(name = "basic_details",  schema = "employee")
@Data
public class BasicDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "employeeid", unique = true, nullable = false)
    private UUID employeeId;

    @Column(name = "firstname", nullable = false, length = 50)
    private String firstName;

    @Column(name = "lastname", nullable = false, length = 50)
    private String lastName;

    @Column(name = "gender", nullable = false, length = 1)
    private Character gender; // Could be replaced with Enum later

    @Column(name = "dob", nullable = false)
    private Timestamp dob;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;
    @Column(name="is_processed")
    private Boolean isProcessed;
}

