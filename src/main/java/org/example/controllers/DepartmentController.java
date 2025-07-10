package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.models.Department;
import org.example.models.Expense;
import org.example.models.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.example.services.DepartmentService;
import org.example.services.ExpenseService;
import org.example.services.StaffService;

import java.util.List;

@Component
@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final ExpenseService expenseService;
    private final StaffService staffService;

    @FXML private Label departmentNameLabel;

    @FXML private TableView<Expense> expensesTable;
    @FXML private TableColumn<Expense, Integer> expenseIdCol;
    @FXML private TableColumn<Expense, Double> amountCol;
    @FXML private TableColumn<Expense, String> dueDateCol;
    @FXML private TableColumn<Expense, String> statusCol;
    @FXML private TableColumn<Expense, Void> expenseActionsCol;

    @FXML private TableView<Staff> staffTable;
    @FXML private TableColumn<Staff, Integer> staffIdCol;
    @FXML private TableColumn<Staff, String> staffNameCol;
    @FXML private TableColumn<Staff, String> staffRoleCol;
    @FXML private TableColumn<Staff, Void> staffActionsCol;

    private Department currentDepartment;

    @Autowired
    public DepartmentController(DepartmentService departmentService, ExpenseService expenseService, StaffService staffService) {
        this.departmentService = departmentService;
        this.expenseService = expenseService;
        this.staffService = staffService;
    }

    @FXML
    public void initialize() {
        currentDepartment = departmentService.getDepartmentById(1);

        if (currentDepartment != null) {
            departmentNameLabel.setText(currentDepartment.getType());

            // Setup Expenses Table columns
            expenseIdCol.setCellValueFactory(new PropertyValueFactory<>("expenseId"));
            amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
            dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
            statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

            // Setup Staff Table columns
            staffIdCol.setCellValueFactory(new PropertyValueFactory<>("staffId"));
            staffNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            staffRoleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

            loadExpenses();
            loadStaff();
        }
    }

    private void loadExpenses() {
        ObservableList<Expense> expenses = FXCollections.observableArrayList(
                expenseService.getExpensesByDepartment(currentDepartment)
        );
        expensesTable.setItems(expenses);
    }

    private void loadStaff() {
        ObservableList<Staff> staffList = FXCollections.observableArrayList(
                staffService.getStaffByDepartment(currentDepartment.getId())
        );
        staffTable.setItems(staffList);
    }

    @FXML
    private void showAddExpenseForm() {
        System.out.println("Show Add Expense Form");
    }

    @FXML
    private void showAddStaffForm() {
        System.out.println("Show Add Staff Form");
    }

    @FXML
    private void goBack() {
        System.out.println("Navigating back to Dashboard");
    }

    // REST Endpoints
    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Integer id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @GetMapping("/{id}/expenses")
    public ResponseEntity<List<Expense>> getDepartmentExpenses(@PathVariable Integer id) {
        return ResponseEntity.ok(expenseService.getExpensesByDepartment(departmentService.getDepartmentById(id)));
    }

    @GetMapping("/{id}/staff")
    public ResponseEntity<List<Staff>> getDepartmentStaff(@PathVariable Integer id) {
        return ResponseEntity.ok(staffService.getStaffByDepartment(id));
    }

    @PostMapping
    public ResponseEntity<Department> createDepartment(@RequestBody DepartmentRequest request) {
        Department department = new Department(request.getType());
        return ResponseEntity.ok(departmentService.createDepartment(department));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Integer id, @RequestBody DepartmentRequest request) {
        Department department = new Department(request.getType());
        return ResponseEntity.ok(departmentService.updateDepartment(id, department));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Integer id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<DepartmentStats> getDepartmentStats(@PathVariable Integer id) {
        Department department = departmentService.getDepartmentById(id);
        List<Staff> staff = staffService.getStaffByDepartment(id);
        List<Expense> expenses = expenseService.getExpensesByDepartment(department);
        
        DepartmentStats stats = new DepartmentStats(
            staff.size(),
            expenses.stream().mapToDouble(Expense::getAmount).sum(),
            expenses.stream().filter(e -> "PENDING".equals(e.getStatus())).count()
        );
        
        return ResponseEntity.ok(stats);
    }

    // DTO classes
    private static class DepartmentRequest {
        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    private static class DepartmentStats {
        private final int staffCount;
        private final double totalExpenses;
        private final long pendingExpenses;

        public DepartmentStats(int staffCount, double totalExpenses, long pendingExpenses) {
            this.staffCount = staffCount;
            this.totalExpenses = totalExpenses;
            this.pendingExpenses = pendingExpenses;
        }

        public int getStaffCount() {
            return staffCount;
        }

        public double getTotalExpenses() {
            return totalExpenses;
        }

        public long getPendingExpenses() {
            return pendingExpenses;
        }
    }
}
