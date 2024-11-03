package org.example.ajpmp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Optional;

public class dashController {
    @FXML private Button signout;
    @FXML private AnchorPane dashMain;
    @FXML private Label nameLabel;
    @FXML private Pane tasksPanel, importantPanel, completedPanel, taskDetailPanel;
    @FXML private VBox taskContainer, impTaskContainer, completedTaskContainer;
    @FXML private TextField taskip;
    @FXML private Label taskName;
    @FXML private TextArea taskDescription;
    @FXML private CheckBox impCheckbox, completedCheckbox;
    @FXML private ImageView closebtn;
    @FXML private Label taskLabel, impTaskLabel, compTaskLabel;
    @FXML private Pane color1, color2, color3, color4, color5, color6;
    @FXML private Button deleteButton;

    private Stage stage;
    private int currentUserId;
    private String currentTask;
    private final HashMap<String, TaskData> taskMap = new HashMap<>();

    private static final String DEFAULT_STYLE = 
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #333333; " +
            "-fx-padding: 5px; " +
            "-fx-background-color: #E6DED1; " +
            "-fx-border-color: #7f5539; " +
            "-fx-border-radius: 5px; " +
            "-fx-background-radius: 5px;" +
            "-fx-cursor: hand;";

    private static class TaskData {
        int taskId;
        Label mainLabel;
        Label importantLabel;
        Label completedLabel;
        boolean isImportant;
        boolean isCompleted;
        String currentColor = "#E6DED1";
        String description = "";

