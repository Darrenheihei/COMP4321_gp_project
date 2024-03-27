module com.example.comp4321_gp_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires htmlparser;
    requires jdbm;


    opens com.example.comp4321_gp_project to javafx.fxml;
    exports Project;
}