package com.socialmedia.demo.repositories.users.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class FriendRequest {
    @Id
    private String id;
    private String userId;
    private String friendId;
    private String friendFirstName;
    private String friendLastName;
    @CreatedDate
    private LocalDateTime creationTimeStamp;
}
