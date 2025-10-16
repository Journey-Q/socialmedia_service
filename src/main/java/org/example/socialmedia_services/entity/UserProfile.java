package org.example.socialmedia_services.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profiles",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_display_name", columnList = "display_name"),
                @Index(name = "idx_is_active", columnList = "is_active"),
                @Index(name = "idx_active_display_name", columnList = "is_active, display_name")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class UserProfile {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "setup_completed", nullable = false)
    @Builder.Default
    private Boolean setupCompleted = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_favourite_activities",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "activity", length = 50)
    @Builder.Default
    private List<String> favouriteActivities = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_preferred_trip_moods",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "mood", length = 50)
    @Builder.Default
    private List<String> preferredTripMoods = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Boolean isPremium = false;

    private Boolean isTripFluence = false;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Version
    @Column(name = "version")
    private Long version;

    // Helper methods
    public void addFavouriteActivity(String activity) {
        if (this.favouriteActivities == null) {
            this.favouriteActivities = new ArrayList<>();
        }
        if (!this.favouriteActivities.contains(activity)) {
            this.favouriteActivities.add(activity);
        }
    }

    public void removeFavouriteActivity(String activity) {
        if (this.favouriteActivities != null) {
            this.favouriteActivities.remove(activity);
        }
    }

    public void addPreferredTripMood(String mood) {
        if (this.preferredTripMoods == null) {
            this.preferredTripMoods = new ArrayList<>();
        }
        if (!this.preferredTripMoods.contains(mood)) {
            this.preferredTripMoods.add(mood);
        }
    }

    public void removePreferredTripMood(String mood) {
        if (this.preferredTripMoods != null) {
            this.preferredTripMoods.remove(mood);
        }
    }

    public void markSetupComplete() {
        this.setupCompleted = true;
    }

    public boolean hasProfileImage() {
        return this.profileImageUrl != null && !this.profileImageUrl.trim().isEmpty();
    }
}

