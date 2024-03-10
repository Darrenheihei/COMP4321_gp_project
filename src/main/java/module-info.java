module com.example.comp4321_gp_project {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.comp4321_gp_project to javafx.fxml;
    exports com.example.comp4321_gp_project;
}