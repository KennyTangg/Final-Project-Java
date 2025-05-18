package com.contactsmanager.visualization;

import com.contactsmanager.performance.PerformanceMetric;

import java.util.List;
import java.util.Map;

/**
 * Utility class for launching different types of visualizations.
 */
public class VisualizationLauncher {

    /**
     * Visualization types available in the application.
     */
    public enum VisualizationType {
        PIE_CHART,
        CSV_VISUALIZATION
    }

    /**
     * Launches a visualization of the specified type with the given performance data.
     *
     * @param type The type of visualization to launch
     * @param performanceData The performance data to visualize
     */
    public static void launchVisualization(VisualizationType type,
                                          Map<String, Map<String, List<PerformanceMetric>>> performanceData) {
        switch (type) {
            case PIE_CHART:
                PerformanceVisualization.launchVisualization(PieChartVisualization.class, performanceData);
                break;
            case CSV_VISUALIZATION:
                launchCSVVisualization();
                break;
            default:
                System.err.println("Unknown visualization type: " + type);
        }
    }

    /**
     * Launches the CSV visualization.
     * This visualization allows the user to select CSV files and visualize performance metrics.
     */
    public static void launchCSVVisualization() {
        new Thread(() -> {
            try {
                javafx.application.Application.launch(CSVVisualization.class);
            } catch (Exception e) {
                System.err.println("Error launching CSV visualization: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}
