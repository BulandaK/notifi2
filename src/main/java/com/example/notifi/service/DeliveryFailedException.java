package com.example.notifi.service;

/** Nieudana (symulowana) próba dostarczenia. Wyjątek dociera do listenera, więc RabbitMQ ponawia wiadomość. */
public class DeliveryFailedException extends RuntimeException {
    public DeliveryFailedException(String message) {
        super(message);
    }
}
