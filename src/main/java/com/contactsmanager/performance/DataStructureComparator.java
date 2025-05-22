package com.contactsmanager.performance;

import com.contactsmanager.interfaces.ContactsManager;
import com.contactsmanager.interfaces.ConnectionsManager;
import com.contactsmanager.model.Contact;
import com.contactsmanager.visualization.VisualizationLauncher;

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
    // Stores different implementations of contact management systems.
    private final List<ContactsManager> contactStructures;
    private final List<ConnectionsManager> connectionStructures; // NEW

    // Names for each ContactsManager, used in logs and output.
    private final List<String> structureNames;
    private final Map<String, Map<String, List<PerformanceMetric>>> results;
    private final int runs;

    /**
     * Creates a new DataStructureComparator.
     *
     * @param runs The number of runs to average over for each operation
     */
    public DataStructureComparator(int runs) {
        this.contactStructures = new ArrayList<>();
        this.connectionStructures = new ArrayList<>();
        this.structureNames = new ArrayList<>();
        this.results = new HashMap<>();
        this.runs = runs;
    }

    /**
     * Adds a data structure to be compared, for both contact-and-connections structures. (Graphs)
     *
     * @param dataStructure The data structure implementation
     * @param name The name of the data structure
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator addDataStructure(ContactsManager contactMgr, ConnectionsManager connMgr, String name) {
        contactStructures.add(contactMgr);
        connectionStructures.add(connMgr);
        structureNames.add(name);
        results.put(name, new HashMap<>());
        return this;
    }

    /**
     * Adds data structure to be compared, but for contact-only structures.
     *
     * @param contactMgr The data structure implementation
     * @param name The name of the data structure
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator addDataStructure(ContactsManager contactMgr, String name) {
        contactStructures.add(contactMgr);
        connectionStructures.add(null); // Placeholder
        structureNames.add(name);
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
        return compareContactOperation(
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
        return compareContactOperationWithResult(
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
        return compareContactOperation(
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
        return compareContactOperation(
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
        return compareContactOperationWithResult(
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
        return compareConnectionOperation(
            "addConnection",
            cm -> cm.addConnection(contact1, contact2)
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
        return compareConnectionOperation(
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
        return compareConnectionOperationWithResult(
            "suggestContacts",
            ds -> ds.suggestContacts(contact)
        );
    }

    /**
     * Compares the performance of a contact-bound operation across all data structures.
     *
     * @param operationName The name of the operation
     * @param operation The operation to perform
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator compareContactOperation(String operationName, Consumer<ContactsManager> operation) {
        for (int i = 0; i < contactStructures.size(); i++) {
            ContactsManager ds = contactStructures.get(i);
            String name = structureNames.get(i);

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
     * Compares the performance of a contact-bound that returns a result across all data structures.
     *
     * @param <T> The type of the result
     * @param operationName The name of the operation
     * @param operation The operation to perform
     * @return This DataStructureComparator for method chaining
     */
    public <T> DataStructureComparator compareContactOperationWithResult(String operationName, Function<ContactsManager, T> operation) {
        for (int i = 0; i < contactStructures.size(); i++) {
            ContactsManager ds = contactStructures.get(i);
            String name = structureNames.get(i);

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
     * Compares the performance of a connection-bound operation across all data structures.
     *
     * @param operationName The name of the operation
     * @param operation The operation to perform
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator compareConnectionOperation(String operationName, Consumer<ConnectionsManager> operation) {
        for (int i = 0; i < connectionStructures.size(); i++) {
            ConnectionsManager cm = connectionStructures.get(i);
            String name = structureNames.get(i);

            if (cm == null) {
                System.out.printf("[SKIPPED] %s - %s: No connection manager implemented.\n", name, operationName);
                continue;
            }

            PerformanceMetric metric = PerformanceMeasurement.measureAverage(
                    () -> operation.accept(cm),
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
     * Compares the performance of a connection-bound that returns a result across all data structures.
     *
     * @param <T> The type of the result
     * @param operationName The name of the operation
     * @param operation The operation to perform
     * @return This DataStructureComparator for method chaining
     */
    public <T> DataStructureComparator compareConnectionOperationWithResult(String operationName, Function<ConnectionsManager, T> operation) {
        for (int i = 0; i < connectionStructures.size(); i++) {
            ConnectionsManager cm = connectionStructures.get(i);
            String name = structureNames.get(i);

            PerformanceMeasurement.MeasuredResult<T> measuredResult = PerformanceMeasurement.measureWithResult(
                    () -> operation.apply(cm),
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
     * Prints a summary of the performance comparison results.
     */
    public void printSummary() {
        System.out.println("\n===== PERFORMANCE METRICS SUMMARY =====");

        // Find all unique operation names
        List<String> operations = new ArrayList<>();
        for (Map<String, List<PerformanceMetric>> dsResults : results.values()) {
            for (String op : dsResults.keySet()) {
                if (!operations.contains(op)) {
                    operations.add(op);
                }
            }
        }

        // Print results for each operation and data structure
        for (String op : operations) {
            for (String dsName : structureNames) {
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

                    System.out.printf("[TEST DATA] %s - %s: Time: %.3f ms, Memory: %.2f KB\n",
                        dsName, op, avgTime, avgMemory);
                }
            }
        }
        System.out.println("========================================");
    }
}
