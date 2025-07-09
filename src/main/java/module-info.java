module com.cse.buet.messagingapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jdk.jdi;


    opens com.messagingapplication to javafx.fxml;
    exports com.messagingapplication;
}