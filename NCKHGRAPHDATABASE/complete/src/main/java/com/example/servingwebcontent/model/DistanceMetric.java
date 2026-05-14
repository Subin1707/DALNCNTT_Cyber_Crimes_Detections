package com.example.servingwebcontent.model;

/**
 * KHOẢNG CÁCH - Các thuật toán tính khoảng cách giữa nodes
 * 
 * Kết hợp 3 thuật toán:
 * 1. EUCLIDEAN: Khoảng cách Euclid (dữ liệu số)
 * 2. MINKOWSKI: Khoảng cách Minkowski (dữ liệu nhiều chiều)
 * 3. HAMMING: Khoảng cách Hamming (dữ liệu boolean)
 */
public abstract class DistanceMetric {

    public abstract double calculate(double[] vector1, double[] vector2);
    public abstract String getName();
    public abstract String getDescription();

    // ============== EUCLIDEAN DISTANCE ==============
    /**
     * Khoảng cách Euclid (L2 norm)
     * 
     * Formula: sqrt(Σ(xi - yi)²)
     * 
     * Ứng dụng:
     * - Dữ liệu liên tục
     * - Vector đặc trưng số thực
     */
    public static class EuclideanDistance extends DistanceMetric {
        @Override
        public double calculate(double[] vector1, double[] vector2) {
            if (vector1 == null || vector2 == null ||
                vector1.length != vector2.length) {
                return Double.MAX_VALUE;
            }

            double sum = 0.0;
            for (int i = 0; i < vector1.length; i++) {
                double diff = vector1[i] - vector2[i];
                sum += diff * diff;
            }

            return Math.sqrt(sum);
        }

        @Override
        public String getName() { return "EUCLIDEAN"; }

        @Override
        public String getDescription() {
            return """
                Khoảng cách Euclid (L2 norm)
                Formula: sqrt(Σ(xi - yi)²)
                Ứng dụng: Dữ liệu liên tục, vector đặc trưng số thực
                Ví dụ: Khoảng cách giữa 2 điểm trong không gian 3D
                """;
        }
    }

    // ============== MINKOWSKI DISTANCE ==============
    /**
     * Khoảng cách Minkowski (Lp norm)
     * 
     * Formula: (Σ|xi - yi|^p)^(1/p)
     * 
     * Trường hợp đặc biệt:
     * - p=1: Manhattan distance (L1 norm)
     * - p=2: Euclidean distance (L2 norm)
     * - p=∞: Chebyshev distance (L∞ norm)
     * 
     * Ứng dụng:
     * - Dữ liệu nhiều chiều
     * - Điều chỉnh linh hoạt theo p
     */
    public static class MinkowskiDistance extends DistanceMetric {
        private final double p;

        public MinkowskiDistance(double p) {
            this.p = p;
        }

        @Override
        public double calculate(double[] vector1, double[] vector2) {
            if (vector1 == null || vector2 == null ||
                vector1.length != vector2.length) {
                return Double.MAX_VALUE;
            }

            double sum = 0.0;
            for (int i = 0; i < vector1.length; i++) {
                double diff = Math.abs(vector1[i] - vector2[i]);
                sum += Math.pow(diff, p);
            }

            return Math.pow(sum, 1.0 / p);
        }

        @Override
        public String getName() { return "MINKOWSKI(p=" + p + ")"; }

        @Override
        public String getDescription() {
            return String.format("""
                Khoảng cách Minkowski (Lp norm, p=%.1f)
                Formula: (Σ|xi - yi|^%.1f)^(1/%.1f)
                
                Trường hợp đặc biệt:
                - p=1: Manhattan distance (L1 norm)
                - p=2: Euclidean distance (L2 norm)
                - p=∞: Chebyshev distance (L∞ norm)
                
                Ứng dụng: Dữ liệu nhiều chiều, điều chỉnh linh hoạt
                """, p, p, p);
        }

        public double getP() { return p; }
    }

