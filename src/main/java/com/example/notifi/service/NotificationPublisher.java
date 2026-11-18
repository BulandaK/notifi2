package com.example.notifi.service;

import com.example.notifi.dto.NotificationMessageDto;
import com.example.notifi.exception.NotificationNotFoundException;
import com.example.notifi.mapper.NotificationMapper;
import com.example.notifi.model.Channel;
import com.example.notifi.model.Notification;
import com.example.notifi.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final DirectExchange directExchange;
    private final NotificationRepository notificationRepository;

    public void publish(Long id) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));

        NotificationMessageDto messageDto = new NotificationMessageDto(notification.getId(),notification.getContent());
        Channel channel = notification.getChannel();

        rabbitTemplate.convertAndSend(directExchange.getName(),channel.toString(), messageDto);
    }
}
