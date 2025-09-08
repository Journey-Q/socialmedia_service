package org.example.socialmedia_services.dto.follow;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FollowListResponse {

    private List<UserFollowInfo> users;
    private long totalCount;
    private int page;
    private int size;
}