package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import com.lucas.learningspringboot.LearningSpringBootSocialAppChat.comments.CommentService;

@Configuration
public class WebSocketConfig {
	
	@Bean
	public HandlerMapping webSocketMapping(CommentService commentService) {
		Map<String, WebSocketHandler> urlMap = new HashMap<>();
		urlMap.put("/topic/comments.new", commentService);
		
		Map<String, CorsConfiguration> corsConfigurationMap = new HashMap<>();
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.addAllowedOrigin("http://localhost:8080");
		corsConfigurationMap.put("/topic/comments.new", corsConfiguration);
		
		SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
		mapping.setOrder(10);
		mapping.setUrlMap(urlMap);
		mapping.setCorsConfigurations(corsConfigurationMap);
		
		return mapping;
	}
	
	@Bean
	public WebSocketHandlerAdapter handlerAdapter() {
		return new WebSocketHandlerAdapter();
	}
}
