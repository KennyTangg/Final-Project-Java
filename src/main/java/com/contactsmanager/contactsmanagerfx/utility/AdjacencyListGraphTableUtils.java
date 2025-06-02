package com.contactsmanager.contactsmanagerfx.utility;

import com.contactsmanager.contactsmanagerfx.model.Contact;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

public class AdjacencyListGraphTableUtils {

    /**
     * Sets up columns for a TableView displaying Contact (as Contact), LinkedList<Contact> (as Known People).
     *
     * @param table The TableView instance to set up.
     */
    @SuppressWarnings("unchecked")
    public static void setup(TableView<?> table) { //The method uses a raw TableView<?> so that it can be filled with whatever type.

        // Cast TableView to the right generic type
        TableView<Map.Entry<Contact, LinkedList<Contact>>> typedTable =
                (TableView<Map.Entry<Contact, LinkedList<Contact>>>) table;

        // Reset to avoid stacking columns from earlier views.
        typedTable.getColumns().clear();

        // Column 1: Contact (Name + ID)
        TableColumn<Map.Entry<Contact, LinkedList<Contact>>, String> contactCol = new TableColumn<>("Contact"); // Create a new column titled "Contact"
        contactCol.setCellValueFactory(entry -> { // For every row, get the key
            Contact c = entry.getValue().getKey();

            return new SimpleStringProperty(c.getName().trim() + " (ID: " + c.getStudentId() + ")"); // Return it as a SimpleStringProperty because JavaFX TableView needs observable strings
        });
        contactCol.setCellFactory(TextFieldTableCell.forTableColumn());

        // Column 2: Connections (as comma-separated names)
        TableColumn<Map.Entry<Contact, LinkedList<Contact>>, String> connectionsCol = new TableColumn<>("Known People"); // Create a new column titled "Known People"
        connectionsCol.setCellValueFactory(entry -> { // For every row, get the key
            LinkedList<Contact> connections = entry.getValue().getValue();

            String joinedNames = connections.stream()
                    .map(c -> c.getName().trim())
                    .collect(Collectors.joining(", ")); // Return it as a SimpleStringProperty because JavaFX TableView needs observable strings
            return new SimpleStringProperty(joinedNames);
        });
        connectionsCol.setCellFactory(TextFieldTableCell.forTableColumn());

        // Attach columns to the table
        typedTable.getColumns().addAll(contactCol, connectionsCol);
    }

    /**
     * Converts an adjacency list map into a list of Map.Entry items for TableView.
     * Fills the table with rows.
     *
     * @param adj The adjacency list.
     * @return ObservableList of Map.Entry<Contact, LinkedList<Contact>>.
     */
    public static ObservableList<Map.Entry<Contact, LinkedList<Contact>>> convertToTableData(Map<Contact, LinkedList<Contact>> adj) {
        return FXCollections.observableArrayList(adj.entrySet());
    }
}