        TaskData(int taskId, Label mainLabel) {
            this.taskId = taskId;
            this.mainLabel = mainLabel;
        }
    }

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            try {
                initializeDatabase();
                initializeUser();
                initializeUI();
                loadUserTasks();
                initializeColorPanels();
            } catch (Exception e) {
                handleError("Initialization Error", "Failed to initialize dashboard: " + e.getMessage());
            }
        });
    }

    private void initializeDatabase() {
        String sql = "ALTER TABLE tasks ADD COLUMN IF NOT EXISTS description TEXT";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            handleError("Database Error", "Failed to initialize database: " + e.getMessage());
        }
    }

    private void initializeUser() {
        currentUserId = Session.getUserId();
        if (currentUserId == 0) {
            handleError("Session Error", "No valid user session found");
            Platform.exit();
        }
    }

    private void initializeUI() {
        taskip.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addTask();
            }
        });

        closebtn.setOnMouseClicked(event -> closeDetailPanel());
        
        if (deleteButton != null) {
            deleteButton.setOnAction(event -> handleDelete());
        }

        tasksPanel.setVisible(true);
        importantPanel.setVisible(false);
        completedPanel.setVisible(false);
        taskDetailPanel.setVisible(false);
    }

    @FXML
    public void displayName(String username) {
        nameLabel.setText(username);
    }

    private void loadUserTasks() {
        String sql = "SELECT id, task_name, is_important, is_completed, description FROM tasks WHERE user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int taskId = rs.getInt("id");
                String taskName = rs.getString("task_name");
                boolean isImportant = rs.getBoolean("is_important");
                boolean isCompleted = rs.getBoolean("is_completed");
                String description = rs.getString("description");

                createTaskUI(taskId, taskName, isImportant, isCompleted, description);
            }
        } catch (SQLException e) {
            handleError("Database Error", "Failed to load tasks: " + e.getMessage());
        }
    }

    private void createTaskUI(int taskId, String taskName, boolean isImportant, boolean isCompleted, String description) {
        Label mainLabel = createTaskLabel(taskName);
        TaskData taskData = new TaskData(taskId, mainLabel);
        taskData.description = description;
        taskMap.put(taskName, taskData);

        if (isImportant) {
            addToImportantPanel(taskName);
        }
        if (isCompleted) {
            addToCompletedPanel(taskName);
        }

        taskContainer.getChildren().add(mainLabel);
    }

    private Label createTaskLabel(String text) {
        Label label = new Label(text);
        label.setStyle(DEFAULT_STYLE);
        label.setPrefWidth(500.0);
        label.setPrefHeight(50.0);
        label.setOnMouseClicked(event -> openTaskDetail(text));
        return label;
    }

    @FXML
    private void addTask() {
        String taskText = taskip.getText().trim();
        if (taskText.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO tasks (user_id, task_name, is_important, is_completed, description) VALUES (?, ?, false, false, '')";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, taskText);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int taskId = generatedKeys.getInt(1);
                        createTaskUI(taskId, taskText, false, false, "");
                        taskip.clear();
                    }
                }
            }
        } catch (SQLException e) {
            handleError("Database Error", "Failed to save task: " + e.getMessage());
        }
    }

    private void openTaskDetail(String taskName) {
        currentTask = taskName;
        TaskData taskData = taskMap.get(taskName);
        
        if (taskData != null) {
            this.taskName.setText(taskName);
            if (taskDescription != null) {
                taskDescription.setText(taskData.description);
            }
            impCheckbox.setSelected(taskData.isImportant);
            completedCheckbox.setSelected(taskData.isCompleted);
            taskDetailPanel.setVisible(true);
            
            taskDetailPanel.setStyle("-fx-background-color: " + taskData.currentColor + ";");
        }
    }

    @FXML
    private void handleSave() {
        TaskData taskData = taskMap.get(currentTask);
        if (taskData == null) return;

        boolean newImportantState = impCheckbox.isSelected();
        boolean newCompletedState = completedCheckbox.isSelected();
        String newDescription = taskDescription != null ? taskDescription.getText() : "";

        String sql = "UPDATE tasks SET is_important = ?, is_completed = ?, description = ? WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, newImportantState);
            pstmt.setBoolean(2, newCompletedState);
            pstmt.setString(3, newDescription);
            pstmt.setInt(4, taskData.taskId);
            pstmt.setInt(5, currentUserId);
            
            if (pstmt.executeUpdate() > 0) {
                taskData.description = newDescription;
                updateTaskStatus(currentTask, newImportantState, newCompletedState);
            }
        } catch (SQLException e) {
            handleError("Database Error", "Failed to update task: " + e.getMessage());
        }

        taskDetailPanel.setVisible(false);
    }

    @FXML
    private void handleDelete() {
        if (currentTask == null) return;
        
        Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDelete.setTitle("Delete Task");
        confirmDelete.setHeaderText(null);
        confirmDelete.setContentText("Are you sure you want to delete this task?");
        
        Optional<ButtonType> result = confirmDelete.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM tasks WHERE id = ? AND user_id = ?";
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                TaskData taskData = taskMap.get(currentTask);
                if (taskData == null) return;
                
                pstmt.setInt(1, taskData.taskId);
                pstmt.setInt(2, currentUserId);
                
                if (pstmt.executeUpdate() > 0) {
                    taskContainer.getChildren().remove(taskData.mainLabel);
                    if (taskData.importantLabel != null) {
                        impTaskContainer.getChildren().remove(taskData.importantLabel);
                    }
                    if (taskData.completedLabel != null) {
                        completedTaskContainer.getChildren().remove(taskData.completedLabel);
                    }
                    
                    taskMap.remove(currentTask);
                    closeDetailPanel();
                }
            } catch (SQLException e) {
                handleError("Database Error", "Failed to delete task: " + e.getMessage());
            }
        }
    }

    private void updateTaskStatus(String taskName, boolean isImportant, boolean isCompleted) {
        TaskData taskData = taskMap.get(taskName);
        if (taskData == null) return;

        if (isImportant != taskData.isImportant) {
            if (isImportant) {
                addToImportantPanel(taskName);
            } else {
                removeFromImportantPanel(taskName);
            }
            taskData.isImportant = isImportant;
        }

        if (isCompleted != taskData.isCompleted) {
            if (isCompleted) {
                addToCompletedPanel(taskName);
            } else {
                removeFromCompletedPanel(taskName);
            }
            taskData.isCompleted = isCompleted;
        }
    }

    private void addToImportantPanel(String taskName) {
        TaskData taskData = taskMap.get(taskName);
        if (taskData != null && taskData.importantLabel == null) {
            Label impLabel = createTaskLabel(taskName);
            impLabel.setStyle(taskData.mainLabel.getStyle());
            taskData.importantLabel = impLabel;
            impTaskContainer.getChildren().add(impLabel);
        }
    }

    private void addToCompletedPanel(String taskName) {
        TaskData taskData = taskMap.get(taskName);
        if (taskData != null && taskData.completedLabel == null) {
            Label compLabel = createTaskLabel(taskName);
            compLabel.setStyle(taskData.mainLabel.getStyle());
            taskData.completedLabel = compLabel;
            completedTaskContainer.getChildren().add(compLabel);
        }
    }

    private void removeFromImportantPanel(String taskName) {
        TaskData taskData = taskMap.get(taskName);
        if (taskData != null && taskData.importantLabel != null) {
            impTaskContainer.getChildren().remove(taskData.importantLabel);
            taskData.importantLabel = null;
        }
    }

    private void removeFromCompletedPanel(String taskName) {
        TaskData taskData = taskMap.get(taskName);
        if (taskData != null && taskData.completedLabel != null) {
            completedTaskContainer.getChildren().remove(taskData.completedLabel);
            taskData.completedLabel = null;
        }
    }

    private void initializeColorPanels() {
        color1.setOnMouseClicked(this::handleColorClick);
        color2.setOnMouseClicked(this::handleColorClick);
        color3.setOnMouseClicked(this::handleColorClick);
        color4.setOnMouseClicked(this::handleColorClick);
        color5.setOnMouseClicked(this::handleColorClick);
        color6.setOnMouseClicked(this::handleColorClick);
    }

    @FXML
    private void handleColorClick(javafx.scene.input.MouseEvent event) {
        Pane colorPane = (Pane) event.getSource();
        String color = colorPane.getStyle()
            .replace("-fx-background-color: ", "")
            .replace(";", "")
            .replace("-fx-background-radius: 50", "")
            .replace("-fx-border-radius: 50", "")
            .trim();
        changeBgColor(color);
    }

    private void changeBgColor(String color) {
        if (currentTask != null) {
            TaskData taskData = taskMap.get(currentTask);
            if (taskData != null) {
                taskData.currentColor = color;
                
                updateLabelColor(taskData.mainLabel, color);
                if (taskData.importantLabel != null) {
                    updateLabelColor(taskData.importantLabel, color);
                }
                if (taskData.completedLabel != null) {
                    updateLabelColor(taskData.completedLabel, color);
                }
                
                taskDetailPanel.setStyle("-fx-background-color: " + color + ";");
            }
        }
    }

    private void updateLabelColor(Label label, String color) {
        String style = DEFAULT_STYLE + "; -fx-background-color: " + color + ";";
        label.setStyle(style);
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

                FXMLLoader loader = new FXMLLoader(getClass().getResource("signup.fxml"));
                Parent root = loader.load();

                Stage signUpStage = new Stage();
                signUpStage.setTitle("Sign Up");
                signUpStage.setScene(new Scene(root));
                signUpStage.show();
            } catch (IOException e) {
                handleError("Navigation Error", "Failed to load signup page: " + e.getMessage());
            }
        }
    }

    private void handleError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(message);
            alert.showAndWait();
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
    public void closeDetailPanel() {
        taskDetailPanel.setVisible(false);
    }
}