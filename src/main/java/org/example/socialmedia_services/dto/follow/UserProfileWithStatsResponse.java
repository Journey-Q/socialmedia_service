package org.example.socialmedia_services.dto.follow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileWithStatsResponse {

    private String userId;
    private String displayName;
    private String bio;
    private String profileImageUrl;
    private Boolean setupCompleted;
    private List<String> favouriteActivities;
    private List<String> preferredTripMoods;
    private LocalDateTime createdAt;
    private Boolean isPremium;
    private Boolean isTripFluence;

    // Stats
    private Integer followersCount;
    private Integer followingCount;
    private Integer postsCount;
    private Integer likesCount;
}