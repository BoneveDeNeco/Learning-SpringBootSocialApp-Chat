package com.lucas.learningspringboot.LearningSpringBootSocialAppChat.comments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

@Service
@EnableBinding(Sink.class)
public class CommentService implements WebSocketHandler {
	
	private static final Logger log = LoggerFactory.getLogger(CommentService.class);
	
	private ObjectMapper mapper;
	private Flux<Comment> flux;
	private FluxSink<Comment> webSocketCommentSink;
	
	public CommentService(ObjectMapper mapper) {
		this.mapper = mapper;
		this.flux = Flux.<Comment>create(
				emitter -> this.webSocketCommentSink = emitter,
				FluxSink.OverflowStrategy.IGNORE)
			.publish()
			.autoConnect();
	}

	@StreamListener(Sink.INPUT)
	public void broadcast(Comment comment) {
		log.info("Broadcasting comment");
		if (webSocketCommentSink != null) {
			log.info("Publishing " + comment.toString() + " to websocket.");
			webSocketCommentSink.next(comment);
		}
	}

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		log.info("Handling WebSocketSession " + session.toString());
		return session.send(this.flux
			.map(comment -> {
				try {
					return mapper.writeValueAsString(comment);
				} catch (JsonProcessingException exception) {
					throw new RuntimeException();
				}
			})
			.log("encode-as-json")
			.map(session::textMessage)
			.log("wrap-as-websocket-message"))
		.log("publish-to-websocket");
	}
}
