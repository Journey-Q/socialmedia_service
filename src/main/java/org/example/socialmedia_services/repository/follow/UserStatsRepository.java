package org.example.socialmedia_services.repository.follow;

import org.example.socialmedia_services.entity.follow.UserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStatsRepository extends JpaRepository<UserStats, String> {

    // Create stats - handled by save() method

    // Get stats for a user
    Optional<UserStats> findByUserId(String userId);

    // Increment followers count
    @Modifying
    @Query("UPDATE UserStats us SET us.followersCount = us.followersCount + 1 WHERE us.userId = :userId")
    int incrementFollowers(@Param("userId") String userId);

    // Decrement followers count
    @Modifying
    @Query("UPDATE UserStats us SET us.followersCount = CASE WHEN us.followersCount > 0 THEN us.followersCount - 1 ELSE 0 END WHERE us.userId = :userId")
    int decrementFollowers(@Param("userId") String userId);

    // Increment following count
    @Modifying
    @Query("UPDATE UserStats us SET us.followingCount = us.followingCount + 1 WHERE us.userId = :userId")
    int incrementFollowing(@Param("userId") String userId);

    // Decrement following count
    @Modifying
    @Query("UPDATE UserStats us SET us.followingCount = CASE WHEN us.followingCount > 0 THEN us.followingCount - 1 ELSE 0 END WHERE us.userId = :userId")
    int decrementFollowing(@Param("userId") String userId);

    // Check if stats exist for a user
    boolean existsByUserId(String userId);

    // Get users with most followers
    @Query("SELECT us FROM UserStats us ORDER BY us.followersCount DESC")
    List<UserStats> findTopUsersByFollowers();

    // Get users with most following
    @Query("SELECT us FROM UserStats us ORDER BY us.followingCount DESC")
    List<UserStats> findTopUsersByFollowing();
}