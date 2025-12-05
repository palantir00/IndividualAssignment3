import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
// import java.util.stream.IntStream; // Optional for Stream API approach

public class MatrixMultiplication {

    // Matrix size for testing. 
    // Recommended sizes: 1024 (fast test), 2048 (better speedup visibility)
    //private static final int SIZE = 1024; 
    private static final int SIZE = 2048; 

    
    // Number of available processing cores
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        // Data preparation
        double[][] matrixA = generateMatrix(SIZE, SIZE);
        double[][] matrixB = generateMatrix(SIZE, SIZE);

        System.out.println("Starting tests for matrix size: " + SIZE + "x" + SIZE);
        System.out.println("Available cores (threads): " + NUM_THREADS);
        System.out.println("--------------------------------------------------");

        // 1. Sequential Approach
        long startSeq = System.nanoTime();
        double[][] resultSeq = multiplySequential(matrixA, matrixB);
        long endSeq = System.nanoTime();
        double timeSeq = (endSeq - startSeq) / 1e9; // Convert nanoseconds to seconds
        System.out.println("Sequential Time: " + String.format("%.4f", timeSeq) + " s");

        // 2. Parallel Approach (using Executors)
        long startPar = System.nanoTime();
        double[][] resultPar = multiplyParallelExecutors(matrixA, matrixB);
        long endPar = System.nanoTime();
        double timePar = (endPar - startPar) / 1e9;
        System.out.println("Parallel Time (Executors): " + String.format("%.4f", timePar) + " s");

        // 3. Metrics Calculation
        double speedup = timeSeq / timePar;
        double efficiency = speedup / NUM_THREADS;

        System.out.println("--------------------------------------------------");
        System.out.println("Speedup: " + String.format("%.2f", speedup));
        System.out.println("Efficiency: " + String.format("%.2f", efficiency));
    }

    // --- Helper: Generate matrix with random doubles ---
    public static double[][] generateMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        Random random = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextDouble();
            }
        }
        return matrix;
    }

    // --- 1. Sequential Multiplication ---
    // Standard O(N^3) algorithm
    public static double[][] multiplySequential(double[][] A, double[][] B) {
        int rowsA = A.length;
        int colsA = A[0].length;
        int colsB = B[0].length;
        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return result;
    }

    // --- 2. Parallel Multiplication (Executors) ---
    // Uses a Fixed Thread Pool to distribute row calculations
    public static double[][] multiplyParallelExecutors(double[][] A, double[][] B) {
        int rowsA = A.length;
        int colsA = A[0].length;
        int colsB = B[0].length;
        double[][] result = new double[rowsA][colsB];

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < rowsA; i++) {
            final int row = i; // Effective final variable for lambda
            executor.submit(() -> {
                for (int j = 0; j < colsB; j++) {
                    double sum = 0;
                    for (int k = 0; k < colsA; k++) {
                        sum += A[row][k] * B[k][j];
                    }
                    result[row][j] = sum;
                }
            });
        }

        executor.shutdown();
        try {
            // Wait for all tasks to finish
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}