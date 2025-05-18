package com.contactsmanager.performance;

import com.contactsmanager.DataStructures.AdjacencyMatrixGraph;
import com.contactsmanager.DataStructures.Graph;
import com.contactsmanager.DataStructures.Hash;
import com.contactsmanager.interfaces.ContactsManager;
import com.contactsmanager.model.Contact;
import com.contactsmanager.utils.CSVDataLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Performance test class that uses CSV data for testing.
 */
public class CSVPerformanceTest {
    private static final int RUNS = 5;
    private static final int MATRIX_SIZE = 2000;
    private static final Random random = new Random();

    /**
     * Runs a performance test using contacts and connections from CSV files.
     *
     * @param contactsFilePath The path to the CSV file containing contact data
     * @param connectionsFilePath The path to the CSV file containing connection data
     */
    public static void runCSVTest(String contactsFilePath, String connectionsFilePath) {
        System.out.println("Running performance test with CSV data...");
        System.out.println("Contacts file: " + contactsFilePath);
        System.out.println("Connections file: " + connectionsFilePath);

        // Check if files exist
        if (!CSVDataLoader.fileExists(contactsFilePath)) {
            System.err.println("Contacts file not found: " + contactsFilePath);
            return;
        }

        if (!CSVDataLoader.fileExists(connectionsFilePath)) {
            System.err.println("Connections file not found: " + connectionsFilePath);
            return;
        }

        try {
            // Create data structures
            Graph graph = new Graph();
            AdjacencyMatrixGraph matrixGraph = new AdjacencyMatrixGraph(MATRIX_SIZE);
            Hash hash = new Hash();

            // Create comparator
            DataStructureComparator comparator = new DataStructureComparator(RUNS)
                .addDataStructure(graph, "Adjacency List")
                .addDataStructure(matrixGraph, "Adjacency Matrix")
                .addDataStructure(hash, "HashMap");

            // Load contacts and measure performance
            System.out.println("\n--- Loading Contacts ---");
            List<Contact> graphContacts = measureContactLoading(graph, contactsFilePath, "Adjacency List");
            List<Contact> matrixContacts = measureContactLoading(matrixGraph, contactsFilePath, "Adjacency Matrix");
            List<Contact> hashContacts = measureContactLoading(hash, contactsFilePath, "HashMap");

            // Load connections and measure performance
            System.out.println("\n--- Loading Connections ---");
            measureConnectionLoading(graph, connectionsFilePath, "Adjacency List");
            measureConnectionLoading(matrixGraph, connectionsFilePath, "Adjacency Matrix");
            measureConnectionLoading(hash, connectionsFilePath, "HashMap");

            // Get a sample of contacts for testing
            List<Contact> sampleContacts = getSampleContacts(graphContacts, 5);

            System.out.println("\n--- Running Performance Tests ---");

            // Test search
            for (Contact contact : sampleContacts) {
                comparator.compareSearchContact(contact.getName());
            }

            // Test suggest contacts
            for (Contact contact : sampleContacts) {
                comparator.compareSuggestContacts(contact.getName());
            }

            // Test list all contacts
            comparator.compareListAllContacts();

            // Test update contact
            for (Contact contact : sampleContacts) {
                String newName = "Updated_" + contact.getName();
                int newId = contact.getStudentId() + 1000;
                comparator.compareUpdateContact(contact, newName, newId);
            }

            // Test delete contact
            for (int i = 0; i < 3; i++) {
                int randomIndex = random.nextInt(graphContacts.size());
                Contact contactToDelete = graphContacts.get(randomIndex);
                comparator.compareDeleteContact(contactToDelete.getName());
            }

            // Print summary
            comparator.printSummary();

        } catch (IOException e) {
            System.err.println("Error loading CSV data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Measures the performance of loading contacts from a CSV file.
     *
     * @param manager The ContactsManager to load contacts into
     * @param filePath The path to the CSV file
     * @param dataStructureName The name of the data structure
     * @return A list of the loaded contacts
     * @throws IOException If an I/O error occurs
     */
    private static List<Contact> measureContactLoading(ContactsManager manager, String filePath, String dataStructureName) throws IOException {
        PerformanceMetric metric = PerformanceMeasurement.measure(
            () -> {
                try {
                    CSVDataLoader.loadContacts(manager, filePath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            },
            dataStructureName,
            "loadContacts"
        );

        System.out.println(metric);

        // Return the loaded contacts
        return manager.listAllContacts();
    }

    /**
     * Measures the performance of loading connections from a CSV file.
     *
     * @param manager The ContactsManager to load connections into
     * @param filePath The path to the CSV file
     * @param dataStructureName The name of the data structure
     * @throws IOException If an I/O error occurs
     */
    private static void measureConnectionLoading(ContactsManager manager, String filePath, String dataStructureName) throws IOException {
        PerformanceMetric metric = PerformanceMeasurement.measure(
            () -> {
                try {
                    CSVDataLoader.loadConnections(manager, filePath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            },
            dataStructureName,
            "loadConnections"
        );

        System.out.println(metric);
    }

    /**
     * Gets a random sample of contacts from a list.
     *
     * @param contacts The list of contacts to sample from
     * @param sampleSize The number of contacts to sample
     * @return A list of sampled contacts
     */
    private static List<Contact> getSampleContacts(List<Contact> contacts, int sampleSize) {
        List<Contact> sample = new ArrayList<>();
        int size = Math.min(sampleSize, contacts.size());

        for (int i = 0; i < size; i++) {
            int randomIndex = random.nextInt(contacts.size());
            sample.add(contacts.get(randomIndex));
        }

        return sample;
    }
}
