package com.socialmedia.demo.repositories.posts.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class Comment {
    @Id
    private String id;
    private String userId;
    private String firstName;
    private String lastName;
    private String content;
    @CreatedDate
    private LocalDateTime creationTimeStamp;
}
