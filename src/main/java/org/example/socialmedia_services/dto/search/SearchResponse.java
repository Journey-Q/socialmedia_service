package org.example.socialmedia_services.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {

    private String title;
    private String subtitle;
    private String image;
    private String type; // "traveller" or "journey"
    private String id;

    // Constructor for traveller results
    public static SearchResponse createTravellerResult(String userId, String displayName, String profileImageUrl, String subtitle) {
        return SearchResponse.builder()
                .id(userId)
                .title(displayName)
                .subtitle(subtitle)
                .image(profileImageUrl)
                .type("traveller")
                .build();
    }

    // Constructor for journey results
    public static SearchResponse createJourneyResult(String postId, String journeyTitle, String imageUrl) {
        return SearchResponse.builder()
                .id(postId)
                .title(journeyTitle)
                .subtitle(null)
                .image(imageUrl)
                .type("journey")
                .build();
    }

    // Constructor for journey results with subtitle (places)
    public static SearchResponse createJourneyResultWithPlaces(String postId, String journeyTitle, String imageUrl, String placesSubtitle) {
        return SearchResponse.builder()
                .id(postId)
                .title(journeyTitle)
                .subtitle(placesSubtitle)
                .image(imageUrl)
                .type("journey")
                .build();
    }
}
