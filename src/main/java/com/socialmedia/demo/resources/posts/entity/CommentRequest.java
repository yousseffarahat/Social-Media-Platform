package com.socialmedia.demo.resources.posts.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CommentRequest {
    private String postId;
    private String userId;
    private String content;
}
