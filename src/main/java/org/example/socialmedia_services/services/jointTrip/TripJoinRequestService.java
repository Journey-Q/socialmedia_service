package org.example.socialmedia_services.services.jointTrip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmedia_services.dto.jointTrip.SendTripRequestDto;
import org.example.socialmedia_services.dto.jointTrip.TripJoinRequestResponse;
import org.example.socialmedia_services.entity.User;
import org.example.socialmedia_services.entity.jointTrip.JointTrip;
import org.example.socialmedia_services.entity.jointTrip.TripJoinRequest;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.UserRepo;
import org.example.socialmedia_services.repository.jointTrip.JointTripRepository;
import org.example.socialmedia_services.repository.jointTrip.TripJoinRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripJoinRequestService {

    private final TripJoinRequestRepository tripJoinRequestRepository;
    private final JointTripRepository jointTripRepository;
    private final UserRepo userRepo;

    @Transactional
    public List<TripJoinRequestResponse> sendTripRequests(SendTripRequestDto request, Long senderId) {
        log.info("Sending trip requests for trip: {} from user: {}", request.getTripId(), senderId);

        // Validate trip exists and is active
        JointTrip trip = jointTripRepository.findActiveByTripId(request.getTripId())
                .orElseThrow(() -> new BadRequestException("Trip not found with ID: " + request.getTripId()));

        // Validate sender is the trip creator
        if (!trip.getUserId().equals(senderId)) {
            throw new BadRequestException("Only the trip creator can send requests");
        }

        // Validate sender exists
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new BadRequestException("Sender not found with ID: " + senderId));

        List<TripJoinRequestResponse> responses = new ArrayList<>();

        // Send request to each receiver
        for (Long receiverId : request.getReceiverIds()) {
            // Validate receiver exists
            if (!userRepo.existsById(receiverId)) {
                log.warn("Receiver not found with ID: {}, skipping", receiverId);
                continue;
            }

            // Check if sender is trying to send to themselves
            if (receiverId.equals(senderId)) {
                log.warn("Cannot send request to yourself, skipping receiver: {}", receiverId);
                continue;
            }

            // Check if request already exists
            if (tripJoinRequestRepository.findExistingRequest(trip.getTripId(), senderId, receiverId).isPresent()) {
                log.warn("Request already exists for trip: {} to receiver: {}, skipping", trip.getTripId(), receiverId);
                continue;
            }

            // Create new request
            TripJoinRequest joinRequest = TripJoinRequest.builder()
                    .tripId(trip.getTripId())
                    .senderId(senderId)
                    .receiverId(receiverId)
                    .requestStatus("PENDING")
                    .isActive(true)
                    .build();

            TripJoinRequest savedRequest = tripJoinRequestRepository.save(joinRequest);
            responses.add(mapToResponse(savedRequest, trip, sender, null));

            log.info("Trip request sent successfully: {}", savedRequest.getRequestId());
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<TripJoinRequestResponse> getReceivedRequests(Long userId) {
        log.info("Fetching received requests for user: {}", userId);

        List<TripJoinRequest> requests = tripJoinRequestRepository.findByReceiverId(userId);

        return requests.stream()
                .map(request -> {
                    JointTrip trip = jointTripRepository.findActiveByTripId(request.getTripId())
                            .orElse(null);
                    User sender = userRepo.findById(request.getSenderId()).orElse(null);
                    User receiver = userRepo.findById(request.getReceiverId()).orElse(null);

                    return mapToResponse(request, trip, sender, receiver);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TripJoinRequestResponse> getSentRequests(Long userId) {
        log.info("Fetching sent requests for user: {}", userId);

        List<TripJoinRequest> requests = tripJoinRequestRepository.findBySenderId(userId);

        return requests.stream()
                .map(request -> {
                    JointTrip trip = jointTripRepository.findActiveByTripId(request.getTripId())
                            .orElse(null);
                    User sender = userRepo.findById(request.getSenderId()).orElse(null);
                    User receiver = userRepo.findById(request.getReceiverId()).orElse(null);

                    return mapToResponse(request, trip, sender, receiver);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TripJoinRequestResponse> getPendingRequests(Long userId) {
        log.info("Fetching pending requests for user: {}", userId);

        List<TripJoinRequest> requests = tripJoinRequestRepository.findPendingRequestsByReceiverId(userId);

        return requests.stream()
                .map(request -> {
                    JointTrip trip = jointTripRepository.findActiveByTripId(request.getTripId())
                            .orElse(null);
                    User sender = userRepo.findById(request.getSenderId()).orElse(null);

                    return mapToResponse(request, trip, sender, null);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public TripJoinRequestResponse acceptRequest(Long requestId, Long userId) {
        log.info("Accepting request: {} by user: {}", requestId, userId);

        TripJoinRequest request = tripJoinRequestRepository.findActiveById(requestId)
                .orElseThrow(() -> new BadRequestException("Request not found with ID: " + requestId));

        // Validate user is the receiver
        if (!request.getReceiverId().equals(userId)) {
            throw new BadRequestException("Only the receiver can accept this request");
        }

        // Validate request is pending
        if (!"PENDING".equals(request.getRequestStatus())) {
            throw new BadRequestException("Request is not in pending status");
        }

        request.acceptRequest();
        TripJoinRequest updatedRequest = tripJoinRequestRepository.save(request);

        JointTrip trip = jointTripRepository.findActiveByTripId(request.getTripId()).orElse(null);
        User sender = userRepo.findById(request.getSenderId()).orElse(null);
        User receiver = userRepo.findById(request.getReceiverId()).orElse(null);

        log.info("Request accepted successfully: {}", requestId);
        return mapToResponse(updatedRequest, trip, sender, receiver);
    }

    @Transactional
    public TripJoinRequestResponse rejectRequest(Long requestId, Long userId) {
        log.info("Rejecting request: {} by user: {}", requestId, userId);

        TripJoinRequest request = tripJoinRequestRepository.findActiveById(requestId)
                .orElseThrow(() -> new BadRequestException("Request not found with ID: " + requestId));

        // Validate user is the receiver
        if (!request.getReceiverId().equals(userId)) {
            throw new BadRequestException("Only the receiver can reject this request");
        }

        // Validate request is pending
        if (!"PENDING".equals(request.getRequestStatus())) {
            throw new BadRequestException("Request is not in pending status");
        }

        request.rejectRequest();
        TripJoinRequest updatedRequest = tripJoinRequestRepository.save(request);

        JointTrip trip = jointTripRepository.findActiveByTripId(request.getTripId()).orElse(null);
        User sender = userRepo.findById(request.getSenderId()).orElse(null);
        User receiver = userRepo.findById(request.getReceiverId()).orElse(null);

        log.info("Request rejected successfully: {}", requestId);
        return mapToResponse(updatedRequest, trip, sender, receiver);
    }

    @Transactional
    public void cancelRequest(Long requestId, Long userId) {
        log.info("Cancelling request: {} by user: {}", requestId, userId);

        TripJoinRequest request = tripJoinRequestRepository.findActiveById(requestId)
                .orElseThrow(() -> new BadRequestException("Request not found with ID: " + requestId));

        // Validate user is the sender
        if (!request.getSenderId().equals(userId)) {
            throw new BadRequestException("Only the sender can cancel this request");
        }

        request.cancelRequest();
        tripJoinRequestRepository.save(request);

        log.info("Request cancelled successfully: {}", requestId);
    }

    // Helper method to map entity to response
    // Helper method to map entity to response
    private TripJoinRequestResponse mapToResponse(TripJoinRequest request, JointTrip trip, User sender, User receiver) {
        return TripJoinRequestResponse.builder()
                .requestId(request.getRequestId())
                .tripId(request.getTripId())
                .tripTitle(trip != null ? trip.getTripTitle() : null)
                .tripDestination(trip != null ? trip.getTripDestination() : null)
                .senderId(request.getSenderId())
                .senderName(sender != null ? sender.getUsername() : null)
                .receiverId(request.getReceiverId())
                .receiverName(receiver != null ? receiver.getUsername() : null)
                .requestStatus(request.getRequestStatus())
                .createdAt(request.getCreatedAt())
                .respondedAt(request.getRespondedAt())
                .build();
    }
}