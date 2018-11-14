package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class LearningSpringBootSocialAppChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearningSpringBootSocialAppChatApplication.class, args);
	}
}
