package com.contactsmanager;

import com.contactsmanager.performance.PerformanceTest;
import com.contactsmanager.visualization.VisualizationLauncher;

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
            System.out.println("2. Run visualization demo");
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
                case 2:
                    runVisualizationDemo();
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

    private static void runVisualizationDemo() {
        System.out.println("Launching Basic JavaFX Application...");

        // Launch the basic JavaFX application
        VisualizationLauncher.launchApp();
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