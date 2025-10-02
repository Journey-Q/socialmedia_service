package org.example.socialmedia_services.dto.post;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateVisitedStatusRequest {
    private String postId;
    private boolean isVisited;

    public UpdateVisitedStatusRequest() {}

    public UpdateVisitedStatusRequest(String postId, boolean isVisited) {
        this.postId = postId;
        this.isVisited = isVisited;
    }
}
