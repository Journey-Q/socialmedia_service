package org.example.socialmedia_services.entity.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "post_likes")
public class Likes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long likeId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "liked_at")
    @CreationTimestamp
    private LocalDateTime likedAt;

    // Default constructor
    public Likes() {
        this.likedAt = LocalDateTime.now();
    }

    // Constructor with parameters
    public Likes(Long postId, Long userId) {
        this();
        this.postId = postId;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Likes{" +
                "likeId=" + likeId +
                ", postId=" + postId +
                ", userId=" + userId +
                ", likedAt=" + likedAt +
                '}';
    }
}
