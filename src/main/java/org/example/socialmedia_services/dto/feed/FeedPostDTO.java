package org.example.socialmedia_services.dto.feed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedPostDTO {
    private Long postId;
    private Long createdById;
    private String creatorName;
    private String creatorProfileUrl;
    private LocalDateTime createdAt;
    private Integer likesCount;
    private Integer commentsCount;
    private Boolean isLikedByUser;

    // Post content
    private String journeyTitle;
    private Integer numberOfDays;
    private List<String> placesVisited;
    private Map<String, Object> budgetInfo;
    private List<String> travelTips;
    private List<String> transportationOptions;
    private List<Map<String, Object>> hotelRecommendations;
    private List<Map<String, Object>> restaurantRecommendations;

    // Place-wise content
    private List<PlaceWiseContentDTO> placeWiseContent;

    // Feed ranking metadata (optional, for debugging)
    private Double rankScore;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceWiseContentDTO {
        private Long placeWiseContentId;
        private String placeName;
        private Double latitude;
        private Double longitude;
        private String address;
        private String tripMood;
        private List<String> activities;
        private List<String> experiences;
        private List<String> imageUrls;
        private Integer sequenceOrder;
    }
}
