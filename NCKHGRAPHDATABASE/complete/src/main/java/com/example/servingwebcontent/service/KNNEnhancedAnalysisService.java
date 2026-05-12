package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.SessionFeatureVectorDTO;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * KNN Analysis Service with Multiple Distance Metrics
 * 
 * Implements three distance metrics as described in the system design:
 * 1. Euclidean Distance: for numeric features (IP count, URL count)
 * 2. Minkowski Distance: for multi-dimensional data (flexible distance metric)
 * 3. Hamming Distance: for boolean/categorical features (VPN, blacklist, spam)
 * 
 * The service calculates fraud probability using all three metrics independently,
 * then compares and reconciles results for higher confidence.
 */
@Service
public class KNNEnhancedAnalysisService {

    // K value for KNN
    private static final int DEFAULT_K = 7;
    
    // Minkowski distance parameter
    private static final double MINKOWSKI_P = 2.0; // Default to Euclidean-like behavior
    
    // Feature indices for vector conversion
    private static final int NUMERIC_FEATURES_COUNT = 4; // numEmails, numIps, numUrls, numDomains
    private static final int BOOLEAN_FEATURES_COUNT = 3;  // hasSharedIps, hasRepeatedUrls, hasHighRiskNodes

    /**
     * Result object containing all three distance metric calculations
     */
    public static class KNNAnalysisResult {
        public final double euclideanScore;
        public final double minkowskiScore;
        public final double hammingScore;
        public final double finalScore;
        public final double confidence;
        public final String recommendation;
        public final List<String> details;

        public KNNAnalysisResult(double euclidean, double minkowski, double hamming, 
                                 double finalScore, double confidence, String recommendation,
                                 List<String> details) {
            this.euclideanScore = euclidean;
            this.minkowskiScore = minkowski;
            this.hammingScore = hamming;
            this.finalScore = finalScore;
            this.confidence = confidence;
            this.recommendation = recommendation;
            this.details = details;
        }
    }

    /**
     * Internal class to track nearest neighbors
     */
    private static class Neighbor {
        double euclideanDist;
        double minkowskiDist;
        double hammingDist;
        boolean isFraud;
        double riskScore;

        Neighbor(double euclidean, double minkowski, double hamming, boolean fraud, double score) {
            this.euclideanDist = euclidean;
            this.minkowskiDist = minkowski;
            this.hammingDist = hamming;
            this.isFraud = fraud;
            this.riskScore = score;
        }
    }

