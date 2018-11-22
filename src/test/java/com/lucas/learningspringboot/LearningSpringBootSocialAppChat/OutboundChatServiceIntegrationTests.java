package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
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
	
	private static final String MESSAGE = "Hello";

	@Autowired
	ChatServiceStreams chatServiceStreams;
	
	@LocalServerPort
	private int port;
	
	@Rule
	public TestRule watcher = new TestWatcher() {
		protected void starting(Description description) {
			System.out.println("START - " + description.getMethodName());
		}
		
		protected void finished(Description description) {
			System.out.println("END - " + description.getMethodName());
		}
	};
	
	@Test
	public void receivesMessageFromBroker() {
		assertThat(sendMessage()).isTrue();
	}
	
	@Test
	public void acceptsWebSocketConnectionFromClients() {
		TestWebSocketHandler testWebSocketHandler = connectToOutboundChatService();
		
		assertThat(testWebSocketHandler.connected).isTrue();
	}
	
	@Test
	public void redirectsIncomingMessageFromBrokerToWebSocketClients() throws InterruptedException, ExecutionException, TimeoutException {
		TestWebSocketHandler testWebSocketHandler = connectToOutboundChatService();
		
		sendMessage();

		assertThat(testWebSocketHandler.receivedMessage.get(10, TimeUnit.SECONDS)).isEqualTo(MESSAGE);
	}
	
	private boolean sendMessage() {
		return chatServiceStreams.brokerToClient().send(MessageBuilder.withPayload(MESSAGE).build());
	}
	
	private TestWebSocketHandler connectToOutboundChatService() {
		WebSocketClient client = new StandardWebSocketClient();
		TestWebSocketHandler testWebSocketHandler = new TestWebSocketHandler();
		
		try {
			client.execute(URI.create("ws://localhost:" + port + "/topic/chatMessage.new"), testWebSocketHandler)
			//.subscribe(aVoid -> System.out.println("Subscribed")); //Doesn't work
			.block(Duration.ofSeconds(2));//Doesn't look good...
		} catch (RuntimeException e) {
			System.out.println("Ignoring timeout");
		}
		
		return testWebSocketHandler;
	}
	
	public class TestWebSocketHandler implements WebSocketHandler {
		boolean connected = false;
		CompletableFuture<String> receivedMessage = new CompletableFuture<>();
		@Override
		public Mono<Void> handle(WebSocketSession session) {
			connected = true;
			return session.receive()
				.map(message -> 
					receivedMessage.complete(message.getPayloadAsText()))
				.then();
		}
	}
}
