package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketMessage.Type;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Flux;
/**
 * These tests need too many mocks to work, so they don't look very clean. I think it's better to start off
 * with a Spring Integration test, then identify components and write unit tests for them.
 * I'm leaving these here for learning and illustration purposes.
 */
@Ignore
public class InboundChatServiceTest {
	
	InboundChatWebSocketHandler inboundChatService;
	ChatServiceStreams chatServiceStream;
	
	@Before
	public void setup() {
		chatServiceStream = mock(ChatServiceStreams.class);
		inboundChatService = new InboundChatWebSocketHandler(chatServiceStream);
	}
	
	@Test
	public void handlesWebsocketSession() {
		WebSocketSession session = mock(WebSocketSession.class);
		when(session.receive()).thenReturn(Flux.empty());
		inboundChatService.handle(session);
		
		verify(session).receive();
	}
	
	@Test
	public void broadcastsMessageToBroker() {
		SubscribableChannel clientToBrokerChannel = mock(SubscribableChannel.class);
		when(chatServiceStream.clientToBroker()).thenReturn(clientToBrokerChannel);
		
		WebSocketSession session = mock(WebSocketSession.class);
		DataBuffer buffer = mock(DataBuffer.class);
		when(buffer.readableByteCount()).thenReturn("Hello".length());
		when(session.receive()).thenReturn(Flux.just(new WebSocketMessage(Type.TEXT, buffer)));
		
		Message<String> message = MessageBuilder.withPayload("Hello").build();
		
		inboundChatService.handle(session).block();
		
		verify(clientToBrokerChannel).send(message);
	}
}
