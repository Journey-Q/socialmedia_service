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
import org.example.socialmedia_services.services.kafka.KafkaProducerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public Follow createFollow(String followingId, String followerId) {
        log.info("Creating follow request: followingId={} wants to follow followerId={}", followingId, followerId);

        if (followingId.equals(followerId)) {
            throw new BadRequestException("Cannot follow yourself");
        }

        if (!userProfileRepository.findActiveByUserId(followingId).isPresent()) {
            throw new BadRequestException("Your profile not found");
        }
        if (!userProfileRepository.findActiveByUserId(followerId).isPresent()) {
            throw new BadRequestException("User to follow not found");
        }

        Optional<Follow> existingFollow = followRepository.findFollowRelationship(followingId, followerId);
        if (existingFollow.isPresent()) {
            throw new BadRequestException("Follow request already sent or you already follow this user");
        }

        Follow newFollow = Follow.builder()
                .followingId(followingId)
                .followerId(followerId)
                .status("pending")
                .build();

        Follow savedFollow = followRepository.save(newFollow);

        createStatsIfNotExists(followingId);
        createStatsIfNotExists(followerId);

        // Send Kafka event for follow request
        sendFollowEventToKafka(savedFollow.getId(), followingId, followerId);

        log.info("Follow request created: id={}, status=pending", savedFollow.getId());
        return savedFollow;
    }

    @Transactional
    public boolean acceptFollowRequest(Long followId, String currentUserId) {
        log.info("Accepting follow request: followId={} by userId={}", followId, currentUserId);

        Optional<Follow> followOpt = followRepository.findById(followId);

        if (!followOpt.isPresent()) {
            throw new BadRequestException("Follow request not found");
        }

        Follow follow = followOpt.get();

        // Verify that the current user is the one being followed (follower)
        if (!follow.getFollowerId().equals(currentUserId)) {
            throw new BadRequestException("You are not authorized to accept this follow request");
        }

        if (!"pending".equals(follow.getStatus())) {
            throw new BadRequestException("Follow request is not pending");
        }

        // Ensure stats exist for both users before updating
        createStatsIfNotExists(currentUserId);
        createStatsIfNotExists(follow.getFollowingId());

        // Update status to accepted
        follow.setStatus("accepted");
        followRepository.save(follow);

        // Update stats:
        // currentUserId (follower) gains a follower
        // followingId gains someone they're following
        int followersUpdated = userStatsRepository.incrementFollowers(currentUserId);
        if (followersUpdated == 0) {
            log.error("Failed to increment followers count for userId={}", currentUserId);
            throw new BadRequestException("Failed to update follower count");
        }

        int followingUpdated = userStatsRepository.incrementFollowing(follow.getFollowingId());
        if (followingUpdated == 0) {
            log.error("Failed to increment following count for userId={}", follow.getFollowingId());
            throw new BadRequestException("Failed to update following count");
        }

        log.info("Follow request accepted: followId={}, followingId={} now follows followerId={}",
                followId, follow.getFollowingId(), currentUserId);
        return true;
    }

    @Transactional
    public boolean rejectFollowRequest(Long followId, String currentUserId) {
        log.info("Rejecting follow request: followId={} by userId={}", followId, currentUserId);

        Optional<Follow> followOpt = followRepository.findById(followId);

        if (!followOpt.isPresent()) {
            throw new BadRequestException("Follow request not found");
        }

        Follow follow = followOpt.get();

        // Verify that the current user is the one being followed (follower)
        if (!follow.getFollowerId().equals(currentUserId)) {
            throw new BadRequestException("You are not authorized to reject this follow request");
        }

        if (!"pending".equals(follow.getStatus())) {
            throw new BadRequestException("Follow request is not pending");
        }

        // Delete the follow request
        followRepository.delete(follow);

        log.info("Follow request rejected and deleted: followId={}", followId);
        return true;
    }

    @Transactional
    public boolean unfollowUser(String followingId, String followerId) {
        log.info("Unfollowing: followingId={} wants to unfollow followerId={}", followingId, followerId);

        Optional<Follow> followOpt = followRepository.findFollowRelationship(followingId, followerId);

        if (!followOpt.isPresent()) {
            throw new BadRequestException("You are not following this user");
        }

        Follow follow = followOpt.get();

        if (!"accepted".equals(follow.getStatus())) {
            throw new BadRequestException("Follow relationship is not active");
        }

        // Delete the follow relationship first
        followRepository.delete(follow);

        // Update stats - decrement counts
        int followersUpdated = userStatsRepository.decrementFollowers(followerId);
        if (followersUpdated == 0) {
            log.warn("Failed to decrement followers count for userId={} - stats may not exist", followerId);
        }

        int followingUpdated = userStatsRepository.decrementFollowing(followingId);
        if (followingUpdated == 0) {
            log.warn("Failed to decrement following count for userId={} - stats may not exist", followingId);
        }

        log.info("Unfollowed successfully: followingId={} unfollowed followerId={}", followingId, followerId);
        return true;
    }

    public FollowersListResponse getFollowersWithProfiles(String userId, int page, int size) {
        log.info("Getting followers for userId={} with profiles", userId);

        Pageable pageable = PageRequest.of(page, size);

        Page<Follow> followsPage = followRepository.findByFollowerIdAndStatus(userId, "accepted", pageable);

        List<FollowerUserInfo> followers = followsPage.getContent().stream()
                .map(follow -> {
                    String followerId = follow.getFollowingId();
                    Optional<UserProfile> profileOpt = userProfileRepository.findActiveByUserId(followerId);

                    if (profileOpt.isPresent()) {
                        UserProfile profile = profileOpt.get();
                        return FollowerUserInfo.builder()
                                .userId(followerId)
                                .displayName(profile.getDisplayName())
                                .profileImageUrl(profile.getProfileImageUrl())
                                .build();
                    }
                    return null;
                })
                .filter(info -> info != null)
                .collect(Collectors.toList());

        return FollowersListResponse.builder()
                .followers(followers)
                .totalCount(followsPage.getTotalElements())
                .currentPage(page)
                .totalPages(followsPage.getTotalPages())
                .build();
    }

    public FollowingListResponse getFollowingWithProfiles(String userId, int page, int size) {
        log.info("Getting following for userId={} with profiles", userId);

        Pageable pageable = PageRequest.of(page, size);

        Page<Follow> followsPage = followRepository.findByFollowingIdAndStatus(userId, "accepted", pageable);

        List<FollowingUserInfo> following = followsPage.getContent().stream()
                .map(follow -> {
                    String followedUserId = follow.getFollowerId();
                    Optional<UserProfile> profileOpt = userProfileRepository.findActiveByUserId(followedUserId);

                    if (profileOpt.isPresent()) {
                        UserProfile profile = profileOpt.get();
                        return FollowingUserInfo.builder()
                                .userId(followedUserId)
                                .displayName(profile.getDisplayName())
                                .profileImageUrl(profile.getProfileImageUrl())
                                .build();
                    }
                    return null;
                })
                .filter(info -> info != null)
                .collect(Collectors.toList());

        return FollowingListResponse.builder()
                .following(following)
                .totalCount(followsPage.getTotalElements())
                .currentPage(page)
                .totalPages(followsPage.getTotalPages())
                .build();
    }

    public FollowRequestsListResponse getPendingFollowRequests(String userId, int page, int size) {
        log.info("Getting pending follow requests for userId={}", userId);

        Pageable pageable = PageRequest.of(page, size);

        Page<Follow> requestsPage = followRepository.findByFollowerIdAndStatus(userId, "pending", pageable);

        List<FollowRequestUserInfo> requests = requestsPage.getContent().stream()
                .map(follow -> {
                    String requesterId = follow.getFollowingId();
                    Optional<UserProfile> profileOpt = userProfileRepository.findActiveByUserId(requesterId);

                    if (profileOpt.isPresent()) {
                        UserProfile profile = profileOpt.get();
                        return FollowRequestUserInfo.builder()
                                .followId(follow.getId())
                                .userId(requesterId)
                                .displayName(profile.getDisplayName())
                                .profileImageUrl(profile.getProfileImageUrl())
                                .requestedAt(follow.getCreatedAt())
                                .build();
                    }
                    return null;
                })
                .filter(info -> info != null)
                .collect(Collectors.toList());

        return FollowRequestsListResponse.builder()
                .requests(requests)
                .totalCount(requestsPage.getTotalElements())
                .currentPage(page)
                .totalPages(requestsPage.getTotalPages())
                .build();
    }

    public UserStatsResponse getStats(String userId) {
        log.info("Getting stats for userId={}", userId);

        Optional<UserStats> statsOpt = userStatsRepository.findById(userId);

        if (statsOpt.isPresent()) {
            UserStats stats = statsOpt.get();
            return UserStatsResponse.builder()
                    .userId(userId)
                    .followersCount(stats.getFollowersCount())
                    .followingCount(stats.getFollowingCount())
                    .postsCount(stats.getPostsCount())
                    .build();
        }

        return UserStatsResponse.builder()
                .userId(userId)
                .followersCount(0)
                .followingCount(0)
                .postsCount(0)
                .build();
    }

    public boolean isFollowing(String currentUserId, String targetUserId) {
        try {
            log.info("Checking if userId={} is following userId={}", currentUserId, targetUserId);

            // Handle edge cases
            if (currentUserId == null || targetUserId == null) {
                log.warn("Null userId provided: currentUserId={}, targetUserId={}", currentUserId, targetUserId);
                return false;
            }

            if (currentUserId.equals(targetUserId)) {
                log.info("User checking if they follow themselves, returning false");
                return false;
            }

            // Check if there's an accepted follow relationship
            // where currentUserId (followingId) is following targetUserId (followerId)
            Optional<Follow> followOpt = followRepository.findFollowRelationship(currentUserId, targetUserId);

            if (followOpt.isPresent()) {
                Follow follow = followOpt.get();
                boolean isFollowing = "accepted".equals(follow.getStatus());
                log.info("Follow relationship found: status={}, isFollowing={}", follow.getStatus(), isFollowing);
                return isFollowing;
            }

            log.info("No follow relationship found");
            return false;
        } catch (Exception e) {
            log.error("Error checking if userId={} is following userId={}: {}", currentUserId, targetUserId, e.getMessage(), e);
            return false;
        }
    }

    public boolean isPending(String currentUserId, String targetUserId) {
        try {
            log.info("Checking if there's a pending follow request from userId={} to userId={}", currentUserId, targetUserId);

            // Handle edge cases
            if (currentUserId == null || targetUserId == null) {
                log.warn("Null userId provided: currentUserId={}, targetUserId={}", currentUserId, targetUserId);
                return false;
            }

            if (currentUserId.equals(targetUserId)) {
                log.info("User checking pending status with themselves, returning false");
                return false;
            }

            // Check if there's a pending follow relationship
            // where currentUserId (followingId) sent a request to targetUserId (followerId)
            Optional<Follow> followOpt = followRepository.findFollowRelationship(currentUserId, targetUserId);

            if (followOpt.isPresent()) {
                Follow follow = followOpt.get();
                boolean isPending = "pending".equals(follow.getStatus());
                log.info("Follow relationship found: status={}, isPending={}", follow.getStatus(), isPending);
                return isPending;
            }

            log.info("No follow relationship found");
            return false;
        } catch (Exception e) {
            log.error("Error checking if there's a pending request from userId={} to userId={}: {}", currentUserId, targetUserId, e.getMessage(), e);
            return false;
        }
    }

    private void createStatsIfNotExists(String userId) {
        if (!userStatsRepository.findById(userId).isPresent()) {
            UserStats stats = UserStats.builder()
                    .userId(userId)
                    .followersCount(0)
                    .followingCount(0)
                    .postsCount(0)
                    .build();
            userStatsRepository.save(stats);
            log.info("Created stats for userId={}", userId);
        }
    }

    private void sendFollowEventToKafka(Long followId, String senderId, String receiverId) {
        try {
            // Get sender's profile
            Optional<UserProfile> senderProfileOpt = userProfileRepository.findActiveByUserId(senderId);
            if (senderProfileOpt.isEmpty()) {
                return; // Skip sending event if sender profile not found
            }
            UserProfile senderProfile = senderProfileOpt.get();

            // Send Kafka event
            kafkaProducerService.sendFollowEvent(
                    followId,
                    senderId,
                    receiverId,
                    senderProfile.getDisplayName(),
                    senderProfile.getProfileImageUrl()
            );
        } catch (Exception e) {
            // Log but don't fail the follow operation if Kafka event fails
            // The exception is already logged in KafkaProducerService
        }
    }
}