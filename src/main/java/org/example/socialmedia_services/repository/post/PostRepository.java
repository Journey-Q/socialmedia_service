package org.example.socialmedia_services.repository.post;

import org.example.socialmedia_services.entity.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // Find posts by user ID
    List<Post> findByCreatedByIdOrderByCreatedAtDesc(Long createdById);

    // Find all posts ordered by creation date
    List<Post> findAllByOrderByCreatedAtDesc();

    // Check if post exists by ID and creator ID (for authorization)
    boolean existsByPostIdAndCreatedById(Long postId, Long createdById);

    // Count posts by user
    @Query("SELECT COUNT(p) FROM Post p WHERE p.createdById = :userId")
    Long countPostsByUserId(@Param("userId") Long userId);

    // Find posts with pagination
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findPostsWithLimit(@Param("limit") int limit, @Param("offset") int offset);
}