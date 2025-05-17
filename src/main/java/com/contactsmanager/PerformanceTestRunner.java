package com.contactsmanager;

import com.contactsmanager.performance.CSVPerformanceTest;
import com.contactsmanager.performance.PerformanceTest;

/**
 * A simple runner class for performance tests.
 * This class provides a convenient way to run performance tests from the command line.
 */
public class PerformanceTestRunner {
    public static void main(String[] args) {
        if (args.length == 0) {
            // Run basic test if no arguments provided
            PerformanceTest.runBasicTest();
            return;
        }

        String testType = args[0].toLowerCase();

        switch (testType) {
            case "basic":
                PerformanceTest.runBasicTest();
                break;

            case "comprehensive":
                int contactCount = 100; // Default
                int density = 20; // Default

                if (args.length > 1) {
                    try {
                        contactCount = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid contact count. Using default: " + contactCount);
                    }
                }

                if (args.length > 2) {
                    try {
                        density = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid density. Using default: " + density);
                    }
                }

                PerformanceTest.runComprehensiveTest(contactCount, density);
                break;

            case "custom":
                if (args.length < 3) {
                    System.err.println("Custom test requires at least contact count and one operation.");
                    System.err.println("Usage: java PerformanceTestRunner custom <contactCount> <operation1,operation2,...>");
                    return;
                }

                int customContactCount;
                try {
                    customContactCount = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid contact count. Using default: 50");
                    customContactCount = 50;
                }

                String[] operations = args[2].split(",");
                PerformanceTest.runCustomTest(customContactCount, operations);
                break;

            case "csv":
                if (args.length < 3) {
                    System.err.println("CSV test requires contact file path and connection file path.");
                    System.err.println("Usage: java PerformanceTestRunner csv <contactsFilePath> <connectionsFilePath>");
                    return;
                }

                String contactsFilePath = args[1];
                String connectionsFilePath = args[2];

                CSVPerformanceTest.runCSVTest(contactsFilePath, connectionsFilePath);
                break;

            default:
                System.err.println("Unknown test type: " + testType);
                System.err.println("Available test types: basic, comprehensive, custom, csv");
        }
    }
}
