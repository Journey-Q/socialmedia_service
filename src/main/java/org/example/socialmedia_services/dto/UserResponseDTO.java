package org.example.socialmedia_services.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long userId;
    private String username;
    private String email;
    private String profileUrl;
    private LocalDateTime createdAt;
    private String role;
    private Boolean isActive;
    private Boolean isSetup;
}
