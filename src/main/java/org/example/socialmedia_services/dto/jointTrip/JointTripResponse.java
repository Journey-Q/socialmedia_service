package org.example.socialmedia_services.dto.jointTrip;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JointTripResponse {

    private Long tripId;
    private Long userId;
    private Long groupId;

    // Frontend expects these field names
    private String title;
    private String destination;
    private String description;
    private String startDate; // Frontend expects date as string "7/8/2025"
    private String endDate;   // Frontend expects date as string "9/8/2025"
    private String duration;  // Frontend expects "3 days"
    private String tripType;

    private Boolean isActive;
    private Boolean isGroupFormed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Frontend expects 'dayByDayItinerary' (not dayByDayItineraries)
    private List<DayItineraryResponse> dayByDayItinerary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class DayItineraryResponse {
        private Long itineraryId;

        // Frontend expects 'day' (not dayNo)
        private Integer day;

        // Frontend expects 'places' (not visitingPlaces)
        private List<String> places;

        private List<String> accommodations;
        private List<String> restaurants;

        // Frontend expects 'notes' (not otherDetails)
        private String notes;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}