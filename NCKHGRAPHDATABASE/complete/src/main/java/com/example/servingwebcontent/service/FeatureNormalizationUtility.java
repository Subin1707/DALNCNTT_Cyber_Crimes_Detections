package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Feature Normalization Utility
 * 
 * Requirement: Normalize data because features have different value ranges
 * Example: requestCount=1000 vs VPN=1 - requestCount would dominate without normalization
 * 
 * Formula: x' = (x - min) / (max - min)
 * Result: All values normalized to [0, 1] range
 * 
 * Vietnamese: Chuẩn hóa dữ liệu để các đặc trưng có ảnh hưởng công bằng
 */
@Component
public class FeatureNormalizationUtility {

    // Min-Max normalization bounds for each feature
    public static class NormalizationBounds {
        public double minIpCount = 0;
        public double maxIpCount = 100;
        
        public double minUrlCount = 0;
        public double maxUrlCount = 200;
        
        public double minEmailCount = 0;
        public double maxEmailCount = 500;
        
        public double minDomainCount = 0;
        public double maxDomainCount = 100;
        
        public double minFailedLoginCount = 0;
        public double maxFailedLoginCount = 50;
        
        public double minRequestFrequency = 0;
        public double maxRequestFrequency = 1000;
    }

    private final NormalizationBounds bounds;

    public FeatureNormalizationUtility() {
        this.bounds = new NormalizationBounds();
    }

    /**
     * Normalize all numeric features in a behavior vector
     * Returns normalized vector with values in [0, 1] range
     * 
     * Example:
     *   Input: ipCount=50 (with bounds 0-100)
     *   Output: 0.5
     */
    public double[] normalizeNumericFeatures(BehaviorFeatureVector vector) {
        if (vector == null) {
            return new double[6]; // Return zeros
        }

        double[] normalized = new double[6];
        
        // Min-Max normalization formula: (x - min) / (max - min)
        normalized[0] = normalizeValue(vector.getIpCount(), bounds.minIpCount, bounds.maxIpCount);
        normalized[1] = normalizeValue(vector.getUrlCount(), bounds.minUrlCount, bounds.maxUrlCount);
        normalized[2] = normalizeValue(vector.getEmailCount(), bounds.minEmailCount, bounds.maxEmailCount);
        normalized[3] = normalizeValue(vector.getDomainCount(), bounds.minDomainCount, bounds.maxDomainCount);
        normalized[4] = normalizeValue(vector.getFailedLoginCount(), bounds.minFailedLoginCount, bounds.maxFailedLoginCount);
        normalized[5] = normalizeValue(vector.getRequestFrequency(), bounds.minRequestFrequency, bounds.maxRequestFrequency);
        
        return normalized;
    }

    /**
     * Normalize a single numeric value using min-max normalization
     * 
     * Formula: x' = (x - min) / (max - min)
     * 
     * Ensures result is in [0, 1] range
     * If min == max, returns 0.0 (no variance)
     */
    private double normalizeValue(double value, double min, double max) {
        if (max == min) {
            return 0.5; // Midpoint if no variance
        }
        
        double normalized = (value - min) / (max - min);
        
        // Clamp to [0, 1]
        return Math.max(0.0, Math.min(1.0, normalized));
    }

    /**
     * Convert boolean features to numeric [0, 1]
     * Used for consistent distance metric calculations
     */
    public double[] normalizeBooleanFeatures(BehaviorFeatureVector vector) {
        if (vector == null) {
            return new double[6]; // Return zeros
        }

        double[] normalized = new double[6];
        
        normalized[0] = vector.isVpn() ? 1.0 : 0.0;
        normalized[1] = vector.isBlacklist() ? 1.0 : 0.0;
        normalized[2] = vector.isSuspiciousUrl() ? 1.0 : 0.0;
        normalized[3] = vector.isTorNetwork() ? 1.0 : 0.0;
        normalized[4] = vector.isSpamPattern() ? 1.0 : 0.0;
        normalized[5] = vector.isAbnormalAccessTime() ? 1.0 : 0.0;
        
        return normalized;
    }

    /**
     * Normalize combined feature vector (both numeric and boolean)
     * Returns a single normalized vector for distance calculations
     */
    public double[] normalizeFullVector(BehaviorFeatureVector vector) {
        double[] numeric = normalizeNumericFeatures(vector);
        double[] boolean_features = normalizeBooleanFeatures(vector);
        
        // Combine both arrays
        double[] combined = new double[numeric.length + boolean_features.length];
        System.arraycopy(numeric, 0, combined, 0, numeric.length);
        System.arraycopy(boolean_features, 0, combined, numeric.length, boolean_features.length);
        
        return combined;
    }

