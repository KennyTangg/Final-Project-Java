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
            suppressConsoleOutputWithResult(operation);
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
            result = suppressConsoleOutputWithResult(operation);
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
    public static <T> T suppressConsoleOutputWithResult(Operation<T> operation) {
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
     * Suppresses console output during operation execution and returns the result.
     * Overloaded version that accepts a Supplier.
     */
    public static <T> T suppressConsoleOutputWithResult(java.util.function.Supplier<T> operation) {
        PrintStream originalOut = System.out;
        try {
            // Redirect System.out to a dummy stream
            System.setOut(new PrintStream(new ByteArrayOutputStream()));
            return operation.get();
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
