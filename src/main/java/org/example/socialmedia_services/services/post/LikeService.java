package org.example.socialmedia_services.services.post;

import org.example.socialmedia_services.entity.UserProfile;
import org.example.socialmedia_services.entity.follow.UserStats;
import org.example.socialmedia_services.entity.post.Likes;
import org.example.socialmedia_services.entity.post.Post;
import org.example.socialmedia_services.entity.post.PostContent;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.UserProfileRepository;
import org.example.socialmedia_services.repository.follow.UserStatsRepository;
import org.example.socialmedia_services.repository.post.LikeRepository;
import org.example.socialmedia_services.repository.post.PostContentRepository;
import org.example.socialmedia_services.repository.post.PostRepository;
import org.example.socialmedia_services.services.kafka.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LikeService {

    private static final Logger logger = LoggerFactory.getLogger(LikeService.class);

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostContentRepository postContentRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserStatsRepository userStatsRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        try {
            // Check if post exists
            Optional<Post> postOptional = postRepository.findById(postId);
            if (postOptional.isEmpty()) {
                throw new BadRequestException("Post not found");
            }
            Post post = postOptional.get();

            // Check if user has already liked the post
            Optional<Likes> existingLike = likeRepository.findByPostIdAndUserId(postId, userId);

            if (existingLike.isPresent()) {
                // User has already liked - remove the like (unlike)
                likeRepository.delete(existingLike.get());

                // Update post likes count
                post.setLikesCount(post.getLikesCount() - 1);
                postRepository.save(post);

                // Decrement user's likes count in user_stats
                decrementUserLikesCount(userId);

                return false; // Unlike action
            } else {
                // User hasn't liked - add the like
                Likes newLike = new Likes(postId, userId);
                likeRepository.save(newLike);

                // Update post likes count
                post.setLikesCount(post.getLikesCount() + 1);
                postRepository.save(post);

                // Increment user's likes count in user_stats
                incrementUserLikesCount(userId);

                // Send Kafka event for like (only when liking, not unliking)
                sendLikeEventToKafka(userId, post);

                return true; // Like action
            }

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to toggle like", e);
        }
    }

    public boolean isPostLikedByUser(Long postId, Long userId) {
        try {
            return likeRepository.existsByPostIdAndUserId(postId, userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check like status", e);
        }
    }

    public Long getLikesCount(Long postId) {
        try {
            // Check if post exists
            Optional<Post> postOptional = postRepository.findById(postId);
            if (postOptional.isEmpty()) {
                throw new BadRequestException("Post not found");
            }

            return likeRepository.countByPostId(postId);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get likes count", e);
        }
    }

    /**
     * Ensures UserStats record exists for the user, creates if doesn't exist
     */
    private void ensureUserStatsExists(String userId) {
        try {
            if (!userStatsRepository.existsByUserId(userId)) {
                logger.info("Creating UserStats record for userId: {}", userId);
                UserStats newStats = new UserStats(userId);
                userStatsRepository.save(newStats);
                logger.info("UserStats record created successfully for userId: {}", userId);
            }
        } catch (Exception e) {
            logger.error("Failed to ensure UserStats exists for userId: {}", userId, e);
            throw new RuntimeException("Failed to create user stats", e);
        }
    }

    /**
     * Increment likes count for a user with proper error handling
     */
    private void incrementUserLikesCount(Long userId) {
        try {
            String userIdStr = String.valueOf(userId);

            // Ensure user stats record exists
            ensureUserStatsExists(userIdStr);

            // Increment the count
            int rowsUpdated = userStatsRepository.incrementLikes(userIdStr);

            if (rowsUpdated > 0) {
                logger.info("Successfully incremented likes count for userId: {}", userId);
            } else {
                logger.warn("No rows updated when incrementing likes for userId: {}", userId);
            }
        } catch (Exception e) {
            logger.error("Failed to increment likes count for userId: {}", userId, e);
            // Don't throw - we don't want to fail the like action if stats update fails
        }
    }

    /**
     * Decrement likes count for a user with proper error handling
     */
    private void decrementUserLikesCount(Long userId) {
        try {
            String userIdStr = String.valueOf(userId);

            // Ensure user stats record exists
            ensureUserStatsExists(userIdStr);

            // Decrement the count
            int rowsUpdated = userStatsRepository.decrementLikes(userIdStr);

            if (rowsUpdated > 0) {
                logger.info("Successfully decremented likes count for userId: {}", userId);
            } else {
                logger.warn("No rows updated when decrementing likes for userId: {}", userId);
            }
        } catch (Exception e) {
            logger.error("Failed to decrement likes count for userId: {}", userId, e);
            // Don't throw - we don't want to fail the unlike action if stats update fails
        }
    }

    private void sendLikeEventToKafka(Long senderId, Post post) {
        try {
            // Get sender's profile
            Optional<UserProfile> senderProfileOpt = userProfileRepository.findActiveByUserId(String.valueOf(senderId));
            if (senderProfileOpt.isEmpty()) {
                return; // Skip sending event if sender profile not found
            }
            UserProfile senderProfile = senderProfileOpt.get();

            // Get post content for post name
            String postName = null;
            PostContent postContent = postContentRepository.findById(post.getPostId()).orElse(null);
            if (postContent != null && postContent.getJourneyTitle() != null) {
                postName = postContent.getJourneyTitle();
            }

            // Send Kafka event
            kafkaProducerService.sendLikeEvent(
                    String.valueOf(senderId),
                    String.valueOf(post.getCreatedById()),
                    senderProfile.getDisplayName(),
                    senderProfile.getProfileImageUrl(),
                    String.valueOf(post.getPostId()),
                    postName
            );
        } catch (Exception e) {
            // Log but don't fail the like operation if Kafka event fails
            // The exception is already logged in KafkaProducerService
        }
    }
}
