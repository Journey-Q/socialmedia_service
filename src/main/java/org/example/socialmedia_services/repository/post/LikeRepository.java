package org.example.socialmedia_services.repository.post;

import org.example.socialmedia_services.entity.post.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Likes, Long> {

    // Find like by post ID and user ID
    Optional<Likes> findByPostIdAndUserId(Long postId, Long userId);

    // Check if user has liked the post
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    // Count likes for a post
    Long countByPostId(Long postId);

    // Delete like by post ID and user ID
    @Modifying
    @Transactional
    void deleteByPostIdAndUserId(Long postId, Long userId);

    // Delete all likes for a post
    @Modifying
    @Transactional
    void deleteByPostId(Long postId);
}
