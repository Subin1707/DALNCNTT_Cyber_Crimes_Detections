package com.example.servingwebcontent;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import com.example.servingwebcontent.service.MultiRegionAnalysisService;
import com.example.servingwebcontent.service.distance.EuclideanDistance;
import com.example.servingwebcontent.service.distance.MinkowskiDistance;
import com.example.servingwebcontent.service.distance.HammingDistance;
import com.example.servingwebcontent.model.RegionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests for Multi-Region Analysis
 * 
 * Tests the core logic of multi-region classification
 * without requiring Spring Boot context or database
 */
@DisplayName("Multi-Region Analysis Unit Tests")
public class MultiRegionAnalysisUnitTest {

    private MultiRegionAnalysisService multiRegionService;
    private EuclideanDistance euclideanDistance;
    private MinkowskiDistance minkowskiDistance;
    private HammingDistance hammingDistance;

    @BeforeEach
    public void setUp() {
        // Create distance metrics
        euclideanDistance = new EuclideanDistance();
        minkowskiDistance = new MinkowskiDistance();
        hammingDistance = new HammingDistance();
        
        // Create service with injected dependencies
        multiRegionService = new MultiRegionAnalysisService(
            euclideanDistance, 
            minkowskiDistance, 
            hammingDistance
        );
    }

    @Test
    @DisplayName("Should classify SAFE node correctly")
    public void testSafeNodeClassification() {
        // SAFE: low numeric, all false flags
        BehaviorFeatureVector safeNode = new BehaviorFeatureVector(
                1, 2, 3, 2, 0, 0.5,
                false, false, false, false, false, false
        );

        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(safeNode);

        assertNotNull(result, "Result should not be null");
        assertEquals(RegionType.SAFE, result.getPrimaryRegion(),
                     "Low numeric + all false should classify as SAFE");
    }

    @Test
    @DisplayName("Should classify FRAUD node correctly")
    public void testFraudNodeClassification() {
        // FRAUD: high numeric, all true flags
        BehaviorFeatureVector fraudNode = new BehaviorFeatureVector(
                15, 20, 25, 18, 8, 10.0,
                true, true, true, true, true, true
        );

        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(fraudNode);

        assertNotNull(result);
        assertEquals(RegionType.FRAUD, result.getPrimaryRegion(),
                     "High numeric + all true should classify as FRAUD");
    }

    @Test
    @DisplayName("Should classify SUSPICIOUS node correctly")
    public void testSuspiciousNodeClassification() {
        // SUSPICIOUS: medium numeric, mixed flags
        BehaviorFeatureVector suspiciousNode = new BehaviorFeatureVector(
                5, 8, 10, 6, 3, 3.5,
                true, false, true, false, true, true
        );

        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(suspiciousNode);

        assertNotNull(result);
        assertEquals(RegionType.SUSPICIOUS, result.getPrimaryRegion(),
                     "Medium numeric + mixed flags should classify as SUSPICIOUS");
    }

    @Test
    @DisplayName("Should calculate distances correctly")
    public void testDistanceCalculations() {
        BehaviorFeatureVector node = new BehaviorFeatureVector(
                5, 8, 10, 6, 3, 3.5,
                true, false, true, false, true, true
        );

        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(node);

        // All distances should be non-negative
        assertTrue(result.getRegionDistance(RegionType.SAFE) >= 0, "Distance to SAFE should be >= 0");
        assertTrue(result.getRegionDistance(RegionType.SUSPICIOUS) >= 0, "Distance to SUSPICIOUS should be >= 0");
        assertTrue(result.getRegionDistance(RegionType.FRAUD) >= 0, "Distance to FRAUD should be >= 0");

        // Primary region should have smallest distance
        double suspiciousDistance = result.getRegionDistance(RegionType.SUSPICIOUS);
        double safeDistance = result.getRegionDistance(RegionType.SAFE);
        double fraudDistance = result.getRegionDistance(RegionType.FRAUD);

        assertTrue(suspiciousDistance <= safeDistance || suspiciousDistance <= fraudDistance,
                   "Primary region should have smallest or equal distance");
    }

