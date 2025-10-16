package org.example.socialmedia_services.dto.jointTrip;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SendTripRequestDto {

    @NotNull(message = "Trip ID is required")
    private Long tripId;

    @NotEmpty(message = "At least one receiver is required")
    private List<Long> receiverIds; // List of follower IDs to send request to
}