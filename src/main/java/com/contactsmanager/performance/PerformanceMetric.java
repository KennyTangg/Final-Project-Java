package com.contactsmanager.performance;

/**
 * Represents a performance metric with execution time and memory usage.
 */
public class PerformanceMetric {
    private final long executionTimeNanos;
    private final long memoryUsedBytes;
    private final String dataStructureName;
    private final String operationName;

    /**
     * Creates a new performance metric.
     *
     * @param executionTimeNanos The execution time in nanoseconds
     * @param memoryUsedBytes The memory used in bytes
     * @param dataStructureName The name of the data structure
     * @param operationName The name of the operation
     */
    public PerformanceMetric(long executionTimeNanos, long memoryUsedBytes, String dataStructureName, String operationName) {
        this.executionTimeNanos = executionTimeNanos;
        this.memoryUsedBytes = memoryUsedBytes;
        this.dataStructureName = dataStructureName;
        this.operationName = operationName;
    }

    /**
     * Gets the execution time in nanoseconds.
     *
     * @return The execution time in nanoseconds
     */
    public long getExecutionTimeNanos() {
        return executionTimeNanos;
    }

    /**
     * Gets the execution time in milliseconds.
     *
     * @return The execution time in milliseconds
     */
    public double getExecutionTimeMillis() {
        return executionTimeNanos / 1_000_000.0;
    }

    /**
     * Gets the memory used in bytes.
     *
     * @return The memory used in bytes
     */
    public long getMemoryUsedBytes() {
        return memoryUsedBytes;
    }

    /**
     * Gets the memory used in kilobytes.
     *
     * @return The memory used in kilobytes
     */
    public double getMemoryUsedKB() {
        return memoryUsedBytes / 1024.0;
    }

    /**
     * Gets the memory used in megabytes.
     *
     * @return The memory used in megabytes
     */
    public double getMemoryUsedMB() {
        return memoryUsedBytes / (1024.0 * 1024.0);
    }

    /**
     * Gets the name of the data structure.
     *
     * @return The name of the data structure
     */
    public String getDataStructureName() {
        return dataStructureName;
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
        return String.format(
            "Performance [%s - %s]: Time: %.3f ms, Memory: %.2f KB",
            dataStructureName,
            operationName,
            getExecutionTimeMillis(),
            getMemoryUsedKB()
        );
    }
}
