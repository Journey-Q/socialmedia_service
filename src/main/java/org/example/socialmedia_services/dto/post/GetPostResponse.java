package org.example.socialmedia_services.dto.post;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Getter
@Setter
public class GetPostResponse {

    private Long postId;
    private Long createdById;
    private String creatorUsername;
    private String creatorProfileUrl;
    private LocalDateTime createdAt;
    private Integer likesCount;
    private Integer commentsCount;

    // Post Content Details
    private String journeyTitle;
    private Integer numberOfDays;
    private List<String> placesVisited;
    private List<PlaceWiseContentDto> placeWiseContent;
    private Map<String, Object> budgetInfo;
    private List<String> travelTips;
    private List<String> transportationOptions;
    private List<Map<String, Object>> hotelRecommendations;
    private List<Map<String, Object>> restaurantRecommendations;

    // Default constructor
    public GetPostResponse() {}

}