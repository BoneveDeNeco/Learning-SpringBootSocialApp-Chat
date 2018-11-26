package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@EnableBinding(ChatServiceStreams.class)
public class InboundChatService extends UserParsingHandshakeHandler {

	private ChatServiceStreams chatServiceStreams;
	
	public InboundChatService(ChatServiceStreams chatServiceStreams) {
		this.chatServiceStreams = chatServiceStreams;
	}
	
	@Override
	public Mono<Void> handleInternal(WebSocketSession session) {
		return session.receive()
				.log("Inbound: incoming chat message (" +getUser(session.getId())+ ")")
				.map(WebSocketMessage::getPayloadAsText)
				.log("Inbound: convert to text (" +getUser(session.getId())+ ")")
				.flatMap(message -> broadcast(message, getUser(session.getId())))
				.log("Inbound: broadcast to broker (" +getUser(session.getId())+ ")")
				.then();
	}
	
	private Mono<?> broadcast(String message, String username) {
		return Mono.fromRunnable(() -> { 
			chatServiceStreams.clientToBroker().send(
					MessageBuilder.withPayload(message)
						.setHeader(ChatServiceStreams.USER_HEADER, username)
						.build());
		});
	}
	
}
