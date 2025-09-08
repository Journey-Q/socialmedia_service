package org.example.socialmedia_services.controller.jointTrip;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.socialmedia_services.dto.jointTrip.CreateJointTripRequest;
import org.example.socialmedia_services.dto.jointTrip.JointTripResponse;
import org.example.socialmedia_services.entity.UserPrincipal;
import org.example.socialmedia_services.services.jointTrip.JointTripService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class JointTripController {

    private final JointTripService jointTripService;

    @PostMapping("/create")
    public ResponseEntity<?> createTrip(
            @Valid @RequestBody CreateJointTripRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        JointTripResponse response = jointTripService.createJointTrip(request, userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Trip created successfully");
        responseData.put("data", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<?> getTripById(@PathVariable Long tripId) {
        JointTripResponse response = jointTripService.getTripById(tripId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Trip retrieved successfully");
        responseData.put("data", response);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/my-trips")
    public ResponseEntity<?> getMyTrips(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        List<JointTripResponse> trips = jointTripService.getUserTrips(userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "User trips retrieved successfully");
        responseData.put("data", trips);
        responseData.put("count", trips.size());

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableTrips() {
        List<JointTripResponse> trips = jointTripService.getAvailableTrips();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Available trips retrieved successfully");
        responseData.put("data", trips);
        responseData.put("count", trips.size());

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchTrips(@RequestParam String query) {
        List<JointTripResponse> trips = jointTripService.searchTrips(query);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Trip search completed successfully");
        responseData.put("data", trips);
        responseData.put("count", trips.size());
        responseData.put("query", query);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/destination/{destination}")
    public ResponseEntity<?> getTripsByDestination(@PathVariable String destination) {
        List<JointTripResponse> trips = jointTripService.getTripsByDestination(destination);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Trips by destination retrieved successfully");
        responseData.put("data", trips);
        responseData.put("count", trips.size());
        responseData.put("destination", destination);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/type/{tripType}")
    public ResponseEntity<?> getTripsByType(@PathVariable String tripType) {
        List<JointTripResponse> trips = jointTripService.getTripsByType(tripType);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Trips by type retrieved successfully");
        responseData.put("data", trips);
        responseData.put("count", trips.size());
        responseData.put("tripType", tripType);

        return ResponseEntity.ok(responseData);
    }

    @PutMapping("/{tripId}")
    public ResponseEntity<?> updateTrip(
            @PathVariable Long tripId,
            @Valid @RequestBody CreateJointTripRequest request,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        JointTripResponse response = jointTripService.updateTrip(tripId, request, userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Trip updated successfully");
        responseData.put("data", response);

        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<?> deleteTrip(
            @PathVariable Long tripId,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();

        jointTripService.deleteTrip(tripId, userId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Trip deleted successfully");

        return ResponseEntity.ok(responseData);
    }
}