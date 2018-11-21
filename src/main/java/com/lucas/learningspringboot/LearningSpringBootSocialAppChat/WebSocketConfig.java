package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import com.lucas.learningspringboot.LearningSpringBootSocialAppChat.comments.CommentService;

@Configuration
public class WebSocketConfig {
	
	@Autowired
	Environment environment;
	
	@Bean
	public HandlerMapping webSocketMapping(CommentService commentService, 
			InboundChatService inboundChatService/*,
			OutboundChatServic outboundChatService*/) {
		
		Map<String, WebSocketHandler> urlMap = new HashMap<>();
		urlMap.put("/topic/comments.new", commentService);
		urlMap.put("/app/chatMessage.new", inboundChatService);
		//urlMap.put("/topic/chatMessage.new", outboundChatService);
		
		Map<String, CorsConfiguration> corsConfigurationMap = new HashMap<>();
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.addAllowedOrigin(getCorsAllowedOrigin());
		corsConfigurationMap.put("/topic/comments.new", corsConfiguration);
		corsConfigurationMap.put("/app/chatMessage.new", corsConfiguration);
		corsConfigurationMap.put("/topic/chatMessage.new", corsConfiguration);
		
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
	
	private String getCorsAllowedOrigin() {
		if (environment.acceptsProfiles("test")) {
			return "*";
		} else {
			return "http://localhost:8080";
		}
	}
}
