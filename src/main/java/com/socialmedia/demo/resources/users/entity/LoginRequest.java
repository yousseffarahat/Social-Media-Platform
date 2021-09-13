package com.socialmedia.demo.resources.users.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class LoginRequest {
    private String username;
    private String password;
}
