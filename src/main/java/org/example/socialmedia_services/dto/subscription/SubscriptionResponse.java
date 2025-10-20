package org.example.socialmedia_services.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private Long subscriptionId;
    private Long userId;
    private String subscriptionPackageId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private String subscriptionType;
    private Boolean isPremium;
    private LocalDateTime createdAt;
}
