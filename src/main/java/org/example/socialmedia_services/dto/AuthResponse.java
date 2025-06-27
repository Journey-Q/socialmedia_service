package org.example.socialmedia_services.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.socialmedia_services.entity.User;
import org.springframework.stereotype.Service;


@Setter
@Getter
public class AuthResponse {
    // Setter methods
    // Getter methods
    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private User user;

    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, User user) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public AuthResponse() {

    }

}
