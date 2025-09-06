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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
@Validated
public class FollowController {

    private final FollowService followService;

    @PostMapping("/create")
    public ResponseEntity<?> createFollow(@Valid @RequestBody FollowRequest followRequest) {
        String currentUserId = getCurrentUserId();

        Follow follow = followService.createFollow(currentUserId, followRequest.getFollowerId(), "pending");

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Follow request sent successfully");
        responseData.put("data", follow);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<?> getFollowers(@PathVariable String userId) {
        List<Follow> followers = followService.getFollowers(userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", followers);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<?> getFollowing(@PathVariable String userId) {
        List<Follow> following = followService.getFollowing(userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", following);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/followers-with-profiles/{userId}")
    public ResponseEntity<?> getFollowersWithProfiles(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        FollowListResponse followers = followService.getFollowersWithProfiles(userId, page, size);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", followers);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/following-with-profiles/{userId}")
    public ResponseEntity<?> getFollowingWithProfiles(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        FollowListResponse following = followService.getFollowingWithProfiles(userId, page, size);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", following);

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/update-status")
    public ResponseEntity<?> updateStatus(@Valid @RequestBody FollowStatusRequest statusRequest) {
        boolean success = followService.updateStatus(
                statusRequest.getFollowingId(),
                statusRequest.getFollowerId(),
                statusRequest.getStatus()
        );

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("message", success ? "Follow status updated successfully" : "Failed to update follow status");

        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFollow(@RequestParam String followingId, @RequestParam String followerId) {
        boolean success = followService.deleteFollow(followingId, followerId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("message", success ? "Follow relationship deleted successfully" : "Failed to delete follow relationship");

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/pending-requests/{userId}")
    public ResponseEntity<?> getPendingRequests(@PathVariable String userId) {
        List<Follow> pendingRequests = followService.getPendingFollowRequests(userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", pendingRequests);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/relationship")
    public ResponseEntity<?> getFollowRelationship(@RequestParam String followingId, @RequestParam String followerId) {
        Optional<Follow> relationship = followService.getFollowRelationship(followingId, followerId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("exists", relationship.isPresent());
        if (relationship.isPresent()) {
            responseData.put("data", relationship.get());
        }

        return ResponseEntity.ok(responseData);
    }

    // User Stats endpoints
    @PostMapping("/stats/create/{userId}")
    public ResponseEntity<?> createUserStats(@PathVariable String userId) {
        boolean success = followService.createStats(userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("message", success ? "User stats created successfully" : "User stats already exist");

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/stats/increment-followers/{userId}")
    public ResponseEntity<?> incrementFollowers(@PathVariable String userId) {
        boolean success = followService.incrementFollowers(userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("message", "Followers count updated");

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/stats/decrement-followers/{userId}")
    public ResponseEntity<?> decrementFollowers(@PathVariable String userId) {
        boolean success = followService.decrementFollowers(userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("message", "Followers count updated");

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/stats/increment-following/{userId}")
    public ResponseEntity<?> incrementFollowing(@PathVariable String userId) {
        boolean success = followService.incrementFollowing(userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("message", "Following count updated");

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/stats/decrement-following/{userId}")
    public ResponseEntity<?> decrementFollowing(@PathVariable String userId) {
        boolean success = followService.decrementFollowing(userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("message", "Following count updated");

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

    // Convenience endpoints for mobile app
    @PostMapping("/send-request")
    public ResponseEntity<?> sendFollowRequest(@Valid @RequestBody FollowRequest followRequest) {
        String currentUserId = getCurrentUserId();

        Follow follow = followService.createFollow(currentUserId, followRequest.getFollowerId(), "pending");

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Follow request sent");
        responseData.put("followId", follow.getId());

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/accept-request")
    public ResponseEntity<?> acceptFollowRequest(@RequestParam String followingId) {
        String currentUserId = getCurrentUserId();

        boolean success = followService.updateStatus(followingId, currentUserId, "accepted");

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("message", success ? "Follow request accepted" : "Failed to accept follow request");

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/reject-request")
    public ResponseEntity<?> rejectFollowRequest(@RequestParam String followingId) {
        String currentUserId = getCurrentUserId();

        boolean success = followService.updateStatus(followingId, currentUserId, "rejected");

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("message", success ? "Follow request rejected" : "Failed to reject follow request");

        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/unfollow")
    public ResponseEntity<?> unfollowUser(@RequestParam String followerId) {
        String currentUserId = getCurrentUserId();

        boolean success = followService.deleteFollow(currentUserId, followerId);

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
        FollowListResponse followers = followService.getFollowersWithProfiles(currentUserId, page, size);

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
        FollowListResponse following = followService.getFollowingWithProfiles(currentUserId, page, size);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", following);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/my-pending-requests")
    public ResponseEntity<?> getMyPendingRequests() {
        String currentUserId = getCurrentUserId();
        List<Follow> pendingRequests = followService.getPendingFollowRequests(currentUserId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", pendingRequests);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/my-stats")
    public ResponseEntity<?> getMyStats() {
        String currentUserId = getCurrentUserId();
        UserStatsResponse stats = followService.getStats(currentUserId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", stats);

        return ResponseEntity.ok(responseData);
    }

    // Admin/Debug endpoints
    @PostMapping("/admin/initialize-all-stats")
    public ResponseEntity<?> initializeAllUserStats() {
        try {
            // Get user IDs from your database - replace these with actual user IDs
            List<String> allUserIds = Arrays.asList("9", "10", "16", "20"); // Update with actual user IDs

            int initialized = 0;
            int alreadyExists = 0;

            for (String userId : allUserIds) {
                boolean created = followService.createStats(userId);
                if (created) {
                    initialized++;
                } else {
                    alreadyExists++;
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stats initialization completed");
            response.put("initialized", initialized);
            response.put("alreadyExists", alreadyExists);
            response.put("totalUsers", allUserIds.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/debug/create-test-follow")
    public ResponseEntity<?> createTestFollow() {
        try {
            // Create a test follow relationship between user 9 and user 10
            Follow testFollow = followService.createFollow("9", "10", "pending");

            // Accept the follow request
            boolean accepted = followService.updateStatus("9", "10", "accepted");

            // Get stats for both users
            UserStatsResponse stats9 = followService.getStats("9");
            UserStatsResponse stats10 = followService.getStats("10");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("followCreated", testFollow);
            response.put("followAccepted", accepted);
            response.put("user9Stats", stats9);
            response.put("user10Stats", stats10);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Helper method to get current user ID from authentication context
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