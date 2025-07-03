module com.cse.buet.messagingapplication {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.cse.buet.messagingapplication to javafx.fxml;
    exports com.cse.buet.messagingapplication;
}