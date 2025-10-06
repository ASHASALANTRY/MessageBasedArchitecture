package com.example.demo.controller;

import com.example.demo.entity.ApiResponse;
import com.example.demo.entity.BasicDetails;
import com.example.demo.entity.EmployeesRequest;
import com.example.demo.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Tag(name = "Employee Details", description = "Employee Details")
@RequiredArgsConstructor
@RestController
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping("/get-employee-details")
    @Operation(summary = "get employee details", description = "fetch employee details", tags = {"Employee Details"})
    @ApiResponses(value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "employee details received")})
    public ResponseEntity<ApiResponse> getEmployeeDetails(@RequestParam(name = "employeeId") UUID employeeId) {
        ApiResponse apiResponse=new ApiResponse();
        BasicDetails basicDetails=employeeService.getEmployeeDetails(employeeId);
            apiResponse.setData(basicDetails);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

    }
    @PostMapping("/add-employee")
    @Operation(summary = "add employee details", description = "save employee details and update status in cache", tags = {"Employee Details"})
    @ApiResponses(value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "employee details stored successfully")})
    public ResponseEntity<ApiResponse> addEmployeeDetails(@RequestBody EmployeesRequest employees){
        ApiResponse apiResponse =new ApiResponse();
        apiResponse.setData(employeeService.addEmployeeDetails(employees));

        return new ResponseEntity<>(apiResponse,HttpStatus.ACCEPTED);
    }


    @GetMapping("/get-status")
    @Operation(summary = "get status", description = "fetch status from cache", tags = {"Employee Details"})
    @ApiResponses(value = {@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "status received")})
    public ResponseEntity<ApiResponse> getStatus(@RequestParam(name = "key") String key){
        ApiResponse apiResponse =new ApiResponse();
        apiResponse.setData(employeeService.getStatus(key));

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

}
