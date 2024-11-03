package org.example.ajpmp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class loginController {

    @FXML
    private TextField usernametf;
    @FXML
    private TextField passwordtf;

    private Stage stage;
    private Scene scene;
    private Parent parent;

    @FXML
    public void login(ActionEvent event) {
        // Test database connection first
        if (!DatabaseUtil.testConnection()) {
            showAlert("Database Error", "Could not connect to database. Please check if XAMPP is running.", Alert.AlertType.ERROR);
            return;
        }

        String username = usernametf.getText().trim();
        String password = passwordtf.getText().trim();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Warning", "Please fill in all fields", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            if (conn == null) {
                showAlert("Error", "Database connection failed", Alert.AlertType.ERROR);
                return;
            }

            String query = "SELECT id, username FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                
                System.out.println("Executing query for username: " + username); // Debug line
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int userId = rs.getInt("id");
                        Session.setUserId(userId);
                        System.out.println("Login successful for user: " + username + " (ID: " + userId + ")");
                        
                        // Add these debug lines
                        System.out.println("Attempting to load dashboard.fxml");
                        System.out.println("Resource URL: " + Main.class.getResource("dashboard.fxml"));


                        // Load dashboard
                        FXMLLoader loader = new FXMLLoader(Main.class.getResource("dashboard.fxml"));   
                        parent = loader.load();

                        System.out.println("Dashboard FXML loaded successfully");

                        dashController dashController = loader.getController();

                        System.out.println("Dashboard controller obtained");
                        
                        dashController.displayName(username);
                        
                        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                        scene = new Scene(parent);
                        stage.setScene(scene);
                        stage.show();
                    } else {
                        showAlert("Login Failed", "Invalid username or password", Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
            showAlert("Database Error", "Error: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (IOException e) {
            System.out.println("FXML loading error: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not load dashboard", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void switchSignup(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("signup.fxml"));
            parent = loader.load();
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(parent);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load signup page", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}