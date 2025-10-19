package org.example.socialmedia_services.repository.jointTrip;

import org.example.socialmedia_services.entity.jointTrip.TripGalleryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripGalleryImageRepository extends JpaRepository<TripGalleryImage, Long> {

    // Find all images for a specific trip (excluding deleted ones)
    @Query("SELECT img FROM TripGalleryImage img WHERE img.tripId = :tripId AND img.isDeleted = false ORDER BY img.createdAt DESC")
    List<TripGalleryImage> findByTripIdAndIsDeletedFalse(@Param("tripId") Long tripId);

    // Find all images for a specific group (excluding deleted ones)
    @Query("SELECT img FROM TripGalleryImage img WHERE img.groupId = :groupId AND img.isDeleted = false ORDER BY img.createdAt DESC")
    List<TripGalleryImage> findByGroupIdAndIsDeletedFalse(@Param("groupId") Long groupId);

    // Find all images uploaded by a specific user in a trip
    @Query("SELECT img FROM TripGalleryImage img WHERE img.tripId = :tripId AND img.userId = :userId AND img.isDeleted = false ORDER BY img.createdAt DESC")
    List<TripGalleryImage> findByTripIdAndUserIdAndIsDeletedFalse(@Param("tripId") Long tripId, @Param("userId") Long userId);

    // Find a specific image by ID (excluding deleted ones)
    @Query("SELECT img FROM TripGalleryImage img WHERE img.imageId = :imageId AND img.isDeleted = false")
    Optional<TripGalleryImage> findByIdAndIsDeletedFalse(@Param("imageId") Long imageId);

    // Check if image exists by Cloudinary Public ID
    boolean existsByCloudinaryPublicId(String cloudinaryPublicId);

    // Find image by Cloudinary Public ID
    Optional<TripGalleryImage> findByCloudinaryPublicId(String cloudinaryPublicId);

    // Count images for a trip
    @Query("SELECT COUNT(img) FROM TripGalleryImage img WHERE img.tripId = :tripId AND img.isDeleted = false")
    Long countByTripIdAndIsDeletedFalse(@Param("tripId") Long tripId);

    // Count images for a group
    @Query("SELECT COUNT(img) FROM TripGalleryImage img WHERE img.groupId = :groupId AND img.isDeleted = false")
    Long countByGroupIdAndIsDeletedFalse(@Param("groupId") Long groupId);

    // Delete all images for a trip (soft delete)
    @Query("UPDATE TripGalleryImage img SET img.isDeleted = true WHERE img.tripId = :tripId")
    void softDeleteAllByTripId(@Param("tripId") Long tripId);
}
