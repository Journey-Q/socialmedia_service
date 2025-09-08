package org.example.socialmedia_services.entity.jointTrip;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trip_day_by_day_itinerary")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TripDayByDayItinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itinerary_id")
    private Long itineraryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private JointTrip jointTrip;

    @Column(name = "day_no", nullable = false)
    private Integer dayNo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "trip_visiting_places",
            joinColumns = @JoinColumn(name = "itinerary_id")
    )
    @Column(name = "place_name", length = 200)
    @Builder.Default
    private List<String> visitingPlaces = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "trip_accommodations",
            joinColumns = @JoinColumn(name = "itinerary_id")
    )
    @Column(name = "accommodation_name", length = 200)
    @Builder.Default
    private List<String> accommodations = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "trip_restaurants",
            joinColumns = @JoinColumn(name = "itinerary_id")
    )
    @Column(name = "restaurant_name", length = 200)
    @Builder.Default
    private List<String> restaurants = new ArrayList<>();

    @Column(name = "other_details", length = 1000)
    private String otherDetails;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
    public void addVisitingPlace(String place) {
        if (this.visitingPlaces == null) {
            this.visitingPlaces = new ArrayList<>();
        }
        if (!this.visitingPlaces.contains(place)) {
            this.visitingPlaces.add(place);
        }
    }

    public void removeVisitingPlace(String place) {
        if (this.visitingPlaces != null) {
            this.visitingPlaces.remove(place);
        }
    }

    public void addAccommodation(String accommodation) {
        if (this.accommodations == null) {
            this.accommodations = new ArrayList<>();
        }
        if (!this.accommodations.contains(accommodation)) {
            this.accommodations.add(accommodation);
        }
    }

    public void removeAccommodation(String accommodation) {
        if (this.accommodations != null) {
            this.accommodations.remove(accommodation);
        }
    }

    public void addRestaurant(String restaurant) {
        if (this.restaurants == null) {
            this.restaurants = new ArrayList<>();
        }
        if (!this.restaurants.contains(restaurant)) {
            this.restaurants.add(restaurant);
        }
    }

    public void removeRestaurant(String restaurant) {
        if (this.restaurants != null) {
            this.restaurants.remove(restaurant);
        }
    }
}