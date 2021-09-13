package com.socialmedia.demo.resources.posts.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PostRequest {
    private String userId;
    private String content;
    private String attachment;
}
