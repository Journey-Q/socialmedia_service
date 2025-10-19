package org.example.socialmedia_services.repository.post;

import org.example.socialmedia_services.entity.post.PlaceWiseContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PlaceWiseContentRepository extends JpaRepository<PlaceWiseContent, Long> {

    // Find all place wise content by post ID ordered by sequence
    List<PlaceWiseContent> findByPostIdOrderBySequenceOrderAsc(Long postId);

    // Find place wise content by post ID
    List<PlaceWiseContent> findByPostId(Long postId);

    // Delete all place wise content for a post
    @Modifying
    @Transactional
    void deleteByPostId(Long postId);

    // Count place wise content for a post
    Long countByPostId(Long postId);

    // Check if place wise content exists for a post
    boolean existsByPostId(Long postId);

    // Find by post ID and sequence order
    PlaceWiseContent findByPostIdAndSequenceOrder(Long postId, Integer sequenceOrder);
}