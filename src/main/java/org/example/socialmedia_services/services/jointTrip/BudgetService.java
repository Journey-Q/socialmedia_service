package org.example.socialmedia_services.services.jointTrip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmedia_services.dto.jointTrip.BudgetResponse;
import org.example.socialmedia_services.dto.jointTrip.CreateBudgetRequest;
import org.example.socialmedia_services.dto.jointTrip.UpdateMemberExpenseRequest;
import org.example.socialmedia_services.entity.jointTrip.BudgetGroup;
import org.example.socialmedia_services.entity.jointTrip.MemberExpense;
import org.example.socialmedia_services.exception.BadRequestException;
import org.example.socialmedia_services.repository.jointTrip.BudgetGroupRepository;
import org.example.socialmedia_services.repository.jointTrip.MemberExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private final BudgetGroupRepository budgetGroupRepository;
    private final MemberExpenseRepository memberExpenseRepository;

    @Transactional
    public BudgetResponse createBudget(CreateBudgetRequest request) {
        log.info("Creating budget for group: {}", request.getGroupId());

        if (budgetGroupRepository.existsByGroupId(request.getGroupId())) {
            throw new BadRequestException("Budget already exists for this group");
        }

        BudgetGroup budgetGroup = BudgetGroup.builder()
                .groupId(request.getGroupId())
                .groupName(request.getGroupName())
                .isActive(true)
                .build();

        budgetGroup = budgetGroupRepository.save(budgetGroup);

        if (request.getMembers() != null && !request.getMembers().isEmpty()) {
            BudgetGroup finalBudgetGroup = budgetGroup;

            for (CreateBudgetRequest.MemberExpenseRequest memberRequest : request.getMembers()) {
                MemberExpense expense = MemberExpense.builder()
                        .budgetGroup(finalBudgetGroup)
                        .userId(memberRequest.getUserId())
                        .memberName(memberRequest.getMemberName())
                        .memberAvatar(memberRequest.getMemberAvatar())
                        .travelExpense(memberRequest.getTravelExpense() != null ? memberRequest.getTravelExpense() : 0.0)
                        .foodExpense(memberRequest.getFoodExpense() != null ? memberRequest.getFoodExpense() : 0.0)
                        .hotelExpense(memberRequest.getHotelExpense() != null ? memberRequest.getHotelExpense() : 0.0)
                        .otherExpense(memberRequest.getOtherExpense() != null ? memberRequest.getOtherExpense() : 0.0)
                        .isActive(true)
                        .build();

                expense.calculateTotal();
                finalBudgetGroup.getMemberExpenses().add(expense);
            }

            budgetGroup.calculateTotals();
            calculateSettlements(budgetGroup);
            budgetGroup = budgetGroupRepository.save(budgetGroup);
        }

        log.info("Budget created successfully with ID: {}", budgetGroup.getBudgetGroupId());
        return mapToResponse(budgetGroup);
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetByGroupId(Long groupId) {
        BudgetGroup budgetGroup = budgetGroupRepository.findActiveByGroupId(groupId)
                .orElseThrow(() -> new BadRequestException("Budget not found for group ID: " + groupId));
        return mapToResponse(budgetGroup);
    }

    @Transactional(readOnly = true)
    public BudgetResponse getBudgetById(Long budgetGroupId) {
        BudgetGroup budgetGroup = budgetGroupRepository.findActiveByBudgetGroupId(budgetGroupId)
                .orElseThrow(() -> new BadRequestException("Budget not found with ID: " + budgetGroupId));
        return mapToResponse(budgetGroup);
    }

    @Transactional
    public BudgetResponse updateMemberExpense(Long budgetGroupId, UpdateMemberExpenseRequest request) {
        BudgetGroup budgetGroup = budgetGroupRepository.findActiveByBudgetGroupId(budgetGroupId)
                .orElseThrow(() -> new BadRequestException("Budget not found with ID: " + budgetGroupId));

        MemberExpense memberExpense = budgetGroup.getMemberExpenses().stream()
                .filter(me -> me.getUserId().equals(request.getUserId()) && Boolean.TRUE.equals(me.getIsActive()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Member expense not found for user ID: " + request.getUserId()));

        switch (request.getCategory().toLowerCase()) {
            case "travel":
                memberExpense.setTravelExpense(request.getAmount());
                break;
            case "food":
                memberExpense.setFoodExpense(request.getAmount());
                break;
            case "hotel":
                memberExpense.setHotelExpense(request.getAmount());
                break;
            case "other":
                memberExpense.setOtherExpense(request.getAmount());
                break;
            default:
                throw new BadRequestException("Invalid expense category: " + request.getCategory());
        }

        memberExpense.calculateTotal();
        budgetGroup.calculateTotals();
        calculateSettlements(budgetGroup);

        BudgetGroup updatedBudget = budgetGroupRepository.save(budgetGroup);
        log.info("Member expense updated successfully for user: {} in budget: {}", request.getUserId(), budgetGroupId);

        return mapToResponse(updatedBudget);
    }

    @Transactional
    public BudgetResponse addMemberToBudget(Long budgetGroupId, CreateBudgetRequest.MemberExpenseRequest memberRequest) {
        BudgetGroup budgetGroup = budgetGroupRepository.findActiveByBudgetGroupId(budgetGroupId)
                .orElseThrow(() -> new BadRequestException("Budget not found with ID: " + budgetGroupId));

        boolean memberExists = budgetGroup.getMemberExpenses().stream()
                .anyMatch(me -> me.getUserId().equals(memberRequest.getUserId()) && Boolean.TRUE.equals(me.getIsActive()));

        if (memberExists) {
            throw new BadRequestException("Member already exists in this budget");
        }

        MemberExpense newExpense = MemberExpense.builder()
                .budgetGroup(budgetGroup)
                .userId(memberRequest.getUserId())
                .memberName(memberRequest.getMemberName())
                .memberAvatar(memberRequest.getMemberAvatar())
                .travelExpense(memberRequest.getTravelExpense() != null ? memberRequest.getTravelExpense() : 0.0)
                .foodExpense(memberRequest.getFoodExpense() != null ? memberRequest.getFoodExpense() : 0.0)
                .hotelExpense(memberRequest.getHotelExpense() != null ? memberRequest.getHotelExpense() : 0.0)
                .otherExpense(memberRequest.getOtherExpense() != null ? memberRequest.getOtherExpense() : 0.0)
                .isActive(true)
                .build();

        newExpense.calculateTotal();
        budgetGroup.getMemberExpenses().add(newExpense);
        budgetGroup.calculateTotals();
        calculateSettlements(budgetGroup);

        BudgetGroup updatedBudget = budgetGroupRepository.save(budgetGroup);
        log.info("Member added to budget successfully: {}", memberRequest.getUserId());

        return mapToResponse(updatedBudget);
    }

    @Transactional
    public void deleteBudget(Long budgetGroupId) {
        BudgetGroup budgetGroup = budgetGroupRepository.findActiveByBudgetGroupId(budgetGroupId)
                .orElseThrow(() -> new BadRequestException("Budget not found with ID: " + budgetGroupId));

        budgetGroup.setIsActive(false);
        budgetGroupRepository.save(budgetGroup);
        log.info("Budget deleted successfully: {}", budgetGroupId);
    }

    // CORRECTED: Settlement calculation
    private void calculateSettlements(BudgetGroup budgetGroup) {
        List<MemberExpense> activeMembers = budgetGroup.getMemberExpenses().stream()
                .filter(me -> Boolean.TRUE.equals(me.getIsActive()))
                .collect(Collectors.toList());

        double average = budgetGroup.getPerPersonAverage();

        for (MemberExpense member : activeMembers) {
            // CORRECTED LOGIC: totalExpense - average
            // Positive = spent MORE = should RECEIVE
            // Negative = spent LESS = should PAY
            double settlement = member.getTotalExpense() - average;
            member.setSettlementAmount(settlement);
        }
    }

    private BudgetResponse mapToResponse(BudgetGroup budgetGroup) {
        List<MemberExpense> activeMembers = budgetGroup.getMemberExpenses().stream()
                .filter(me -> Boolean.TRUE.equals(me.getIsActive()))
                .collect(Collectors.toList());

        // Creditors = spent MORE than average (should RECEIVE money)
        List<MemberExpense> creditors = activeMembers.stream()
                .filter(m -> m.getSettlementAmount() > 0.01)
                .sorted(Comparator.comparingDouble(MemberExpense::getSettlementAmount).reversed())
                .collect(Collectors.toList());

        // Debtors = spent LESS than average (should PAY money)
        List<MemberExpense> debtors = activeMembers.stream()
                .filter(m -> m.getSettlementAmount() < -0.01)
                .sorted(Comparator.comparingDouble(MemberExpense::getSettlementAmount))
                .collect(Collectors.toList());

        List<BudgetResponse.MemberExpenseResponse> memberResponses = activeMembers.stream()
                .map(member -> {
                    BudgetResponse.SettlementStatus status = getSettlementStatus(member, creditors, debtors);

                    return BudgetResponse.MemberExpenseResponse.builder()
                            .expenseId(member.getExpenseId())
                            .userId(member.getUserId())
                            .memberName(member.getMemberName())
                            .memberAvatar(member.getMemberAvatar())
                            .travelExpense(member.getTravelExpense())
                            .foodExpense(member.getFoodExpense())
                            .hotelExpense(member.getHotelExpense())
                            .otherExpense(member.getOtherExpense())
                            .totalExpense(member.getTotalExpense())
                            .settlementAmount(member.getSettlementAmount())
                            .settlementStatus(status)
                            .createdAt(member.getCreatedAt())
                            .updatedAt(member.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return BudgetResponse.builder()
                .budgetGroupId(budgetGroup.getBudgetGroupId())
                .groupId(budgetGroup.getGroupId())
                .groupName(budgetGroup.getGroupName())
                .totalExpense(budgetGroup.getTotalExpense())
                .perPersonAverage(budgetGroup.getPerPersonAverage())
                .members(memberResponses)
                .createdAt(budgetGroup.getCreatedAt())
                .updatedAt(budgetGroup.getUpdatedAt())
                .build();
    }

    private BudgetResponse.SettlementStatus getSettlementStatus(
            MemberExpense member,
            List<MemberExpense> creditors,
            List<MemberExpense> debtors) {

        double settlement = member.getSettlementAmount();

        if (Math.abs(settlement) < 0.01) {
            return BudgetResponse.SettlementStatus.builder()
                    .status("settled")
                    .amount(0.0)
                    .message("All settled! No money exchange needed.")
                    .build();
        } else if (settlement > 0) {
            // Positive = spent MORE = should RECEIVE money
            MemberExpense debtor = debtors.isEmpty() ? null : debtors.get(0);
            return BudgetResponse.SettlementStatus.builder()
                    .status("should_receive")
                    .amount(Math.abs(settlement))
                    .message(debtor != null ?
                            String.format("Should receive LKR %.0f from %s",
                                    Math.abs(settlement), debtor.getMemberName()) :
                            String.format("Should receive LKR %.0f", Math.abs(settlement)))
                    .peerName(debtor != null ? debtor.getMemberName() : null)
                    .peerUserId(debtor != null ? debtor.getUserId() : null)
                    .build();
        } else {
            // Negative = spent LESS = should PAY money
            MemberExpense creditor = creditors.isEmpty() ? null : creditors.get(0);
            return BudgetResponse.SettlementStatus.builder()
                    .status("owes")
                    .amount(Math.abs(settlement))
                    .message(creditor != null ?
                            String.format("Owes LKR %.0f to %s",
                                    Math.abs(settlement), creditor.getMemberName()) :
                            String.format("Owes LKR %.0f", Math.abs(settlement)))
                    .peerName(creditor != null ? creditor.getMemberName() : null)
                    .peerUserId(creditor != null ? creditor.getUserId() : null)
                    .build();
        }
    }
}