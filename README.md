# Contacts Manager - Data Structure Implementation and Performance Analysis

## Project Overview

This project implements a contacts management system using different data structure approaches to analyze and compare their performance characteristics. The system is built using JavaFX for the user interface and provides comprehensive tools for performance analysis and visualization.

## Implementation Details

### Data Structures

The project implements three distinct data structures for managing contacts and their relationships:

1. **Adjacency Matrix Graph (AdjacencyMatrixGraphCB)**
   - Fixed-size implementation using a byte matrix
   - Space Complexity: O(n²) where n is the maximum number of contacts
   - Efficient for dense graphs with many connections
   - Quick connection lookups with O(1) time complexity
   - Memory intensive for sparse connections

2. **Graph (Adjacency List)**
   - Dynamic size implementation using HashMap of LinkedLists
   - Space Complexity: O(V + E) where V is vertices and E is edges
   - Efficient for sparse graphs
   - Better memory utilization for fewer connections
   - Slower connection lookups compared to matrix

3. **Hash-based Implementation**
   - Uses HashMap for O(1) contact lookups
   - Efficient for basic contact management
   - Less suitable for relationship management
   - Best for scenarios prioritizing contact information over connections

### Core Features

1. **Contact Management**
   - Add/Remove contacts
   - Update contact information
   - Search contacts
   - List all contacts

2. **Connection Management**
   - Add/Remove connections between contacts
   - Suggest connections based on existing relationships (displayed in the top UI panel)
   - Support for both directed and undirected relationships

3. **Graph Traversal**
   - Breadth-First Search (BFS) implementation (outputs to terminal)
   - Depth-First Search (DFS) implementation (outputs to terminal)
   - Connection path analysis

### User Interface Output Behavior

The application uses a combination of GUI and terminal output:

1. **GUI Display**
   - Contact suggestions appear in the top panel (replacing "Please select a view mode")
   - Contact list and basic operations feedback shown in the main interface
   - Connection status updates in the UI

2. **Terminal Output**
   - BFS traversal results
   - DFS traversal results
   - Detailed operation logs and error messages
   - Performance metrics and debug information

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── contactsmanager/
│   │           └── contactsmanagerfx/
│   │               ├── dataStructures/
│   │               │   ├── AbstractGraphCB.java
│   │               │   ├── AdjacencyMatrixGraphCB.java
│   │               │   └── HashImplementation.java
│   │               ├── interfaces/
│   │               │   ├── ConnectionsManager.java
│   │               │   └── ContactsManager.java
│   │               ├── model/
│   │               │   └── Contact.java
│   │               ├── performance/
│   │               │   ├── PerformanceAnalyzer.java
│   │               │   └── MetricsCollector.java
│   │               ├── utility/
│   │               │   └── DataLoader.java
│   │               ├── App.java
│   │               ├── AppDisplayController.java
│   │               └── Main.java
│   └── resources/
│       └── com/
│           └── contactsmanager/
│               └── contactsmanagerfx/
│                   ├── css/
│                   │   └── stylesheet.css
│                   └── views/
│                       └── MainDisplay.fxml
└── test/
    └── java/
        └── com/
            └── contactsmanager/
                └── test/
                    ├── AdjacencyMatrixTests.java
                    ├── GraphTests.java
                    └── HashTests.java
```

## Performance Analysis

### Time Complexity Analysis

| Operation          | Adjacency Matrix | Adjacency List | Hash-based |
|-------------------|------------------|----------------|------------|
| Add Contact       | O(1)             | O(1)           | O(1)       |
| Delete Contact    | O(n)             | O(E)           | O(1)       |
| Add Connection    | O(1)             | O(1)           | O(1)       |
| Remove Connection | O(1)             | O(V)           | O(1)       |
| Search Contact    | O(n)             | O(1)           | O(1)       |
| Suggest Contacts  | O(n²)            | O(V + E)       | O(n)       |

Where:
- n = number of contacts
- V = number of vertices (contacts)
- E = number of edges (connections)

### Memory Usage Analysis

1. **Adjacency Matrix**
   - Fixed memory allocation: n² bytes for connections
   - Additional n * sizeof(Contact) for contact storage
   - Best for dense graphs (many connections)

2. **Adjacency List**
   - Dynamic memory allocation
   - Space proportional to actual connections
   - Efficient for sparse graphs
   - Overhead from LinkedList nodes

3. **Hash-based**
   - Linear space complexity O(n)
   - Minimal overhead
   - Most memory-efficient for basic contact storage

## Running the Application

### Prerequisites
- Java JDK 11 or higher
- JavaFX SDK
- Maven (for dependency management)
- Terminal/Console access (required for viewing traversal outputs)

### Build and Run
```bash
# Build the project
mvn clean install

# Run the application
java -jar target/contacts-manager.jar
```

### Usage Notes
1. **Viewing Traversal Results**
   - Keep the terminal window open while running the application
   - BFS and DFS results will be printed to the terminal
   - The terminal also displays detailed operation logs

2. **Contact Suggestions**
   - Suggestions appear in the top panel of the GUI
   - The text "Please select a view mode" will be replaced with suggestions
   - Suggestions are updated in real-time as connections change

### Testing
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AdjacencyMatrixTests
```

## Future Improvements

1. **Performance Optimizations**
   - Implement caching for frequently accessed contacts
   - Optimize memory usage in adjacency matrix for sparse graphs
   - Add indexing for faster search operations

2. **Feature Enhancements**
   - Add support for contact groups/categories
   - Implement advanced connection suggestion algorithms
   - Add support for contact metadata/attributes

3. **UI/UX Improvements**
   - Add graph visualization for connections
   - Implement batch operations
   - Add export/import functionality

## Conclusion

This implementation demonstrates the trade-offs between different data structures for contact management. The adjacency matrix provides fast connection operations but uses more memory, while the adjacency list is more memory-efficient but slower for some operations. The hash-based implementation offers the best performance for basic contact management but lacks efficient relationship management capabilities.

The choice of data structure should depend on the specific use case:
- Use Adjacency Matrix for dense networks with frequent connection queries
- Use Adjacency List for sparse networks with memory constraints
- Use Hash-based implementation for simple contact management without complex relationships

## Contributors
Anastasia Larasati (2802547692)
Kenny Tang (2802517733)
Tiffany Widjaja (2802503791)
[OOP Class & Data Structures Class]
[BINUS International]
