package org.example.socialmedia_services.entity.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts",
        indexes = {
                @Index(name = "idx_created_by_id", columnList = "created_by_id"),
                @Index(name = "idx_created_at", columnList = "created_at"),
                @Index(name = "idx_created_by_created_at", columnList = "created_by_id, created_at"),
                @Index(name = "idx_likes_count", columnList = "likes_count"),
                @Index(name = "idx_comments_count", columnList = "comments_count")
        })
@Getter @Setter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "created_by_id", nullable = false)
    private Long createdById;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "likes_count")
    private Integer likesCount = 0;

    @Column(name = "comments_count")
    private Integer commentsCount = 0;

    // PostContent is mapped by post_id (PostContent.post_id references Post.post_id)
    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PostContent postContent;

    // Default constructor
    public Post() {
        this.createdAt = LocalDateTime.now();
        this.likesCount = 0;
        this.commentsCount = 0;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postId=" + postId +
                ", createdById=" + createdById +
                ", createdAt=" + createdAt +
                ", likesCount=" + likesCount +
                ", commentsCount=" + commentsCount +
                '}';
    }
}