    // ============== HAMMING DISTANCE ==============
    /**
     * Khoảng cách Hamming
     * 
     * Formula: Số lượng vị trí khác nhau giữa 2 vector
     * 
     * Ứng dụng:
     * - Dữ liệu boolean (binary)
     * - So sánh bit hoặc chuỗi bit
     * 
     * Ví dụ:
     * [1,0,1,0] vs [1,1,1,0] => Hamming = 1 (khác tại vị trí 1)
     */
    public static class HammingDistance extends DistanceMetric {
        @Override
        public double calculate(double[] vector1, double[] vector2) {
            if (vector1 == null || vector2 == null ||
                vector1.length != vector2.length) {
                return Double.MAX_VALUE;
            }

            int differences = 0;
            for (int i = 0; i < vector1.length; i++) {
                // Coi vector là binary: 0 hoặc khác 0
                boolean bit1 = vector1[i] != 0.0;
                boolean bit2 = vector2[i] != 0.0;
                if (bit1 != bit2) {
                    differences++;
                }
            }

            return (double) differences;
        }

        @Override
        public String getName() { return "HAMMING"; }

        @Override
        public String getDescription() {
            return """
                Khoảng cách Hamming
                Formula: Số lượng vị trí khác nhau
                
                Ứng dụng: Dữ liệu boolean (binary), so sánh bit
                
                Ví dụ:
                [1,0,1,0] vs [1,1,1,0] => Hamming = 1
                """;
        }
    }

    // ============== Manhattan Distance (Special Minkowski) ==============
    /**
     * Khoảng cách Manhattan (Minkowski với p=1)
     * 
     * Formula: Σ|xi - yi|
     * 
     * Ứng dụng:
     * - Grid-based movement
     * - Urban distance (taxicab geometry)
     */
    public static class ManhattanDistance extends DistanceMetric {
        @Override
        public double calculate(double[] vector1, double[] vector2) {
            if (vector1 == null || vector2 == null ||
                vector1.length != vector2.length) {
                return Double.MAX_VALUE;
            }

            double sum = 0.0;
            for (int i = 0; i < vector1.length; i++) {
                sum += Math.abs(vector1[i] - vector2[i]);
            }

            return sum;
        }

        @Override
        public String getName() { return "MANHATTAN"; }

        @Override
        public String getDescription() {
            return """
                Khoảng cách Manhattan (L1 norm)
                Formula: Σ|xi - yi|
                
                Ứng dụng: Grid-based movement, urban distance
                Còn gọi: Taxicab geometry
                """;
        }
    }

    // ============== Cosine Similarity (1 - Cosine Similarity) ==============
    /**
     * Khoảng cách dựa trên Cosine Similarity
     * 
     * Formula: 1 - (u·v) / (||u|| * ||v||)
     * 
     * Ứng dụng:
     * - Vector cao chiều
     * - So sánh hướng của vector
     * - Phân tích văn bản
     */
    public static class CosineDistance extends DistanceMetric {
        @Override
        public double calculate(double[] vector1, double[] vector2) {
            if (vector1 == null || vector2 == null ||
                vector1.length != vector2.length) {
                return Double.MAX_VALUE;
            }

            double dotProduct = 0.0;
            double norm1 = 0.0;
            double norm2 = 0.0;

            for (int i = 0; i < vector1.length; i++) {
                dotProduct += vector1[i] * vector2[i];
                norm1 += vector1[i] * vector1[i];
                norm2 += vector2[i] * vector2[i];
            }

            norm1 = Math.sqrt(norm1);
            norm2 = Math.sqrt(norm2);

            if (norm1 == 0.0 || norm2 == 0.0) {
                return Double.MAX_VALUE;
            }

            double cosineSimilarity = dotProduct / (norm1 * norm2);
            return 1.0 - cosineSimilarity;
        }

        @Override
        public String getName() { return "COSINE"; }

        @Override
        public String getDescription() {
            return """
                Khoảng cách Cosine (1 - Cosine Similarity)
                Formula: 1 - (u·v) / (||u|| * ||v||)
                
                Ứng dụng: Vector cao chiều, so sánh hướng
                Thường dùng: Phân tích văn bản, NLP
                """;
        }
    }

    // ============== Factory Method ==============
    public static DistanceMetric getMetric(String name) {
        return switch (name.toUpperCase()) {
            case "EUCLIDEAN" -> new EuclideanDistance();
            case "MINKOWSKI" -> new MinkowskiDistance(2.0);
            case "HAMMING" -> new HammingDistance();
            case "MANHATTAN" -> new ManhattanDistance();
            case "COSINE" -> new CosineDistance();
            default -> new EuclideanDistance();
        };
    }

    /**
     * Tạo Minkowski distance với p tùy chỉnh
     */
    public static DistanceMetric createMinkowski(double p) {
        return new MinkowskiDistance(p);
    }
}
