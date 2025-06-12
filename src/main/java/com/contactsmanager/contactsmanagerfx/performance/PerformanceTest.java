package com.contactsmanager.contactsmanagerfx.performance;

import com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyMatrixGraphCB;
import com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyListGraphCB;
import com.contactsmanager.contactsmanagerfx.dataStructures.HashMapCB;
import com.contactsmanager.contactsmanagerfx.model.Contact;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class for running performance tests on different data structure implementations.
 */
public class PerformanceTest {
    private static final int DEFAULT_RUNS = 1;
    private static final AtomicInteger contactCounter = new AtomicInteger(0);

    /**
     * Runs a custom performance test with specific operations.
     */
    public static void runCustomTest(int contactCount, String... operations) {
        System.out.println("\n=== Performance Test ===");
        System.out.printf("Contacts: %d | Operations: %s\n\n",
            contactCount, String.join(", ", operations));
    
        // Create data structures with proper sizing
        AdjacencyListGraphCB graph = new AdjacencyListGraphCB();
        AdjacencyMatrixGraphCB matrixGraph = new AdjacencyMatrixGraphCB(Math.min(contactCount, 3000));
        HashMapCB hash = new HashMapCB();
    
        // Setup the comparator
        DataStructureComparator comparator = new DataStructureComparator(DEFAULT_RUNS)
                .addDataStructure(graph, graph, "Adjacency List")
                .addDataStructure(matrixGraph, matrixGraph, "Adjacency Matrix")
                .addDataStructure(hash, "HashMap");
    
        // Always generate contacts
        Contact[] contacts = new Contact[contactCount];
        for (int i = 0; i < contactCount; i++) {
            contacts[i] = generateUniqueContact();
        }
    
        // Only populate the data structures if the operation needs existing data
        if (needsInitialContacts(operations)) {
            for (Contact contact : contacts) {
                graph.addContact(contact);
                matrixGraph.addContact(contact);
                hash.addContact(contact);
            }
        }
    
        // Run the requested operations
        for (String operation : operations) {
            System.out.println("Testing: " + operation.toUpperCase());
            switch (operation.toLowerCase()) {
                case "add":
                    Contact testContact = new Contact("Contact", contactCount);
                    comparator.compareAddContact(testContact);
                    break;
                case "search":
                    if (contacts.length > 0) {
                        comparator.compareSearchContact(contacts[0].getName());
                    }
                    break;
                case "list":
                    comparator.compareListAllContacts();
                    break;
                case "update":
                    if (contacts.length > 0) {
                        comparator.compareUpdateContact(contacts[0], "UpdatedContact" + contactCounter.getAndIncrement(), 999999);
                    }
                    break;
                case "delete":
                    if (contacts.length > 0) {
                        comparator.compareDeleteContact(contacts[0]);
                    }
                    break;
                case "addconnection":
                    if (contacts.length > 1) {
                        comparator.compareAddConnection(contacts[0].getName(), contacts[1].getName());
                    }
                    break;
                case "removeconnection":
                    if (contacts.length > 1) {
                        comparator.compareRemoveConnection(contacts[0].getName(), contacts[1].getName());
                    }
                    break;
                case "suggest":
                    if (contacts.length > 0) {
                        comparator.compareSuggestContacts(contacts[0].getName());
                    }
                    break;
                default:
                    System.out.println("Unknown operation: " + operation);
            }
            System.out.println();
        }
    
        System.out.println("=== Results ===");
        comparator.printSummary();
        System.out.println("\nTest complete.");
    }
    

    /**
     * Checks if any of the operations require initial contacts to be present.
     */
    private static boolean needsInitialContacts(String[] operations) {
        for (String op : operations) {
            switch (op.toLowerCase()) {
                case "search":
                case "update":
                case "delete":
                case "addconnection":
                case "removeconnection":
                case "suggest":
                    return true;
            }
        }
        return false;
    }

    /**
     * Generates a unique contact with a guaranteed unique name and ID.
     */
    private static Contact generateUniqueContact() {
        int counter = contactCounter.getAndIncrement();
        return new Contact("Contact" + counter, 1000000 + counter);
    }
}
