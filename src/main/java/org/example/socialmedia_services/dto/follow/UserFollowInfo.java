package org.example.socialmedia_services.dto.follow;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserFollowInfo {

    private String userId;
    private String displayName;
    private String profileImageUrl;
    private String status; // pending, accepted, rejected
    private Boolean isMutualFollow;
    private LocalDateTime createdAt;
}