package com.socialmedia.demo.repositories.users.boundary;

import com.socialmedia.demo.repositories.users.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    List<User> findByIdNot(String id);

    List<User> findByIdNotAndFriendsNotContaining(String userId, String userId2);

    Optional<User> findByUsername(String username);
}
