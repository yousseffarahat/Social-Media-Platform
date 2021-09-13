package com.socialmedia.demo.repositories.posts.boundary;

import com.socialmedia.demo.repositories.posts.entity.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<Comment, String> {
}
