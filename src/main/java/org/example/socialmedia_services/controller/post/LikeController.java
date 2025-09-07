package org.example.socialmedia_services.controller.post;

import org.example.socialmedia_services.entity.UserPrincipal;
import org.example.socialmedia_services.services.post.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/posts/{postId}/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleLike(@PathVariable Long postId) {

        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUser().getUserId();

        boolean isLiked = likeService.toggleLike(postId, userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("isLiked", isLiked);
        responseData.put("message", isLiked ? "Post liked successfully" : "Post unliked successfully");

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getLikeStatus(@PathVariable Long postId) {

        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUser().getUserId();

        boolean isLiked = likeService.isPostLikedByUser(postId, userId);
        Long likesCount = likeService.getLikesCount(postId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("isLiked", isLiked);
        responseData.put("likesCount", likesCount);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/count")
    public ResponseEntity<?> getLikesCount(@PathVariable Long postId) {

        Long likesCount = likeService.getLikesCount(postId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("likesCount", likesCount);

        return ResponseEntity.ok(responseData);
    }
}
