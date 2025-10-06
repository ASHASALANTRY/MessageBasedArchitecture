package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class IntegrationException extends RuntimeException{
    public IntegrationException(String message){
        super(message);
    }
    public IntegrationException(String message, Throwable cause){
        super(message,cause);
    }
}
