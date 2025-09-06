package org.example.socialmedia_services.services.follow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmedia_services.dto.follow.*;
import org.example.socialmedia_services.entity.follow.Follow;
import org.example.socialmedia_services.entity.UserProfile;
import org.example.socialmedia_services.entity.follow.UserStats;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.follow.FollowRepository;
import org.example.socialmedia_services.repository.UserProfileRepository;
import org.example.socialmedia_services.repository.follow.UserStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowRepository followRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserStatsRepository userStatsRepository;

    @Transactional
    public Follow createFollow(String followingId, String followerId, String status) {
        log.info("Creating follow request: {} -> {} with status: {}", followingId, followerId, status);

        // Validate input
        if (followingId.equals(followerId)) {
            throw new BadRequestException("Cannot follow yourself");
        }

        // Check if both users exist
        if (!userProfileRepository.findActiveByUserId(followingId).isPresent()) {
            throw new BadRequestException("Following user not found");
        }
        if (!userProfileRepository.findActiveByUserId(followerId).isPresent()) {
            throw new BadRequestException("Follower user not found");
        }

        // Check if follow relationship already exists
        Optional<Follow> existingFollow = followRepository.findFollowRelationship(followingId, followerId);
        if (existingFollow.isPresent()) {
            throw new BadRequestException("Follow relationship already exists");
        }

        // Create new follow relationship
        Follow newFollow = Follow.builder()
                .followingId(followingId)
                .followerId(followerId)
                .status(status != null ? status : "pending")
                .build();

        Follow savedFollow = followRepository.save(newFollow);

        // Create user stats if they don't exist
        createStatsIfNotExists(followingId);
        createStatsIfNotExists(followerId);

        // IMPORTANT: Only update stats if status is accepted (direct acceptance)
        if ("accepted".equals(status)) {
            // followerId gets a new follower (the followingId user is following them)
            int followersUpdated = userStatsRepository.incrementFollowers(followerId);
            // followingId is now following someone (they're following followerId)
            int followingUpdated = userStatsRepository.incrementFollowing(followingId);

            log.info("Direct acceptance - Updated stats: followerId {} gained {} followers, followingId {} gained {} following",
                    followerId, followersUpdated, followingId, followingUpdated);
        }

        log.info("Created follow relationship: {} -> {} with status: {}", followingId, followerId, status);
        return savedFollow;
    }

    @Transactional(readOnly = true)
    public List<Follow> getFollowers(String followerId) {
        return followRepository.getFollowers(followerId);
    }

    @Transactional(readOnly = true)
    public List<Follow> getFollowing(String followingId) {
        return followRepository.getFollowing(followingId);
    }

    @Transactional
    public boolean updateStatus(String followingId, String followerId, String status) {
        log.info("Updating follow status: {} -> {} to {}", followingId, followerId, status);

        // Check if follow relationship exists
        Optional<Follow> followOptional = followRepository.findFollowRelationship(followingId, followerId);
        if (!followOptional.isPresent()) {
            throw new BadRequestException("Follow relationship not found");
        }

        Follow follow = followOptional.get();
        String oldStatus = follow.getStatus();

        // Update status
        int updated = followRepository.updateStatus(followingId, followerId, status);

        if (updated > 0) {
            // Create stats if they don't exist
            createStatsIfNotExists(followingId);
            createStatsIfNotExists(followerId);

            // Update stats based on status change
            updateStatsOnStatusChange(followingId, followerId, oldStatus, status);
            log.info("Updated follow status: {} -> {} from {} to {}", followingId, followerId, oldStatus, status);
            return true;
        }

        return false;
    }

    @Transactional
    public boolean deleteFollow(String followingId, String followerId) {
        log.info("Deleting follow relationship: {} -> {}", followingId, followerId);

        // Check if follow relationship exists and get its status
        Optional<Follow> followOptional = followRepository.findFollowRelationship(followingId, followerId);
        if (!followOptional.isPresent()) {
            throw new BadRequestException("Follow relationship not found");
        }

        Follow follow = followOptional.get();
        String status = follow.getStatus();

        int deleted = followRepository.deleteFollow(followingId, followerId);

        if (deleted > 0) {
            // If the follow was accepted, decrement stats
            if ("accepted".equals(status)) {
                createStatsIfNotExists(followingId);
                createStatsIfNotExists(followerId);

                int followersUpdated = userStatsRepository.decrementFollowers(followerId);
                int followingUpdated = userStatsRepository.decrementFollowing(followingId);

                log.info("Follow deleted - Updated stats: followerId {} lost {} followers, followingId {} lost {} following",
                        followerId, followersUpdated, followingId, followingUpdated);
            }
            log.info("Deleted follow relationship: {} -> {}", followingId, followerId);
            return true;
        }

        return false;
    }

    // UserStats methods
    @Transactional
    public boolean createStats(String userId) {
        if (userStatsRepository.existsByUserId(userId)) {
            log.info("Stats already exist for user: {}", userId);
            return false; // Stats already exist
        }

        UserStats userStats = new UserStats(userId);
        userStatsRepository.save(userStats);
        log.info("Created stats for user: {}", userId);
        return true;
    }

    @Transactional
    public boolean incrementFollowers(String userId) {
        createStatsIfNotExists(userId);
        int updated = userStatsRepository.incrementFollowers(userId);
        log.info("Incremented followers for user {}: updated {} records", userId, updated);
        return updated > 0;
    }

    @Transactional
    public boolean decrementFollowers(String userId) {
        createStatsIfNotExists(userId);
        int updated = userStatsRepository.decrementFollowers(userId);
        log.info("Decremented followers for user {}: updated {} records", userId, updated);
        return updated > 0;
    }

    @Transactional
    public boolean incrementFollowing(String userId) {
        createStatsIfNotExists(userId);
        int updated = userStatsRepository.incrementFollowing(userId);
        log.info("Incremented following for user {}: updated {} records", userId, updated);
        return updated > 0;
    }

    @Transactional
    public boolean decrementFollowing(String userId) {
        createStatsIfNotExists(userId);
        int updated = userStatsRepository.decrementFollowing(userId);
        log.info("Decremented following for user {}: updated {} records", userId, updated);
        return updated > 0;
    }

    @Transactional(readOnly = true)
    public UserStatsResponse getStats(String userId) {
        createStatsIfNotExists(userId);

        Optional<UserStats> statsOptional = userStatsRepository.findByUserId(userId);

        if (statsOptional.isPresent()) {
            UserStats stats = statsOptional.get();
            log.info("Retrieved stats for user {}: followers={}, following={}",
                    userId, stats.getFollowersCount(), stats.getFollowingCount());
            return UserStatsResponse.builder()
                    .userId(stats.getUserId())
                    .followersCount(stats.getFollowersCount())
                    .followingCount(stats.getFollowingCount())
                    .build();
        } else {
            // This shouldn't happen after createStatsIfNotExists, but just in case
            log.warn("No stats found for user {} even after creation attempt", userId);
            return UserStatsResponse.builder()
                    .userId(userId)
                    .followersCount(0)
                    .followingCount(0)
                    .build();
        }
    }

    // Additional helper methods
    @Transactional(readOnly = true)
    public FollowListResponse getFollowersWithProfiles(String followerId, int page, int size) {
        List<Follow> follows = followRepository.getFollowers(followerId);
        long totalCount = follows.size();

        // Apply pagination
        int start = page * size;
        int end = Math.min(start + size, follows.size());
        List<Follow> paginatedFollows = follows.subList(start, end);

        List<UserFollowInfo> followers = paginatedFollows.stream()
                .map(follow -> {
                    Optional<UserProfile> profileOpt = userProfileRepository.findActiveByUserId(follow.getFollowingId());
                    if (profileOpt.isPresent()) {
                        UserProfile profile = profileOpt.get();
                        boolean isMutual = followRepository.findFollowRelationshipByStatus(follow.getFollowingId(), followerId, "accepted").isPresent();

                        return UserFollowInfo.builder()
                                .userId(profile.getUserId())
                                .displayName(profile.getDisplayName())
                                .profileImageUrl(profile.getProfileImageUrl())
                                .status(follow.getStatus())
                                .isMutualFollow(isMutual)
                                .createdAt(follow.getCreatedAt())
                                .build();
                    }
                    return null;
                })
                .filter(userInfo -> userInfo != null)
                .collect(Collectors.toList());

        return FollowListResponse.builder()
                .users(followers)
                .totalCount(totalCount)
                .page(page)
                .size(size)
                .build();
    }

    @Transactional(readOnly = true)
    public FollowListResponse getFollowingWithProfiles(String followingId, int page, int size) {
        List<Follow> follows = followRepository.getFollowing(followingId);
        long totalCount = follows.size();

        // Apply pagination
        int start = page * size;
        int end = Math.min(start + size, follows.size());
        List<Follow> paginatedFollows = follows.subList(start, end);

        List<UserFollowInfo> following = paginatedFollows.stream()
                .map(follow -> {
                    Optional<UserProfile> profileOpt = userProfileRepository.findActiveByUserId(follow.getFollowerId());
                    if (profileOpt.isPresent()) {
                        UserProfile profile = profileOpt.get();
                        boolean isMutual = followRepository.findFollowRelationshipByStatus(followingId, follow.getFollowerId(), "accepted").isPresent();

                        return UserFollowInfo.builder()
                                .userId(profile.getUserId())
                                .displayName(profile.getDisplayName())
                                .profileImageUrl(profile.getProfileImageUrl())
                                .status(follow.getStatus())
                                .isMutualFollow(isMutual)
                                .createdAt(follow.getCreatedAt())
                                .build();
                    }
                    return null;
                })
                .filter(userInfo -> userInfo != null)
                .collect(Collectors.toList());

        return FollowListResponse.builder()
                .users(following)
                .totalCount(totalCount)
                .page(page)
                .size(size)
                .build();
    }

    @Transactional(readOnly = true)
    public List<Follow> getPendingFollowRequests(String followerId) {
        return followRepository.getPendingFollowRequests(followerId);
    }

    @Transactional(readOnly = true)
    public Optional<Follow> getFollowRelationship(String followingId, String followerId) {
        return followRepository.findFollowRelationship(followingId, followerId);
    }

    // Private helper methods
    private void createStatsIfNotExists(String userId) {
        if (!userStatsRepository.existsByUserId(userId)) {
            createStats(userId);
        }
    }

    private void updateStatsOnStatusChange(String followingId, String followerId, String oldStatus, String newStatus) {
        log.info("Updating stats: followingId={}, followerId={}, oldStatus={}, newStatus={}", followingId, followerId, oldStatus, newStatus);

        // Create stats if they don't exist
        createStatsIfNotExists(followingId);
        createStatsIfNotExists(followerId);

        // If changing from non-accepted to accepted
        if (!"accepted".equals(oldStatus) && "accepted".equals(newStatus)) {
            // followerId gains a follower (someone is now following them)
            int followersUpdated = userStatsRepository.incrementFollowers(followerId);
            // followingId gains a following (they are now following someone)
            int followingUpdated = userStatsRepository.incrementFollowing(followingId);

            log.info("Stats incremented - followerId {} gained {} followers, followingId {} gained {} following",
                    followerId, followersUpdated, followingId, followingUpdated);
        }
        // If changing from accepted to non-accepted (unfollowing)
        else if ("accepted".equals(oldStatus) && !"accepted".equals(newStatus)) {
            // followerId loses a follower
            int followersUpdated = userStatsRepository.decrementFollowers(followerId);
            // followingId loses a following
            int followingUpdated = userStatsRepository.decrementFollowing(followingId);

            log.info("Stats decremented - followerId {} lost {} followers, followingId {} lost {} following",
                    followerId, followersUpdated, followingId, followingUpdated);
        }

        // Log current stats after update
        try {
            UserStats followerStats = userStatsRepository.findByUserId(followerId).orElse(null);
            UserStats followingStats = userStatsRepository.findByUserId(followingId).orElse(null);

            if (followerStats != null) {
                log.info("Updated stats for followerId {}: followers={}, following={}",
                        followerId, followerStats.getFollowersCount(), followerStats.getFollowingCount());
            }
            if (followingStats != null) {
                log.info("Updated stats for followingId {}: followers={}, following={}",
                        followingId, followingStats.getFollowersCount(), followingStats.getFollowingCount());
            }
        } catch (Exception e) {
            log.warn("Error logging updated stats", e);
        }
    }
}