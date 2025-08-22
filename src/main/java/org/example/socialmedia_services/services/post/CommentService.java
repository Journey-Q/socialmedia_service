package org.example.socialmedia_services.services.post;

import org.example.socialmedia_services.entity.User;
import org.example.socialmedia_services.entity.post.Comments;
import org.example.socialmedia_services.entity.post.Post;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.UserRepo;
import org.example.socialmedia_services.repository.post.CommentRepository;
import org.example.socialmedia_services.repository.post.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private UserRepo userRepository;

    @Transactional
    public Comments addComment(Long postId, Long userId, String commentText) {
        try {
            // Check if post exists
            Optional<Post> postOptional = postRepository.findById(postId);
            if (postOptional.isEmpty()) {
                throw new BadRequestException("Post not found");
            }
            Post post = postOptional.get();

            // Check if user exists
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                throw new BadRequestException("User not found");
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

            return savedComment;

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to add comment", e);
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

            // Find the post to update comments count
            Optional<Post> postOptional = postRepository.findById(comment.getPostId());
            if (postOptional.isPresent()) {
                Post post = postOptional.get();
                post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
                postRepository.save(post);
            }

            // Delete the comment
            commentRepository.delete(comment);

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

            // Get comments
            List<Comments> comments = commentRepository.findByPostIdOrderByCommentedAtDesc(postId);

            // Convert to response format with user details
            return comments.stream().map(comment -> {
                Map<String, Object> commentData = new HashMap<>();
                commentData.put("commentId", comment.getCommentId());
                commentData.put("postId", comment.getPostId());
                commentData.put("userId", comment.getUserId());
                commentData.put("commentText", comment.getCommentText());
                commentData.put("commentedAt", comment.getCommentedAt());

                // Get user details
                Optional<User> userOptional = userRepository.findById(comment.getUserId());
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    commentData.put("username", user.getUsername());
                    commentData.put("userProfileUrl", user.getProfileUrl());
                } else {
                    commentData.put("username", "Unknown User");
                    commentData.put("userProfileUrl", null);
                }

                return commentData;
            }).collect(Collectors.toList());

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get comments", e);
        }
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
}