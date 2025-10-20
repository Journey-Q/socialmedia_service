package org.example.socialmedia_services.controller.follow;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.socialmedia_services.dto.follow.*;
import org.example.socialmedia_services.entity.UserPrincipal;
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

    @PostMapping("/accept-request/{followId}")
    public ResponseEntity<?> acceptFollowRequest(@PathVariable Long followId) {
        String currentUserId = getCurrentUserId();

        boolean success = followService.acceptFollowRequest(followId, currentUserId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", success);
        responseData.put("message", success ? "Follow request accepted" : "Failed to accept follow request");

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/reject-request/{followId}")
    public ResponseEntity<?> rejectFollowRequest(@PathVariable Long followId) {
        String currentUserId = getCurrentUserId();

        boolean success = followService.rejectFollowRequest(followId, currentUserId);

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

    @GetMapping("/pending-requests")
    public ResponseEntity<?> getPendingFollowRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String currentUserId = getCurrentUserId();
        FollowRequestsListResponse requests = followService.getPendingFollowRequests(currentUserId, page, size);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", requests);

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

    @GetMapping("/is_following/{userId}")
    public ResponseEntity<?> isFollowing(@PathVariable String userId) {
        String currentUserId = getCurrentUserId();

        boolean isFollowing = followService.isFollowing(currentUserId, userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("isFollowing", isFollowing);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/is_pending/{userId}")
    public ResponseEntity<?> isPending(@PathVariable String userId) {
        String currentUserId = getCurrentUserId();

        boolean isPending = followService.isPending(currentUserId, userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("isPending", isPending);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<?> getFollowersByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        FollowersListResponse followers = followService.getFollowersWithProfiles(userId, page, size);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", followers);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<?> getFollowingByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        FollowingListResponse following = followService.getFollowingWithProfiles(userId, page, size);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", following);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/users-profiles")
    public ResponseEntity<?> getAllUsersWithStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        AllUsersProfilesResponse allUsers = followService.getAllUsersWithStats(page, size);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("data", allUsers);

        return ResponseEntity.ok(responseData);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Get UserDetails from principal
        UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
        String userId = String.valueOf(userDetails.getUserId());
        return userId;
    }
}