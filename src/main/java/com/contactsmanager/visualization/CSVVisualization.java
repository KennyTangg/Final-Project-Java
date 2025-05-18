package com.contactsmanager.visualization;

import com.contactsmanager.DataStructures.AdjacencyMatrixGraph;
import com.contactsmanager.DataStructures.Graph;
import com.contactsmanager.DataStructures.Hash;
import com.contactsmanager.interfaces.ContactsManager;
import com.contactsmanager.model.Contact;
import com.contactsmanager.performance.DataStructureComparator;
import com.contactsmanager.performance.PerformanceMetric;
import com.contactsmanager.utils.CSVDataLoader;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A visualization class that loads data from CSV files and visualizes performance metrics.
 */
public class CSVVisualization extends Application {

    private Map<String, Map<String, List<PerformanceMetric>>> performanceData;
    private Stage primaryStage;
    private String contactsFilePath;
    private String connectionsFilePath;
    private DataStructureComparator comparator;
    private final int RUNS = 3;
    private final int MATRIX_SIZE = 2000;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("CSV Performance Visualization");

        // Create initial UI for file selection
        BorderPane root = createFileSelectionUI();
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates the UI for selecting CSV files.
     *
     * @return A BorderPane containing the file selection UI
     */
    private BorderPane createFileSelectionUI() {
        BorderPane root = new BorderPane();
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("CSV Performance Visualization");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label instructionLabel = new Label("Select CSV files containing contacts and connections data:");

        Button selectContactsButton = new Button("Select Contacts CSV");
        Label contactsFileLabel = new Label("No file selected");

        Button selectConnectionsButton = new Button("Select Connections CSV");
        Label connectionsFileLabel = new Label("No file selected");

        Button runButton = new Button("Run Performance Test and Visualize");
        runButton.setDisable(true);

        // Set up file chooser for contacts
        selectContactsButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Contacts CSV File");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            // Set initial directory to "datas" folder if it exists
            File datasDir = new File("datas");
            if (datasDir.exists() && datasDir.isDirectory()) {
                fileChooser.setInitialDirectory(datasDir);
            }

            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                contactsFilePath = selectedFile.getAbsolutePath();
                contactsFileLabel.setText(selectedFile.getName());

                // Enable run button if both files are selected
                if (connectionsFilePath != null) {
                    runButton.setDisable(false);
                }
            }
        });

        // Set up file chooser for connections
        selectConnectionsButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Connections CSV File");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            // Set initial directory to "datas" folder if it exists
            File datasDir = new File("datas");
            if (datasDir.exists() && datasDir.isDirectory()) {
                fileChooser.setInitialDirectory(datasDir);
            }

            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                connectionsFilePath = selectedFile.getAbsolutePath();
                connectionsFileLabel.setText(selectedFile.getName());

                // Enable run button if both files are selected
                if (contactsFilePath != null) {
                    runButton.setDisable(false);
                }
            }
        });

        // Set up run button
        runButton.setOnAction(e -> {
            try {
                loadDataAndRunTest();
                showVisualization();
            } catch (Exception ex) {
                ex.printStackTrace();
                Label errorLabel = new Label("Error: " + ex.getMessage());
                errorLabel.setStyle("-fx-text-fill: red;");
                content.getChildren().add(errorLabel);
            }
        });

        // Add components to layout
        HBox contactsBox = new HBox(10, selectContactsButton, contactsFileLabel);
        HBox connectionsBox = new HBox(10, selectConnectionsButton, connectionsFileLabel);

        content.getChildren().addAll(
            titleLabel,
            instructionLabel,
            contactsBox,
            connectionsBox,
            runButton
        );

        root.setCenter(content);
        return root;
    }

    /**
     * Loads data from CSV files and runs performance tests.
     *
     * @throws IOException If an I/O error occurs
     */
    private void loadDataAndRunTest() throws IOException {
        // Create data structures
        Graph graph = new Graph();
        AdjacencyMatrixGraph matrixGraph = new AdjacencyMatrixGraph(MATRIX_SIZE);
        Hash hash = new Hash();

        // Create comparator
        comparator = new DataStructureComparator(RUNS)
            .addDataStructure(graph, "Adjacency List")
            .addDataStructure(matrixGraph, "Adjacency Matrix")
            .addDataStructure(hash, "HashMap");

        // Load contacts
        loadContacts(graph, matrixGraph, hash);

        // Load connections
        loadConnections(graph, matrixGraph, hash);

        // Run performance tests
        runPerformanceTests(graph, matrixGraph, hash);

        // Get performance data
        performanceData = comparator.getResults();
    }

    /**
     * Loads contacts from the selected CSV file into all data structures.
     *
     * @param graph The Graph data structure
     * @param matrixGraph The AdjacencyMatrixGraph data structure
     * @param hash The Hash data structure
     * @throws IOException If an I/O error occurs
     */
    private void loadContacts(Graph graph, AdjacencyMatrixGraph matrixGraph, Hash hash) throws IOException {
        CSVDataLoader.loadContacts(graph, contactsFilePath);
        CSVDataLoader.loadContacts(matrixGraph, contactsFilePath);
        CSVDataLoader.loadContacts(hash, contactsFilePath);
    }

    /**
     * Loads connections from the selected CSV file into all data structures.
     *
     * @param graph The Graph data structure
     * @param matrixGraph The AdjacencyMatrixGraph data structure
     * @param hash The Hash data structure
     * @throws IOException If an I/O error occurs
     */
    private void loadConnections(Graph graph, AdjacencyMatrixGraph matrixGraph, Hash hash) throws IOException {
        CSVDataLoader.loadConnections(graph, connectionsFilePath);
        CSVDataLoader.loadConnections(matrixGraph, connectionsFilePath);
        CSVDataLoader.loadConnections(hash, connectionsFilePath);
    }

    /**
     * Runs performance tests on all data structures.
     *
     * @param graph The Graph data structure
     * @param matrixGraph The AdjacencyMatrixGraph data structure
     * @param hash The Hash data structure
     */
    private void runPerformanceTests(Graph graph, AdjacencyMatrixGraph matrixGraph, Hash hash) {
        // Get a sample of contacts for testing
        List<Contact> contacts = graph.listAllContacts();
        List<Contact> sampleContacts = getSampleContacts(contacts, 5);

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
    }

    /**
     * Gets a random sample of contacts from a list.
     *
     * @param contacts The list of contacts to sample from
     * @param sampleSize The number of contacts to sample
     * @return A list of sampled contacts
     */
    private List<Contact> getSampleContacts(List<Contact> contacts, int sampleSize) {
        List<Contact> sample = new ArrayList<>();
        int size = Math.min(sampleSize, contacts.size());

        for (int i = 0; i < size; i++) {
            int randomIndex = (int) (Math.random() * contacts.size());
            sample.add(contacts.get(randomIndex));
        }

        return sample;
    }

    /**
     * Shows the visualization of performance data.
     */
    private void showVisualization() {
        BorderPane root = createVisualizationUI();
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Performance Visualization - CSV Data");
    }

    /**
     * Creates the UI for visualizing performance data.
     *
     * @return A BorderPane containing the visualization UI
     */
    private BorderPane createVisualizationUI() {
        BorderPane root = new BorderPane();

        // Create pie chart UI directly
        VBox pieChartUI = createPieChartUI();

        // Add pie chart UI to root
        root.setCenter(pieChartUI);

        return root;
    }



    /**
     * Creates the UI for pie chart visualization.
     *
     * @return A VBox containing the pie chart UI
     */
    private VBox createPieChartUI() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        // Create operation selector
        HBox selectorBox = new HBox(10);
        Label operationLabel = new Label("Select Operation:");
        ComboBox<String> operationSelector = new ComboBox<>();

        // Get all operations
        List<String> operations = getOperations();
        operationSelector.getItems().addAll(operations);

        operationSelector.setPromptText("Select an operation");

        selectorBox.getChildren().addAll(operationLabel, operationSelector);

        // Create metric selector
        HBox metricSelectorBox = new HBox(10);
        Label metricLabel = new Label("Select Metric:");
        ComboBox<String> metricSelector = new ComboBox<>();
        metricSelector.getItems().addAll("Execution Time (ms)", "Memory Usage (KB)");
        metricSelector.setPromptText("Select a metric");

        metricSelectorBox.getChildren().addAll(metricLabel, metricSelector);

        // Create pie chart
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Performance Distribution");

        // Update chart when selections change
        operationSelector.setOnAction(e -> updatePieChart(pieChart, operationSelector.getValue(), metricSelector.getValue()));
        metricSelector.setOnAction(e -> updatePieChart(pieChart, operationSelector.getValue(), metricSelector.getValue()));

        // Set initial chart title
        pieChart.setTitle("Please select an operation and metric");

        // Set VBox properties
        VBox.setVgrow(pieChart, Priority.ALWAYS);
        container.getChildren().addAll(selectorBox, metricSelectorBox, pieChart);

        return container;
    }

    /**
     * Updates the pie chart with the selected operation and metric.
     *
     * @param pieChart The pie chart to update
     * @param operation The selected operation
     * @param metricType The selected metric type
     */
    private void updatePieChart(PieChart pieChart, String operation, String metricType) {
        // Check if operation or metric type is null
        if (operation == null || metricType == null) {
            pieChart.setTitle("Please select an operation and metric");
            pieChart.setData(FXCollections.observableArrayList());
            return;
        }

        boolean isTimeMetric = metricType.startsWith("Execution Time");

        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        // Calculate total value for percentage calculation
        double totalValue = 0;
        Map<String, Double> values = new HashMap<>();

        for (String dataStructure : performanceData.keySet()) {
            Map<String, List<PerformanceMetric>> dsData = performanceData.get(dataStructure);
            List<PerformanceMetric> metrics = dsData.get(operation);

            if (metrics != null && !metrics.isEmpty()) {
                double avgValue;

                if (isTimeMetric) {
                    avgValue = metrics.stream()
                        .mapToDouble(PerformanceMetric::getExecutionTimeMillis)
                        .average()
                        .orElse(0);
                } else {
                    avgValue = metrics.stream()
                        .mapToDouble(PerformanceMetric::getMemoryUsedKB)
                        .average()
                        .orElse(0);
                }

                values.put(dataStructure, avgValue);
                totalValue += avgValue;
            }
        }

        // Create pie chart slices
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            String dataStructure = entry.getKey();
            double value = entry.getValue();
            double percentage = (totalValue > 0) ? (value / totalValue) * 100 : 0;

            String label = String.format("%s (%.1f%%)", dataStructure, percentage);
            pieChartData.add(new PieChart.Data(label, value));
        }

        pieChart.setData(pieChartData);

        if (isTimeMetric) {
            pieChart.setTitle("Execution Time Distribution - " + operation);
        } else {
            pieChart.setTitle("Memory Usage Distribution - " + operation);
        }
    }

    /**
     * Gets the list of operations from the performance data.
     *
     * @return A list of operation names
     */
    private List<String> getOperations() {
        List<String> operations = new ArrayList<>();

        for (Map<String, List<PerformanceMetric>> dsData : performanceData.values()) {
            for (String op : dsData.keySet()) {
                if (!operations.contains(op)) {
                    operations.add(op);
                }
            }
        }

        return operations;
    }

    /**
     * Main method to launch the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
