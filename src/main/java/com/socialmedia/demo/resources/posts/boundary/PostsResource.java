package com.socialmedia.demo.resources.posts.boundary;

import com.socialmedia.demo.common.JWTTokenGenerator;
import com.socialmedia.demo.common.ResponseMessage;
import com.socialmedia.demo.repositories.posts.boundary.CommentRepository;
import com.socialmedia.demo.repositories.posts.boundary.PostRepository;
import com.socialmedia.demo.repositories.posts.entity.Comment;
import com.socialmedia.demo.repositories.posts.entity.Post;
import com.socialmedia.demo.repositories.users.boundary.UserRepository;
import com.socialmedia.demo.repositories.users.entity.User;
import com.socialmedia.demo.resources.posts.control.PostsCtrl;
import com.socialmedia.demo.resources.posts.entity.CommentModel;
import com.socialmedia.demo.resources.posts.entity.CommentRequest;
import com.socialmedia.demo.resources.posts.entity.PostModel;
import com.socialmedia.demo.resources.posts.entity.PostRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Api(value="Posts Resource", description="Resource responsible for handling all requests handling posts and comments")
public class PostsResource {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostsCtrl postsCtrl;
    private final JWTTokenGenerator jwtTokenGenerator;
    private final String encryptionKey;

    @Autowired
    public PostsResource(PostRepository postRepository, CommentRepository commentRepository,
                         UserRepository userRepository, PostsCtrl postsCtrl,
                         JWTTokenGenerator jwtTokenGenerator, @Value("${encryption.key}") String encryptionKey) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postsCtrl = postsCtrl;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.encryptionKey = encryptionKey;
    }

    @ApiOperation(value = "Returns paginated posts that arent submitted by the currently logged in user," +
            " sorted by creation date to be displayed on the timeline")
    @GetMapping("/")
    public ResponseEntity<Page<PostModel>> getAllPostsNotByUser(@RequestParam("userId") String userId,
                                                                @RequestParam("page") int page,
                                                                @RequestParam("size") int size, @RequestParam("token") String token) {
        if(checkUserToken(token)) {
            Pageable pageable = PageRequest.of(page,size, Sort.by("creationTimeStamp"));
            Page<Post> posts = postRepository.findByUserIdNot(userId, pageable);
            return ResponseEntity.ok().body(postsCtrl.getPostModels(posts));
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Returns paginated posts that are submitted by the currently logged in user," +
            " sorted by creation date to be displayed on the profile")
    @GetMapping("/profile")
    public ResponseEntity<Page<PostModel>> getAllPostsByUser(@RequestParam("userId") String userId,
                                                        @RequestParam("page") int page,
                                                        @RequestParam("size") int size, @RequestParam("token") String token) {

        if(checkUserToken(token)) {
            Pageable pageable = PageRequest.of(page,size, Sort.by("creationTimeStamp"));
            Page<Post> posts = postRepository.findByUserId(userId, pageable);
            return ResponseEntity.ok().body(postsCtrl.getPostModels(posts));
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Likes a post by adding the userId to the likes set on the post entity")
    @PutMapping("/like/{postId}")
    public ResponseEntity<ResponseMessage> likePost(@PathVariable("postId") String postId, @RequestParam("userId") String userId,
                                                    @RequestParam("token") String token) {
        if(checkUserToken(token)) {
            Optional<Post> post = postRepository.findById(postId);
            if(post.isPresent()){
                if(post.get().getLikes()!=null){
                    post.get().getLikes().add(userId);
                }else{
                    Set<String> likes = new HashSet<>();
                    likes.add(userId);
                    post.get().setLikes(likes);
                }
                postRepository.save(post.get());
                return ResponseEntity.ok().body(new ResponseMessage("Successfully Liked post"));
            }else {
                return ResponseEntity.notFound().build();
            }
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Unlikes a post by removing the userId to the likes set on the post entity")
    @DeleteMapping("/like/{postId}")
    public ResponseEntity<ResponseMessage> unLikePost(@PathVariable("postId") String postId, @RequestParam("userId") String userId,
                                             @RequestParam("token") String token) {
        if(checkUserToken(token)) {
            Optional<Post> post = postRepository.findById(postId);
            if(post.isPresent()){
                if(post.get().getLikes()!=null){
                    post.get().getLikes().remove(userId);
                }
                postRepository.save(post.get());
                return ResponseEntity.ok().body(new ResponseMessage("Successfully UnLiked post"));
            }else {
                return ResponseEntity.notFound().build();
            }
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Returns all posts for the admin to view, so that they can then delete a post")
    @GetMapping("/posts")
    public ResponseEntity<List<PostModel>> getAllPosts(@RequestParam("token") String token) {
        if(checkAdminToken(token)){
            List<Post> posts = postRepository.findAll();
            return ResponseEntity.ok().body(postsCtrl.getPostModels(posts));
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Returns all comments for the admin to view, so that they can then delete a comment")
    @GetMapping("/comments")
    public ResponseEntity<List<CommentModel>> getAllComments(@RequestParam("token") String token) {
        if(checkAdminToken(token)) {
            List<Comment> comments = commentRepository.findAll();
            return ResponseEntity.ok().body(postsCtrl.getCommentModels(comments));
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Deletes a post from the database")
    @DeleteMapping("/{postId}")
    public ResponseEntity<ResponseMessage> deletePost(@PathVariable("postId") String postId, @RequestParam("token") String token) {
        if(checkAdminToken(token)) {
            Optional<Post> post = postRepository.findById(postId);
            if (post.isPresent()) {
                postRepository.delete(post.get());
                return ResponseEntity.ok().body(new ResponseMessage("Successfully deleted the post"));
            } else {
                return ResponseEntity.notFound().build();
            }
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Deletes a comment from the database")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ResponseMessage> deleteComment(@PathVariable("commentId") String commentId, @RequestParam("token") String token) {
        if(checkAdminToken(token)){
            Optional<Comment> comment = commentRepository.findById(commentId);
            if(comment.isPresent()){
                commentRepository.delete(comment.get());
                return ResponseEntity.ok().body(new ResponseMessage("Successfully deleted the comment"));
            }else{
                return ResponseEntity.notFound().build();
            }
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Adds a post to the database with its respective content and attachment, linked by the userId")
    @PostMapping("/")
    public ResponseEntity<PostModel> addPost(@RequestBody PostRequest postRequest, @RequestParam("token") String token) {
        Optional<User> user = userRepository.findById(postRequest.getUserId());
        if(checkUserToken(token) && user.isPresent()) {
            Post post = postRepository.save(postsCtrl.createPost(postRequest, user.get()));
            return ResponseEntity.ok().body(postsCtrl.getPostModel(post));
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ApiOperation(value = "Adds a comment to the database with its respective content and adding it to its respective post in the database")
    @PostMapping("/comments/")
    public ResponseEntity<CommentModel> addComment(@RequestBody CommentRequest commentRequest, @RequestParam("token") String token) {
        if(checkUserToken(token)) {
            Optional<Post> post = postRepository.findById(commentRequest.getPostId());
            if (post.isPresent()) {
                Optional<User> user = userRepository.findById(commentRequest.getUserId());
                if (user.isPresent()) {
                    Comment comment = postsCtrl.createComment(commentRequest, user.get());
                    comment = commentRepository.save(comment);
                    List<Comment> postComments = post.get().getComments();
                    postComments.add(comment);
                    post.get().setComments(postComments);
                    postRepository.save(post.get());
                    return ResponseEntity.ok().body(postsCtrl.getCommentModel(comment));
                } else {
                    return ResponseEntity.badRequest().build();
                }
            } else {
                return ResponseEntity.notFound().build();
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
