package com.example.servingwebcontent.service;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Enhanced Consensus Engine Service
 * 
 * Vietnamese: Dịch vụ Consensus Engine nâng cao
 * 
 * Requirement 7: Yêu cầu Consensus Engine
 * Hệ thống cần: Consensus Logic để:
 * - so sánh kết quả
 * - giảm sai số
 * - xử lý trường hợp lệch dữ liệu
 * 
 * Example 1 - High Convergence (độ tin cậy cao):
 *   Thuật toán   Kết quả
 *   Euclidid     90%
 *   Minkowski    88%
 *   Hamming      95%
 *   => độ tin cậy cao
 * 
 * Example 2 - Low Convergence (tăng mức nghi ngờ):
 *   Thuật toán   Kết quả
 *   Euclidid     10%
 *   Minkowski    15%
 *   Hamming      95%
 *   => tăng mức nghi ngờ và giám sát
 */
@Service
public class EnhancedConsensusEngineService {

    // Method weights (can be configured)
    private static final double WEIGHT_RULE = 0.40;        // Rule-Based foundation
    private static final double WEIGHT_KNN = 0.25;         // KNN behavioral analysis
    private static final double WEIGHT_REGION = 0.20;      // Multi-Region analysis
    private static final double WEIGHT_PROBABILITY = 0.15; // Statistical probability

    /**
     * Convergence Analysis Result
     * Analyzes how well different algorithms agree
     */
    public static class ConvergenceAnalysis {
        public final double euclideanScore;
        public final double minkowskiScore;
        public final double hammingScore;
        public final double averageScore;
        public final double standardDeviation;
        public final double convergenceRatio;    // How well do methods agree (0-1)
        public final boolean isHighConvergence;  // > 0.7 = high
        public final boolean isLowConvergence;   // < 0.3 = low
        public final String status;
        public final List<String> details;

        public ConvergenceAnalysis(double euclidean, double minkowski, double hamming) {
            this.euclideanScore = euclidean;
            this.minkowskiScore = minkowski;
            this.hammingScore = hamming;

            // Calculate statistics
            this.averageScore = (euclidean + minkowski + hamming) / 3.0;
            this.standardDeviation = calculateStdDev(euclidean, minkowski, hamming, averageScore);
            this.convergenceRatio = calculateConvergence(standardDeviation);
            this.isHighConvergence = convergenceRatio > 0.7;
            this.isLowConvergence = convergenceRatio < 0.3;
            this.status = determineStatus(convergenceRatio);
            this.details = generateDetails();
        }

        private double calculateStdDev(double e, double m, double h, double avg) {
            double variance = (Math.pow(e - avg, 2) + Math.pow(m - avg, 2) + Math.pow(h - avg, 2)) / 3.0;
            return Math.sqrt(variance);
        }

        private double calculateConvergence(double stdDev) {
            // Lower std dev = higher convergence
            // Exponential decay: e^(-stdDev * factor)
            return Math.exp(-stdDev * 2.0);
        }

        private String determineStatus(double ratio) {
            if (ratio > 0.7) return "HIGH_CONVERGENCE - High confidence";
            if (ratio > 0.5) return "MEDIUM_CONVERGENCE - Moderate confidence";
            if (ratio > 0.3) return "LOW_CONVERGENCE - Low confidence";
            return "DIVERGENCE - Very low confidence, possible obfuscation";
        }

        private List<String> generateDetails() {
            List<String> details = new ArrayList<>();
            details.add(String.format("Euclidean: %.2f%%", euclideanScore * 100));
            details.add(String.format("Minkowski: %.2f%%", minkowskiScore * 100));
            details.add(String.format("Hamming: %.2f%%", hammingScore * 100));
            details.add(String.format("Average: %.2f%%", averageScore * 100));
            details.add(String.format("Std Dev: %.4f", standardDeviation));
            details.add(String.format("Convergence: %.2f%%", convergenceRatio * 100));
            return details;
        }
    }

    /**
     * Method Agreement Analysis
     * Checks how well different analysis methods (Rule, KNN, Region, Probability) agree
     */
    public static class MethodAgreementAnalysis {
        public final double ruleScore;
        public final double knnScore;
        public final double regionScore;
        public final double probabilityScore;
        public final double average;
        public final double stdDev;
        public final double agreementRatio;
        public final List<String> agreeingMethods;
        public final List<String> disagreeingMethods;
        public final String consensus;
        public final boolean hasConsensus;
        public final List<String> anomalies;

