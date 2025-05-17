package com.contactsmanager.performance;

import com.contactsmanager.DataStructures.AdjacencyMatrixGraph;
import com.contactsmanager.DataStructures.Graph;
import com.contactsmanager.DataStructures.Hash;
import com.contactsmanager.model.Contact;

import java.util.Random;

/**
 * A class for running performance tests on different data structure implementations.
 */
public class PerformanceTest {
    private static final int DEFAULT_RUNS = 5;
    private static final int DEFAULT_MATRIX_SIZE = 100;
    private static final Random random = new Random();

    /**
     * Runs a basic performance test comparing the three data structure implementations.
     */
    public static void runBasicTest() {
        System.out.println("Running basic performance test...");
        
        // Create data structures
        Graph graph = new Graph();
        AdjacencyMatrixGraph matrixGraph = new AdjacencyMatrixGraph(DEFAULT_MATRIX_SIZE);
        Hash hash = new Hash();
        
        // Create comparator
        DataStructureComparator comparator = new DataStructureComparator(DEFAULT_RUNS)
            .addDataStructure(graph, "Adjacency List")
            .addDataStructure(matrixGraph, "Adjacency Matrix")
            .addDataStructure(hash, "HashMap");
        
        // Create test contacts
        Contact contact1 = new Contact("John", 123);
        Contact contact2 = new Contact("Jane", 456);
        Contact contact3 = new Contact("Jack", 789);
        
        // Add contacts to all data structures
        comparator.compareAddContact(contact1);
        comparator.compareAddContact(contact2);
        comparator.compareAddContact(contact3);
        
        // Test search
        comparator.compareSearchContact("John");
        
        // Test add connection
        comparator.compareAddConnection("John", "Jane");
        
        // Test suggest contacts
        comparator.compareSuggestContacts("John");
        
        // Test list all contacts
        comparator.compareListAllContacts();
        
        // Print summary
        comparator.printSummary();
    }
    
    /**
     * Runs a comprehensive performance test with a larger dataset.
     *
     * @param contactCount The number of contacts to create
     * @param connectionDensity The percentage (0-100) of possible connections to create
     */
    public static void runComprehensiveTest(int contactCount, int connectionDensity) {
        System.out.println("Running comprehensive performance test...");
        System.out.println("Contacts: " + contactCount);
        System.out.println("Connection Density: " + connectionDensity + "%");
        
        // Create data structures
        Graph graph = new Graph();
        AdjacencyMatrixGraph matrixGraph = new AdjacencyMatrixGraph(Math.max(contactCount, DEFAULT_MATRIX_SIZE));
        Hash hash = new Hash();
        
        // Create comparator
        DataStructureComparator comparator = new DataStructureComparator(DEFAULT_RUNS)
            .addDataStructure(graph, "Adjacency List")
            .addDataStructure(matrixGraph, "Adjacency Matrix")
            .addDataStructure(hash, "HashMap");
        
        // Create contacts
        Contact[] contacts = new Contact[contactCount];
        for (int i = 0; i < contactCount; i++) {
            contacts[i] = new Contact("Contact" + i, 1000 + i);
            
            // Add to all data structures
            graph.addContact(contacts[i]);
            matrixGraph.addContact(contacts[i]);
            hash.addContact(contacts[i]);
        }
        
        // Create connections based on density
        int maxConnections = (contactCount * (contactCount - 1)) / 2;
        int connectionsToCreate = (int) (maxConnections * (connectionDensity / 100.0));
        
        System.out.println("Creating " + connectionsToCreate + " connections...");
        
        for (int i = 0; i < connectionsToCreate; i++) {
            int idx1 = random.nextInt(contactCount);
            int idx2;
            do {
                idx2 = random.nextInt(contactCount);
            } while (idx2 == idx1);
            
            String name1 = contacts[idx1].getName();
            String name2 = contacts[idx2].getName();
            
            graph.addConnection(name1, name2);
            matrixGraph.addConnection(name1, name2);
            hash.addConnection(name1, name2);
        }
        
        System.out.println("Setup complete. Starting performance tests...");
        
        // Test operations
        // 1. Search for existing contacts
        for (int i = 0; i < 5; i++) {
            int idx = random.nextInt(contactCount);
            comparator.compareSearchContact(contacts[idx].getName());
        }
        
        // 2. Search for non-existent contact
        comparator.compareSearchContact("NonExistentContact");
        
        // 3. List all contacts
        comparator.compareListAllContacts();
        
        // 4. Suggest contacts for random contacts
        for (int i = 0; i < 5; i++) {
            int idx = random.nextInt(contactCount);
            comparator.compareSuggestContacts(contacts[idx].getName());
        }
        
        // 5. Update random contacts
        for (int i = 0; i < 5; i++) {
            int idx = random.nextInt(contactCount);
            Contact contact = contacts[idx];
            String newName = "Updated" + contact.getName();
            int newId = contact.getStudentId() + 1000;
            
            comparator.compareUpdateContact(contact, newName, newId);
        }
        
        // 6. Add new contacts
        for (int i = 0; i < 5; i++) {
            Contact newContact = new Contact("NewContact" + i, 2000 + i);
            comparator.compareAddContact(newContact);
        }
        
        // 7. Delete random contacts
        for (int i = 0; i < 5; i++) {
            int idx = random.nextInt(contactCount);
            comparator.compareDeleteContact(contacts[idx].getName());
        }
        
        // Print summary
        comparator.printSummary();
    }
    
    /**
     * Runs a custom performance test with specific operations.
     *
     * @param contactCount The number of contacts to create
     * @param operations The operations to test
     */
    public static void runCustomTest(int contactCount, String... operations) {
        System.out.println("Running custom performance test...");
        
        // Create data structures
        Graph graph = new Graph();
        AdjacencyMatrixGraph matrixGraph = new AdjacencyMatrixGraph(Math.max(contactCount, DEFAULT_MATRIX_SIZE));
        Hash hash = new Hash();
        
        // Create comparator
        DataStructureComparator comparator = new DataStructureComparator(DEFAULT_RUNS)
            .addDataStructure(graph, "Adjacency List")
            .addDataStructure(matrixGraph, "Adjacency Matrix")
            .addDataStructure(hash, "HashMap");
        
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
            hash.addConnection(name1, name2);
        }
        
        System.out.println("Setup complete. Starting performance tests...");
        
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
        
        // Print summary
        comparator.printSummary();
    }
}
