package com.contactsmanager.contactsmanagerfx;

import com.contactsmanager.contactsmanagerfx.performance.PerformanceTest;

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
            System.out.println("1. Run custom performance test");
            System.out.println("0. Exit");

            System.out.print("\nEnter your choice: ");
            int choice = getIntInput();

            switch (choice) {
                case 0:
                    System.out.println("Exiting...");
                    return;
                case 1:
                    runCustomTest();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }


    private static void runCustomTest() {
        System.out.print("Enter number of contacts (1-10000): ");
        int contactCount = getValidatedContactInput();

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


    private static int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    /**
     * Gets validated input for the number of contacts, ensuring it's between 1 and 10,000.
     * Provides helpful guidance and error messages for invalid input.
     */
    private static int getValidatedContactInput() {
        final int MIN_CONTACTS = 1;
        final int MAX_CONTACTS = 10000;

        while (true) {
            try {
                int contactCount = Integer.parseInt(scanner.nextLine());

                if (contactCount < MIN_CONTACTS) {
                    System.out.printf("Number of contacts must be at least %d. Please enter a valid number (1-10000): ", MIN_CONTACTS);
                } else if (contactCount > MAX_CONTACTS) {
                    System.out.printf("Number of contacts cannot exceed %d. Please enter a valid number (1-10000): ", MAX_CONTACTS);
                } else {
                    // Valid input - provide helpful information for large datasets
                    if (contactCount >= 5000) {
                        System.out.printf("Testing with %d contacts - this may take a few moments for large operations like 'add'...%n", contactCount);
                    }
                    return contactCount;
                }
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid integer between 1 and 10000: ");
            }
        }
    }
}