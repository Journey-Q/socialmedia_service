package org.example.socialmedia_services.dto.follow;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserStatsResponse {

    private String userId;
    private Integer followersCount;
    private Integer followingCount;
}