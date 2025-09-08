package org.example.socialmedia_services.dto.jointTrip;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateJointTripRequest {

    // Frontend sends 'title' -> maps to tripTitle
    @NotBlank(message = "Trip title is required")
    @Size(max = 200, message = "Trip title cannot exceed 200 characters")
    private String title;

    // Frontend sends 'destination' -> maps to tripDestination
    @NotBlank(message = "Destination is required")
    @Size(max = 100, message = "Destination cannot exceed 100 characters")
    private String destination;

    // Frontend sends 'description' -> maps to tripDescription
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    // Frontend sends date strings like "7/8/2025" - we'll parse these
    @NotBlank(message = "Start date is required")
    private String startDate;

    @NotBlank(message = "End date is required")
    private String endDate;

    // Frontend sends 'tripType'
    @NotBlank(message = "Trip type is required")
    @Size(max = 50, message = "Trip type cannot exceed 50 characters")
    private String tripType;

    // Frontend sends 'duration' as string like "3 days"
    private String duration;

    // Frontend sends 'dayByDayItinerary' array
    @Valid
    private List<DayItineraryRequest> dayByDayItinerary;

    // Inner class matching frontend structure exactly
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class DayItineraryRequest {

        // Frontend sends 'day' (not dayNo)
        @NotNull(message = "Day number is required")
        @Min(value = 1, message = "Day number must be at least 1")
        private Integer day;

        // Frontend sends 'places' array
        private List<String> places;

        // Frontend sends 'accommodations' array
        private List<String> accommodations;

        // Frontend sends 'restaurants' array
        private List<String> restaurants;

        // Frontend sends 'notes' (not otherDetails)
        @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        private String notes;
    }

    // Helper methods to convert to internal format
    public String getTripTitle() {
        return this.title;
    }

    public String getTripDestination() {
        return this.destination;
    }

    public String getTripDescription() {
        return this.description;
    }

    public LocalDateTime getTripStartDate() {
        return parseDateString(this.startDate);
    }

    public LocalDateTime getTripEndDate() {
        return parseDateString(this.endDate);
    }

    // Parse frontend date format "7/8/2025" to LocalDateTime
    private LocalDateTime parseDateString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            String[] parts = dateStr.trim().split("/");
            if (parts.length == 3) {
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                return LocalDateTime.of(year, month, day, 9, 0); // Default to 9 AM
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Expected DD/MM/YYYY, got: " + dateStr);
        }

        throw new IllegalArgumentException("Invalid date format. Expected DD/MM/YYYY, got: " + dateStr);
    }
}