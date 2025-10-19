package org.example.socialmedia_services.dto.jointTrip_Group_gallery;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DeleteMultipleImagesDTO {

    @NotEmpty(message = "At least one image ID is required")
    private List<Long> imageIds;
}