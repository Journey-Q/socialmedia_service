package org.example.socialmedia_services.entity.follow;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "follows",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"follower_id", "following_id"})
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id", nullable = false, length = 255)
    private String followerId; // ID of the user being followed

    @Column(name = "following_id", nullable = false, length = 255)
    private String followingId; // ID of the user following someone

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "pending"; // pending, accepted, rejected

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Follow(String followingId, String followerId, String status) {
        this.followingId = followingId;
        this.followerId = followerId;
        this.status = status != null ? status : "pending";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Follow{" +
                "id=" + id +
                ", followerId='" + followerId + '\'' +
                ", followingId='" + followingId + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}