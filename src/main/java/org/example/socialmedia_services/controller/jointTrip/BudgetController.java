package org.example.socialmedia_services.controller.jointTrip;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.socialmedia_services.dto.jointTrip.BudgetResponse;
import org.example.socialmedia_services.dto.jointTrip.CreateBudgetRequest;
import org.example.socialmedia_services.dto.jointTrip.UpdateMemberExpenseRequest;
import org.example.socialmedia_services.services.jointTrip.BudgetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping("/create")
    public ResponseEntity<?> createBudget(@Valid @RequestBody CreateBudgetRequest request) {
        BudgetResponse response = budgetService.createBudget(request);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Budget created successfully");
        responseData.put("data", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getBudgetByGroupId(@PathVariable Long groupId) {
        BudgetResponse response = budgetService.getBudgetByGroupId(groupId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Budget retrieved successfully");
        responseData.put("data", response);

        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/{budgetGroupId}")
    public ResponseEntity<?> getBudgetById(@PathVariable Long budgetGroupId) {
        BudgetResponse response = budgetService.getBudgetById(budgetGroupId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Budget retrieved successfully");
        responseData.put("data", response);

        return ResponseEntity.ok(responseData);
    }

    @PutMapping("/{budgetGroupId}/expense")
    public ResponseEntity<?> updateMemberExpense(
            @PathVariable Long budgetGroupId,
            @Valid @RequestBody UpdateMemberExpenseRequest request) {

        BudgetResponse response = budgetService.updateMemberExpense(budgetGroupId, request);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Member expense updated successfully");
        responseData.put("data", response);

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/{budgetGroupId}/member")
    public ResponseEntity<?> addMemberToBudget(
            @PathVariable Long budgetGroupId,
            @Valid @RequestBody CreateBudgetRequest.MemberExpenseRequest request) {

        BudgetResponse response = budgetService.addMemberToBudget(budgetGroupId, request);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Member added to budget successfully");
        responseData.put("data", response);

        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/{budgetGroupId}")
    public ResponseEntity<?> deleteBudget(@PathVariable Long budgetGroupId) {
        budgetService.deleteBudget(budgetGroupId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Budget deleted successfully");

        return ResponseEntity.ok(responseData);
    }
}