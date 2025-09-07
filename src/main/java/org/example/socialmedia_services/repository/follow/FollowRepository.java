package org.example.socialmedia_services.repository.follow;

import org.example.socialmedia_services.entity.follow.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // Create follow - handled by save() method

    // Get all followers of a user (users following this user)
    @Query("SELECT f FROM Follow f WHERE f.followerId = :followerId AND f.status = 'accepted'")
    List<Follow> getFollowers(@Param("followerId") String followerId);

    // Get all users that a user is following
    @Query("SELECT f FROM Follow f WHERE f.followingId = :followingId AND f.status = 'accepted'")
    List<Follow> getFollowing(@Param("followingId") String followingId);

    // Get pending follow requests for a user
    @Query("SELECT f FROM Follow f WHERE f.followerId = :followerId AND f.status = 'pending'")
    List<Follow> getPendingFollowRequests(@Param("followerId") String followerId);

    // Update status of a follow request
    @Modifying
    @Query("UPDATE Follow f SET f.status = :status, f.updatedAt = CURRENT_TIMESTAMP WHERE f.followingId = :followingId AND f.followerId = :followerId")
    int updateStatus(@Param("followingId") String followingId, @Param("followerId") String followerId, @Param("status") String status);

    // Delete follow relationship
    @Modifying
    @Query("DELETE FROM Follow f WHERE f.followingId = :followingId AND f.followerId = :followerId")
    int deleteFollow(@Param("followingId") String followingId, @Param("followerId") String followerId);

    // Check if follow relationship exists
    @Query("SELECT f FROM Follow f WHERE f.followingId = :followingId AND f.followerId = :followerId")
    Optional<Follow> findFollowRelationship(@Param("followingId") String followingId, @Param("followerId") String followerId);

    // Check if follow relationship exists with specific status
    @Query("SELECT f FROM Follow f WHERE f.followingId = :followingId AND f.followerId = :followerId AND f.status = :status")
    Optional<Follow> findFollowRelationshipByStatus(@Param("followingId") String followingId, @Param("followerId") String followerId, @Param("status") String status);

    // Get all follow relationships for a user (for cleanup when user is deleted)
    @Query("SELECT f FROM Follow f WHERE f.followingId = :userId OR f.followerId = :userId")
    List<Follow> findAllFollowRelationshipsForUser(@Param("userId") String userId);

    // Get mutual follows
    @Query("SELECT f1 FROM Follow f1 WHERE f1.followingId = :userId AND f1.status = 'accepted' AND " +
            "EXISTS (SELECT f2 FROM Follow f2 WHERE f2.followingId = f1.followerId AND f2.followerId = :userId AND f2.status = 'accepted')")
    List<Follow> findMutualFollows(@Param("userId") String userId);

    // Count accepted followers
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followerId = :followerId AND f.status = 'accepted'")
    long countAcceptedFollowers(@Param("followerId") String followerId);

    // Count accepted following
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followingId = :followingId AND f.status = 'accepted'")
    long countAcceptedFollowing(@Param("followingId") String followingId);

    // Count pending requests
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.followerId = :followerId AND f.status = 'pending'")
    long countPendingRequests(@Param("followerId") String followerId);
}