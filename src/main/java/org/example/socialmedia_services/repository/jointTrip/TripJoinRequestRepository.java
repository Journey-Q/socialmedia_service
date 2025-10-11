package org.example.socialmedia_services.repository.jointTrip;

import org.example.socialmedia_services.entity.jointTrip.TripJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripJoinRequestRepository extends JpaRepository<TripJoinRequest, Long> {

    // Find request by ID
    @Query("SELECT tjr FROM TripJoinRequest tjr WHERE tjr.requestId = :requestId AND tjr.isActive = true")
    Optional<TripJoinRequest> findActiveById(@Param("requestId") Long requestId);

    // Find all requests for a specific trip
    @Query("SELECT tjr FROM TripJoinRequest tjr WHERE tjr.tripId = :tripId AND tjr.isActive = true ORDER BY tjr.createdAt DESC")
    List<TripJoinRequest> findByTripId(@Param("tripId") Long tripId);

    // Find all requests sent by a user
    @Query("SELECT tjr FROM TripJoinRequest tjr WHERE tjr.senderId = :senderId AND tjr.isActive = true ORDER BY tjr.createdAt DESC")
    List<TripJoinRequest> findBySenderId(@Param("senderId") Long senderId);

    // Find all requests received by a user
    @Query("SELECT tjr FROM TripJoinRequest tjr WHERE tjr.receiverId = :receiverId AND tjr.isActive = true ORDER BY tjr.createdAt DESC")
    List<TripJoinRequest> findByReceiverId(@Param("receiverId") Long receiverId);

    // Find pending requests for a receiver
    @Query("SELECT tjr FROM TripJoinRequest tjr WHERE tjr.receiverId = :receiverId AND tjr.requestStatus = 'PENDING' AND tjr.isActive = true ORDER BY tjr.createdAt DESC")
    List<TripJoinRequest> findPendingRequestsByReceiverId(@Param("receiverId") Long receiverId);

    // Check if request already exists
    @Query("SELECT tjr FROM TripJoinRequest tjr WHERE tjr.tripId = :tripId AND tjr.senderId = :senderId AND tjr.receiverId = :receiverId AND tjr.isActive = true")
    Optional<TripJoinRequest> findExistingRequest(@Param("tripId") Long tripId,
                                                  @Param("senderId") Long senderId,
                                                  @Param("receiverId") Long receiverId);

    // Find requests by status
    @Query("SELECT tjr FROM TripJoinRequest tjr WHERE tjr.requestStatus = :status AND tjr.isActive = true ORDER BY tjr.createdAt DESC")
    List<TripJoinRequest> findByStatus(@Param("status") String status);

    // Find accepted requests for a trip
    @Query("SELECT tjr FROM TripJoinRequest tjr WHERE tjr.tripId = :tripId AND tjr.requestStatus = 'ACCEPTED' AND tjr.isActive = true")
    List<TripJoinRequest> findAcceptedRequestsByTripId(@Param("tripId") Long tripId);

    // Count pending requests for a user
    @Query("SELECT COUNT(tjr) FROM TripJoinRequest tjr WHERE tjr.receiverId = :receiverId AND tjr.requestStatus = 'PENDING' AND tjr.isActive = true")
    long countPendingRequestsByReceiverId(@Param("receiverId") Long receiverId);

    // Update request status
    @Modifying
    @Query("UPDATE TripJoinRequest tjr SET tjr.requestStatus = :status, tjr.respondedAt = CURRENT_TIMESTAMP WHERE tjr.requestId = :requestId")
    int updateRequestStatus(@Param("requestId") Long requestId, @Param("status") String status);

    // Deactivate request
    @Modifying
    @Query("UPDATE TripJoinRequest tjr SET tjr.isActive = false WHERE tjr.requestId = :requestId")
    int deactivateRequest(@Param("requestId") Long requestId);
}