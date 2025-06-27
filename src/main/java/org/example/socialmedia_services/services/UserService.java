package org.example.socialmedia_services.services;
import org.example.socialmedia_services.config.AppConfig;
import org.example.socialmedia_services.dto.AuthResponse;
import org.example.socialmedia_services.dto.LoginRequest;
import org.example.socialmedia_services.dto.SignupRequest;
import org.example.socialmedia_services.entity.User;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;

@Service
public class UserService {

    @Autowired
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    private UserRepo repo;

    @Autowired
    private AppConfig appConfig;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

   @Transactional
    public AuthResponse register(SignupRequest req) {

        boolean emailExists = repo.existsByEmail(req.getEmail());
        boolean usernameExists = repo.existsByUsername(req.getName());

        if (emailExists && usernameExists) {
            throw new BadRequestException("Both email and username are already in use");
        } else if (emailExists) {
            throw new BadRequestException("Email is already in use");
        } else if (usernameExists) {
            throw new BadRequestException("Username is already in use");
        }
        User user = new User();
        user.setEmail(req.getEmail());
        user.setUsername(req.getName());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setProfileUrl("https://ui-avatars.com/api/?name=" + req.getName() + "&background=6366f1&color=fff&size=200");
        repo.save(user);
        String token = jwtService.generateToken(user);
        AuthResponse authResponse = new AuthResponse();
        authResponse.setUser(user);
        authResponse.setAccessToken(token);
        authResponse.setExpiresIn(appConfig.getExpirationTime());
        return authResponse;

    }

  @Transactional
    public AuthResponse verify(LoginRequest req) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        if (authentication.isAuthenticated()) {
            User user = new User();
            user = (User) authentication.getPrincipal();
            String token = jwtService.generateToken(user);
            AuthResponse authResponse = new AuthResponse();
            authResponse.setUser(user);
            authResponse.setAccessToken(token);
            authResponse.setExpiresIn(appConfig.getExpirationTime());
            return authResponse;
        } else {
            throw new BadCredentialsException("Invalid username or password");

        }
    }
}
