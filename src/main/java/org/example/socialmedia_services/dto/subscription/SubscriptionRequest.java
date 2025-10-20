package org.example.socialmedia_services.dto.subscription;

import lombok.Data;

@Data
public class SubscriptionRequest {
    private Long userId;
    private String subscriptionPackageId; // Subscription package ID from the request
    private Integer durationMonths; // Subscription duration in months (e.g., 1, 3, 6, 12)
    private String subscriptionType; // PREMIUM, BASIC, etc. (optional, defaults to PREMIUM)
}
