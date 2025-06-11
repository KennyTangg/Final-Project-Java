package com.contactsmanager.contactsmanagerfx.performance;

import com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyMatrixGraphCB;
import com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyListGraphCB;
import com.contactsmanager.contactsmanagerfx.dataStructures.HashMapCB;
import com.contactsmanager.contactsmanagerfx.model.Contact;

/**
 * A class for running performance tests on different data structure implementations.
 */
public class PerformanceTest {
    private static final int DEFAULT_RUNS = 5;
    private static final int DEFAULT_MATRIX_SIZE = 100;

    /**
     * Runs a custom performance test with specific operations.
     *
     * @param contactCount The number of contacts to create
     * @param operations The operations to test
     */
    public static void runCustomTest(int contactCount, String... operations) {
        System.out.println("Running custom performance test...");

        // Create data structures
        // Add extra capacity for test operations (add, update operations that might add new contacts)
        int matrixCapacity = Math.max(contactCount + 10, DEFAULT_MATRIX_SIZE);
        AdjacencyListGraphCB graph = new AdjacencyListGraphCB();
        AdjacencyMatrixGraphCB matrixGraph = new AdjacencyMatrixGraphCB(matrixCapacity);
        HashMapCB hash = new HashMapCB();

        // Create comparator for all operations (includes HashMap)
        DataStructureComparator allComparator = new DataStructureComparator(DEFAULT_RUNS)
                .addDataStructure(graph, graph,"Adjacency List")
                .addDataStructure(matrixGraph, matrixGraph, "Adjacency Matrix")
                .addDataStructure(hash,"HashMap");

        // Create comparator for connection operations (only first two data structures)
        DataStructureComparator connectionComparator = new DataStructureComparator(DEFAULT_RUNS)
                .addDataStructure(graph, graph,"Adjacency List")
                .addDataStructure(matrixGraph, matrixGraph, "Adjacency Matrix");

        // Create contacts
        Contact[] contacts = new Contact[contactCount];
        for (int i = 0; i < contactCount; i++) {
            contacts[i] = new Contact("Contact" + i, 1000 + i);

            // Add to all data structures
            graph.addContact(contacts[i]);
            matrixGraph.addContact(contacts[i]);
            hash.addContact(contacts[i]);
        }

        // Create some connections
        for (int i = 0; i < contactCount - 1; i++) {
            String name1 = contacts[i].getName();
            String name2 = contacts[i + 1].getName();

            graph.addConnection(name1, name2);
            matrixGraph.addConnection(name1, name2);
        }

        // Run specified operations
        for (String operation : operations) {
            switch (operation.toLowerCase()) {
                case "add":
                    allComparator.compareAddContact(new Contact("TestContact", 9999));
                    break;
                case "search":
                    allComparator.compareSearchContact(contacts[0].getName());
                    break;
                case "list":
                    allComparator.compareListAllContacts();
                    break;
                case "suggest":
                    // Use connection comparator (only first two data structures)
                    connectionComparator.compareSuggestContacts(contacts[0].getName());
                    break;
                case "update":
                    allComparator.compareUpdateContact(new Contact("Contact", 1000), "UpdatedContact", 8888);
                    break;
                case "delete":
                    allComparator.compareDeleteContact(new Contact("Contact", 1000));
                    break;
                case "addconnection":
                    // Use connection comparator (only first two data structures)
                    connectionComparator.compareAddConnection(contacts[0].getName(), contacts[contactCount - 1].getName());
                    break;
                case "removeconnection":
                    // Use connection comparator (only first two data structures)
                    connectionComparator.compareRemoveConnection(contacts[0].getName(), contacts[1].getName());
                    break;
                default:
                    System.out.println("Unknown operation: " + operation);
            }
        }
        System.out.println("\n----- End of Test Data Collection -----");

        // Print summary for both comparators
        allComparator.printSummary();
        connectionComparator.printSummary();
    }
}
