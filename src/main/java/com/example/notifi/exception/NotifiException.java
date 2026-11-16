package com.example.notifi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class NotifiException extends RuntimeException {
    private final HttpStatus status;

    NotifiException(String message,HttpStatus status){
        super(message);
        this.status = status;
    }
}
