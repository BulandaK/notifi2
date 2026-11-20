package com.example.notifi.service;

import com.example.notifi.dto.NotificationMessageDto;
import com.example.notifi.model.Channel;
import com.example.notifi.model.Notification;
import com.example.notifi.model.NotificationStatus;
import com.example.notifi.model.Priority;
import com.example.notifi.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class DeliveryServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-17T12:00:00Z");
    private static final NotificationMessageDto MESSAGE = new NotificationMessageDto(1L, "hej", Priority.HIGH);

    NotificationRepository repository;
    Notification notification;

    @BeforeEach
    void setup() {
        repository = Mockito.mock(NotificationRepository.class);
        notification = new Notification();
        notification.setId(1L);
        notification.setChannel(Channel.EMAIL);
        notification.setStatus(NotificationStatus.QUEUED);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(notification));
    }

    private DeliveryService service(double failureRate) {
        return new DeliveryService(repository, Clock.fixed(NOW, ZoneOffset.UTC), failureRate, () -> 0.5);
    }

    @Test
    void deliver_Queued_MarkedSent() {
        service(0.0).deliver(MESSAGE);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getSentAt()).isEqualTo(NOW);
        assertThat(notification.getAttempts()).isEqualTo(1);
    }

    @Test
    void deliver_Duplicate_Skipped() {
        DeliveryService service = service(0.0);
        service.deliver(MESSAGE);
        service.deliver(MESSAGE); // ta sama wiadomość drugi raz

        assertThat(notification.getAttempts()).isEqualTo(1);
    }

    @Test
    void deliver_Cancelled_Skipped() {
        notification.setStatus(NotificationStatus.CANCELLED);

        service(0.0).deliver(MESSAGE);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.CANCELLED);
        assertThat(notification.getAttempts()).isZero();
    }

    @Test
    void deliver_AlwaysFailing_ThreeAttemptsThenFailed() {
        DeliveryService service = service(1.0);

        assertThatThrownBy(() -> service.deliver(MESSAGE)).isInstanceOf(DeliveryFailedException.class);
        assertThatThrownBy(() -> service.deliver(MESSAGE)).isInstanceOf(DeliveryFailedException.class);
        service.deliver(MESSAGE); // trzecia próba: brak wyjątku, wiadomość zostaje zatwierdzona (ack)

        assertThat(notification.getAttempts()).isEqualTo(3);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }
}
