package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;

@Service
@EnableBinding(ChatServiceStreams.class)
public class OutboundChatService {
	
	@StreamListener(ChatServiceStreams.BROKER_TO_CLIENT)
	public void listen(String message) {
		
	}
}
