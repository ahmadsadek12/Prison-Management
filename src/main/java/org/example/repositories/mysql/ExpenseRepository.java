package org.example.repositories.mysql;

import org.example.models.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    
    // Find expenses by status
    List<Expense> findByStatus(String status);
    
    // Find expenses by department ID
    List<Expense> findByDepartmentId(Integer departmentId);
    
    // Find expenses by department ID and status
    List<Expense> findByDepartmentIdAndStatus(Integer departmentId, String status);
    
    // Find expenses by amount range
    List<Expense> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    // Find expenses by department ID and amount range
    List<Expense> findByDepartmentIdAndAmountBetween(Integer departmentId, BigDecimal minAmount, BigDecimal maxAmount);
    
    // Find expenses by due date range
    List<Expense> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find expenses by department ID and due date range
    List<Expense> findByDepartmentIdAndDueDateBetween(Integer departmentId, LocalDate startDate, LocalDate endDate);
    
    // Find overdue expenses
    List<Expense> findByStatusAndDueDateBefore(String status, LocalDate date);
    
    // Find overdue expenses by department
    List<Expense> findByDepartmentIdAndStatusAndDueDateBefore(Integer departmentId, String status, LocalDate date);
}
