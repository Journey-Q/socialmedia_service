package org.example.socialmedia_services.dto.post;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// Response DTO for the complete bucket list
@Getter
@Setter
public class GetBucketListResponse {
    private Long bucketListId;
    private Long userId;
    private List<BucketListItemResponse> bucketListItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public GetBucketListResponse() {}

    public GetBucketListResponse(Long bucketListId, Long userId, List<BucketListItemResponse> bucketListItems,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.bucketListId = bucketListId;
        this.userId = userId;
        this.bucketListItems = bucketListItems;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "GetBucketListResponse{" +
                "bucketListId=" + bucketListId +
                ", userId=" + userId +
                ", bucketListItems=" + bucketListItems +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

// Request DTO for adding post to bucket list


// Request DTO for updating visited status
