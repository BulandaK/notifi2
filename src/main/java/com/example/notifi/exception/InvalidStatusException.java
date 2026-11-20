package com.example.notifi.exception;

import org.springframework.http.HttpStatus;

public class InvalidStatusException extends NotifiException {
    public InvalidStatusException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
