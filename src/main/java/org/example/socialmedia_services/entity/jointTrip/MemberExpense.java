package org.example.socialmedia_services.entity.jointTrip;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_expense")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long expenseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_group_id", nullable = false)
    private BudgetGroup budgetGroup;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "member_name", nullable = false, length = 200)
    private String memberName;

    @Column(name = "member_avatar", length = 500)
    private String memberAvatar;

    @Column(name = "travel_expense", nullable = false)
    @Builder.Default
    private Double travelExpense = 0.0;

    @Column(name = "food_expense", nullable = false)
    @Builder.Default
    private Double foodExpense = 0.0;

    @Column(name = "hotel_expense", nullable = false)
    @Builder.Default
    private Double hotelExpense = 0.0;

    @Column(name = "other_expense", nullable = false)
    @Builder.Default
    private Double otherExpense = 0.0;

    @Column(name = "total_expense", nullable = false)
    @Builder.Default
    private Double totalExpense = 0.0;

    @Column(name = "settlement_amount", nullable = false)
    @Builder.Default
    private Double settlementAmount = 0.0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void calculateTotal() {
        this.totalExpense = this.travelExpense + this.foodExpense +
                this.hotelExpense + this.otherExpense;
    }
}