package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

@Service
@EnableBinding(ChatServiceStreams.class)
public class OutboundChatService  extends UserParsingHandshakeHandler {
	
	private final static Logger log = LoggerFactory.getLogger(OutboundChatService.class);
	
	protected Flux<Message<String>> flux;
	private FluxSink<Message<String>> chatMessageSink;
	
	public OutboundChatService() {
		this.flux = Flux.<Message<String>>create(
				emitter -> this.chatMessageSink = emitter, FluxSink.OverflowStrategy.IGNORE)
			.publish()
			.autoConnect();
	}
	
	@StreamListener(ChatServiceStreams.BROKER_TO_CLIENT)
	public void listen(Message<String> message) {
		if (chatMessageSink != null) {
			log.info("Publishing " + message + " to web socket.");
			chatMessageSink.next(message);
		}
	}
	
	@Override
	public Mono<Void> handleInternal(WebSocketSession session) {
		return session.send(
				this.flux
					.filter(message -> validate(message, getUser(session.getId())))
					.map(this::transform)
					.map(session::textMessage)
					.log("Outbound: Wrap as websocket message"))
				.log("Outbound: Publish to websocket");
	}
	
	private boolean validate(Message<String> message, String user) {
		if (message.getPayload().startsWith("@")) {
			String targetUser = message.getPayload().substring(1, message.getPayload().indexOf(" "));
			String sender = message.getHeaders().get(ChatServiceStreams.USER_HEADER, String.class);
			
			return user.equals(targetUser) || user.equals(sender);
		} else {
			return true;
		}
	}
	
	private String transform(Message<String> message) {
		String user = message.getHeaders().get(ChatServiceStreams.USER_HEADER, String.class);
		if (message.getPayload().startsWith("@")) {
			return "(" + user + "): " + message.getPayload();
		} else {
			return "(" + user + ") (all): " + message.getPayload();
		}
		
	}
}
