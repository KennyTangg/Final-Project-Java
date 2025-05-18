package com.contactsmanager.visualization;

import com.contactsmanager.performance.PerformanceMetric;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

/**
 * Base class for performance visualization using JavaFX.
 * This class provides the foundation for creating visual representations
 * of performance metrics for different data structures.
 */
public abstract class PerformanceVisualization extends Application {

    protected Map<String, Map<String, List<PerformanceMetric>>> performanceData;

    /**
     * Sets the performance data to be visualized.
     *
     * @param performanceData A map of data structure names to operation names to performance metrics
     */
    public void setPerformanceData(Map<String, Map<String, List<PerformanceMetric>>> performanceData) {
        this.performanceData = performanceData;
    }

    /**
     * Creates the content for the visualization.
     * This method should be implemented by subclasses to create specific visualizations.
     *
     * @return The root node for the visualization scene
     */
    protected abstract BorderPane createContent();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = createContent();
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Performance Visualization");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Launches the visualization with the given performance data.
     *
     * @param visualizationClass The class of the visualization to launch
     * @param performanceData The performance data to visualize
     */
    public static void launchVisualization(Class<? extends PerformanceVisualization> visualizationClass,
                                          Map<String, Map<String, List<PerformanceMetric>>> performanceData) {
        try {
            // Check if JavaFX is already running
            if (Platform.isFxApplicationThread()) {
                // Create and show the visualization directly
                PerformanceVisualization visualization = visualizationClass.getDeclaredConstructor().newInstance();
                visualization.setPerformanceData(performanceData);
                Stage stage = new Stage();
                visualization.start(stage);
            } else {
                // Initialize JavaFX and then create the visualization
                PerformanceVisualization visualization = visualizationClass.getDeclaredConstructor().newInstance();
                visualization.setPerformanceData(performanceData);

                // Store the visualization instance in a static field to be accessed by the launcher
                VisualizationHolder.setVisualization(visualization);

                // Launch the JavaFX application
                new Thread(() -> Application.launch(visualizationClass)).start();
            }
        } catch (Exception e) {
            System.err.println("Error launching visualization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper class to hold the visualization instance between threads.
     */
    private static class VisualizationHolder {
        private static PerformanceVisualization visualization;

        public static void setVisualization(PerformanceVisualization vis) {
            visualization = vis;
        }

        public static PerformanceVisualization getVisualization() {
            return visualization;
        }
    }
}
