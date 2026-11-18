package com.example.notifi.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class RabbitConfig {

    public static final String EMAIL_QUEUE = "email.queue";
    public static final String PUSH_QUEUE = "push.queue";

    @Bean
    public DirectExchange direct() {
        return new DirectExchange("notification.direct");
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE).quorum().build();
    }

    @Bean
    public Queue pushQueue() {
        return QueueBuilder.durable(PUSH_QUEUE).quorum().build();
    }

    @Bean
    public Binding bindingEmail(DirectExchange direct, Queue emailQueue) {
        return BindingBuilder.bind(emailQueue)
                .to(direct)
                .with("EMAIL");
    }

    @Bean
    public Binding bindingPush(DirectExchange direct, Queue pushQueue) {
        return BindingBuilder.bind(pushQueue)
                .to(direct)
                .with("PUSH");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

}
