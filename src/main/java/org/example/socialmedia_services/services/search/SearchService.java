package org.example.socialmedia_services.services.search;

import org.example.socialmedia_services.dto.search.SearchResponse;
import org.example.socialmedia_services.entity.UserProfile;
import org.example.socialmedia_services.entity.post.PlaceWiseContent;
import org.example.socialmedia_services.entity.post.PostContent;
import org.example.socialmedia_services.repository.UserProfileRepository;
import org.example.socialmedia_services.repository.post.PlaceWiseContentRepository;
import org.example.socialmedia_services.repository.post.PostContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PostContentRepository postContentRepository;

    @Autowired
    private PlaceWiseContentRepository placeWiseContentRepository;

    public List<SearchResponse> search(String query, int limit) {
        List<SearchResponse> results = new ArrayList<>();

        // Search users by display name
        Pageable userPageable = PageRequest.of(0, limit / 2);
        List<UserProfile> users = userProfileRepository.findByDisplayNameContainingIgnoreCaseAndIsActiveTrue(query, userPageable);

        for (UserProfile user : users) {
            results.add(convertUserToSearchResponse(user));
        }

        // Search posts by journey title and places visited
        int remainingLimit = limit - results.size();
        if (remainingLimit > 0) {
            Pageable postPageable = PageRequest.of(0, remainingLimit);

            // Search by journey title
            List<PostContent> postsByTitle = postContentRepository.findByJourneyTitleContainingIgnoreCase(query, postPageable);

            // Search by places visited
            List<PostContent> postsByPlaces = postContentRepository.findByPlacesVisitedContaining(query, postPageable);

            // Combine and deduplicate posts
            Set<Long> addedPostIds = new HashSet<>();

            // Add title matches first
            for (PostContent post : postsByTitle) {
                if (results.size() >= limit) break;
                if (!addedPostIds.contains(post.getPostId())) {
                    results.add(convertPostToSearchResponse(post));
                    addedPostIds.add(post.getPostId());
                }
            }

            // Add place matches
            for (PostContent post : postsByPlaces) {
                if (results.size() >= limit) break;
                if (!addedPostIds.contains(post.getPostId())) {
                    String matchingPlaces = getMatchingPlaces(post.getPlacesVisited(), query);
                    results.add(convertPostToSearchResponseWithPlaces(post, matchingPlaces));
                    addedPostIds.add(post.getPostId());
                }
            }
        }

        return results;
    }

    private SearchResponse convertUserToSearchResponse(UserProfile user) {
        String subtitle = generateUserSubtitle(user);
        return SearchResponse.createTravellerResult(
                user.getUserId(),
                user.getDisplayName(),
                user.getProfileImageUrl(),
                subtitle
        );
    }

    private SearchResponse convertPostToSearchResponse(PostContent post) {
        String imageUrl = getPostImageUrl(post);
        return SearchResponse.createJourneyResult(
                post.getPostId().toString(),
                post.getJourneyTitle(),
                imageUrl
        );
    }

    private SearchResponse convertPostToSearchResponseWithPlaces(PostContent post, String placesSubtitle) {
        String imageUrl = getPostImageUrl(post);
        return SearchResponse.createJourneyResultWithPlaces(
                post.getPostId().toString(),
                post.getJourneyTitle(),
                imageUrl,
                placesSubtitle
        );
    }

    private String generateUserSubtitle(UserProfile user) {
        if (user.getFavouriteActivities() != null && !user.getFavouriteActivities().isEmpty()) {
            String activity = user.getFavouriteActivities().get(0);
            return activity + " Enthusiast";
        } else if (user.getPreferredTripMoods() != null && !user.getPreferredTripMoods().isEmpty()) {
            String mood = user.getPreferredTripMoods().get(0);
            return mood + " Traveller";
        }
        return "Travel Explorer";
    }

    private String getPostImageUrl(PostContent post) {
        // Try to get image from place-wise content first - fetch separately
        List<PlaceWiseContent> placeWiseContentList = placeWiseContentRepository.findByPostIdOrderBySequenceOrderAsc(post.getPostId());
        if (placeWiseContentList != null && !placeWiseContentList.isEmpty()) {
            // Get the first place (ordered by sequenceOrder ASC)
            var firstPlace = placeWiseContentList.get(0);

            // Get the first image URL from the first place
            if (firstPlace.getImageUrls() != null && !firstPlace.getImageUrls().isEmpty()) {
                return firstPlace.getImageUrls().get(0);
            }
        }

        // Default travel image if no images found
        return "https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=400&h=300&fit=crop";
    }

    private String getMatchingPlaces(List<String> placesVisited, String query) {
        if (placesVisited == null || placesVisited.isEmpty()) {
            return null;
        }

        List<String> matchingPlaces = placesVisited.stream()
                .filter(place -> place.toLowerCase().contains(query.toLowerCase()))
                .limit(3)
                .collect(Collectors.toList());

        if (matchingPlaces.isEmpty()) {
            return null;
        }

        return String.join(", ", matchingPlaces);
    }
}