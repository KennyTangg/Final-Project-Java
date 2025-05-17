package com.contactsmanager;

import com.contactsmanager.DataStructures.AdjacencyMatrixGraph;
import com.contactsmanager.DataStructures.Graph;
import com.contactsmanager.DataStructures.Hash;
import com.contactsmanager.model.Contact;
import com.contactsmanager.performance.CSVPerformanceTest;
import com.contactsmanager.performance.PerformanceTest;
import com.contactsmanager.utils.CSVDataLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main class for the Contacts Manager application.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Welcome to Contacts Manager Performance Testing");

        while (true) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Run basic performance test");
            System.out.println("2. Run comprehensive performance test");
            System.out.println("3. Run custom performance test");
            System.out.println("4. Run CSV-based performance test");
            System.out.println("5. Run demo with Adjacency List (Graph)");
            System.out.println("6. Run demo with Adjacency Matrix");
            System.out.println("7. Run demo with HashMap");
            System.out.println("0. Exit");

            System.out.print("\nEnter your choice: ");
            int choice = getIntInput();

            switch (choice) {
                case 0:
                    System.out.println("Exiting...");
                    return;
                case 1:
                    PerformanceTest.runBasicTest();
                    break;
                case 2:
                    System.out.print("Enter number of contacts (recommended: 50-500): ");
                    int contactCount = getIntInput();
                    System.out.print("Enter connection density percentage (0-100): ");
                    int density = getIntInput();
                    PerformanceTest.runComprehensiveTest(contactCount, density);
                    break;
                case 3:
                    runCustomTest();
                    break;
                case 4:
                    runCSVTest();
                    break;
                case 5:
                    runGraphDemo();
                    break;
                case 6:
                    runMatrixDemo();
                    break;
                case 7:
                    runHashDemo();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void runCustomTest() {
        System.out.print("Enter number of contacts: ");
        int contactCount = getIntInput();

        System.out.println("Available operations:");
        System.out.println("- add: Test adding a contact");
        System.out.println("- search: Test searching for a contact");
        System.out.println("- list: Test listing all contacts");
        System.out.println("- suggest: Test suggesting contacts");
        System.out.println("- update: Test updating a contact");
        System.out.println("- delete: Test deleting a contact");
        System.out.println("- addconnection: Test adding a connection");
        System.out.println("- removeconnection: Test removing a connection");

        System.out.print("Enter operations to test (comma-separated): ");
        String operationsInput = scanner.nextLine();
        String[] operations = operationsInput.split(",");

        for (int i = 0; i < operations.length; i++) {
            operations[i] = operations[i].trim();
        }

        PerformanceTest.runCustomTest(contactCount, operations);
    }

    private static void runGraphDemo() {
        Graph graph = new Graph();
        runDemo(graph, "Adjacency List (Graph)");
    }

    private static void runMatrixDemo() {
        AdjacencyMatrixGraph matrix = new AdjacencyMatrixGraph(100);
        runDemo(matrix, "Adjacency Matrix");
    }

    private static void runHashDemo() {
        Hash hash = new Hash();
        runDemo(hash, "HashMap");
    }

    private static void runCSVTest() {
        System.out.println("\nCSV-based Performance Test");
        System.out.println("-------------------------");

        // Define the data directory
        String dataDir = "datas";

        // Get available CSV files
        List<String> csvFiles = CSVDataLoader.getAvailableCSVFiles(dataDir);

        if (csvFiles.isEmpty()) {
            System.out.println("No CSV files found in the '" + dataDir + "' directory.");
            return;
        }

        // Display available contact files
        System.out.println("\nAvailable contact files:");
        List<String> contactFiles = new ArrayList<>();
        for (int i = 0; i < csvFiles.size(); i++) {
            String file = csvFiles.get(i);
            if (file.contains("Names")) {
                contactFiles.add(file);
                System.out.println((contactFiles.size()) + ". " + new File(file).getName());
            }
        }

        if (contactFiles.isEmpty()) {
            System.out.println("No contact files found.");
            return;
        }

        System.out.print("\nSelect a contact file (1-" + contactFiles.size() + "): ");
        int contactFileChoice = getIntInput();

        if (contactFileChoice < 1 || contactFileChoice > contactFiles.size()) {
            System.out.println("Invalid choice.");
            return;
        }

        String selectedContactFile = contactFiles.get(contactFileChoice - 1);

        // Display available connection files
        System.out.println("\nAvailable connection files:");
        List<String> connectionFiles = new ArrayList<>();
        for (int i = 0; i < csvFiles.size(); i++) {
            String file = csvFiles.get(i);
            if (file.contains("Connections")) {
                connectionFiles.add(file);
                System.out.println((connectionFiles.size()) + ". " + new File(file).getName());
            }
        }

        if (connectionFiles.isEmpty()) {
            System.out.println("No connection files found.");
            return;
        }

        System.out.print("\nSelect a connection file (1-" + connectionFiles.size() + "): ");
        int connectionFileChoice = getIntInput();

        if (connectionFileChoice < 1 || connectionFileChoice > connectionFiles.size()) {
            System.out.println("Invalid choice.");
            return;
        }

        String selectedConnectionFile = connectionFiles.get(connectionFileChoice - 1);

        // Run the CSV-based performance test
        CSVPerformanceTest.runCSVTest(selectedContactFile, selectedConnectionFile);
    }

    private static void runDemo(com.contactsmanager.interfaces.ContactsManager manager, String name) {
        System.out.println("\nRunning demo with " + name);

        // Create and add contacts
        Contact contact1 = new Contact("John", 123);
        Contact contact2 = new Contact("Jane", 456);
        Contact contact3 = new Contact("Jack", 789);

        manager.addContact(contact1);
        manager.addContact(contact2);
        manager.addContact(contact3);

        System.out.println("\nAdded contacts:");
        System.out.println(manager.listAllContacts());

        // Add connections
        manager.addConnection("John", "Jane");
        manager.addConnection("Jane", "Jack");

        // Suggest contacts
        System.out.println("\nSuggested contacts for John:");
        System.out.println(manager.suggestContacts("John"));

        // Update a contact
        manager.updateContact(contact1, "Johnny", 999);
        System.out.println("\nAfter updating John to Johnny:");
        System.out.println(manager.listAllContacts());

        // Delete a contact
        manager.deleteContact("Jack");
        System.out.println("\nAfter deleting Jack:");
        System.out.println(manager.listAllContacts());
    }

    private static int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
}