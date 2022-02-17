module server.tz {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires postgis.geometry;
    requires java.desktop;


    opens server.tz to javafx.fxml;
    exports server.tz;
}