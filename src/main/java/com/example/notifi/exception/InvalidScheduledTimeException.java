package com.example.notifi.exception;

import org.springframework.http.HttpStatus;

public class InvalidScheduledTimeException extends NotifiException {
    public InvalidScheduledTimeException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
