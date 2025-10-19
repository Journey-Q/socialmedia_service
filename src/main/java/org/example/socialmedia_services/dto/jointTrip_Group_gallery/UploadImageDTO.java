package org.example.socialmedia_services.dto.jointTrip_Group_gallery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UploadImageDTO {

    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Cloudinary URL is required")
    private String cloudinaryUrl;

    @NotBlank(message = "Cloudinary Public ID is required")
    private String cloudinaryPublicId;

    private String caption;

    private Long fileSize;

    private Integer imageWidth;

    private Integer imageHeight;

    private String format;
}