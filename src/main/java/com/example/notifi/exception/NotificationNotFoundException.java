package com.example.notifi.exception;

import org.springframework.http.HttpStatus;

public class NotificationNotFoundException extends NotifiException {
    public NotificationNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
