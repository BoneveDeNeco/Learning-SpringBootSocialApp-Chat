package com.lucas.learningspringboot.LearningSpringBootSocialAppChat.comments;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
	private String id;
	private String imageId;
	private String comment;
}
