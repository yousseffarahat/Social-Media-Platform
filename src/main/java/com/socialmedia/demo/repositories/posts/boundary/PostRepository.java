package com.socialmedia.demo.repositories.posts.boundary;

import com.socialmedia.demo.repositories.posts.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<Post, String> {
    Page<Post> findByUserIdNot(String userId, Pageable pageable);
    Page<Post> findByUserId(String userId, Pageable pageable);
}
