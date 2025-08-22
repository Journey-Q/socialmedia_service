package org.example.socialmedia_services.repository.post;

import org.example.socialmedia_services.entity.post.PostContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PostContentRepository extends JpaRepository<PostContent, Long> {

    // Find PostContent by postId
    Optional<PostContent> findByPostId(Long postId);

    // Delete PostContent by postId (instead of deleteByPost)
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
}