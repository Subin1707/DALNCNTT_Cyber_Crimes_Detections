package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import com.example.servingwebcontent.dto.SecurityRegionDTO;
import com.example.servingwebcontent.model.RegionType;
import com.example.servingwebcontent.service.distance.DistanceMetric;
import com.example.servingwebcontent.service.distance.EuclideanDistance;
import com.example.servingwebcontent.service.distance.HammingDistance;
import com.example.servingwebcontent.service.distance.MinkowskiDistance;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Multi-Region Analysis Service
 * Analyzes user behavior across three security regions (Safe, Suspicious, Fraud)
 * Uses multiple distance metrics to calculate membership probability
 */
@Service
public class MultiRegionAnalysisService {

    private final EuclideanDistance euclideanDistance;
    private final MinkowskiDistance minkowskiDistance;
    private final HammingDistance hammingDistance;

    private final Map<RegionType, SecurityRegionDTO> regions;

    public MultiRegionAnalysisService(EuclideanDistance euclideanDistance,
                                      MinkowskiDistance minkowskiDistance,
                                      HammingDistance hammingDistance) {
        this.euclideanDistance = euclideanDistance;
        this.minkowskiDistance = minkowskiDistance;
        this.hammingDistance = hammingDistance;
        this.regions = initializeRegions();
    }

    /**
     * Initialize default region vectors
     */
    private Map<RegionType, SecurityRegionDTO> initializeRegions() {
        Map<RegionType, SecurityRegionDTO> map = new HashMap<>();

        // SAFE region: Low-risk behavior
        BehaviorFeatureVector safeVector = new BehaviorFeatureVector(
                1, 2, 5, 3, 0, 1.0,
                false, false, false, false, false, false
        );
        map.put(RegionType.SAFE, new SecurityRegionDTO(RegionType.SAFE, safeVector));

        // SUSPICIOUS region: Medium-risk behavior
        BehaviorFeatureVector suspiciousVector = new BehaviorFeatureVector(
                5, 8, 10, 6, 3, 3.5,
                true, false, true, false, true, true
        );
        map.put(RegionType.SUSPICIOUS, new SecurityRegionDTO(RegionType.SUSPICIOUS, suspiciousVector));

        // FRAUD region: High-risk behavior
        BehaviorFeatureVector fraudVector = new BehaviorFeatureVector(
                15, 20, 25, 18, 8, 10.0,
                true, true, true, true, true, true
        );
        map.put(RegionType.FRAUD, new SecurityRegionDTO(RegionType.FRAUD, fraudVector));

        return map;
    }

    /**
     * Analyze node against all three regions
     * @param node Behavior feature vector to analyze
     * @return RegionAnalysisResult containing distances and probabilities
     */
    public RegionAnalysisResult analyzeAgainstRegions(BehaviorFeatureVector node) {
        if (node == null) {
            return new RegionAnalysisResult();
        }

        RegionAnalysisResult result = new RegionAnalysisResult();

        // Calculate distances using all three metrics
        for (RegionType regionType : RegionType.values()) {
            SecurityRegionDTO region = regions.get(regionType);

            // Calculate distance using each metric
            double euclideanDist = euclideanDistance.calculate(node, region.getCenterVector());
            double minkowskiDist = minkowskiDistance.calculate(node, region.getCenterVector());
            double hammingDist = hammingDistance.calculate(node, region.getCenterVector());

            // Average the three distances
            double avgDistance = (euclideanDist + minkowskiDist + hammingDist) / 3.0;

            // Convert distance to probability (closer = higher probability)
            // Using inverse exponential: prob = e^(-distance*k) where k controls sensitivity
            double probability = Math.exp(-avgDistance * 2.5);

            result.addRegionDistance(regionType, avgDistance);
            result.addRegionProbability(regionType, probability);
            result.addMetricDistance(regionType, "euclidean", euclideanDist);
            result.addMetricDistance(regionType, "minkowski", minkowskiDist);
            result.addMetricDistance(regionType, "hamming", hammingDist);
        }

        // Normalize probabilities to sum to 1.0
        result.normalizeProbabilities();

        // Determine primary region
        RegionType primaryRegion = result.getPrimaryRegion();
        result.setPrimaryRegion(primaryRegion);

        // Detect anomalies (large probability divergence)
        double maxProb = Collections.max(result.getRegionProbabilities().values());
        double minProb = Collections.min(result.getRegionProbabilities().values());
        double divergence = maxProb - minProb;
        result.setAnomalyScore(divergence);

        return result;
    }

