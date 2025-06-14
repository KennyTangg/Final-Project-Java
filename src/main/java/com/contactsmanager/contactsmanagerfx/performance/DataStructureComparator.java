package com.contactsmanager.contactsmanagerfx.performance;

import com.contactsmanager.contactsmanagerfx.interfaces.ContactsManager;
import com.contactsmanager.contactsmanagerfx.interfaces.ConnectionsManager;
import com.contactsmanager.contactsmanagerfx.model.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
     * Sets the current batch size for performance reporting.
     * This should be called to indicate how many contacts are in the data structures.
     *
     * @param batchSize The number of contacts in the data structures
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator setBatchSize(int batchSize) {
        this.currentBatchSize = batchSize;
        return this;
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
                final int currentRun = run; // Make effectively final for lambda

                // Show progress
                System.out.printf("  [%s] Run %d/%d... ", name, run + 1, runs);

                // Extended warmup for larger datasets to ensure JVM optimization
                int warmupRuns = Math.max(3, currentBatchSize / 2000);
                for (int w = 0; w < warmupRuns; w++) {
                    final int warmupIndex = w; // Make effectively final for lambda
                    ContactsManager warmupDs;
                    if (contactStructures.get(i) instanceof com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyListGraphCB) {
                        warmupDs = new com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyListGraphCB();
                    } else if (contactStructures.get(i) instanceof com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyMatrixGraphCB) {
                        warmupDs = new com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyMatrixGraphCB(matrixSize);
                    } else {
                        warmupDs = new com.contactsmanager.contactsmanagerfx.dataStructures.HashMapCB();
                    }

                    PerformanceMeasurement.suppressConsoleOutput(() -> {
                        int warmupSize = Math.min(200, currentBatchSize / 10);
                        for (int j = 0; j < warmupSize; j++) {
                            Contact temp = new Contact("Warmup" + warmupIndex + "_" + j, 999000 + warmupIndex * 1000 + j);
                            warmupDs.addContact(temp);
                        }
                    });
                }

                // Stabilize memory and get baseline
                System.gc();
                try { Thread.sleep(150); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

                // Create data structure and measure its memory footprint
                long beforeMemory = getUsedMemory();

                ContactsManager ds;
                if (contactStructures.get(i) instanceof com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyListGraphCB) {
                    ds = new com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyListGraphCB();
                } else if (contactStructures.get(i) instanceof com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyMatrixGraphCB) {
                    ds = new com.contactsmanager.contactsmanagerfx.dataStructures.AdjacencyMatrixGraphCB(matrixSize);
                } else {
                    ds = new com.contactsmanager.contactsmanagerfx.dataStructures.HashMapCB();
                }

                // Time the contact addition
                long startTime = System.nanoTime();
                PerformanceMeasurement.suppressConsoleOutput(() -> {
                    // Add all contacts without any interruptions or console output
                    for (int j = 0; j < currentBatchSize; j++) {
                        Contact temp = new Contact(contact.getName() + currentRun + "_" + j, contact.getStudentId() + currentRun + j);
                        ds.addContact(temp);
                    }
                });
                long endTime = System.nanoTime();

                // Measure total memory after everything is added
                long afterMemory = getUsedMemory();
                long rawDifference = afterMemory - beforeMemory;

                long timeTaken = endTime - startTime;

                // Use actual measurement only - no theoretical fallbacks
                long memoryUsed = Math.max(rawDifference, 0);
                System.out.printf("[DEBUG] %s - addContact: measured diff: %d bytes%n", name, memoryUsed);

                PerformanceMetric metric = new PerformanceMetric(timeTaken, memoryUsed, name, "addContact");

                // Verify the number of contacts actually added
                int actualCount = ds.listAllContacts().size();
                if (actualCount != currentBatchSize) {
                    System.out.println("Warning: Expected " + currentBatchSize + " contacts but found " + actualCount);
                }

                runMetrics.add(metric);

                // Print metrics for this run
                System.out.printf("Time: %s, Memory: %d bytes%n",
                    formatTime(metric.getTimeNanos() / 1_000_000.0),
                    metric.getMemoryBytes()
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
     * Generates realistic connections between contacts to make suggest feature meaningful.
     * Creates a network where each contact has 2-5 connections on average.
     *
     * @param connectionDensity The percentage of possible connections to create (0.0 to 1.0)
     * @return This DataStructureComparator for method chaining
     */
    public DataStructureComparator generateConnections(double connectionDensity) {
        System.out.println("\n=== Generating Realistic Connection Data ===");

        // Calculate target connections once for all data structures to ensure consistency
        int targetConnections = 0;
        int contactCount = 0;

        // Get contact count from the first available data structure
        for (int i = 0; i < connectionStructures.size(); i++) {
            if (connectionStructures.get(i) != null) {
                ContactsManager contactManager = contactStructures.get(i);
                List<Contact> allContacts = contactManager.listAllContacts();
                contactCount = allContacts.size();
                break;
            }
        }

        if (contactCount >= 2) {
            int maxPossibleConnections = (contactCount * (contactCount - 1)) / 2;

            // Use consistent calculation for all data structures
            if (contactCount <= 1000) {
                targetConnections = (int) (maxPossibleConnections * connectionDensity);
            } else {
                // For large datasets, use a fixed connections-per-contact approach
                int connectionsPerContact = Math.max(3, (int)(contactCount * 0.01));
                connectionsPerContact = Math.min(connectionsPerContact, 50);
                targetConnections = (contactCount * connectionsPerContact) / 2;
            }

            // Apply consistent limits
            int minConnectionsPerContact = Math.min(3, contactCount - 1);
            int minTotalConnections = (contactCount * minConnectionsPerContact) / 2;
            targetConnections = Math.max(targetConnections, minTotalConnections);

            int maxReasonableConnections = Math.min(maxPossibleConnections, contactCount * 20);
            targetConnections = Math.min(targetConnections, maxReasonableConnections);

            double actualDensity = (double) targetConnections / maxPossibleConnections * 100;
            System.out.printf("Target connections for all structures: %d (%.3f%% density)\n", targetConnections, actualDensity);
        }

        for (int i = 0; i < connectionStructures.size(); i++) {
            ConnectionsManager cm = connectionStructures.get(i);
            String name = structureNames.get(i);

            if (cm == null) {
                System.out.printf("[SKIPPED] %s - No connection manager implemented.\n", name);
                continue;
            }

            // Get all contacts from the corresponding contact manager
            ContactsManager contactManager = contactStructures.get(i);
            List<Contact> allContacts = contactManager.listAllContacts();

            if (allContacts.size() < 2) {
                System.out.printf("[SKIPPED] %s - Need at least 2 contacts to create connections.\n", name);
                continue;
            }

            System.out.printf("Generating connections for %s with %d contacts...\n", name, allContacts.size());

            // Calculate max possible connections for this specific data structure
            int maxPossibleConnections = (allContacts.size() * (allContacts.size() - 1)) / 2;

            // Create connections using a random but balanced approach
            java.util.Random random = new java.util.Random(42); // Fixed seed for reproducible results
            java.util.Set<String> createdConnections = new java.util.HashSet<>();
            int connectionsCreated = 0;

            // Progress tracking
            int progressInterval = Math.max(1, targetConnections / 20); // Show progress every 5%
            int nextProgressUpdate = progressInterval;

            // First pass: Ensure each contact has at least one connection
            for (int j = 0; j < allContacts.size() && connectionsCreated < targetConnections; j++) {
                Contact contact1 = allContacts.get(j);

                // Find a random contact to connect to
                for (int attempts = 0; attempts < 10 && connectionsCreated < targetConnections; attempts++) {
                    int randomIndex = random.nextInt(allContacts.size());
                    if (randomIndex == j) continue; // Skip self

                    Contact contact2 = allContacts.get(randomIndex);
                    String connectionKey = createConnectionKey(contact1.getName(), contact2.getName());

                    if (!createdConnections.contains(connectionKey)) {
                        PerformanceMeasurement.suppressConsoleOutput(() ->
                            cm.addConnection(contact1.getName(), contact2.getName()));
                        createdConnections.add(connectionKey);
                        connectionsCreated++;

                        // Show progress
                        if (connectionsCreated >= nextProgressUpdate) {
                            double progress = (double) connectionsCreated / targetConnections * 100;
                            System.out.printf("  Progress: %d/%d connections (%.1f%%)%n",
                                connectionsCreated, targetConnections, progress);
                            nextProgressUpdate += progressInterval;
                        }
                        break;
                    }
                }
            }

            // Second pass: Add remaining connections randomly
            while (connectionsCreated < targetConnections) {
                int index1 = random.nextInt(allContacts.size());
                int index2 = random.nextInt(allContacts.size());

                if (index1 == index2) continue; // Skip self-connections

                Contact contact1 = allContacts.get(index1);
                Contact contact2 = allContacts.get(index2);
                String connectionKey = createConnectionKey(contact1.getName(), contact2.getName());

                if (!createdConnections.contains(connectionKey)) {
                    PerformanceMeasurement.suppressConsoleOutput(() ->
                        cm.addConnection(contact1.getName(), contact2.getName()));
                    createdConnections.add(connectionKey);
                    connectionsCreated++;

                    // Show progress
                    if (connectionsCreated >= nextProgressUpdate) {
                        double progress = (double) connectionsCreated / targetConnections * 100;
                        System.out.printf("  Progress: %d/%d connections (%.1f%%)%n",
                            connectionsCreated, targetConnections, progress);
                        nextProgressUpdate += progressInterval;
                    }
                }

                // Prevent infinite loop if we can't create more unique connections
                if (createdConnections.size() >= maxPossibleConnections) {
                    break;
                }
            }

            System.out.printf("Created %d connections for %s\n", connectionsCreated, name);
        }

        System.out.println("=== Connection Generation Complete ===\n");
        return this;
    }

    /**
     * Helper method to create a consistent connection key for tracking created connections.
     */
    private String createConnectionKey(String name1, String name2) {
        // Ensure consistent ordering to avoid duplicate connections
        if (name1.compareTo(name2) < 0) {
            return name1 + ":" + name2;
        } else {
            return name2 + ":" + name1;
        }
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
                // Show progress
                System.out.printf("  [%s] Run %d/%d... ", structureName, run + 1, runs);

                // For individual operations, use amplified measurement to get detectable signal
                System.gc();
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                long beforeMemory = getUsedMemory();

                String nameToDelete = contact.getName() + run;

                // Amplify the operation significantly to get measurable memory usage like addContact
                int amplificationFactor = Math.max(1000, currentBatchSize); // Much higher amplification to match addContact behavior
                long startTime = System.nanoTime();
                for (int amp = 0; amp < amplificationFactor; amp++) {
                    final int ampFinal = amp; // Make effectively final for lambda
                    PerformanceMeasurement.suppressConsoleOutput(() -> ds.deleteContact(nameToDelete + "_amp" + ampFinal));
                }
                long endTime = System.nanoTime();

                // Measure memory after amplified operations
                long afterMemory = getUsedMemory();
                long rawDifference = afterMemory - beforeMemory;
                System.out.printf("[DEBUG] %s - deleteContact: beforeMemory: %d bytes, afterMemory: %d bytes, diff: %d bytes%n",
                    structureName, beforeMemory, afterMemory, rawDifference);

                // Calculate per-operation metrics
                long timeTaken = (endTime - startTime) / amplificationFactor;

                // Use actual measurement only - no artificial fallbacks
                long memoryUsed = Math.max(rawDifference / amplificationFactor, 0);
                System.out.printf("[DEBUG] %s - deleteContact: measured diff: %d bytes, per-operation: %d bytes%n",
                    structureName, rawDifference, memoryUsed);

                System.out.printf("[DEBUG] %s - deleteContact: final memoryUsed: %d bytes (%.2f KB)%n",
                    structureName, memoryUsed, memoryUsed / 1024.0);

                PerformanceMetric metric = new PerformanceMetric(timeTaken, memoryUsed, structureName, "deleteContact");
                runMetrics.add(metric);
                System.out.printf("Time: %s, Memory: %d bytes%n",
                    formatTime(timeTaken / 1_000_000.0), memoryUsed);
            }

            PerformanceMetric finalMetric = removeOutliersAndAverage(runMetrics, structureName, "deleteContact");
            results.get(structureName).computeIfAbsent("deleteContact", k -> new ArrayList<>()).add(finalMetric);

            // Show only actual measurements
            System.out.printf("%s%n", finalMetric);
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
                // Show progress
                System.out.printf("  [%s] Run %d/%d... ", structureName, run + 1, runs);

                // Use stable memory measurement approach
                long beforeMemory = getStableMemoryMeasurement();

                String originalName = contact.getName() + run;
                int originalId = contact.getStudentId() + run;
                String newName = newNamePrefix + run;
                int newId = newStudentIdStart + run;

                // Use consistent amplification factor that scales with data size to ensure realistic measurements
                int amplificationFactor = Math.max(100, Math.min(2000, currentBatchSize / 2));
                long startTime = System.nanoTime();
                for (int amp = 0; amp < amplificationFactor; amp++) {
                    final int ampFinal = amp; // Make effectively final for lambda
                    Contact originalContact = new Contact(originalName + "_amp" + ampFinal, originalId + ampFinal);
                    PerformanceMeasurement.suppressConsoleOutput(() -> ds.updateContact(originalContact, newName + "_amp" + ampFinal, newId + ampFinal));
                }
                long endTime = System.nanoTime();

                // Use stable memory measurement after operations
                long afterMemory = getStableMemoryMeasurement();
                long rawDifference = afterMemory - beforeMemory;
                System.out.printf("[DEBUG] %s - updateContact: beforeMemory: %d bytes, afterMemory: %d bytes, diff: %d bytes%n",
                    structureName, beforeMemory, afterMemory, rawDifference);

                // Calculate per-operation metrics
                long timeTaken = (endTime - startTime) / amplificationFactor;

                // Use actual measurement only - no artificial calculations
                long memoryUsed = Math.max(rawDifference / amplificationFactor, 0);
                System.out.printf("[DEBUG] %s - updateContact: measured diff: %d bytes, per-operation: %d bytes%n",
                    structureName, rawDifference, memoryUsed);

                System.out.printf("[DEBUG] %s - updateContact: final memoryUsed: %d bytes (%.2f KB)%n",
                    structureName, memoryUsed, memoryUsed / 1024.0);

                PerformanceMetric metric = new PerformanceMetric(timeTaken, memoryUsed, structureName, "updateContact");
                runMetrics.add(metric);
                System.out.printf("Time: %s, Memory: %d bytes%n",
                    formatTime(timeTaken / 1_000_000.0), memoryUsed);
            }

            PerformanceMetric finalMetric = removeOutliersAndAverage(runMetrics, structureName, "updateContact");
            results.get(structureName).computeIfAbsent("updateContact", k -> new ArrayList<>()).add(finalMetric);

            // Show only actual measurements
            System.out.printf("%s%n", finalMetric);
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
     * Compares the performance of a contact operation that returns a result across all data structures.
     * Uses device-agnostic measurement approach for consistent results across different environments.
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
                // Show progress
                System.out.printf("  [%s] Run %d/%d... ", name, run + 1, runs);

                PerformanceMetric metric = measureOperationRobustly(operationName, name, () -> operation.apply(ds));
                runMetrics.add(metric);
            }

            PerformanceMetric finalMetric = removeOutliersAndAverage(runMetrics, name, operationName);
            results.get(name).computeIfAbsent(operationName, k -> new ArrayList<>()).add(finalMetric);

            // Show only actual measurements
            System.out.printf("%s%n", finalMetric);
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
                System.out.printf("[SKIPPED] %s - %s: Connection operations not supported (contacts-only implementation).\n", name, operationName);

                // Add a placeholder result to maintain consistency in results
                PerformanceMetric placeholderMetric = new PerformanceMetric(0, 0, name, operationName);
                results.get(name).computeIfAbsent(operationName, k -> new ArrayList<>()).add(placeholderMetric);
                System.out.printf("Time: 0.000000 ms, Memory: 0 bytes\n");
                continue;
            }

            List<PerformanceMetric> runMetrics = new ArrayList<>();
            for (int run = 0; run < runs; run++) {
                // Show progress
                System.out.printf("  [%s] Run %d/%d... ", name, run + 1, runs);

                // Use the same direct memory measurement approach as addContact
                System.gc();
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                long beforeMemory = getUsedMemory();

                long startTime = System.nanoTime();
                PerformanceMeasurement.suppressConsoleOutput(() -> operation.accept(cm));
                long endTime = System.nanoTime();

                // Measure memory after operation
                long afterMemory = getUsedMemory();
                long rawDifference = afterMemory - beforeMemory;
                long timeTaken = endTime - startTime;

                // Handle negative memory differences (caused by GC during measurement)
                long memoryUsed;
                if (rawDifference < 0) {
                    // If negative, use 0 - don't artificially inflate
                    memoryUsed = 0;
                } else {
                    // Use actual measurement without artificial minimums
                    memoryUsed = rawDifference;
                }

                PerformanceMetric metric = new PerformanceMetric(timeTaken, memoryUsed, name, operationName);
                runMetrics.add(metric);
            }

            PerformanceMetric finalMetric = removeOutliersAndAverage(runMetrics, name, operationName);
            results.get(name).computeIfAbsent(operationName, k -> new ArrayList<>()).add(finalMetric);

            // Show only actual measurements
            System.out.printf("%s%n", finalMetric);
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
                System.out.printf("[SKIPPED] %s - %s: Connection operations not supported (contacts-only implementation).\n", name, operationName);

                // Add a placeholder result to maintain consistency in results
                PerformanceMetric placeholderMetric = new PerformanceMetric(0, 0, name, operationName);
                results.get(name).computeIfAbsent(operationName, k -> new ArrayList<>()).add(placeholderMetric);
                System.out.printf("Time: 0.000000 ms, Memory: 0 bytes\n");
                continue;
            }

            // Perform extensive warmup for the first data structure to eliminate JVM initialization overhead
            if (i == 0) {
                System.out.printf("  [%s] Performing JVM warmup for consistent measurements...%n", name);
                for (int warmup = 0; warmup < 5; warmup++) {
                    PerformanceMeasurement.suppressConsoleOutput(() -> operation.apply(cm));
                    System.gc();
                    try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            }

            List<PerformanceMetric> runMetrics = new ArrayList<>();
            for (int run = 0; run < runs; run++) {
                // Show progress
                System.out.printf("  [%s] Run %d/%d... ", name, run + 1, runs);

                // Extended stabilization for consistent cross-device measurements
                stabilizeMemoryEnvironment();
                long beforeMemory = getStableMemoryMeasurement();

                long startTime = System.nanoTime();
                operation.apply(cm);
                long endTime = System.nanoTime();

                // Measure memory after operation with stabilization
                stabilizeMemoryEnvironment();
                long afterMemory = getStableMemoryMeasurement();

                long rawDifference = afterMemory - beforeMemory;
                long timeTaken = endTime - startTime;

                System.out.printf("[DEBUG] %s - %s: beforeMemory: %d bytes, afterMemory: %d bytes, diff: %d bytes%n",
                    name, operationName, beforeMemory, afterMemory, rawDifference);

                // Handle negative memory differences (caused by GC during measurement)
                long memoryUsed;
                if (rawDifference < 0) {
                    // If negative, use 0 - don't artificially inflate
                    memoryUsed = 0;
                } else {
                    // Use actual measurement without artificial minimums
                    memoryUsed = rawDifference;
                }

                System.out.printf("[DEBUG] %s - %s: final memoryUsed: %d bytes%n",
                    name, operationName, memoryUsed);

                PerformanceMetric metric = new PerformanceMetric(timeTaken, memoryUsed, name, operationName);
                runMetrics.add(metric);
            }

            PerformanceMetric finalMetric = removeOutliersAndAverage(runMetrics, name, operationName);
            results.get(name).computeIfAbsent(operationName, k -> new ArrayList<>()).add(finalMetric);

            // Show only actual measurements
            System.out.printf("%s%n", finalMetric);
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
     * Prints a comprehensive summary of the performance comparison results.
     */
    public void printSummary() {
        System.out.println("\n===== PERFORMANCE METRICS SUMMARY =====");
        System.out.println("Batch Size: " + currentBatchSize + " contacts");
        System.out.println("Runs per test: " + runs + " (averaged with outlier removal)");

        // Add runtime environment analysis
        analyzeRuntimeEnvironment();

        System.out.println("\nPerformance Results:");
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

                // Show measurements
                System.out.printf("%s - %s: Time: %s, Memory: %d bytes%n",
                    structureName, operation,
                    formatTime(avgTotalTime / 1_000_000.0),
                    (long)avgTotalMemory);
            }
        }
        System.out.println("========================================");
    }

    /**
     * Provides runtime environment analysis for performance context.
     */
    private void analyzeRuntimeEnvironment() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        double memoryUsagePercent = (usedMemory * 100.0) / maxMemory;

        System.out.printf("Runtime Environment: %.1f%% memory used", memoryUsagePercent);
        if (memoryUsagePercent > 70) {
            System.out.print(" ⚠️ HIGH");
        } else if (memoryUsagePercent > 50) {
            System.out.print(" ⚠️ MODERATE");
        } else {
            System.out.print(" ✅ GOOD");
        }
        System.out.println();
    }

    /**
     * Formats time values to show appropriate precision based on magnitude.
     * Ensures no value shows as 0.00 ms when it's actually non-zero.
     */
    private String formatTime(double timeMs) {
        if (timeMs >= 1.0) {
            return String.format("%.2f ms", timeMs);
        } else if (timeMs >= 0.1) {
            return String.format("%.3f ms", timeMs);
        } else if (timeMs >= 0.01) {
            return String.format("%.4f ms", timeMs);
        } else if (timeMs >= 0.001) {
            return String.format("%.5f ms", timeMs);
        } else {
            return String.format("%.6f ms", timeMs);
        }
    }

    /**
     * Gets the current memory usage in bytes with improved accuracy.
     * Uses multiple measurements and stabilization to reduce artifacts.
     */
    private static long getUsedMemory() {
        // Force GC and stabilize memory before measurement
        System.gc();
        try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        System.gc();
        try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Robust measurement method that works consistently across different devices and JVM environments.
     * Uses multiple measurement strategies and validation to ensure reliable results.
     */
    private <T> PerformanceMetric measureOperationRobustly(String operationName, String structureName, Supplier<T> operation) {
        // Strategy 1: Direct measurement for operations that don't allocate memory
        if (isNonAllocatingOperation(operationName)) {
            return measureNonAllocatingOperation(operationName, structureName, operation);
        }

        // Strategy 2: Amplified measurement for operations that may allocate small amounts
        return measureWithAmplification(operationName, structureName, operation);
    }

    /**
     * Determines if an operation typically doesn't allocate new memory.
     */
    private boolean isNonAllocatingOperation(String operationName) {
        return operationName.equals("searchContact") ||
               operationName.equals("deleteContact");
    }

    /**
     * Measures operations that don't allocate memory using direct measurement with validation.
     */
    private <T> PerformanceMetric measureNonAllocatingOperation(String operationName, String structureName, Supplier<T> operation) {
        // Extended stabilization for cross-device consistency
        stabilizeMemoryEnvironment();

        // Take baseline measurement with multiple samples for accuracy
        long baselineMemory = getStableMemoryMeasurement();

        // Measure operation timing
        long startTime = System.nanoTime();
        operation.get();
        long endTime = System.nanoTime();

        // Measure memory after operation with stabilization
        stabilizeMemoryEnvironment();
        long afterMemory = getStableMemoryMeasurement();

        long rawDifference = afterMemory - baselineMemory;
        long timeTaken = endTime - startTime;

        System.out.printf("[DEBUG] %s - %s: beforeMemory: %d bytes, afterMemory: %d bytes, diff: %d bytes%n",
            structureName, operationName, baselineMemory, afterMemory, rawDifference);

        // Use actual measurement only - no artificial calculations
        long memoryUsed = Math.max(rawDifference, 0);

        System.out.printf("[DEBUG] %s - %s: final memoryUsed: %d bytes%n",
            structureName, operationName, memoryUsed);

        return new PerformanceMetric(timeTaken, memoryUsed, structureName, operationName);
    }

    /**
     * Measures operations with amplification for better signal detection.
     */
    private <T> PerformanceMetric measureWithAmplification(String operationName, String structureName, Supplier<T> operation) {
        // Extended stabilization for cross-device consistency
        stabilizeMemoryEnvironment();
        long beforeMemory = getStableMemoryMeasurement();

        // Use adaptive amplification based on data size and operation type
        int amplificationFactor = calculateAmplificationFactor(operationName, currentBatchSize);

        long startTime = System.nanoTime();
        List<T> results = new ArrayList<>();
        for (int amp = 0; amp < amplificationFactor; amp++) {
            T result = operation.get();
            results.add(result);
        }
        long endTime = System.nanoTime();

        // Clear results immediately to measure actual operation memory
        results.clear();

        // Measure memory after operations with stabilization
        stabilizeMemoryEnvironment();
        long afterMemory = getStableMemoryMeasurement();

        long rawDifference = afterMemory - beforeMemory;
        long timeTaken = (endTime - startTime) / amplificationFactor;

        System.out.printf("[DEBUG] %s - %s: beforeMemory: %d bytes, afterMemory: %d bytes, diff: %d bytes (amplified by %d)%n",
            structureName, operationName, beforeMemory, afterMemory, rawDifference, amplificationFactor);

        // Calculate per-operation memory usage
        long memoryUsed = Math.max(rawDifference / amplificationFactor, 0);

        System.out.printf("[DEBUG] %s - %s: final memoryUsed: %d bytes%n",
            structureName, operationName, memoryUsed);

        return new PerformanceMetric(timeTaken, memoryUsed, structureName, operationName);
    }

    /**
     * Stabilizes the memory environment for consistent measurements across devices.
     */
    private void stabilizeMemoryEnvironment() {
        // Multiple GC calls with delays for thorough cleanup
        for (int i = 0; i < 3; i++) {
            System.gc();
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        // Additional stabilization delay for different JVM behaviors
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    /**
     * Gets a stable memory measurement by taking multiple readings and using median.
     */
    private long getStableMemoryMeasurement() {
        List<Long> measurements = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            measurements.add(getUsedMemory());
            if (i < 4) {
                try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }

        // Sort and return median for stability
        measurements.sort(Long::compareTo);
        return measurements.get(measurements.size() / 2);
    }

    /**
     * Calculates appropriate amplification factor based on operation and data size.
     * Uses consistent methodology for fair comparison across operations.
     */
    private int calculateAmplificationFactor(String operationName, int dataSize) {
        // Use consistent amplification strategy for fair comparison
        switch (operationName) {
            case "searchContact":
            case "deleteContact":
                // Non-allocating operations need minimal amplification
                return 1; // Direct measurement is more accurate
            case "listAllContacts":
                // List operations allocate new collections - use minimal amplification
                return Math.max(1, Math.min(10, dataSize / 1000));
            case "updateContact":
            case "suggestContacts":
                // Operations that may allocate temporary objects
                return Math.max(10, Math.min(100, dataSize / 100));
            default:
                // Conservative amplification for unknown operations
                return Math.max(10, Math.min(50, dataSize / 200));
        }
    }
}
