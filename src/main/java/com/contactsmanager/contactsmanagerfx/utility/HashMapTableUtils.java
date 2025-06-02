package com.contactsmanager.contactsmanagerfx.utility;

import com.contactsmanager.contactsmanagerfx.model.Contact;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

import java.util.Map;

public class HashMapTableUtils {

    /**
     * Sets up columns for a TableView displaying String (as Key), Contact (as Details).
     *
     * @param table The TableView instance to set up.
     */
    @SuppressWarnings("unchecked")
    public static void setup(TableView<?> table) { //The method uses a raw TableView<?> so that it can be filled with whatever type.

        // Cast TableView to the right generic type
        TableView<Map.Entry<String, Contact>> typedTable =
                (TableView<Map.Entry<String, Contact>>) table;

        // Reset to avoid stacking columns from earlier views.
        typedTable.getColumns().clear();

        // Column 1: Key
        TableColumn<Map.Entry<String, Contact>, String> keyCol = new TableColumn<>("Key"); // Create a new column titled "Key"
        keyCol.setCellValueFactory(entry ->  // For every row, get the key
            new SimpleStringProperty(entry.getValue().getKey())
        );
        keyCol.setCellFactory(TextFieldTableCell.forTableColumn());

        // Column 2: Details
        TableColumn<Map.Entry<String, Contact>, String> detailCol = new TableColumn<>("Details"); // Create a new column titled "Details"
        detailCol.setCellValueFactory(entry -> {
            Contact contact = entry.getValue().getValue();
            return new SimpleStringProperty(contact.getName().trim() + " (ID: " + contact.getStudentId() + ")");
        });
        detailCol.setCellFactory(TextFieldTableCell.forTableColumn());

        // Add columns to table
        typedTable.getColumns().addAll(keyCol, detailCol);
    }

    /**
     * Converts a Map<String, Contact> to an ObservableList for TableView.
     *
     * @param hash The hashmap of contacts.
     * @return ObservableList of Map.Entry<String, Contact>
     */
    public static ObservableList<Map.Entry<String, Contact>> convertToTableData(Map<String, Contact> hash) {
        return FXCollections.observableArrayList(hash.entrySet());
    }
}
