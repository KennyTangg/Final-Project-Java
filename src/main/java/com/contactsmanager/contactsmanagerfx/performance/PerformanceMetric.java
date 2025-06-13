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
        return String.format("Time: %s, Memory: %s",
            formatTime(timeNanos / 1_000_000.0),
            formatMemory(memoryBytes / 1024.0));
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