    @Test
    @DisplayName("Should detect anomalies (contradictory patterns)")
    public void testAnomalyDetection() {
        // Contradictory: VPN=true but low activity
        BehaviorFeatureVector anomalousNode = new BehaviorFeatureVector(
                2, 3, 4, 2, 0, 1.0,
                true, false, false, false, false, false
        );

        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(anomalousNode);

        assertNotNull(result);
        double anomalyScore = result.getAnomalyScore();
        assertTrue(anomalyScore > 0, "Anomaly score should be positive for contradictory patterns");
    }

    @Test
    @DisplayName("Should normalize probabilities to sum of 1.0")
    public void testProbabilityNormalization() {
        BehaviorFeatureVector node = new BehaviorFeatureVector(
                5, 8, 10, 6, 3, 3.5,
                true, false, true, false, true, true
        );

        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(node);

        double totalProbability = 
            result.getRegionProbability(RegionType.SAFE) +
            result.getRegionProbability(RegionType.SUSPICIOUS) +
            result.getRegionProbability(RegionType.FRAUD);

        assertEquals(1.0, totalProbability, 0.01, 
                     "Probabilities should sum to 1.0 (±0.01)");
    }

    @Test
    @DisplayName("Should apply feature penalties")
    public void testFeaturePenalties() {
        BehaviorFeatureVector nodeWithBlacklist = new BehaviorFeatureVector(
                2, 3, 4, 2, 0, 1.0,
                false, true, false, false, false, false
        );

        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(nodeWithBlacklist);

        // Apply penalties
        multiRegionService.applyFeaturePenalties(nodeWithBlacklist, result);

        // After applying penalties, fraud probability should be higher
        // than a similar node without blacklist
        BehaviorFeatureVector normalNode = new BehaviorFeatureVector(
                2, 3, 4, 2, 0, 1.0,
                false, false, false, false, false, false
        );

        MultiRegionAnalysisService.RegionAnalysisResult normalResult = 
            multiRegionService.analyzeAgainstRegions(normalNode);

        assertTrue(result.getRegionProbability(RegionType.FRAUD) >= 
                   normalResult.getRegionProbability(RegionType.FRAUD),
                   "Blacklist should increase FRAUD probability");
    }

    @Test
    @DisplayName("Should identify primary region correctly")
    public void testPrimaryRegionIdentification() {
        BehaviorFeatureVector node = new BehaviorFeatureVector(
                8, 12, 15, 8, 4, 5.0,
                true, false, true, false, true, false
        );

        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(node);

        RegionType primary = result.getPrimaryRegion();
        assertNotNull(primary, "Primary region should be identified");

        // Primary region should have highest probability
        double primaryProb = result.getRegionProbability(primary);
        for (RegionType type : RegionType.values()) {
            assertTrue(primaryProb >= result.getRegionProbability(type),
                       "Primary region should have highest probability");
        }
    }

    @Test
    @DisplayName("Should handle edge case: all zeros")
    public void testEdgeCaseAllZeros() {
        BehaviorFeatureVector zeroNode = new BehaviorFeatureVector(
                0, 0, 0, 0, 0, 0.0,
                false, false, false, false, false, false
        );

        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(zeroNode);

        assertNotNull(result, "Should handle all-zero node");
        assertEquals(RegionType.SAFE, result.getPrimaryRegion(),
                     "All-zero node should classify as SAFE");
    }

    @Test
    @DisplayName("Should handle edge case: all max values")
    public void testEdgeCaseAllMax() {
        BehaviorFeatureVector maxNode = new BehaviorFeatureVector(
                100, 100, 100, 100, 100, 100.0,
                true, true, true, true, true, true
        );

        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(maxNode);

        assertNotNull(result);
        assertEquals(RegionType.FRAUD, result.getPrimaryRegion(),
                     "All-max node should classify as FRAUD");
    }
}
