package com.socialmedia.demo.resources.users.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FriendRequestModel {
    private String id;
    private String userId;
    private String friendId;
    private String friendFirstName;
    private String friendLastName;
}
