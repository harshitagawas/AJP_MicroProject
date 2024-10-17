module org.example.ajpmp {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.ajpmp to javafx.fxml;
    exports org.example.ajpmp;
}