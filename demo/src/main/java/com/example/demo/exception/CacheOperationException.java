package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class CacheOperationException extends RuntimeException{
    public CacheOperationException(String message){
        super(message);
    }
    public CacheOperationException(String message,Throwable cause){
        super(message,cause);
    }

}
