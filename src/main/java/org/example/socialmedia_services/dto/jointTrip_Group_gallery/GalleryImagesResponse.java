package org.example.socialmedia_services.dto.jointTrip_Group_gallery;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GalleryImagesResponse {

    private Long tripId;
    private Long groupId;
    private Long totalImages;
    private List<ImageResponseDTO> images;
}