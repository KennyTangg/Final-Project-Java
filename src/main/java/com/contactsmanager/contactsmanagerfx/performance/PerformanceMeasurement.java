package com.contactsmanager.contactsmanagerfx.performance;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Utility class for measuring performance metrics.
 */
public class PerformanceMeasurement {

    /**
     * Measures the execution time and memory usage of an operation.
     * For addContact operations, measures the total data structure footprint.
     */
    public static PerformanceMetric measure(Runnable operation, String structureName, String operationName) {
        return measure(operation, structureName, operationName, 1000); // Default batch size
    }

    /**
     * Measures the execution time and memory usage of an operation with known batch size.
     * For addContact operations, measures the total data structure footprint.
     */
    public static PerformanceMetric measure(Runnable operation, String structureName, String operationName, int batchSize) {
        // Warm up JVM and stabilize memory BEFORE measurement
        System.gc();
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // Multiple warmup runs for JIT optimization
        for (int i = 0; i < 3; i++) {
            suppressConsoleOutput(() -> operation.run());
        }

        // Force garbage collection and wait for stabilization
        System.gc();
        try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        long timeTaken;
        long memoryUsed;

        if (operationName.equals("addContact")) {
            // For addContact, measure total memory footprint after all contacts are added
            long beforeMemory = getUsedMemory();
            long startTime = System.nanoTime();
            suppressConsoleOutput(() -> operation.run());
            long endTime = System.nanoTime();

            // Force GC to get accurate memory reading
            System.gc();
            try { Thread.sleep(20); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            long afterMemory = getUsedMemory();

            timeTaken = endTime - startTime;
            // Use actual memory measurement without artificial minimums
            long rawMemoryDifference = afterMemory - beforeMemory;
            memoryUsed = Math.max(rawMemoryDifference, 0); // Only prevent negative values
        } else {
            // For other operations, use the same approach as addContact for realistic measurements
            // Single operations often don't trigger measurable memory changes, so we use amplified measurement

            // Stabilize memory before measurement
            System.gc();
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            long baselineMemory = getUsedMemory();

            // Run operation multiple times to amplify memory signal (like addContact does)
            int iterations = 10;
            long startTime = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                suppressConsoleOutput(() -> operation.run());
            }
            long endTime = System.nanoTime();

            // Force GC and measure memory after amplified operations
            System.gc();
            try { Thread.sleep(20); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            long afterMemory = getUsedMemory();

            // Calculate per-operation metrics
            long totalMemoryDifference = afterMemory - baselineMemory;
            long perOperationMemory = totalMemoryDifference / iterations;

            timeTaken = (endTime - startTime) / iterations; // Average time per operation

            // Use actual measurement without artificial inflation
            memoryUsed = Math.max(perOperationMemory, 0); // Only prevent negative values
        }

        return new PerformanceMetric(timeTaken, memoryUsed, structureName, operationName);
    }

    /**
     * Measures the execution time and memory usage of an operation that returns a result.
     * Uses amplified measurement approach for better accuracy.
     */
    public static <T> MeasuredResult<T> measureWithResult(Operation<T> operation, String structureName, String operationName) {
        return measureWithResult(operation, structureName, operationName, 1000); // Default batch size
    }

    /**
     * Measures the execution time and memory usage of an operation that returns a result with known batch size.
     * Uses amplified measurement approach for better accuracy.
     */
    public static <T> MeasuredResult<T> measureWithResult(Operation<T> operation, String structureName, String operationName, int batchSize) {
        // Warm up JVM and stabilize memory BEFORE measurement
        System.gc();
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // Multiple warmup runs for JIT optimization
        for (int i = 0; i < 3; i++) {
            suppressConsoleOutputWithResult(() -> operation.execute());
        }

        // Stabilize memory before measurement
        System.gc();
        try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        long baselineMemory = getUsedMemory();

        // Run the operation multiple times to amplify memory usage signal
        int iterations = 10; // Run 10 times to amplify memory signal
        T result = null;
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            result = suppressConsoleOutputWithResult(() -> operation.execute());
        }
        long endTime = System.nanoTime();

        // Measure memory after multiple operations
        long afterMemory = getUsedMemory();

        // Calculate per-operation metrics
        long timeTaken = (endTime - startTime) / iterations; // Average time per operation
        long totalMemoryDifference = afterMemory - baselineMemory;
        long perOperationMemory = totalMemoryDifference / iterations; // Average memory per operation

        // Use actual measurement without artificial inflation
        long memoryUsed = Math.max(perOperationMemory, 0); // Only prevent negative values

        PerformanceMetric metric = new PerformanceMetric(timeTaken, memoryUsed, structureName, operationName);
        return new MeasuredResult<>(result, metric);
    }

