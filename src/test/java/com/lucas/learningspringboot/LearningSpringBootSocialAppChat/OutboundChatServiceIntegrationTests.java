package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OutboundChatServiceIntegrationTests {
	
	@Autowired
	ChatServiceStreams chatServiceStreams;

	@Test
	public void redirectsMessagesFromBrokerToWebSocketClients() {
		chatServiceStreams.brokerToClient().send(MessageBuilder.withPayload("Hello").build());
	}
}
