package org.example.socialmedia_services.controller.post;

import jakarta.validation.Valid;
import org.example.socialmedia_services.dto.post.AddToBucketListRequest;
import org.example.socialmedia_services.dto.post.GetBucketListResponse;
import org.example.socialmedia_services.dto.post.UpdateVisitedStatusRequest;
import org.example.socialmedia_services.entity.UserPrincipal;
import org.example.socialmedia_services.services.post.BucketListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/bucket-list")
@Validated
public class BucketListController {

    @Autowired
    private BucketListService bucketListService;

    /**
     * Get bucket list for current user
     */
    @GetMapping
    public ResponseEntity<?> getBucketList() {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getUser().getUserId();

            GetBucketListResponse response = bucketListService.getBucketList(userId);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "Bucket list retrieved successfully");
            responseData.put("data", response);

            return ResponseEntity.ok(responseData);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving bucket list: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get bucket list for specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBucketListByUserId(@PathVariable Long userId) {
        try {
            GetBucketListResponse response = bucketListService.getBucketList(userId);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "Bucket list retrieved successfully");
            responseData.put("data", response);

            return ResponseEntity.ok(responseData);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error retrieving bucket list: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Add post to bucket list
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToBucketList(@Valid @RequestBody AddToBucketListRequest request) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getUser().getUserId();

            GetBucketListResponse response = bucketListService.addToBucketList(userId, request.getPostId());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "Post added to bucket list successfully");
            responseData.put("data", response);

            return ResponseEntity.ok(responseData);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error adding post to bucket list: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Remove post from bucket list
     */
    @DeleteMapping("/remove/{postId}")
    public ResponseEntity<?> removeFromBucketList(@PathVariable String postId) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getUser().getUserId();

            GetBucketListResponse response = bucketListService.removeFromBucketList(userId, postId);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "Post removed from bucket list successfully");
            responseData.put("data", response);

            return ResponseEntity.ok(responseData);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error removing post from bucket list: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Update visited status of post in bucket list
     */
    @PutMapping("/update-visited")
    public ResponseEntity<?> updateVisitedStatus(@Valid @RequestBody UpdateVisitedStatusRequest request) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getUser().getUserId();

            GetBucketListResponse response = bucketListService.updateVisitedStatus(
                    userId, request.getPostId(), request.isVisited());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "Visited status updated successfully");
            responseData.put("data", response);

            return ResponseEntity.ok(responseData);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error updating visited status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }


}