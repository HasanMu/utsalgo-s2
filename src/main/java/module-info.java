module com.algo.uts {
    requires javafx.controls;
    requires javafx.fxml;
    

    opens com.algo.uts to javafx.fxml;
    exports com.algo.uts;
    
}
