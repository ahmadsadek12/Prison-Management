package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.example.models.Prisoner;
import org.example.models.Crime;
import org.example.models.VisitorLog;
import org.example.models.Visitor;
import org.example.models.MedicalRecord;
import org.example.services.VisitorLogService;
import org.example.services.VisitorService;
import org.example.services.CrimeService;
import org.example.services.MedicalRecordService;
import org.example.services.CellService;
import org.example.config.SpringFXMLLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
public class PrisonerDetailsController {
    @FXML
    private Label nameLabel;
    @FXML
    private Label ageLabel;
    @FXML
    private Label genderLabel;
    @FXML
    private Label sentenceStartLabel;
    @FXML
    private Label sentenceEndLabel;
    @FXML
    private Label blockLabel;
    @FXML
    private Label sentenceDurationLabel;
    @FXML
    private Label timeServedLabel;
    @FXML
    private TextField crimeSearchField;
    @FXML
    private ComboBox<String> crimeFilterComboBox;
    @FXML
    private TableView<Crime> crimesTable;
    @FXML
    private TableColumn<Crime, String> crimeNameColumn;
    @FXML
    private TableColumn<Crime, String> crimeTypeColumn;
    @FXML
    private TableColumn<Crime, String> crimeDateColumn;
    @FXML
    private TableColumn<Crime, String> crimeEndDateColumn;
    @FXML
    private TableColumn<Crime, Void> crimeActionsColumn;
    @FXML
    private TextField visitorSearchField;
    @FXML
    private ComboBox<String> visitorFilterComboBox;
    @FXML
    private TableView<VisitorLog> visitorLogsTable;
    @FXML
    private TableColumn<VisitorLog, String> visitorNameColumn;
    @FXML
    private TableColumn<VisitorLog, String> relationshipColumn;
    @FXML
    private TableColumn<VisitorLog, String> visitDateColumn;
    @FXML
    private TableColumn<VisitorLog, String> visitStatusColumn;
    @FXML
    private TableColumn<VisitorLog, Void> visitActionsColumn;
    @FXML
    private TextField medicalSearchField;
    @FXML
    private ComboBox<String> medicalFilterComboBox;
    @FXML
    private TableView<MedicalRecord> medicalRecordsTable;
    @FXML
    private TableColumn<MedicalRecord, String> medicalDateColumn;
    @FXML
    private TableColumn<MedicalRecord, String> medicalTypeColumn;
    @FXML
    private TableColumn<MedicalRecord, String> medicalDescriptionColumn;
    @FXML
    private TableColumn<MedicalRecord, String> medicalStatusColumn;
    @FXML
    private TableColumn<MedicalRecord, Void> medicalActionsColumn;

    private Prisoner prisoner;
    private Parent root;

    @Autowired
    private FXMLLoader fxmlLoader;

    @Autowired
    private SpringFXMLLoader springFXMLLoader;

    @Autowired
    private VisitorLogService visitorLogService;

    @Autowired
    private VisitorService visitorService;

    @Autowired
    private CrimeService crimeService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private CellService cellService;

    public Parent getRoot() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    @FXML
    public void initialize() {
        try {
        setupCrimesTable();
        setupVisitorLogsTable();
        setupMedicalRecordsTable();
            System.out.println("[DEBUG] PrisonerDetailsController initialized successfully.");
        } catch (Exception e) {
            System.err.println("[ERROR] Exception in PrisonerDetailsController.initialize(): " + e);
            e.printStackTrace();
            showError("Initialization Error", "Failed to initialize prisoner details", e.getMessage());
        }
    }

    private void setupCrimesTable() {
        crimeNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        crimeTypeColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        crimeDateColumn.setCellValueFactory(new PropertyValueFactory<>("sentenceStartDate"));
        
        crimeEndDateColumn.setCellValueFactory(cellData -> {
            Crime crime = cellData.getValue();
            try {
                if (crime.getSentenceStartDate() != null && crime.getSentenceDuration() != null) {
                    LocalDate startDate = crime.getSentenceStartDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                    LocalDate endDate = startDate.plusDays(crime.getSentenceDuration());
                    return new ReadOnlyStringWrapper(endDate.toString());
                }
            } catch (Exception e) {
                System.err.println("Error calculating sentence end date: " + e.getMessage());
            }
            return new ReadOnlyStringWrapper("N/A");
        });

        // Add delete button to crimeActionsColumn
        crimeActionsColumn.setCellFactory(col -> {
            return new javafx.scene.control.TableCell<Crime, Void>() {
                private final javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("Delete");
                {
                    deleteButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 5px;");
                    
                    deleteButton.setOnAction(event -> {
                        Crime crime = getTableView().getItems().get(getIndex());
                        PrisonerDetailsController.this.handleDeleteCrime(crime);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(deleteButton);
                    }
                }
            };
        });
    }

    private void setupVisitorLogsTable() {
        visitorNameColumn.setCellValueFactory(cellData -> {
            VisitorLog log = cellData.getValue();
            try {
                Visitor visitor = visitorService.getVisitorById(log.getVisitorId()).orElse(null);
                return new ReadOnlyStringWrapper(visitor != null ? visitor.getName() : "Unknown");
            } catch (Exception e) {
                return new ReadOnlyStringWrapper("Error loading visitor");
            }
        });

        relationshipColumn.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().getRelationship()));

