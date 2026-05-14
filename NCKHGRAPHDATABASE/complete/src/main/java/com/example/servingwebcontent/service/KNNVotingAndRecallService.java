package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * KNN Voting and Recall Service
 * 
 * Vietnamese: Dịch vụ bỏ phiếu và Recall cho KNN
 * 
 * Requirement 6: Yêu cầu voting - Sau khi tìm K node gần nhất, hệ thống cần bỏ phiếu
 * Example:
 *   SAFE   FRAUD
 *   1      6
 *   => kết luận: node gần hành vi gian lận
 * 
 * Requirement 7: Yêu cầu độ tin cậy
 *   P < 0.05 - có ý nghĩa thống kê
 *   Recall ≥ 80% - phát hiện tốt
 *   Recall = TP / (TP + FN) × 100%
 */
@Service
public class KNNVotingAndRecallService {

    /**
     * Voting result from K-nearest neighbors
     * Each neighbor votes based on its classification
     */
    public static class VotingResult {
        private final Map<String, Integer> votes = new HashMap<>();
        private final int totalVoters;
        private final String winner;
        private final double winnerRatio;
        private final double confidence;
        private final List<String> details;

        public VotingResult(Map<String, Integer> votes, int totalVoters, String winner,
                           double winnerRatio, double confidence, List<String> details) {
            this.votes.putAll(votes);
            this.totalVoters = totalVoters;
            this.winner = winner;
            this.winnerRatio = winnerRatio;
            this.confidence = confidence;
            this.details = new ArrayList<>(details);
        }

        public Map<String, Integer> getVotes() { return votes; }
        public int getTotalVoters() { return totalVoters; }
        public String getWinner() { return winner; }
        public double getWinnerRatio() { return winnerRatio; }
        public double getConfidence() { return confidence; }
        public List<String> getDetails() { return details; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Voting Result:\n");
            votes.forEach((classification, count) ->
                sb.append(String.format("  %s: %d votes (%.1f%%)\n", 
                    classification, count, (count * 100.0 / totalVoters)))
            );
            sb.append(String.format("Winner: %s (%.1f%% confidence)\n", winner, confidence * 100));
            return sb.toString();
        }
    }

    /**
     * Perform voting among K-nearest neighbors
     * Each neighbor casts one vote based on its classification
     * 
     * Example:
     *   K=7 neighbors:
     *   6 vote FRAUD
     *   1 votes SAFE
     *   Winner: FRAUD (85.7% votes)
     */
    public VotingResult performVoting(List<KNNNeighbor> neighbors, int k) {
        if (neighbors == null || neighbors.isEmpty()) {
            return createEmptyVotingResult();
        }

        // Take only K nearest neighbors
        int actualK = Math.min(k, neighbors.size());
        List<KNNNeighbor> kNearest = neighbors.subList(0, actualK);

        // Count votes by classification
        Map<String, Integer> votes = new HashMap<>();
        for (KNNNeighbor neighbor : kNearest) {
            String classification = neighbor.getClassification();
            votes.put(classification, votes.getOrDefault(classification, 0) + 1);
        }

        // Determine winner (majority vote)
        String winner = null;
        int maxVotes = 0;
        for (Map.Entry<String, Integer> entry : votes.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                winner = entry.getKey();
            }
        }

        if (winner == null) {
            winner = "UNKNOWN";
        }

        double winnerRatio = (double) maxVotes / actualK;
        double confidence = calculateVotingConfidence(winnerRatio, actualK);

        List<String> details = new ArrayList<>();
        details.add(String.format("K-value: %d", actualK));
        details.add(String.format("Total voters: %d", actualK));
        
        final String finalWinner = winner;
        votes.forEach((classification, count) ->
            details.add(String.format("%s: %d votes (%.1f%%)", 
                classification, count, (count * 100.0 / actualK)))
        );
        details.add(String.format("Winner: %s with %.1f%% votes", finalWinner, winnerRatio * 100));
        details.add(String.format("Confidence: %.2f%%", confidence * 100));

