package com.example.notifi.worker;

import com.example.notifi.config.RabbitConfig;
import com.example.notifi.dto.NotificationMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("push-worker")
public class PushWorker {

    @RabbitListener(queues = RabbitConfig.PUSH_QUEUE)
    public void handle(NotificationMessageDto message) {
        log.info("PUSH WORKER-----------------------");
        log.info("przyszło z kolejki o id - {} - {}", message.id(), message.content());
    }
}
