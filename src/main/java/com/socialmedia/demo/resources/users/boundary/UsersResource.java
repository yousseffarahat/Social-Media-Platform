package com.socialmedia.demo.resources.users.boundary;

import com.socialmedia.demo.common.JWTTokenGenerator;
import com.socialmedia.demo.common.ResponseMessage;
import com.socialmedia.demo.common.Utilities;
import com.socialmedia.demo.repositories.users.boundary.FriendRequestRepository;
import com.socialmedia.demo.repositories.users.boundary.UserRepository;
import com.socialmedia.demo.repositories.users.entity.FriendRequest;
import com.socialmedia.demo.repositories.users.entity.User;
import com.socialmedia.demo.resources.users.control.UserCtrl;
import com.socialmedia.demo.resources.users.entity.FriendRequestModel;
import com.socialmedia.demo.resources.users.entity.LoginRequest;
import com.socialmedia.demo.resources.users.entity.UserModel;
import com.socialmedia.demo.resources.users.entity.UserRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/users", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Api(value="Users Resource", description="Resource responsible for handling all requests handling user management and authentication")
public class UsersResource {
    private final UserRepository userRepository;
    private final UserCtrl userCtrl;
    private final FriendRequestRepository friendRequestRepository;
    private final JWTTokenGenerator jwtTokenGenerator;
    private final String encryptionKey;
    private final String tokenDuration;

    @Autowired
    public UsersResource(UserRepository userRepository, UserCtrl userCtrl, FriendRequestRepository friendRequestRepository,
                         JWTTokenGenerator jwtTokenGenerator, @Value("${encryption.key}") String encryptionKey,
                         @Value("${token.duration}") String tokenDuration) {
        this.userRepository = userRepository;
        this.userCtrl = userCtrl;
        this.friendRequestRepository = friendRequestRepository;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.encryptionKey = encryptionKey;
        this.tokenDuration = tokenDuration;
    }

