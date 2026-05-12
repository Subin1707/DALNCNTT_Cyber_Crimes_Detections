package com.example.servingwebcontent.dto;

import com.example.servingwebcontent.model.RegionType;

import java.util.HashMap;
import java.util.Map;

/**
 * Security Region DTO
 * Represents a behavioral region (Safe/Suspicious/Fraud)
 * Contains the center vector and feature weights for that region
 */
public class SecurityRegionDTO {

    private RegionType type;
    private BehaviorFeatureVector centerVector;
    private Map<String, Double> featureWeights;
    private int sampleCount;

    // Constructor
    public SecurityRegionDTO() {
        this.featureWeights = new HashMap<>();
        this.sampleCount = 0;
    }

    public SecurityRegionDTO(RegionType type, BehaviorFeatureVector centerVector) {
        this.type = type;
        this.centerVector = centerVector;
        this.featureWeights = initializeDefaultWeights();
        this.sampleCount = 0;
    }

    /**
     * Initialize default weights based on region type
     */
    private Map<String, Double> initializeDefaultWeights() {
        Map<String, Double> weights = new HashMap<>();

        switch (type) {
            case SAFE:
                // Low weights for safe region
                weights.put("vpn", 1.0);
                weights.put("blacklist", 0.5);
                weights.put("torNetwork", 0.3);
                weights.put("spamPattern", 0.4);
                weights.put("suspiciousUrl", 0.5);
                weights.put("abnormalAccessTime", 0.6);
                break;

            case SUSPICIOUS:
                // Medium weights
                weights.put("vpn", 6.0);
                weights.put("blacklist", 5.0);
                weights.put("torNetwork", 8.0);
                weights.put("spamPattern", 5.5);
                weights.put("suspiciousUrl", 6.0);
                weights.put("abnormalAccessTime", 5.5);
                break;

            case FRAUD:
                // High weights for fraud region
                weights.put("vpn", 8.0);
                weights.put("blacklist", 10.0);
                weights.put("torNetwork", 12.0);
                weights.put("spamPattern", 8.0);
                weights.put("suspiciousUrl", 9.0);
                weights.put("abnormalAccessTime", 7.0);
                break;
        }

        return weights;
    }

    // Getters and setters
    public RegionType getType() {
        return type;
    }

    public void setType(RegionType type) {
        this.type = type;
    }

    public BehaviorFeatureVector getCenterVector() {
        return centerVector;
    }

    public void setCenterVector(BehaviorFeatureVector centerVector) {
        this.centerVector = centerVector;
    }

    public Map<String, Double> getFeatureWeights() {
        return featureWeights;
    }

    public void setFeatureWeights(Map<String, Double> featureWeights) {
        this.featureWeights = featureWeights;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(int sampleCount) {
        this.sampleCount = Math.max(0, sampleCount);
    }

    public void incrementSampleCount() {
        this.sampleCount++;
    }

    /**
     * Get weight for a specific feature
     */
    public double getFeatureWeight(String featureName) {
        return featureWeights.getOrDefault(featureName, 1.0);
    }

    /**
     * Set weight for a specific feature
     */
    public void setFeatureWeight(String featureName, double weight) {
        featureWeights.put(featureName, Math.max(0.0, weight));
    }

    @Override
    public String toString() {
        return "SecurityRegionDTO{" +
                "type=" + type +
                ", sampleCount=" + sampleCount +
                ", featureWeights=" + featureWeights +
                '}';
    }
}
