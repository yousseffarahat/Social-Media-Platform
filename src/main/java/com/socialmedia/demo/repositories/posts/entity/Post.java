package com.socialmedia.demo.repositories.posts.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@Data
public class Post {
    @Id
    private String id;
    private String content;
    private String attachment;
    private String userId;
    private String userFirstName;
    private String userLastName;
    /** List of comments instead of ids to minimize queries/ api calls on feed page,
     * could be set as a list of ids but would need aggregation when querying
     */
    private List<Comment> comments;
    // List of users who liked this post
    private Set<String> likes;
    @CreatedDate
    private LocalDateTime creationTimeStamp;
}
