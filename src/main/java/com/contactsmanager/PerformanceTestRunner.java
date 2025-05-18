package com.contactsmanager;

import com.contactsmanager.performance.CSVPerformanceTest;
import com.contactsmanager.performance.PerformanceTest;
import com.contactsmanager.visualization.VisualizationLauncher;

/**
 * A simple runner class for performance tests.
 * This class provides a convenient way to run performance tests from the command line.
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

            case "visualize":
                runVisualizationDemo();
                break;

            default:
                System.err.println("Unknown test type: " + testType);
                System.err.println("Available test types: custom, csv, visualize");
        }
    }

    /**
     * Runs a visualization test with sample data.
     */
    private static void runVisualizationDemo() {
        System.out.println("Running visualization test...");

        // Launch the CSV visualization directly, which will now only show pie charts
        VisualizationLauncher.VisualizationType visualizationType = VisualizationLauncher.VisualizationType.CSV_VISUALIZATION;
        VisualizationLauncher.launchVisualization(visualizationType, null);
    }
}
