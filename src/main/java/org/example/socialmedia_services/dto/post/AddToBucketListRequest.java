package org.example.socialmedia_services.dto.post;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToBucketListRequest {
    private String postId;

    public AddToBucketListRequest() {}

    public AddToBucketListRequest(String postId) {
        this.postId = postId;
    }
}
