package org.example.socialmedia_services.services.post;

import org.example.socialmedia_services.dto.post.GetPostResponse;
import org.example.socialmedia_services.dto.post.PlaceWiseContentDto;
import org.example.socialmedia_services.dto.post.PostContentRequest;
import org.example.socialmedia_services.entity.User;
import org.example.socialmedia_services.entity.post.PlaceWiseContent;
import org.example.socialmedia_services.entity.post.Post;
import org.example.socialmedia_services.entity.post.PostContent;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.UserRepo;
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

            // Create post content with the saved post's ID
            PostContent postContent = new PostContent();
            postContent.setPost(savedPost);
            postContent.setPostId(savedPost.getPostId()); // Set the same ID
            postContent.setJourneyTitle(request.getJourneyTitle());
            postContent.setNumberOfDays(request.getNumberOfDays());
            postContent.setPlacesVisited(request.getPlacesVisited());
            postContent.setBudgetInfo(request.getBudgetInfo());
            postContent.setTravelTips(request.getTravelTips());
            postContent.setTransportationOptions(request.getTransportationOptions());
            postContent.setHotelRecommendations(request.getHotelRecommendations());
            postContent.setRestaurantRecommendations(request.getRestaurantRecommendations());

            // Create place wise content list for cascade save
            List<PlaceWiseContent> placeWiseContentList = new ArrayList<>();

            if (request.getPlaceWiseContent() != null && !request.getPlaceWiseContent().isEmpty()) {
                int sequenceOrder = 1;
                for (PlaceWiseContentDto placeDto : request.getPlaceWiseContent()) {
                    PlaceWiseContent placeContent = new PlaceWiseContent();
                    placeContent.setPostId(savedPost.getPostId()); // Set post_id to reference PostContent
                    placeContent.setPlaceName(placeDto.getPlaceName());
                    placeContent.setLatitude(placeDto.getLatitude());
                    placeContent.setLongitude(placeDto.getLongitude());
                    placeContent.setAddress(placeDto.getAddress());
                    placeContent.setTripMood(placeDto.getTripMood());
                    placeContent.setActivities(placeDto.getActivities());
                    placeContent.setExperiences(placeDto.getExperiences());
                    placeContent.setImageUrls(placeDto.getImageUrls());
                    placeContent.setSequenceOrder(sequenceOrder++);

                    placeWiseContentList.add(placeContent);
                }
            }

            // Set the place wise content list to PostContent for cascade save
            postContent.setPlaceWiseContentList(placeWiseContentList);

            // Save PostContent (will cascade save PlaceWiseContent)
            PostContent savedPostContent = postContentRepository.save(postContent);

            // Update the post reference
            savedPost.setPostContent(savedPostContent);

            // Convert place wise content to DTOs for response
            List<PlaceWiseContentDto> placeWiseContentDtos = placeWiseContentList.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

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

    public GetPostResponse getPostById(Long postId) {
        try {
            // Find the post
            Optional<Post> postOptional = postRepository.findById(postId);
            if (postOptional.isEmpty()) {
                throw new BadRequestException("Post not found");
            }

            Post post = postOptional.get();

            // Find the user who created the post
            Optional<User> userOptional = userRepository.findById(post.getCreatedById());
            if (userOptional.isEmpty()) {
                throw new BadRequestException("Post creator not found");
            }
            User user = userOptional.get();

            // Find post content
            Optional<PostContent> contentOptional = postContentRepository.findByPostId(postId);
            if (contentOptional.isEmpty()) {
                throw new BadRequestException("Post content not found");
            }
            PostContent postContent = contentOptional.get();

            // Get place wise content using the relationship from PostContent
            List<PlaceWiseContent> placeWiseContentList = postContent.getPlaceWiseContentList();
            List<PlaceWiseContentDto> placeWiseContentDtos = placeWiseContentList != null ?
                    placeWiseContentList.stream()
                            .map(this::convertToDto)
                            .collect(Collectors.toList()) : new ArrayList<>();

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