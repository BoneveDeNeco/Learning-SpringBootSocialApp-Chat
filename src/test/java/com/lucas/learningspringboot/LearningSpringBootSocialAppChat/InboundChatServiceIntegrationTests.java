package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.StandardWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class InboundChatServiceIntegrationTests {
	
	private static final String USER = "Marvin";

	private static final String MESSAGE = "Test";

	@Autowired
	ChatServiceStreams chatServiceStreams;
	
	@Autowired
	MessageCollector messageCollector;
	
	@SpyBean
	InboundChatService handler;
	
	String sessionId = "";
	
	@LocalServerPort
	private int port;
	
	@Test
	public void broadcastsMessagesToClients() throws URISyntaxException {
		sendMessage(MESSAGE);
		
		Message<?> message = collectMessageFromChannel(chatServiceStreams.clientToBroker());
		assertThat(message.getPayload()).isNotNull();
	}
	
	@Test
	public void returnsUsernameInHeader() throws URISyntaxException {
		sendMessage(MESSAGE);
		
		Message<?> message = collectMessageFromChannel(chatServiceStreams.clientToBroker());
		
		assertThat(message.getHeaders().get(ChatServiceStreams.USER_HEADER)).isEqualTo(USER);
	}
	
	private void sendMessage(String message) throws URISyntaxException {
		WebSocketClient webSocketClient = new StandardWebSocketClient();
		webSocketClient.execute(new URI("ws://localhost:"+ port +"/app/chatMessage.new?user=" + USER), new WebSocketHandler() {
			
			@Override
			public Mono<Void> handle(WebSocketSession session) {
				return session.send(Flux.just(session.textMessage(message)))
					.then();
			}
		}).block();
	}
	
	private Message<?> collectMessageFromChannel(MessageChannel channel) {
		BlockingQueue<Message<?>> messages = messageCollector.forChannel(channel);
		return messages.poll();
	}
	
}
