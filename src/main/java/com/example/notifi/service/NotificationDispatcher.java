package com.example.notifi.service;

import com.example.notifi.model.Notification;
import com.example.notifi.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@Profile("api")
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final NotificationRepository repository;
    private final NotificationPublisher publisher;
    private final Clock clock;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void dispatch() {
        List<Notification> pendingNotifications = repository.lockDueNotifications(Instant.now(clock),50);
        for (Notification notification : pendingNotifications) {
            log.info(" przysla wiadomosc: {}", notification.getContent());
        }
    }
}
