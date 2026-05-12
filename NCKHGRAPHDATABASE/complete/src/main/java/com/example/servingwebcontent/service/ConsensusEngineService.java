package com.example.servingwebcontent.service;

import com.example.servingwebcontent.model.RegionType;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Consensus Engine Service
 * Combines multiple analysis methods to produce final risk score
 * Weighted formula: FinalRisk = 0.4*Rule + 0.25*KNN + 0.2*MultiRegion + 0.15*Probability
 */
@Service
public class ConsensusEngineService {

    // Weights for consensus
    private static final double WEIGHT_RULE = 0.40;
    private static final double WEIGHT_KNN = 0.25;
    private static final double WEIGHT_REGION = 0.20;
    private static final double WEIGHT_PROBABILITY = 0.15;

    /**
     * Produce consensus from all analysis methods
     */
    public ConsensusResult produceConsensus(double ruleScore,
                                           double knnScore,
                                           double regionScore,
                                           double probabilityScore) {
        ConsensusResult result = new ConsensusResult();

        // Validate scores
        ruleScore = Math.max(0.0, Math.min(1.0, ruleScore / 100.0));
        knnScore = Math.max(0.0, Math.min(1.0, knnScore / 100.0));
        regionScore = Math.max(0.0, Math.min(1.0, regionScore / 100.0));
        probabilityScore = Math.max(0.0, Math.min(1.0, probabilityScore / 100.0));

        // Store individual scores
        result.setRuleScore(ruleScore);
        result.setKnnScore(knnScore);
        result.setRegionScore(regionScore);
        result.setProbabilityScore(probabilityScore);

        // Calculate weighted consensus
        double consensusScore = (ruleScore * WEIGHT_RULE) +
                (knnScore * WEIGHT_KNN) +
                (regionScore * WEIGHT_REGION) +
                (probabilityScore * WEIGHT_PROBABILITY);

        result.setConsensusScore(consensusScore);

        // Calculate agreement level (how similar are the scores)
        double agreement = calculateAgreement(ruleScore, knnScore, regionScore, probabilityScore);
        result.setAgreementLevel(agreement);

        // Detect disagreement (potential anomaly)
        boolean disagreement = detectDisagreement(ruleScore, knnScore, regionScore, probabilityScore);
        result.setDisagreement(disagreement);

        // Generate detailed analysis
        result.setAnalysis(generateAnalysis(ruleScore, knnScore, regionScore, probabilityScore, disagreement));

        // Calculate confidence
        double confidence = calculateConfidence(agreement, disagreement, consensusScore);
        result.setConfidence(confidence);

        return result;
    }

    /**
     * Calculate agreement level between all four methods
     * Returns value between 0.0 (no agreement) and 1.0 (perfect agreement)
     */
    private double calculateAgreement(double rule, double knn, double region, double probability) {
        double[] scores = {rule, knn, region, probability};

        // Calculate standard deviation
        double mean = (rule + knn + region + probability) / 4.0;
        double variance = 0.0;

        for (double score : scores) {
            variance += Math.pow(score - mean, 2);
        }
        variance /= 4.0;
        double stdDev = Math.sqrt(variance);

        // Convert standard deviation to agreement score
        // Higher std dev = lower agreement
        double agreement = Math.exp(-stdDev * 3.0);

        return Math.max(0.0, Math.min(1.0, agreement));
    }

    /**
     * Detect significant disagreement between methods
     * Indicates potential obfuscated attack or ambiguous behavior
     */
    private boolean detectDisagreement(double rule, double knn, double region, double probability) {
        double[] scores = {rule, knn, region, probability};
        double max = Arrays.stream(scores).max().orElse(0.0);
        double min = Arrays.stream(scores).min().orElse(0.0);

        // Disagreement if difference > 0.25 (25%)
        double difference = max - min;
        return difference > 0.25;
    }

