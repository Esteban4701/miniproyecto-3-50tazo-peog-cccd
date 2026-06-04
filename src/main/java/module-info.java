module com.example._0tazo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example._0tazo to javafx.fxml;
    exports com.example._0tazo;
}