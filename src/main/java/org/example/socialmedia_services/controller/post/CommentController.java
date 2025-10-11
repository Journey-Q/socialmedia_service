package org.example.socialmedia_services.controller.post;

import jakarta.validation.Valid;
import org.example.socialmedia_services.dto.post.AddCommentRequest;
import org.example.socialmedia_services.dto.post.AddCommentRequest;
import org.example.socialmedia_services.entity.UserPrincipal;
import org.example.socialmedia_services.entity.post.Comments;
import org.example.socialmedia_services.services.post.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts/{postId}/comments")
@Validated
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/add")
    public ResponseEntity<?> addComment(@PathVariable Long postId, @Valid @RequestBody AddCommentRequest request) {

        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUser().getUserId();

        Comments comment = commentService.addComment(postId, userId, request.getCommentText());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Comment added successfully");
        responseData.put("data", comment);

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/{commentId}/reply")
    public ResponseEntity<?> replyToComment(@PathVariable Long postId,
                                            @PathVariable Long commentId,
                                            @Valid @RequestBody AddCommentRequest request) {

        // Get current authew3nticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUser().getUserId();

        Comments replyComment = commentService.replyToComment(postId, commentId, userId, request.getCommentText());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Reply added successfully");
        responseData.put("data", replyComment);

        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {

        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUser().getUserId();

        boolean deleted = commentService.deleteComment(commentId, userId);

        Map<String, Object> responseData = new HashMap<>();

        if (deleted) {
            responseData.put("success", true);
            responseData.put("message", "Comment deleted successfully");
            return ResponseEntity.ok(responseData);
        } else {
            responseData.put("success", false);
            responseData.put("message", "Failed to delete comment");
            return ResponseEntity.badRequest().body(responseData);
        }
    }

    @GetMapping
    public ResponseEntity<?> getComments(@PathVariable Long postId) {

        List<Map<String, Object>> comments = commentService.getCommentsByPostId(postId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Comments retrieved successfully");
        responseData.put("data", comments);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<?> getReplies(@PathVariable Long postId, @PathVariable Long commentId) {

        List<Map<String, Object>> replies = commentService.getRepliesByCommentId(commentId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Replies retrieved successfully");
        responseData.put("data", replies);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/count")
    public ResponseEntity<?> getCommentsCount(@PathVariable Long postId) {

        Long commentsCount = commentService.getCommentsCount(postId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("commentsCount", commentsCount);

        return ResponseEntity.ok(responseData);
    }
}