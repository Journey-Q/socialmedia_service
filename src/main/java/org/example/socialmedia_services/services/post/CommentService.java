package org.example.socialmedia_services.services.post;

import org.example.socialmedia_services.entity.UserProfile;
import org.example.socialmedia_services.entity.post.Comments;
import org.example.socialmedia_services.entity.post.Post;
import org.example.socialmedia_services.entity.post.PostContent;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.UserProfileRepository;
import org.example.socialmedia_services.repository.post.CommentRepository;
import org.example.socialmedia_services.repository.post.PostContentRepository;
import org.example.socialmedia_services.repository.post.PostRepository;
import org.example.socialmedia_services.services.kafka.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostContentRepository postContentRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Transactional
    public Comments addComment(Long postId, Long userId, String commentText) {
        try {
            // Check if post exists
            Optional<Post> postOptional = postRepository.findById(postId);
            if (postOptional.isEmpty()) {
                throw new BadRequestException("Post not found");
            }
            Post post = postOptional.get();

            // Check if user exists and is active
            Optional<UserProfile> userOptional = userProfileRepository.findActiveByUserId(String.valueOf(userId));
            if (userOptional.isEmpty()) {
                throw new BadRequestException("User not found or inactive");
            }

            // Validate comment text
            if (commentText == null || commentText.trim().isEmpty()) {
                throw new BadRequestException("Comment text cannot be empty");
            }

            // Create and save comment
            Comments comment = new Comments(postId, userId, commentText.trim());
            Comments savedComment = commentRepository.save(comment);

            // Update comments count
            post.setCommentsCount(post.getCommentsCount() + 1);
            postRepository.save(post);

            // Send Kafka event for comment
            sendCommentEventToKafka(userId, post, savedComment, userOptional.get());

            return savedComment;

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to add comment", e);
        }
    }

    @Transactional
    public Comments replyToComment(Long postId, Long parentCommentId, Long userId, String commentText) {
        try {
            // Check if post exists
            Optional<Post> postOptional = postRepository.findById(postId);
            if (postOptional.isEmpty()) {
                throw new BadRequestException("Post not found");
            }
            Post post = postOptional.get();

            // Check if parent comment exists and belongs to the same post
            Optional<Comments> parentCommentOptional = commentRepository.findById(parentCommentId);
            if (parentCommentOptional.isEmpty()) {
                throw new BadRequestException("Parent comment not found");
            }
            Comments parentComment = parentCommentOptional.get();

            if (!parentComment.getPostId().equals(postId)) {
                throw new BadRequestException("Parent comment does not belong to this post");
            }

            // Check if user exists and is active
            Optional<UserProfile> userOptional = userProfileRepository.findActiveByUserId(String.valueOf(userId));
            if (userOptional.isEmpty()) {
                throw new BadRequestException("User not found or inactive");
            }

            // Validate comment text
            if (commentText == null || commentText.trim().isEmpty()) {
                throw new BadRequestException("Comment text cannot be empty");
            }

            // Create and save reply comment
            Comments replyComment = new Comments(postId, userId, commentText.trim(), parentCommentId);
            Comments savedReplyComment = commentRepository.save(replyComment);

            // Update comments count
            post.setCommentsCount(post.getCommentsCount() + 1);
            postRepository.save(post);

            // Send Kafka event for reply comment
            sendCommentEventToKafka(userId, post, savedReplyComment, userOptional.get());

            return savedReplyComment;

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to reply to comment", e);
        }
    }

    @Transactional
    public boolean deleteComment(Long commentId, Long userId) {
        try {
            // Find the comment
            Optional<Comments> commentOptional = commentRepository.findById(commentId);
            if (commentOptional.isEmpty()) {
                throw new BadRequestException("Comment not found");
            }
            Comments comment = commentOptional.get();

            // Check if the user is the owner of the comment
            if (!comment.getUserId().equals(userId)) {
                throw new BadRequestException("You are not authorized to delete this comment");
            }

            // Find and delete all replies to this comment first
            List<Comments> replies = commentRepository.findByParentIdOrderByCommentedAtAsc(commentId);
            int totalDeletedComments = 1 + replies.size(); // Original comment + replies

            for (Comments reply : replies) {
                commentRepository.delete(reply);
            }

            // Delete the original comment
            commentRepository.delete(comment);

            // Find the post to update comments count
            Optional<Post> postOptional = postRepository.findById(comment.getPostId());
            if (postOptional.isPresent()) {
                Post post = postOptional.get();
                post.setCommentsCount(Math.max(0, post.getCommentsCount() - totalDeletedComments));
                postRepository.save(post);
            }

            return true;

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete comment", e);
        }
    }

    public List<Map<String, Object>> getCommentsByPostId(Long postId) {
        try {
            // Check if post exists
            Optional<Post> postOptional = postRepository.findById(postId);
            if (postOptional.isEmpty()) {
                throw new BadRequestException("Post not found");
            }

            // Get all comments for the post
            List<Comments> allComments = commentRepository.findByPostIdOrderByCommentedAtDesc(postId);

            // Separate top-level comments and replies
            List<Comments> topLevelComments = allComments.stream()
                    .filter(comment -> comment.getParentId() == null)
                    .collect(Collectors.toList());

            Map<Long, List<Comments>> repliesMap = allComments.stream()
                    .filter(comment -> comment.getParentId() != null)
                    .collect(Collectors.groupingBy(Comments::getParentId));

            // Convert to response format with nested replies
            return topLevelComments.stream().map(comment -> {
                Map<String, Object> commentData = buildCommentData(comment);

                // Add replies if any
                List<Comments> replies = repliesMap.getOrDefault(comment.getCommentId(), new ArrayList<>());
                List<Map<String, Object>> replyDataList = replies.stream()
                        .map(this::buildCommentData)
                        .collect(Collectors.toList());

                commentData.put("replies", replyDataList);
                commentData.put("replyCount", replyDataList.size());

                return commentData;
            }).collect(Collectors.toList());

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get comments", e);
        }
    }

    private Map<String, Object> buildCommentData(Comments comment) {
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("commentId", comment.getCommentId());
        commentData.put("postId", comment.getPostId());
        commentData.put("userId", comment.getUserId());
        commentData.put("commentText", comment.getCommentText());
        commentData.put("parentId", comment.getParentId());
        commentData.put("commentedAt", comment.getCommentedAt());

        // Get user details (only active users)
        Optional<UserProfile> userOptional = userProfileRepository.findActiveByUserId(String.valueOf(comment.getUserId()));
        if (userOptional.isPresent()) {
            UserProfile userProfile = userOptional.get();
            commentData.put("username", userProfile.getDisplayName());
            commentData.put("userProfileUrl", userProfile.getProfileImageUrl());
        } else {
            commentData.put("username", "Unknown User");
            commentData.put("userProfileUrl", null);
        }

        return commentData;
    }

    public Long getCommentsCount(Long postId) {
        try {
            // Check if post exists
            Optional<Post> postOptional = postRepository.findById(postId);
            if (postOptional.isEmpty()) {
                throw new BadRequestException("Post not found");
            }

            return commentRepository.countByPostId(postId);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get comments count", e);
        }
    }

    public List<Map<String, Object>> getRepliesByCommentId(Long commentId) {
        try {
            // Check if comment exists
            Optional<Comments> commentOptional = commentRepository.findById(commentId);
            if (commentOptional.isEmpty()) {
                throw new BadRequestException("Comment not found");
            }

            // Get replies
            List<Comments> replies = commentRepository.findByParentIdOrderByCommentedAtAsc(commentId);

            // Convert to response format with user details
            return replies.stream().map(this::buildCommentData).collect(Collectors.toList());

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get replies", e);
        }
    }

    private void sendCommentEventToKafka(Long senderId, Post post, Comments comment, UserProfile senderProfile) {
        try {
            // Get post content for post name
            String postName = null;
            PostContent postContent = postContentRepository.findById(post.getPostId()).orElse(null);
            if (postContent != null && postContent.getJourneyTitle() != null) {
                postName = postContent.getJourneyTitle();
            }

            // Send Kafka event
            kafkaProducerService.sendCommentEvent(
                    String.valueOf(senderId),
                    String.valueOf(post.getCreatedById()),
                    senderProfile.getDisplayName(),
                    senderProfile.getProfileImageUrl(),
                    String.valueOf(post.getPostId()),
                    postName,
                    String.valueOf(comment.getCommentId()),
                    comment.getCommentText()
            );
        } catch (Exception e) {
            // Log but don't fail the comment operation if Kafka event fails
            // The exception is already logged in KafkaProducerService
        }
    }
}