    /**
     * Z-score normalization (alternative to min-max)
     * Formula: x' = (x - mean) / std_dev
     * 
     * Useful when features may have outliers
     */
    public double[] zScoreNormalize(double[] values, double mean, double stdDev) {
        if (stdDev == 0) {
            // No variance - all values are the same
            return new double[values.length]; // Return zeros
        }

        double[] normalized = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            normalized[i] = (values[i] - mean) / stdDev;
        }
        
        return normalized;
    }

    /**
     * Calculate statistics for a list of vectors (for dynamic bounds)
     */
    public static class FeatureStatistics {
        public double meanIpCount;
        public double stdDevIpCount;
        public double meanUrlCount;
        public double stdDevUrlCount;
        // ... other statistics
        
        public double maxIpCount;
        public double maxUrlCount;
        // ... other maximums
    }

    /**
     * Calculate feature statistics from a list of vectors
     * Useful for updating normalization bounds dynamically
     */
    public FeatureStatistics calculateStatistics(List<BehaviorFeatureVector> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            return new FeatureStatistics();
        }

        FeatureStatistics stats = new FeatureStatistics();

        // Calculate IP Count statistics
        double[] ipCounts = vectors.stream()
                .mapToDouble(BehaviorFeatureVector::getIpCount)
                .toArray();
        stats.meanIpCount = calculateMean(ipCounts);
        stats.stdDevIpCount = calculateStdDev(ipCounts, stats.meanIpCount);
        stats.maxIpCount = Arrays.stream(ipCounts).max().orElse(0);

        // Calculate URL Count statistics
        double[] urlCounts = vectors.stream()
                .mapToDouble(BehaviorFeatureVector::getUrlCount)
                .toArray();
        stats.meanUrlCount = calculateMean(urlCounts);
        stats.stdDevUrlCount = calculateStdDev(urlCounts, stats.meanUrlCount);
        stats.maxUrlCount = Arrays.stream(urlCounts).max().orElse(0);

        return stats;
    }

    /**
     * Calculate mean of array
     */
    private double calculateMean(double[] values) {
        if (values.length == 0) return 0;
        return Arrays.stream(values).sum() / values.length;
    }

    /**
     * Calculate standard deviation
     */
    private double calculateStdDev(double[] values, double mean) {
        if (values.length <= 1) return 0;
        
        double variance = 0;
        for (double value : values) {
            variance += Math.pow(value - mean, 2);
        }
        variance /= values.length;
        
        return Math.sqrt(variance);
    }

    /**
     * Update normalization bounds based on new data
     * Called periodically to keep bounds relevant
     */
    public void updateBounds(List<BehaviorFeatureVector> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            return;
        }

        FeatureStatistics stats = calculateStatistics(vectors);

        // Update bounds with 20% margin for safety
        double margin = 0.2;
        bounds.maxIpCount = stats.maxIpCount * (1 + margin);
        bounds.maxUrlCount = stats.maxUrlCount * (1 + margin);
    }

    /**
     * Get current normalization bounds
     */
    public NormalizationBounds getBounds() {
        return bounds;
    }

    /**
     * Set custom normalization bounds
     */
    public void setBounds(NormalizationBounds customBounds) {
        if (customBounds != null) {
            this.bounds.minIpCount = customBounds.minIpCount;
            this.bounds.maxIpCount = customBounds.maxIpCount;
            this.bounds.minUrlCount = customBounds.minUrlCount;
            this.bounds.maxUrlCount = customBounds.maxUrlCount;
            this.bounds.minEmailCount = customBounds.minEmailCount;
            this.bounds.maxEmailCount = customBounds.maxEmailCount;
            this.bounds.minDomainCount = customBounds.minDomainCount;
            this.bounds.maxDomainCount = customBounds.maxDomainCount;
            this.bounds.minFailedLoginCount = customBounds.minFailedLoginCount;
            this.bounds.maxFailedLoginCount = customBounds.maxFailedLoginCount;
            this.bounds.minRequestFrequency = customBounds.minRequestFrequency;
            this.bounds.maxRequestFrequency = customBounds.maxRequestFrequency;
        }
    }
}
