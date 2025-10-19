package org.example.socialmedia_services.entity.jointTrip;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "trip_gallery_images",
        indexes = {
                @Index(name = "idx_gallery_group_id", columnList = "group_id"),
                @Index(name = "idx_gallery_trip_id", columnList = "trip_id"),
                @Index(name = "idx_gallery_user_id", columnList = "user_id"),
                @Index(name = "idx_gallery_created_at", columnList = "created_at")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TripGalleryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // User who uploaded the image

    @Column(name = "cloudinary_url", nullable = false, length = 500)
    private String cloudinaryUrl; // Full URL from Cloudinary

    @Column(name = "cloudinary_public_id", nullable = false, length = 255)
    private String cloudinaryPublicId; // Public ID for deletion from Cloudinary

    @Column(name = "caption", length = 500)
    private String caption;

    @Column(name = "file_size")
    private Long fileSize; // File size in bytes

    @Column(name = "image_width")
    private Integer imageWidth;

    @Column(name = "image_height")
    private Integer imageHeight;

    @Column(name = "format", length = 20)
    private String format; // e.g., jpg, png, webp

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
    public void updateCaption(String newCaption) {
        this.caption = newCaption;
    }

    public void softDelete() {
        this.isDeleted = true;
    }
}
