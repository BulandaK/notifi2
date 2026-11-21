package com.example.notifi.service;

public class DeliveryFailedException extends RuntimeException {
    public DeliveryFailedException(String message) {
        super(message);
    }
}
