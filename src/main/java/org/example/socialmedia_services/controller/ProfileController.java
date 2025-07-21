package org.example.socialmedia_services.controller;


import jakarta.validation.Valid;
import org.example.socialmedia_services.dto.profile.ProfileSetupdtoRequest;
import org.example.socialmedia_services.services.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@Validated
public class ProfileController {

    @Autowired
    private ProfileService profileService;



    @PostMapping("/setup")
    public ResponseEntity<?> completeUserSetup(
            @Valid @RequestBody ProfileSetupdtoRequest setupDTO) {

        boolean response = profileService.completeUserSetup(setupDTO);
        return ResponseEntity.ok("Profile setup complete");
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileSetupdtoRequest profileDTO) {
        boolean response = profileService.updateProfile(profileDTO);
        return ResponseEntity.ok("Profile setup complete");
    }


}