    /**
     * Generate textual analysis of consensus
     */
    private String generateAnalysis(double rule, double knn, double region, double probability, boolean disagreement) {
        StringBuilder analysis = new StringBuilder();

        // Identify which methods agree
        List<String> highMethods = new ArrayList<>();
        List<String> lowMethods = new ArrayList<>();

        if (rule > 0.5) highMethods.add("Rule-Based");
        else lowMethods.add("Rule-Based");

        if (knn > 0.5) highMethods.add("KNN");
        else lowMethods.add("KNN");

        if (region > 0.5) highMethods.add("Multi-Region");
        else lowMethods.add("Multi-Region");

        if (probability > 0.5) highMethods.add("Probabilistic");
        else lowMethods.add("Probabilistic");

        // Generate narrative
        if (disagreement) {
            analysis.append("⚠️ DISAGREEMENT DETECTED: ");
            analysis.append(highMethods).append(" indicate HIGH risk, ");
            analysis.append(lowMethods).append(" indicate LOW risk. ");
            analysis.append("This suggests OBFUSCATED behavior - anomaly investigation recommended. ");
        } else {
            if (highMethods.size() >= 3) {
                analysis.append("✓ CONSENSUS HIGH RISK: ");
                analysis.append(highMethods).append(" all indicate fraud. ");
            } else if (lowMethods.size() >= 3) {
                analysis.append("✓ CONSENSUS LOW RISK: ");
                analysis.append(lowMethods).append(" all indicate safety. ");
            } else {
                analysis.append("⚠️ MIXED SIGNALS: Multiple methods indicate varying risk levels. ");
            }
        }

        return analysis.toString();
    }

    /**
     * Calculate confidence in final decision
     * Lower when disagreement present, higher with agreement
     */
    private double calculateConfidence(double agreement, boolean disagreement, double consensusScore) {
        double confidence = agreement;

        // Reduce confidence if disagreement detected
        if (disagreement) {
            confidence *= 0.6; // 40% reduction
        }

        // Boost confidence for extreme scores (very safe or very risky)
        if (consensusScore < 0.2 || consensusScore > 0.8) {
            confidence *= 1.2;
            confidence = Math.min(1.0, confidence);
        }

        return confidence;
    }

    /**
     * ConsensusResult class
     */
    public static class ConsensusResult {
        private double ruleScore;
        private double knnScore;
        private double regionScore;
        private double probabilityScore;
        private double consensusScore;
        private double agreementLevel;
        private boolean disagreement;
        private String analysis;
        private double confidence;

        // Getters and setters
        public double getRuleScore() {
            return ruleScore;
        }

        public void setRuleScore(double ruleScore) {
            this.ruleScore = ruleScore;
        }

        public double getKnnScore() {
            return knnScore;
        }

        public void setKnnScore(double knnScore) {
            this.knnScore = knnScore;
        }

        public double getRegionScore() {
            return regionScore;
        }

        public void setRegionScore(double regionScore) {
            this.regionScore = regionScore;
        }

        public double getProbabilityScore() {
            return probabilityScore;
        }

        public void setProbabilityScore(double probabilityScore) {
            this.probabilityScore = probabilityScore;
        }

        public double getConsensusScore() {
            return consensusScore;
        }

        public void setConsensusScore(double consensusScore) {
            this.consensusScore = Math.max(0.0, Math.min(1.0, consensusScore));
        }

        public double getAgreementLevel() {
            return agreementLevel;
        }

        public void setAgreementLevel(double agreementLevel) {
            this.agreementLevel = Math.max(0.0, Math.min(1.0, agreementLevel));
        }

        public boolean isDisagreement() {
            return disagreement;
        }

        public void setDisagreement(boolean disagreement) {
            this.disagreement = disagreement;
        }

        public String getAnalysis() {
            return analysis;
        }

        public void setAnalysis(String analysis) {
            this.analysis = analysis;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = Math.max(0.0, Math.min(1.0, confidence));
        }

        public String getRiskLevel() {
            if (consensusScore < 0.33) {
                return "SAFE";
            } else if (consensusScore < 0.67) {
                return "SUSPICIOUS";
            } else {
                return "CRITICAL";
            }
        }

        @Override
        public String toString() {
            return "ConsensusResult{" +
                    "consensusScore=" + String.format("%.3f", consensusScore) +
                    ", riskLevel=" + getRiskLevel() +
                    ", agreement=" + String.format("%.1%", agreementLevel) +
                    ", confidence=" + String.format("%.1%", confidence) +
                    ", disagreement=" + disagreement +
                    '}';
        }
    }
}
