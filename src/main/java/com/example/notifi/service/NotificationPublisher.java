package com.example.notifi.service;

import com.example.notifi.config.RabbitConfig;
import com.example.notifi.dto.NotificationMessageDto;
import com.example.notifi.model.Notification;
import com.example.notifi.model.Priority;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    static final int HIGH_PRIORITY = 5;
    static final int LOW_PRIORITY = 0;
    private static final long CONFIRM_TIMEOUT_MS = 5000;

    private final RabbitTemplate rabbitTemplate;


    public void publish(Notification notification) {
        NotificationMessageDto dto = new NotificationMessageDto(
                notification.getId(), notification.getContent(), notification.getPriority());
        int priority = notification.getPriority() == Priority.HIGH ? HIGH_PRIORITY : LOW_PRIORITY;

        rabbitTemplate.invoke(ops -> {
            ops.convertAndSend(RabbitConfig.EXCHANGE, notification.getChannel().name(), dto, message -> {
                message.getMessageProperties().setPriority(priority);
                return message;
            });
            ops.waitForConfirmsOrDie(CONFIRM_TIMEOUT_MS);
            return null;
        });
    }
}
