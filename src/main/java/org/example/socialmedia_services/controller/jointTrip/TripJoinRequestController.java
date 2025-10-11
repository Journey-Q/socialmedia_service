package org.example.socialmedia_services.controller.jointTrip;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.socialmedia_services.dto.jointTrip.SendTripRequestDto;
import org.example.socialmedia_services.dto.jointTrip.TripJoinRequestResponse;
import org.example.socialmedia_services.entity.UserPrincipal;
import org.example.socialmedia_services.services.jointTrip.TripJoinRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trip-requests")
@RequiredArgsConstructor
public class TripJoinRequestController {

    private final TripJoinRequestService tripJoinRequestService;

    @PostMapping("/send")
    public ResponseEntity<?> sendTripRequests(
            @Valid @RequestBody SendTripRequestDto request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        List<TripJoinRequestResponse> responses = tripJoinRequestService.sendTripRequests(request, userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Trip requests sent successfully");
        responseData.put("data", responses);
        responseData.put("count", responses.size());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

    @GetMapping("/received")
    public ResponseEntity<?> getReceivedRequests(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        List<TripJoinRequestResponse> requests = tripJoinRequestService.getReceivedRequests(userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Received requests retrieved successfully");
        responseData.put("data", requests);
        responseData.put("count", requests.size());

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/sent")
    public ResponseEntity<?> getSentRequests(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        List<TripJoinRequestResponse> requests = tripJoinRequestService.getSentRequests(userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Sent requests retrieved successfully");
        responseData.put("data", requests);
        responseData.put("count", requests.size());

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        List<TripJoinRequestResponse> requests = tripJoinRequestService.getPendingRequests(userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Pending requests retrieved successfully");
        responseData.put("data", requests);
        responseData.put("count", requests.size());

        return ResponseEntity.ok(responseData);
    }

    @PutMapping("/{requestId}/accept")
    public ResponseEntity<?> acceptRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        TripJoinRequestResponse response = tripJoinRequestService.acceptRequest(requestId, userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Request accepted successfully");
        responseData.put("data", response);

        return ResponseEntity.ok(responseData);
    }

    @PutMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        TripJoinRequestResponse response = tripJoinRequestService.rejectRequest(requestId, userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Request rejected successfully");
        responseData.put("data", response);

        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/{requestId}/cancel")
    public ResponseEntity<?> cancelRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        tripJoinRequestService.cancelRequest(requestId, userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Request cancelled successfully");

        return ResponseEntity.ok(responseData);
    }
}