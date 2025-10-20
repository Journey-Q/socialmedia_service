package org.example.socialmedia_services.scheduler;

import org.example.socialmedia_services.entity.User;
import org.example.socialmedia_services.entity.post.Post;
import org.example.socialmedia_services.repository.UserRepo;
import org.example.socialmedia_services.repository.follow.FollowRepository;
import org.example.socialmedia_services.repository.post.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class TripInfluencerScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TripInfluencerScheduler.class);

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private PostRepository postRepository;

    /**
     * Scheduled task to check and update tripfluencer status for users
     * Runs every 10 minutes
     * Users become tripfluencers if they have:
     * - More than 1000 followers
     * - More than 10000 total likes on their posts
     */
    @Scheduled(fixedRate = 600000) // 10 minutes = 600,000 milliseconds
    @Transactional
    public void updateTripInfluencerStatus() {
        logger.info("Starting scheduled tripfluencer status check");
        try {
            List<User> allUsers = userRepo.findAll();
            int updatedCount = 0;

            for (User user : allUsers) {
                String userId = String.valueOf(user.getUserId());

                // Count followers (users who are following this user with status "ACTIVE")
                long followersCount = followRepository.findByFollowingIdAndStatus(userId, "ACTIVE", Pageable.unpaged()).getTotalElements();

                // Get all posts by this user and sum up their likes
                List<Post> userPosts = postRepository.findByCreatedByIdOrderByCreatedAtDesc(user.getUserId());
                long totalLikes = userPosts.stream()
                    .mapToLong(post -> post.getLikesCount() != null ? post.getLikesCount() : 0)
                    .sum();

                // Check if user meets tripfluencer criteria
                boolean shouldBeTripInfluencer = followersCount > 1000 && totalLikes > 10000;

                // Update status if changed
                if (shouldBeTripInfluencer && !Boolean.TRUE.equals(user.getIsTripInfluencer())) {
                    user.setIsTripInfluencer(true);
                    userRepo.save(user);
                    updatedCount++;
                    logger.info("User {} promoted to tripfluencer (followers: {}, total likes: {})",
                               user.getUsername(), followersCount, totalLikes);
                } else if (!shouldBeTripInfluencer && Boolean.TRUE.equals(user.getIsTripInfluencer())) {
                    user.setIsTripInfluencer(false);
                    userRepo.save(user);
                    updatedCount++;
                    logger.info("User {} demoted from tripfluencer (followers: {}, total likes: {})",
                               user.getUsername(), followersCount, totalLikes);
                }
            }

            logger.info("Scheduled tripfluencer status check completed. Updated {} users", updatedCount);
        } catch (Exception e) {
            logger.error("Error in scheduled tripfluencer status check: {}", e.getMessage(), e);
        }
    }
}
