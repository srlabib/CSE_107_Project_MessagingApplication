module com.cse.buet.messagingapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jdk.jdi;
    requires opencv;

    opens com.messagingapplication.VideoCall to javafx.graphics, javafx.fxml;
    exports com.messagingapplication;
    opens com.messagingapplication to javafx.fxml, javafx.graphics;
}