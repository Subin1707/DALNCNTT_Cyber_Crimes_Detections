package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Feature Weights Service
 * 
 * Vietnamese: Dịch vụ trọng số đặc trưng
 * Requirement: Không phải đặc trưng nào cũng quan trọng như nhau
 * 
 * Ví dụ:
 * Đặc trưng     Trọng số
 * VPN          5
 * blacklist    10
 * TOR          12
 * 
 * Ý nghĩa: Một đặc trưng nguy hiểm mạnh có thể kéo node về miền vi phạm
 * 
 * Role: Adjust distance calculations based on feature importance
 * High-risk features have higher weights, pulling scores toward fraud
 */
@Service
public class FeatureWeightsService {

    /**
     * Feature weight definitions
     * Higher weight = more important for fraud detection
     */
    public static class FeatureWeights {
        // Security boolean features - highest priority
        public double torNetworkWeight = 12.0;      // Highest risk
        public double blacklistWeight = 10.0;       // High risk
        public double vpnWeight = 5.0;              // Medium-high risk
        public double spamPatternWeight = 8.0;      // High risk
        public double suspiciousUrlWeight = 7.0;    // Medium-high risk
        public double abnormalAccessTimeWeight = 4.0; // Medium risk

        // Numeric features - moderate priority
        public double ipCountWeight = 3.0;          // Multiple IPs = suspicious
        public double urlCountWeight = 2.5;         // Multiple URLs = suspicious
        public double emailCountWeight = 2.0;       // Multiple emails = moderate
        public double domainCountWeight = 2.0;      // Multiple domains = moderate
        public double failedLoginCountWeight = 6.0; // Failed logins = high risk
        public double requestFrequencyWeight = 3.0; // High frequency = suspicious
    }

    private final FeatureWeights weights;

    public FeatureWeightsService() {
        this.weights = new FeatureWeights();
    }

    /**
     * Calculate weighted distance for a behavior vector
     * Incorporates feature importance into distance calculations
     * 
     * Formula:
     * weighted_distance = Σ(feature_value * feature_weight) / Σ(weights)
     * 
     * Higher weight = larger impact on final distance
     */
    public double calculateWeightedDistance(BehaviorFeatureVector vector, 
                                           BehaviorFeatureVector centerVector) {
        if (vector == null || centerVector == null) {
            return Double.MAX_VALUE;
        }

        double weightedSum = 0.0;
        double totalWeight = 0.0;

        // Numeric features with weights
        weightedSum += Math.abs(vector.getIpCount() - centerVector.getIpCount()) * weights.ipCountWeight;
        totalWeight += weights.ipCountWeight;

        weightedSum += Math.abs(vector.getUrlCount() - centerVector.getUrlCount()) * weights.urlCountWeight;
        totalWeight += weights.urlCountWeight;

        weightedSum += Math.abs(vector.getEmailCount() - centerVector.getEmailCount()) * weights.emailCountWeight;
        totalWeight += weights.emailCountWeight;

        weightedSum += Math.abs(vector.getDomainCount() - centerVector.getDomainCount()) * weights.domainCountWeight;
        totalWeight += weights.domainCountWeight;

        weightedSum += Math.abs(vector.getFailedLoginCount() - centerVector.getFailedLoginCount()) * weights.failedLoginCountWeight;
        totalWeight += weights.failedLoginCountWeight;

        weightedSum += Math.abs(vector.getRequestFrequency() - centerVector.getRequestFrequency()) * weights.requestFrequencyWeight;
        totalWeight += weights.requestFrequencyWeight;

        // Boolean features with weights
        if (vector.isTorNetwork() != centerVector.isTorNetwork()) {
            weightedSum += weights.torNetworkWeight;
        }
        totalWeight += weights.torNetworkWeight;

        if (vector.isBlacklist() != centerVector.isBlacklist()) {
            weightedSum += weights.blacklistWeight;
        }
        totalWeight += weights.blacklistWeight;

        if (vector.isVpn() != centerVector.isVpn()) {
            weightedSum += weights.vpnWeight;
        }
        totalWeight += weights.vpnWeight;

        if (vector.isSpamPattern() != centerVector.isSpamPattern()) {
            weightedSum += weights.spamPatternWeight;
        }
        totalWeight += weights.spamPatternWeight;

        if (vector.isSuspiciousUrl() != centerVector.isSuspiciousUrl()) {
            weightedSum += weights.suspiciousUrlWeight;
        }
        totalWeight += weights.suspiciousUrlWeight;

        if (vector.isAbnormalAccessTime() != centerVector.isAbnormalAccessTime()) {
            weightedSum += weights.abnormalAccessTimeWeight;
        }
        totalWeight += weights.abnormalAccessTimeWeight;

        // Return normalized weighted distance
        return totalWeight > 0 ? weightedSum / totalWeight : weightedSum;
    }