        public MethodAgreementAnalysis(double rule, double knn, double region, double probability) {
            this.ruleScore = rule;
            this.knnScore = knn;
            this.regionScore = region;
            this.probabilityScore = probability;

            // Calculate statistics
            this.average = (rule + knn + region + probability) / 4.0;
            this.stdDev = calculateStdDev(rule, knn, region, probability, average);
            this.agreementRatio = calculateAgreement(stdDev);

            // Classify methods as agreeing or disagreeing
            this.agreeingMethods = new ArrayList<>();
            this.disagreeingMethods = new ArrayList<>();
            classifyMethods();

            // Determine if consensus exists
            this.hasConsensus = agreeingMethods.size() >= 3;
            this.consensus = determineConsensus();
            this.anomalies = detectAnomalies();
        }

        private double calculateStdDev(double r, double k, double rg, double p, double avg) {
            double variance = (Math.pow(r - avg, 2) + Math.pow(k - avg, 2) + 
                             Math.pow(rg - avg, 2) + Math.pow(p - avg, 2)) / 4.0;
            return Math.sqrt(variance);
        }

        private double calculateAgreement(double stdDev) {
            // Lower std dev = higher agreement
            return Math.exp(-stdDev * 3.0);
        }

        private void classifyMethods() {
            // If method score is within 0.15 of average, it's "agreeing"
            if (Math.abs(ruleScore - average) < 0.15) {
                agreeingMethods.add("Rule-Based");
            } else {
                disagreeingMethods.add("Rule-Based");
            }

            if (Math.abs(knnScore - average) < 0.15) {
                agreeingMethods.add("KNN");
            } else {
                disagreeingMethods.add("KNN");
            }

            if (Math.abs(regionScore - average) < 0.15) {
                agreeingMethods.add("Multi-Region");
            } else {
                disagreeingMethods.add("Multi-Region");
            }

            if (Math.abs(probabilityScore - average) < 0.15) {
                agreeingMethods.add("Probability");
            } else {
                disagreeingMethods.add("Probability");
            }
        }

        private String determineConsensus() {
            if (average > 0.67) return "HIGH_RISK (Consensus: Fraud)";
            if (average > 0.33) return "MEDIUM_RISK (Consensus: Suspicious)";
            return "LOW_RISK (Consensus: Safe)";
        }

        private List<String> detectAnomalies() {
            List<String> anomalies = new ArrayList<>();

            // Check for extreme disagreements
            double[] scores = {ruleScore, knnScore, regionScore, probabilityScore};
            double max = Arrays.stream(scores).max().orElse(0.0);
            double min = Arrays.stream(scores).min().orElse(0.0);
            double range = max - min;

            if (range > 0.5) {
                anomalies.add("EXTREME_DISAGREEMENT: 50%+ score difference");
                anomalies.add(String.format("  Range: [%.2f, %.2f]", min, max));
            } else if (range > 0.25) {
                anomalies.add("SIGNIFICANT_DISAGREEMENT: 25%+ score difference");
            }

            // Check for specific anomalous patterns
            int highScores = 0;
            int lowScores = 0;
            for (double score : scores) {
                if (score > 0.75) highScores++;
                if (score < 0.25) lowScores++;
            }

            if (highScores > 0 && lowScores > 0) {
                anomalies.add("BIMODAL_DISTRIBUTION: Methods split into high and low");
                anomalies.add("  Possible obfuscated attack or borderline case");
            }

            return anomalies;
        }

        @Override
        public String toString() {
            return String.format(
                "Method Agreement:\n" +
                "  Rule-Based: %.2f%% %s\n" +
                "  KNN: %.2f%% %s\n" +
                "  Multi-Region: %.2f%% %s\n" +
                "  Probability: %.2f%% %s\n" +
                "  Average: %.2f%%\n" +
                "  Agreement: %.2f%%\n" +
                "  Consensus: %s",
                ruleScore * 100, inRange(ruleScore) ? "✓" : "✗",
                knnScore * 100, inRange(knnScore) ? "✓" : "✗",
                regionScore * 100, inRange(regionScore) ? "✓" : "✗",
                probabilityScore * 100, inRange(probabilityScore) ? "✓" : "✗",
                average * 100,
                agreementRatio * 100,
                consensus
            );
        }

        private boolean inRange(double score) {
            return Math.abs(score - average) < 0.15;
        }
    }