    /**
     * Gets the current memory usage in bytes.
     */
    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Returns minimum realistic memory usage for any operation.
     * Even the smallest operations use some memory for temporary variables, object references, etc.
     */
    private static long getMinimumRealisticMemory(String operationName) {
        // Very conservative minimums - just enough to show that operations aren't "free"
        switch (operationName) {
            case "searchContact":
                return 64;   // String comparison, temporary variables
            case "listAllContacts":
                return 128;  // ArrayList creation overhead
            case "updateContact":
                return 96;   // Object creation overhead
            case "deleteContact":
                return 64;   // Cleanup operations
            case "addContact":
                return 1024; // Object creation and data structure allocation
            case "suggestContacts":
                return 128;  // Search and collection operations
            case "addConnection":
                return 64;   // Connection creation
            case "removeConnection":
                return 64;   // Connection removal
            default:
                return 64;   // Default minimum
        }
    }

    /**
     * Calculates realistic memory usage for operations based on actual measurement.
     * Uses actual memory difference and avoids artificial baselines.
     */
    private static long calculateRealisticMemoryForOperation(long actualDifference, String operationName, String structureName) {
        // Always use actual measurement when available
        long actualMemory = Math.abs(actualDifference);

        // Only use minimums when actual measurement is truly zero or negative
        if (actualMemory < 64) {
            // Very conservative minimums - just enough to show operations aren't "free"
            switch (operationName) {
                case "listAllContacts":
                    return 512; // 512 bytes for ArrayList creation
                case "searchContact":
                    return 256; // 256 bytes for search operations
                case "updateContact":
                    return 384; // 384 bytes for object updates
                case "deleteContact":
                    return 256; // 256 bytes for deletion
                case "suggestContacts":
                    return 512; // 512 bytes for filtering
                case "addConnection":
                case "removeConnection":
                    return 256; // 256 bytes for connection operations
                default:
                    return 128; // 128 bytes default minimum
            }
        }

        return actualMemory;
    }

    /**
     * Calculates realistic memory usage based on actual measurement and data size.
     * This method ensures all operations show realistic memory usage that scales consistently.
     * Memory calculations are based on realistic operation complexity and data structure characteristics.
     */
    private static long calculateDataSizeAwareMemory(long actualDifference, String operationName, String structureName, int batchSize) {
        long actualMemory = Math.abs(actualDifference);



        // Use realistic memory calculations that scale consistently with data size
        // Base memory + scaling factor based on operation complexity

        switch (operationName) {
            case "listAllContacts":
                // List operations create ArrayList and populate it - memory scales linearly with data size
                // Base overhead + reasonable bytes per contact reference
                long listMemory = 4096 + (long) batchSize * 8; // 4KB base + 8 bytes per contact
                return Math.max(actualMemory, listMemory);

            case "searchContact":
                // Search operations - memory should scale modestly with data size
                // Different data structures have different search memory patterns
                long searchMemory;
                switch (structureName) {
                    case "Adjacency Matrix":
                        // Array iteration - moderate memory scaling
                        searchMemory = 1024 + (batchSize / 50); // 1KB base + modest scaling
                        break;
                    case "Adjacency List":
                        // HashMap iteration - slightly more memory
                        searchMemory = 512 + (batchSize / 100); // 512B base + minimal scaling
                        break;
                    case "HashMap":
                        // Hash lookup - relatively constant with minimal scaling
                        searchMemory = 1024 + (batchSize / 200); // 1KB base + very minimal scaling
                        break;
                    default:
                        searchMemory = 512 + (batchSize / 100);
                }
                return Math.max(actualMemory, searchMemory);

            case "updateContact":
                // Update operations create new Contact objects and modify data structures
                // Memory should scale modestly with data size
                long updateMemory;
                switch (structureName) {
                    case "Adjacency Matrix":
                        // Direct array assignment - minimal memory
                        updateMemory = 512 + (batchSize / 200); // 512B base + minimal scaling
                        break;
                    case "Adjacency List":
                        // HashMap operations - more memory for object manipulation
                        updateMemory = 1024 + (batchSize / 50); // 1KB base + modest scaling
                        break;
                    case "HashMap":
                        // Hash update - moderate memory
                        updateMemory = 768 + (batchSize / 100); // 768B base + minimal scaling
                        break;
                    default:
                        updateMemory = 512 + (batchSize / 100);
                }
                return Math.max(actualMemory, updateMemory);

            case "deleteContact":
                // Delete operations require search + cleanup - memory scales with search complexity
                long deleteMemory = 768 + (batchSize / 100); // 768B base + minimal scaling
                return Math.max(actualMemory, deleteMemory);

            case "suggestContacts":
                // Suggest operations do filtering and create result collections
                // Memory scales with potential results but not too aggressively
                long suggestMemory = 2048 + (batchSize / 20); // 2KB base + moderate scaling
                return Math.max(actualMemory, suggestMemory);

            case "addConnection":
            case "removeConnection":
                // Connection operations - memory should scale modestly with data structure size
                long connectionMemory = 1024 + (batchSize / 100); // 1KB base + minimal scaling
                return Math.max(actualMemory, connectionMemory);

            default:
                return Math.max(actualMemory, 512 + (batchSize / 100));
        }
    }

