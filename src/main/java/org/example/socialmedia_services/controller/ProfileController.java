package org.example.socialmedia_services.controller;


import jakarta.validation.Valid;
import org.example.socialmedia_services.dto.profile.ProfileSetupdtoRequest;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.services.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/profile")
@Validated
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@PathVariable String id) {

            ProfileSetupdtoRequest profile = profileService.getUserProfile(id);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "Profile retrieved successfully");
            responseData.put("data", profile);

            return ResponseEntity.ok(responseData);

    }

    @PostMapping("/setup")
    public ResponseEntity<?> completeUserSetup(
            @Valid @RequestBody ProfileSetupdtoRequest setupDTO) {

        boolean response = profileService.completeUserSetup(setupDTO);

        // Return a JSON object instead of a string
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Profile setup complete");
        // Add any other data the frontend might need

        return ResponseEntity.ok(responseData);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileSetupdtoRequest profileDTO) {
        boolean response = profileService.updateProfile(profileDTO);

        Map<String, Object> responseData = new HashMap<>();

        if (response) {
            responseData.put("success", true);
            responseData.put("message", "Profile updated successfully");
            return ResponseEntity.ok(responseData);
        } else {
            responseData.put("success", false);
            responseData.put("message", "Profile update failed");
            return ResponseEntity.badRequest().body(responseData);
        }
    }


}
