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
 * 
 * Vietnamese: Dịch vụ Phân tích Theo Miền
 * 
 * Phương pháp miền là phương pháp phân tích hành vi bằng cách:
 * - Đặt node vào không gian đặc trưng
 * - Xác định node gần miền hành vi nào nhất
 * 
 * Hệ thống chia thành 3 miền:
 * 1. SAFE (an toàn) - hành vi bình thường
 * 2. SUSPICIOUS (nghi ngờ) - hành vi bất thường nhưng chưa rõ
 * 3. FRAUD (vi phạm) - hành vi nguy hiểm rõ ràng
 * 
 * Sử dụng 3 thuật toán khoảng cách:
 * - Euclidean: cho dữ liệu số (IP count, URL count)
 * - Minkowski: cho dữ liệu nhiều chiều phức tạp
 * - Hamming: cho dữ liệu boolean (VPN, blacklist, TOR)
 * 
 * Tích hợp trọng số đặc trưng để một đặc trưng nguy hiểm
 * có thể kéo node về miền vi phạm.
 */
@Service
public class MultiRegionAnalysisService {

    private final EuclideanDistance euclideanDistance;
    private final MinkowskiDistance minkowskiDistance;
    private final HammingDistance hammingDistance;
    private final FeatureWeightsService featureWeightsService;
    private final FeatureNormalizationUtility normalizationUtility;

    private final Map<RegionType, SecurityRegionDTO> regions;

