package org.example.socialmedia_services.dto.post;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GetPostUserResponse {
    // Getters and Setters
    private String id;
    private String location;
    private String journeyTitle;
    private List<String> postImages;

    // Default constructor
    public GetPostUserResponse() {}

    // Constructor with parameters
    public GetPostUserResponse(String id, String location, String journeyTitle, List<String> postImages) {
        this.id = id;
        this.location = location;
        this.journeyTitle = journeyTitle;
        this.postImages = postImages;
    }

    @Override
    public String toString() {
        return "GetPostUserResponse{" +
                "id='" + id + '\'' +
                ", location='" + location + '\'' +
                ", journeyTitle='" + journeyTitle + '\'' +
                ", postImages=" + postImages +
                '}';
    }
}