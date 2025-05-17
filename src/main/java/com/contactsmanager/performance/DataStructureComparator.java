package com.contactsmanager.performance;

import com.contactsmanager.interfaces.ContactsManager;
import com.contactsmanager.model.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A utility class for comparing the performance of different data structure implementations.
 */
public class DataStructureComparator {
    private final List<ContactsManager> dataStructures;
    private final List<String> dataStructureNames;
    private final Map<String, Map<String, List<PerformanceMetric>>> results;
    private final int runs;

    /**
     * Creates a new DataStructureComparator.
     *
     * @param runs The number of runs to average over for each operation
     */
    public DataStructureComparator(int runs) {
        this.dataStructures = new ArrayList<>();
        this.dataStructureNames = new ArrayList<>();
        this.results = new HashMap<>();
        this.runs = runs;
    }

    /**
     * Adds a data structure to be compared.
     *
     * @param dataStructure The data structure implementation
     * @param name The name of the data structure
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator addDataStructure(ContactsManager dataStructure, String name) {
        dataStructures.add(dataStructure);
        dataStructureNames.add(name);
        results.put(name, new HashMap<>());
        return this;
    }

    /**
     * Compares the performance of adding a contact across all data structures.
     *
     * @param contact The contact to add
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator compareAddContact(Contact contact) {
        return compareOperation(
            "addContact",
            ds -> ds.addContact(contact)
        );
    }

    /**
     * Compares the performance of searching for a contact across all data structures.
     *
     * @param name The name to search for
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator compareSearchContact(String name) {
        return compareOperationWithResult(
            "searchContact",
            ds -> ds.searchContact(name)
        );
    }

    /**
     * Compares the performance of deleting a contact across all data structures.
     *
     * @param name The name of the contact to delete
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator compareDeleteContact(String name) {
        return compareOperation(
            "deleteContact",
            ds -> ds.deleteContact(name)
        );
    }

    /**
     * Compares the performance of updating a contact across all data structures.
     *
     * @param contact The contact to update
     * @param newName The new name
     * @param newStudentId The new student ID
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator compareUpdateContact(Contact contact, String newName, int newStudentId) {
        return compareOperation(
            "updateContact",
            ds -> ds.updateContact(contact, newName, newStudentId)
        );
    }

    /**
     * Compares the performance of listing all contacts across all data structures.
     *
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator compareListAllContacts() {
        return compareOperationWithResult(
            "listAllContacts",
            ContactsManager::listAllContacts
        );
    }

    /**
     * Compares the performance of adding a connection across all data structures.
     *
     * @param contact1 The first contact
     * @param contact2 The second contact
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator compareAddConnection(String contact1, String contact2) {
        return compareOperation(
            "addConnection",
            ds -> ds.addConnection(contact1, contact2)
        );
    }

    /**
     * Compares the performance of removing a connection across all data structures.
     *
     * @param contact1 The first contact
     * @param contact2 The second contact
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator compareRemoveConnection(String contact1, String contact2) {
        return compareOperation(
            "removeConnection",
            ds -> ds.removeConnection(contact1, contact2)
        );
    }

    /**
     * Compares the performance of suggesting contacts across all data structures.
     *
     * @param contact The contact to get suggestions for
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator compareSuggestContacts(String contact) {
        return compareOperationWithResult(
            "suggestContacts",
            ds -> ds.suggestContacts(contact)
        );
    }

    /**
     * Compares the performance of a custom operation across all data structures.
     *
     * @param operationName The name of the operation
     * @param operation The operation to perform
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator compareOperation(String operationName, Consumer<ContactsManager> operation) {
        for (int i = 0; i < dataStructures.size(); i++) {
            ContactsManager ds = dataStructures.get(i);
            String name = dataStructureNames.get(i);
            
            PerformanceMetric metric = PerformanceMeasurement.measureAverage(
                () -> operation.accept(ds),
                name,
                operationName,
                runs
            );
            
            results.get(name).computeIfAbsent(operationName, k -> new ArrayList<>()).add(metric);
            System.out.println(metric);
        }
        
        return this;
    }

    /**
     * Compares the performance of a custom operation that returns a result across all data structures.
     *
     * @param <T> The type of the result
     * @param operationName The name of the operation
     * @param operation The operation to perform
     * @return This DataStructureComparator for method chaining
     */
    public <T> DataStructureComparator compareOperationWithResult(String operationName, Function<ContactsManager, T> operation) {
        for (int i = 0; i < dataStructures.size(); i++) {
            ContactsManager ds = dataStructures.get(i);
            String name = dataStructureNames.get(i);
            
            PerformanceMeasurement.MeasuredResult<T> measuredResult = PerformanceMeasurement.measureWithResult(
                () -> operation.apply(ds),
                name,
                operationName
            );
            
            PerformanceMetric metric = measuredResult.getMetric();
            results.get(name).computeIfAbsent(operationName, k -> new ArrayList<>()).add(metric);
            System.out.println(metric);
        }
        
        return this;
    }

    /**
     * Gets the results of all performance comparisons.
     *
     * @return A map of data structure names to operation names to performance metrics
     */
    public Map<String, Map<String, List<PerformanceMetric>>> getResults() {
        return results;
    }

    /**
     * Prints a summary of the performance comparison results.
     */
    public void printSummary() {
        System.out.println("\n===== Performance Comparison Summary =====");
        
        // Find all unique operation names
        List<String> operations = new ArrayList<>();
        for (Map<String, List<PerformanceMetric>> dsResults : results.values()) {
            for (String op : dsResults.keySet()) {
                if (!operations.contains(op)) {
                    operations.add(op);
                }
            }
        }
        
        // Print header
        System.out.print("Operation");
        for (String dsName : dataStructureNames) {
            System.out.print("\t| " + dsName + " (time ms)");
            System.out.print("\t| " + dsName + " (memory KB)");
        }
        System.out.println();
        
        // Print separator
        System.out.print("-".repeat(10));
        for (int i = 0; i < dataStructureNames.size(); i++) {
            System.out.print("-".repeat(40));
        }
        System.out.println();
        
        // Print results for each operation
        for (String op : operations) {
            System.out.print(op);
            
            for (String dsName : dataStructureNames) {
                Map<String, List<PerformanceMetric>> dsResults = results.get(dsName);
                List<PerformanceMetric> metrics = dsResults.get(op);
                
                if (metrics != null && !metrics.isEmpty()) {
                    // Calculate average time and memory
                    double avgTime = metrics.stream()
                        .mapToDouble(PerformanceMetric::getExecutionTimeMillis)
                        .average()
                        .orElse(0);
                    
                    double avgMemory = metrics.stream()
                        .mapToDouble(PerformanceMetric::getMemoryUsedKB)
                        .average()
                        .orElse(0);
                    
                    System.out.printf("\t| %.3f", avgTime);
                    System.out.printf("\t| %.2f", avgMemory);
                } else {
                    System.out.print("\t| N/A");
                    System.out.print("\t| N/A");
                }
            }
            
            System.out.println();
        }
        
        System.out.println("========================================");
    }
}
