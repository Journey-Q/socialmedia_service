package org.example.socialmedia_services.dto.follow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllUsersProfilesResponse {

    private List<UserProfileWithStatsResponse> users;
    private Long totalCount;
    private Integer currentPage;
    private Integer totalPages;
}
