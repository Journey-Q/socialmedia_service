package org.example.socialmedia_services.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_user_username", columnList = "username"),
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_is_active", columnList = "is_active"),
                @Index(name = "idx_user_created_at", columnList = "created_at")
        })
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name="profile_url")
    private String profileUrl;


    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String role= "USER";


    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_trip_influencer")
    private Boolean isTripInfluencer = false;

    private Boolean isSetup = false;



    // Default constructor
    public User() {
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    // Constructor with parameters
    public User(String username, String password, String email, String role) {
        this();
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public void SetisSetup(boolean isSetup) {
        this.isSetup = isSetup;
    }

    public boolean getIsSetup() {
        return isSetup;
    }


    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +

                '}';
    }
}
