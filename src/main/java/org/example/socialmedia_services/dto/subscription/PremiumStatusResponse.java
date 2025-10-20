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
public class PremiumStatusResponse {
    private Long userId;
    private Boolean isPremium;
    private LocalDateTime subscriptionEndDate;
    private String subscriptionStatus;
}