    /**
     * Calculates theoretical memory footprint for data structures.
     * This helps validate that our measurements are in the right ballpark.
     */
    public static long calculateTheoreticalMemory(String structureName, int contactCount) {
        switch (structureName) {
            case "Adjacency Matrix":
                // Matrix: contactCount × contactCount × 1 byte (byte[][])
                // Contacts array: contactCount × 8 bytes (reference) + Contact objects
                // Contact objects: ~100 bytes each (rough estimate)
                long matrixMemory = (long) contactCount * contactCount; // byte matrix
                long contactsArrayMemory = (long) contactCount * 8; // reference array
                long contactObjectsMemory = (long) contactCount * 100; // Contact objects
                return matrixMemory + contactsArrayMemory + contactObjectsMemory;

            case "Adjacency List":
                // HashMap: ~32 bytes overhead + entries
                // Each entry: Contact object + LinkedList + references
                // Rough estimate: 200 bytes per contact
                return (long) contactCount * 200;

            case "HashMap":
                // HashMap: ~32 bytes overhead + entries
                // Each entry: Contact object + hash entry overhead
                // Rough estimate: 150 bytes per contact
                return (long) contactCount * 150;

            default:
                return contactCount * 100; // Default estimate
        }
    }

    /**
     * Calculates theoretical memory usage for specific operations.
     * Different operations have different memory footprints.
     */
    public static long calculateTheoreticalOperationMemory(String structureName, String operationName, int contactCount) {
        switch (operationName.toLowerCase()) {
            case "addcontact":
                return calculateTheoreticalMemory(structureName, contactCount);

            case "searchcontact":
                // Search operations typically don't allocate memory, just read existing data
                return 0;

            case "updatecontact":
                // Update may create temporary objects during the operation
                switch (structureName) {
                    case "Adjacency Matrix":
                        return 200; // Minimal temporary object creation
                    case "Adjacency List":
                        return 150; // Some temporary objects for list manipulation
                    case "HashMap":
                        return 100; // Minimal overhead for hash operations
                    default:
                        return 100;
                }

            case "deletecontact":
                // Delete operations may have some cleanup overhead
                switch (structureName) {
                    case "Adjacency Matrix":
                        return 100; // Minimal cleanup
                    case "Adjacency List":
                        return 200; // List cleanup operations
                    case "HashMap":
                        return 50; // Minimal hash cleanup
                    default:
                        return 100;
                }

            case "listallcontacts":
                // Creating a new list with all contacts
                return contactCount * 8; // Array of references

            case "suggestcontacts":
                // Suggestion algorithms create temporary collections
                switch (structureName) {
                    case "Adjacency Matrix":
                        return contactCount * 4; // Efficient matrix traversal
                    case "Adjacency List":
                        return contactCount * 12; // More complex list traversal
                    case "HashMap":
                        return contactCount * 8; // Moderate hash-based traversal
                    default:
                        return contactCount * 8;
                }

            case "addconnection":
            case "removeconnection":
                // Connection operations have minimal memory overhead
                return 64; // Small temporary objects

            default:
                return 100; // Default estimate
        }
    }

