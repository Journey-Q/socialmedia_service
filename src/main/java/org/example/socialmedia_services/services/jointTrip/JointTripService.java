package org.example.socialmedia_services.services.jointTrip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmedia_services.dto.jointTrip.CreateJointTripRequest;
import org.example.socialmedia_services.dto.jointTrip.JointTripResponse;
import org.example.socialmedia_services.entity.jointTrip.JointTrip;
import org.example.socialmedia_services.entity.jointTrip.TripDayByDayItinerary;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.jointTrip.JointTripRepository;
import org.example.socialmedia_services.repository.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JointTripService {

    private final JointTripRepository jointTripRepository;
    private final UserRepo userRepo;

    @Transactional
    public JointTripResponse createJointTrip(CreateJointTripRequest request, Long userId) {
        log.info("Creating joint trip for user: {}", userId);

        // Validate user exists
        if (!userRepo.existsById(userId)) {
            throw new BadRequestException("User not found with ID: " + userId);
        }

        // Get dates from frontend format
        LocalDateTime startDate = request.getTripStartDate();
        LocalDateTime endDate = request.getTripEndDate();

        // Validate dates
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("End date must be after start date");
        }

        if (startDate.isBefore(LocalDateTime.now().minusDays(1))) {
            throw new BadRequestException("Start date cannot be in the past");
        }

        // Calculate trip duration
        int duration = (int) ChronoUnit.DAYS.between(
                startDate.toLocalDate(),
                endDate.toLocalDate()
        ) + 1; // +1 to include both start and end dates

        // Create joint trip entity using frontend field names
        JointTrip jointTrip = JointTrip.builder()
                .userId(userId)
                .tripTitle(request.getTripTitle())
                .tripDestination(request.getTripDestination())
                .tripDescription(request.getTripDescription())
                .tripStartDate(startDate)
                .tripEndDate(endDate)
                .tripDuration(duration)
                .tripType(request.getTripType())
                .isActive(true)
                .isGroupFormed(false)
                .build();

        // Save the trip first to get the ID
        JointTrip savedTrip = jointTripRepository.save(jointTrip);

        // Create day-by-day itineraries if provided
        if (request.getDayByDayItinerary() != null && !request.getDayByDayItinerary().isEmpty()) {
            final JointTrip finalSavedTrip = savedTrip; // Make it effectively final for lambda
            List<TripDayByDayItinerary> itineraries = request.getDayByDayItinerary().stream()
                    .map(itineraryRequest -> TripDayByDayItinerary.builder()
                            .jointTrip(finalSavedTrip) // Use final reference
                            .dayNo(itineraryRequest.getDay()) // Frontend sends 'day'
                            .visitingPlaces(itineraryRequest.getPlaces() != null ?
                                    itineraryRequest.getPlaces() : List.of())
                            .accommodations(itineraryRequest.getAccommodations() != null ?
                                    itineraryRequest.getAccommodations() : List.of())
                            .restaurants(itineraryRequest.getRestaurants() != null ?
                                    itineraryRequest.getRestaurants() : List.of())
                            .otherDetails(itineraryRequest.getNotes()) // Frontend sends 'notes'
                            .isActive(true)
                            .build())
                    .collect(Collectors.toList());

            savedTrip.setDayByDayItineraries(itineraries);
            savedTrip = jointTripRepository.save(savedTrip);
        }

        log.info("Joint trip created successfully with ID: {}", savedTrip.getTripId());
        return mapToResponse(savedTrip);
    }

    @Transactional(readOnly = true)
    public JointTripResponse getTripById(Long tripId) {
        JointTrip trip = jointTripRepository.findActiveByTripId(tripId)
                .orElseThrow(() -> new BadRequestException("Trip not found with ID: " + tripId));

        return mapToResponse(trip);
    }

    @Transactional(readOnly = true)
    public List<JointTripResponse> getUserTrips(Long userId) {
        List<JointTrip> trips = jointTripRepository.findActiveByUserId(userId);
        return trips.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JointTripResponse> searchTrips(String searchTerm) {
        List<JointTrip> trips = jointTripRepository.searchTrips(searchTerm);
        return trips.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JointTripResponse> getAvailableTrips() {
        List<JointTrip> trips = jointTripRepository.findAvailableTripsForJoining(LocalDateTime.now());
        return trips.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JointTripResponse> getTripsByDestination(String destination) {
        List<JointTrip> trips = jointTripRepository.findByDestination(destination);
        return trips.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JointTripResponse> getTripsByType(String tripType) {
        List<JointTrip> trips = jointTripRepository.findByTripType(tripType);
        return trips.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public JointTripResponse updateTrip(Long tripId, CreateJointTripRequest request, Long userId) {
        JointTrip trip = jointTripRepository.findActiveByTripId(tripId)
                .orElseThrow(() -> new BadRequestException("Trip not found with ID: " + tripId));

        // Check if user is the creator
        if (!trip.getUserId().equals(userId)) {
            throw new BadRequestException("You can only update trips that you created");
        }

        // Get dates from frontend format
        LocalDateTime startDate = request.getTripStartDate();
        LocalDateTime endDate = request.getTripEndDate();

        // Validate dates
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("End date must be after start date");
        }

        // Calculate new duration
        int duration = (int) ChronoUnit.DAYS.between(
                startDate.toLocalDate(),
                endDate.toLocalDate()
        ) + 1;

        // Update trip details
        trip.setTripTitle(request.getTripTitle());
        trip.setTripDestination(request.getTripDestination());
        trip.setTripDescription(request.getTripDescription());
        trip.setTripStartDate(startDate);
        trip.setTripEndDate(endDate);
        trip.setTripDuration(duration);
        trip.setTripType(request.getTripType());

        // FIXED: Handle itineraries in a single transaction without multiple saves
        if (request.getDayByDayItinerary() != null) {
            // Clear existing itineraries (Hibernate will handle cascading)
            trip.getDayByDayItineraries().clear();

            // Create new itineraries and add them to the trip
            for (CreateJointTripRequest.DayItineraryRequest itineraryRequest : request.getDayByDayItinerary()) {
                TripDayByDayItinerary newItinerary = TripDayByDayItinerary.builder()
                        .jointTrip(trip)
                        .dayNo(itineraryRequest.getDay())
                        .visitingPlaces(itineraryRequest.getPlaces() != null ?
                                new ArrayList<>(itineraryRequest.getPlaces()) : new ArrayList<>())
                        .accommodations(itineraryRequest.getAccommodations() != null ?
                                new ArrayList<>(itineraryRequest.getAccommodations()) : new ArrayList<>())
                        .restaurants(itineraryRequest.getRestaurants() != null ?
                                new ArrayList<>(itineraryRequest.getRestaurants()) : new ArrayList<>())
                        .otherDetails(itineraryRequest.getNotes())
                        .isActive(true)
                        .build();

                // Add to trip's itinerary list
                trip.getDayByDayItineraries().add(newItinerary);
            }
        }

        // Single save operation at the end
        JointTrip updatedTrip = jointTripRepository.save(trip);
        log.info("Trip updated successfully: {}", tripId);
        return mapToResponse(updatedTrip);
    }

    @Transactional
    public void deleteTrip(Long tripId, Long userId) {
        JointTrip trip = jointTripRepository.findActiveByTripId(tripId)
                .orElseThrow(() -> new BadRequestException("Trip not found with ID: " + tripId));

        // Check if user is the creator
        if (!trip.getUserId().equals(userId)) {
            throw new BadRequestException("You can only delete trips that you created");
        }

        trip.deactivateTrip();
        jointTripRepository.save(trip);
        log.info("Trip deleted successfully: {}", tripId);
    }

    // Helper method to convert entity to response DTO (matching frontend expectations)
    private JointTripResponse mapToResponse(JointTrip trip) {
        List<JointTripResponse.DayItineraryResponse> itineraryResponses = trip.getDayByDayItineraries()
                .stream()
                .map(itinerary -> JointTripResponse.DayItineraryResponse.builder()
                        .itineraryId(itinerary.getItineraryId())
                        .day(itinerary.getDayNo()) // Map dayNo to 'day' for frontend
                        .places(itinerary.getVisitingPlaces()) // Map visitingPlaces to 'places'
                        .accommodations(itinerary.getAccommodations())
                        .restaurants(itinerary.getRestaurants())
                        .notes(itinerary.getOtherDetails()) // Map otherDetails to 'notes'
                        .createdAt(itinerary.getCreatedAt())
                        .updatedAt(itinerary.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return JointTripResponse.builder()
                .tripId(trip.getTripId())
                .userId(trip.getUserId())
                .groupId(trip.getGroupId())
                .title(trip.getTripTitle()) // Frontend expects 'title'
                .destination(trip.getTripDestination()) // Frontend expects 'destination'
                .description(trip.getTripDescription()) // Frontend expects 'description'
                .startDate(formatDateForFrontend(trip.getTripStartDate())) // Format for frontend
                .endDate(formatDateForFrontend(trip.getTripEndDate())) // Format for frontend
                .duration(trip.getTripDuration() + " " + (trip.getTripDuration() == 1 ? "day" : "days"))
                .tripType(trip.getTripType())
                .isActive(trip.getIsActive())
                .isGroupFormed(trip.getIsGroupFormed())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .dayByDayItinerary(itineraryResponses) // Frontend expects 'dayByDayItinerary'
                .build();
    }

    // Format date for frontend "7/8/2025" format
    private String formatDateForFrontend(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.getDayOfMonth() + "/" +
                dateTime.getMonthValue() + "/" +
                dateTime.getYear();
    }
}