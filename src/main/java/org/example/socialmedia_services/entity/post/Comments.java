package org.example.socialmedia_services.entity.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "post_comments",
        indexes = {
                @Index(name = "idx_comment_post_id", columnList = "post_id"),
                @Index(name = "idx_comment_user_id", columnList = "user_id"),
                @Index(name = "idx_comment_parent_id", columnList = "parent_id"),
                @Index(name = "idx_comment_post_created", columnList = "post_id, commented_at")
        })
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

    @Column(name = "parent_id")
    private Long parentId; // For replies to comments

    @Column(name = "commented_at")
    @CreationTimestamp
    private LocalDateTime commentedAt;

    // Default constructor
    public Comments() {
        this.commentedAt = LocalDateTime.now();
    }

    // Constructor with parameters for top-level comments
    public Comments(Long postId, Long userId, String commentText) {
        this();
        this.postId = postId;
        this.userId = userId;
        this.commentText = commentText;
        this.parentId = null; // Top-level comment
    }

    // Constructor with parameters for reply comments
    public Comments(Long postId, Long userId, String commentText, Long parentId) {
        this();
        this.postId = postId;
        this.userId = userId;
        this.commentText = commentText;
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return "Comments{" +
                "commentId=" + commentId +
                ", postId=" + postId +
                ", userId=" + userId +
                ", commentText='" + commentText + '\'' +
                ", parentId=" + parentId +
                ", commentedAt=" + commentedAt +
                '}';
    }
}