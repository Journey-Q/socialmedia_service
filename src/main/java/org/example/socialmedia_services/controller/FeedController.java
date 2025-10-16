package org.example.socialmedia_services.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmedia_services.dto.feed.FeedResponse;
import org.example.socialmedia_services.services.feed.FeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
@Slf4j
public class FeedController {

    private final FeedService feedService;
    @GetMapping
    public ResponseEntity<?> getPersonalizedFeed(
            @RequestParam String userId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        try {
            log.info("Fetching personalized feed for user: {}, page: {}, size: {}", userId, page, size);

            FeedResponse feedResponse = feedService.getPersonalizedFeed(userId, page, size);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "Personalized feed retrieved successfully");
            responseData.put("data", feedResponse);

            return ResponseEntity.ok(responseData);

        } catch (Exception e) {
            log.error("Error fetching personalized feed: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch personalized feed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
