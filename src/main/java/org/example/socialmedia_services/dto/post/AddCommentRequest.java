package org.example.socialmedia_services.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AddCommentRequest {

    @NotBlank(message = "Comment text is required")
    @Size(max = 1000, message = "Comment text cannot exceed 1000 characters")
    private String commentText;

    // Default constructor
    public AddCommentRequest() {}

    // Constructor with parameter
    public AddCommentRequest(String commentText) {
        this.commentText = commentText;
    }

    // Getters and Setters

}