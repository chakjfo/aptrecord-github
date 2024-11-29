module com.example.aptrecord {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    opens com.example.aptrecord to javafx.fxml;
    opens com.example.aptrecord.Controller to javafx.fxml;
    exports com.example.aptrecord;
    exports com.example.aptrecord.Controller;
}
