package com.example.notifi.worker;

import com.example.notifi.config.RabbitConfig;
import com.example.notifi.dto.NotificationMessageDto;
import com.example.notifi.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("push-worker")
@RequiredArgsConstructor
public class PushWorker {

    private final DeliveryService deliveryService;

    @RabbitListener(queues = RabbitConfig.PUSH_QUEUE)
    public void handle(NotificationMessageDto message) {
        deliveryService.deliver(message);
    }
}
