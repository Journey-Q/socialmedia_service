package org.example.socialmedia_services.services.subscription;

import lombok.extern.slf4j.Slf4j;
import org.example.socialmedia_services.dto.subscription.PremiumStatusResponse;
import org.example.socialmedia_services.dto.subscription.SubscriptionRequest;
import org.example.socialmedia_services.dto.subscription.SubscriptionResponse;
import org.example.socialmedia_services.entity.User;
import org.example.socialmedia_services.entity.subscription.Subscription;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.UserRepo;
import org.example.socialmedia_services.repository.subscription.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepo userRepo;

    /**
     * Add a new subscription for a user
     */
    @Transactional
    public SubscriptionResponse addSubscription(SubscriptionRequest request) {
        log.info("Adding subscription for userId: {}", request.getUserId());

        // Validate request
        if (request.getUserId() == null) {
            throw new BadRequestException("User ID is required");
        }

        if (request.getDurationMonths() == null || request.getDurationMonths() <= 0) {
            throw new BadRequestException("Valid duration in months is required");
        }

        // Check if user exists
        Optional<User> userOptional = userRepo.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User not found");
        }
        User user = userOptional.get();

        // Check if user already has an active subscription
        LocalDateTime now = LocalDateTime.now();
        Optional<Subscription> existingSubscription = subscriptionRepository.findActiveSubscriptionByUserId(
                request.getUserId(), now);

        if (existingSubscription.isPresent()) {
            throw new BadRequestException("User already has an active subscription");
        }

        // Calculate end date based on duration
        LocalDateTime endDate = now.plusMonths(request.getDurationMonths());

        // Create new subscription
        Subscription subscription = new Subscription();
        subscription.setUserId(request.getUserId());
        subscription.setSubscriptionPackageId(request.getSubscriptionPackageId());
        subscription.setStartDate(now);
        subscription.setEndDate(endDate);
        subscription.setStatus("ACTIVE");
        subscription.setSubscriptionType(
                request.getSubscriptionType() != null ? request.getSubscriptionType() : "PREMIUM"
        );

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // Update user's premium status
        user.setIsPremium(true);
        userRepo.save(user);

        log.info("Subscription created successfully: subscriptionId={}, userId={}, endDate={}",
                savedSubscription.getSubscriptionId(), request.getUserId(), endDate);

        return mapToResponse(savedSubscription, true);
    }

    /**
     * Get premium status for a user
     */
    public PremiumStatusResponse getPremiumStatus(Long userId) {
        log.info("Checking premium status for userId: {}", userId);

        // Check if user exists
        Optional<User> userOptional = userRepo.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User not found");
        }
        User user = userOptional.get();

        LocalDateTime now = LocalDateTime.now();
        Optional<Subscription> activeSubscription = subscriptionRepository.findActiveSubscriptionByUserId(userId, now);

        if (activeSubscription.isPresent()) {
            Subscription subscription = activeSubscription.get();

            // Double-check if subscription is truly active
            if (subscription.getEndDate().isAfter(now)) {
                // Ensure user's premium flag is set
                if (!Boolean.TRUE.equals(user.getIsPremium())) {
                    user.setIsPremium(true);
                    userRepo.save(user);
                }

                return PremiumStatusResponse.builder()
                        .userId(userId)
                        .isPremium(true)
                        .subscriptionEndDate(subscription.getEndDate())
                        .subscriptionStatus(subscription.getStatus())
                        .build();
            } else {
                // Subscription has expired, update it
                subscription.setStatus("EXPIRED");
                subscriptionRepository.save(subscription);

                // Update user's premium status
                user.setIsPremium(false);
                userRepo.save(user);
            }
        } else {
            // No active subscription, ensure user's premium flag is false
            if (Boolean.TRUE.equals(user.getIsPremium())) {
                user.setIsPremium(false);
                userRepo.save(user);
            }
        }

        return PremiumStatusResponse.builder()
                .userId(userId)
                .isPremium(false)
                .subscriptionEndDate(null)
                .subscriptionStatus("NONE")
                .build();
    }

    /**
     * Get all subscriptions for a user
     */
    public List<SubscriptionResponse> getUserSubscriptions(Long userId) {
        log.info("Getting all subscriptions for userId: {}", userId);

        // Check if user exists
        Optional<User> userOptional = userRepo.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User not found");
        }
        User user = userOptional.get();

        List<Subscription> subscriptions = subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return subscriptions.stream()
                .map(subscription -> mapToResponse(subscription, Boolean.TRUE.equals(user.getIsPremium())))
                .collect(Collectors.toList());
    }

    /**
     * Cancel a subscription
     */
    @Transactional
    public SubscriptionResponse cancelSubscription(Long subscriptionId, Long userId) {
        log.info("Cancelling subscription: subscriptionId={}, userId={}", subscriptionId, userId);

        Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(subscriptionId);
        if (subscriptionOptional.isEmpty()) {
            throw new BadRequestException("Subscription not found");
        }

        Subscription subscription = subscriptionOptional.get();

        // Verify the subscription belongs to the user
        if (!subscription.getUserId().equals(userId)) {
            throw new BadRequestException("You are not authorized to cancel this subscription");
        }

        // Update subscription status
        subscription.setStatus("CANCELLED");
        subscription.setUpdatedAt(LocalDateTime.now());
        Subscription updatedSubscription = subscriptionRepository.save(subscription);

        // Update user's premium status
        Optional<User> userOptional = userRepo.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setIsPremium(false);
            userRepo.save(user);
        }

        log.info("Subscription cancelled successfully: subscriptionId={}", subscriptionId);

        return mapToResponse(updatedSubscription, false);
    }

    /**
     * Update expired subscriptions (can be called by a scheduled job)
     */
    @Transactional
    public void updateExpiredSubscriptions() {
        log.info("Checking for expired subscriptions");

        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expiredSubscriptions = subscriptionRepository.findExpiredSubscriptions(now);

        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus("EXPIRED");
            subscription.setUpdatedAt(now);
            subscriptionRepository.save(subscription);

            // Update user's premium status
            Optional<User> userOptional = userRepo.findById(subscription.getUserId());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setIsPremium(false);
                userRepo.save(user);
                log.info("Expired subscription updated: subscriptionId={}, userId={}",
                        subscription.getSubscriptionId(), subscription.getUserId());
            }
        }

        log.info("Updated {} expired subscriptions", expiredSubscriptions.size());
    }

    /**
     * Map Subscription entity to SubscriptionResponse DTO
     */
    private SubscriptionResponse mapToResponse(Subscription subscription, Boolean isPremium) {
        return SubscriptionResponse.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .userId(subscription.getUserId())
                .subscriptionPackageId(subscription.getSubscriptionPackageId())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus())
                .subscriptionType(subscription.getSubscriptionType())
                .isPremium(isPremium)
                .createdAt(subscription.getCreatedAt())
                .build();
    }
}
