package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Mono;

public abstract class UserParsingHandshakeHandler implements WebSocketHandler{
	
	private static final Logger logger = LoggerFactory.getLogger(UserParsingHandshakeHandler.class);

	protected final Map<String, String> userMap;
	
	public UserParsingHandshakeHandler() {
		userMap = new HashMap<>();
	}
	
	public Mono<Void> handle(WebSocketSession session) {
		userMap.put(session.getId(), 
				Stream.of(session.getHandshakeInfo().getUri().getQuery().split("&"))
				.map(s -> s.split("="))
				.filter(strings -> strings[0].equals("user"))
				.findFirst()
				.map(strings -> strings[1])
				.orElse(""));
		
		return handleInternal(session);
	}
	
	abstract protected Mono<Void> handleInternal(WebSocketSession session);
	
	public String getUser(String id) {
		return userMap.get(id);
	}
}
