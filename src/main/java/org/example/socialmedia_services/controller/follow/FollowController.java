package org.example.socialmedia_services.controller.follow;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.socialmedia_services.dto.follow.*;
import org.example.socialmedia_services.entity.follow.Follow;
import org.example.socialmedia_services.services.follow.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
@Validated
public class FollowController {

    private final FollowService followService;

    @PostMapping("/send-request")
    public ResponseEntity<?> sendFollowRequest(@Valid @RequestBody FollowRequest followRequest) {
        String currentUserId = getCurrentUserId();

        Follow follow = followService.createFollow(currentUserId, followRequest.getFollowerId());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Follow request sent");
        responseData.put("followId", follow.getId());

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/accept-request")
    public ResponseEntity<?> acceptFollowRequest(@RequestParam String followingId) {
        String currentUserId = getCurrentUserId();

        boolean success = followService.acceptFollowRequest(followingId, currentUserId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("message", success ? "Follow request accepted" : "Failed to accept follow request");

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/reject-request")
    public ResponseEntity<?> rejectFollowRequest(@RequestParam String followingId) {
        String currentUserId = getCurrentUserId();

        boolean success = followService.rejectFollowRequest(followingId, currentUserId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("message", success ? "Follow request rejected" : "Failed to reject follow request");

        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/unfollow")
    public ResponseEntity<?> unfollowUser(@RequestParam String followerId) {
        String currentUserId = getCurrentUserId();

        boolean success = followService.unfollowUser(currentUserId, followerId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("message", success ? "Unfollowed successfully" : "Failed to unfollow");

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/my-followers")
    public ResponseEntity<?> getMyFollowers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String currentUserId = getCurrentUserId();
        FollowersListResponse followers = followService.getFollowersWithProfiles(currentUserId, page, size);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", followers);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/my-following")
    public ResponseEntity<?> getMyFollowing(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String currentUserId = getCurrentUserId();
        FollowingListResponse following = followService.getFollowingWithProfiles(currentUserId, page, size);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", following);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<?> getUserStats(@PathVariable String userId) {
        UserStatsResponse stats = followService.getStats(userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", stats);

        return ResponseEntity.ok(responseData);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.example.socialmedia_services.entity.UserPrincipal) {
            org.example.socialmedia_services.entity.UserPrincipal userPrincipal =
                    (org.example.socialmedia_services.entity.UserPrincipal) authentication.getPrincipal();
            return userPrincipal.getUserId().toString();
        }
        throw new RuntimeException("User not authenticated");
    }
}