module com.example._0tazo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.junit.jupiter.api;

    opens com.example._0tazo to javafx.fxml;
    opens com.example._0tazo.controller to javafx.fxml;
    opens com.example._0tazo.model to javafx.fxml, org.junit.platform.commons;
    opens com.example._0tazo.model.exception to javafx.fxml, org.junit.platform.commons;
    exports com.example._0tazo;
}