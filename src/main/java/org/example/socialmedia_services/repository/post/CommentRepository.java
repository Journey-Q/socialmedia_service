package org.example.socialmedia_services.repository.post;

import org.example.socialmedia_services.entity.post.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comments, Long> {

    // Find comments by post ID ordered by creation date
    List<Comments> findByPostIdOrderByCommentedAtDesc(Long postId);

    // Count comments for a post
    Long countByPostId(Long postId);

    // Find comments by user ID
    List<Comments> findByUserIdOrderByCommentedAtDesc(Long userId);

    // Delete all comments for a post
    void deleteByPostId(Long postId);

    // Check if comment exists by ID and user ID (for authorization)
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);
}