    /**
     * Produce enhanced consensus with convergence and agreement analysis
     */
    public ConsensusResultEnhanced produceEnhancedConsensus(
            double ruleScore,
            double knnScoreEuclidean,
            double knnScoreMinkowski,
            double knnScoreHamming,
            double regionScore,
            double probabilityScore) {

        ConsensusResultEnhanced result = new ConsensusResultEnhanced();

        // Normalize scores to [0, 1]
        ruleScore = Math.max(0.0, Math.min(1.0, ruleScore));
        knnScoreEuclidean = Math.max(0.0, Math.min(1.0, knnScoreEuclidean));
        knnScoreMinkowski = Math.max(0.0, Math.min(1.0, knnScoreMinkowski));
        knnScoreHamming = Math.max(0.0, Math.min(1.0, knnScoreHamming));
        regionScore = Math.max(0.0, Math.min(1.0, regionScore));
        probabilityScore = Math.max(0.0, Math.min(1.0, probabilityScore));

        // Analyze KNN convergence (how well do the three distance metrics agree)
        ConvergenceAnalysis knnConvergence = new ConvergenceAnalysis(
            knnScoreEuclidean, knnScoreMinkowski, knnScoreHamming);
        result.setKnnConvergence(knnConvergence);

        // Average KNN score (weighted by convergence)
        double knnScore = knnConvergence.averageScore;
        if (!knnConvergence.isHighConvergence) {
            // Reduce confidence if KNN methods don't converge
            knnScore *= knnConvergence.convergenceRatio;
        }

        // Analyze method agreement
        MethodAgreementAnalysis methodAgreement = new MethodAgreementAnalysis(
            ruleScore, knnScore, regionScore, probabilityScore);
        result.setMethodAgreement(methodAgreement);

        // Calculate weighted consensus
        double consensusScore = (ruleScore * WEIGHT_RULE) +
                (knnScore * WEIGHT_KNN) +
                (regionScore * WEIGHT_REGION) +
                (probabilityScore * WEIGHT_PROBABILITY);

        result.setConsensusScore(consensusScore);
        result.setRiskLevel(determineRiskLevel(consensusScore));

        // Calculate confidence
        double baseConfidence = calculateConfidence(methodAgreement);
        double adjustedConfidence = adjustConfidenceByConvergence(baseConfidence, knnConvergence);
        result.setConfidence(adjustedConfidence);

        // Store individual scores
        result.setRuleScore(ruleScore);
        result.setKnnScore(knnScore);
        result.setRegionScore(regionScore);
        result.setProbabilityScore(probabilityScore);

        return result;
    }

    /**
     * Calculate confidence based on method agreement
     */
    private double calculateConfidence(MethodAgreementAnalysis agreement) {
        if (agreement.hasConsensus) {
            return Math.min(1.0, 0.8 + (agreement.agreementRatio * 0.2));
        } else {
            return Math.min(1.0, agreement.agreementRatio * 0.6);
        }
    }

    /**
     * Adjust confidence by KNN convergence
     */
    private double adjustConfidenceByConvergence(double confidence, ConvergenceAnalysis convergence) {
        if (convergence.isHighConvergence) {
            return Math.min(1.0, confidence * 1.2);
        } else if (convergence.isLowConvergence) {
            return confidence * 0.7;
        }
        return confidence;
    }

    /**
     * Determine risk level
     */
    private String determineRiskLevel(double score) {
        if (score < 0.33) return "SAFE";
        if (score < 0.67) return "SUSPICIOUS";
        return "CRITICAL";
    }

    /**
     * Enhanced Consensus Result
     */
    public static class ConsensusResultEnhanced {
        private double consensusScore;
        private String riskLevel;
        private double confidence;
        private double ruleScore;
        private double knnScore;
        private double regionScore;
        private double probabilityScore;
        private ConvergenceAnalysis knnConvergence;
        private MethodAgreementAnalysis methodAgreement;

        // Getters and Setters
        public double getConsensusScore() { return consensusScore; }
        public void setConsensusScore(double score) { this.consensusScore = score; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String level) { this.riskLevel = level; }

        public double getConfidence() { return confidence; }
        public void setConfidence(double conf) { this.confidence = conf; }

        public double getRuleScore() { return ruleScore; }
        public void setRuleScore(double score) { this.ruleScore = score; }

        public double getKnnScore() { return knnScore; }
        public void setKnnScore(double score) { this.knnScore = score; }

        public double getRegionScore() { return regionScore; }
        public void setRegionScore(double score) { this.regionScore = score; }

        public double getProbabilityScore() { return probabilityScore; }
        public void setProbabilityScore(double score) { this.probabilityScore = score; }

        public ConvergenceAnalysis getKnnConvergence() { return knnConvergence; }
        public void setKnnConvergence(ConvergenceAnalysis analysis) { this.knnConvergence = analysis; }

        public MethodAgreementAnalysis getMethodAgreement() { return methodAgreement; }
        public void setMethodAgreement(MethodAgreementAnalysis analysis) { this.methodAgreement = analysis; }

        public boolean hasAnomalies() {
            return methodAgreement != null && !methodAgreement.anomalies.isEmpty();
        }

        @Override
        public String toString() {
            return String.format(
                "Enhanced Consensus:\n" +
                "  Risk Level: %s (%.3f)\n" +
                "  Confidence: %.1f%%\n" +
                "  Method Agreement: %s\n" +
                "  Anomalies: %s",
                riskLevel, consensusScore, confidence * 100,
                methodAgreement != null ? methodAgreement.consensus : "N/A",
                methodAgreement != null && !methodAgreement.anomalies.isEmpty() ?
                    methodAgreement.anomalies : "None"
            );
        }
    }
}
