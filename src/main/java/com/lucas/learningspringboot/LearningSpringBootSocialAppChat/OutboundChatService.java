package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@EnableBinding(ChatServiceStreams.class)
public class OutboundChatService  implements WebSocketHandler {
	
	@StreamListener(ChatServiceStreams.BROKER_TO_CLIENT)
	public void listen(String message) {
		
	}
	
	@Override
	public Mono<Void> handle(WebSocketSession session) {
		return Mono.empty();
	}
}
