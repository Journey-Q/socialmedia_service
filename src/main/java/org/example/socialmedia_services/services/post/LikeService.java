package org.example.socialmedia_services.services.post;

import org.example.socialmedia_services.entity.UserProfile;
import org.example.socialmedia_services.entity.post.Likes;
import org.example.socialmedia_services.entity.post.Post;
import org.example.socialmedia_services.entity.post.PostContent;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.UserProfileRepository;
import org.example.socialmedia_services.repository.post.LikeRepository;
import org.example.socialmedia_services.repository.post.PostContentRepository;
import org.example.socialmedia_services.repository.post.PostRepository;
import org.example.socialmedia_services.services.kafka.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostContentRepository postContentRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

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

                // Update likes count
                post.setLikesCount(post.getLikesCount() - 1);
                postRepository.save(post);

                return false; // Unlike action
            } else {
                // User hasn't liked - add the like
                Likes newLike = new Likes(postId, userId);
                likeRepository.save(newLike);

                // Update likes count
                post.setLikesCount(post.getLikesCount() + 1);
                postRepository.save(post);

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
