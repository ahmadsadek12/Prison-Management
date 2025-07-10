package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.example.config.SpringFXMLLoader;
import java.util.logging.Logger;
import java.util.logging.Level;

public class JavaFXApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger(JavaFXApplication.class.getName());
    private ConfigurableApplicationContext applicationContext;
    private SpringFXMLLoader springFXMLLoader;

    @Override
    public void init() {
        LOGGER.info("Initializing JavaFX Application...");
        try {
            applicationContext = new SpringApplicationBuilder(PrisonManagementApplication.class)
                .headless(false)
                .run();
            LOGGER.info("Spring Application Context initialized successfully");
            
            springFXMLLoader = applicationContext.getBean(SpringFXMLLoader.class);
            if (springFXMLLoader == null) {
                throw new IllegalStateException("Failed to retrieve SpringFXMLLoader bean");
            }
            LOGGER.info("SpringFXMLLoader bean retrieved successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during initialization", e);
            Platform.exit();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        LOGGER.info("Starting JavaFX Application...");
        try {
            LOGGER.info("Loading dashboard.fxml...");
            Parent root = springFXMLLoader.load("/fxml/dashboard.fxml");
            LOGGER.info("FXML loaded successfully");

            Scene scene = new Scene(root);
            scene.getStylesheets().add("/styles/dashboard.css");
            LOGGER.info("Scene created and stylesheet added");

            primaryStage.setTitle("Prison Management System");
            primaryStage.setScene(scene);
            primaryStage.show();
            LOGGER.info("Primary stage shown");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting application", e);
            displayError("Application Error", "Failed to start application", e.getMessage());
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping JavaFX Application...");
        if (applicationContext != null) {
            applicationContext.close();
        }
    }

    private void displayError(String title, String header, String content) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
