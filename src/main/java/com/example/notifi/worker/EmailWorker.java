package com.example.notifi.worker;

import com.example.notifi.config.RabbitConfig;
import com.example.notifi.dto.NotificationMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailWorker {

    @RabbitListener(queues = RabbitConfig.EMAIL_QUEUE)
    public void handle(NotificationMessageDto message) {
        log.info("EMAIL WORKER-----------------------");
        log.info("przyszło z kolejki o id - {} - {}", message.id(), message.content());
    }
}
