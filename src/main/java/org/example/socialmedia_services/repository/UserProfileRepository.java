package org.example.socialmedia_services.repository;

import org.example.socialmedia_services.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    // Basic profile queries
    @Query("SELECT up FROM UserProfile up WHERE up.userId = :userId AND up.isActive = true")
    Optional<UserProfile> findActiveByUserId(@Param("userId") String userId);

    @Query("SELECT up FROM UserProfile up WHERE up.isActive = :isActive")
    Page<UserProfile> findAllByIsActive(@Param("isActive") Boolean isActive, Pageable pageable);

    @Query("SELECT up FROM UserProfile up WHERE up.displayName = :displayName AND up.isActive = true")
    Optional<UserProfile> findByDisplayName(@Param("displayName") String displayName);

    @Query("SELECT COUNT(up) FROM UserProfile up WHERE up.setupCompleted = true AND up.isActive = true")
    long countCompletedProfiles();

    // Search functionality for SearchService
    @Query("SELECT up FROM UserProfile up WHERE " +
            "LOWER(up.displayName) LIKE LOWER(CONCAT('%', :displayName, '%')) AND " +
            "up.isActive = true")
    List<UserProfile> findByDisplayNameContainingIgnoreCaseAndIsActiveTrue(
            @Param("displayName") String displayName,
            Pageable pageable);

    // Activity-based queries
    @Query("SELECT up FROM UserProfile up JOIN up.favouriteActivities fa WHERE fa IN :activities AND up.isActive = true")
    List<UserProfile> findByFavouriteActivities(@Param("activities") List<String> activities);

    @Query("SELECT up FROM UserProfile up JOIN up.favouriteActivities fa WHERE fa = :activity AND up.isActive = true")
    List<UserProfile> findByFavouriteActivity(@Param("activity") String activity);

    // Trip mood-based queries
    @Query("SELECT up FROM UserProfile up JOIN up.preferredTripMoods ptm WHERE ptm IN :moods AND up.isActive = true")
    List<UserProfile> findByPreferredTripMoods(@Param("moods") List<String> moods);

    @Query("SELECT up FROM UserProfile up JOIN up.preferredTripMoods ptm WHERE ptm = :mood AND up.isActive = true")
    List<UserProfile> findByPreferredTripMood(@Param("mood") String mood);

    // Combined activity and mood queries
    @Query("SELECT DISTINCT up FROM UserProfile up " +
            "JOIN up.favouriteActivities fa " +
            "JOIN up.preferredTripMoods ptm " +
            "WHERE fa IN :activities AND ptm IN :moods AND up.isActive = true")
    List<UserProfile> findByActivitiesAndMoods(
            @Param("activities") List<String> activities,
            @Param("moods") List<String> moods
    );

    // Search queries
    @Query("SELECT up FROM UserProfile up WHERE " +
            "(LOWER(up.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(up.bio) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "up.isActive = true")
    List<UserProfile> searchProfiles(@Param("searchTerm") String searchTerm);

    // Statistics queries
    @Query("SELECT fa, COUNT(fa) FROM UserProfile up JOIN up.favouriteActivities fa WHERE up.isActive = true GROUP BY fa ORDER BY COUNT(fa) DESC")
    List<Object[]> getMostPopularActivities();

    @Query("SELECT ptm, COUNT(ptm) FROM UserProfile up JOIN up.preferredTripMoods ptm WHERE up.isActive = true GROUP BY ptm ORDER BY COUNT(ptm) DESC")
    List<Object[]> getMostPopularTripMoods();

    @Modifying
    @Query("UPDATE UserProfile up SET up.isActive = false, up.updatedAt = CURRENT_TIMESTAMP WHERE up.userId = :userId")
    int deactivateProfile(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE UserProfile up SET up.profileImageUrl = :imageUrl, up.updatedAt = CURRENT_TIMESTAMP WHERE up.userId = :userId")
    int updateProfileImage(@Param("userId") String userId, @Param("imageUrl") String imageUrl);

    @Modifying
    @Query("UPDATE UserProfile up SET up.bio = :bio, up.updatedAt = CURRENT_TIMESTAMP WHERE up.userId = :userId")
    int updateBio(@Param("userId") String userId, @Param("bio") String bio);

    @Modifying
    @Query("UPDATE UserProfile up SET up.displayName = :displayName, up.updatedAt = CURRENT_TIMESTAMP WHERE up.userId = :userId")
    int updateDisplayName(@Param("userId") String userId, @Param("displayName") String displayName);

    // Recommendation queries (for finding similar users)
    @Query("SELECT up FROM UserProfile up " +
            "JOIN up.favouriteActivities fa " +
            "WHERE fa IN (SELECT fa2 FROM UserProfile up2 JOIN up2.favouriteActivities fa2 WHERE up2.userId = :userId) " +
            "AND up.userId != :userId AND up.isActive = true " +
            "GROUP BY up.userId " +
            "ORDER BY COUNT(fa) DESC")
    List<UserProfile> findSimilarUsersByActivities(@Param("userId") String userId);

    @Query("SELECT up FROM UserProfile up " +
            "JOIN up.preferredTripMoods ptm " +
            "WHERE ptm IN (SELECT ptm2 FROM UserProfile up2 JOIN up2.preferredTripMoods ptm2 WHERE up2.userId = :userId) " +
            "AND up.userId != :userId AND up.isActive = true " +
            "GROUP BY up.userId " +
            "ORDER BY COUNT(ptm) DESC")
    List<UserProfile> findSimilarUsersByTripMoods(@Param("userId") String userId);
}