    /**
     * Apply weighted feature penalties
     * If node has dangerous features, reduce distance to fraud region
     */
    public void applyFeaturePenalties(BehaviorFeatureVector node, RegionAnalysisResult result) {
        if (node == null || result == null) {
            return;
        }

        SecurityRegionDTO fraudRegion = regions.get(RegionType.FRAUD);
        double fraudDistance = result.getRegionDistance(RegionType.FRAUD);

        // Apply penalties for dangerous features
        if (node.isBlacklist()) {
            fraudDistance *= 0.3; // Severe penalty
            result.addDetail("Blacklist detected - significantly closer to fraud region");
        }

        if (node.isTorNetwork()) {
            fraudDistance *= 0.4;
            result.addDetail("TOR network detected - significantly closer to fraud region");
        }

        if (node.isVpn() && node.isBlacklist()) {
            fraudDistance *= 0.5;
            result.addDetail("VPN + Blacklist combination - indicates obfuscation attempt");
        }

        if (node.isSpamPattern()) {
            fraudDistance *= 0.6;
            result.addDetail("Spam pattern detected - increased fraud probability");
        }

        // Update result
        result.addRegionDistance(RegionType.FRAUD, fraudDistance);
        double fraudProb = Math.exp(-fraudDistance * 2.5);
        result.addRegionProbability(RegionType.FRAUD, fraudProb);
        result.normalizeProbabilities();
    }

    /**
     * Get or update region
     */
    public SecurityRegionDTO getRegion(RegionType type) {
        return regions.get(type);
    }

    public void updateRegion(RegionType type, SecurityRegionDTO region) {
        regions.put(type, region);
    }

    /**
     * Region Analysis Result
     */
    public static class RegionAnalysisResult {
        private final Map<RegionType, Double> regionDistances = new HashMap<>();
        private final Map<RegionType, Double> regionProbabilities = new HashMap<>();
        private final Map<String, Map<String, Double>> metricDistances = new HashMap<>();
        private RegionType primaryRegion;
        private double anomalyScore;
        private final List<String> details = new ArrayList<>();

        public void addRegionDistance(RegionType type, double distance) {
            regionDistances.put(type, distance);
        }

        public void addRegionProbability(RegionType type, double probability) {
            regionProbabilities.put(type, probability);
        }

        public void addMetricDistance(RegionType region, String metric, double distance) {
            metricDistances.computeIfAbsent(region.name(), k -> new HashMap<>())
                    .put(metric, distance);
        }

        public void normalizeProbabilities() {
            double sum = regionProbabilities.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();
            if (sum > 0) {
                regionProbabilities.replaceAll((k, v) -> v / sum);
            }
        }

        public void setPrimaryRegion(RegionType type) {
            this.primaryRegion = type;
        }

        public RegionType getPrimaryRegion() {
            if (primaryRegion != null) {
                return primaryRegion;
            }
            // Determine from probabilities
            return regionProbabilities.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(RegionType.SAFE);
        }

        public double getRegionDistance(RegionType type) {
            return regionDistances.getOrDefault(type, Double.MAX_VALUE);
        }

        public double getRegionProbability(RegionType type) {
            return regionProbabilities.getOrDefault(type, 0.0);
        }

        public Map<RegionType, Double> getRegionDistances() {
            return new HashMap<>(regionDistances);
        }

        public Map<RegionType, Double> getRegionProbabilities() {
            return new HashMap<>(regionProbabilities);
        }

        public void setAnomalyScore(double score) {
            this.anomalyScore = score;
        }

        public double getAnomalyScore() {
            return anomalyScore;
        }

        public void addDetail(String detail) {
            this.details.add(detail);
        }

        public List<String> getDetails() {
            return new ArrayList<>(details);
        }

        @Override
        public String toString() {
            return "RegionAnalysisResult{" +
                    "primary=" + primaryRegion +
                    ", anomalyScore=" + String.format("%.2f", anomalyScore) +
                    ", probabilities=" + regionProbabilities +
                    '}';
        }
    }
}
