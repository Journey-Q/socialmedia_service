package org.example.socialmedia_services.repository.jointTrip;

import org.example.socialmedia_services.entity.jointTrip.BudgetGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetGroupRepository extends JpaRepository<BudgetGroup, Long> {

    @Query("SELECT bg FROM BudgetGroup bg WHERE bg.groupId = :groupId AND bg.isActive = true")
    Optional<BudgetGroup> findActiveByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT bg FROM BudgetGroup bg WHERE bg.budgetGroupId = :budgetGroupId AND bg.isActive = true")
    Optional<BudgetGroup> findActiveByBudgetGroupId(@Param("budgetGroupId") Long budgetGroupId);

    @Query("SELECT bg FROM BudgetGroup bg WHERE bg.isActive = true ORDER BY bg.createdAt DESC")
    List<BudgetGroup> findAllActive();

    @Modifying
    @Query("UPDATE BudgetGroup bg SET bg.isActive = false WHERE bg.budgetGroupId = :budgetGroupId")
    int deactivateBudget(@Param("budgetGroupId") Long budgetGroupId);

    boolean existsByGroupId(Long groupId);
}