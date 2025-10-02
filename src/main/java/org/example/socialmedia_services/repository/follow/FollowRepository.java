package org.example.socialmedia_services.repository.follow;

import org.example.socialmedia_services.entity.follow.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    @Query("SELECT f FROM Follow f WHERE f.followingId = :followingId AND f.followerId = :followerId")
    Optional<Follow> findFollowRelationship(
            @Param("followingId") String followingId,
            @Param("followerId") String followerId
    );

    Page<Follow> findByFollowerIdAndStatus(String followerId, String status, Pageable pageable);

    Page<Follow> findByFollowingIdAndStatus(String followingId, String status, Pageable pageable);
}