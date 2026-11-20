package com.example.notifi.service;

import com.example.notifi.config.RabbitConfig;
import com.example.notifi.dto.NotificationMessageDto;
import com.example.notifi.model.Notification;
import com.example.notifi.model.NotificationStatus;
import com.example.notifi.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.function.DoubleSupplier;


@Slf4j
@Service
public class DeliveryService {

    private final NotificationRepository repository;
    private final Clock clock;
    private final double failureRate;
    private final DoubleSupplier random;

    @Autowired
    public DeliveryService(NotificationRepository repository,
                           Clock clock,
                           @Value("${notifi.simulation.failure-rate:0.0}") double failureRate) {
        this(repository, clock, failureRate, Math::random);
    }

    DeliveryService(NotificationRepository repository, Clock clock, double failureRate, DoubleSupplier random) {
        this.repository = repository;
        this.clock = clock;
        this.failureRate = failureRate;
        this.random = random;
    }

    @Transactional(noRollbackFor = DeliveryFailedException.class)
    public void deliver(NotificationMessageDto message) {
        Optional<Notification> found = repository.findByIdForUpdate(message.id());
        if (found.isEmpty()) {
            log.warn("Powiadomienie {} nie istnieje, pomijam", message.id());
            return;
        }
        Notification notification = found.get();
        if (notification.getStatus() != NotificationStatus.QUEUED) {
            log.info("Powiadomienie {} ma status {}, pomijam (duplikat lub anulowane)",
                    notification.getId(), notification.getStatus());
            return;
        }

        notification.setAttempts(notification.getAttempts() + 1);

        if (random.getAsDouble() < failureRate) {
            if (notification.getAttempts() >= RabbitConfig.MAX_ATTEMPTS) {
                notification.setStatus(NotificationStatus.FAILED);
                log.error("Powiadomienie {}: próba {}/{} nieudana, status FAILED",
                        notification.getId(), notification.getAttempts(), RabbitConfig.MAX_ATTEMPTS);
                return;
            }
            log.warn("Powiadomienie {}: próba {}/{} nieudana, ponawiam",
                    notification.getId(), notification.getAttempts(), RabbitConfig.MAX_ATTEMPTS);
            throw new DeliveryFailedException("Symulowany błąd dostarczenia " + notification.getId());
        }

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(Instant.now(clock));
        log.info("[{}] Dostarczono powiadomienie {} do {} (priorytet {}, próba {}): {}",
                notification.getChannel(), notification.getId(), notification.getRecipient(),
                notification.getPriority(), notification.getAttempts(), notification.getContent());
    }
}
