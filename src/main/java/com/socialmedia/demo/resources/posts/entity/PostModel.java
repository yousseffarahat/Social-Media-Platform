package com.socialmedia.demo.resources.posts.entity;

import com.socialmedia.demo.repositories.posts.entity.Comment;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Data
public class PostModel {
    private String id;
    private String content;
    private String attachment;
    private String userId;
    private String userFirstName;
    private String userLastName;
    private List<Comment> comments;
    private Set<String> likes;
}
