package com.contactsmanager.contactsmanagerfx.performance;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for measuring performance metrics.
 */
public class PerformanceMeasurement {
    private static final int WARMUP_ITERATIONS = 1;  // Single warmup is enough
    private static final int MEASUREMENT_ITERATIONS = 1;  // Single measurement is enough
    private static final long STABILIZATION_DELAY = 100; // ms to let GC settle

    /**
     * Measures the execution time and memory usage of an operation.
     */
    public static PerformanceMetric measure(Runnable operation, String structureName, String operationName) {
        // Single warmup
        operation.run();
        
        // Memory measurement before
        System.gc(); // suggest GC before measuring
        try {
            Thread.sleep(STABILIZATION_DELAY); // let it settle
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long before = getUsedMemory();

        // Time measurement
        long startTime = System.nanoTime();
        operation.run();
        long endTime = System.nanoTime();

        // Memory measurement after
        System.gc();
        try {
            Thread.sleep(STABILIZATION_DELAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long after = getUsedMemory();

        long timeTaken = endTime - startTime;
        long memoryUsed = after - before;

        // Defensive: If negative (which can happen), set to 0
        memoryUsed = Math.max(0, memoryUsed);

        return new PerformanceMetric(timeTaken, memoryUsed, structureName, operationName);
    }

    /**
     * Measures the execution time and memory usage of an operation that returns a result.
     */
    public static <T> MeasuredResult<T> measureWithResult(Operation<T> operation, String structureName, String operationName) {
        // Single warmup
        operation.execute();
        
        // Memory measurement before
        System.gc(); // suggest GC before measuring
        try {
            Thread.sleep(STABILIZATION_DELAY); // let it settle
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long before = getUsedMemory();

        // Time measurement
        long startTime = System.nanoTime();
        T result = operation.execute();
        long endTime = System.nanoTime();

        // Memory measurement after
        System.gc();
        try {
            Thread.sleep(STABILIZATION_DELAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long after = getUsedMemory();

        long timeTaken = endTime - startTime;
        long memoryUsed = after - before;

        // Defensive: If negative (which can happen), set to 0
        memoryUsed = Math.max(0, memoryUsed);
        
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