    /**
     * Calculates theoretical time complexity for operations in nanoseconds.
     * Based on algorithmic complexity and typical performance characteristics.
     */
    public static long calculateTheoreticalTime(String structureName, String operationName, int contactCount) {
        // Base time constants (in nanoseconds) - calibrated from real measurements
        final long CONSTANT_TIME = 1000; // ~1 microsecond for O(1) operations
        final long LINEAR_BASE = 100; // ~100 nanoseconds per element for O(n) operations
        final long LOG_BASE = 50; // ~50 nanoseconds per log element for O(log n) operations

        switch (operationName.toLowerCase()) {
            case "addcontact":
                switch (structureName) {
                    case "Adjacency Matrix":
                        return CONSTANT_TIME * 2; // O(1) - direct array access
                    case "Adjacency List":
                        return CONSTANT_TIME * 3; // O(1) - hash map insertion
                    case "HashMap":
                        return CONSTANT_TIME; // O(1) - optimal hash insertion
                    default:
                        return CONSTANT_TIME * 2;
                }

            case "searchcontact":
                switch (structureName) {
                    case "Adjacency Matrix":
                        return CONSTANT_TIME / 2; // O(1) - direct array access, fastest
                    case "Adjacency List":
                        return LINEAR_BASE * contactCount / 1000; // O(n) - linear search in worst case
                    case "HashMap":
                        return CONSTANT_TIME / 3; // O(1) - hash lookup, very fast
                    default:
                        return CONSTANT_TIME;
                }

            case "updatecontact":
                switch (structureName) {
                    case "Adjacency Matrix":
                        return CONSTANT_TIME * 3; // O(1) - direct access + update
                    case "Adjacency List":
                        return CONSTANT_TIME * 4; // O(1) - hash lookup + list update
                    case "HashMap":
                        return CONSTANT_TIME * 2; // O(1) - hash update
                    default:
                        return CONSTANT_TIME * 3;
                }

            case "deletecontact":
                switch (structureName) {
                    case "Adjacency Matrix":
                        return LINEAR_BASE * contactCount / 100; // O(n) - need to clear row/column
                    case "Adjacency List":
                        return CONSTANT_TIME * 5; // O(1) - hash removal + list cleanup
                    case "HashMap":
                        return CONSTANT_TIME * 2; // O(1) - hash removal
                    default:
                        return CONSTANT_TIME * 3;
                }

            case "listallcontacts":
                // O(n) for all structures - need to iterate through all contacts
                return LINEAR_BASE * contactCount;

            case "suggestcontacts":
                switch (structureName) {
                    case "Adjacency Matrix":
                        return LINEAR_BASE * contactCount / 10; // O(n) - efficient matrix traversal
                    case "Adjacency List":
                        return LINEAR_BASE * contactCount / 2; // O(n) - list traversal with more overhead
                    case "HashMap":
                        return LINEAR_BASE * contactCount / 5; // O(n) - hash-based traversal
                    default:
                        return LINEAR_BASE * contactCount;
                }

            case "addconnection":
            case "removeconnection":
                switch (structureName) {
                    case "Adjacency Matrix":
                        return CONSTANT_TIME; // O(1) - direct matrix access
                    case "Adjacency List":
                        return CONSTANT_TIME * 2; // O(1) - hash + list operations
                    case "HashMap":
                        return CONSTANT_TIME * 2; // O(1) - hash operations
                    default:
                        return CONSTANT_TIME;
                }

            default:
                return CONSTANT_TIME;
        }
    }



    /**
     * Suppresses console output during operation execution to prevent I/O overhead.
     */
    public static void suppressConsoleOutput(Runnable operation) {
        PrintStream originalOut = System.out;
        try {
            // Redirect System.out to a dummy stream
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            operation.run();
        } finally {
            // Restore original System.out
            System.setOut(originalOut);
        }
    }

    /**
     * Suppresses console output during operation execution and returns the result.
     */
    private static <T> T suppressConsoleOutputWithResult(Operation<T> operation) {
        PrintStream originalOut = System.out;
        try {
            // Redirect System.out to a dummy stream
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            return operation.execute();
        } finally {
            // Restore original System.out
            System.setOut(originalOut);
        }
    }

    /**
     * Interface for operations that return a result.
     */
    @FunctionalInterface
    public interface Operation<T> {
        T execute();
    }

    /**
     * Class to hold both the operation result and performance metrics.
     */
    public static class MeasuredResult<T> {
        private final T result;
        private final PerformanceMetric metric;

        public MeasuredResult(T result, PerformanceMetric metric) {
            this.result = result;
            this.metric = metric;
        }

        public T getResult() {
            return result;
        }

        public PerformanceMetric getMetric() {
            return metric;
        }
    }
}
