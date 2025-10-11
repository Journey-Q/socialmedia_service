package org.example.socialmedia_services.repository.jointTrip;

import org.example.socialmedia_services.entity.jointTrip.MemberExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberExpenseRepository extends JpaRepository<MemberExpense, Long> {

    @Query("SELECT me FROM MemberExpense me WHERE me.budgetGroup.budgetGroupId = :budgetGroupId AND me.isActive = true")
    List<MemberExpense> findByBudgetGroupId(@Param("budgetGroupId") Long budgetGroupId);

    @Query("SELECT me FROM MemberExpense me WHERE me.budgetGroup.budgetGroupId = :budgetGroupId AND me.userId = :userId AND me.isActive = true")
    Optional<MemberExpense> findByBudgetGroupIdAndUserId(@Param("budgetGroupId") Long budgetGroupId,
                                                         @Param("userId") Long userId);

    @Query("SELECT me FROM MemberExpense me WHERE me.userId = :userId AND me.isActive = true")
    List<MemberExpense> findByUserId(@Param("userId") Long userId);
}