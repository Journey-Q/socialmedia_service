package org.example.socialmedia_services.entity.jointTrip;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "joint_trip")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JointTrip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // Creator of the trip

    @Column(name = "group_id")
    private Long groupId; // Will be set when group is formed

    @Column(name = "trip_title", nullable = false, length = 200)
    private String tripTitle;

    @Column(name = "trip_destination", nullable = false, length = 100)
    private String tripDestination;

    @Column(name = "trip_description", length = 1000)
    private String tripDescription;

    @Column(name = "trip_start_date", nullable = false)
    private LocalDateTime tripStartDate;

    @Column(name = "trip_end_date", nullable = false)
    private LocalDateTime tripEndDate;

    @Column(name = "trip_duration", nullable = false)
    private Integer tripDuration; // in days

    @Column(name = "trip_type", nullable = false, length = 50)
    private String tripType;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_group_formed", nullable = false)
    @Builder.Default
    private Boolean isGroupFormed = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // One-to-many relationship with day-by-day itinerary
    @OneToMany(mappedBy = "jointTrip", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<TripDayByDayItinerary> dayByDayItineraries = new ArrayList<>();

    // Helper methods
    public void addDayItinerary(TripDayByDayItinerary itinerary) {
        if (this.dayByDayItineraries == null) {
            this.dayByDayItineraries = new ArrayList<>();
        }
        itinerary.setJointTrip(this);
        this.dayByDayItineraries.add(itinerary);
    }

    public void removeDayItinerary(TripDayByDayItinerary itinerary) {
        if (this.dayByDayItineraries != null) {
            this.dayByDayItineraries.remove(itinerary);
            itinerary.setJointTrip(null);
        }
    }

    public void markGroupFormed(Long groupId) {
        this.groupId = groupId;
        this.isGroupFormed = true;
    }

    public void deactivateTrip() {
        this.isActive = false;
    }
}