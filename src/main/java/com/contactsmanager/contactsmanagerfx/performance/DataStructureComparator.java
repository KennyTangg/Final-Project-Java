package com.contactsmanager.contactsmanagerfx.performance;

import com.contactsmanager.contactsmanagerfx.interfaces.ContactsManager;
import com.contactsmanager.contactsmanagerfx.interfaces.ConnectionsManager;
import com.contactsmanager.contactsmanagerfx.model.Contact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.Comparator;

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
    private static final double OUTLIER_THRESHOLD = 1.5; // IQR multiplier for outlier detection

    private int currentBatchSize = 0; // Add this field to track the actual batch size
    private static final int MAX_MATRIX_SIZE = 10000; // Maximum size for adjacency matrix
    private static final int BATCH_SIZE = 100; // Smaller batch size for adding contacts
    private static final long MEMORY_STABILIZATION_DELAY = 200; // ms

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
     * @param contactMgr The data structure implementation of contacts
     * @param connMgr The data structure implementation of connections
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
        currentBatchSize = contact.getStudentId();
        System.out.printf("\nTesting with %d contacts...\n", currentBatchSize);
        
        for (int i = 0; i < contactStructures.size(); i++) {
            String name = structureNames.get(i);
            List<PerformanceMetric> runMetrics = new ArrayList<>();
            
            // For matrix, use a more reasonable size
            int matrixSize = Math.min(currentBatchSize, MAX_MATRIX_SIZE);
            if (name.equals("Adjacency Matrix")) {
                System.out.printf("Using matrix size: %d x %d\n", matrixSize, matrixSize);
            }
            
            for (int run = 0; run < runs; run++) {
                ContactsManager ds;
                if (contactStructures.get(i) instanceof com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyListGraphCB) {
                    ds = new com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyListGraphCB();
                } else if (contactStructures.get(i) instanceof com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyMatrixGraphCB) {
                    ds = new com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyMatrixGraphCB(matrixSize);
                } else {
                    ds = new com.contactsmanager.contactsmanagerfx.dataStructures.HashMapCB();
                }

                // Get initial memory
                long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                
                // Start timing
                long startTime = System.nanoTime();
                
                // Add contacts in smaller batches
                int batchSize = currentBatchSize;
                for (int j = 0; j < batchSize; j += BATCH_SIZE) {
                    int currentBatch = Math.min(BATCH_SIZE, batchSize - j);
                    for (int k = 0; k < currentBatch; k++) {
                        Contact temp = new Contact(contact.getName() + run + "_" + (j + k), contact.getStudentId() + run + j + k);
                        ds.addContact(temp);
                    }
                    
                    // Stabilize memory between batches
                    if (j + BATCH_SIZE < batchSize) {
                        System.gc();
                        try { Thread.sleep(MEMORY_STABILIZATION_DELAY); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    }
                }
                
                long endTime = System.nanoTime();
                long totalTime = endTime - startTime;
                
                // Wait for memory to stabilize
                try { Thread.sleep(MEMORY_STABILIZATION_DELAY); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                
                // Measure final memory
                long after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                long totalMemoryUsed = Math.max(0, after - before);
                
                // For matrix, add the theoretical memory used by the matrix itself
                if (name.equals("Adjacency Matrix")) {
                    // Each boolean in the matrix takes 1 byte
                    long matrixMemory = matrixSize * matrixSize;
                    totalMemoryUsed += matrixMemory;
                    System.out.printf("Matrix memory usage: %.2f KB%n", matrixMemory / 1024.0);
                } else if (name.equals("Adjacency List")) {
                    // Each LinkedList node takes ~24 bytes (object overhead + next pointer + data)
                    // Each HashMap entry takes ~36 bytes (Entry object + key + value + next)
                    long listMemory = currentBatchSize * 24; // LinkedList nodes
                    long mapMemory = currentBatchSize * 36;  // HashMap entries
                    totalMemoryUsed += (listMemory + mapMemory);
                    System.out.printf("List memory usage: %.2f KB%n", (listMemory + mapMemory) / 1024.0);
                } else if (name.equals("HashMap")) {
                    // Each HashMap entry takes ~36 bytes (Entry object + key + value + next)
                    long mapMemory = currentBatchSize * 36;
                    totalMemoryUsed += mapMemory;
                    System.out.printf("HashMap memory usage: %.2f KB%n", mapMemory / 1024.0);
                }
                
                // Verify the number of contacts actually added
                int actualCount = ds.listAllContacts().size();
                if (actualCount != batchSize) {
                    System.out.println("Warning: Expected " + batchSize + " contacts but found " + actualCount);
                }
                
                PerformanceMetric metric = new PerformanceMetric(
                    totalTime,
                    totalMemoryUsed,
                    name,
                    "addContact"
                );
                runMetrics.add(metric);
                
                // Print metrics for this run
                System.out.printf("Run %d - Time: %.2f ms, Memory: %.2f KB%n",
                    run + 1,
                    totalTime / 1_000_000.0,
                    totalMemoryUsed / 1024.0
                );
            }
            
            if (!runMetrics.isEmpty()) {
                PerformanceMetric finalMetric = removeOutliersAndAverage(runMetrics, name, "addContact");
                results.get(name).computeIfAbsent("addContact", k -> new ArrayList<>()).add(finalMetric);
            }
        }
        return this;
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
     * @param contact The contact to delete
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator compareDeleteContact(Contact contact) {
        for (int i = 0; i < contactStructures.size(); i++) {
            ContactsManager ds = contactStructures.get(i);
            String structureName = structureNames.get(i);

            List<PerformanceMetric> runMetrics = new ArrayList<>();
            for (int run = 0; run < runs; run++) {
                String nameToDelete = contact.getName() + run;
                PerformanceMetric metric = PerformanceMeasurement.measure(
                    () -> ds.deleteContact(nameToDelete),
                    structureName,
                    "deleteContact"
                );
                runMetrics.add(metric);
            }

            PerformanceMetric finalMetric = removeOutliersAndAverage(runMetrics, structureName, "deleteContact");
            results.get(structureName).computeIfAbsent("deleteContact", k -> new ArrayList<>()).add(finalMetric);
            System.out.println(finalMetric);
        }
        return this;
    }


    /**
     * Compares the performance of updating a contact's details across all data structures.
     * For each run, a contact with the name and student ID generated from the given base contact is updated.
     * The updated contact will have a new name and student ID generated using the provided prefix and starting value.
     *
     * @param contact The base contact used to generate the original entries (original name and student ID will have a run-specific suffix)
     * @param newNamePrefix The prefix used to generate the new name for the contact during update (e.g., "updated" → "updated0", "updated1", ...)
     * @param newStudentIdStart The starting student ID used for updates; each run increments from this value (e.g., 2000 → 2000, 2001, ...)
     * @return This DataStructureComparator for method chaining
     */

    public DataStructureComparator compareUpdateContact(Contact contact, String newNamePrefix, int newStudentIdStart) {
        for (int i = 0; i < contactStructures.size(); i++) {
            ContactsManager ds = contactStructures.get(i);
            String structureName = structureNames.get(i);

            List<PerformanceMetric> runMetrics = new ArrayList<>();
            for (int run = 0; run < runs; run++) {
                String originalName = contact.getName() + run;
                int originalId = contact.getStudentId() + run;
                String newName = newNamePrefix + run;
                int newId = newStudentIdStart + run;

                Contact originalContact = new Contact(originalName, originalId);
                PerformanceMetric metric = PerformanceMeasurement.measure(
                    () -> ds.updateContact(originalContact, newName, newId),
                    structureName,
                    "updateContact"
                );
                runMetrics.add(metric);
            }

            PerformanceMetric finalMetric = removeOutliersAndAverage(runMetrics, structureName, "updateContact");
            results.get(structureName).computeIfAbsent("updateContact", k -> new ArrayList<>()).add(finalMetric);
            System.out.println(finalMetric);
        }
        return this;
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
            cm -> cm.removeConnection(contact1, contact2)
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
            cm -> cm.suggestContacts(contact)
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

            List<PerformanceMetric> runMetrics = new ArrayList<>();
            for (int run = 0; run < runs; run++) {
                PerformanceMetric metric = PerformanceMeasurement.measure(
                    () -> operation.accept(ds),
                    name,
                    operationName
                );
                runMetrics.add(metric);
            }

            PerformanceMetric finalMetric = removeOutliersAndAverage(runMetrics, name, operationName);
            results.get(name).computeIfAbsent(operationName, k -> new ArrayList<>()).add(finalMetric);
            System.out.println(finalMetric);
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

            List<PerformanceMetric> runMetrics = new ArrayList<>();
            for (int run = 0; run < runs; run++) {
                PerformanceMeasurement.MeasuredResult<T> result = PerformanceMeasurement.measureWithResult(
                    () -> operation.apply(ds),
                    name,
                    operationName
                );
                runMetrics.add(result.getMetric());
            }

            PerformanceMetric finalMetric = removeOutliersAndAverage(runMetrics, name, operationName);
            results.get(name).computeIfAbsent(operationName, k -> new ArrayList<>()).add(finalMetric);
            System.out.println(finalMetric);
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

            List<PerformanceMetric> runMetrics = new ArrayList<>();
            for (int run = 0; run < runs; run++) {
                PerformanceMetric metric = PerformanceMeasurement.measure(
                    () -> operation.accept(cm),
                    name,
                    operationName
                );
                runMetrics.add(metric);
            }

            PerformanceMetric finalMetric = removeOutliersAndAverage(runMetrics, name, operationName);
            results.get(name).computeIfAbsent(operationName, k -> new ArrayList<>()).add(finalMetric);
            System.out.println(finalMetric);
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

            if (cm == null) {
                System.out.printf("[SKIPPED] %s - %s: No connection manager implemented.\n", name, operationName);
                continue;
            }

            List<PerformanceMetric> runMetrics = new ArrayList<>();
            for (int run = 0; run < runs; run++) {
                PerformanceMeasurement.MeasuredResult<T> result = PerformanceMeasurement.measureWithResult(
                    () -> operation.apply(cm),
                    name,
                    operationName
                );
                runMetrics.add(result.getMetric());
            }

            PerformanceMetric finalMetric = removeOutliersAndAverage(runMetrics, name, operationName);
            results.get(name).computeIfAbsent(operationName, k -> new ArrayList<>()).add(finalMetric);
            System.out.println(finalMetric);
        }
        return this;
    }

    private PerformanceMetric removeOutliersAndAverage(List<PerformanceMetric> metrics, String structureName, String operationName) {
        if (metrics.size() <= 2) {
            // If we have 2 or fewer measurements, just average them
            long totalTime = 0;
            long totalMemory = 0;
            for (PerformanceMetric metric : metrics) {
                totalTime += metric.getTimeNanos();
                totalMemory += metric.getMemoryBytes();
            }
            return new PerformanceMetric(
                totalTime / metrics.size(),
                totalMemory / metrics.size(),
                structureName,
                operationName
            );
        }

        // Sort metrics by execution time
        metrics.sort((a, b) -> Long.compare(a.getTimeNanos(), b.getTimeNanos()));

        // Calculate quartiles
        int q1Index = metrics.size() / 4;
        int q3Index = (3 * metrics.size()) / 4;
        long q1 = metrics.get(q1Index).getTimeNanos();
        long q3 = metrics.get(q3Index).getTimeNanos();
        long iqr = q3 - q1;

        // Remove outliers
        List<PerformanceMetric> filteredMetrics = new ArrayList<>();
        for (PerformanceMetric metric : metrics) {
            long time = metric.getTimeNanos();
            if (time >= q1 - OUTLIER_THRESHOLD * iqr && time <= q3 + OUTLIER_THRESHOLD * iqr) {
                filteredMetrics.add(metric);
            }
        }

        // Calculate average of remaining metrics
        long totalTime = 0;
        long totalMemory = 0;
        for (PerformanceMetric metric : filteredMetrics) {
            totalTime += metric.getTimeNanos();
            totalMemory += metric.getMemoryBytes();
        }

        return new PerformanceMetric(
            totalTime / filteredMetrics.size(),
            totalMemory / filteredMetrics.size(),
            structureName,
            operationName
        );
    }


    /**
     * Prints a summary of the performance comparison results.
     */
    public void printSummary() {
        System.out.println("\n===== PERFORMANCE METRICS SUMMARY =====");
        System.out.println("Batch Size: " + currentBatchSize + " contacts");
        System.out.println("\nTotal Metrics:");
        System.out.println("----------------------------------------");
        
        for (String structureName : structureNames) {
            Map<String, List<PerformanceMetric>> structureResults = results.get(structureName);
            for (Map.Entry<String, List<PerformanceMetric>> entry : structureResults.entrySet()) {
                String operation = entry.getKey();
                List<PerformanceMetric> metrics = entry.getValue();
                
                // Calculate total metrics
                long totalTime = 0;
                long totalMemory = 0;
                for (PerformanceMetric metric : metrics) {
                    totalTime += metric.getTimeNanos();
                    totalMemory += metric.getMemoryBytes();
                }
                
                // Calculate averages
                double avgTotalTime = totalTime / (double)metrics.size();
                double avgTotalMemory = totalMemory / (double)metrics.size();
                
                System.out.printf("%s - %s:%n", structureName, operation);
                System.out.printf("  Total Time: %.2f ms%n", avgTotalTime / 1_000_000.0);
                System.out.printf("  Total Memory: %.2f KB%n", avgTotalMemory / 1024.0);
                if (metrics.size() > 1) {
                    System.out.printf("  Per Contact - Time: %.6f ms, Memory: %.2f KB%n", 
                        (avgTotalTime / 1_000_000.0) / currentBatchSize,
                        (avgTotalMemory / 1024.0) / currentBatchSize);
                }
            }
        }
        System.out.println("========================================");
    }
}