    /**
     * Analyze fraud probability using all three distance metrics
     */
    public KNNAnalysisResult analyzeWithMultipleMetrics(
            SessionFeatureVectorDTO currentFeatures,
            List<SessionFeatureService.HistoricalSessionSample> historicalSamples) {

        if (historicalSamples == null || historicalSamples.isEmpty()) {
            return new KNNAnalysisResult(0.5, 0.5, 0.5, 0.5, 0.1, 
                "INSUFFICIENT_HISTORY", 
                List.of("Insufficient historical data for KNN analysis"));
        }

        List<String> details = new ArrayList<>();
        
        // Extract feature vectors
        double[] currentNumeric = extractNumericFeatures(currentFeatures);
        boolean[] currentBoolean = extractBooleanFeatures(currentFeatures);

        // Calculate all three distance metrics for all neighbors
        List<Neighbor> allNeighbors = new ArrayList<>();
        for (SessionFeatureService.HistoricalSessionSample sample : historicalSamples) {
            double[] sampleNumeric = extractNumericFeatures(sample.features());
            boolean[] sampleBoolean = extractBooleanFeatures(sample.features());

            double euclidean = calculateEuclideanDistance(currentNumeric, sampleNumeric);
            double minkowski = calculateMinkowskiDistance(currentNumeric, sampleNumeric, MINKOWSKI_P);
            double hamming = calculateHammingDistance(currentBoolean, sampleBoolean);

            allNeighbors.add(new Neighbor(euclidean, minkowski, hamming, sample.fraud(), sample.score()));
        }

        // Find K-nearest neighbors for each metric and calculate scores
        int k = Math.min(DEFAULT_K, historicalSamples.size());
        
        double euclideanScore = calculateKNNScore(allNeighbors, k, "euclidean");
        double minkowskiScore = calculateKNNScore(allNeighbors, k, "minkowski");
        double hammingScore = calculateKNNScore(allNeighbors, k, "hamming");

        // Record individual metric scores
        details.add(String.format("Euclidean KNN Score: %.2f%%", euclideanScore * 100));
        details.add(String.format("Minkowski KNN Score: %.2f%%", minkowskiScore * 100));
        details.add(String.format("Hamming KNN Score: %.2f%%", hammingScore * 100));
        details.add(String.format("K-value used: %d", k));
        details.add(String.format("Historical samples: %d", historicalSamples.size()));

        // Calculate confidence and final score
        double confidence = calculateConfidence(euclideanScore, minkowskiScore, hammingScore);
        double finalScore = calculateFinalScore(euclideanScore, minkowskiScore, hammingScore);
        String recommendation = generateRecommendation(euclideanScore, minkowskiScore, hammingScore, confidence);

        // Detect divergence in metrics (potential anomaly indicator)
        if (confidence < 0.5) {
            details.add("⚠️ WARNING: Significant divergence between metrics detected!");
            details.add(String.format("  - Numeric data assessment: %.1f%%", euclideanScore * 100));
            details.add(String.format("  - Multi-dimensional assessment: %.1f%%", minkowskiScore * 100));
            details.add(String.format("  - Security flags assessment: %.1f%%", hammingScore * 100));
            details.add("  → This suggests behavioral inconsistency (possibly obfuscated attack)");
        }

        details.add(String.format("Final Confidence Score: %.2f%%", confidence * 100));

        return new KNNAnalysisResult(euclideanScore, minkowskiScore, hammingScore,
                                     finalScore, confidence, recommendation, details);
    }

    /**
     * Euclidean Distance: sqrt(sum((x_i - y_i)^2))
     * Best for numeric features like IP count and URL count
     */
    private double calculateEuclideanDistance(double[] x, double[] y) {
        double sumSquares = 0.0;
        int minLen = Math.min(x.length, y.length);
        
        for (int i = 0; i < minLen; i++) {
            double diff = x[i] - y[i];
            sumSquares += diff * diff;
        }
        
        return Math.sqrt(sumSquares);
    }

    /**
     * Minkowski Distance: (sum(|x_i - y_i|^p))^(1/p)
     * When p=2, equals Euclidean. When p=1, equals Manhattan.
     * More flexible for multi-dimensional data
     */
    private double calculateMinkowskiDistance(double[] x, double[] y, double p) {
        double sum = 0.0;
        int minLen = Math.min(x.length, y.length);
        
        for (int i = 0; i < minLen; i++) {
            sum += Math.pow(Math.abs(x[i] - y[i]), p);
        }
        
        return Math.pow(sum, 1.0 / p);
    }

    /**
     * Hamming Distance: count of positions where x_i != y_i
     * Best for boolean/categorical features (VPN, blacklist, spam email, etc.)
     */
    private double calculateHammingDistance(boolean[] x, boolean[] y) {
        int differences = 0;
        int minLen = Math.min(x.length, y.length);
        
        for (int i = 0; i < minLen; i++) {
            if (x[i] != y[i]) {
                differences++;
            }
        }
        
        return (double) differences;
    }

    /**
     * Extract numeric features from session features
     * [numEmails, numIps, numUrls, numDomains]
     */
    private double[] extractNumericFeatures(SessionFeatureVectorDTO features) {
        return new double[]{
                features.getNumEmails(),
                features.getNumIps(),
                features.getNumUrls(),
                features.getNumDomains()
        };
    }

    /**
     * Extract boolean features from session features
     * [hasSharedIps, hasRepeatedUrls, hasHighRiskNodes]
     */
    private boolean[] extractBooleanFeatures(SessionFeatureVectorDTO features) {
        return new boolean[]{
                features.getNumSharedIps() > 0,
                features.getNumRepeatedUrls() > 0,
                features.getNumHighRiskNodes() > 0
        };
    }