    @ApiOperation(value = "Returns all others users that arent already friends and haven't been sent a friend request, so that the we can list them " +
            "in the frontend so then the user will be able to add them as a friend")
    @GetMapping("/all/{userId}")
    public ResponseEntity<List<UserModel>> getAllUsersNotByUser(@PathVariable("userId") String userId, @RequestParam("token") String token) {
        if(checkUserToken(token)) {
            List<User> users = userRepository.findByIdNotAndFriendsNotContaining(userId,userId);
            List<FriendRequest> friendRequests = friendRequestRepository.findByUserIdOrFriendId(userId,userId);
            List<User> filteredUsers = new ArrayList<>(users);
            users.forEach(user -> {
                friendRequests.forEach(friendRequest -> {
                    if (user.getId().equals(friendRequest.getFriendId()) || user.getId().equals(friendRequest.getUserId())) {
                        filteredUsers.remove(user);
                    }
                });
            });
            return ResponseEntity.ok().body(userCtrl.getUserModels(filteredUsers));
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Returns all user friends to display in profile page")
    @GetMapping("/friends/{userId}")
    public ResponseEntity<List<UserModel>> getAllFriends(@PathVariable("userId") String userId, @RequestParam("token") String token) {
        if(checkUserToken(token)) {
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                List<User> friends = (List<User>) userRepository.findAllById(user.get().getFriends());
                return ResponseEntity.ok().body(userCtrl.getUserModels(friends));
            } else {
                return ResponseEntity.notFound().build();
            }
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Returns all user friend requests to display them in the profile page where the user can accept/reject them")
    @GetMapping("/friendRequests/{userId}")
    public ResponseEntity<List<FriendRequestModel>> getAllFriendRequests(@PathVariable("userId") String userId, @RequestParam("token") String token) {
        if(checkUserToken(token)) {
            return ResponseEntity.ok().body(userCtrl.getFriendRequestModels(friendRequestRepository.findByFriendId(userId)));
        }else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Endpoint responsible for adding two friends, creating a friend request entity to show in profile page")
    @PostMapping("/addFriend/{friendId}")
    public ResponseEntity<ResponseMessage> addFriend(@PathVariable("friendId") String friendId,
                                                     @RequestParam("userId") String userId, @RequestParam("token") String token) {
        if(checkUserToken(token)){
            Optional<User> user = userRepository.findById(userId);
            Optional<User> friend = userRepository.findById(friendId);
            if(user.isPresent() && friend.isPresent()){
                if(user.get().getFriends()!=null && user.get().getFriends().contains(friendId)){
                    return ResponseEntity.badRequest().body(new ResponseMessage("Friend Already added"));
                }else{
                    Optional<FriendRequest> existingFriendRequest = friendRequestRepository.findByUserIdAndFriendId(userId,friendId);
                    if(existingFriendRequest.isPresent()){
                        return ResponseEntity.badRequest().body(new ResponseMessage("Friend Request Already Sent"));
                    }else{
                        FriendRequest friendRequest = userCtrl.createFriendRequest(userId, friend.get());
                        friendRequestRepository.save(friendRequest);
                        return ResponseEntity.ok().body(new ResponseMessage("Friend Request Sent Successfully"));
                    }
                }
            }else{
                return ResponseEntity.notFound().build();
            }
        }else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Endpoint responsible for accepting the friend request, and adding the users to their respective friendslist")
    @PutMapping("/acceptRequest/{friendId}")
    public ResponseEntity<ResponseMessage> acceptFriendRequest(@PathVariable("friendId") String friendId,
                                                               @RequestParam("userId") String userId, @RequestParam("token") String token){
        if(checkUserToken(token)){
            Optional<FriendRequest> friendRequest = friendRequestRepository.findByUserIdAndFriendId(userId,friendId);
            if(friendRequest.isPresent()){
                Optional<User> user = userRepository.findById(userId);
                Optional<User> friend = userRepository.findById(friendId);
                if (user.isPresent() && friend.isPresent()) {
                    if (user.get().getFriends() != null) {
                        user.get().getFriends().add(friendId);
                    } else {
                        Set<String> friends = new HashSet<>();
                        friends.add(friendId);
                        user.get().setFriends(friends);
                    }

                    if (friend.get().getFriends() != null) {
                        friend.get().getFriends().add(userId);
                    } else {
                        Set<String> friends = new HashSet<>();
                        friends.add(userId);
                        friend.get().setFriends(friends);
                    }
                    userRepository.save(user.get());
                    userRepository.save(friend.get());
                    friendRequestRepository.deleteById(friendRequest.get().getId());
                    return ResponseEntity.ok().body(new ResponseMessage("Successfully added friend"));
                }else{
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessage("Could not find user"));
                }
            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessage("Could not find friend Request"));
            }
        }else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Endpoint responsible for rejecting the friend request and deleting it")
    @PutMapping("/rejectRequest/{friendId}")
    public ResponseEntity<ResponseMessage> rejectFriendRequest(@PathVariable("friendId") String friendId,
                                                               @RequestParam("userId") String userId, @RequestParam("token") String token){
        if(checkUserToken(token)) {
            Optional<FriendRequest> friendRequest = friendRequestRepository.findByUserIdAndFriendId(userId, friendId);
            if (friendRequest.isPresent()) {
                friendRequestRepository.deleteById(friendRequest.get().getId());
                return ResponseEntity.ok().body(new ResponseMessage("Successfully rejected friend Request"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessage("Could not find friend Request"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Returns user Model by Id")
    @GetMapping("/{userId}")
    public ResponseEntity<UserModel> getUserInfo(@PathVariable("userId") String userId, @RequestParam("token") String token) {
        if(checkUserToken(token)){
            Optional<User> user = userRepository.findById(userId);
            return user.map(value -> ResponseEntity.ok().body(userCtrl.getUserModel(value))).orElseGet(() -> ResponseEntity.notFound().build());
        }else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Returns all users, so that the admin can view them and activate/deactivate them")
    @GetMapping("/")
    public ResponseEntity<List<UserModel>> getAllUsers(@RequestParam("token") String token) {
        if(checkAdminToken(token)) {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok().body(userCtrl.getUserModels(users));
        }else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Returns if username is already in the system or not as the username has to be unique")
    @GetMapping("/checkUsername/{username}")
    public ResponseEntity<Boolean> checkUsername(@PathVariable("username") String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return ResponseEntity.ok().body(user.isEmpty());
    }

    @ApiOperation(value = "Registers user, creating a user entity with their respective info and encrypted password," +
            "then returns the authentication token that would then be sent with all requests")
    @PostMapping("/")
    public ResponseEntity<ResponseMessage> registerUser(@RequestBody UserRequest userRequest) throws Exception {
        User user = userRepository.save(userCtrl.createUser(userRequest, encryptionKey));
        return ResponseEntity.ok().body(new ResponseMessage(jwtTokenGenerator.createToken(userCtrl.getUserModel(user), tokenDuration, encryptionKey)));
    }

    @ApiOperation(value = "Checks if username and passwords match users in the database while also checking the activated boolean," +
            " returns authentication token if correct")
    @PostMapping("/login")
    public ResponseEntity<ResponseMessage> login(@RequestBody LoginRequest loginRequest) throws Exception {
        Optional<User> user = userRepository.findByUsername(loginRequest.getUsername());
        if (user.isPresent() && Utilities.decryptWithAES(user.get().getPassword(), encryptionKey).equals(loginRequest.getPassword())) {
            if (user.get().isActivated()) {
                return ResponseEntity.ok().body(new ResponseMessage(jwtTokenGenerator.createToken(userCtrl.getUserModel(user.get()), tokenDuration, encryptionKey)));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @ApiOperation(value = "Deactivates user by changing their activated boolean which gets checked on login")
    @PutMapping("/deactivate/{userId}")
    public ResponseEntity<ResponseMessage> deactivateUser(@PathVariable("userId") String userId,
                                                          @RequestParam("token") String token) {
        if(checkAdminToken(token)){
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()){
                user.get().setActivated(false);
                userRepository.save(user.get());
                return ResponseEntity.ok().body(new ResponseMessage("Successfully deactivated User"));
            }else {
                return ResponseEntity.badRequest().body(new ResponseMessage("Could not find user"));
            }
        }else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Activates a previously deactivated user by changing their activated boolean")
    @PutMapping("/activate/{userId}")
    public ResponseEntity<ResponseMessage> activateUser(@PathVariable("userId") String userId, @RequestParam("token") String token) {
        if(checkAdminToken(token)){
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()){
                user.get().setActivated(true);
                userRepository.save(user.get());
                return ResponseEntity.ok().body(new ResponseMessage("Successfully activated User"));
            }else {
                return ResponseEntity.badRequest().body(new ResponseMessage("Could not find user"));
            }
        }else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    private boolean checkUserToken(String token) {
        if(token!=null && !token.isEmpty() && !token.isBlank()){
            try {
                return jwtTokenGenerator.validateToken(token, encryptionKey);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
        }else{
            return false;
        }
    }

    private boolean checkAdminToken(String token) {
        if(token!=null && !token.isEmpty() && !token.isBlank()){
            try {
                if(jwtTokenGenerator.validateToken(token, encryptionKey)){
                    User user = jwtTokenGenerator.getUserFromToken(token, encryptionKey);
                    return user.isAdmin();
                }else{
                    return false;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
        }else{
            return false;
        }
    }
}
