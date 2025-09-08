package org.example.socialmedia_services.dto.follow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FollowStatusRequest {

    @NotBlank(message = "Following user ID is required")
    private String followingId;

    @NotBlank(message = "Follower user ID is required")
    private String followerId;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "accepted|rejected", message = "Status must be either 'accepted' or 'rejected'")
    private String status;
}