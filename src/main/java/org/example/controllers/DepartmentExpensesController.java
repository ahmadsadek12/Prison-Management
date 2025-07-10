package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ContentDisplay;
import org.example.models.Department;
import org.example.models.Expense;
import org.example.services.DepartmentService;
import org.example.services.ExpenseService;
import org.example.config.SpringFXMLLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.time.format.DateTimeFormatter;
import javafx.scene.layout.HBox;

@Component
public class DepartmentExpensesController {
    private static final Logger LOGGER = Logger.getLogger(DepartmentExpensesController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML private Label departmentNameLabel;

    @FXML private TableView<Expense> expensesTable;
    @FXML private TableColumn<Expense, Integer> expenseIdColumn;
    @FXML private TableColumn<Expense, Double> expenseAmountColumn;
    @FXML private TableColumn<Expense, String> expenseDateColumn;
    @FXML private TableColumn<Expense, String> expenseStatusColumn;
    @FXML private TableColumn<Expense, Void> expenseActionsColumn;

    private final DepartmentService departmentService;
    private final ExpenseService expenseService;
    private Department department;
    private Parent root;

    @Autowired
    private SpringFXMLLoader springFXMLLoader;

    @Autowired
    public DepartmentExpensesController(DepartmentService departmentService, ExpenseService expenseService) {
        this.departmentService = departmentService;
        this.expenseService = expenseService;
    }

    public Parent getRoot() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    @FXML
    public void initialize() {
        setupExpensesTable();
    }

    private void setupExpensesTable() {
        // Initialize expenses table columns
        expenseIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        expenseAmountColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getAmount()).asObject());
        expenseDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDueDate().format(DATE_FORMATTER)));
        expenseStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Set up actions column with Edit and Delete buttons for expenses
        expenseActionsColumn.setCellFactory(col -> new TableCell<Expense, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonBox = new HBox(5, editButton, deleteButton);
            {
                editButton.setStyle("-fx-padding: 2 8 2 8; -fx-background-radius: 4; -fx-background-color: #64b5f6; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-padding: 2 8 2 8; -fx-background-radius: 4; -fx-background-color: #e57373; -fx-text-fill: white;");
                editButton.setOnAction(event -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    try {
                        EditExpenseController controller = springFXMLLoader.loadAndGetController("/fxml/edit-expense.fxml", EditExpenseController.class);
                        Stage dialogStage = new Stage();
                        dialogStage.setTitle("Edit Expense");
                        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                        dialogStage.initOwner(departmentNameLabel.getScene().getWindow());
                        dialogStage.setScene(new javafx.scene.Scene(controller.getRoot()));
                        controller.setDialogStage(dialogStage);
                        controller.initData(expense, department, null);
                        
                        // Refresh the table when the dialog is closed
                        dialogStage.setOnHidden(event2 -> {
                            loadExpenses();
                        });
                        
                        dialogStage.showAndWait();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error opening edit expense dialog", e);
                        showError("Error", "Failed to open edit expense dialog: " + e.getMessage());
                    }
                });
                deleteButton.setOnAction(event -> {
                    Expense expense = getTableView().getItems().get(getIndex());
                    handleDeleteExpense(expense);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonBox);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setStyle("-fx-alignment: CENTER;");
            }
        });
    }

    public void initData(Department department) {
        this.department = department;
        updateUI();
    }

    private void updateUI() {
        if (department != null) {
            departmentNameLabel.setText(department.getType() + " - Expenses");
            
            // Load expenses first
            loadExpenses();
        }
    }

    private void loadExpenses() {
        try {
            List<Expense> expenses = expenseService.getExpensesByDepartment(department);
            ObservableList<Expense> expensesList = FXCollections.observableArrayList(expenses);
            expensesTable.setItems(expensesList);
            expensesTable.refresh(); // Force table refresh
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading expenses", e);
            showError("Error", "Failed to load expenses: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteExpense(Expense expense) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Expense");
        alert.setHeaderText("Delete Expense");
        alert.setContentText("Are you sure you want to delete this expense? This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    expenseService.deleteExpense(expense.getId());
                    loadExpenses(); // Refresh the table
                } catch (Exception e) {
                    showError("Error", "Failed to delete expense: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleAddExpense() {
        try {
            EditExpenseController controller = springFXMLLoader.loadAndGetController("/fxml/edit-expense.fxml", EditExpenseController.class);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Expense");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(departmentNameLabel.getScene().getWindow());
            dialogStage.setScene(new javafx.scene.Scene(controller.getRoot()));
            controller.setDialogStage(dialogStage);
            controller.initData(null, department, null);
            
            // Refresh the table when the dialog is closed
            dialogStage.setOnHidden(event -> {
                loadExpenses();
            });
            
            dialogStage.showAndWait();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening add expense dialog", e);
            showError("Error", "Failed to open add expense dialog: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 