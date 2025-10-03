package org.example.socialmedia_services.services.post;

import org.example.socialmedia_services.dto.post.GetPostResponse;
import org.example.socialmedia_services.dto.post.GetPostUserResponse;
import org.example.socialmedia_services.dto.post.PlaceWiseContentDto;
import org.example.socialmedia_services.dto.post.PostContentRequest;
import org.example.socialmedia_services.entity.User;
import org.example.socialmedia_services.entity.post.PlaceWiseContent;
import org.example.socialmedia_services.entity.post.Post;
import org.example.socialmedia_services.entity.post.PostContent;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.UserRepo;
import org.example.socialmedia_services.repository.follow.UserStatsRepository;
import org.example.socialmedia_services.repository.post.PlaceWiseContentRepository;
import org.example.socialmedia_services.repository.post.PostContentRepository;
import org.example.socialmedia_services.repository.post.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostContentRepository postContentRepository;

    @Autowired
    private PlaceWiseContentRepository placeWiseContentRepository;

    @Autowired
    private UserStatsRepository userStatsRepo;

    @Autowired
    private UserRepo userRepository;

    @Transactional
    public GetPostResponse createPost(PostContentRequest request, Long userId) {
        try {
            // Verify user exists
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                throw new BadRequestException("User not found");
            }
            User user = userOptional.get();

            // Create the post first
            Post post = new Post();
            post.setCreatedById(userId);
            Post savedPost = postRepository.save(post);
            int res = userStatsRepo.incrementPosts(String.valueOf(userId));

            // Create post content with the saved post's ID
            PostContent postContent = new PostContent();
            postContent.setPostId(savedPost.getPostId()); // Set the post_id directly
            postContent.setJourneyTitle(request.getJourneyTitle());
            postContent.setNumberOfDays(request.getNumberOfDays());
            postContent.setPlacesVisited(request.getPlacesVisited());
            postContent.setBudgetInfo(request.getBudgetInfo());
            postContent.setTravelTips(request.getTravelTips());
            postContent.setTransportationOptions(request.getTransportationOptions());
            postContent.setHotelRecommendations(request.getHotelRecommendations());
            postContent.setRestaurantRecommendations(request.getRestaurantRecommendations());

            // Save PostContent
            PostContent savedPostContent = postContentRepository.save(postContent);

            // Create and save place wise content entries
            List<PlaceWiseContentDto> placeWiseContentDtos = new ArrayList<>();

            if (request.getPlaceWiseContent() != null && !request.getPlaceWiseContent().isEmpty()) {
                int sequenceOrder = 1;
                for (PlaceWiseContentDto placeDto : request.getPlaceWiseContent()) {
                    PlaceWiseContent placeContent = new PlaceWiseContent();
                    placeContent.setPostId(savedPost.getPostId()); // Set post_id to reference Post
                    placeContent.setPlaceName(placeDto.getPlaceName());
                    placeContent.setLatitude(placeDto.getLatitude());
                    placeContent.setLongitude(placeDto.getLongitude());
                    placeContent.setAddress(placeDto.getAddress());
                    placeContent.setTripMood(placeDto.getTripMood());
                    placeContent.setActivities(placeDto.getActivities());
                    placeContent.setExperiences(placeDto.getExperiences());
                    placeContent.setImageUrls(placeDto.getImageUrls());
                    placeContent.setSequenceOrder(sequenceOrder++);

                    // Save each place wise content separately
                    PlaceWiseContent savedPlaceContent = placeWiseContentRepository.save(placeContent);
                    placeWiseContentDtos.add(convertToDto(savedPlaceContent));
                }
            }

            // Update the post reference (bidirectional relationship)
            savedPost.setPostContent(savedPostContent);

            // Create and return response
            GetPostResponse response = new GetPostResponse();
            response.setPostId(savedPost.getPostId());
            response.setCreatedById(savedPost.getCreatedById());
            response.setCreatorUsername(user.getUsername());
            response.setCreatorProfileUrl(user.getProfileUrl());
            response.setCreatedAt(savedPost.getCreatedAt());
            response.setLikesCount(savedPost.getLikesCount());
            response.setCommentsCount(savedPost.getCommentsCount());

            // Set post content details
            response.setJourneyTitle(savedPostContent.getJourneyTitle());
            response.setNumberOfDays(savedPostContent.getNumberOfDays());
            response.setPlacesVisited(savedPostContent.getPlacesVisited());
            response.setPlaceWiseContent(placeWiseContentDtos);
            response.setBudgetInfo(savedPostContent.getBudgetInfo());
            response.setTravelTips(savedPostContent.getTravelTips());
            response.setTransportationOptions(savedPostContent.getTransportationOptions());
            response.setHotelRecommendations(savedPostContent.getHotelRecommendations());
            response.setRestaurantRecommendations(savedPostContent.getRestaurantRecommendations());

            return response;

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create post", e);
        }
    }

    @Transactional
    public boolean deletePost(Long postId, Long userId) {
        try {
            // Find the post
            Optional<Post> postOptional = postRepository.findById(postId);
            if (postOptional.isEmpty()) {
                throw new BadRequestException("Post not found");
            }

            Post post = postOptional.get();

            // Check if the user is the owner of the post
            if (!post.getCreatedById().equals(userId)) {
                throw new BadRequestException("You are not authorized to delete this post");
            }

            // Delete the post - CASCADE will handle related data automatically
            postRepository.delete(post);

            return true;

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete post", e);
        }
    }

    // Optimized method to get post with all related data
    @Transactional(readOnly = true)
    public GetPostResponse getPostById(Long postId) {
        try {
            // Fetch post with all content in optimized queries
            Optional<Post> postOptional = postRepository.findByIdWithAllContent(postId);
            if (postOptional.isEmpty()) {
                throw new BadRequestException("Post not found");
            }

            Post post = postOptional.get();
            PostContent postContent = post.getPostContent();

            if (postContent == null) {
                throw new BadRequestException("Post content not found");
            }

            // Find the user who created the post
            Optional<User> userOptional = userRepository.findById(post.getCreatedById());
            if (userOptional.isEmpty()) {
                throw new BadRequestException("Post creator not found");
            }
            User user = userOptional.get();

            // Convert place wise content to DTOs
            List<PlaceWiseContentDto> placeWiseContentDtos = new ArrayList<>();
            if (postContent.getPlaceWiseContentList() != null) {
                placeWiseContentDtos = postContent.getPlaceWiseContentList().stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
            }

            // Create response
            GetPostResponse response = new GetPostResponse();
            response.setPostId(post.getPostId());
            response.setCreatedById(post.getCreatedById());
            response.setCreatorUsername(user.getUsername());
            response.setCreatorProfileUrl(user.getProfileUrl());
            response.setCreatedAt(post.getCreatedAt());
            response.setLikesCount(post.getLikesCount());
            response.setCommentsCount(post.getCommentsCount());

            // Set post content details
            response.setJourneyTitle(postContent.getJourneyTitle());
            response.setNumberOfDays(postContent.getNumberOfDays());
            response.setPlacesVisited(postContent.getPlacesVisited());
            response.setPlaceWiseContent(placeWiseContentDtos);
            response.setBudgetInfo(postContent.getBudgetInfo());
            response.setTravelTips(postContent.getTravelTips());
            response.setTransportationOptions(postContent.getTransportationOptions());
            response.setHotelRecommendations(postContent.getHotelRecommendations());
            response.setRestaurantRecommendations(postContent.getRestaurantRecommendations());

            return response;

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve post", e);
        }
    }

    // New method to get posts by user with all related data
    @Transactional(readOnly = true)
    public List<GetPostUserResponse> getPostsByUser(Long userId) {
        try {
            // Verify user exists
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isEmpty()) {
                throw new BadRequestException("User not found");
            }
            User user = userOptional.get();

            // Fetch posts by user with all content
            List<Post> posts = postRepository.findByUserIdWithAllContent(userId);

            // Convert to simplified response DTOs
            List<GetPostUserResponse> responses = new ArrayList<>();
            for (Post post : posts) {
                PostContent postContent = post.getPostContent();

                // Create simplified response
                GetPostUserResponse response = new GetPostUserResponse();
                response.setId(String.valueOf(post.getPostId()));

                if (postContent != null) {
                    // Set journey title
                    response.setJourneyTitle(postContent.getJourneyTitle());

                    // Set location - use first place with address or create from places visited
                    String location = getLocationString(postContent);
                    response.setLocation(location);

                    // Set post images - get first place image as cover
                    List<String> postImages = getFirstPlaceImages(postContent);
                    response.setPostImages(postImages);
                } else {
                    // Fallback values if post content is null
                    response.setJourneyTitle("Untitled Journey");
                    response.setLocation("Unknown Location");
                    response.setPostImages(new ArrayList<>());
                }

                responses.add(response);
            }

            return responses;

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching user posts: " + e.getMessage(), e);
        }
    }

    // Method to get all posts for feed
    @Transactional(readOnly = true)
    public List<GetPostResponse> getAllPosts() {
        try {
            // Fetch all posts with content
            List<Post> posts = postRepository.findAllPostsWithContent();

            // Convert to response DTOs
            List<GetPostResponse> responses = new ArrayList<>();
            for (Post post : posts) {
                // Get user info for each post
                Optional<User> userOptional = userRepository.findById(post.getCreatedById());
                if (userOptional.isEmpty()) {
                    continue; // Skip posts with missing users
                }
                User user = userOptional.get();

                PostContent postContent = post.getPostContent();

                // Convert place wise content to DTOs
                List<PlaceWiseContentDto> placeWiseContentDtos = new ArrayList<>();
                if (postContent != null && postContent.getPlaceWiseContentList() != null) {
                    placeWiseContentDtos = postContent.getPlaceWiseContentList().stream()
                            .map(this::convertToDto)
                            .collect(Collectors.toList());
                }

                // Create response
                GetPostResponse response = new GetPostResponse();
                response.setPostId(post.getPostId());
                response.setCreatedById(post.getCreatedById());
                response.setCreatorUsername(user.getUsername());
                response.setCreatorProfileUrl(user.getProfileUrl());
                response.setCreatedAt(post.getCreatedAt());
                response.setLikesCount(post.getLikesCount());
                response.setCommentsCount(post.getCommentsCount());

                // Set post content details if available
                if (postContent != null) {
                    response.setJourneyTitle(postContent.getJourneyTitle());
                    response.setNumberOfDays(postContent.getNumberOfDays());
                    response.setPlacesVisited(postContent.getPlacesVisited());
                    response.setPlaceWiseContent(placeWiseContentDtos);
                    response.setBudgetInfo(postContent.getBudgetInfo());
                    response.setTravelTips(postContent.getTravelTips());
                    response.setTransportationOptions(postContent.getTransportationOptions());
                    response.setHotelRecommendations(postContent.getHotelRecommendations());
                    response.setRestaurantRecommendations(postContent.getRestaurantRecommendations());
                }

                responses.add(response);
            }

            return responses;

        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve all posts", e);
        }
    }

    // Helper method to get location string for simplified response
    private String getLocationString(PostContent postContent) {
        // Try to get location from first place with address
        if (postContent.getPlaceWiseContentList() != null && !postContent.getPlaceWiseContentList().isEmpty()) {
            PlaceWiseContent firstPlace = postContent.getPlaceWiseContentList().get(0);
            if (firstPlace.getAddress() != null && !firstPlace.getAddress().trim().isEmpty()) {
                return firstPlace.getAddress();
            }
            if (firstPlace.getPlaceName() != null && !firstPlace.getPlaceName().trim().isEmpty()) {
                return firstPlace.getPlaceName();
            }
        }

        // Fallback to places visited list
        if (postContent.getPlacesVisited() != null && !postContent.getPlacesVisited().isEmpty()) {
            return String.join(", ", postContent.getPlacesVisited());
        }

        return "Unknown Location";
    }

    // Helper method to get first place images for simplified response
    private List<String> getFirstPlaceImages(PostContent postContent) {
        List<String> postImages = new ArrayList<>();

        // Get images from first place
        if (postContent.getPlaceWiseContentList() != null && !postContent.getPlaceWiseContentList().isEmpty()) {
            PlaceWiseContent firstPlace = postContent.getPlaceWiseContentList().get(0);
            if (firstPlace.getImageUrls() != null && !firstPlace.getImageUrls().isEmpty()) {
                // Add the first image as cover
                postImages.add(firstPlace.getImageUrls().get(0));
            }
        }

        return postImages;
    }

    // Helper method to convert PlaceWiseContent entity to DTO
    private PlaceWiseContentDto convertToDto(PlaceWiseContent entity) {
        PlaceWiseContentDto dto = new PlaceWiseContentDto();
        dto.setPlaceName(entity.getPlaceName());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setAddress(entity.getAddress());
        dto.setTripMood(entity.getTripMood());
        dto.setActivities(entity.getActivities());
        dto.setExperiences(entity.getExperiences());
        dto.setImageUrls(entity.getImageUrls());
        dto.setSequenceOrder(entity.getSequenceOrder());
        return dto;
    }
}