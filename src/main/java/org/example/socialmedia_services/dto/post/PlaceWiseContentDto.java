package org.example.socialmedia_services.dto.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PlaceWiseContentDto {

    @NotBlank(message = "Place name is required")
    private String placeName;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private String tripMood;
    private List<String> activities;
    private List<String> experiences;
    private List<String> imageUrls;
    private Integer sequenceOrder;

    // Default constructor
    public PlaceWiseContentDto() {}

    // Constructor with parameters
    public PlaceWiseContentDto(String placeName, BigDecimal latitude, BigDecimal longitude) {
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
