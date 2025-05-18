# Contacts Manager - Data Structure Performance Comparison

This project implements a contacts management system using different data structures and provides tools to compare their performance. It includes a basic JavaFX application for visualization.

## Project Structure

```
src/
├── main/
│   └── java/
│       └── com/
│           └── contactsmanager/
│               ├── DataStructures/       # Data structure implementations
│               │   ├── AdjacencyMatrixGraph.java
│               │   ├── Graph.java        # Adjacency List implementation
│               │   └── Hash.java         # HashMap implementation
│               ├── interfaces/           # Interfaces
│               │   ├── ConnectionManager.java
│               │   └── ContactsManager.java
│               ├── model/                # Data models
│               │   └── Contact.java
│               ├── performance/          # Performance measurement tools
│               │   ├── DataStructureComparator.java
│               │   ├── PerformanceMeasurement.java
│               │   ├── PerformanceMetric.java
│               │   └── PerformanceTest.java
│               ├── utils/                # Utility classes
│               │   └── CSVDataLoader.java
│               ├── visualization/        # Visualization components
│               │   ├── App.java          # Basic JavaFX application
│               │   └── VisualizationLauncher.java
│               ├── Main.java             # Main application entry point
│               └── PerformanceTestRunner.java # Command-line runner for performance tests
└── test/
    └── java/
        └── com/
            └── contactsmanager/
                └── test/                 # Test classes
                    ├── AdjacencyMatrixTest.java
                    ├── GraphTest.java
                    └── HashTest.java
```

## Data Files

The project includes CSV data files for testing:

```
datas/
├── Random_Connections_List_100.csv   # 100 random connections
├── Random_Connections_List_1000.csv  # 1000 random connections
├── Random_Names_List_100.csv         # 100 random contacts
└── Random_Names_List_1000.csv        # 1000 random contacts
```

## Data Structures

The project implements the `ContactsManager` interface using three different data structures:

1. **Graph (Adjacency List)** - Uses a HashMap of LinkedLists to represent contacts and their connections
2. **AdjacencyMatrixGraph** - Uses a matrix to represent connections between contacts
3. **Hash** - Uses a HashMap to store contacts and their connections

## Performance Comparison

The project includes a performance comparison framework that allows you to:

1. Measure execution time and memory usage for different operations
2. Compare the performance of different data structure implementations
3. Run various test scenarios (basic, comprehensive, custom)

## Running the Application

### Main Application

```
java com.contactsmanager.Main
```

This will launch the interactive menu where you can:
- Run different performance tests
- Launch the basic JavaFX application

### Performance Test Runner

```
java com.contactsmanager.PerformanceTestRunner [test-type] [options]
```

Available test types:
- `custom <contactCount> <operations>` - Runs a custom test with specific operations

Examples:
```
java com.contactsmanager.PerformanceTestRunner custom 100 add,search,list
```

## Test Classes

The project includes test classes for each data structure implementation:

- `GraphTest` - Tests the Adjacency List implementation
- `AdjacencyMatrixTest` - Tests the Adjacency Matrix implementation
- `HashTest` - Tests the HashMap implementation

## Performance Measurement

The performance measurement framework includes:

- `PerformanceMetric` - Represents a performance measurement with time and memory usage
- `PerformanceMeasurement` - Utility for measuring performance
- `DataStructureComparator` - Compares performance across different data structures
- `PerformanceTest` - Predefined test scenarios
- `CSVDataLoader` - Utility for loading contacts and connections from CSV files

## Visualization

The project includes a basic JavaFX application that can be launched from:
- The main menu (option 2)

The visualization components include:
- `App` - A basic JavaFX application
- `VisualizationLauncher` - Utility for launching the JavaFX application

## Contact Model

The `Contact` class represents a contact with:
- Name
- Student ID
