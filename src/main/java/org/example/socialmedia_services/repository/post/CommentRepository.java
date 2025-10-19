// Add these methods to your CommentRepository interface

package org.example.socialmedia_services.repository.post;

import org.example.socialmedia_services.entity.post.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comments, Long> {

    // Existing methods
    List<Comments> findByPostIdOrderByCommentedAtDesc(Long postId);
    Long countByPostId(Long postId);

    // New methods for reply functionality
    List<Comments> findByParentIdOrderByCommentedAtAsc(Long parentId);
    List<Comments> findByPostIdAndParentIdIsNullOrderByCommentedAtDesc(Long postId);
    Long countByParentId(Long parentId);

    // Delete all comments for a post
    @Modifying
    @Transactional
    void deleteByPostId(Long postId);
}