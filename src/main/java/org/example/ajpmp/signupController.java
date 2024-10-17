package org.example.ajpmp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;





import java.io.IOException;

public class signupController {
    @FXML
    TextField nameTextfield;
    private Stage stage;
    private Parent parent;
    private Scene scene;

    @FXML
    public void switchLogin(MouseEvent e) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login.fxml"));
        parent = fxmlLoader.load();
        stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        scene = new Scene(parent);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void switchToDashboard(ActionEvent e) throws IOException {
        String username = nameTextfield.getText();

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("dashboard.fxml"));
        parent = fxmlLoader.load(); // Load the FXML into the root
        dashController dash = fxmlLoader.getController();
        dash.displayName(username);
        stage = (Stage) ((Node) e.getSource()).getScene().getWindow(); // Get the stage
        scene = new Scene(parent); // Create a new scene with the root
        stage.setScene(scene); // Set the scene to the stage
        stage.show(); // Show the stage
    }

}
