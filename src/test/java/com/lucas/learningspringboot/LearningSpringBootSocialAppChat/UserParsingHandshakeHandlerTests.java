package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Mono;

public class UserParsingHandshakeHandlerTests {
	
	public static final String SESSION_ID = "2asd3";
	public static final String USER = "Marvin";
	
	private WebSocketSession webSocketSession;
	TestUserParsingHandshakeHandler userParsingHandshakeHandler;
	
	@Before
	public void setup() {
		userParsingHandshakeHandler = new TestUserParsingHandshakeHandler();
		setupMocks();
	}
	
	@Test
	public void parsesUsernameFromUri() {
		userParsingHandshakeHandler.handle(webSocketSession);
		
		assertThat(userParsingHandshakeHandler.getUser(SESSION_ID)).isEqualTo(USER);
	}
	
	@Test
	public void delegatesSessionHandlingToChildClasses() {
		userParsingHandshakeHandler.handle(webSocketSession).block();
		
		assertThat(userParsingHandshakeHandler.hasDelegated()).isTrue();
	}
	
	public class TestUserParsingHandshakeHandler extends UserParsingHandshakeHandler {
		
		private boolean delegated = false;
		
		public boolean hasDelegated() {
			return delegated;
		}
		
		@Override
		protected Mono<Void> handleInternal(WebSocketSession session) {
			return Mono.fromCallable(() -> {
				delegated = true;
				return null;
			});
		}
	}
	
	private void setupMocks() {
		webSocketSession = mock(WebSocketSession.class);
		HandshakeInfo handshakeInfo = mock(HandshakeInfo.class);
		URI uri = URI.create("ws://localhost:8200/topic/chatMessage.new?user=" + USER);
		when(webSocketSession.getHandshakeInfo()).thenReturn(handshakeInfo);
		when(webSocketSession.getId()).thenReturn(SESSION_ID);
		when(handshakeInfo.getUri()).thenReturn(uri);
	}
}
