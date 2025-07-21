package org.example.socialmedia_services.dto.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor


public class ProfileSetupdtoRequest {
    @NotNull(message = "User ID is required")
    @JsonProperty("user_id")
    private String userId;

    @NotBlank(message = "Display name is required")
    @Size(min = 1, max = 50, message = "Display name must be between 1 and 50 characters")
    @JsonProperty("display_name")
    private String displayName;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    @JsonProperty("bio")
    private String bio;

    @JsonProperty("favourite_activities")
    private List<String> favouriteActivities;

    @JsonProperty("preferred_trip_moods")
    private List<String> preferredTripMoods;

    @URL(message = "Profile image URL must be a valid URL")
    @JsonProperty("profile_image_url")
    private String profileImageUrl;
}
