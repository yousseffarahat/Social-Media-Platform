package com.socialmedia.demo.resources.users.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@NoArgsConstructor
@Data
public class UserModel {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private Set<String> friends;
    private boolean activated;
    private boolean admin;
}
