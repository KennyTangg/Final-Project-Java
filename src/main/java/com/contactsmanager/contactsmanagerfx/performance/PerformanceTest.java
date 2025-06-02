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
        AdjacencyListGraphCB graph = new AdjacencyListGraphCB();
        AdjacencyMatrixGraphCB matrixGraph = new AdjacencyMatrixGraphCB(Math.max(contactCount, DEFAULT_MATRIX_SIZE));
        HashMapCB hash = new HashMapCB();

        // Create comparator
        DataStructureComparator comparator = new DataStructureComparator(DEFAULT_RUNS)
                .addDataStructure(graph, graph,"Adjacency List")
                .addDataStructure(matrixGraph, matrixGraph, "Adjacency Matrix")
                .addDataStructure(hash,"HashMap");

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
                    Contact newContact = new Contact("TestContact", 9999);
                    comparator.compareAddContact(newContact);
                    break;
                case "search":
                    comparator.compareSearchContact(contacts[0].getName());
                    break;
                case "list":
                    comparator.compareListAllContacts();
                    break;
                case "suggest":
                    comparator.compareSuggestContacts(contacts[0].getName());
                    break;
                case "update":
                    comparator.compareUpdateContact(contacts[0], "UpdatedContact", 8888);
                    break;
                case "delete":
                    comparator.compareDeleteContact(contacts[contactCount - 1].getName());
                    break;
                case "addconnection":
                    comparator.compareAddConnection(contacts[0].getName(), contacts[contactCount - 1].getName());
                    break;
                case "removeconnection":
                    comparator.compareRemoveConnection(contacts[0].getName(), contacts[1].getName());
                    break;
                default:
                    System.out.println("Unknown operation: " + operation);
            }
        }
        System.out.println("\n----- End of Test Data Collection -----");

        // Print summary
        comparator.printSummary();
    }
}