    public MultiRegionAnalysisService(EuclideanDistance euclideanDistance,
                                      MinkowskiDistance minkowskiDistance,
                                      HammingDistance hammingDistance,
                                      FeatureWeightsService featureWeightsService,
                                      FeatureNormalizationUtility normalizationUtility) {
        this.euclideanDistance = euclideanDistance;
        this.minkowskiDistance = minkowskiDistance;
        this.hammingDistance = hammingDistance;
        this.featureWeightsService = featureWeightsService;
        this.normalizationUtility = normalizationUtility;
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
     * 
     * Quy trình:
     * 1. Tính khoảng cách từ node tới từng miền (3 thuật toán)
     * 2. Chuyển khoảng cách thành xác suất
     * 3. Chuẩn hóa xác suất
     * 4. Áp dụng trọng số đặc trưng nguy hiểm
     * 5. Phát hiện anomaly
     * 
     * @param node Behavior feature vector to analyze
     * @return RegionAnalysisResult containing distances and probabilities
     */
    public RegionAnalysisResult analyzeAgainstRegions(BehaviorFeatureVector node) {
        if (node == null) {
            return new RegionAnalysisResult();
        }

        RegionAnalysisResult result = new RegionAnalysisResult();

        // Step 1: Calculate distances using all three metrics for each region
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

        // Step 2: Normalize probabilities to sum to 1.0
        result.normalizeProbabilities();

        // Step 3: Apply weighted feature penalties for dangerous features
        applyWeightedFeaturePenalties(node, result);

        // Step 4: Determine primary region
        RegionType primaryRegion = result.getPrimaryRegion();
        result.setPrimaryRegion(primaryRegion);

        // Step 5: Detect anomalies (large probability divergence)
        double maxProb = Collections.max(result.getRegionProbabilities().values());
        double minProb = Collections.min(result.getRegionProbabilities().values());
        double divergence = maxProb - minProb;
        result.setAnomalyScore(divergence);

        return result;
    }

    /**
     * Apply weighted feature penalties to adjust region distances
     * 
     * Vietnamese: Áp dụng trọng số đặc trưng nguy hiểm
     * 
     * Ý tưởng chính:
     * - Một đặc trưng nguy hiểm mạnh có thể kéo node về miền vi phạm
     * - Ví dụ: 9 đặc trưng an toàn + 1 blacklist => node vẫn gần miền vi phạm
     * 
     * Cơ chế:
     * - Nếu node có đặc trưng nguy hiểm
     * - Giảm khoảng cách tới FRAUD region
     * - Tăng khoảng cách tới SAFE region
     * 
     * @param node Node to analyze
     * @param result Region analysis result to modify
     */
    private void applyWeightedFeaturePenalties(BehaviorFeatureVector node, RegionAnalysisResult result) {
        if (node == null || result == null) {
            return;
        }

        double fraudDistance = result.getRegionDistance(RegionType.FRAUD);
        double safeDistance = result.getRegionDistance(RegionType.SAFE);
        double suspiciousDistance = result.getRegionDistance(RegionType.SUSPICIOUS);
        
        List<String> appliedPenalties = new ArrayList<>();

        // ============ CRITICAL FEATURES (Highest Impact) ============
        
        // 1. Blacklist (weight = 10.0)
        if (node.isBlacklist()) {
            fraudDistance *= 0.3;  // Severe penalty: reduce by 70%
            safeDistance *= 1.5;   // Increase distance from SAFE
            appliedPenalties.add("⚠️ BLACKLIST (weight=10): Severe penalty - significantly closer to FRAUD");
        }

        // 2. TOR Network (weight = 12.0)
        if (node.isTorNetwork()) {
            fraudDistance *= 0.4;  // Severe penalty: reduce by 60%
            safeDistance *= 2.0;   // Double distance from SAFE
            appliedPenalties.add("⚠️ TOR NETWORK (weight=12): Severe penalty - strongly suggests FRAUD");
        }

        // ============ HIGH-RISK FEATURES ============
        
        // 3. Spam Pattern (weight = 8.0)
        if (node.isSpamPattern()) {
            fraudDistance *= 0.55;
            suspiciousDistance *= 0.8;
            appliedPenalties.add("⚠️ SPAM PATTERN (weight=8): High-risk feature detected");
        }

        // 4. Suspicious URL (weight = 7.0)
        if (node.isSuspiciousUrl()) {
            fraudDistance *= 0.6;
            suspiciousDistance *= 0.85;
            appliedPenalties.add("⚠️ SUSPICIOUS URL (weight=7): Malicious URL detected");
        }

        // 5. Failed Login Count (weight = 6.0)
        if (node.getFailedLoginCount() > 5) {
            fraudDistance *= 0.65;
            suspiciousDistance *= 0.9;
            appliedPenalties.add(String.format("⚠️ FAILED LOGINS (weight=6): %d failed attempts", 
                node.getFailedLoginCount()));
        }

        // ============ OBFUSCATION ATTEMPTS ============
        
        // 6. VPN + Blacklist combination (indicates hiding)
        if (node.isVpn() && node.isBlacklist()) {
            fraudDistance *= 0.25;  // Ultra severe
            safeDistance *= 3.0;
            appliedPenalties.add("⚠️ VPN+BLACKLIST COMBO: Obfuscation attempt detected - extreme risk");
        }

        // 7. VPN alone (weight = 5.0)
        else if (node.isVpn()) {
            fraudDistance *= 0.7;
            appliedPenalties.add("ℹ️ VPN (weight=5): Privacy tool detected - moderate risk");
        }

        // ============ ABNORMAL BEHAVIOR ============
        
        // 8. Abnormal Access Time (weight = 4.0)
        if (node.isAbnormalAccessTime()) {
            fraudDistance *= 0.75;
            suspiciousDistance *= 0.95;
            appliedPenalties.add("ℹ️ ABNORMAL TIME (weight=4): Access outside normal hours");
        }

        // ============ NUMERIC THRESHOLD PENALTIES ============
        
        // 9. High IP Count (weight = 3.0)
        if (node.getIpCount() > 10) {
            fraudDistance *= 0.8;
            suspiciousDistance *= 0.9;
            appliedPenalties.add(String.format("ℹ️ HIGH IP COUNT (weight=3): %d unique IPs", node.getIpCount()));
        }

        // 10. High URL Count (weight = 2.5)
        if (node.getUrlCount() > 20) {
            fraudDistance *= 0.82;
            suspiciousDistance *= 0.92;
            appliedPenalties.add(String.format("ℹ️ HIGH URL COUNT (weight=2.5): %d unique URLs", node.getUrlCount()));
        }

        // 11. High Request Frequency (weight = 3.0)
        if (node.getRequestFrequency() > 50) {
            fraudDistance *= 0.85;
            suspiciousDistance *= 0.95;
            appliedPenalties.add(String.format("ℹ️ HIGH FREQUENCY (weight=3): %.1f req/min", node.getRequestFrequency()));
        }

        // ============ CUMULATIVE PENALTY ============
        // If multiple dangerous features detected, apply additional penalty
        int dangerousFeatureCount = countDangerousFeatures(node);
        if (dangerousFeatureCount >= 3) {
            fraudDistance *= 0.9; // Additional 10% reduction
            appliedPenalties.add(String.format("🚨 CUMULATIVE: %d dangerous features detected - additional penalty", 
                dangerousFeatureCount));
        }

        // Update result with adjusted distances
        result.addRegionDistance(RegionType.FRAUD, fraudDistance);
        result.addRegionDistance(RegionType.SAFE, safeDistance);
        result.addRegionDistance(RegionType.SUSPICIOUS, suspiciousDistance);

        // Recalculate probabilities with new distances
        double fraudProb = Math.exp(-fraudDistance * 2.5);
        double safeProb = Math.exp(-safeDistance * 2.5);
        double suspiciousProb = Math.exp(-suspiciousDistance * 2.5);

        result.addRegionProbability(RegionType.FRAUD, fraudProb);
        result.addRegionProbability(RegionType.SAFE, safeProb);
        result.addRegionProbability(RegionType.SUSPICIOUS, suspiciousProb);

        // Renormalize probabilities
        result.normalizeProbabilities();

        // Store penalty details
        for (String penalty : appliedPenalties) {
            result.addDetail(penalty);
        }
    }

    /**
     * Count dangerous features in a behavior vector
     * Helper method for cumulative penalty calculation
     */
    private int countDangerousFeatures(BehaviorFeatureVector node) {
        int count = 0;
        
        if (node.isBlacklist()) count++;
        if (node.isTorNetwork()) count++;
        if (node.isVpn()) count++;
        if (node.isSpamPattern()) count++;
        if (node.isSuspiciousUrl()) count++;
        if (node.isAbnormalAccessTime()) count++;
        if (node.getFailedLoginCount() > 5) count++;
        if (node.getIpCount() > 10) count++;
        if (node.getUrlCount() > 20) count++;
        if (node.getRequestFrequency() > 50) count++;

        return count;
    }

    /**
     * Generate detailed region analysis report
     * 
     * Vietnamese: Tạo báo cáo phân tích miền chi tiết
     * 
     * Báo cáo bao gồm:
     * 1. Khoảng cách tới từng miền
     * 2. Xác suất từng miền
     * 3. Các đặc trưng đã áp dụng penalty
     * 4. Kết luận miền chính
     */
    public String generateDetailedReport(BehaviorFeatureVector node, RegionAnalysisResult result) {
        if (result == null) {
            return "No region analysis result";
        }

        StringBuilder report = new StringBuilder();
        report.append("=== MULTI-REGION ANALYSIS REPORT ===\n\n");

        // Section 1: Region Distances
        report.append("DISTANCES FROM NODE TO REGIONS:\n");
        report.append(String.format("  Distance to SAFE:       %.2f\n", result.getRegionDistance(RegionType.SAFE)));
        report.append(String.format("  Distance to SUSPICIOUS: %.2f\n", result.getRegionDistance(RegionType.SUSPICIOUS)));
        report.append(String.format("  Distance to FRAUD:      %.2f\n", result.getRegionDistance(RegionType.FRAUD)));

        // Section 2: Region Probabilities
        report.append("\nPROBABILITY OF MEMBERSHIP:\n");
        report.append(String.format("  P(SAFE):        %.2f%%\n", result.getRegionProbability(RegionType.SAFE) * 100));
        report.append(String.format("  P(SUSPICIOUS):  %.2f%%\n", result.getRegionProbability(RegionType.SUSPICIOUS) * 100));
        report.append(String.format("  P(FRAUD):       %.2f%%\n", result.getRegionProbability(RegionType.FRAUD) * 100));

        // Section 3: Primary Region
        report.append("\nPRIMARY REGION:\n");
        report.append(String.format("  Region: %s\n", result.getPrimaryRegion()));
        report.append(String.format("  Confidence: %.2f%%\n", 
            result.getRegionProbability(result.getPrimaryRegion()) * 100));

        // Section 4: Metric Distances
        report.append("\nDISTANCE METRICS BREAKDOWN:\n");
        Map<String, Map<String, Double>> metricDistances = result.getMetricDistances();
        for (RegionType region : RegionType.values()) {
            report.append(String.format("  %s:\n", region.name()));
            Map<String, Double> metrics = metricDistances.get(region.name());
            if (metrics != null) {
                metrics.forEach((metric, distance) ->
                    report.append(String.format("    - %s: %.2f\n", metric, distance))
                );
            }
        }

        // Section 5: Applied Penalties
        report.append("\nAPPLIED FEATURE PENALTIES:\n");
        List<String> penalties = result.getDetails();
        if (penalties.isEmpty()) {
            report.append("  No penalties applied\n");
        } else {
            for (String penalty : penalties) {
                report.append(String.format("  %s\n", penalty));
            }
        }

        // Section 6: Anomaly Score
        report.append("\nANOMALY SCORE:\n");
        report.append(String.format("  Score: %.4f\n", result.getAnomalyScore()));
        if (result.getAnomalyScore() > 0.5) {
            report.append("  Assessment: High - Node behavior is highly ambiguous\n");
        } else if (result.getAnomalyScore() > 0.3) {
            report.append("  Assessment: Medium - Node behavior shows some ambiguity\n");
        } else {
            report.append("  Assessment: Low - Node behavior is clear\n");
        }

        return report.toString();
    }

    /**
     * Legacy method - deprecated in favor of applyWeightedFeaturePenalties
     * Kept for backward compatibility
     */
    @Deprecated
    public void applyFeaturePenalties(BehaviorFeatureVector node, RegionAnalysisResult result) {
        applyWeightedFeaturePenalties(node, result);
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

        public Map<String, Map<String, Double>> getMetricDistances() {
            return new HashMap<>(metricDistances);
        }

        public void addMetricDistance(String region, String metric, double distance) {
            metricDistances.computeIfAbsent(region, k -> new HashMap<>())
                    .put(metric, distance);
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
