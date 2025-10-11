package org.example.socialmedia_services.entity.jointTrip;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budget_group")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_group_id")
    private Long budgetGroupId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "group_name", nullable = false, length = 200)
    private String groupName;

    @Column(name = "total_expense", nullable = false)
    @Builder.Default
    private Double totalExpense = 0.0;

    @Column(name = "per_person_average", nullable = false)
    @Builder.Default
    private Double perPersonAverage = 0.0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "budgetGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<MemberExpense> memberExpenses = new ArrayList<>();

    public void calculateTotals() {
        this.totalExpense = memberExpenses.stream()
                .filter(me -> Boolean.TRUE.equals(me.getIsActive()))
                .mapToDouble(MemberExpense::getTotalExpense)
                .sum();

        long activeCount = memberExpenses.stream()
                .filter(me -> Boolean.TRUE.equals(me.getIsActive()))
                .count();

        this.perPersonAverage = activeCount > 0 ? this.totalExpense / activeCount : 0.0;
    }
}