    /**
     * Apply feature weights to distance arrays
     * Useful for weighted Euclidean or Minkowski distances
     */
    public double[] applyWeightsToVector(double[] vector) {
        if (vector == null || vector.length == 0) {
            return vector;
        }

        double[] weighted = new double[vector.length];
        double[] featureWeights = {
            weights.ipCountWeight,
            weights.urlCountWeight,
            weights.emailCountWeight,
            weights.domainCountWeight,
            weights.failedLoginCountWeight,
            weights.requestFrequencyWeight,
            weights.vpnWeight,
            weights.blacklistWeight,
            weights.suspiciousUrlWeight,
            weights.torNetworkWeight,
            weights.spamPatternWeight,
            weights.abnormalAccessTimeWeight
        };

        for (int i = 0; i < Math.min(vector.length, featureWeights.length); i++) {
            weighted[i] = vector[i] * featureWeights[i];
        }

        // Fill remaining with original weights
        for (int i = featureWeights.length; i < vector.length; i++) {
            weighted[i] = vector[i];
        }

        return weighted;
    }

    /**
     * Calculate risk score based on feature presence and weights
     * Higher risk features contribute more to fraud score
     * 
     * Formula: riskScore = Σ(isFeaturePresent * featureWeight)
     */
    public double calculateRiskScoreByWeights(BehaviorFeatureVector vector) {
        if (vector == null) {
            return 0.0;
        }

        double riskScore = 0.0;

        // High-risk boolean features
        if (vector.isTorNetwork()) riskScore += weights.torNetworkWeight;
        if (vector.isBlacklist()) riskScore += weights.blacklistWeight;
        if (vector.isSpamPattern()) riskScore += weights.spamPatternWeight;
        if (vector.isSuspiciousUrl()) riskScore += weights.suspiciousUrlWeight;
        if (vector.isVpn()) riskScore += weights.vpnWeight;
        if (vector.isAbnormalAccessTime()) riskScore += weights.abnormalAccessTimeWeight;

        // Numeric feature thresholds
        if (vector.getFailedLoginCount() > 5) {
            riskScore += weights.failedLoginCountWeight;
        }
        
        if (vector.getIpCount() > 10) {
            riskScore += weights.ipCountWeight;
        }
        
        if (vector.getUrlCount() > 20) {
            riskScore += weights.urlCountWeight;
        }

        return riskScore;
    }

    /**
     * Get feature importance ranking
     * Returns sorted list of features by weight importance
     */
    public List<FeatureImportance> getRankedFeatures() {
        List<FeatureImportance> ranked = new ArrayList<>();

        ranked.add(new FeatureImportance("TOR Network", weights.torNetworkWeight));
        ranked.add(new FeatureImportance("Blacklist", weights.blacklistWeight));
        ranked.add(new FeatureImportance("Spam Pattern", weights.spamPatternWeight));
        ranked.add(new FeatureImportance("Suspicious URL", weights.suspiciousUrlWeight));
        ranked.add(new FeatureImportance("Failed Login Count", weights.failedLoginCountWeight));
        ranked.add(new FeatureImportance("VPN", weights.vpnWeight));
        ranked.add(new FeatureImportance("IP Count", weights.ipCountWeight));
        ranked.add(new FeatureImportance("Request Frequency", weights.requestFrequencyWeight));
        ranked.add(new FeatureImportance("URL Count", weights.urlCountWeight));
        ranked.add(new FeatureImportance("Abnormal Access Time", weights.abnormalAccessTimeWeight));
        ranked.add(new FeatureImportance("Email Count", weights.emailCountWeight));
        ranked.add(new FeatureImportance("Domain Count", weights.domainCountWeight));

        ranked.sort((a, b) -> Double.compare(b.weight, a.weight));
        return ranked;
    }

    /**
     * Feature importance record
     */
    public static class FeatureImportance {
        public final String featureName;
        public final double weight;

        public FeatureImportance(String featureName, double weight) {
            this.featureName = featureName;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return String.format("%s: %.1f", featureName, weight);
        }
    }

    /**
     * Get or set feature weights
     */
    public FeatureWeights getWeights() {
        return weights;
    }

    /**
     * Update specific feature weight
     */
    public void setFeatureWeight(String featureName, double weight) {
        switch (featureName.toLowerCase()) {
            case "tor" -> weights.torNetworkWeight = Math.max(0, weight);
            case "blacklist" -> weights.blacklistWeight = Math.max(0, weight);
            case "vpn" -> weights.vpnWeight = Math.max(0, weight);
            case "spam" -> weights.spamPatternWeight = Math.max(0, weight);
            case "suspiciousurl" -> weights.suspiciousUrlWeight = Math.max(0, weight);
            case "abnormaltime" -> weights.abnormalAccessTimeWeight = Math.max(0, weight);
            case "ipcount" -> weights.ipCountWeight = Math.max(0, weight);
            case "urlcount" -> weights.urlCountWeight = Math.max(0, weight);
            case "failedlogin" -> weights.failedLoginCountWeight = Math.max(0, weight);
            // ... other features
        }
    }
}
