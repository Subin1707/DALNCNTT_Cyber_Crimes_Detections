package com.example.servingwebcontent.model;

import java.util.Arrays;

/**
 * DISTANCE METRIC
 *
 * Hệ thống thuật toán khoảng cách dùng cho:
 * - KNN
 * - Region Classification
 * - Fraud Detection
 * - Node Similarity
 * - Domain Movement
 *
 * Hỗ trợ:
 * - Euclidean
 * - Manhattan
 * - Minkowski
 * - Hamming
 * - Cosine
 */
public abstract class DistanceMetric {

    // =========================================================
    // ABSTRACT METHODS
    // =========================================================

    public abstract double calculate(double[] vector1, double[] vector2);

    public abstract String getName();

    public abstract String getDescription();

    // =========================================================
    // COMMON VALIDATION
    // =========================================================

    /**
     * Kiểm tra vector hợp lệ
     */
    protected boolean isValid(double[] vector1, double[] vector2) {

        if (vector1 == null || vector2 == null) {
            return false;
        }

        if (vector1.length == 0 || vector2.length == 0) {
            return false;
        }

        return vector1.length == vector2.length;
    }

    /**
     * Làm sạch vector NaN/Infinity
     */
    protected double sanitize(double value) {

        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }

        return value;
    }

    /**
     * Chuẩn hóa vector
     */
    protected double[] normalize(double[] vector) {

        if (vector == null) {
            return new double[0];
        }

        double norm = 0.0;

        for (double v : vector) {
            norm += v * v;
        }

        norm = Math.sqrt(norm);

        if (norm == 0.0) {
            return Arrays.copyOf(vector, vector.length);
        }

        double[] normalized = new double[vector.length];

        for (int i = 0; i < vector.length; i++) {
            normalized[i] = vector[i] / norm;
        }

        return normalized;
    }

    // =========================================================
    // EUCLIDEAN DISTANCE
    // =========================================================

    /**
     * Công thức:
     *
     * d(x,y) = sqrt(Σ(xi - yi)^2)
     */
    public static class EuclideanDistance extends DistanceMetric {

        @Override
        public double calculate(double[] vector1, double[] vector2) {

            if (!isValid(vector1, vector2)) {
                return Double.MAX_VALUE;
            }

            double sum = 0.0;

            for (int i = 0; i < vector1.length; i++) {

                double v1 = sanitize(vector1[i]);
                double v2 = sanitize(vector2[i]);

                double diff = v1 - v2;

                sum += diff * diff;
            }

            return Math.sqrt(sum);
        }

        @Override
        public String getName() {
            return "EUCLIDEAN";
        }

        @Override
        public String getDescription() {
            return """
                    Euclidean Distance (L2 Norm)

                    Formula:
                    sqrt(Σ(xi - yi)^2)

                    Use Cases:
                    - KNN
                    - Fraud similarity
                    - Numerical vectors
                    - Graph embeddings
                    """;
        }
    }

    // =========================================================
    // MANHATTAN DISTANCE
    // =========================================================

    /**
     * Công thức:
     *
     * d(x,y) = Σ|xi - yi|
     */
    public static class ManhattanDistance extends DistanceMetric {

        @Override
        public double calculate(double[] vector1, double[] vector2) {

            if (!isValid(vector1, vector2)) {
                return Double.MAX_VALUE;
            }

            double sum = 0.0;

            for (int i = 0; i < vector1.length; i++) {

                double v1 = sanitize(vector1[i]);
                double v2 = sanitize(vector2[i]);

                sum += Math.abs(v1 - v2);
            }

            return sum;
        }

        @Override
        public String getName() {
            return "MANHATTAN";
        }

        @Override
        public String getDescription() {
            return """
                    Manhattan Distance (L1 Norm)

                    Formula:
                    Σ|xi - yi|

                    Use Cases:
                    - Grid movement
                    - Sparse data
                    - High dimensional data
                    """;
        }
    }

    // =========================================================
    // MINKOWSKI DISTANCE
    // =========================================================

    /**
     * Công thức:
     *
     * d(x,y) = (Σ|xi-yi|^p)^(1/p)
     */
    public static class MinkowskiDistance extends DistanceMetric {

        private final double p;

        public MinkowskiDistance(double p) {

            if (p <= 0) {
                throw new IllegalArgumentException("p must be > 0");
            }

            this.p = p;
        }

        @Override
        public double calculate(double[] vector1, double[] vector2) {

            if (!isValid(vector1, vector2)) {
                return Double.MAX_VALUE;
            }

            double sum = 0.0;

            for (int i = 0; i < vector1.length; i++) {

                double v1 = sanitize(vector1[i]);
                double v2 = sanitize(vector2[i]);

                sum += Math.pow(Math.abs(v1 - v2), p);
            }

            return Math.pow(sum, 1.0 / p);
        }

        @Override
        public String getName() {
            return "MINKOWSKI(p=" + p + ")";
        }

        @Override
        public String getDescription() {
            return """
                    Minkowski Distance

                    Formula:
                    (Σ|xi-yi|^p)^(1/p)

                    Special Cases:
                    p=1 -> Manhattan
                    p=2 -> Euclidean

                    Use Cases:
                    - Flexible distance
                    - Multi-dimensional vectors
                    """;
        }

        public double getP() {
            return p;
        }
    }

    // =========================================================
    // HAMMING DISTANCE
    // =========================================================

    /**
     * Công thức:
     *
     * số lượng vị trí khác nhau
     */
    public static class HammingDistance extends DistanceMetric {

        @Override
        public double calculate(double[] vector1, double[] vector2) {

            if (!isValid(vector1, vector2)) {
                return Double.MAX_VALUE;
            }

            int differences = 0;

            for (int i = 0; i < vector1.length; i++) {

                boolean bit1 = sanitize(vector1[i]) != 0.0;
                boolean bit2 = sanitize(vector2[i]) != 0.0;

                if (bit1 != bit2) {
                    differences++;
                }
            }

            return differences;
        }

        @Override
        public String getName() {
            return "HAMMING";
        }

        @Override
        public String getDescription() {
            return """
                    Hamming Distance

                    Formula:
                    count(xi != yi)

                    Use Cases:
                    - Binary vectors
                    - Boolean comparison
                    - Security signatures
                    """;
        }
    }

    // =========================================================
    // COSINE DISTANCE
    // =========================================================

    /**
     * Công thức:
     *
     * 1 - (A.B / |A||B|)
     */
    public static class CosineDistance extends DistanceMetric {

        @Override
        public double calculate(double[] vector1, double[] vector2) {

            if (!isValid(vector1, vector2)) {
                return Double.MAX_VALUE;
            }

            double dotProduct = 0.0;
            double norm1 = 0.0;
            double norm2 = 0.0;

            for (int i = 0; i < vector1.length; i++) {

                double v1 = sanitize(vector1[i]);
                double v2 = sanitize(vector2[i]);

                dotProduct += v1 * v2;

                norm1 += v1 * v1;
                norm2 += v2 * v2;
            }

            norm1 = Math.sqrt(norm1);
            norm2 = Math.sqrt(norm2);

            if (norm1 == 0.0 || norm2 == 0.0) {
                return 1.0;
            }

            double cosineSimilarity = dotProduct / (norm1 * norm2);

            cosineSimilarity = Math.max(-1.0,
                    Math.min(1.0, cosineSimilarity));

            return 1.0 - cosineSimilarity;
        }

        @Override
        public String getName() {
            return "COSINE";
        }

        @Override
        public String getDescription() {
            return """
                    Cosine Distance

                    Formula:
                    1 - (A.B / |A||B|)

                    Use Cases:
                    - NLP
                    - Embedding vectors
                    - Similarity search
                    - Fraud pattern analysis
                    """;
        }
    }

    // =========================================================
    // FACTORY
    // =========================================================

    public static DistanceMetric getMetric(String name) {

        if (name == null) {
            return new EuclideanDistance();
        }

        return switch (name.toUpperCase()) {

            case "EUCLIDEAN" ->
                    new EuclideanDistance();

            case "MANHATTAN" ->
                    new ManhattanDistance();

            case "HAMMING" ->
                    new HammingDistance();

            case "COSINE" ->
                    new CosineDistance();

            case "MINKOWSKI" ->
                    new MinkowskiDistance(2.0);

            default ->
                    new EuclideanDistance();
        };
    }

    /**
     * Tạo Minkowski custom
     */
    public static DistanceMetric createMinkowski(double p) {
        return new MinkowskiDistance(p);
    }

    // =========================================================
    // DEBUG
    // =========================================================

    public static void printVector(double[] vector) {

        if (vector == null) {
            System.out.println("null");
            return;
        }

        System.out.println(Arrays.toString(vector));
    }
}