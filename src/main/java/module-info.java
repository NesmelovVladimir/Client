module server.tz {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.fasterxml.jackson.databind;


    opens server.tz to javafx.fxml;
    exports server.tz;
}