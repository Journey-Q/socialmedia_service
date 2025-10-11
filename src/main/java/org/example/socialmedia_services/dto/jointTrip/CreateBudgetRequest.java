package org.example.socialmedia_services.dto.jointTrip;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateBudgetRequest {

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotBlank(message = "Group name is required")
    @Size(max = 200, message = "Group name cannot exceed 200 characters")
    private String groupName;

    @Valid
    private List<MemberExpenseRequest> members;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class MemberExpenseRequest {

        @NotNull(message = "User ID is required")
        private Long userId;

        @NotBlank(message = "Member name is required")
        private String memberName;

        private String memberAvatar;

        @Min(value = 0, message = "Travel expense cannot be negative")
        @Builder.Default
        private Double travelExpense = 0.0;

        @Min(value = 0, message = "Food expense cannot be negative")
        @Builder.Default
        private Double foodExpense = 0.0;

        @Min(value = 0, message = "Hotel expense cannot be negative")
        @Builder.Default
        private Double hotelExpense = 0.0;

        @Min(value = 0, message = "Other expense cannot be negative")
        @Builder.Default
        private Double otherExpense = 0.0;
    }
}