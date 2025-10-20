package org.example.socialmedia_services.repository.subscription;

import org.example.socialmedia_services.entity.subscription.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // Find active subscription by user ID
    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.status = 'ACTIVE' AND s.endDate > :currentDate ORDER BY s.endDate DESC")
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId, @Param("currentDate") LocalDateTime currentDate);

    // Find all subscriptions by user ID
    List<Subscription> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find all active subscriptions
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate > :currentDate")
    List<Subscription> findAllActiveSubscriptions(@Param("currentDate") LocalDateTime currentDate);

    // Find expired subscriptions that need to be updated
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate <= :currentDate")
    List<Subscription> findExpiredSubscriptions(@Param("currentDate") LocalDateTime currentDate);

    // Check if user has active subscription
    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.userId = :userId AND s.status = 'ACTIVE' AND s.endDate > :currentDate")
    boolean hasActiveSubscription(@Param("userId") Long userId, @Param("currentDate") LocalDateTime currentDate);
}
