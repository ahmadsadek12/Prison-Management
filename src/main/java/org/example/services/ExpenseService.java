package org.example.services;

import org.example.models.Expense;
import org.example.models.Department;
import org.example.repositories.mysql.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Service
@Transactional
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    
    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }
    
    @Transactional(readOnly = true)
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Expense getExpenseById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Expense ID cannot be null");
        }
        return expenseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
    }
    
    @Transactional
    public Expense createExpense(Expense expense) {
        if (expense == null) {
            throw new IllegalArgumentException("Expense cannot be null");
        }
        validateExpense(expense);
        
        expense.setStatus("PENDING");
        
        return expenseRepository.save(expense);
    }
    
    @Transactional
    public Expense updateExpense(Integer id, Expense updatedExpense) {
        if (id == null) {
            throw new IllegalArgumentException("Expense ID cannot be null");
        }
        if (updatedExpense == null) {
            throw new IllegalArgumentException("Updated expense cannot be null");
        }
        
        Expense existingExpense = getExpenseById(id);
        validateExpense(updatedExpense);
        
        // Only update allowed fields
        existingExpense.setDepartment(updatedExpense.getDepartment());
        existingExpense.setAmount(updatedExpense.getAmount());
        existingExpense.setDueDate(updatedExpense.getDueDate());
        existingExpense.setStatus(updatedExpense.getStatus());
        
        return expenseRepository.save(existingExpense);
    }
    
    @Transactional
    public void deleteExpense(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Expense ID cannot be null");
        }
        
        Expense expense = getExpenseById(id);
        if (!"PENDING".equals(expense.getStatus())) {
            throw new IllegalStateException("Cannot delete expense that is not pending. Current status: " + expense.getStatus());
        }
        
        expenseRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByDepartment(Department department) {
        if (department == null) {
            throw new IllegalArgumentException("Department cannot be null");
        }
        return expenseRepository.findByDepartmentId(department.getId());
    }
    
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByDepartmentAndStatus(Department department, String status) {
        if (department == null) {
            throw new IllegalArgumentException("Department cannot be null");
        }
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        return expenseRepository.findByDepartmentIdAndStatus(department.getId(), status);
    }
    
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        return expenseRepository.findByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByAmountRange(double minAmount, double maxAmount) {
        if (minAmount < 0) {
            throw new IllegalArgumentException("Minimum amount must be non-negative");
        }
        if (maxAmount < minAmount) {
            throw new IllegalArgumentException("Maximum amount must be greater than or equal to minimum amount");
        }
        return expenseRepository.findByAmountBetween(
            BigDecimal.valueOf(minAmount), 
            BigDecimal.valueOf(maxAmount)
        );
    }
    
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByDepartmentAndAmountRange(Department department, double minAmount, double maxAmount) {
        if (department == null) {
            throw new IllegalArgumentException("Department cannot be null");
        }
        if (minAmount < 0) {
            throw new IllegalArgumentException("Minimum amount must be non-negative");
        }
        if (maxAmount < minAmount) {
            throw new IllegalArgumentException("Maximum amount must be greater than or equal to minimum amount");
        }
        return expenseRepository.findByDepartmentIdAndAmountBetween(
            department.getId(),
            BigDecimal.valueOf(minAmount),
            BigDecimal.valueOf(maxAmount)
        );
    }
    
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByDueDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must not be before start date");
        }
        return expenseRepository.findByDueDateBetween(startDate, endDate);
    }
    
    @Transactional(readOnly = true)
    public List<Expense> getExpensesByDepartmentAndDueDateRange(Department department, LocalDate startDate, LocalDate endDate) {
        if (department == null) {
            throw new IllegalArgumentException("Department cannot be null");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must not be before start date");
        }
        return expenseRepository.findByDepartmentIdAndDueDateBetween(
            department.getId(),
            startDate,
            endDate
        );
    }
    
    @Transactional
    public Expense markExpenseAsPaid(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Expense ID cannot be null");
        }
        
        Expense expense = getExpenseById(id);
        expense.setStatus("PAID");
        return expenseRepository.save(expense);
    }
    
    @Transactional(readOnly = true)
    public List<Expense> getOverdueExpenses() {
        return expenseRepository.findByStatusAndDueDateBefore("PENDING", LocalDate.now());
    }
    
    @Transactional(readOnly = true)
    public List<Expense> getOverdueExpensesByDepartment(Department department) {
        if (department == null) {
            throw new IllegalArgumentException("Department cannot be null");
        }
        return expenseRepository.findByDepartmentIdAndStatusAndDueDateBefore(
            department.getId(),
            "PENDING",
            LocalDate.now()
        );
    }
    
    private void validateExpense(Expense expense) {
        if (expense.getDepartment() == null) {
            throw new IllegalArgumentException("Department cannot be null");
        }
        if (expense.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        if (expense.getDueDate() == null) {
            throw new IllegalArgumentException("Due date cannot be null");
        }
        if (expense.getDueDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date cannot be in the past");
        }
    }
}
