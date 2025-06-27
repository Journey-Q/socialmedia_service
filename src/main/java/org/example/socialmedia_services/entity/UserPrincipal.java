package org.example.socialmedia_services.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert user role to Spring Security authority format
        return Collections.singleton(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        // Account never expires in your system
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // No account locking mechanism in your system
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Credentials (password) never expire in your system
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Use the isActive field from your User entity
        return user.getIsActive() != null ? user.getIsActive() : false;
    }

    // Additional methods to access the wrapped User entity
    public User getUser() {
        return user;
    }

    public Long getUserId() {
        return user.getUserId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getRole() {
        return user.getRole();
    }

    public boolean isActive() {
        return user.getIsActive() != null ? user.getIsActive() : false;
    }
}