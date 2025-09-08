package org.example.socialmedia_services.entity.follow;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserStats {

    @Id
    @Column(name = "user_id", nullable = false, length = 255)
    private String userId; // References user_profiles.user_id

    @Column(name = "followers_count", nullable = false)
    @Builder.Default
    private Integer followersCount = 0;

    @Column(name = "following_count", nullable = false)
    @Builder.Default
    private Integer followingCount = 0;

    public UserStats(String userId) {
        this.userId = userId;
        this.followersCount = 0;
        this.followingCount = 0;
    }

    // Helper methods to increment/decrement counts
    public void incrementFollowers() {
        this.followersCount++;
    }

    public void decrementFollowers() {
        if (this.followersCount > 0) {
            this.followersCount--;
        }
    }

    public void incrementFollowing() {
        this.followingCount++;
    }

    public void decrementFollowing() {
        if (this.followingCount > 0) {
            this.followingCount--;
        }
    }

    @Override
    public String toString() {
        return "UserStats{" +
                "userId='" + userId + '\'' +
                ", followersCount=" + followersCount +
                ", followingCount=" + followingCount +
                '}';
    }
}