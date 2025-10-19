package org.example.socialmedia_services.controller.jointTrip;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmedia_services.dto.jointTrip_Group_gallery.*;
import org.example.socialmedia_services.services.jointTrip.TripGalleryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TripGalleryController {

    private final TripGalleryService galleryService;

    /**
     * Upload a single image
     * POST /api/gallery/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(@Valid @RequestBody UploadImageDTO uploadDTO) {
        log.info("Received request to upload image for trip: {}", uploadDTO.getTripId());

        try {
            ImageResponseDTO response = galleryService.uploadImage(uploadDTO);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Image uploaded successfully");
            result.put("data", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            log.error("Error uploading image: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to upload image: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Upload multiple images
     * POST /api/gallery/upload/multiple
     */
    @PostMapping("/upload/multiple")
    public ResponseEntity<Map<String, Object>> uploadMultipleImages(@Valid @RequestBody UploadMultipleImagesDTO uploadDTO) {
        log.info("Received request to upload {} images for trip: {}",
                uploadDTO.getImages().size(), uploadDTO.getTripId());

        try {
            List<ImageResponseDTO> responses = galleryService.uploadMultipleImages(uploadDTO);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", responses.size() + " images uploaded successfully");
            result.put("data", responses);
            result.put("totalUploaded", responses.size());

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            log.error("Error uploading multiple images: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to upload images: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get all images for a trip
     * GET /api/gallery/trip/{tripId}
     */
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<Map<String, Object>> getGalleryImages(@PathVariable Long tripId) {
        log.info("Received request to get gallery images for trip: {}", tripId);

        try {
            GalleryImagesResponse response = galleryService.getGalleryImages(tripId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Gallery images retrieved successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error retrieving gallery images: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve gallery images: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get all images for a group
     * GET /api/gallery/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<Map<String, Object>> getImagesByGroup(@PathVariable Long groupId) {
        log.info("Received request to get images for group: {}", groupId);

        try {
            GalleryImagesResponse response = galleryService.getImagesByGroup(groupId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Group images retrieved successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error retrieving group images: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve group images: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get a specific image by ID
     * GET /api/gallery/{imageId}
     */
    @GetMapping("/{imageId}")
    public ResponseEntity<Map<String, Object>> getImageById(@PathVariable Long imageId) {
        log.info("Received request to get image with ID: {}", imageId);

        try {
            ImageResponseDTO response = galleryService.getImageById(imageId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Image retrieved successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error retrieving image: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve image: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Update image caption
     * PUT /api/gallery/{imageId}/caption
     */
    @PutMapping("/{imageId}/caption")
    public ResponseEntity<Map<String, Object>> updateImageCaption(
            @PathVariable Long imageId,
            @Valid @RequestBody UpdateCaptionDTO updateDTO) {

        log.info("Received request to update caption for image: {}", imageId);

        try {
            // Ensure imageId from path matches the DTO
            updateDTO.setImageId(imageId);

            ImageResponseDTO response = galleryService.updateImageCaption(updateDTO);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Caption updated successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error updating caption: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to update caption: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Delete a single image
     * DELETE /api/gallery/{imageId}
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable Long imageId) {
        log.info("Received request to delete image: {}", imageId);

        try {
            galleryService.deleteImage(imageId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Image deleted successfully");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error deleting image: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to delete image: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Delete multiple images
     * DELETE /api/gallery/delete/multiple
     */
    @DeleteMapping("/delete/multiple")
    public ResponseEntity<Map<String, Object>> deleteMultipleImages(@Valid @RequestBody DeleteMultipleImagesDTO deleteDTO) {
        log.info("Received request to delete {} images", deleteDTO.getImageIds().size());

        try {
            galleryService.deleteMultipleImages(deleteDTO);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", deleteDTO.getImageIds().size() + " images deleted successfully");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error deleting multiple images: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to delete images: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Check if image exists
     * GET /api/gallery/{imageId}/exists
     */
    @GetMapping("/{imageId}/exists")
    public ResponseEntity<Map<String, Object>> doesImageExist(@PathVariable Long imageId) {
        log.info("Received request to check if image exists: {}", imageId);

        try {
            boolean exists = galleryService.doesImageExist(imageId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("exists", exists);
            result.put("imageId", imageId);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error checking image existence: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to check image existence: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get image file size
     * GET /api/gallery/{imageId}/size
     */
    @GetMapping("/{imageId}/size")
    public ResponseEntity<Map<String, Object>> getImageFileSize(@PathVariable Long imageId) {
        log.info("Received request to get file size for image: {}", imageId);

        try {
            Long fileSize = galleryService.getImageFileSize(imageId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("fileSize", fileSize);
            result.put("fileSizeKB", fileSize / 1024);
            result.put("fileSizeMB", fileSize / (1024 * 1024));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error getting image file size: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to get file size: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get images uploaded by a specific user for a trip
     * GET /api/gallery/trip/{tripId}/user/{userId}
     */
    @GetMapping("/trip/{tripId}/user/{userId}")
    public ResponseEntity<Map<String, Object>> getImagesByUserAndTrip(
            @PathVariable Long tripId,
            @PathVariable Long userId) {

        log.info("Received request to get images for trip: {} by user: {}", tripId, userId);

        try {
            List<ImageResponseDTO> images = galleryService.getImagesByUserAndTrip(tripId, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "User images retrieved successfully");
            result.put("data", images);
            result.put("totalImages", images.size());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error retrieving user images: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve user images: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}