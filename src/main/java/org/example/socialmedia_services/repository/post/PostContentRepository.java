package org.example.socialmedia_services.repository.post;

import org.example.socialmedia_services.entity.post.PostContent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostContentRepository extends JpaRepository<PostContent, Long> {

    // Find PostContent by postId
    Optional<PostContent> findByPostId(Long postId);

    // Delete PostContent by postId
    @Modifying
    @Transactional
    void deleteByPostId(Long postId);

    // Check if PostContent exists by postId
    boolean existsByPostId(Long postId);

    // Alternative: Custom query if you prefer
    @Modifying
    @Transactional
    @Query("DELETE FROM PostContent pc WHERE pc.postId = :postId")
    void deletePostContentByPostId(@Param("postId") Long postId);

    // Search by journey title (case-insensitive)
    List<PostContent> findByJourneyTitleContainingIgnoreCase(String journeyTitle, Pageable pageable);

    // Search within places_visited JSON array using PostgreSQL JSON functions
    @Query(value = "SELECT * FROM post_content pc WHERE " +
            "EXISTS (SELECT 1 FROM jsonb_array_elements_text(pc.places_visited) AS place " +
            "WHERE LOWER(place) LIKE LOWER(CONCAT('%', :place, '%')))",
            nativeQuery = true)
    List<PostContent> findByPlacesVisitedContaining(@Param("place") String place, Pageable pageable);

    // Combined search in both journey title and places visited
    @Query(value = "SELECT DISTINCT pc.* FROM post_content pc WHERE " +
            "LOWER(pc.journey_title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "EXISTS (SELECT 1 FROM jsonb_array_elements_text(pc.places_visited) AS place " +
            "WHERE LOWER(place) LIKE LOWER(CONCAT('%', :query, '%')))",
            nativeQuery = true)
    List<PostContent> findByJourneyTitleOrPlacesVisitedContaining(@Param("query") String query, Pageable pageable);
}