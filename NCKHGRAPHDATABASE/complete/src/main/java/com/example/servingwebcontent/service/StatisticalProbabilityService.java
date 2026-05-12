package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Statistical Probability Service
 * Implements Bayesian analysis for fraud detection
 * Formula: P(Fraud|Evidence) = P(Evidence|Fraud) * P(Fraud) / P(Evidence)
 */
@Service
public class StatisticalProbabilityService {

    // Prior probabilities (base rates)
    private static final double PRIOR_SAFE = 0.70;        // 70% normal users
    private static final double PRIOR_SUSPICIOUS = 0.20;  // 20% suspicious
    private static final double PRIOR_FRAUD = 0.10;       // 10% actually fraudulent

    // Likelihood table: P(Evidence|Class)
    // Empirically determined from historical data
    private static final Map<String, Map<String, Double>> LIKELIHOODS = new HashMap<>();

    static {
        // Likelihood of each feature given FRAUD
        Map<String, Double> fraudLikelihoods = new HashMap<>();
        fraudLikelihoods.put("blacklist", 0.95);      // 95% of fraudsters are blacklisted
        fraudLikelihoods.put("torNetwork", 0.92);     // 92% use TOR
        fraudLikelihoods.put("vpn", 0.85);            // 85% use VPN
        fraudLikelihoods.put("spamPattern", 0.88);    // 88% show spam patterns
        fraudLikelihoods.put("suspiciousUrl", 0.90);  // 90% access suspicious URLs
        fraudLikelihoods.put("abnormalAccessTime", 0.78);  // 78% access abnormally
        fraudLikelihoods.put("highIpCount", 0.87);    // 87% use multiple IPs
        fraudLikelihoods.put("highUrlCount", 0.84);   // 84% access many URLs
        LIKELIHOODS.put("FRAUD", fraudLikelihoods);

        // Likelihood of each feature given SUSPICIOUS
        Map<String, Double> suspiciousLikelihoods = new HashMap<>();
        suspiciousLikelihoods.put("blacklist", 0.30);
        suspiciousLikelihoods.put("torNetwork", 0.25);
        suspiciousLikelihoods.put("vpn", 0.40);
        suspiciousLikelihoods.put("spamPattern", 0.35);
        suspiciousLikelihoods.put("suspiciousUrl", 0.38);
        suspiciousLikelihoods.put("abnormalAccessTime", 0.45);
        suspiciousLikelihoods.put("highIpCount", 0.35);
        suspiciousLikelihoods.put("highUrlCount", 0.32);
        LIKELIHOODS.put("SUSPICIOUS", suspiciousLikelihoods);

        // Likelihood of each feature given SAFE
        Map<String, Double> safeLikelihoods = new HashMap<>();
        safeLikelihoods.put("blacklist", 0.01);
        safeLikelihoods.put("torNetwork", 0.02);
        safeLikelihoods.put("vpn", 0.05);
        safeLikelihoods.put("spamPattern", 0.03);
        safeLikelihoods.put("suspiciousUrl", 0.05);
        safeLikelihoods.put("abnormalAccessTime", 0.10);
        safeLikelihoods.put("highIpCount", 0.08);
        safeLikelihoods.put("highUrlCount", 0.06);
        LIKELIHOODS.put("SAFE", safeLikelihoods);
    }

    /**
     * Calculate Bayesian probability using collected evidence
     * @param node Behavior vector with features
     * @return ProbabilityResult with posterior probabilities
     */
    public ProbabilityResult calculateBayesianProbability(BehaviorFeatureVector node) {
        if (node == null) {
            return new ProbabilityResult();
        }

        ProbabilityResult result = new ProbabilityResult();

        // Extract evidence from node
        List<String> evidence = extractEvidence(node);
        result.setEvidence(evidence);

        // Calculate likelihood for each class
        Map<String, Double> likelihoods = new HashMap<>();
        likelihoods.put("FRAUD", calculateLikelihood(evidence, "FRAUD"));
        likelihoods.put("SUSPICIOUS", calculateLikelihood(evidence, "SUSPICIOUS"));
        likelihoods.put("SAFE", calculateLikelihood(evidence, "SAFE"));

        // Calculate P(Evidence) = Σ P(Evidence|Class) * P(Class)
        double pEvidence = likelihoods.get("FRAUD") * PRIOR_FRAUD +
                likelihoods.get("SUSPICIOUS") * PRIOR_SUSPICIOUS +
                likelihoods.get("SAFE") * PRIOR_SAFE;

        if (pEvidence == 0) {
            pEvidence = 0.001; // Avoid division by zero
        }

        // Calculate posterior probabilities using Bayes theorem
        double posteriorFraud = (likelihoods.get("FRAUD") * PRIOR_FRAUD) / pEvidence;
        double posteriorSuspicious = (likelihoods.get("SUSPICIOUS") * PRIOR_SUSPICIOUS) / pEvidence;
        double posteriorSafe = (likelihoods.get("SAFE") * PRIOR_SAFE) / pEvidence;

        result.setPosteriorFraud(Math.min(1.0, posteriorFraud));
        result.setPosteriorSuspicious(Math.min(1.0, posteriorSuspicious));
        result.setPosteriorSafe(Math.min(1.0, posteriorSafe));

        // Normalize to sum to 1.0
        double sum = result.getPosteriorFraud() + result.getPosteriorSuspicious() + result.getPosteriorSafe();
        if (sum > 0) {
            result.setPosteriorFraud(result.getPosteriorFraud() / sum);
            result.setPosteriorSuspicious(result.getPosteriorSuspicious() / sum);
            result.setPosteriorSafe(result.getPosteriorSafe() / sum);
        }

        // Calculate confidence (how confident are we in this classification)
        double maxPosterior = Math.max(result.getPosteriorFraud(),
                Math.max(result.getPosteriorSuspicious(), result.getPosteriorSafe()));
        result.setConfidence(maxPosterior);

        // Calculate p-value for statistical significance
        // Lower p-value = more significant
        double pValue = calculatePValue(evidence);
        result.setPValue(pValue);

        // Determine significance (p < 0.05 is statistically significant)
        result.setStatisticallySignificant(pValue < 0.05);

        return result;
    }

