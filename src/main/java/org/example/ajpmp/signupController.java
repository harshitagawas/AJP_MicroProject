package org.example.ajpmp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class signupController {
    @FXML
    private TextField nameTextfield;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField passwordTextField;
    
    private Stage stage;
    private Parent parent;
    private Scene scene;

    @FXML
    public void switchLogin(MouseEvent e) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/ajpmp/login.fxml"));
        Parent parent = fxmlLoader.load();
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.show();
        System.out.println("Switched to login");
    }

    @FXML
    public void switchToDashboard(ActionEvent e) throws IOException {
        String username = nameTextfield.getText().trim();
        String email = emailTextField.getText().trim();
        String password = passwordTextField.getText().trim();

        // Check if any field is empty
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            // Show an alert
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty Fields");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all fields before continuing.");
            alert.showAndWait();
            return; // Exit the method to prevent further execution
        }

        // Insert user data into the database
        try (Connection connection = DatabaseUtil.getConnection()) {
            String query = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, password);
            preparedStatement.executeUpdate();
            preparedStatement.executeUpdate();
            System.out.println("User created successfully."); // Debug
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Switch to the dashboard scene
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("dashboard.fxml"));
        parent = fxmlLoader.load();
        dashController dash = fxmlLoader.getController();
        dash.displayName(username);
        stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        scene = new Scene(parent);
        stage.setScene(scene);
        stage.show();
    }
}