        visitDateColumn.setCellValueFactory(cellData ->
            new ReadOnlyStringWrapper(cellData.getValue().getDate().toString()));

        visitStatusColumn.setCellValueFactory(cellData ->
            new ReadOnlyStringWrapper(cellData.getValue().getStatus()));

        // Add action buttons to visitActionsColumn
        visitActionsColumn.setCellFactory(col -> new javafx.scene.control.TableCell<VisitorLog, Void>() {
            private final javafx.scene.control.Button editButton = new javafx.scene.control.Button("Edit");
            private final javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("Delete");
            private final javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(5, editButton, deleteButton);
            {
                editButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 5px;");
                deleteButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 5px;");
                buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
                
                editButton.setOnAction(event -> {
                    VisitorLog log = getTableView().getItems().get(getIndex());
                    handleEditVisitorLog(log);
                });
                deleteButton.setOnAction(event -> {
                    VisitorLog log = getTableView().getItems().get(getIndex());
                    handleDeleteVisitorLog(log);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
    }

    private void setupMedicalRecordsTable() {
        medicalDateColumn.setCellValueFactory(cellData -> {
            MedicalRecord record = cellData.getValue();
            if (record.getRecordDate() != null) {
                return new ReadOnlyStringWrapper(record.getRecordDate().toLocalDate().toString());
            }
            return new ReadOnlyStringWrapper("N/A");
        });
        medicalTypeColumn.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        medicalDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        medicalStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        // Add action buttons to medicalActionsColumn
        medicalActionsColumn.setCellFactory(col -> new javafx.scene.control.TableCell<MedicalRecord, Void>() {
            private final javafx.scene.control.Button editButton = new javafx.scene.control.Button("Edit");
            private final javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("Delete");
            private final javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(5, editButton, deleteButton);
            {
                editButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 5px;");
                deleteButton.setStyle("-fx-font-size: 10px; -fx-padding: 2px 5px;");
                buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
                
                editButton.setOnAction(event -> {
                    MedicalRecord record = getTableView().getItems().get(getIndex());
                    handleEditMedicalRecord(record);
                });
                deleteButton.setOnAction(event -> {
                    MedicalRecord record = getTableView().getItems().get(getIndex());
                    handleDeleteMedicalRecord(record);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
    }

    public void setPrisoner(Prisoner prisoner) {
        try {
            if (prisoner == null) {
                throw new IllegalArgumentException("Prisoner is null");
            }
        this.prisoner = prisoner;
        updateLabels();
        loadData();
            System.out.println("[DEBUG] Prisoner set in PrisonerDetailsController: " + prisoner.getName());
        } catch (Exception e) {
            System.err.println("[ERROR] Exception in PrisonerDetailsController.setPrisoner(): " + e);
            e.printStackTrace();
            showError("Prisoner Error", "Failed to set prisoner", e.getMessage());
        }
    }

    private void updateLabels() {
        if (prisoner != null) {
            nameLabel.setText(prisoner.getName());
            ageLabel.setText(String.valueOf(prisoner.getAge()));
            genderLabel.setText(prisoner.getGender());
            sentenceStartLabel.setText(prisoner.getSentenceStart().toString());
            sentenceEndLabel.setText(prisoner.getSentenceEnd().toString());
            blockLabel.setText(prisoner.getCell().getBlock().getType());
            sentenceDurationLabel.setText(prisoner.getSentenceDuration() + " years");
            timeServedLabel.setText(prisoner.getTimeServed() + " years");
        }
    }

    private void loadData() {
        loadCrimes();
        loadVisitorLogs();
        loadMedicalRecords();
    }

    private void loadCrimes() {
        try {
            List<Crime> crimes = crimeService.getCrimesByPrisoner(prisoner.getId().toString());
            crimesTable.getItems().setAll(crimes);
        } catch (Exception e) {
            System.err.println("Error loading crimes: " + e.getMessage());
        }
    }

    private void loadVisitorLogs() {
        try {
            List<VisitorLog> visitorLogs = visitorLogService.getVisitorLogsByPrisoner(prisoner.getId().toString());
            visitorLogsTable.getItems().setAll(visitorLogs);
        } catch (Exception e) {
            System.err.println("Error loading visitor logs: " + e.getMessage());
        }
    }

    private void loadMedicalRecords() {
        try {
            System.out.println("Loading medical records for prisoner ID: " + prisoner.getId());
            List<MedicalRecord> medicalRecords = medicalRecordService.getMedicalRecordsByPrisonerId(prisoner.getId().toString());
            System.out.println("Found " + medicalRecords.size() + " medical records");
            
            if (medicalRecords.isEmpty()) {
                System.out.println("No medical records found for prisoner: " + prisoner.getName());
            } else {
                for (MedicalRecord record : medicalRecords) {
                    System.out.println("Medical record: " + record.getDiagnosis() + " - " + record.getStatus());
                }
            }
            
            medicalRecordsTable.getItems().setAll(medicalRecords);
        } catch (Exception e) {
            System.err.println("Error loading medical records: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditDetails() {
        try {
            EditPrisonerController controller = springFXMLLoader.loadAndGetController("/fxml/edit-prisoner.fxml", EditPrisonerController.class);
            controller.setPrisoner(prisoner);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Prisoner Details");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(root.getScene().getWindow());
            
            Scene scene = new Scene(controller.getRoot());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
            updateLabels();
            loadData();
        } catch (Exception e) {
            System.err.println("Error loading edit prisoner dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleAddCrime() {
        try {
            AddCrimeController controller = springFXMLLoader.loadAndGetController("/fxml/add-crime.fxml", AddCrimeController.class);
            controller.setPrisoner(prisoner);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Crime");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(root.getScene().getWindow());
            Scene scene = new Scene(controller.getRoot());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            loadCrimes();
        } catch (Exception e) {
            System.err.println("Error loading add crime dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddVisit() {
        try {
            AddVisitorLogController controller = springFXMLLoader.loadAndGetController("/fxml/add-visitor-log.fxml", AddVisitorLogController.class);
            controller.setPrisoner(prisoner);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Visitor Log");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(root.getScene().getWindow());
            Scene scene = new Scene(controller.getRoot());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            loadVisitorLogs();
        } catch (Exception e) {
            System.err.println("Error loading add visitor log dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddMedicalRecord() {
        try {
            AddMedicalRecordController controller = springFXMLLoader.loadAndGetController("/fxml/add-medical-record.fxml", AddMedicalRecordController.class);
            controller.setPrisoner(prisoner);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Medical Record");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(root.getScene().getWindow());
            Scene scene = new Scene(controller.getRoot());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            loadMedicalRecords();
        } catch (Exception e) {
            System.err.println("Error loading add medical record dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateReport() {
        // TODO: Implement report generation
        System.out.println("Generating report for prisoner: " + prisoner.getName());
    }

    @FXML
    private void handleExportData() {
        // TODO: Implement data export
        System.out.println("Exporting data for prisoner: " + prisoner.getName());
    }

    @FXML
    private void handlePrintDetails() {
        // TODO: Implement printing
        System.out.println("Printing details for prisoner: " + prisoner.getName());
    }

    // --- Visitor Log Edit/Delete ---
    private void handleEditVisitorLog(VisitorLog log) {
        try {
            AddVisitorLogController controller = springFXMLLoader.loadAndGetController("/fxml/add-visitor-log.fxml", AddVisitorLogController.class);
            controller.setPrisoner(prisoner);
            controller.setVisitorLog(log);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Visitor Log");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(root.getScene().getWindow());
            Scene scene = new Scene(controller.getRoot());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            loadVisitorLogs();
        } catch (Exception e) {
            System.err.println("Error loading edit visitor log dialog: " + e.getMessage());
        }
    }
    private void handleDeleteVisitorLog(VisitorLog log) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this visitor log?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Delete Visitor Log");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                visitorLogService.deleteVisitorLog(log.getId());
                loadVisitorLogs();
            }
        });
    }

    // --- Medical Record Edit/Delete ---
    private void handleEditMedicalRecord(MedicalRecord record) {
        try {
            AddMedicalRecordController controller = springFXMLLoader.loadAndGetController("/fxml/add-medical-record.fxml", AddMedicalRecordController.class);
            controller.setPrisoner(prisoner);
            controller.setMedicalRecord(record);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Medical Record");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(root.getScene().getWindow());
            Scene scene = new Scene(controller.getRoot());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            loadMedicalRecords();
        } catch (Exception e) {
            System.err.println("Error loading edit medical record dialog: " + e.getMessage());
        }
    }
    private void handleDeleteMedicalRecord(MedicalRecord record) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this medical record?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Delete Medical Record");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                medicalRecordService.deleteMedicalRecord(record.getId());
                loadMedicalRecords();
            }
        });
    }

    // --- Crime Delete ---
    private void handleDeleteCrime(Crime crime) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this crime?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Delete Crime");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                crimeService.deleteCrime(crime.getId());
                loadCrimes();
            }
        });
    }
}