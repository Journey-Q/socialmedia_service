// FollowRequest.java
package org.example.socialmedia_services.dto.follow;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FollowRequest {

    @NotBlank(message = "Follower user ID is required")
    private String followerId; // The user to follow

}