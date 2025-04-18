module com.example.vehiclehiresystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;


    opens com.example.vehiclehiresystem to javafx.fxml;
    exports com.example.vehiclehiresystem;
}