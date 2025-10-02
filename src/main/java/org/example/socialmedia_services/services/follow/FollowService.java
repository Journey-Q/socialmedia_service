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

        log.info("Follow request created: id={}, status=pending", savedFollow.getId());
        return savedFollow;
    }

    @Transactional
    public boolean acceptFollowRequest(String followingId, String currentUserId) {
        log.info("Accepting follow request: followingId={} by followerId={}", followingId, currentUserId);

        Optional<Follow> followOpt = followRepository.findFollowRelationship(followingId, currentUserId);

        if (!followOpt.isPresent()) {
            throw new BadRequestException("Follow request not found");
        }

        Follow follow = followOpt.get();

        if (!"pending".equals(follow.getStatus())) {
            throw new BadRequestException("Follow request is not pending");
        }

        follow.setStatus("accepted");
        followRepository.save(follow);

        userStatsRepository.incrementFollowers(currentUserId);
        userStatsRepository.incrementFollowing(followingId);

        log.info("Follow request accepted: followingId={} now follows followerId={}", followingId, currentUserId);
        return true;
    }

    @Transactional
    public boolean rejectFollowRequest(String followingId, String currentUserId) {
        log.info("Rejecting follow request: followingId={} by followerId={}", followingId, currentUserId);

        Optional<Follow> followOpt = followRepository.findFollowRelationship(followingId, currentUserId);

        if (!followOpt.isPresent()) {
            throw new BadRequestException("Follow request not found");
        }

        Follow follow = followOpt.get();

        if (!"pending".equals(follow.getStatus())) {
            throw new BadRequestException("Follow request is not pending");
        }

        followRepository.delete(follow);

        log.info("Follow request rejected and deleted: followingId={}", followingId);
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

        followRepository.delete(follow);

        userStatsRepository.decrementFollowers(followerId);
        userStatsRepository.decrementFollowing(followingId);

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
}