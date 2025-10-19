package org.example.socialmedia_services.entity.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@Table(name = "post_content",
        indexes = {
                @Index(name = "idx_post_content_id", columnList = "post_id"),
                @Index(name = "idx_journey_title", columnList = "journey_title")
        })
public class PostContent {

    @Id
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "journey_title")
    private String journeyTitle;

    @Column(name = "number_of_days")
    private Integer numberOfDays;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "places_visited", columnDefinition = "jsonb")
    private List<String> placesVisited;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "budget_info", columnDefinition = "jsonb")
    private Map<String, Object> budgetInfo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "travel_tips", columnDefinition = "jsonb")
    private List<String> travelTips;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "transportation_options", columnDefinition = "jsonb")
    private List<String> transportationOptions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hotel_recommendations", columnDefinition = "jsonb")
    private List<Map<String, Object>> hotelRecommendations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "restaurant_recommendations", columnDefinition = "jsonb")
    private List<Map<String, Object>> restaurantRecommendations;

    // One-to-Many relationship with PlaceWiseContent
    // PlaceWiseContent.post_id references PostContent.post_id
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "post_id") // Foreign key in place_wise_content table
    @OrderBy("sequenceOrder ASC")
    private List<PlaceWiseContent> placeWiseContentList;

    // Default constructor
    public PostContent() {}
}