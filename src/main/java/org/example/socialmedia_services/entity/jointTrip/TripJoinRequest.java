package org.example.socialmedia_services.entity.jointTrip;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "trip_join_request")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TripJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId; // User who created and sent the trip request

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId; // User who received the trip request

    @Column(name = "group_id")
    private Long groupId; // Group ID associated with the trip

    @Column(name = "request_status", nullable = false, length = 20)
    @Builder.Default
    private String requestStatus = "PENDING"; // PENDING, ACCEPTED, REJECTED, CANCELLED

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    // Helper methods
    public void acceptRequest() {
        this.requestStatus = "ACCEPTED";
        this.respondedAt = LocalDateTime.now();
    }

    public void rejectRequest() {
        this.requestStatus = "REJECTED";
        this.respondedAt = LocalDateTime.now();
    }

    public void cancelRequest() {
        this.requestStatus = "CANCELLED";
        this.isActive = false;
    }
}