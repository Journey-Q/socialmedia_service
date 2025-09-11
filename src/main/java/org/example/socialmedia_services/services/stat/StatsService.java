package org.example.socialmedia_services.services.stat;

import org.example.socialmedia_services.dto.post.GetStatResponse;
import org.example.socialmedia_services.entity.follow.UserStats;
import org.example.socialmedia_services.repository.follow.UserStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    @Autowired
    private UserStatsRepository userStatsRepository;

    public GetStatResponse getUserStats(String userId) {
        UserStats userStats = userStatsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User stats not found for user: " + userId));

        return GetStatResponse.builder()
                .userId(userStats.getUserId())
                .followersCount(userStats.getFollowersCount())
                .postsCount(userStats.getPostsCount())
                .followingCount(userStats.getFollowingCount())
                .build();
    }
}