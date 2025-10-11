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
public class BudgetResponse {

    private Long budgetGroupId;
    private Long groupId;
    private String groupName;
    private Double totalExpense;
    private Double perPersonAverage;
    private List<MemberExpenseResponse> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class MemberExpenseResponse {
        private Long expenseId;
        private Long userId;
        private String memberName;
        private String memberAvatar;
        private Double travelExpense;
        private Double foodExpense;
        private Double hotelExpense;
        private Double otherExpense;
        private Double totalExpense;
        private Double settlementAmount;
        private SettlementStatus settlementStatus;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class SettlementStatus {
        private String status;
        private Double amount;
        private String message;
        private String peerName;
        private Long peerUserId;
    }
}