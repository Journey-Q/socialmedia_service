package org.example.socialmedia_services.dto.jointTrip;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TripJoinRequestResponse {

    private Long requestId;
    private Long tripId;
    private String tripTitle;
    private String tripDestination;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private String requestStatus; // PENDING, ACCEPTED, REJECTED, CANCELLED
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}