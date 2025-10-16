package org.example.socialmedia_services.repository.post;

import org.example.socialmedia_services.entity.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    // Optimized method to fetch post with all related data in a single query
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.postContent pc " +
            "LEFT JOIN FETCH pc.placeWiseContentList pwc " +
            "WHERE p.postId = :postId " +
            "ORDER BY pwc.sequenceOrder ASC")
    Optional<Post> findByIdWithAllContent(@Param("postId") Long postId);

    // Optimized method to fetch posts by user with all related data
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.postContent pc " +
            "LEFT JOIN FETCH pc.placeWiseContentList pwc " +
            "WHERE p.createdById = :userId " +
            "ORDER BY p.createdAt DESC, pwc.sequenceOrder ASC")
    List<Post> findByUserIdWithAllContent(@Param("userId") Long userId);

    // Optimized method to fetch all posts with content (for feed)
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.postContent pc " +
            "LEFT JOIN FETCH pc.placeWiseContentList " +
            "ORDER BY p.createdAt DESC")
    List<Post> findAllPostsWithContent();

    // Method to fetch posts with content and place data with pagination
    @Query(value = "SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.postContent pc " +
            "ORDER BY p.createdAt DESC",
            countQuery = "SELECT COUNT(p) FROM Post p")
    List<Post> findPostsWithContentPaginated(@Param("limit") int limit, @Param("offset") int offset);
}