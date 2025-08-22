package org.example.socialmedia_services.controller.post;

import jakarta.validation.Valid;
import org.example.socialmedia_services.dto.post.GetPostResponse;
import org.example.socialmedia_services.dto.post.PostContentRequest;
import org.example.socialmedia_services.entity.UserPrincipal;
import org.example.socialmedia_services.services.post.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@Validated
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping("/create")
    public ResponseEntity<?> createPost(@Valid @RequestBody PostContentRequest request) {

        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUser().getUserId();

        GetPostResponse response = postService.createPost(request, userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Post created successfully");
        responseData.put("data", response);

        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {

        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUser().getUserId();

        boolean deleted = postService.deletePost(postId, userId);

        Map<String, Object> responseData = new HashMap<>();

        if (deleted) {
            responseData.put("success", true);
            responseData.put("message", "Post deleted successfully");
            return ResponseEntity.ok(responseData);
        } else {
            responseData.put("success", false);
            responseData.put("message", "Failed to delete post");
            return ResponseEntity.badRequest().body(responseData);
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {

        GetPostResponse response = postService.getPostById(postId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Post retrieved successfully");
        responseData.put("data", response);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GetPostResponse>> getPostsByUser(@PathVariable Long userId) {

            List<GetPostResponse> responses = postService.getPostsByUser(userId);
            return ResponseEntity.ok(responses);
    }

    @GetMapping("/feed")
    public ResponseEntity<List<GetPostResponse>> getAllPosts() {
        try {
            List<GetPostResponse> responses = postService.getAllPosts();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}