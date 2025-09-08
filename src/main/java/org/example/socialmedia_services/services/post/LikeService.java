package org.example.socialmedia_services.services.post;

import org.example.socialmedia_services.entity.post.Likes;
import org.example.socialmedia_services.entity.post.Post;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.post.LikeRepository;
import org.example.socialmedia_services.repository.post.PostRepository;
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
}
