package org.example.socialmedia_services.repository.jointTrip;

import org.example.socialmedia_services.entity.jointTrip.JointTrip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JointTripRepository extends JpaRepository<JointTrip, Long> {

    // Basic queries
    @Query("SELECT jt FROM JointTrip jt WHERE jt.tripId = :tripId AND jt.isActive = true")
    Optional<JointTrip> findActiveByTripId(@Param("tripId") Long tripId);

    @Query("SELECT jt FROM JointTrip jt WHERE jt.userId = :userId AND jt.isActive = true ORDER BY jt.createdAt DESC")
    List<JointTrip> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT jt FROM JointTrip jt WHERE jt.groupId = :groupId AND jt.isActive = true")
    List<JointTrip> findActiveByGroupId(@Param("groupId") Long groupId);

    // Search queries
    @Query("SELECT jt FROM JointTrip jt WHERE " +
            "(LOWER(jt.tripTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(jt.tripDestination) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(jt.tripDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "jt.isActive = true ORDER BY jt.createdAt DESC")
    List<JointTrip> searchTrips(@Param("searchTerm") String searchTerm);

    @Query("SELECT jt FROM JointTrip jt WHERE " +
            "LOWER(jt.tripDestination) LIKE LOWER(CONCAT('%', :destination, '%')) AND " +
            "jt.isActive = true ORDER BY jt.createdAt DESC")
    List<JointTrip> findByDestination(@Param("destination") String destination);

    @Query("SELECT jt FROM JointTrip jt WHERE " +
            "LOWER(jt.tripType) = LOWER(:tripType) AND " +
            "jt.isActive = true ORDER BY jt.createdAt DESC")
    List<JointTrip> findByTripType(@Param("tripType") String tripType);

    // Date-based queries
    @Query("SELECT jt FROM JointTrip jt WHERE " +
            "jt.tripStartDate >= :startDate AND jt.tripEndDate <= :endDate AND " +
            "jt.isActive = true ORDER BY jt.tripStartDate ASC")
    List<JointTrip> findTripsByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT jt FROM JointTrip jt WHERE " +
            "jt.tripStartDate >= :currentDate AND " +
            "jt.isActive = true ORDER BY jt.tripStartDate ASC")
    List<JointTrip> findUpcomingTrips(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT jt FROM JointTrip jt WHERE " +
            "jt.tripEndDate < :currentDate AND " +
            "jt.isActive = true ORDER BY jt.tripEndDate DESC")
    List<JointTrip> findPastTrips(@Param("currentDate") LocalDateTime currentDate);

    // Duration-based queries
    @Query("SELECT jt FROM JointTrip jt WHERE " +
            "jt.tripDuration >= :minDuration AND jt.tripDuration <= :maxDuration AND " +
            "jt.isActive = true ORDER BY jt.createdAt DESC")
    List<JointTrip> findTripsByDurationRange(@Param("minDuration") Integer minDuration,
                                             @Param("maxDuration") Integer maxDuration);

    // Group formation queries
    @Query("SELECT jt FROM JointTrip jt WHERE " +
            "jt.isGroupFormed = false AND jt.isActive = true AND " +
            "jt.tripStartDate > :currentDate ORDER BY jt.createdAt DESC")
    List<JointTrip> findAvailableTripsForJoining(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT jt FROM JointTrip jt WHERE " +
            "jt.isGroupFormed = true AND jt.isActive = true ORDER BY jt.createdAt DESC")
    List<JointTrip> findTripsWithFormedGroups();

    // Statistics queries
    @Query("SELECT COUNT(jt) FROM JointTrip jt WHERE jt.userId = :userId AND jt.isActive = true")
    long countActiveTripsForUser(@Param("userId") Long userId);

    @Query("SELECT jt.tripType, COUNT(jt) FROM JointTrip jt WHERE jt.isActive = true GROUP BY jt.tripType ORDER BY COUNT(jt) DESC")
    List<Object[]> getMostPopularTripTypes();

    @Query("SELECT jt.tripDestination, COUNT(jt) FROM JointTrip jt WHERE jt.isActive = true GROUP BY jt.tripDestination ORDER BY COUNT(jt) DESC")
    List<Object[]> getMostPopularDestinations();

    // Update queries
    @Modifying
    @Query("UPDATE JointTrip jt SET jt.isActive = false, jt.updatedAt = CURRENT_TIMESTAMP WHERE jt.tripId = :tripId")
    int deactivateTrip(@Param("tripId") Long tripId);

    @Modifying
    @Query("UPDATE JointTrip jt SET jt.groupId = :groupId, jt.isGroupFormed = true, jt.updatedAt = CURRENT_TIMESTAMP WHERE jt.tripId = :tripId")
    int markGroupFormed(@Param("tripId") Long tripId, @Param("groupId") Long groupId);

    @Modifying
    @Query("UPDATE JointTrip jt SET jt.tripTitle = :title, jt.updatedAt = CURRENT_TIMESTAMP WHERE jt.tripId = :tripId")
    int updateTripTitle(@Param("tripId") Long tripId, @Param("title") String title);

    @Modifying
    @Query("UPDATE JointTrip jt SET jt.tripDescription = :description, jt.updatedAt = CURRENT_TIMESTAMP WHERE jt.tripId = :tripId")
    int updateTripDescription(@Param("tripId") Long tripId, @Param("description") String description);
}