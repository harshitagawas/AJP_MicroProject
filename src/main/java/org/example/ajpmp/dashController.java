package org.example.ajpmp;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.util.HashMap;

public class dashController {
    @FXML
    private Button signout;

    @FXML
    private AnchorPane dashMain;

    Stage stage;

    @FXML
    Label nameLabel;

    @FXML
    private Pane tasksPanel;

    @FXML
    private Pane importantPanel;

    @FXML
    private Pane completedPanel;

    @FXML
    private VBox taskContainer;

    @FXML
    private TextField taskip;

    @FXML
    private Pane taskDetailPanel;

    @FXML
    private Label taskName;

    @FXML
    private CheckBox impCheckbox;

    @FXML
    private VBox impTaskContainer;

    @FXML
    private CheckBox completedCheckbox;

    @FXML
    private VBox completedTaskContainer;

    @FXML
    private ImageView closebtn;

    @FXML
    public Label taskLabel;
    @FXML
    public Label impTaskLabel;
    @FXML
    public Label compTaskLabel;

    @FXML
    private Pane color1, color2, color3, color4, color5, color6;

    private String currentTask;  // Store the current task being worked on
    private HashMap<String, Label> importantTasksMap = new HashMap<>();  // Store important tasks for easy removal
    private HashMap<String, Label> completedTasksMap = new HashMap<>();

    @FXML
    public void displayName(String username) {
        nameLabel.setText(username);
    }

    @FXML
    public void initialize() {
        tasksPanel.setVisible(false);
        importantPanel.setVisible(false);
        completedPanel.setVisible(false);
        taskDetailPanel.setVisible(false);

        taskip.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addTasks();
            }
        });
    }

    @FXML
    public void displayTaskPanel() {
        tasksPanel.setVisible(true);
        importantPanel.setVisible(false);
        completedPanel.setVisible(false);
    }

    @FXML
    public void displayImpPanel() {
        importantPanel.setVisible(true);
        tasksPanel.setVisible(false);
        completedPanel.setVisible(false);
    }

    @FXML
    public void displayCompPanel() {
        importantPanel.setVisible(false);
        tasksPanel.setVisible(false);
        completedPanel.setVisible(true);
    }

    @FXML
    public void addTasks() {
        String task = taskip.getText();
        if (!task.isEmpty()) {
            taskLabel = new Label(task);
            taskLabel.setStyle(
                    "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: #333333; " +
                            "-fx-padding: 5px; " +
                            "-fx-background-color:  #E6DED1; " +
                            "-fx-border-color: #7f5539; " +
                            "-fx-border-radius: 5px; " +
                            "-fx-background-radius: 5px;" +
                            "-fx-cursor: hand;"
            );

            taskLabel.setPrefWidth(500.0);
            taskLabel.setPrefHeight(50.0);

            taskLabel.setOnMouseClicked(event -> openTaskDetailPanel(task));
            taskContainer.getChildren().add(taskLabel);
            taskip.clear();
        }
    }

    @FXML
    public void openTaskDetailPanel(String task) {
        currentTask = task;
        taskDetailPanel.setVisible(true);
        taskName.setText(task);

        // Check if the task is already marked important
        impCheckbox.setSelected(importantTasksMap.containsKey(task));
        // Check if the task is already marked completed
        completedCheckbox.setSelected(completedTasksMap.containsKey(task));
    }

    @FXML
    public void handleSave() {
        taskDetailPanel.setVisible(false);

        // Handle important task checkbox
        if (impCheckbox.isSelected()) {
            if (!importantTasksMap.containsKey(currentTask)) {
                addToImpPanel(currentTask);
            }
        } else {
            if (importantTasksMap.containsKey(currentTask)) {
                removeFromImpPanel(currentTask);
            }
        }

        // Handle completed task checkbox
        if (completedCheckbox.isSelected()) {
            if (!completedTasksMap.containsKey(currentTask)) {
                addToCompPanel(currentTask);
            }
        } else {
            if (completedTasksMap.containsKey(currentTask)) {
                removeFromCompPanel(currentTask);
            }
        }


    }

    @FXML
    public void addToImpPanel(String task) {
        impTaskLabel = new Label(task);

        impTaskLabel.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-padding: 5px; " +
                        "-fx-background-color:  #E6DED1; " +
                        "-fx-border-color: #7f5539; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px;" +
                        "-fx-cursor: hand;"
        );

        impTaskLabel.setPrefWidth(500.0);
        impTaskLabel.setPrefHeight(50.0);
        impTaskLabel.setOnMouseClicked(event -> openTaskDetailPanel(task));
        impTaskContainer.getChildren().add(impTaskLabel);
        importantTasksMap.put(task, impTaskLabel);  // Add the task to the important tasks map
    }

    @FXML
    public void removeFromImpPanel(String task) {
        Label impTaskLabel = importantTasksMap.get(task);
        if (impTaskLabel != null) {
            impTaskContainer.getChildren().remove(impTaskLabel);
            importantTasksMap.remove(task);  // Remove the task from the important tasks map
        }
    }

    @FXML
    public void addToCompPanel(String task) {
        compTaskLabel = new Label(task);

        compTaskLabel.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-padding: 5px; " +
                        "-fx-background-color:  #E6DED1; " +
                        "-fx-border-color: #7f5539; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px;" +
                        "-fx-cursor: hand;"
        );

        compTaskLabel.setPrefWidth(500.0);
        compTaskLabel.setPrefHeight(50.0);

        compTaskLabel.setOnMouseClicked(event -> openTaskDetailPanel(task));
        completedTaskContainer.getChildren().add(compTaskLabel);
        completedTasksMap.put(task, compTaskLabel);  // Add the task to the completed tasks map
    }

    @FXML
    public void removeFromCompPanel(String task) {
        Label compTaskLabel = completedTasksMap.get(task);
        if (compTaskLabel != null) {
            completedTaskContainer.getChildren().remove(compTaskLabel);  // Remove the task label from the VBox
            completedTasksMap.remove(task);  // Remove the task from the completed tasks map
        }
    }

    @FXML
    public void signOut() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sign Out");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to sign out?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                stage = (Stage) dashMain.getScene().getWindow();
                stage.close();

                // Load the sign-up page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("signup.fxml"));
                Parent root = loader.load();

                Stage signUpStage = new Stage();
                signUpStage.setTitle("Sign Up");
                signUpStage.setScene(new Scene(root));
                signUpStage.show();  // Show the new sign-up page
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void detailPanelClose() {
        closebtn.setOnMouseClicked(event -> {
            taskDetailPanel.setVisible(false);
        });
    }

    // Color changing logic
    @FXML
    public void detailPanelBg() {
        Pane[] colorPanel = {color1, color2, color3, color4, color5, color6};
        String[] colors = {"#b8d8e3", "#e9db0c", "#535878", "#f1b5b5", "#F28482", "#BB7B85"};

        for (int i = 0; i < colorPanel.length; i++) {
            int index = i;
            colorPanel[index].setOnMouseClicked(event -> changeBgColor(colors[index]));
        }
    }

    private void changeBgColor(String color) {
        Label[] labels = {taskLabel, impTaskLabel, compTaskLabel};
        for (Label label : labels) {
            if (label != null) {
                label.setStyle(label.getStyle() + "; -fx-background-color: " + color + ";");
            }
        }
        taskDetailPanel.setStyle("-fx-background-color: " + color + ";");
    }
}

