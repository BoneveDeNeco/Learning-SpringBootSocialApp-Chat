package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.reactivestreams.Publisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class OutboundChatServiceTests {
	
	private static final String MESSAGE_TO_ALL = "Hi all!";
	private static final String CARL_SESSION_ID = "3";
	private static final String BOB_SESSION_ID = "2";
	private static final String ALICE_SESSION_ID = "1";
	private static final String MESSAGE_TO_CARL = "@Carl Hello";
	private static final String MESSAGE_TO_BOB = "@Bob Hi";
	private static final String MESSAGE_TO_ALICE = "@Alice Hello";
	TestOutboundChatService outboundChatService;
	
	@Before
	public void setup() {
		outboundChatService = new TestOutboundChatService();
		outboundChatService.setUser(ALICE_SESSION_ID, "Alice");
		outboundChatService.setUser(BOB_SESSION_ID, "Bob");
		outboundChatService.setUser(CARL_SESSION_ID, "Carl");
	}
	
	@Test
	public void filtersOutMessagesTargetedToOtherUsers() {
		WebSocketSession session = getMockWebSocketSession();
		outboundChatService.setMessageFlux(getIncomingMessages());
		
		outboundChatService.handleInternal(session).block();
		
		List<WebSocketMessage> messageList = getSentMessages(session);
		assertThat(messageList.size()).isEqualTo(1);
		assertThat(messageList.get(0).getPayloadAsText()).contains(MESSAGE_TO_ALICE);
	}
	
	@Test
	public void sendTargetedMessageBackToSender() {
		WebSocketSession session = getMockWebSocketSession();
		Flux<Message<String>> flux = Flux.just(
				MessageBuilder.withPayload(MESSAGE_TO_BOB)
					.setHeader(ChatServiceStreams.USER_HEADER, "Carl").build(),
				MessageBuilder.withPayload(MESSAGE_TO_CARL)
					.setHeader(ChatServiceStreams.USER_HEADER, "Alice").build());
		outboundChatService.setMessageFlux(flux);
		
		outboundChatService.handleInternal(session);
		
		List<WebSocketMessage> messageList = getSentMessages(session);
		assertThat(messageList.size()).isEqualTo(1);
		assertThat(messageList.get(0).getPayloadAsText()).contains(MESSAGE_TO_CARL);
	}
	
	@Test
	public void appendsSenderNameInTargetedMessages() {
		WebSocketSession session = getMockWebSocketSession();
		outboundChatService.setMessageFlux(getIncomingMessages());
		
		outboundChatService.handleInternal(session).block();
		
		List<WebSocketMessage> messageList = getSentMessages(session);
		assertThat(messageList.get(0).getPayloadAsText()).isEqualTo("(Bob): " + MESSAGE_TO_ALICE);
	}
	
	@Test
	public void marksMessagesSentToAll() {
		WebSocketSession session = getMockWebSocketSession();
		Flux<Message<String>> flux = Flux.just(
				MessageBuilder.withPayload(MESSAGE_TO_ALL).setHeader(ChatServiceStreams.USER_HEADER, "Alice").build());
		outboundChatService.setMessageFlux(flux);
		
		outboundChatService.handleInternal(session);
		
		assertThat(getSentMessages(session).get(0).getPayloadAsText()).isEqualTo("(Alice) (all): " + MESSAGE_TO_ALL);
	}
	
	private WebSocketSession getMockWebSocketSession() {
		WebSocketSession session = mock(WebSocketSession.class);
		when(session.send(any())).thenReturn(Mono.empty());
		when(session.getId()).thenReturn(ALICE_SESSION_ID);
		when(session.textMessage(anyString())).thenAnswer(new Answer<WebSocketMessage>() {
			@Override
			public WebSocketMessage answer(InvocationOnMock invocation) throws Throwable {
				String message = invocation.getArgument(0);
				WebSocketMessage mockedMessage = mock(WebSocketMessage.class);
				when(mockedMessage.getPayloadAsText()).thenReturn(message);
				return mockedMessage;
			}
		});
		return session;
	}
	
	private Flux<Message<String>> getIncomingMessages() {
		return Flux.just(
				MessageBuilder.withPayload(MESSAGE_TO_BOB)
				.setHeader(ChatServiceStreams.USER_HEADER, "Carl").build(),
			MessageBuilder.withPayload(MESSAGE_TO_ALICE)
				.setHeader(ChatServiceStreams.USER_HEADER, "Bob").build());
	}
	
	private List<WebSocketMessage> getSentMessages(WebSocketSession session) {
		ArgumentCaptor<Publisher<WebSocketMessage>> argumentCaptor = ArgumentCaptor.forClass(Publisher.class);
		verify(session).send(argumentCaptor.capture());
		Publisher<WebSocketMessage> messages = argumentCaptor.getValue();
		Flux<WebSocketMessage> messageFlux = Flux.from(messages);
		return messageFlux.collectList().block(Duration.ofSeconds(1));
	}
	
	public class TestOutboundChatService extends OutboundChatService {
		public void setMessageFlux(Flux<Message<String>> flux) {
			this.flux = flux;
		}
		
		public void setUser(String id, String user) {
			this.userMap.put(id, user);
		}
	}
}
