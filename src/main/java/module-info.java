module com.ft {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.ft to javafx.fxml;
    exports com.ft;
}
