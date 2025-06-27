package org.example.socialmedia_services.controller;

import jakarta.validation.Valid;
import org.example.socialmedia_services.dto.AuthResponse;
import org.example.socialmedia_services.dto.LoginRequest;
import org.example.socialmedia_services.dto.SignupRequest;
import org.example.socialmedia_services.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userservice;


    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        AuthResponse response = userservice.register(signUpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = userservice.verify(loginRequest);
        return ResponseEntity.ok(response);
    }
}
