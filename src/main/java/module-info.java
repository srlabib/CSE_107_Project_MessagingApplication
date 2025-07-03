module com.cse.buet.messagingapplication {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.messagingapplication to javafx.fxml;
    exports com.messagingapplication;
}