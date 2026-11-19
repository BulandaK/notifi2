package com.example.notifi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class NotifiApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotifiApplication.class, args);
	}

}
