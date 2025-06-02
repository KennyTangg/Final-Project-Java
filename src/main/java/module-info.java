module com.contactsmanager.contactsmanagerfx {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.contactsmanager.contactsmanagerfx to javafx.fxml;
    exports com.contactsmanager.contactsmanagerfx;
}