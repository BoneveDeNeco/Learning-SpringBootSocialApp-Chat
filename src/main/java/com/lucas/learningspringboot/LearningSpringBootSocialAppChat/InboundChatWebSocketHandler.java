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
public class InboundChatWebSocketHandler implements WebSocketHandler {

	private ChatServiceStreams chatServiceStreams;
	
	public InboundChatWebSocketHandler(ChatServiceStreams chatServiceStreams) {
		this.chatServiceStreams = chatServiceStreams;
	}
	
	@Override
	public Mono<Void> handle(WebSocketSession session) {
		return session.receive()
				.log("Inbound: incoming chat message")
				.map(WebSocketMessage::getPayloadAsText)
				.log("Inbound: convert to text")
				.map(message -> session.getId() + ": " + message)
				.log("Inbound: mark with session id")
				.map(MessageBuilder::withPayload)
				.map(MessageBuilder::build)
				.flatMap(this::broadcast)
				.log("Inbound: broadcast to broker")
				.then();
	}
	
	private Mono<?> broadcast(Message<String> message) {
		return Mono.fromRunnable(() -> { 
			chatServiceStreams.clientToBroker().send(message);
		});
	}
	
}
