package org.example.socialmedia_services.dto.follow;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FollowResponse {

    private Long id;
    private String followerId;
    private String followingId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}