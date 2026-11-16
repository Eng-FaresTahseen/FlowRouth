module com.ft {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.ft to javafx.fxml;
    exports com.ft;
}
