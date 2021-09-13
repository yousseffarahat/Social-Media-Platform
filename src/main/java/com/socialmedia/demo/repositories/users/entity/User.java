package com.socialmedia.demo.repositories.users.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.Set;

@NoArgsConstructor
@Data
public class User {
    @Id
    private String id;
    @Indexed(unique=true)
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    // list of friend ids
    private Set<String> friends;
    private boolean activated;
    private boolean admin;
    @CreatedDate
    private LocalDateTime creationTimeStamp;
}
