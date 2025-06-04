package com.contactsmanager.contactsmanagerfx.utility;

import com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyMatrixGraphCB;
import com.contactsmanager.contactsmanagerfx.model.Contact;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

public class AdjacencyMatrixGraphUtils {

    /**
     * Sets up columns for a TableView displaying int (as Index), Contact (as From Contact), and names of contacts of the To Contacts
     *
     * @param table The TableView instance to set up
     * @param graph The Adjacency Matrix Graph Contacts Book to set up
     */
    @SuppressWarnings("unchecked")
    public static void setup(TableView<?> table, AdjacencyMatrixGraphCB graph) { //The method uses a raw TableView<?> so that it can be filled with whatever type.

        // Cast TableView to the right generic type
        TableView<ObservableList<String>> typedTable = (TableView<ObservableList<String>>) table;

        // Reset to avoid stacking columns from earlier views.
        typedTable.getColumns().clear();

        Contact[] contacts = graph.getContactsBook();
        byte[][] matrix = graph.getMatrix();
        int maxSize = graph.getMaxSize();

        // Column 0: Index
        TableColumn<ObservableList<String>, String> indexCol = new TableColumn<>("Index");
        indexCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        indexCol.setCellFactory(TextFieldTableCell.forTableColumn());
        typedTable.getColumns().add(indexCol);

        // Column 1: From Contact (Name + ID)
        TableColumn<ObservableList<String>, String> fromContactCol = new TableColumn<>("From Contact");
        fromContactCol.setCellValueFactory(data -> {
            String name = data.getValue().get(1); // Name is stored in column index 1
            int index = Integer.parseInt(data.getValue().get(0)); // Index is stored in column 0
            // Defensive fallback if data is malformed
            String display = name;
            try {
                Contact contact = graph.getContactsBook()[index];
                if (contact != null) {
                    display = contact.getName() + " (ID: " + contact.getStudentId() + ")";
                }
            } catch (Exception ignored) {}
            return new SimpleStringProperty(display);
        });
        fromContactCol.setCellFactory(TextFieldTableCell.forTableColumn());
        typedTable.getColumns().add(fromContactCol);


        // Columns 2 to (maxSize+1): each contact as column header
        for (int i = 0; i < maxSize; i++) {
            final int colIndex = i + 2;
            String title = (contacts[i] != null) ? contacts[i].getName() : "null";
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(title);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(colIndex)));
            col.setCellFactory(TextFieldTableCell.forTableColumn());
            typedTable.getColumns().add(col);
        }

        // Fill rows
        ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
        for (int i = 0; i < maxSize; i++) {
            ObservableList<String> row = FXCollections.observableArrayList();
            row.add(String.valueOf(i)); // Index
            row.add(contacts[i] != null ? contacts[i].getName() : "null"); // From contact name
            for (int j = 0; j < maxSize; j++) {
                row.add(Byte.toString(matrix[i][j]));
            }
            rows.add(row);
        }

        typedTable.setItems(rows);
    }
}
