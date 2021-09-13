package com.socialmedia.demo.resources.users.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
}
