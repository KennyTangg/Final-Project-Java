package com.contactsmanager;

import com.contactsmanager.performance.PerformanceTest;

/**
 * A runner class for performance tests.
 * Run performance tests from the command line.
 */
public class PerformanceTestRunner {
    public static void main(String[] args) {
        if (args.length == 0) {
            // Show usage information if no arguments provided
            System.out.println("Usage: java PerformanceTestRunner [test-type] [options]");
            System.out.println("Available test types: custom, csv, visualize");
            return;
        }

        String testType = args[0].toLowerCase();

        switch (testType) {

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
            default:
                System.err.println("Unknown test type: " + testType);
                System.err.println("Available test types: custom, csv, visualize");
        }
    }
}
