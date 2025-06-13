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

                // Stabilize memory and get baseline
                System.gc();
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                long baselineMemory = getUsedMemory();

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

                // Handle negative memory differences (caused by GC during measurement)
                long memoryUsed;
                if (rawDifference < 0) {
                    // If negative, estimate based on theoretical memory usage
                    long theoretical = PerformanceMeasurement.calculateTheoreticalMemory(name, currentBatchSize);
                    memoryUsed = Math.max(theoretical, 0);
                    System.out.printf("[DEBUG] %s - addContact: negative diff (%d bytes), using theoretical: %d bytes%n",
                        name, rawDifference, memoryUsed);
                } else {
                    memoryUsed = rawDifference;
                    System.out.printf("[DEBUG] %s - addContact: positive diff: %d bytes%n",
                        name, rawDifference);
                }

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

                // Print theoretical memory for comparison
                long theoretical = PerformanceMeasurement.calculateTheoreticalMemory(name, currentBatchSize);
                System.out.printf("  Theoretical Memory: %s%n", formatMemory(theoretical / 1024.0));
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

            // Calculate number of connections to create with reasonable limits
            int maxPossibleConnections = (allContacts.size() * (allContacts.size() - 1)) / 2; // n*(n-1)/2 for undirected graph

            // For large datasets, use a more reasonable approach
            int targetConnections;
            if (allContacts.size() <= 1000) {
                // For small datasets, use percentage-based approach
                targetConnections = (int) (maxPossibleConnections * connectionDensity);
            } else {
                // For large datasets, use connections-per-contact approach to avoid exponential growth
                int connectionsPerContact = Math.max(3, (int)(allContacts.size() * 0.01)); // 1% of contacts, minimum 3
                connectionsPerContact = Math.min(connectionsPerContact, 50); // Cap at 50 connections per contact
                targetConnections = (allContacts.size() * connectionsPerContact) / 2;
            }

            // Ensure each contact has at least 2-3 connections for meaningful suggestions
            int minConnectionsPerContact = Math.min(3, allContacts.size() - 1);
            int minTotalConnections = (allContacts.size() * minConnectionsPerContact) / 2;
            targetConnections = Math.max(targetConnections, minTotalConnections);

            // Cap the maximum to prevent excessive generation time
            int maxReasonableConnections = Math.min(maxPossibleConnections, allContacts.size() * 20);
            targetConnections = Math.min(targetConnections, maxReasonableConnections);

            double actualDensity = (double) targetConnections / maxPossibleConnections * 100;
            System.out.printf("Target connections: %d (%.3f%% density)\n", targetConnections, actualDensity);

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

            // Show theoretical comparison
            long theoreticalMemory = PerformanceMeasurement.calculateTheoreticalOperationMemory(structureName, "deleteContact", currentBatchSize);
            long theoreticalTime = PerformanceMeasurement.calculateTheoreticalTime(structureName, "deleteContact", currentBatchSize);
            System.out.printf("%s | Theoretical: Time: %s, Memory: %d bytes%n",
                finalMetric, formatTime(theoreticalTime / 1_000_000.0), theoreticalMemory);
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

                // For individual operations, use amplified measurement to get detectable signal
                System.gc();
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                long beforeMemory = getUsedMemory();

                String originalName = contact.getName() + run;
                int originalId = contact.getStudentId() + run;
                String newName = newNamePrefix + run;
                int newId = newStudentIdStart + run;

                // Amplify the operation significantly to get measurable memory usage like addContact
                int amplificationFactor = Math.max(1000, currentBatchSize); // Much higher amplification to match addContact behavior
                long startTime = System.nanoTime();
                for (int amp = 0; amp < amplificationFactor; amp++) {
                    final int ampFinal = amp; // Make effectively final for lambda
                    Contact originalContact = new Contact(originalName + "_amp" + ampFinal, originalId + ampFinal);
                    PerformanceMeasurement.suppressConsoleOutput(() -> ds.updateContact(originalContact, newName + "_amp" + ampFinal, newId + ampFinal));
                }
                long endTime = System.nanoTime();

                // Measure memory after amplified operations
                long afterMemory = getUsedMemory();
                long rawDifference = afterMemory - beforeMemory;
                System.out.printf("[DEBUG] %s - updateContact: beforeMemory: %d bytes, afterMemory: %d bytes, diff: %d bytes%n",
                    structureName, beforeMemory, afterMemory, rawDifference);

                // Calculate per-operation metrics
                long timeTaken = (endTime - startTime) / amplificationFactor;

                // Use actual measurement only - no artificial fallbacks
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

            // Show theoretical comparison
            long theoreticalMemory = PerformanceMeasurement.calculateTheoreticalOperationMemory(structureName, "updateContact", currentBatchSize);
            long theoreticalTime = PerformanceMeasurement.calculateTheoreticalTime(structureName, "updateContact", currentBatchSize);
            System.out.printf("%s | Theoretical: Time: %s, Memory: %d bytes%n",
                finalMetric, formatTime(theoreticalTime / 1_000_000.0), theoreticalMemory);
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
                    operationName,
                    currentBatchSize
                );
                runMetrics.add(metric);
            }

            PerformanceMetric finalMetric = removeOutliersAndAverage(runMetrics, name, operationName);
            results.get(name).computeIfAbsent(operationName, k -> new ArrayList<>()).add(finalMetric);

            // Show theoretical comparison
            long theoreticalMemory = PerformanceMeasurement.calculateTheoreticalOperationMemory(name, operationName, currentBatchSize);
            long theoreticalTime = PerformanceMeasurement.calculateTheoreticalTime(name, operationName, currentBatchSize);
            System.out.printf("%s | Theoretical: Time: %s, Memory: %d bytes%n",
                finalMetric, formatTime(theoreticalTime / 1_000_000.0), theoreticalMemory);
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
                // Show progress
                System.out.printf("  [%s] Run %d/%d... ", name, run + 1, runs);

                // For search operations, don't collect results to avoid artificial memory allocation
                System.gc();
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                long beforeMemory = getUsedMemory();

                long startTime = System.nanoTime();
                // For search operations, just perform the operation without collecting results
                if (operationName.equals("searchContact")) {
                    // Single operation without result collection for accurate memory measurement
                    T result = operation.apply(ds);
                    // Don't store the result to avoid artificial memory allocation
                } else {
                    // For other operations that legitimately need result collection
                    int amplificationFactor = Math.max(500, currentBatchSize / 2);
                    List<T> results = new ArrayList<>();
                    for (int amp = 0; amp < amplificationFactor; amp++) {
                        T result = operation.apply(ds);
                        results.add(result);
                    }
                    results.clear(); // Clear immediately after measurement
                }
                long endTime = System.nanoTime();

                // Measure memory after operation
                long afterMemory = getUsedMemory();
                long rawDifference = afterMemory - beforeMemory;
                System.out.printf("[DEBUG] %s - %s: beforeMemory: %d bytes, afterMemory: %d bytes, diff: %d bytes%n",
                    name, operationName, beforeMemory, afterMemory, rawDifference);

                // Calculate per-operation metrics
                long timeTaken = endTime - startTime;
                if (!operationName.equals("searchContact")) {
                    // For operations with amplification, divide by amplification factor
                    int amplificationFactor = Math.max(500, currentBatchSize / 2);
                    timeTaken = timeTaken / amplificationFactor;
                    rawDifference = rawDifference / amplificationFactor;
                }

                // Use actual measurement without artificial inflation
                long memoryUsed = Math.max(rawDifference, 0); // Only prevent negative values
                System.out.printf("[DEBUG] %s - %s: final memoryUsed: %d bytes%n",
                    name, operationName, memoryUsed);

                PerformanceMetric metric = new PerformanceMetric(timeTaken, memoryUsed, name, operationName);
                runMetrics.add(metric);
            }

            PerformanceMetric finalMetric = removeOutliersAndAverage(runMetrics, name, operationName);
            results.get(name).computeIfAbsent(operationName, k -> new ArrayList<>()).add(finalMetric);

            // Show theoretical comparison
            long theoreticalMemory = PerformanceMeasurement.calculateTheoreticalOperationMemory(name, operationName, currentBatchSize);
            long theoreticalTime = PerformanceMeasurement.calculateTheoreticalTime(name, operationName, currentBatchSize);
            System.out.printf("%s | Theoretical: Time: %s, Memory: %d bytes%n",
                finalMetric, formatTime(theoreticalTime / 1_000_000.0), theoreticalMemory);
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

            // Show theoretical comparison
            long theoreticalMemory = PerformanceMeasurement.calculateTheoreticalOperationMemory(name, operationName, currentBatchSize);
            long theoreticalTime = PerformanceMeasurement.calculateTheoreticalTime(name, operationName, currentBatchSize);
            System.out.printf("%s | Theoretical: Time: %s, Memory: %d bytes%n",
                finalMetric, formatTime(theoreticalTime / 1_000_000.0), theoreticalMemory);
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
                // Show progress
                System.out.printf("  [%s] Run %d/%d... ", name, run + 1, runs);

                // Use the same direct memory measurement approach as addContact
                System.gc();
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                long beforeMemory = getUsedMemory();

                long startTime = System.nanoTime();
                T result = operation.apply(cm);
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

            // Show theoretical comparison
            long theoreticalMemory = PerformanceMeasurement.calculateTheoreticalOperationMemory(name, operationName, currentBatchSize);
            long theoreticalTime = PerformanceMeasurement.calculateTheoreticalTime(name, operationName, currentBatchSize);
            System.out.printf("%s | Theoretical: Time: %s, Memory: %d bytes%n",
                finalMetric, formatTime(theoreticalTime / 1_000_000.0), theoreticalMemory);
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
     * Prints a comprehensive summary of the performance comparison results with theoretical comparisons.
     */
    public void printSummary() {
        System.out.println("\n===== PERFORMANCE METRICS SUMMARY =====");
        System.out.println("Batch Size: " + currentBatchSize + " contacts");
        System.out.println("Runs per test: " + runs + " (averaged with outlier removal)");
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

                // Get theoretical memory and time for comparison
                long theoreticalMemory = PerformanceMeasurement.calculateTheoreticalOperationMemory(structureName, operation, currentBatchSize);
                long theoreticalTime = PerformanceMeasurement.calculateTheoreticalTime(structureName, operation, currentBatchSize);

                System.out.printf("%s - %s: Total Time: %s, Total Memory: %d bytes (Theoretical: Time: %s, Memory: %d bytes)%n",
                    structureName, operation,
                    formatTime(avgTotalTime / 1_000_000.0),
                    (long)avgTotalMemory,
                    formatTime(theoreticalTime / 1_000_000.0),
                    theoreticalMemory);
            }
        }
        System.out.println("========================================");
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
     * Formats memory values to show appropriate precision based on magnitude.
     * Ensures no value shows as 0.00 KB when it's actually non-zero.
     */
    private String formatMemory(double memoryKB) {
        if (memoryKB >= 1.0) {
            return String.format("%.2f KB", memoryKB);
        } else if (memoryKB >= 0.1) {
            return String.format("%.3f KB", memoryKB);
        } else if (memoryKB >= 0.01) {
            return String.format("%.4f KB", memoryKB);
        } else if (memoryKB >= 0.001) {
            return String.format("%.5f KB", memoryKB);
        } else {
            return String.format("%.6f KB", memoryKB);
        }
    }

    /**
     * Gets the current memory usage in bytes.
     */
    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Calculates realistic memory usage based on actual timing data and operation characteristics.
     * This provides meaningful estimates when direct memory measurement shows no allocation.
     */
    private long calculateRealisticMemoryFromTime(String structureName, String operationName, int dataSize, long timeTakenNanos) {
        // Convert time to milliseconds for easier calculation
        double timeMs = timeTakenNanos / 1_000_000.0;

        // Base memory that scales with data size and operation complexity
        long baseMemory = 1024; // 1KB minimum

        // Calculate memory based on time complexity and data structure characteristics
        switch (structureName) {
            case "Adjacency Matrix":
                switch (operationName) {
                    case "searchContact":
                        // Matrix search can be O(n) in worst case, time reflects complexity
                        return Math.max((long)(timeMs * 50 + Math.sqrt(dataSize) * 2), baseMemory);
                    case "updateContact":
                        // Matrix update requires finding and modifying entries
                        return Math.max((long)(timeMs * 100 + Math.log(dataSize) * 8), baseMemory);
                    case "deleteContact":
                        // Matrix delete requires clearing row/column
                        return Math.max((long)(timeMs * 80 + Math.sqrt(dataSize) * 4), baseMemory);
                    default:
                        return baseMemory;
                }

            case "Adjacency List":
                switch (operationName) {
                    case "searchContact":
                        // List search through HashMap, time reflects lookup complexity
                        return Math.max((long)(timeMs * 30 + Math.log(dataSize) * 4), baseMemory);
                    case "updateContact":
                        // HashMap modification + list updates
                        return Math.max((long)(timeMs * 60 + Math.log(dataSize) * 6), baseMemory);
                    case "deleteContact":
                        // HashMap removal + list cleanup
                        return Math.max((long)(timeMs * 50 + Math.log(dataSize) * 5), baseMemory);
                    default:
                        return baseMemory;
                }

            case "HashMap":
                switch (operationName) {
                    case "searchContact":
                        // O(1) HashMap lookup, minimal time = minimal memory
                        return Math.max((long)(timeMs * 20 + Math.log(dataSize) * 2), baseMemory);
                    case "updateContact":
                        // O(1) HashMap modification
                        return Math.max((long)(timeMs * 40 + Math.log(dataSize) * 3), baseMemory);
                    case "deleteContact":
                        // O(1) HashMap removal
                        return Math.max((long)(timeMs * 35 + Math.log(dataSize) * 2.5), baseMemory);
                    default:
                        return baseMemory;
                }

            default:
                return baseMemory;
        }
    }

    /**
     * Calculates realistic memory usage for operations based on data structure characteristics,
     * operation complexity, and actual timing data to provide meaningful memory estimates.
     */
    private long calculateRealisticMemoryUsage(String structureName, String operationName, int dataSize, long timeTakenNanos) {
        long baseMemory = 1024; // 1KB minimum

        // Convert time to milliseconds for easier calculation
        double timeMs = timeTakenNanos / 1_000_000.0;

        // Data structure specific memory characteristics with realistic scaling
        switch (structureName) {
            case "Adjacency Matrix":
                switch (operationName) {
                    case "listAllContacts":
                        // Matrix listing requires scanning entire matrix + result collection
                        return Math.max((long)(dataSize * 12 + timeMs * 100), baseMemory);
                    case "searchContact":
                        // Matrix search has O(n) complexity in worst case - more memory intensive
                        return Math.max((long)(Math.sqrt(dataSize) * 16 + timeMs * 200 + dataSize * 0.5), (long)(baseMemory * 2));
                    case "updateContact":
                        // Matrix update requires finding and modifying entries - complex operation
                        return Math.max((long)(Math.log(dataSize) * 32 + timeMs * 300 + dataSize * 0.3), (long)(baseMemory * 2));
                    case "deleteContact":
                        // Matrix delete requires clearing row/column - memory intensive
                        return Math.max((long)(Math.sqrt(dataSize) * 12 + timeMs * 250 + dataSize * 0.4), (long)(baseMemory * 2));
                    default:
                        return baseMemory;
                }

            case "Adjacency List":
                switch (operationName) {
                    case "listAllContacts":
                        // List traversal + result collection
                        return Math.max((long)(dataSize * 8 + timeMs * 80), baseMemory);
                    case "searchContact":
                        // HashMap lookup with potential list traversal - moderate complexity
                        return Math.max((long)(Math.log(dataSize) * 24 + timeMs * 150 + dataSize * 0.2), (long)(baseMemory * 1.5));
                    case "updateContact":
                        // HashMap modification + list updates - moderate complexity
                        return Math.max((long)(Math.log(dataSize) * 20 + timeMs * 200 + dataSize * 0.25), (long)(baseMemory * 1.5));
                    case "deleteContact":
                        // HashMap removal + list cleanup - moderate complexity
                        return Math.max((long)(Math.log(dataSize) * 16 + timeMs * 180 + dataSize * 0.2), (long)(baseMemory * 1.5));
                    default:
                        return baseMemory;
                }

            case "HashMap":
                switch (operationName) {
                    case "listAllContacts":
                        // Efficient HashMap iteration + result collection
                        return Math.max((long)(dataSize * 6 + timeMs * 60), baseMemory);
                    case "searchContact":
                        // O(1) HashMap lookup - minimal but still measurable
                        return Math.max((long)(Math.log(dataSize) * 8 + timeMs * 100 + dataSize * 0.1), baseMemory);
                    case "updateContact":
                        // O(1) HashMap modification - minimal but still measurable
                        return Math.max((long)(Math.log(dataSize) * 12 + timeMs * 120 + dataSize * 0.15), baseMemory);
                    case "deleteContact":
                        // O(1) HashMap removal - minimal but still measurable
                        return Math.max((long)(Math.log(dataSize) * 10 + timeMs * 110 + dataSize * 0.12), baseMemory);
                    default:
                        return baseMemory;
                }

            default:
                return baseMemory;
        }
    }
}
