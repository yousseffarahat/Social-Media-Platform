package com.socialmedia.demo.repositories.users.boundary;

import com.socialmedia.demo.repositories.users.entity.FriendRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {
    List<FriendRequest> findByUserId(String userId);
    List<FriendRequest> findByUserIdOrFriendId(String userId, String userId2);
    List<FriendRequest> findByFriendId(String userId);
    Optional<FriendRequest> findByUserIdAndFriendId(String userId, String friendId);
}
