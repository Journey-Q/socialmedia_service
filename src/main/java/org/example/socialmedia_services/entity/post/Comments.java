package org.example.socialmedia_services.entity.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "post_comments")
public class Comments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String commentText;

    @Column(name = "commented_at")
    @CreationTimestamp
    private LocalDateTime commentedAt;

    // Default constructor
    public Comments() {
        this.commentedAt = LocalDateTime.now();
    }

    // Constructor with parameters
    public Comments(Long postId, Long userId, String commentText) {
        this();
        this.postId = postId;
        this.userId = userId;
        this.commentText = commentText;
    }

    @Override
    public String toString() {
        return "Comments{" +
                "commentId=" + commentId +
                ", postId=" + postId +
                ", userId=" + userId +
                ", commentText='" + commentText + '\'' +
                ", commentedAt=" + commentedAt +
                '}';
    }
}
