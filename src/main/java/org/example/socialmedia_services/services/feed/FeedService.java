package org.example.socialmedia_services.services.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmedia_services.dto.feed.FeedPostDTO;
import org.example.socialmedia_services.dto.feed.FeedResponse;
import org.example.socialmedia_services.entity.UserProfile;
import org.example.socialmedia_services.entity.follow.Follow;
import org.example.socialmedia_services.entity.post.PlaceWiseContent;
import org.example.socialmedia_services.entity.post.Post;
import org.example.socialmedia_services.entity.post.PostContent;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.UserProfileRepository;
import org.example.socialmedia_services.repository.follow.FollowRepository;
import org.example.socialmedia_services.repository.post.LikeRepository;
import org.example.socialmedia_services.repository.post.PlaceWiseContentRepository;
import org.example.socialmedia_services.repository.post.PostContentRepository;
import org.example.socialmedia_services.repository.post.PostRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

    private final PostRepository postRepository;
    private final PostContentRepository postContentRepository;
    private final PlaceWiseContentRepository placeWiseContentRepository;
    private final FollowRepository followRepository;
    private final UserProfileRepository userProfileRepository;
    private final LikeRepository likeRepository;
    private final FeedRankingService feedRankingService;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Generate personalized feed for a user with ranking algorithm
     */
    @Transactional(readOnly = true)
    public FeedResponse getPersonalizedFeed(String userId, Integer page, Integer size) {
        log.info("Generating personalized feed for userId={}, page={}, size={}", userId, page, size);

        // Validate and set defaults
        int pageNumber = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? Math.min(size, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;

        // Get user profile (optional - if not found, use default preferences)
        log.info("Looking for user profile with userId={}", userId);
        Optional<UserProfile> userProfileOpt = userProfileRepository.findActiveByUserId(userId);

        if (userProfileOpt.isEmpty()) {
            log.warn("User profile not found or not active for userId={}, using default preferences", userId);
            // Create a temporary profile with empty preferences for ranking
            UserProfile userProfile = new UserProfile();
            userProfile.setUserId(userId);
            userProfile.setFavouriteActivities(new ArrayList<>());
            userProfile.setPreferredTripMoods(new ArrayList<>());

            // Continue with feed generation using default profile
            return generateFeed(userId, userProfile, pageNumber, pageSize);
        }

        UserProfile userProfile = userProfileOpt.get();
        log.info("Found user profile for userId={}, displayName={}", userId, userProfile.getDisplayName());

        return generateFeed(userId, userProfile, pageNumber, pageSize);
    }

    /**
     * Generate feed with ranking
     */
    private FeedResponse generateFeed(String userId, UserProfile userProfile, int pageNumber, int pageSize) {

        // Get list of users the current user is following (accepted follows only)
        Set<String> followingIds = followRepository
                .findByFollowingIdAndStatus(userId, "accepted", PageRequest.of(0, 10000))
                .getContent()
                .stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toSet());

        log.info("User {} is following {} users", userId, followingIds.size());

        // Fetch all posts with content (optimized query)
        List<Post> allPosts = postRepository.findAllPostsWithContent();
        log.info("Fetched {} total posts from database", allPosts.size());

        if (allPosts.isEmpty()) {
            log.warn("No posts found in database - returning empty feed");
            return FeedResponse.builder()
                    .posts(Collections.emptyList())
                    .currentPage(pageNumber)
                    .pageSize(pageSize)
                    .totalPosts(0)
                    .totalPages(0)
                    .hasMore(false)
                    .build();
        }

        // Calculate scores and rank posts
        List<PostWithScore> rankedPosts = allPosts.stream()
                .map(post -> {
                    double score = feedRankingService.calculatePostScore(post, userProfile, followingIds);
                    return new PostWithScore(post, score);
                })
                .sorted(Comparator.comparingDouble(PostWithScore::getScore).reversed())
                .collect(Collectors.toList());

        log.info("Ranked {} posts by score", rankedPosts.size());

        // Apply pagination
        int totalPosts = rankedPosts.size();
        int startIndex = pageNumber * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalPosts);

        if (startIndex >= totalPosts) {
            // Return empty feed if page is out of bounds
            return FeedResponse.builder()
                    .posts(Collections.emptyList())
                    .currentPage(pageNumber)
                    .pageSize(pageSize)
                    .totalPosts(totalPosts)
                    .totalPages((int) Math.ceil((double) totalPosts / pageSize))
                    .hasMore(false)
                    .build();
        }

        List<PostWithScore> pagedPosts = rankedPosts.subList(startIndex, endIndex);

        // Convert to DTOs with user details
        List<FeedPostDTO> feedPostDTOs = pagedPosts.stream()
                .map(pws -> convertToFeedPostDTO(pws.getPost(), userId, pws.getScore()))
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalPosts / pageSize);
        boolean hasMore = endIndex < totalPosts;

        log.info("Returning {} posts for page {}", feedPostDTOs.size(), pageNumber);

        return FeedResponse.builder()
                .posts(feedPostDTOs)
                .currentPage(pageNumber)
                .pageSize(pageSize)
                .totalPosts(totalPosts)
                .totalPages(totalPages)
                .hasMore(hasMore)
                .build();
    }

    /**
     * Convert Post entity to FeedPostDTO
     */
    private FeedPostDTO convertToFeedPostDTO(Post post, String currentUserId, Double rankScore) {
        // Get creator profile
        Optional<UserProfile> creatorProfileOpt = userProfileRepository
                .findActiveByUserId(String.valueOf(post.getCreatedById()));

        String creatorName = "Unknown User";
        String creatorProfileUrl = null;
        if (creatorProfileOpt.isPresent()) {
            UserProfile creatorProfile = creatorProfileOpt.get();
            creatorName = creatorProfile.getDisplayName();
            creatorProfileUrl = creatorProfile.getProfileImageUrl();
        }

        // Check if current user has liked this post
        boolean isLikedByUser = false;
        if (currentUserId != null) {
            try {
                Long currentUserIdLong = Long.valueOf(currentUserId);
                isLikedByUser = likeRepository.existsByPostIdAndUserId(post.getPostId(), currentUserIdLong);
            } catch (NumberFormatException e) {
                log.warn("Invalid user ID format: {}", currentUserId);
            }
        }

        FeedPostDTO.FeedPostDTOBuilder builder = FeedPostDTO.builder()
                .postId(post.getPostId())
                .createdById(post.getCreatedById())
                .creatorName(creatorName)
                .creatorProfileUrl(creatorProfileUrl)
                .createdAt(post.getCreatedAt())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .isLikedByUser(isLikedByUser);

        // Add rank score if provided
        if (rankScore != null) {
            builder.rankScore(rankScore);
        }

        // Add post content if available - fetch manually
        PostContent postContent = postContentRepository.findById(post.getPostId()).orElse(null);
        if (postContent != null) {
            builder.journeyTitle(postContent.getJourneyTitle())
                    .numberOfDays(postContent.getNumberOfDays())
                    .placesVisited(postContent.getPlacesVisited())
                    .budgetInfo(postContent.getBudgetInfo())
                    .travelTips(postContent.getTravelTips())
                    .transportationOptions(postContent.getTransportationOptions())
                    .hotelRecommendations(postContent.getHotelRecommendations())
                    .restaurantRecommendations(postContent.getRestaurantRecommendations());

            // Convert place-wise content - fetch separately
            List<PlaceWiseContent> placeWiseContentList = placeWiseContentRepository.findByPostId(post.getPostId());
            if (placeWiseContentList != null && !placeWiseContentList.isEmpty()) {
                List<FeedPostDTO.PlaceWiseContentDTO> placeWiseContentDTOs = placeWiseContentList.stream()
                        .map(this::convertToPlaceWiseContentDTO)
                        .collect(Collectors.toList());
                builder.placeWiseContent(placeWiseContentDTOs);
            }
        }

        return builder.build();
    }

    /**
     * Convert PlaceWiseContent entity to DTO
     */
    private FeedPostDTO.PlaceWiseContentDTO convertToPlaceWiseContentDTO(PlaceWiseContent pwc) {
        return FeedPostDTO.PlaceWiseContentDTO.builder()
                .placeWiseContentId(pwc.getPlaceWiseContentId())
                .placeName(pwc.getPlaceName())
                .latitude(pwc.getLatitude() != null ? pwc.getLatitude().doubleValue() : null)
                .longitude(pwc.getLongitude() != null ? pwc.getLongitude().doubleValue() : null)
                .address(pwc.getAddress())
                .tripMood(pwc.getTripMood())
                .activities(pwc.getActivities())
                .experiences(pwc.getExperiences())
                .imageUrls(pwc.getImageUrls())
                .sequenceOrder(pwc.getSequenceOrder())
                .build();
    }

    /**
     * Helper class to hold post with its calculated score
     */
    private static class PostWithScore {
        private final Post post;
        private final double score;

        public PostWithScore(Post post, double score) {
            this.post = post;
            this.score = score;
        }

        public Post getPost() {
            return post;
        }

        public double getScore() {
            return score;
        }
    }
}
