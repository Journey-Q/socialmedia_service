package org.example.socialmedia_services.dto.post;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetStatResponse {

    private String userId;
    private Integer followersCount;
    private Integer postsCount;
    private Integer followingCount;

}