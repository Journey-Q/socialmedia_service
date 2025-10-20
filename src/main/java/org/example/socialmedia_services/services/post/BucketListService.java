package org.example.socialmedia_services.services.post;

import org.example.socialmedia_services.dto.post.BucketListItemResponse;
import org.example.socialmedia_services.dto.post.GetBucketListResponse;
import org.example.socialmedia_services.dto.post.GetPostResponse;
import org.example.socialmedia_services.dto.post.PlaceWiseContentDto;
import org.example.socialmedia_services.entity.post.BucketList;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.post.BucketListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BucketListService {

    @Autowired
    private BucketListRepository bucketListRepository;

    @Autowired
    private PostService postService;

    /**
     * Get bucket list for a user
     */
    @Transactional(readOnly = true, propagation = org.springframework.transaction.annotation.Propagation.SUPPORTS)
    public GetBucketListResponse getBucketList(Long userId) {
        try {
            Optional<BucketList> bucketListOptional = bucketListRepository.findByUserId(userId);

            if (bucketListOptional.isEmpty()) {
                // Return empty bucket list if none exists
                return new GetBucketListResponse(null, userId, new ArrayList<>(), null, null);
            }

            BucketList bucketList = bucketListOptional.get();
            List<BucketListItemResponse> bucketListItems = new ArrayList<>();

            if (bucketList.getBucketListItems() != null) {
                for (BucketList.BucketListItemData item : bucketList.getBucketListItems()) {
                    try {
                        // Get specific post details by post ID - FIXED: now using getPostById
                        Long postId = Long.valueOf(item.getPostId());
                        GetPostResponse postDetails = postService.getPostById(postId);

                        if (postDetails != null) {
                            BucketListItemResponse bucketListItem = new BucketListItemResponse();

                            // Extract destination from journey title or places visited
                            String destination = extractDestinationFromPost(postDetails);
                            bucketListItem.setDestination(destination);

                            // Get first image from place wise content
                            String image = extractFirstImageFromPost(postDetails);
                            bucketListItem.setImage(image);

                            // Set completion status
                            bucketListItem.setCompleted(item.isVisited());
                            bucketListItem.setVisitedDate(item.getVisitedDate());

                            // Set description from location details
                            String description = extractDescriptionFromPost(postDetails);
                            bucketListItem.setDescription(description);

                            // Set journey ID
                            bucketListItem.setJourneyId(item.getPostId());

                            bucketListItems.add(bucketListItem);
                        }
                    } catch (Exception e) {
                        // Skip invalid post IDs and continue
                        System.err.println("Error processing bucket list item with postId " + item.getPostId() + ": " + e.getMessage());
                        continue;
                    }
                }
            }

            return new GetBucketListResponse(
                    bucketList.getBucketListId(),
                    bucketList.getUserId(),
                    bucketListItems,
                    bucketList.getCreatedAt(),
                    bucketList.getUpdatedAt()
            );

        } catch (Exception e) {
            throw new RuntimeException("Error fetching bucket list: " + e.getMessage(), e);
        }
    }

    /**
     * Add post to bucket list
     */
    @Transactional
    public GetBucketListResponse addToBucketList(Long userId, String postId) {
        try {
            Optional<BucketList> bucketListOptional = bucketListRepository.findByUserId(userId);
            BucketList bucketList;

            if (bucketListOptional.isEmpty()) {
                // Create new bucket list
                bucketList = new BucketList();
                bucketList.setUserId(userId);
                bucketList.setBucketListItems(new ArrayList<>());
            } else {
                bucketList = bucketListOptional.get();
                if (bucketList.getBucketListItems() == null) {
                    bucketList.setBucketListItems(new ArrayList<>());
                }
            }

            // Check if post already exists in bucket list
            boolean postExists = bucketList.getBucketListItems().stream()
                    .anyMatch(item -> item.getPostId().equals(postId));

            if (postExists) {
                throw new BadRequestException("Post already exists in bucket list");
            }

            // Add new item to bucket list
            BucketList.BucketListItemData newItem = new BucketList.BucketListItemData(postId, false, null);
            bucketList.getBucketListItems().add(newItem);

            // Save bucket list
            bucketListRepository.save(bucketList);

            // Return simple response without fetching full details
            return new GetBucketListResponse(
                    bucketList.getBucketListId(),
                    bucketList.getUserId(),
                    new ArrayList<>(),
                    bucketList.getCreatedAt(),
                    bucketList.getUpdatedAt()
            );

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error adding post to bucket list: " + e.getMessage(), e);
        }
    }

    /**
     * Remove post from bucket list
     */
    @Transactional
    public GetBucketListResponse removeFromBucketList(Long userId, String postId) {
        try {
            Optional<BucketList> bucketListOptional = bucketListRepository.findByUserId(userId);

            if (bucketListOptional.isEmpty()) {
                throw new BadRequestException("Bucket list not found");
            }

            BucketList bucketList = bucketListOptional.get();

            if (bucketList.getBucketListItems() == null) {
                throw new BadRequestException("Post not found in bucket list");
            }

            // Remove item from bucket list
            boolean removed = bucketList.getBucketListItems().removeIf(item -> item.getPostId().equals(postId));

            if (!removed) {
                throw new BadRequestException("Post not found in bucket list");
            }

            // Save updated bucket list
            bucketListRepository.save(bucketList);

            // Return simple response without fetching full details
            return new GetBucketListResponse(
                    bucketList.getBucketListId(),
                    bucketList.getUserId(),
                    new ArrayList<>(),
                    bucketList.getCreatedAt(),
                    bucketList.getUpdatedAt()
            );

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error removing post from bucket list: " + e.getMessage(), e);
        }
    }

    /**
     * Update visited status of a post in bucket list
     */
    @Transactional
    public GetBucketListResponse updateVisitedStatus(Long userId, String postId, boolean isVisited) {
        try {
            Optional<BucketList> bucketListOptional = bucketListRepository.findByUserId(userId);

            if (bucketListOptional.isEmpty()) {
                throw new BadRequestException("Bucket list not found");
            }

            BucketList bucketList = bucketListOptional.get();

            if (bucketList.getBucketListItems() == null) {
                throw new BadRequestException("Post not found in bucket list");
            }

            // Find and update the item
            BucketList.BucketListItemData itemToUpdate = bucketList.getBucketListItems().stream()
                    .filter(item -> item.getPostId().equals(postId))
                    .findFirst()
                    .orElse(null);

            if (itemToUpdate == null) {
                throw new BadRequestException("Post not found in bucket list");
            }

            itemToUpdate.setVisited(isVisited);
            itemToUpdate.setVisitedDate(isVisited ? LocalDateTime.now() : null);

            // Save updated bucket list
            bucketListRepository.save(bucketList);

            // Return simple response without fetching full details
            return new GetBucketListResponse(
                    bucketList.getBucketListId(),
                    bucketList.getUserId(),
                    new ArrayList<>(),
                    bucketList.getCreatedAt(),
                    bucketList.getUpdatedAt()
            );

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error updating visited status: " + e.getMessage(), e);
        }
    }

    /**
     * Check if a post is saved in user's bucket list
     */
    @Transactional(readOnly = true, propagation = org.springframework.transaction.annotation.Propagation.SUPPORTS)
    public boolean isSaved(Long userId, String postId) {
        try {
            Optional<BucketList> bucketListOptional = bucketListRepository.findByUserId(userId);

            if (bucketListOptional.isEmpty()) {
                return false;
            }

            BucketList bucketList = bucketListOptional.get();

            if (bucketList.getBucketListItems() == null || bucketList.getBucketListItems().isEmpty()) {
                return false;
            }

            // Check if post exists in bucket list
            return bucketList.getBucketListItems().stream()
                    .anyMatch(item -> item.getPostId().equals(postId));

        } catch (Exception e) {
            throw new RuntimeException("Error checking if post is saved: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to extract destination name from post details
     */
    private String extractDestinationFromPost(GetPostResponse post) {
        // Try journey title first
        if (post.getJourneyTitle() != null && !post.getJourneyTitle().trim().isEmpty()) {
            return post.getJourneyTitle();
        }

        // Try first place name from place wise content
        if (post.getPlaceWiseContent() != null && !post.getPlaceWiseContent().isEmpty()) {
            String placeName = post.getPlaceWiseContent().get(0).getPlaceName();
            if (placeName != null && !placeName.trim().isEmpty()) {
                return placeName;
            }
        }

        // Try places visited list
        if (post.getPlacesVisited() != null && !post.getPlacesVisited().isEmpty()) {
            return post.getPlacesVisited().get(0);
        }

        return "Unknown Destination";
    }

    /**
     * Helper method to extract first image from post details
     */
    private String extractFirstImageFromPost(GetPostResponse post) {
        // Try to get image from place wise content
        if (post.getPlaceWiseContent() != null && !post.getPlaceWiseContent().isEmpty()) {
            List<String> imageUrls = post.getPlaceWiseContent().get(0).getImageUrls();
            if (imageUrls != null && !imageUrls.isEmpty()) {
                return imageUrls.get(0);
            }
        }

        return null; // Return null if no image found
    }

    /**
     * Helper method to extract description from post location details
     */
    private String extractDescriptionFromPost(GetPostResponse post) {
        StringBuilder description = new StringBuilder();

        // Add place wise content details
        if (post.getPlaceWiseContent() != null && !post.getPlaceWiseContent().isEmpty()) {
            PlaceWiseContentDto firstPlace = post.getPlaceWiseContent().get(0);

            // Add address if available
            if (firstPlace.getAddress() != null && !firstPlace.getAddress().trim().isEmpty()) {
                description.append(firstPlace.getAddress());
            }

            // Add activities if available
            if (firstPlace.getActivities() != null && !firstPlace.getActivities().isEmpty()) {
                if (description.length() > 0) {
                    description.append(" - ");
                }
                description.append(String.join(", ", firstPlace.getActivities()));
            }

            // Add experiences if available
            if (firstPlace.getExperiences() != null && !firstPlace.getExperiences().isEmpty()) {
                if (description.length() > 0) {
                    description.append(" - ");
                }
                description.append(String.join(", ", firstPlace.getExperiences()));
            }
        }

        // Fallback to places visited if no detailed description
        if (description.length() == 0 && post.getPlacesVisited() != null && !post.getPlacesVisited().isEmpty()) {
            description.append(String.join(", ", post.getPlacesVisited()));
        }

        // Final fallback
        if (description.length() == 0) {
            description.append("Explore this amazing destination");
        }

        return description.toString();
    }
}