        return new VotingResult(votes, actualK, finalWinner, winnerRatio, confidence, details);
    }

    /**
     * Calculate confidence of voting result
     * Based on how decisive the majority is
     * 
     * Formula:
     * - If winner has 100% votes: confidence = 1.0 (unanimous)
     * - If winner has 51% votes: confidence = 0.02 (barely won)
     * - Uses sigmoid-like scaling for smooth transitions
     */
    private double calculateVotingConfidence(double winnerRatio, int k) {
        if (k <= 1) return 1.0; // Single vote is conclusive
        
        // For k=7:
        // Winner ratio 6/7 = 0.857 -> confidence ~0.7
        // Winner ratio 5/7 = 0.714 -> confidence ~0.4
        // Winner ratio 4/7 = 0.571 -> confidence ~0.1
        
        double scaledRatio = (winnerRatio - 0.5) / 0.5; // Scale from [0.5, 1.0] to [0, 1]
        return Math.max(0.0, scaledRatio);
    }

    /**
     * KNN Neighbor representation for voting
     */
    public static class KNNNeighbor {
        private final BehaviorFeatureVector vector;
        private final String classification;  // "SAFE", "SUSPICIOUS", "FRAUD"
        private final double distance;
        private final double weight; // Inverse distance weight

        public KNNNeighbor(BehaviorFeatureVector vector, String classification, double distance) {
            this.vector = vector;
            this.classification = classification;
            this.distance = distance;
            this.weight = 1.0 / Math.max(0.01, distance);
        }

        public BehaviorFeatureVector getVector() { return vector; }
        public String getClassification() { return classification; }
        public double getDistance() { return distance; }
        public double getWeight() { return weight; }
    }

    /**
     * Weighted voting - neighbors closer to the point have more influence
     */
    public VotingResult performWeightedVoting(List<KNNNeighbor> neighbors, int k) {
        if (neighbors == null || neighbors.isEmpty()) {
            return createEmptyVotingResult();
        }

        int actualK = Math.min(k, neighbors.size());
        List<KNNNeighbor> kNearest = neighbors.subList(0, actualK);

        // Weighted vote counting
        Map<String, Double> weightedVotes = new HashMap<>();
        double totalWeight = 0.0;

        for (KNNNeighbor neighbor : kNearest) {
            String classification = neighbor.getClassification();
            double weight = neighbor.getWeight();

            weightedVotes.put(classification, 
                weightedVotes.getOrDefault(classification, 0.0) + weight);
            totalWeight += weight;
        }

        // Determine winner
        String winner = null;
        double maxWeightedVotes = 0.0;
        for (Map.Entry<String, Double> entry : weightedVotes.entrySet()) {
            if (entry.getValue() > maxWeightedVotes) {
                maxWeightedVotes = entry.getValue();
                winner = entry.getKey();
            }
        }

        if (winner == null) {
            winner = "UNKNOWN";
        }

        double winnerRatio = maxWeightedVotes / totalWeight;
        double confidence = calculateVotingConfidence(winnerRatio, actualK);

        List<String> details = new ArrayList<>();
        details.add(String.format("Weighted Voting (K=%d)", actualK));
        details.add(String.format("Total weight: %.2f", totalWeight));
        
        final String finalWinner = winner;
        final double finalTotalWeight = totalWeight;
        weightedVotes.forEach((classification, weight) ->
            details.add(String.format("%s: %.2f weighted votes (%.1f%%)", 
                classification, weight, (weight * 100.0 / finalTotalWeight)))
        );
        details.add(String.format("Winner: %s with %.1f%% weighted votes", finalWinner, winnerRatio * 100));

        return new VotingResult(new HashMap<>(), actualK, finalWinner, winnerRatio, confidence, details);
    }

    /**
     * Recall Calculation
     * Recall = TP / (TP + FN) × 100%
     * 
     * TP (True Positive): Fraud cases correctly identified as fraud
     * FN (False Negative): Fraud cases incorrectly identified as safe/suspicious
     * 
     * Requirement: Recall ≥ 80% (phát hiện tốt)
     */
    public static class RecallMetrics {
        public final int truePositives;      // Correctly identified fraud
        public final int falseNegatives;     // Missed fraud cases
        public final int trueNegatives;      // Correctly identified safe
        public final int falsePositives;     // False fraud alarms
        public final double recall;          // TP / (TP + FN)
        public final double precision;       // TP / (TP + FP)
        public final double fScore;          // Harmonic mean of precision and recall
        public final boolean meetsRequirement; // recall >= 0.80

        public RecallMetrics(int tp, int fn, int tn, int fp) {
            this.truePositives = tp;
            this.falseNegatives = fn;
            this.trueNegatives = tn;
            this.falsePositives = fp;

            // Calculate metrics
            this.recall = (tp + fn == 0) ? 0.0 : (double) tp / (tp + fn);
            this.precision = (tp + fp == 0) ? 0.0 : (double) tp / (tp + fp);
            this.fScore = calculateFScore(precision, recall);
            this.meetsRequirement = recall >= 0.80;
        }

        private double calculateFScore(double precision, double recall) {
            if (precision + recall == 0) return 0.0;
            return 2.0 * (precision * recall) / (precision + recall);
        }

        @Override
        public String toString() {
            return String.format(
                "Recall Metrics:\n" +
                "  TP (Correct Fraud): %d\n" +
                "  FN (Missed Fraud): %d\n" +
                "  TN (Correct Safe): %d\n" +
                "  FP (False Alarms): %d\n" +
                "  Recall: %.2f%% %s\n" +
                "  Precision: %.2f%%\n" +
                "  F-Score: %.2f%%",
                truePositives, falseNegatives, trueNegatives, falsePositives,
                recall * 100, meetsRequirement ? "✓" : "✗",
                precision * 100, fScore * 100
            );
        }
    }

    /**
     * Calculate recall metrics from confusion matrix
     */
    public RecallMetrics calculateRecall(int tp, int fn, int tn, int fp) {
        return new RecallMetrics(tp, fn, tn, fp);
    }

    /**
     * Statistical Significance Testing
     * P-value < 0.05 indicates statistically significant result
     * 
     * Vietnamese: P < 0.05 có ý nghĩa thống kê
     */
    public static class StatisticalSignificance {
        public final double pValue;
        public final boolean isSignificant;  // p-value < 0.05
        public final String interpretation;
        public final int sampleSize;

        public StatisticalSignificance(double pValue, int sampleSize) {
            this.pValue = pValue;
            this.sampleSize = sampleSize;
            this.isSignificant = pValue < 0.05;
            this.interpretation = generateInterpretation(pValue);
        }

        private String generateInterpretation(double p) {
            if (p < 0.001) return "Highly significant (p < 0.001)";
            if (p < 0.01) return "Very significant (p < 0.01)";
            if (p < 0.05) return "Significant (p < 0.05)";
            if (p < 0.10) return "Marginally significant (p < 0.10)";
            return "Not significant (p ≥ 0.10)";
        }

        @Override
        public String toString() {
            return String.format("P-value: %.4f (%s) [n=%d]", pValue, interpretation, sampleSize);
        }
    }

    /**
     * Calculate p-value using chi-square test
     * Compares observed classification results against expected distribution
     */
    public StatisticalSignificance calculatePValue(int observedFraud, int observedSafe, 
                                                  int expectedFraud, int expectedSafe) {
        // Chi-square statistic: Σ((O-E)²/E)
        double chi2 = 0.0;

        if (expectedFraud > 0) {
            chi2 += Math.pow(observedFraud - expectedFraud, 2) / expectedFraud;
        }
        
        if (expectedSafe > 0) {
            chi2 += Math.pow(observedSafe - expectedSafe, 2) / expectedSafe;
        }

        // Approximate p-value using chi-square distribution
        // For 1 degree of freedom:
        // p-value ≈ erfc(sqrt(chi2/2))
        double pValue = approximatePValue(chi2, 1);

        return new StatisticalSignificance(pValue, observedFraud + observedSafe);
    }

    /**
     * Approximate p-value from chi-square statistic
     */
    private double approximatePValue(double chi2, int degreesOfFreedom) {
        // Simplified approximation
        if (chi2 < 2.706) return 0.10;  // p >= 0.10
        if (chi2 < 3.841) return 0.05;  // 0.05 <= p < 0.10
        if (chi2 < 6.635) return 0.01;  // 0.01 <= p < 0.05
        if (chi2 < 10.828) return 0.001; // p < 0.01
        return 0.0001;
    }

    /**
     * Create empty/default voting result
     */
    private VotingResult createEmptyVotingResult() {
        Map<String, Integer> emptyVotes = new HashMap<>();
        emptyVotes.put("UNKNOWN", 0);
        List<String> details = List.of("No neighbors available for voting");
        return new VotingResult(emptyVotes, 0, "UNKNOWN", 0.0, 0.0, details);
    }
}
