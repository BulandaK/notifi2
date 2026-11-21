package com.example.notifi.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "notification.direct";
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String PUSH_QUEUE = "push.queue";

    public static final String DLX = "notification.dlx";
    public static final String DLQ = "notification.dlq";

    public static final int MAX_ATTEMPTS = 3;

    @Bean
    public DirectExchange direct() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange dlx() {
        return new DirectExchange(DLX);
    }

    @Bean
    public Queue emailQueue() {
        return channelQueue(EMAIL_QUEUE);
    }

    @Bean
    public Queue pushQueue() {
        return channelQueue(PUSH_QUEUE);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).quorum().build();
    }

    @Bean
    public Binding bindingEmail(DirectExchange direct, Queue emailQueue) {
        return BindingBuilder.bind(emailQueue).to(direct).with("EMAIL");
    }

    @Bean
    public Binding bindingPush(DirectExchange direct, Queue pushQueue) {
        return BindingBuilder.bind(pushQueue).to(direct).with("PUSH");
    }

    @Bean
    public Binding bindingDeadLetter(DirectExchange dlx, Queue deadLetterQueue) {
        return BindingBuilder.bind(deadLetterQueue).to(dlx).with(DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    private static Queue channelQueue(String name) {
        return QueueBuilder.durable(name)
                .quorum()
                .deliveryLimit(MAX_ATTEMPTS)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DLQ)
                .build();
    }
}
