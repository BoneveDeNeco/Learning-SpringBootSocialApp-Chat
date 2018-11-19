package com.lucas.learningspringboot.LearningSpringBootSocialAppChat;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface ChatServiceStreams {
	String NEW_COMMENTS = "newComments";
	String CLIENT_TO_BROKER = "clientToBroker";
	String BROKER_TO_CLIENT = "brokerToClient";
	
	@Input(NEW_COMMENTS)
	SubscribableChannel newComments();
	
	@Output(CLIENT_TO_BROKER)
	MessageChannel clientToBroker();
	
	@Input(BROKER_TO_CLIENT)
	SubscribableChannel brokerToClient();
}
