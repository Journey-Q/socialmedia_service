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
public class FollowRequestsListResponse {

    private List<FollowRequestUserInfo> requests;
    private long totalCount;
    private int currentPage;
    private int totalPages;
}