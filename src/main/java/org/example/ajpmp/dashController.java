package org.example.ajpmp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Optional;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.StackPane;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;

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
    @FXML private Pane calendarPanel;
    @FXML private GridPane calendarGrid;
    @FXML private Label monthYearLabel;
    @FXML private Label calendarLabel;
    @FXML private GridPane weekDaysHeader;
    @FXML private Button prevMonth;
    @FXML private Button nextMonth;
    @FXML private Button todayButton;

    private LocalDate currentDate = LocalDate.now();
    private YearMonth currentYearMonth;
    private DateTimeFormatter monthYearFormat = DateTimeFormatter.ofPattern("MMMM yyyy");  
    private Calendar calendar = Calendar.getInstance();

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
                calendarLabel.setOnMouseClicked(event -> displayCalendarPanel());

            } catch (Exception e) {
                handleError("Initialization Error", "Failed to initialize dashboard: " + e.getMessage());
            }
        });
    }

    // Database methods

    private void initializeDatabase() {
        String sql = "ALTER TABLE tasks ADD COLUMN IF NOT EXISTS description TEXT";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            handleError("Database Error", "Failed to initialize database: " + e.getMessage());
        }
    }

    // User methods

    private void initializeUser() {
        currentUserId = Session.getUserId();
        if (currentUserId == 0) {
            handleError("Session Error", "No valid user session found");
            Platform.exit();
        }
    }

    // UI methods
    
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

    // Task methods

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

    // Task detail methods

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

    // Color panel methods

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

    // Task panel methods

    @FXML
    public void displayTaskPanel() {
        tasksPanel.setVisible(true);
        importantPanel.setVisible(false);
        completedPanel.setVisible(false);
        calendarPanel.setVisible(false);
    }

    @FXML
    public void displayImpPanel() {
        importantPanel.setVisible(true);
        tasksPanel.setVisible(false);
        completedPanel.setVisible(false);
        calendarPanel.setVisible(false);
    }

    @FXML
    public void displayCompPanel() {
        importantPanel.setVisible(false);
        tasksPanel.setVisible(false);
        completedPanel.setVisible(true);
        calendarPanel.setVisible(false);
    }

    @FXML
    public void closeDetailPanel() {
        taskDetailPanel.setVisible(false);
    }

    @FXML
    private void displayCalendarPanel() {
    tasksPanel.setVisible(false);
    importantPanel.setVisible(false);
    completedPanel.setVisible(false);
    calendarPanel.setVisible(true);
    taskDetailPanel.setVisible(false);
    }

    // Calendar methods
    
    @FXML
    private void initializeCalendar() {
        currentYearMonth = YearMonth.from(currentDate);
        setupWeekDaysHeader();
        updateCalendar();
    }

        private void setupWeekDaysHeader() {
        String[] weekDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(weekDays[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #AB8C76;");
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setTextAlignment(TextAlignment.CENTER);
            dayLabel.setPrefWidth(70);
            weekDaysHeader.add(dayLabel, i, 0);
        }
    }

    @FXML
    private void handlePrevMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }

    @FXML
    private void goToToday() {
        currentYearMonth = YearMonth.from(LocalDate.now());
        updateCalendar();
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();
        
        monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        // Get the first day of the month
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        
        // Get the number of days in the month
        int daysInMonth = currentYearMonth.lengthOfMonth();

        int row = 0;
        int column = dayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            StackPane dayCell = createDayCell(day, date);
            
            if (column > 6) {
                column = 0;
                row++;
            }
            
            calendarGrid.add(dayCell, column++, row);
        }
    }

    private StackPane createDayCell(int day, LocalDate date) {
        StackPane dayCell = new StackPane();
        dayCell.setPrefSize(70, 70);
        
        VBox content = new VBox();
        content.setAlignment(Pos.CENTER);
        
        Label dayLabel = new Label(String.valueOf(day));
        dayLabel.setStyle("-fx-font-size: 16px;");
        
        content.getChildren().add(dayLabel);
        
        // Style for the day cell
        String baseStyle = "-fx-background-radius: 5; -fx-padding: 5; -fx-cursor: hand;";
        
        if (date.equals(LocalDate.now())) {
            // Today's date style
            dayCell.setStyle(baseStyle + "-fx-background-color: #AB8C76; -fx-opacity: 0.7;");
            dayLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            // Regular day style
            dayCell.setStyle(baseStyle + "-fx-background-color: #F0F0F0;");
        }
        
        // Hover effect
        dayCell.setOnMouseEntered(e -> {
            if (!date.equals(LocalDate.now())) {
                dayCell.setStyle(baseStyle + "-fx-background-color: #E6DED1;");
            }
        });
        
        dayCell.setOnMouseExited(e -> {
            if (!date.equals(LocalDate.now())) {
                dayCell.setStyle(baseStyle + "-fx-background-color: #F0F0F0;");
            }
        });
        
        // Click handler
        dayCell.setOnMouseClicked(e -> {
            showDateSelectedAlert(date);
        });
        
        dayCell.getChildren().add(content);
        return dayCell;
    }

    private void showDateSelectedAlert(LocalDate date) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Date Selected");
        alert.setHeaderText(null);
        alert.setContentText("You have selected: " + 
            date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        
        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #F0F0F0;" +
            "-fx-border-color: #AB8C76;" +
            "-fx-border-width: 2px;"
        );
        
        alert.show();
    }
}