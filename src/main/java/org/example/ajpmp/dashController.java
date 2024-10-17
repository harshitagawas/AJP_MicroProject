package org.example.ajpmp;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;

import javafx.scene.image.ImageView;
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

    private String currentTask;  // Store the current task being worked on
    private HashMap<String, Label> importantTasksMap = new HashMap<>();  // Store important tasks for easy remove
    private HashMap<String, Label> completedTasksMap = new HashMap<>();


    @FXML
    public void displayName(String username){
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
            Label taskLabel = new Label(task);
            taskLabel.setStyle(
                    "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: #333333; " +
                            "-fx-padding: 5px; " +
                            "-fx-background-color:  #E6DED1; " +
                            "-fx-border-color: #7f5539; " +
                            "-fx-border-radius: 5px; " +
                            "-fx-background-radius: 5px"+
                            "-fx-cursor: hand"


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

        // Check if the task is already marked  important
        impCheckbox.setSelected(importantTasksMap.containsKey(task));
        // Check if the task is already marked  completed
        completedCheckbox.setSelected(completedTasksMap.containsKey(task));
    }

    // Handle the Save buttonfrom task detail panel
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

    // Add the task to the important panel
    @FXML
    public void addToImpPanel(String task) {
        Label impTaskLabel = new Label(task);

        impTaskLabel.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-padding: 5px; " +
                        "-fx-background-color:  #E6DED1; " +
                        "-fx-border-color: #7f5539; " +
                        "-fx-border-radius: 5px; " +
                        "-fx-background-radius: 5px"+
                        "-fx-cursor: hand"

                
        );

        impTaskLabel.setPrefWidth(500.0);
        impTaskLabel.setPrefHeight(50.0);
        impTaskLabel.setOnMouseClicked(event -> openTaskDetailPanel(task));
        impTaskContainer.getChildren().add(impTaskLabel);
        importantTasksMap.put(task, impTaskLabel);  // Add the task to the important tasks map
    }

    // Remove the task from the important panel
    @FXML
    public void removeFromImpPanel(String task) {
        Label impTaskLabel = importantTasksMap.get(task);
        if (impTaskLabel != null) {
            impTaskContainer.getChildren().remove(impTaskLabel);
            importantTasksMap.remove(task);  // Remove the task from the important tasks map
        }
    }

    // Add the task to the completed panel
    @FXML
    public void addToCompPanel(String task) {
        Label compTaskLabel = new Label(task);
        compTaskLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333; -fx-padding: 5px; -fx-background-color: #f9f9f9; -fx-border-color: #cccccc; -fx-border-radius: 5px;");
        compTaskLabel.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-padding: 5px; " +
                        "-fx-background-color:  #E6DED1; " +
                        "-fx-border-color: #7f5539; " +
                        "-fx-border-radius: 5px; "+
                        "-fx-background-radius: 5px"+
                        "-fx-cursor: hand"


        );

        compTaskLabel.setPrefWidth(500.0);
        compTaskLabel.setPrefHeight(50.0);

        compTaskLabel.setOnMouseClicked(event -> openTaskDetailPanel(task));
        completedTaskContainer.getChildren().add(compTaskLabel);
        completedTasksMap.put(task, compTaskLabel);  // Add the task to the completed tasks map
    }

    // Remove the task from the completed panel
    @FXML
    public void removeFromCompPanel(String task) {
        Label compTaskLabel = completedTasksMap.get(task);
        if (compTaskLabel != null) {
            completedTaskContainer.getChildren().remove(compTaskLabel);  // Remove the task label from the VBox
            completedTasksMap.remove(task);  // Remove the task from the completed tasks map
        }
    }

    @FXML
    public void signOut(){

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Sign Out");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to sign out?");


        if (alert.showAndWait().get() == ButtonType.OK) {
            // User clicked OK
            try {

                stage = (Stage) dashMain.getScene().getWindow();
                stage.close();

                // Load the sign-up pag
                FXMLLoader loader = new FXMLLoader(getClass().getResource("signup.fxml"));
                Parent root = loader.load();

                Stage signUpStage = new Stage();
                signUpStage.setTitle("Sign Up");
                signUpStage.setScene(new Scene(root));
                signUpStage.show(); // Show the new sign-up page
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


}
