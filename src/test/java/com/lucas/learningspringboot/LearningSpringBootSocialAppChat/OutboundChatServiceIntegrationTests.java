package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.time.Duration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.StandardWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OutboundChatServiceIntegrationTests {
	
	@Autowired
	ChatServiceStreams chatServiceStreams;
	
	@LocalServerPort
	private int port;
	
	@Test
	public void receivesMessageFromBroker() {
		assertThat(sendMessage()).isTrue();
	}
	
	@Test
	public void acceptsWebSocketConnectionFromClients() {
		TestWebSocketHandler testWebSocketHandler = connectToOutboundChatService();
		
		assertThat(testWebSocketHandler.session).isNotNull();
	}
	
	private boolean sendMessage() {
		return chatServiceStreams.brokerToClient().send(MessageBuilder.withPayload("Hello").build());
	}
	
	private TestWebSocketHandler connectToOutboundChatService() {
		WebSocketClient client = new StandardWebSocketClient();
		TestWebSocketHandler testWebSocketHandler = new TestWebSocketHandler();
		
		client.execute(URI.create("ws://localhost:" + port + "/topic/chatMessage.new"), testWebSocketHandler)
			.block(Duration.ofSeconds(2));
		
		return testWebSocketHandler;
	}
	
	public class TestWebSocketHandler implements WebSocketHandler {
		protected WebSocketSession session;
		
		@Override
		public Mono<Void> handle(WebSocketSession session) {
			this.session = session;
			return Mono.empty();
		}
	}
}
