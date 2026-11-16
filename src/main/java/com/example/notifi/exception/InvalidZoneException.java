package com.example.notifi.exception;

import org.springframework.http.HttpStatus;

public class InvalidZoneException extends NotifiException {
    public InvalidZoneException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
