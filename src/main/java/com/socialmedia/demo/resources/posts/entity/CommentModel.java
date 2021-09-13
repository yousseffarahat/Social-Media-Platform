package com.socialmedia.demo.resources.posts.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class CommentModel {
    private String id;
    private String userId;
    private String firstName;
    private String lastName;
    private String content;
}
