package org.example.socialmedia_services.dto.jointTrip_Group_gallery;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateCaptionDTO {

    @NotNull(message = "Image ID is required")
    private Long imageId;

    @Size(max = 500, message = "Caption cannot exceed 500 characters")
    private String caption;
}