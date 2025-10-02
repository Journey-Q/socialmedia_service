package org.example.socialmedia_services.dto.follow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowRequestUserInfo {

    private Long followId;  // Added for accept/reject operations
    private String userId;
    private String displayName;
    private String profileImageUrl;
    private LocalDateTime requestedAt;
}