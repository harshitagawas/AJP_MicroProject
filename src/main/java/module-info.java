module org.example.ajpmp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires mysql.connector.j;
    requires java.sql.rowset;

    opens org.example.ajpmp to javafx.fxml;
    exports org.example.ajpmp;
}
