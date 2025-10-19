package org.example.socialmedia_services.dto.jointTrip_Group_gallery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UploadMultipleImagesDTO {

    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "At least one image is required")
    @Valid
    private List<ImageData> images;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageData {
        @NotNull(message = "Cloudinary URL is required")
        private String cloudinaryUrl;

        @NotNull(message = "Cloudinary Public ID is required")
        private String cloudinaryPublicId;

        private String caption;
        private Long fileSize;
        private Integer imageWidth;
        private Integer imageHeight;
        private String format;
    }
}