    /**
     * Calculate KNN fraud probability score using a specific distance metric
     * Uses inverse distance weighting for K-nearest neighbors
     */
    private double calculateKNNScore(List<Neighbor> allNeighbors, int k, String metric) {
        List<Neighbor> sorted = new ArrayList<>(allNeighbors);
        
        // Sort by the specified metric
        switch (metric.toLowerCase()) {
            case "euclidean":
                sorted.sort(Comparator.comparingDouble(n -> n.euclideanDist));
                break;
            case "minkowski":
                sorted.sort(Comparator.comparingDouble(n -> n.minkowskiDist));
                break;
            case "hamming":
                sorted.sort(Comparator.comparingDouble(n -> n.hammingDist));
                break;
            default:
                sorted.sort(Comparator.comparingDouble(n -> n.euclideanDist));
        }

        // Weighted voting with K-nearest neighbors
        double fraudWeight = 0.0;
        double totalWeight = 0.0;

        for (int i = 0; i < k && i < sorted.size(); i++) {
            Neighbor neighbor = sorted.get(i);
            double distance = getDistance(neighbor, metric);
            
            // Inverse distance weighting (closer neighbors have higher weight)
            double weight = 1.0 / Math.max(0.001, distance + 1.0);
            totalWeight += weight;
            
            if (neighbor.isFraud) {
                fraudWeight += weight;
            }
        }

        return totalWeight > 0 ? fraudWeight / totalWeight : 0.5;
    }

    /**
     * Get the appropriate distance value from neighbor based on metric type
     */
    private double getDistance(Neighbor neighbor, String metric) {
        return switch (metric.toLowerCase()) {
            case "euclidean" -> neighbor.euclideanDist;
            case "minkowski" -> neighbor.minkowskiDist;
            case "hamming" -> neighbor.hammingDist;
            default -> neighbor.euclideanDist;
        };
    }

    /**
     * Calculate confidence score based on agreement between metrics
     * High confidence when all three metrics produce similar results (within ~5%)
     * Low confidence when metrics diverge significantly
     */
    private double calculateConfidence(double euclidean, double minkowski, double hamming) {
        double maxScore = Math.max(euclidean, Math.max(minkowski, hamming));
        double minScore = Math.min(euclidean, Math.min(minkowski, hamming));
        double divergence = maxScore - minScore;

        // Confidence decreases as divergence increases
        // If divergence < 0.05 (5%), confidence is high
        // If divergence > 0.30 (30%), confidence is very low
        return Math.max(0.0, 1.0 - (divergence * 2.5));
    }

    /**
     * Calculate final fraud probability score
     * Uses weighted average of three metrics
     * If any metric shows very high risk, that takes priority (safety-first principle)
     */
    private double calculateFinalScore(double euclidean, double minkowski, double hamming) {
        // Check for safety-first override: if any metric shows >80% fraud risk
        double maxScore = Math.max(euclidean, Math.max(minkowski, hamming));
        if (maxScore > 0.80) {
            return maxScore; // Take the maximum for safety
        }

        // Otherwise, use weighted average (equal weights)
        return (euclidean + minkowski + hamming) / 3.0;
    }

    /**
     * Generate human-readable recommendation based on metric comparison
     */
    private String generateRecommendation(double euclidean, double minkowski, double hamming, double confidence) {
        double maxScore = Math.max(euclidean, Math.max(minkowski, hamming));

        if (maxScore > 0.80) {
            return "🚨 CRITICAL: High fraud probability detected across metrics. Immediate investigation required.";
        } else if (maxScore > 0.60) {
            return "⚠️ HIGH: Significant fraud indicators detected. Consider blocking or monitoring.";
        } else if (maxScore > 0.40) {
            return "⚡ MEDIUM: Moderate fraud indicators present. Recommend monitoring and investigation.";
        } else if (maxScore > 0.20) {
            return "ℹ️ LOW: Minor fraud indicators. Continue routine monitoring.";
        } else {
            return "✅ SAFE: No significant fraud indicators. Allow normal operations.";
        }
    }
}
