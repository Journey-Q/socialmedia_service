package org.example.socialmedia_services.services.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmedia_services.entity.UserProfile;
import org.example.socialmedia_services.entity.post.PlaceWiseContent;
import org.example.socialmedia_services.entity.post.Post;
import org.example.socialmedia_services.entity.post.PostContent;
import org.example.socialmedia_services.repository.post.PlaceWiseContentRepository;
import org.example.socialmedia_services.repository.post.PostContentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedRankingService {

    private final PostContentRepository postContentRepository;
    private final PlaceWiseContentRepository placeWiseContentRepository;

    // Ranking weights - tuned for optimal feed experience
    private static final double FOLLOWER_WEIGHT = 3.0;
    private static final double RECENCY_WEIGHT = 2.5;
    private static final double ENGAGEMENT_WEIGHT = 2.0;
    private static final double ACTIVITY_MATCH_WEIGHT = 1.5;
    private static final double MOOD_MATCH_WEIGHT = 1.5;

    // Engagement scoring
    private static final double LIKE_SCORE = 1.0;
    private static final double COMMENT_SCORE = 2.0;

    // Time decay parameters
    private static final double RECENCY_DECAY_DAYS = 7.0;

    /**
     * Calculate ranking score for a post based on multiple factors
     */
    public double calculatePostScore(Post post, UserProfile userProfile, Set<String> followingIds) {
        double score = 0.0;

        // 1. Follower Boost - higher if post is from someone the user follows
        score += calculateFollowerBoost(post, followingIds);

        // 2. Recency Score - more recent posts score higher
        score += calculateRecencyScore(post);

        // 3. Engagement Score - likes and comments
        score += calculateEngagementScore(post);

        // 4. Activity Matching - match user's favorite activities with post activities
        score += calculateActivityMatchScore(post, userProfile);

        // 5. Trip Mood Matching - match user's preferred moods with post moods
        score += calculateMoodMatchScore(post, userProfile);

        log.debug("Post {} score: {}", post.getPostId(), score);
        return score;
    }

    /**
     * Boost score if post is from a followed user
     */
    private double calculateFollowerBoost(Post post, Set<String> followingIds) {
        String postCreatorId = String.valueOf(post.getCreatedById());
        if (followingIds.contains(postCreatorId)) {
            return FOLLOWER_WEIGHT * 10.0; // Significant boost for followed users
        }
        return 0.0;
    }

    /**
     * Calculate recency score with time decay
     * Recent posts get higher scores, with exponential decay over time
     */
    private double calculateRecencyScore(Post post) {
        LocalDateTime now = LocalDateTime.now();
        long hoursSincePost = ChronoUnit.HOURS.between(post.getCreatedAt(), now);
        double daysSincePost = hoursSincePost / 24.0;

        // Exponential decay: score decreases as post gets older
        double recencyScore = Math.exp(-daysSincePost / RECENCY_DECAY_DAYS) * 10.0;

        return RECENCY_WEIGHT * recencyScore;
    }

    /**
     * Calculate engagement score based on likes and comments
     */
    private double calculateEngagementScore(Post post) {
        int likes = post.getLikesCount() != null ? post.getLikesCount() : 0;
        int comments = post.getCommentsCount() != null ? post.getCommentsCount() : 0;

        double engagementScore = (likes * LIKE_SCORE) + (comments * COMMENT_SCORE);

        // Apply logarithmic scaling to prevent viral posts from dominating
        if (engagementScore > 0) {
            engagementScore = Math.log1p(engagementScore) * 3.0;
        }

        return ENGAGEMENT_WEIGHT * engagementScore;
    }

    /**
     * Calculate activity matching score
     * Compares user's favorite activities with post's activities
     */
    private double calculateActivityMatchScore(Post post, UserProfile userProfile) {
        if (userProfile.getFavouriteActivities() == null || userProfile.getFavouriteActivities().isEmpty()) {
            return 0.0;
        }

        // Fetch PostContent manually
        PostContent postContent = postContentRepository.findById(post.getPostId()).orElse(null);
        if (postContent == null) {
            return 0.0;
        }

        // Get user's favorite activities (case-insensitive)
        Set<String> userActivities = userProfile.getFavouriteActivities().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // Get all activities from post's place-wise content - fetch separately
        List<PlaceWiseContent> placeWiseContentList = placeWiseContentRepository.findByPostId(post.getPostId());
        if (placeWiseContentList == null || placeWiseContentList.isEmpty()) {
            return 0.0;
        }

        Set<String> postActivities = placeWiseContentList.stream()
                .filter(pwc -> pwc.getActivities() != null)
                .flatMap(pwc -> pwc.getActivities().stream())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // Calculate match percentage
        long matchCount = userActivities.stream()
                .filter(postActivities::contains)
                .count();

        if (matchCount == 0) {
            return 0.0;
        }

        double matchScore = (double) matchCount / userActivities.size() * 10.0;
        return ACTIVITY_MATCH_WEIGHT * matchScore;
    }

    /**
     * Calculate trip mood matching score
     * Compares user's preferred trip moods with post's moods
     */
    private double calculateMoodMatchScore(Post post, UserProfile userProfile) {
        if (userProfile.getPreferredTripMoods() == null || userProfile.getPreferredTripMoods().isEmpty()) {
            return 0.0;
        }

        // Fetch PostContent manually
        PostContent postContent = postContentRepository.findById(post.getPostId()).orElse(null);
        if (postContent == null) {
            return 0.0;
        }

        // Get user's preferred moods (case-insensitive)
        Set<String> userMoods = userProfile.getPreferredTripMoods().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // Get all moods from post's place-wise content - fetch separately
        List<PlaceWiseContent> placeWiseContentList = placeWiseContentRepository.findByPostId(post.getPostId());
        if (placeWiseContentList == null || placeWiseContentList.isEmpty()) {
            return 0.0;
        }

        Set<String> postMoods = placeWiseContentList.stream()
                .map(PlaceWiseContent::getTripMood)
                .filter(mood -> mood != null && !mood.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // Calculate match percentage
        long matchCount = userMoods.stream()
                .filter(postMoods::contains)
                .count();

        if (matchCount == 0) {
            return 0.0;
        }

        double matchScore = (double) matchCount / userMoods.size() * 10.0;
        return MOOD_MATCH_WEIGHT * matchScore;
    }
}
