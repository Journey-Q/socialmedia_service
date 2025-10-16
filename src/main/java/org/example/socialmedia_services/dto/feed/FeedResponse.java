package org.example.socialmedia_services.dto.feed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedResponse {
    private List<FeedPostDTO> posts;
    private Integer currentPage;
    private Integer pageSize;
    private Integer totalPosts;
    private Integer totalPages;
    private Boolean hasMore;
}
