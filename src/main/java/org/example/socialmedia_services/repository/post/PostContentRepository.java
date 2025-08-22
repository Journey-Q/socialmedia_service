package org.example.socialmedia_services.repository.post;

import org.example.socialmedia_services.entity.post.Post;
import org.example.socialmedia_services.entity.post.PostContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostContentRepository extends JpaRepository<PostContent, Long> {

    // Find post content by post
    Optional<PostContent> findByPost(Post post);

    // Find post content by post ID (now using postId as primary key)
    Optional<PostContent> findByPostId(Long postId);

    // Delete post content by post
    void deleteByPost(Post post);

    // Check if post content exists by post ID
    boolean existsByPostId(Long postId);
}