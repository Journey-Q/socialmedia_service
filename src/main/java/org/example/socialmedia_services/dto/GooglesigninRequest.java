package org.example.socialmedia_services.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class GooglesigninRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String Photourl;

    // Getter methods
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotourl() {
        return Photourl;
    }

    public String setPhotourl(String url){
        return this.Photourl = url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
