package org.example.socialmedia_services.services.jointTrip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmedia_services.dto.jointTrip_Group_gallery.*;
import org.example.socialmedia_services.entity.User;
import org.example.socialmedia_services.entity.jointTrip.TripGalleryImage;
import org.example.socialmedia_services.repository.jointTrip.TripGalleryImageRepository;
import org.example.socialmedia_services.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripGalleryService {

    private final TripGalleryImageRepository galleryRepository;
    private final UserRepo userRepo;

    /**
     * Upload a single image
     */
    @Transactional
    public ImageResponseDTO uploadImage(UploadImageDTO uploadDTO) {
        log.info("Uploading image for trip: {}, group: {}, user: {}",
                uploadDTO.getTripId(), uploadDTO.getGroupId(), uploadDTO.getUserId());

        // Check if image already exists
        if (galleryRepository.existsByCloudinaryPublicId(uploadDTO.getCloudinaryPublicId())) {
            throw new RuntimeException("Image with this Cloudinary ID already exists");
        }

        TripGalleryImage image = TripGalleryImage.builder()
                .tripId(uploadDTO.getTripId())
                .groupId(uploadDTO.getGroupId())
                .userId(uploadDTO.getUserId())
                .cloudinaryUrl(uploadDTO.getCloudinaryUrl())
                .cloudinaryPublicId(uploadDTO.getCloudinaryPublicId())
                .caption(uploadDTO.getCaption())
                .fileSize(uploadDTO.getFileSize())
                .imageWidth(uploadDTO.getImageWidth())
                .imageHeight(uploadDTO.getImageHeight())
                .format(uploadDTO.getFormat())
                .build();

        TripGalleryImage savedImage = galleryRepository.save(image);
        log.info("Image uploaded successfully with ID: {}", savedImage.getImageId());

        return convertToResponseDTO(savedImage);
    }

    /**
     * Upload multiple images
     */
    @Transactional
    public List<ImageResponseDTO> uploadMultipleImages(UploadMultipleImagesDTO uploadDTO) {
        log.info("Uploading {} images for trip: {}, group: {}",
                uploadDTO.getImages().size(), uploadDTO.getTripId(), uploadDTO.getGroupId());

        List<ImageResponseDTO> responses = new ArrayList<>();

        for (UploadMultipleImagesDTO.ImageData imageData : uploadDTO.getImages()) {
            // Check if image already exists
            if (galleryRepository.existsByCloudinaryPublicId(imageData.getCloudinaryPublicId())) {
                log.warn("Skipping duplicate image: {}", imageData.getCloudinaryPublicId());
                continue;
            }

            TripGalleryImage image = TripGalleryImage.builder()
                    .tripId(uploadDTO.getTripId())
                    .groupId(uploadDTO.getGroupId())
                    .userId(uploadDTO.getUserId())
                    .cloudinaryUrl(imageData.getCloudinaryUrl())
                    .cloudinaryPublicId(imageData.getCloudinaryPublicId())
                    .caption(imageData.getCaption())
                    .fileSize(imageData.getFileSize())
                    .imageWidth(imageData.getImageWidth())
                    .imageHeight(imageData.getImageHeight())
                    .format(imageData.getFormat())
                    .build();

            TripGalleryImage savedImage = galleryRepository.save(image);
            responses.add(convertToResponseDTO(savedImage));
        }

        log.info("Successfully uploaded {} images", responses.size());
        return responses;
    }

    /**
     * Get all images for a trip
     */
    @Transactional(readOnly = true)
    public GalleryImagesResponse getGalleryImages(Long tripId) {
        log.info("Fetching gallery images for trip: {}", tripId);

        List<TripGalleryImage> images = galleryRepository.findByTripIdAndIsDeletedFalse(tripId);
        List<ImageResponseDTO> imageDTOs = images.stream()
                .map(this::convertToResponseDTOWithUserInfo)
                .collect(Collectors.toList());

        Long totalImages = galleryRepository.countByTripIdAndIsDeletedFalse(tripId);

        return GalleryImagesResponse.builder()
                .tripId(tripId)
                .groupId(images.isEmpty() ? null : images.get(0).getGroupId())
                .totalImages(totalImages)
                .images(imageDTOs)
                .build();
    }

    /**
     * Get a specific image by ID
     */
    @Transactional(readOnly = true)
    public ImageResponseDTO getImageById(Long imageId) {
        log.info("Fetching image with ID: {}", imageId);

        TripGalleryImage image = galleryRepository.findByIdAndIsDeletedFalse(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with ID: " + imageId));

        return convertToResponseDTOWithUserInfo(image);
    }

    /**
     * Update image caption
     */
    @Transactional
    public ImageResponseDTO updateImageCaption(UpdateCaptionDTO updateDTO) {
        log.info("Updating caption for image: {}", updateDTO.getImageId());

        TripGalleryImage image = galleryRepository.findByIdAndIsDeletedFalse(updateDTO.getImageId())
                .orElseThrow(() -> new RuntimeException("Image not found with ID: " + updateDTO.getImageId()));

        image.updateCaption(updateDTO.getCaption());
        TripGalleryImage updatedImage = galleryRepository.save(image);

        log.info("Caption updated successfully for image: {}", updateDTO.getImageId());
        return convertToResponseDTO(updatedImage);
    }

    /**
     * Delete a single image (soft delete)
     */
    @Transactional
    public void deleteImage(Long imageId) {
        log.info("Deleting image with ID: {}", imageId);

        TripGalleryImage image = galleryRepository.findByIdAndIsDeletedFalse(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with ID: " + imageId));

        image.softDelete();
        galleryRepository.save(image);

        log.info("Image deleted successfully: {}", imageId);
    }

    /**
     * Delete multiple images (soft delete)
     */
    @Transactional
    public void deleteMultipleImages(DeleteMultipleImagesDTO deleteDTO) {
        log.info("Deleting {} images", deleteDTO.getImageIds().size());

        for (Long imageId : deleteDTO.getImageIds()) {
            Optional<TripGalleryImage> imageOpt = galleryRepository.findByIdAndIsDeletedFalse(imageId);
            if (imageOpt.isPresent()) {
                TripGalleryImage image = imageOpt.get();
                image.softDelete();
                galleryRepository.save(image);
            } else {
                log.warn("Image not found or already deleted: {}", imageId);
            }
        }

        log.info("Successfully deleted {} images", deleteDTO.getImageIds().size());
    }

    /**
     * Check if image exists
     */
    @Transactional(readOnly = true)
    public boolean doesImageExist(Long imageId) {
        return galleryRepository.findByIdAndIsDeletedFalse(imageId).isPresent();
    }

    /**
     * Get image file size
     */
    @Transactional(readOnly = true)
    public Long getImageFileSize(Long imageId) {
        TripGalleryImage image = galleryRepository.findByIdAndIsDeletedFalse(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with ID: " + imageId));
        return image.getFileSize();
    }

    /**
     * Get images by user for a trip
     */
    @Transactional(readOnly = true)
    public List<ImageResponseDTO> getImagesByUserAndTrip(Long tripId, Long userId) {
        log.info("Fetching images for trip: {} uploaded by user: {}", tripId, userId);

        List<TripGalleryImage> images = galleryRepository.findByTripIdAndUserIdAndIsDeletedFalse(tripId, userId);
        return images.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get images by group
     */
    @Transactional(readOnly = true)
    public GalleryImagesResponse getImagesByGroup(Long groupId) {
        log.info("Fetching images for group: {}", groupId);

        List<TripGalleryImage> images = galleryRepository.findByGroupIdAndIsDeletedFalse(groupId);
        List<ImageResponseDTO> imageDTOs = images.stream()
                .map(this::convertToResponseDTOWithUserInfo)
                .collect(Collectors.toList());

        Long totalImages = galleryRepository.countByGroupIdAndIsDeletedFalse(groupId);

        return GalleryImagesResponse.builder()
                .groupId(groupId)
                .tripId(images.isEmpty() ? null : images.get(0).getTripId())
                .totalImages(totalImages)
                .images(imageDTOs)
                .build();
    }

    // Helper methods

    private ImageResponseDTO convertToResponseDTO(TripGalleryImage image) {
        return ImageResponseDTO.builder()
                .imageId(image.getImageId())
                .tripId(image.getTripId())
                .groupId(image.getGroupId())
                .userId(image.getUserId())
                .cloudinaryUrl(image.getCloudinaryUrl())
                .cloudinaryPublicId(image.getCloudinaryPublicId())
                .caption(image.getCaption())
                .fileSize(image.getFileSize())
                .imageWidth(image.getImageWidth())
                .imageHeight(image.getImageHeight())
                .format(image.getFormat())
                .createdAt(image.getCreatedAt())
                .updatedAt(image.getUpdatedAt())
                .build();
    }

    private ImageResponseDTO convertToResponseDTOWithUserInfo(TripGalleryImage image) {
        ImageResponseDTO dto = convertToResponseDTO(image);

        // Fetch user information
        Optional<User> userOpt = userRepo.findById(image.getUserId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            dto.setUploaderUsername(user.getUsername());
            dto.setUploaderProfileUrl(user.getProfileUrl());
        }

        return dto;
    }
}