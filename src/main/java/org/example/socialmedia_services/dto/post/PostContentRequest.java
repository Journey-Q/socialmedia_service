package org.example.socialmedia_services.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PostContentRequest {

    @NotBlank(message = "Journey title is required")
    private String journeyTitle;

    @NotNull(message = "Number of days is required")
    @Positive(message = "Number of days must be positive")
    private Integer numberOfDays;

    private List<String> placesVisited;

    @Valid
    private List<PlaceWiseContentDto> placeWiseContent;

    private Map<String, Object> budgetInfo;
    private List<String> travelTips;
    private List<String> transportationOptions;
    private List<Map<String, Object>> hotelRecommendations;
    private List<Map<String, Object>> restaurantRecommendations;

    // Default constructor
    public PostContentRequest() {}

}