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

public class loginController {

    private Stage stage;
    private Parent parent;
    private Scene scene;



    @FXML
    public void switchToDashboard(ActionEvent e) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("dashboard.fxml"));
        parent = fxmlLoader.load();  // Load the FXML into the root



        stage = (Stage) ((Node) e.getSource()).getScene().getWindow();  // Get the stage
        scene = new Scene(parent);  // Create a new scene with the root
        stage.setScene(scene);  // Set the scene to the stage
        stage.show();  // Show the stage
    }

    public void switchSignup(MouseEvent e) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("signup.fxml"));
        parent = fxmlLoader.load(); // Load the FXML into the root
        stage = (Stage) ((Node) e.getSource()).getScene().getWindow(); // Get the stage
        scene = new Scene(parent); // Create a new scene with the root
        stage.setScene(scene); // Set the scene to the stage
        stage.show(); // Show the stage
    }

}
