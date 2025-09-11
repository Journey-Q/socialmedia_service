package org.example.socialmedia_services.services;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmedia_services.dto.profile.ProfileResponseDTO;
import org.example.socialmedia_services.dto.profile.ProfileSetupdtoRequest;
import org.example.socialmedia_services.entity.UserProfile;
import org.example.socialmedia_services.entity.follow.UserStats;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.UserProfileRepository;
import org.example.socialmedia_services.repository.follow.UserStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserStatsRepository userStatsRepository;

    @Transactional
    public boolean completeUserSetup(ProfileSetupdtoRequest setupDTO) {

        log.info("Completing user setup for user: {}", setupDTO.getUserId());

        // Check if display name is already taken by another user
        Optional<UserProfile> existingProfile = userProfileRepository.findByDisplayName(setupDTO.getDisplayName());
        if (existingProfile.isPresent() && !existingProfile.get().getUserId().equals(setupDTO.getUserId())) {
            throw new BadRequestException("Display name '" + setupDTO.getDisplayName() + "' is already taken");
        }

        // Get existing profile or create new one
        UserProfile userProfile = userProfileRepository.findActiveByUserId(setupDTO.getUserId())
                .orElse(UserProfile.builder()
                        .userId(setupDTO.getUserId())
                        .setupCompleted(false)
                        .isActive(true)
                        .build());

        // Update profile with setup data
        userProfile.setDisplayName(setupDTO.getDisplayName());
        userProfile.setBio(setupDTO.getBio());
        userProfile.setProfileImageUrl(setupDTO.getProfileImageUrl());

        // Clear existing lists and add new ones
        userProfile.getFavouriteActivities().clear();
        if (setupDTO.getFavouriteActivities() != null) {
            userProfile.getFavouriteActivities().addAll(setupDTO.getFavouriteActivities());
        }

        userProfile.getPreferredTripMoods().clear();
        if (setupDTO.getPreferredTripMoods() != null) {
            userProfile.getPreferredTripMoods().addAll(setupDTO.getPreferredTripMoods());
        }

        // Mark setup as complete
        userProfile.markSetupComplete();

        // Save to database
        UserProfile savedProfile = userProfileRepository.save(userProfile);
        UserStats stats = new UserStats(setupDTO.getUserId());
        UserStats saved_stats = userStatsRepository.save(stats);

        log.info("User setup completed successfully for user: {}", setupDTO.getUserId());

        // Build and return response
        return true;
    }

    @Transactional(readOnly = true)
    public ProfileResponseDTO getUserProfile(String userId) {
        UserProfile userProfile = userProfileRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Profile not found for user: " + userId));

        return ProfileResponseDTO.builder()
                .userId(userProfile.getUserId())
                .displayName(userProfile.getDisplayName())
                .bio(userProfile.getBio())
                .favouriteActivities(userProfile.getFavouriteActivities())
                .preferredTripMoods(userProfile.getPreferredTripMoods())
                .profileImageUrl(userProfile.getProfileImageUrl())
                .isPremium(userProfile.getIsPremium())
                .isTripFluence(userProfile.getIsTripFluence())
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserProfile> findSimilarUsers(String userId) {
        // Find users with similar activities
        List<UserProfile> similarByActivities = userProfileRepository.findSimilarUsersByActivities(userId);

        // Find users with similar trip moods
        List<UserProfile> similarByMoods = userProfileRepository.findSimilarUsersByTripMoods(userId);

        // You can implement logic to combine and rank these results
        return similarByActivities;
    }

    @Transactional(readOnly = true)
    public boolean isDisplayNameAvailable(String displayName, String excludeUserId) {
        Optional<UserProfile> existingProfile = userProfileRepository.findByDisplayName(displayName);
        return existingProfile.isEmpty() || existingProfile.get().getUserId().equals(excludeUserId);
    }




    @Transactional
    public void deactivateProfile(String userId) {
        int updatedRows = userProfileRepository.deactivateProfile(userId);
        if (updatedRows == 0) {
            throw new BadRequestException("Profile not found for user: " + userId);
        }
        log.info("Profile deactivated for user: {}", userId);
    }

    @Transactional
    public Boolean updateProfile(ProfileSetupdtoRequest setupDTO) {
        try {
            Optional<UserProfile> optionalUserProfile = userProfileRepository.findActiveByUserId(setupDTO.getUserId());

            if (optionalUserProfile.isEmpty()) {
                return false;
            }

            UserProfile userProfile = optionalUserProfile.get();

            // Update the existing profile with new data from setupDTO
            userProfile.setDisplayName(setupDTO.getDisplayName());
            userProfile.setBio(setupDTO.getBio());

            // Clear existing lists and add new ones (following the pattern from completeUserSetup)
            userProfile.getFavouriteActivities().clear();
            if (setupDTO.getFavouriteActivities() != null) {
                userProfile.getFavouriteActivities().addAll(setupDTO.getFavouriteActivities());
            }

            userProfile.getPreferredTripMoods().clear();
            if (setupDTO.getPreferredTripMoods() != null) {
                userProfile.getPreferredTripMoods().addAll(setupDTO.getPreferredTripMoods());
            }

            userProfile.setProfileImageUrl(setupDTO.getProfileImageUrl());

            // Save the updated profile
            userProfileRepository.save(userProfile);

            return true;
        } catch (Exception e) {
            log.error("Error updating profile for user: {}", setupDTO.getUserId(), e);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<UserProfile> searchProfiles(String searchTerm) {
        return userProfileRepository.searchProfiles(searchTerm);
    }

    @Transactional(readOnly = true)
    public List<UserProfile> findByActivities(List<String> activities) {
        return userProfileRepository.findByFavouriteActivities(activities);
    }

    @Transactional(readOnly = true)
    public List<UserProfile> findByTripMoods(List<String> moods) {
        return userProfileRepository.findByPreferredTripMoods(moods);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getPopularActivities() {
        return userProfileRepository.getMostPopularActivities();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getPopularTripMoods() {
        return userProfileRepository.getMostPopularTripMoods();
    }
}
