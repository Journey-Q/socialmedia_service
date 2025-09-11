package org.example.socialmedia_services.dto.profile;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponseDTO {

    @JsonProperty("user_id")
    private String userId;



    @JsonProperty("display_name")
    private String displayName;


    @JsonProperty("bio")
    private String bio;

    @JsonProperty("favourite_activities")
    private List<String> favouriteActivities;

    @JsonProperty("preferred_trip_moods")
    private List<String> preferredTripMoods;


    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    private Boolean isPremium ;

    private Boolean isTripFluence ;
}
