package org.example.socialmedia_services.dto.post;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// Response DTO for individual bucket list items
@Getter
@Setter
public class BucketListItemResponse {
    private String destination;
    private String image;
    private boolean isCompleted;
    private LocalDateTime visitedDate;
    private String description;
    private String journeyId;

    // Constructors
    public BucketListItemResponse() {}

    public BucketListItemResponse(String destination, String image, boolean isCompleted,
                                  LocalDateTime visitedDate, String description, String journeyId) {
        this.destination = destination;
        this.image = image;
        this.isCompleted = isCompleted;
        this.visitedDate = visitedDate;
        this.description = description;
        this.journeyId = journeyId;
    }

    @Override
    public String toString() {
        return "BucketListItemResponse{" +
                "destination='" + destination + '\'' +
                ", image='" + image + '\'' +
                ", isCompleted=" + isCompleted +
                ", visitedDate=" + visitedDate +
                ", description='" + description + '\'' +
                ", journeyId='" + journeyId + '\'' +
                '}';
    }
}
