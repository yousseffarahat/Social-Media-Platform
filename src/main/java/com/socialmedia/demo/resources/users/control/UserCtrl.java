package com.socialmedia.demo.resources.users.control;

import com.socialmedia.demo.common.Utilities;
import com.socialmedia.demo.repositories.users.entity.FriendRequest;
import com.socialmedia.demo.repositories.users.entity.User;
import com.socialmedia.demo.resources.users.entity.FriendRequestModel;
import com.socialmedia.demo.resources.users.entity.UserModel;
import com.socialmedia.demo.resources.users.entity.UserRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class UserCtrl {
    public User createUser(UserRequest userRequest, String encryptionKey) throws Exception {
        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setUsername(userRequest.getUsername());
        //Encrypt password before adding to database
        user.setPassword(Utilities.encryptWithAES(userRequest.getPassword(), encryptionKey));
        user.setFriends(new HashSet<>());
        user.setActivated(true);
        user.setAdmin(false);
        return user;
    }

    public FriendRequest createFriendRequest(String userId, User friend) {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setUserId(userId);
        friendRequest.setFriendId(friend.getId());
        friendRequest.setFriendFirstName(friend.getFirstName());
        friendRequest.setFriendLastName(friend.getLastName());
        return friendRequest;
    }

    public List<UserModel> getUserModels(List<User> users) {
        List<UserModel> models = new ArrayList<>();
        users.forEach(user-> models.add(getUserModel(user)));
        return models;
    }

    public UserModel getUserModel(User user) {
        return Utilities.getObjectMapper().convertValue(user, UserModel.class);
    }


    public List<FriendRequestModel> getFriendRequestModels(List<FriendRequest> friendRequests) {
        List<FriendRequestModel> models = new ArrayList<>();
        friendRequests.forEach(friendRequest-> models.add(getFriendRequestModel(friendRequest)));
        return models;
    }

    public FriendRequestModel getFriendRequestModel(FriendRequest friendRequest) {
        return Utilities.getObjectMapper().convertValue(friendRequest, FriendRequestModel.class);
    }
}
