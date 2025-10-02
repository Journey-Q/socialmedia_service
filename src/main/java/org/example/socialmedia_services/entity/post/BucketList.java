package org.example.socialmedia_services.entity.post;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bucket_lists")
public class BucketList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bucket_list_id")
    private Long bucketListId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bucket_list_items", columnDefinition = "jsonb")
    private List<BucketListItemData> bucketListItems;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public BucketList() {
        this.createdAt = LocalDateTime.now();
    }

    public BucketList(Long userId, List<BucketListItemData> bucketListItems) {
        this.userId = userId;
        this.bucketListItems = bucketListItems;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getBucketListId() {
        return bucketListId;
    }

    public void setBucketListId(Long bucketListId) {
        this.bucketListId = bucketListId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<BucketListItemData> getBucketListItems() {
        return bucketListItems;
    }

    public void setBucketListItems(List<BucketListItemData> bucketListItems) {
        this.bucketListItems = bucketListItems;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Inner class for JSON data
    public static class BucketListItemData {
        private String postId;
        private boolean isVisited;
        private LocalDateTime visitedDate;

        // Constructors
        public BucketListItemData() {}

        public BucketListItemData(String postId, boolean isVisited, LocalDateTime visitedDate) {
            this.postId = postId;
            this.isVisited = isVisited;
            this.visitedDate = visitedDate;
        }

        // Getters and Setters
        public String getPostId() {
            return postId;
        }

        public void setPostId(String postId) {
            this.postId = postId;
        }

        public boolean isVisited() {
            return isVisited;
        }

        public void setVisited(boolean visited) {
            isVisited = visited;
        }

        public LocalDateTime getVisitedDate() {
            return visitedDate;
        }

        public void setVisitedDate(LocalDateTime visitedDate) {
            this.visitedDate = visitedDate;
        }
    }
}