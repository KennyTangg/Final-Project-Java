package com.contactsmanager.contactsmanagerfx;

import com.contactsmanager.contactsmanagerfx.model.Contact;
import com.contactsmanager.contactsmanagerfx.dataStructures.HashMapCB;
import com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyMatrixGraphCB;
import com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyListGraphCB;
import com.contactsmanager.contactsmanagerfx.utility.AdjacencyMatrixGraphUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AppDisplayController implements Initializable {

    /*----- GUI: View and display --------------------------*/
    @FXML private ComboBox<String> viewSelector;
    @FXML private TableView<?> tableDisplay; // Accepts any row type
    @FXML private TextArea outputArea;

    /*----- GUI: Search contact ----------------------------*/
    @FXML private TextField searchNameField;
    @FXML private Button searchButton;

    /*----- GUI: Add contact -------------------------------*/
    @FXML private TextField addNameField;
    @FXML private TextField addIdField;
    @FXML private Button addButton;

    /*----- GUI: Delete contact ----------------------------*/
    @FXML private TextField deleteNameField;
    @FXML private Button deleteButton;

    /*----- GUI: Update contact ----------------------------*/
    @FXML private TextField oldNameField;
    @FXML private TextField newNameField;
    @FXML private TextField newIdField;
    @FXML private Button updateButton;

    /*----- GUI: Manage connections ------------------------*/
    @FXML private TextField connection1NameField;
    @FXML private TextField connection2NameField;
    @FXML private Button addConnectionButton;
    @FXML private Button removeConnectionButton;

    /*----- GUI: Suggestions and Traversal -----------------*/
    @FXML private TextField suggestNameField;
    @FXML private Button suggestButton;
    @FXML private Button bfsButton;
    @FXML private Button dfsButton;

    // Data Structures
    private AdjacencyMatrixGraphCB adjMatrixUndirectedGraphCB;
    private AdjacencyMatrixGraphCB adjMatrixDirectedGraphCB;
    private AdjacencyListGraphCB adjListUndirectedGraphCB;
    private AdjacencyListGraphCB adjListDirectedGraphCB;
    private HashMapCB hashMapCB;

    /*========================================================================*/
    /*===== Setters and Loaders Management ===================================*/
    /*
     * LOADERS
     * Pattern used:
     * Dependency Injection (DI) – Behavioral/ Creational Principle
     */

    /**
     * Sets the Adjacency List Graph and loads it into the display.
     * @param graph The graph to load
     * @param directed True if directed, false if undirected
     */
    public void setAdjMatrixGraph(AdjacencyMatrixGraphCB graph, boolean directed) {
        if (directed) {
            this.adjMatrixDirectedGraphCB = graph;
        } else {
            this.adjMatrixUndirectedGraphCB = graph;
        }

        loadAdjacencyMatrixTable(directed);
    }

    /**
     * Sets the Adjacency Matrix Graph and loads it into the display.
     * @param graph The graph to load
     * @param directed True if directed, false if undirected
     */
    public void setAdjListGraph(AdjacencyListGraphCB graph, boolean directed) {
        if (directed) {
            this.adjListDirectedGraphCB = graph;
        } else {
            this.adjListUndirectedGraphCB = graph;
        }

        loadAdjacencyListTable(directed); // Auto load default value
    }

    /**
     * Sets the HashMap and loads it into the display
     * @param hash The hashmap to load
     */
    public void setHashMap(HashMapCB hash) {
        this.hashMapCB = hash;

        loadHashMapTable();
    }

    private void loadAdjacencyMatrixTable(boolean directed) {
        if ((directed && adjMatrixDirectedGraphCB == null) || (!directed && adjMatrixUndirectedGraphCB == null)) {
            System.out.println("No graph data loaded for this mode.");
            return;
        }
        if (directed) {
            AdjacencyMatrixGraphUtils.setup(tableDisplay, adjMatrixDirectedGraphCB);
        } else {
            AdjacencyMatrixGraphUtils.setup(tableDisplay, adjMatrixUndirectedGraphCB);
        }

    }

    @SuppressWarnings("unchecked")
    private void loadAdjacencyListTable(boolean directed) {
        if ((directed && adjListDirectedGraphCB == null) || (!directed && adjListUndirectedGraphCB == null)) {
            System.out.println("No graph data loaded for this mode.");
            return;
        }

        // Setup the column headers and structure
        com.contactsmanager.contactsmanagerfx.utility.AdjacencyListGraphTableUtils.setup(tableDisplay);

        // Cast TableView to appropriate generic type for this data
        TableView<Map.Entry<Contact, LinkedList<Contact>>> typedTable =
                (TableView<Map.Entry<Contact, LinkedList<Contact>>>) tableDisplay;

        ObservableList<Map.Entry<Contact, LinkedList<Contact>>> data;
        if (directed) {
            data = com.contactsmanager.contactsmanagerfx.utility.AdjacencyListGraphTableUtils.convertToTableData(adjListDirectedGraphCB.getAdjacencyList());
        } else {
            data = com.contactsmanager.contactsmanagerfx.utility.AdjacencyListGraphTableUtils.convertToTableData(adjListUndirectedGraphCB.getAdjacencyList());
        }

        typedTable.setItems(data);
    }

    @SuppressWarnings("unchecked")
    private void loadHashMapTable() {
        // Setup the column headers and structure
        com.contactsmanager.contactsmanagerfx.utility.HashMapTableUtils.setup(tableDisplay);

        // Cast TableView to appropriate generic type for this data
        TableView<Map.Entry<String, Contact>> typedTable =
                (TableView<Map.Entry<String, Contact>>) tableDisplay;

        ObservableList<Map.Entry<String, Contact>> data =
                com.contactsmanager.contactsmanagerfx.utility.HashMapTableUtils.convertToTableData(hashMapCB.getHashMap());

        typedTable.setItems(data);
    }

    /*========================================================================*/
    /*===== GUI Display management ===========================================*/

    // RELOAD TABLE
    /**
     * Reloads table. Table changes with each view. (Select in the combobox)
     */
    public void reloadTableByCurrentMode() {
        String selected = viewSelector.getValue();
        System.out.println("Selected view: " + selected);

        // You cannot access any controls if you don't choose a view.
        if (selected.equals("Select a view")) {
            tableDisplay.getItems().clear();
            tableDisplay.getColumns().clear();
            outputArea.setText("Please select a view mode.");
            toggleControls(false);
            return;
        }
        toggleControls(true);

        tableDisplay.setPlaceholder(new Label("No data to display. Add contacts or change view.")); // Placeholder text

        if (selected.contains("Adjacency Matrix (directed)")) {
            loadAdjacencyMatrixTable(true);
        } else if (selected.contains("Adjacency Matrix (undirected)")) {
            loadAdjacencyMatrixTable(false);
        } else if (selected.contains("Adjacency List (directed)")) {
            loadAdjacencyListTable(true);
        } else if (selected.contains("Adjacency List (undirected)")) {
            loadAdjacencyListTable(false);
        } else if (selected.contains("HashMap View")) {
            loadHashMapTable();
        }
    }

    /**
     * Used to toggle the GUI input fields and buttons based on whether a data view is selected.
     */
    private void toggleControls(boolean enabled) {
        addButton.setDisable(!enabled);
        deleteButton.setDisable(!enabled);
        updateButton.setDisable(!enabled);
        searchButton.setDisable(!enabled);
        addConnectionButton.setDisable(!enabled);
        removeConnectionButton.setDisable(!enabled);
        suggestButton.setDisable(!enabled);
        bfsButton.setDisable(!enabled);
        dfsButton.setDisable(!enabled);
        addNameField.setDisable(!enabled);
        addIdField.setDisable(!enabled);
        deleteNameField.setDisable(!enabled);
        oldNameField.setDisable(!enabled);
        newNameField.setDisable(!enabled);
        newIdField.setDisable(!enabled);
        searchNameField.setDisable(!enabled);
        connection1NameField.setDisable(!enabled);
        connection2NameField.setDisable(!enabled);
        suggestNameField.setDisable(!enabled);

    }

    /**
     * Clears output
     */
    private void clearOutput() {
        outputArea.setText("");
        outputArea.setStyle("-fx-text-fill: black;");
    }

    private Contact searchByMode(String name) {
        String selected = viewSelector.getValue();

        if (selected.contains("Adjacency Matrix (directed)") && adjMatrixDirectedGraphCB != null) {
            return adjMatrixDirectedGraphCB.searchContact(name);
        } else if (selected.contains("Adjacency Matrix (undirected)") && adjMatrixUndirectedGraphCB != null) {
            return adjMatrixUndirectedGraphCB.searchContact(name);
        } else if (selected.contains("Adjacency List (directed)") && adjListDirectedGraphCB != null) {
            return adjListDirectedGraphCB.searchContact(name);
        } else if (selected.contains("Adjacency List (undirected)") && adjListUndirectedGraphCB != null) {
            return adjListUndirectedGraphCB.searchContact(name);
        } else if (selected.contains("HashMap View") && hashMapCB != null) {
            return hashMapCB.searchContact(name);
        }

        return null; // fallback
    }

    /*========================================================================*/
    /*===== Functions of App management ======================================*/
    /*
     * Executes add/delete/update/traverse across all (or most) underlying contact book data structures.
     */

    private void addOnAllModes(String name, int id) {
        Contact newContact = new Contact(name, id);
        adjMatrixDirectedGraphCB.addContact(newContact);
        adjMatrixUndirectedGraphCB.addContact(newContact);
        adjListDirectedGraphCB.addContact(newContact);
        adjListUndirectedGraphCB.addContact(newContact);
        hashMapCB.addContact(newContact);
    }

    private void deleteOnAllModes(String name) {
        adjMatrixDirectedGraphCB.deleteContact(name);
        adjMatrixUndirectedGraphCB.deleteContact(name);
        adjListDirectedGraphCB.deleteContact(name);
        adjListUndirectedGraphCB.deleteContact(name);
        hashMapCB.deleteContact(name);
    }

    private void updateOnAllModes(String oldName, String newName, int newId) {
        Contact tempContact = new Contact(oldName, 0);
        adjMatrixDirectedGraphCB.updateContact(tempContact, newName, newId);
        adjMatrixUndirectedGraphCB.updateContact(tempContact, newName, newId);
        adjListDirectedGraphCB.updateContact(tempContact, newName, newId);
        adjListUndirectedGraphCB.updateContact(tempContact, newName, newId);
        hashMapCB.updateContact(tempContact, newName, newId);
    }

    private void addConnectionGraphModes(String name1, String name2) {
        adjMatrixDirectedGraphCB.addConnection(name1, name2);
        adjMatrixUndirectedGraphCB.addConnection(name1, name2);
        adjListDirectedGraphCB.addConnection(name1, name2);
        adjListUndirectedGraphCB.addConnection(name1, name2);
    }

    private void removeConnectionGraphModes(String name1, String name2) {
        adjMatrixDirectedGraphCB.removeConnection(name1, name2);
        adjMatrixUndirectedGraphCB.removeConnection(name1, name2);
        adjListDirectedGraphCB.removeConnection(name1, name2);
        adjListUndirectedGraphCB.removeConnection(name1, name2);
    }

    private List<Contact> suggestByMode(String name) {
        String selected = viewSelector.getValue();

        if (selected.contains("Adjacency Matrix (directed)") && adjMatrixDirectedGraphCB != null) {
            return adjMatrixDirectedGraphCB.suggestContacts(name);
        } else if (selected.contains("Adjacency Matrix (undirected)") && adjMatrixUndirectedGraphCB != null) {
            return adjMatrixUndirectedGraphCB.suggestContacts(name);
        } else if (selected.contains("Adjacency List (directed)") && adjListDirectedGraphCB != null) {
            return adjListDirectedGraphCB.suggestContacts(name);
        } else if (selected.contains("Adjacency List (undirected)") && adjListUndirectedGraphCB != null) {
            return adjListUndirectedGraphCB.suggestContacts(name);
        }
        // No suggestions for Hash Map.

        return null; // fallback
    }

    private void bfsTraversalbyMode(String name) {
        String selected = viewSelector.getValue();

        if (selected.contains("Adjacency Matrix (directed)") && adjMatrixDirectedGraphCB != null) {
            adjMatrixDirectedGraphCB.bfsTraversal(name);
        } else if (selected.contains("Adjacency Matrix (undirected)") && adjMatrixUndirectedGraphCB != null) {
            adjMatrixUndirectedGraphCB.bfsTraversal(name);
        } else if (selected.contains("Adjacency List (directed)") && adjListDirectedGraphCB != null) {
            adjListDirectedGraphCB.bfsTraversal(name);
        } else if (selected.contains("Adjacency List (undirected)") && adjListUndirectedGraphCB != null) {
            adjListUndirectedGraphCB.bfsTraversal(name);
        } else { // No suggestions for Hash Map.
            outputArea.setStyle("-fx-text-fill: red;");
            outputArea.setText("Cannot do traversals in HashMap view.");
        }
    }

    private void dfsTraversalbyMode(String name) {
        String selected = viewSelector.getValue();

        if (selected.contains("Adjacency Matrix (directed)") && adjMatrixDirectedGraphCB != null) {
            adjMatrixDirectedGraphCB.dfsTraversal(name);
        } else if (selected.contains("Adjacency Matrix (undirected)") && adjMatrixUndirectedGraphCB != null) {
            adjMatrixUndirectedGraphCB.dfsTraversal(name);
        } else if (selected.contains("Adjacency List (directed)") && adjListDirectedGraphCB != null) {
            adjListDirectedGraphCB.dfsTraversal(name);
        } else if (selected.contains("Adjacency List (undirected)") && adjListUndirectedGraphCB != null) {
            adjListUndirectedGraphCB.dfsTraversal(name);
        } else { // No suggestions for Hash Map.
            outputArea.setStyle("-fx-text-fill: red;");
            outputArea.setText("Cannot do traversals in HashMap view.");
        }
    }

    /*========================================================================*/
    /*===== Initialization and Event Binding =================================*/
    /**
     * This section sets up event listeners for all UI buttons.
     * It binds each button to the logic that interacts with the selected graph view.
     * Also initializes the viewSelector ComboBox behavior and default states.
     */

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        viewSelector.setItems(FXCollections.observableArrayList(
                "Adjacency Matrix (directed)",
                "Adjacency Matrix (undirected)",
                "Adjacency List (directed)",
                "Adjacency List (undirected)",
                "HashMap View"
        ));

        viewSelector.setOnAction(event -> reloadTableByCurrentMode());
        viewSelector.setValue("Select a view"); // Prompt user to select a mode


        /*--------------------------------------------------------------------*/
        // Event binding for Search Contact functionality
        searchButton.setOnAction(event -> {
            clearOutput(); // Clear so that there's no residue from prev logs.
            String name = searchNameField.getText().trim();

            Contact found = searchByMode(name);

            if (found != null) {
                outputArea.setStyle("-fx-text-fill: green;");
                outputArea.setText("Found: " + found.getName() + " (ID: " + found.getStudentId() + ")");
            } else {
                outputArea.setStyle("-fx-text-fill: red;");
                outputArea.setText("Contact not found.");
            }
        });

        /*--------------------------------------------------------------------*/
        // Event binding for Add Contact
        addButton.setOnAction(event -> {
            clearOutput(); // Clear so that there's no residue from prev logs.
            String name = addNameField.getText().trim();
            int id;
            try {
                id = Integer.parseInt(addIdField.getText().trim());
                addOnAllModes(name, id);
            } catch (NumberFormatException e) {
                outputArea.setStyle("-fx-text-fill: red;");
                outputArea.setText("ID inserted is not an integer.");
            }
            reloadTableByCurrentMode();
        });

        /*--------------------------------------------------------------------*/
        // Event binding for Delete Contact
        deleteButton.setOnAction(event -> {
            clearOutput(); // Clear so that there's no residue from prev logs.
            String name = deleteNameField.getText().trim();
            deleteOnAllModes(name);
            reloadTableByCurrentMode();
        });

        /*--------------------------------------------------------------------*/
        // Event binding for Update Contact info
        updateButton.setOnAction(event -> {
            clearOutput(); // Clear so that there's no residue from prev logs.
            String oldName = oldNameField.getText().trim();
            String newName = newNameField.getText().trim();
            int newId;
            try {
                newId = Integer.parseInt(newIdField.getText().trim());
                updateOnAllModes(oldName, newName, newId);
            } catch (NumberFormatException e) {
                outputArea.setStyle("-fx-text-fill: red;");
                outputArea.setText("ID inserted is not an integer.");
            }
            reloadTableByCurrentMode();
        });

        /*--------------------------------------------------------------------*/
        // Event binding for Add Connection
        addConnectionButton.setOnAction(event -> {
            clearOutput(); // Clear so that there's no residue from prev logs.
            String name1 = connection1NameField.getText().trim();
            String name2 = connection2NameField.getText().trim();
            addConnectionGraphModes(name1, name2);
            reloadTableByCurrentMode();
        });

        /*--------------------------------------------------------------------*/
        // Event binding for Remove Connection
        removeConnectionButton.setOnAction(event -> {
            clearOutput(); // Clear so that there's no residue from prev logs.
            String name1 = connection1NameField.getText().trim();
            String name2 = connection2NameField.getText().trim();
            removeConnectionGraphModes(name1, name2);
            reloadTableByCurrentMode();
        });

        /*--------------------------------------------------------------------*/
        // Event binding for Suggest Contacts (friends of friends)
        suggestButton.setOnAction(event -> {
            clearOutput(); // Clear so that there's no residue from prev logs.
            String name = suggestNameField.getText().trim();

            List<Contact> suggestions = suggestByMode(name);

            if (suggestions == null) {
                outputArea.setStyle("-fx-text-fill: red;");
                outputArea.setText("Cannot suggest contacts in HashMap view.");
            } else if (suggestions.isEmpty()) {
                outputArea.setStyle("-fx-text-fill: green;");
                outputArea.setText("No contacts can be recommended.");
            } else {
                outputArea.setStyle("-fx-text-fill: green;");
                outputArea.setText(suggestions.toString());
            }
            reloadTableByCurrentMode();
        });

        /*--------------------------------------------------------------------*/
        // Event binding for DFS Traversal
        dfsButton.setOnAction(event -> {
            clearOutput(); // Clear so that there's no residue from prev logs.
            String name = suggestNameField.getText().trim();
            dfsTraversalbyMode(name);
            reloadTableByCurrentMode();
        });

        /*--------------------------------------------------------------------*/
        // Event binding for BFS Traversal
        bfsButton.setOnAction(event -> {
            clearOutput(); // Clear so that there's no residue from prev logs.
            String name = suggestNameField.getText().trim();
            bfsTraversalbyMode(name);
            reloadTableByCurrentMode();
        });
    }
}
