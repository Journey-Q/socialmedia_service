package org.example.socialmedia_services.controller.subscription;

import lombok.extern.slf4j.Slf4j;
import org.example.socialmedia_services.dto.subscription.PremiumStatusResponse;
import org.example.socialmedia_services.dto.subscription.SubscriptionRequest;
import org.example.socialmedia_services.dto.subscription.SubscriptionResponse;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.services.subscription.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@Slf4j
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * Add a new subscription
     * POST /api/subscriptions/add
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addSubscription(@RequestBody SubscriptionRequest request) {
        try {
            log.info("Adding subscription for userId: {}", request.getUserId());

            SubscriptionResponse response = subscriptionService.addSubscription(request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Subscription added successfully");
            result.put("subscription", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (BadRequestException e) {
            log.error("Bad request while adding subscription: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            log.error("Error adding subscription: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to add subscription: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Check if user is premium
     * GET /api/subscriptions/premium-status/{userId}
     */
    @GetMapping("/premium-status/{userId}")
    public ResponseEntity<Map<String, Object>> getPremiumStatus(@PathVariable Long userId) {
        try {
            log.info("Checking premium status for userId: {}", userId);

            PremiumStatusResponse response = subscriptionService.getPremiumStatus(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("premiumStatus", response);

            return ResponseEntity.ok(result);

        } catch (BadRequestException e) {
            log.error("Bad request while checking premium status: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            log.error("Error checking premium status: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to check premium status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get all subscriptions for a user
     * GET /api/subscriptions/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserSubscriptions(@PathVariable Long userId) {
        try {
            log.info("Getting subscriptions for userId: {}", userId);

            List<SubscriptionResponse> subscriptions = subscriptionService.getUserSubscriptions(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("subscriptions", subscriptions);
            result.put("count", subscriptions.size());

            return ResponseEntity.ok(result);

        } catch (BadRequestException e) {
            log.error("Bad request while getting user subscriptions: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            log.error("Error getting user subscriptions: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to get user subscriptions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Cancel a subscription
     * DELETE /api/subscriptions/{subscriptionId}/cancel
     */
    @DeleteMapping("/{subscriptionId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelSubscription(
            @PathVariable Long subscriptionId,
            @RequestParam Long userId) {
        try {
            log.info("Cancelling subscription: subscriptionId={}, userId={}", subscriptionId, userId);

            SubscriptionResponse response = subscriptionService.cancelSubscription(subscriptionId, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Subscription cancelled successfully");
            result.put("subscription", response);

            return ResponseEntity.ok(result);

        } catch (BadRequestException e) {
            log.error("Bad request while cancelling subscription: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            log.error("Error cancelling subscription: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to cancel subscription: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Check if user is premium (simple boolean response)
     * GET /api/subscriptions/is-premium/{userId}
     */
    @GetMapping("/is-premium/{userId}")
    public ResponseEntity<Map<String, Object>> isPremiumUser(@PathVariable Long userId) {
        try {
            log.info("Checking if user is premium: userId={}", userId);

            PremiumStatusResponse response = subscriptionService.getPremiumStatus(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("userId", userId);
            result.put("isPremium", response.getIsPremium());

            return ResponseEntity.ok(result);

        } catch (BadRequestException e) {
            log.error("Bad request while checking premium user: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            log.error("Error checking premium user: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to check premium user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
