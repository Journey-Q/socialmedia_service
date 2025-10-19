package org.example.socialmedia_services.dto.jointTrip_Group_gallery;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ImageResponseDTO {

    private Long imageId;
    private Long tripId;
    private Long groupId;
    private Long userId;
    private String cloudinaryUrl;
    private String cloudinaryPublicId;
    private String caption;
    private Long fileSize;
    private Integer imageWidth;
    private Integer imageHeight;
    private String format;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional user info (optional)
    private String uploaderUsername;
    private String uploaderProfileUrl;
}