package org.example.socialmedia_services.dto.follow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {

    private String userId;
    private Integer followersCount;
    private Integer followingCount;
    private Integer postsCount;
}