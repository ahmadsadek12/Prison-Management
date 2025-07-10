package org.example.controllers;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.models.Expense;
import org.example.models.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.example.services.ExpenseService;
import org.example.services.DepartmentService;

import java.time.LocalDate;
import java.util.List;

@Component
@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpensesController {

    private final ExpenseService expenseService;
    private final DepartmentService departmentService;

    @FXML private TableView<Expense> expensesTable;
    @FXML private TableColumn<Expense, Integer> expenseIdCol;
    @FXML private TableColumn<Expense, String> departmentCol;
    @FXML private TableColumn<Expense, Double> amountCol;
    @FXML private TableColumn<Expense, String> dueDateCol;
    @FXML private TableColumn<Expense, String> statusCol;
    @FXML private TableColumn<Expense, Void> actionsCol;

    @Autowired
    public ExpensesController(ExpenseService expenseService, DepartmentService departmentService) {
        this.expenseService = expenseService;
        this.departmentService = departmentService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadExpenses();
    }

    private void setupTableColumns() {
        expenseIdCol.setCellValueFactory(new PropertyValueFactory<>("expenseId"));
        departmentCol.setCellValueFactory(expenseData ->
                new ReadOnlyStringWrapper(expenseData.getValue().getDepartment().getType()));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadExpenses() {
        ObservableList<Expense> expenses = FXCollections.observableArrayList(expenseService.getAllExpenses());
        expensesTable.setItems(expenses);
    }

    @FXML
    private void showAddExpenseForm() {
        System.out.println("Show Add Expense Form");
    }

    @FXML
    private void markExpenseAsPaid() {
        Expense selectedExpense = expensesTable.getSelectionModel().getSelectedItem();
        if (selectedExpense != null) {
            expenseService.markExpenseAsPaid(selectedExpense.getId());
            expensesTable.refresh();
            System.out.println("Marked expense as paid: " + selectedExpense.getId());
        } else {
            System.out.println("No expense selected.");
        }
    }

    @FXML
    private void goBack() {
        System.out.println("Navigating back to Dashboard");
    }

    // REST Endpoints
    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Integer id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Expense>> getExpensesByDepartment(@PathVariable Integer departmentId) {
        Department department = departmentService.getDepartmentById(departmentId);
        return ResponseEntity.ok(expenseService.getExpensesByDepartment(department));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Expense>> getExpensesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(expenseService.getExpensesByStatus(status));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Expense>> getOverdueExpenses() {
        return ResponseEntity.ok(expenseService.getOverdueExpenses());
    }

    @GetMapping("/department/{departmentId}/overdue")
    public ResponseEntity<List<Expense>> getOverdueExpensesByDepartment(@PathVariable Integer departmentId) {
        Department department = departmentService.getDepartmentById(departmentId);
        return ResponseEntity.ok(expenseService.getOverdueExpensesByDepartment(department));
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestBody ExpenseRequest request) {
        Department department = departmentService.getDepartmentById(request.getDepartmentId());
        Expense expense = new Expense(department, request.getAmount(), request.getDueDate());
        return ResponseEntity.ok(expenseService.createExpense(expense));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable Integer id, @RequestBody ExpenseRequest request) {
        Department department = departmentService.getDepartmentById(request.getDepartmentId());
        Expense expense = new Expense(department, request.getAmount(), request.getDueDate());
        return ResponseEntity.ok(expenseService.updateExpense(id, expense));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Integer id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<Expense> markExpenseAsPaid(@PathVariable Integer id) {
        return ResponseEntity.ok(expenseService.markExpenseAsPaid(id));
    }

    @GetMapping("/stats")
    public ResponseEntity<ExpenseStats> getExpenseStats() {
        List<Expense> allExpenses = expenseService.getAllExpenses();
        
        double totalAmount = allExpenses.stream()
            .mapToDouble(Expense::getAmount)
            .sum();
        
        double pendingAmount = allExpenses.stream()
            .filter(e -> e.isPending())
            .mapToDouble(Expense::getAmount)
            .sum();
        
        long overdueCount = allExpenses.stream()
            .filter(Expense::isOverdue)
            .count();
        
        return ResponseEntity.ok(new ExpenseStats(
            totalAmount,
            pendingAmount,
            overdueCount,
            allExpenses.stream().filter(Expense::isPaid).count()
        ));
    }

    // DTO classes
    private static class ExpenseRequest {
        private Integer departmentId;
        private double amount;
        private LocalDate dueDate;

        public Integer getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Integer departmentId) {
            this.departmentId = departmentId;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public LocalDate getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
        }
    }

    private static class ExpenseStats {
        private final double totalAmount;
        private final double pendingAmount;
        private final long overdueCount;
        private final long paidCount;

        public ExpenseStats(double totalAmount, double pendingAmount, long overdueCount, long paidCount) {
            this.totalAmount = totalAmount;
            this.pendingAmount = pendingAmount;
            this.overdueCount = overdueCount;
            this.paidCount = paidCount;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public double getPendingAmount() {
            return pendingAmount;
        }

        public long getOverdueCount() {
            return overdueCount;
        }

        public long getPaidCount() {
            return paidCount;
        }
    }
}