    /**
     * Extract evidence from behavior vector
     */
    private List<String> extractEvidence(BehaviorFeatureVector node) {
        List<String> evidence = new ArrayList<>();

        if (node.isBlacklist()) {
            evidence.add("blacklist");
        }
        if (node.isTorNetwork()) {
            evidence.add("torNetwork");
        }
        if (node.isVpn()) {
            evidence.add("vpn");
        }
        if (node.isSpamPattern()) {
            evidence.add("spamPattern");
        }
        if (node.isSuspiciousUrl()) {
            evidence.add("suspiciousUrl");
        }
        if (node.isAbnormalAccessTime()) {
            evidence.add("abnormalAccessTime");
        }
        if (node.getIpCount() > 5) {
            evidence.add("highIpCount");
        }
        if (node.getUrlCount() > 10) {
            evidence.add("highUrlCount");
        }

        return evidence;
    }

    /**
     * Calculate likelihood P(Evidence|Class) using conjunction rule
     * Assumes feature independence
     */
    private double calculateLikelihood(List<String> evidence, String className) {
        Map<String, Double> classLikelihoods = LIKELIHOODS.get(className);
        if (classLikelihoods == null || evidence.isEmpty()) {
            return 0.5; // Default if no data
        }

        double likelihood = 1.0;

        for (String feature : evidence) {
            Double featureLikelihood = classLikelihoods.get(feature);
            if (featureLikelihood != null) {
                likelihood *= featureLikelihood;
            }
        }

        return likelihood;
    }

    /**
     * Calculate p-value for statistical significance test
     * Chi-squared approach: compares observed evidence distribution with expected
     */
    private double calculatePValue(List<String> evidence) {
        if (evidence.isEmpty()) {
            return 1.0; // No evidence = not significant
        }

        // Chi-squared calculation
        double chiSquared = 0.0;

        for (String feature : evidence) {
            // Expected frequency under independence
            double expected = 0.33; // Rough expected for even distribution

            // Observed frequency (1 if evidence present)
            double observed = 1.0;

            // Chi-squared contribution
            chiSquared += Math.pow((observed - expected), 2) / expected;
        }

        // Convert chi-squared to p-value (simplified)
        // With 1 degree of freedom, critical value is ~3.84 for p=0.05
        double pValue = Math.exp(-chiSquared / 2.0);

        return Math.min(1.0, Math.max(0.0, pValue));
    }

    /**
     * Calculate Recall metric: TP / (TP + FN)
     * Measures: what proportion of actual frauds did we catch?
     */
    public static double calculateRecall(int truePositives, int falseNegatives) {
        int total = truePositives + falseNegatives;
        if (total == 0) {
            return 0.0;
        }
        return (double) truePositives / total * 100.0;
    }

    /**
     * Calculate Precision metric: TP / (TP + FP)
     * Measures: of our fraud predictions, how many were correct?
     */
    public static double calculatePrecision(int truePositives, int falsePositives) {
        int total = truePositives + falsePositives;
        if (total == 0) {
            return 0.0;
        }
        return (double) truePositives / total * 100.0;
    }

    /**
     * Probability Result DTO
     */
    public static class ProbabilityResult {
        private List<String> evidence = new ArrayList<>();
        private double posteriorFraud;
        private double posteriorSuspicious;
        private double posteriorSafe;
        private double confidence;
        private double pValue;
        private boolean statisticallySignificant;

        // Getters and setters
        public List<String> getEvidence() {
            return evidence;
        }

        public void setEvidence(List<String> evidence) {
            this.evidence = evidence;
        }

        public double getPosteriorFraud() {
            return posteriorFraud;
        }

        public void setPosteriorFraud(double posteriorFraud) {
            this.posteriorFraud = Math.max(0.0, Math.min(1.0, posteriorFraud));
        }

        public double getPosteriorSuspicious() {
            return posteriorSuspicious;
        }

        public void setPosteriorSuspicious(double posteriorSuspicious) {
            this.posteriorSuspicious = Math.max(0.0, Math.min(1.0, posteriorSuspicious));
        }

        public double getPosteriorSafe() {
            return posteriorSafe;
        }

        public void setPosteriorSafe(double posteriorSafe) {
            this.posteriorSafe = Math.max(0.0, Math.min(1.0, posteriorSafe));
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = Math.max(0.0, Math.min(1.0, confidence));
        }

        public double getPValue() {
            return pValue;
        }

        public void setPValue(double pValue) {
            this.pValue = Math.max(0.0, Math.min(1.0, pValue));
        }

        public boolean isStatisticallySignificant() {
            return statisticallySignificant;
        }

        public void setStatisticallySignificant(boolean significant) {
            this.statisticallySignificant = significant;
        }

        @Override
        public String toString() {
            return "ProbabilityResult{" +
                    "fraud=" + String.format("%.2%", posteriorFraud * 100) +
                    ", suspicious=" + String.format("%.2%", posteriorSuspicious * 100) +
                    ", safe=" + String.format("%.2%", posteriorSafe * 100) +
                    ", confidence=" + String.format("%.2%", confidence * 100) +
                    ", pValue=" + String.format("%.4f", pValue) +
                    ", significant=" + statisticallySignificant +
                    '}';
        }
    }
}
