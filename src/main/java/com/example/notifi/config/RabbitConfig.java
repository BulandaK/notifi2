package com.example.notifi.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue email(){
        return QueueBuilder.durable("email").quorum().build();
    }

    @Bean
    public Queue push() {
        return QueueBuilder.durable("push").quorum().build();
    }

}
