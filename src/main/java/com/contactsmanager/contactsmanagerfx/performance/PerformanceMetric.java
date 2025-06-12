package com.contactsmanager.contactsmanagerfx.performance;

/**
 * Class to hold performance metrics for a single operation.
 */
public class PerformanceMetric {
    private final long timeNanos;
    private final long memoryBytes;
    private final String structureName;
    private final String operationName;

    /**
     * Creates a new performance metric.
     *
     * @param timeNanos The execution time in nanoseconds
     * @param memoryBytes The memory used in bytes
     * @param structureName The name of the data structure
     * @param operationName The name of the operation
     */
    public PerformanceMetric(long timeNanos, long memoryBytes, String structureName, String operationName) {
        this.timeNanos = timeNanos;
        this.memoryBytes = memoryBytes;
        this.structureName = structureName;
        this.operationName = operationName;
    }

    /**
     * Gets the execution time in nanoseconds.
     *
     * @return The execution time in nanoseconds
     */
    public long getTimeNanos() {
        return timeNanos;
    }

    /**
     * Gets the execution time in milliseconds.
     *
     * @return The execution time in milliseconds
     */
    public double getExecutionTimeMillis() {
        return timeNanos / 1_000_000.0;
    }

    /**
     * Gets the memory used in bytes.
     *
     * @return The memory used in bytes
     */
    public long getMemoryBytes() {
        return memoryBytes;
    }

    /**
     * Gets the memory used in kilobytes.
     *
     * @return The memory used in kilobytes
     */
    public double getMemoryUsedKB() {
        return memoryBytes / 1024.0;
    }

    /**
     * Gets the memory used in megabytes.
     *
     * @return The memory used in megabytes
     */
    public double getMemoryUsedMB() {
        return memoryBytes / (1024.0 * 1024.0);
    }

    /**
     * Gets the name of the data structure.
     *
     * @return The name of the data structure
     */
    public String getStructureName() {
        return structureName;
    }

    /**
     * Gets the name of the operation.
     *
     * @return The name of the operation
     */
    public String getOperationName() {
        return operationName;
    }

    @Override
    public String toString() {
        return String.format("Time: %.3f ms, Memory: %.2f KB",
            timeNanos / 1_000_000.0,
            memoryBytes / 1024.0);
    }

    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static long measureDataStructureMemory(Object dataStructure) {
        // Implementation of measureDataStructureMemory method
        // This method should return the memory used by the data structure
        // For example, you can use System.identityHashCode(dataStructure) to get the memory
        // or use a memory profiler to measure the actual memory usage
        return 0; // Placeholder return, actual implementation needed
    }
}
