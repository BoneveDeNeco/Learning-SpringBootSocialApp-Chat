package com.lucas.learningspringboot.LearningSpringBootSocialAppChat.comments;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentServiceTests {
	
	//@Test
	public void broadcastsComments() {
		Comment comment = new Comment("1", "1", "comment");
	}
}
