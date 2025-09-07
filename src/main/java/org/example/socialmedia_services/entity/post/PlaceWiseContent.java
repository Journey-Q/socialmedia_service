package org.example.socialmedia_services.entity.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "place_wise_content")
public class PlaceWiseContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_wise_content_id")
    private Long placeWiseContentId;

    // Foreign key referencing post_content.post_id
    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "place_name", nullable = false)
    private String placeName;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "address")
    private String address;

    @Column(name = "trip_mood")
    private String tripMood;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "activities", columnDefinition = "jsonb")
    private List<String> activities;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "experiences", columnDefinition = "jsonb")
    private List<String> experiences;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_urls", columnDefinition = "jsonb")
    private List<String> imageUrls;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    // Default constructor
    public PlaceWiseContent() {}

    // Constructor with parameters
    public PlaceWiseContent(Long postId, String placeName) {
        this.postId = postId;
        this.placeName = placeName;
    }
}