package com.socialmedia.demo.resources.posts.control;

import com.socialmedia.demo.common.Utilities;
import com.socialmedia.demo.repositories.posts.entity.Comment;
import com.socialmedia.demo.repositories.posts.entity.Post;
import com.socialmedia.demo.repositories.users.entity.User;
import com.socialmedia.demo.resources.posts.entity.CommentModel;
import com.socialmedia.demo.resources.posts.entity.CommentRequest;
import com.socialmedia.demo.resources.posts.entity.PostModel;
import com.socialmedia.demo.resources.posts.entity.PostRequest;
import com.socialmedia.demo.resources.users.entity.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
public class PostsCtrl {
    public Post createPost(PostRequest postRequest, User user) {
        Post post = new Post();
        post.setUserId(postRequest.getUserId());
        post.setUserFirstName(user.getFirstName());
        post.setUserLastName(user.getLastName());
        post.setContent(postRequest.getContent());
        post.setAttachment(postRequest.getAttachment());
        post.setComments(new ArrayList<>());
        post.setLikes(new HashSet<>());
        return post;
    }

    public Comment createComment(CommentRequest commentRequest, User user) {
        Comment comment = new Comment();
        comment.setUserId(commentRequest.getUserId());
        comment.setContent(commentRequest.getContent());
        comment.setFirstName(user.getFirstName());
        comment.setLastName(user.getLastName());
        return comment;
    }

    public Page<PostModel> getPostModels(Page<Post> posts) {
        return posts.map(this::getPostModel);
    }

    public List<PostModel> getPostModels(List<Post> posts) {
        List<PostModel> models = new ArrayList<>();
        posts.forEach(post-> models.add(getPostModel(post)));
        return models;
    }

    public PostModel getPostModel(Post post) {
        return Utilities.getObjectMapper().convertValue(post, PostModel.class);
    }

    public List<CommentModel> getCommentModels(List<Comment> comments) {
        List<CommentModel> models = new ArrayList<>();
        comments.forEach(comment-> models.add(getCommentModel(comment)));
        return models;
    }

    public CommentModel getCommentModel(Comment comment) {
        return Utilities.getObjectMapper().convertValue(comment, CommentModel.class);
    }

}
