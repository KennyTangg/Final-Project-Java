package com.contactsmanager.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Utility class for measuring performance of operations.
 */
public class PerformanceMeasurement {
    
    /**
     * Measures the performance of a given operation.
     *
     * @param operation The operation to measure
     * @param dataStructureName The name of the data structure
     * @param operationName The name of the operation
     * @return A PerformanceMetric object containing the results
     */
    public static PerformanceMetric measure(Runnable operation, String dataStructureName, String operationName) {
        // Force garbage collection before measurement to reduce noise
        System.gc();
        
        // Measure memory before operation
        long memoryBefore = getUsedMemory();
        
        // Measure time
        long startTime = System.nanoTime();
        operation.run();
        long endTime = System.nanoTime();
        
        // Measure memory after operation
        long memoryAfter = getUsedMemory();
        
        // Calculate metrics
        long executionTime = endTime - startTime;
        long memoryUsed = memoryAfter - memoryBefore;
        
        // If memory used is negative (due to GC), set to 0
        if (memoryUsed < 0) {
            memoryUsed = 0;
        }
        
        return new PerformanceMetric(executionTime, memoryUsed, dataStructureName, operationName);
    }
    
    /**
     * Measures the performance of a given operation that returns a result.
     *
     * @param <T> The type of the result
     * @param operation The operation to measure
     * @param dataStructureName The name of the data structure
     * @param operationName The name of the operation
     * @return A MeasuredResult object containing both the result and performance metrics
     */
    public static <T> MeasuredResult<T> measureWithResult(Supplier<T> operation, String dataStructureName, String operationName) {
        // Force garbage collection before measurement to reduce noise
        System.gc();
        
        // Measure memory before operation
        long memoryBefore = getUsedMemory();
        
        // Measure time and get result
        long startTime = System.nanoTime();
        T result = operation.get();
        long endTime = System.nanoTime();
        
        // Measure memory after operation
        long memoryAfter = getUsedMemory();
        
        // Calculate metrics
        long executionTime = endTime - startTime;
        long memoryUsed = memoryAfter - memoryBefore;
        
        // If memory used is negative (due to GC), set to 0
        if (memoryUsed < 0) {
            memoryUsed = 0;
        }
        
        PerformanceMetric metric = new PerformanceMetric(executionTime, memoryUsed, dataStructureName, operationName);
        return new MeasuredResult<>(result, metric);
    }
    
    /**
     * Measures the average performance of a given operation over multiple runs.
     *
     * @param operation The operation to measure
     * @param dataStructureName The name of the data structure
     * @param operationName The name of the operation
     * @param runs The number of runs to average over
     * @return A PerformanceMetric object containing the average results
     */
    public static PerformanceMetric measureAverage(Runnable operation, String dataStructureName, String operationName, int runs) {
        long totalTime = 0;
        long totalMemory = 0;
        
        List<PerformanceMetric> metrics = new ArrayList<>();
        
        for (int i = 0; i < runs; i++) {
            PerformanceMetric metric = measure(operation, dataStructureName, operationName);
            metrics.add(metric);
            totalTime += metric.getExecutionTimeNanos();
            totalMemory += metric.getMemoryUsedBytes();
        }
        
        return new PerformanceMetric(totalTime / runs, totalMemory / runs, dataStructureName, operationName);
    }
    
    /**
     * Gets the currently used memory in bytes.
     *
     * @return The used memory in bytes
     */
    private static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    /**
     * Class to hold both a result and its performance metrics.
     *
     * @param <T> The type of the result
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
