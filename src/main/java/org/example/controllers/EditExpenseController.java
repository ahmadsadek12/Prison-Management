package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.models.Expense;
import org.example.models.Department;
import org.example.services.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class EditExpenseController {
    @FXML private TextField amountField;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<String> statusComboBox;

    private Expense expense;
    private Department department;
    private Stage dialogStage;
    private Runnable onSaveCallback;

    @Autowired
    private ExpenseService expenseService;

    public void initData(Expense expense, Department department, Runnable onSaveCallback) {
        this.expense = expense;
        this.department = department;
        this.onSaveCallback = onSaveCallback;
        
        // Populate status combo box
        statusComboBox.getItems().addAll("PAID", "PENDING");
        
        if (expense != null) {
            amountField.setText(String.valueOf(expense.getAmount()));
            dueDatePicker.setValue(expense.getDueDate());
            statusComboBox.setValue(expense.getStatus());
        } else {
            amountField.setText("");
            dueDatePicker.setValue(null);
            statusComboBox.setValue("PENDING"); // Default for new expenses
        }
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    private void handleSave() {
        try {
            String amountStr = amountField.getText().trim();
            LocalDate dueDate = dueDatePicker.getValue();
            String status = statusComboBox.getValue();

            if (amountStr.isEmpty() || dueDate == null || status == null) {
                showError("Validation Error", "All fields are required.");
                return;
            }

            double amount = Double.parseDouble(amountStr);

            if (expense == null) {
                expense = new Expense();
                expense.setDepartment(department);
            }
            expense.setAmount(amount);
            expense.setDueDate(dueDate);
            expense.setStatus(status);

            if (expense.getId() == null) {
                expenseService.createExpense(expense);
            } else {
                expenseService.updateExpense(expense.getId(), expense);
            }
            if (onSaveCallback != null) onSaveCallback.run();
            if (dialogStage != null) dialogStage.close();
        } catch (Exception e) {
            showError("Error", "Failed to save expense: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) dialogStage.close();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // For dialog loading
    private javafx.scene.Parent root;
    public javafx.scene.Parent getRoot() { return root; }
    public void setRoot(javafx.scene.Parent root) { this.root = root; }
} 