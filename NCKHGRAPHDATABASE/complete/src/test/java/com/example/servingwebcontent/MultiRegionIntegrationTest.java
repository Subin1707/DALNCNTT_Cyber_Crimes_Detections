package com.example.servingwebcontent;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import com.example.servingwebcontent.service.MultiRegionAnalysisService;
import com.example.servingwebcontent.model.RegionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Multi-Region Analysis
 * 
 * Tests validate:
 * 1. SAFE nodes classified correctly (low numeric, no flags)
 * 2. FRAUD nodes classified correctly (high numeric, all flags)
 * 3. SUSPICIOUS nodes classified correctly (mixed features)
 * 4. Distance calculations using 3 metrics
 * 5. Feature penalty application
 * 6. Anomaly detection for contradictory patterns
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class MultiRegionIntegrationTest {

    @Autowired
    private MultiRegionAnalysisService multiRegionService;

    /**
     * Test 1: SAFE node classification
     * Expected: Primary region = SAFE, high probability
     */
    @Test
    public void testSafeNodeClassification() {
        BehaviorFeatureVector safeNode = new BehaviorFeatureVector(
                1, 2, 3, 2, 0, 0.5,      // Numeric: all low
                false, false, false,      // Flags: all false
                false, false, false
        );

        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(safeNode);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(RegionType.SAFE, result.getPrimaryRegion(), 
                     "Safe node should be classified as SAFE region");
        assertGreater(result.getRegionProbability(RegionType.SAFE), 0.7, 
                      "Safe node should have >70% probability for SAFE region");
    }

    /**
     * Test 2: FRAUD node classification
     * Expected: Primary region = FRAUD, high probability
     */
    @Test
    public void testFraudNodeClassification() {
        BehaviorFeatureVector fraudNode = new BehaviorFeatureVector(
                15, 20, 25, 18, 8, 10.0,  // Numeric: all high
                true, true, true,          // Flags: all true
                true, true, true
        );

        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(fraudNode);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(RegionType.FRAUD, result.getPrimaryRegion(), 
                     "Fraud node should be classified as FRAUD region");
        assertGreater(result.getRegionProbability(RegionType.FRAUD), 0.7, 
                      "Fraud node should have >70% probability for FRAUD region");
    }

    /**
     * Test 3: SUSPICIOUS node classification
     * Expected: Primary region = SUSPICIOUS, high probability
     */
    @Test
    public void testSuspiciousNodeClassification() {
        BehaviorFeatureVector suspiciousNode = new BehaviorFeatureVector(
                5, 8, 10, 6, 3, 3.5,     // Numeric: medium
                true, false, true,        // Flags: mixed
                false, true, true
        );

        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(suspiciousNode);
        
        assertNotNull(result, "Result should not be null");
        assertEquals(RegionType.SUSPICIOUS, result.getPrimaryRegion(), 
                     "Suspicious node should be classified as SUSPICIOUS region");
        assertGreater(result.getRegionProbability(RegionType.SUSPICIOUS), 0.6, 
                      "Suspicious node should have >60% probability for SUSPICIOUS region");
    }

    /**
     * Test 4: Distance calculations
     * Expected: Distances increase from SAFE → FRAUD node
     */
    @Test
    public void testDistanceCalculations() {
        BehaviorFeatureVector safeNode = new BehaviorFeatureVector(
                1, 2, 3, 2, 0, 0.5,
                false, false, false, false, false, false
        );
        BehaviorFeatureVector fraudNode = new BehaviorFeatureVector(
                15, 20, 25, 18, 8, 10.0,
                true, true, true, true, true, true
        );

        MultiRegionAnalysisService.RegionAnalysisResult safeResult = 
            multiRegionService.analyzeAgainstRegions(safeNode);
        MultiRegionAnalysisService.RegionAnalysisResult fraudResult = 
            multiRegionService.analyzeAgainstRegions(fraudNode);

        // Safe node should be closer to SAFE region
        double safeToSafeDistance = safeResult.getRegionDistance(RegionType.SAFE);
        double safeToFraudDistance = safeResult.getRegionDistance(RegionType.FRAUD);
        assertTrue(safeToSafeDistance < safeToFraudDistance, 
                   "Safe node closer to SAFE region than FRAUD region");

        // Fraud node should be closer to FRAUD region
        double fraudToFraudDistance = fraudResult.getRegionDistance(RegionType.FRAUD);
        double fraudToSafeDistance = fraudResult.getRegionDistance(RegionType.SAFE);
        assertTrue(fraudToFraudDistance < fraudToSafeDistance, 
                   "Fraud node closer to FRAUD region than SAFE region");
    }

    /**
     * Test 5: Anomaly detection
     * Expected: High anomaly score for contradictory patterns (VPN=true, blacklist=false, but high counts)
     */
    @Test
    public void testAnomalyDetection() {
        // Contradictory: VPN true (suspicious) but low activity (safe)
        BehaviorFeatureVector anomalousNode = new BehaviorFeatureVector(
                2, 3, 4, 2, 0, 1.0,      // Low numeric (SAFE pattern)
                true, false, false,       // VPN true but rest false (contradictory)
                false, false, false
        );

        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(anomalousNode);
        
        assertNotNull(result, "Result should not be null");
        assertGreater(result.getAnomalyScore(), 0.3, 
                      "Anomalous pattern should have high anomaly score (>0.3)");
    }

    /**
     * Test 6: Feature penalty application
     * Expected: Blacklist and TOR flags significantly increase fraud distance
     */
    @Test
    public void testFeaturePenalties() {
        // Normally safe, but with dangerous flags
        BehaviorFeatureVector nodeWithBlacklist = new BehaviorFeatureVector(
                2, 3, 4, 2, 0, 1.0,
                false, true, false,       // Blacklist = true (dangerous)
                false, false, false
        );

        BehaviorFeatureVector nodeWithTOR = new BehaviorFeatureVector(
                2, 3, 4, 2, 0, 1.0,
                false, false, false,
                true, false, false       // TOR = true (dangerous)
        );

        MultiRegionAnalysisService.RegionAnalysisResult resultBlacklist = 
            multiRegionService.analyzeAgainstRegions(nodeWithBlacklist);
        MultiRegionAnalysisService.RegionAnalysisResult resultTOR = 
            multiRegionService.analyzeAgainstRegions(nodeWithTOR);

        multiRegionService.applyFeaturePenalties(nodeWithBlacklist, resultBlacklist);
        multiRegionService.applyFeaturePenalties(nodeWithTOR, resultTOR);

        // After penalties, FRAUD probability should increase
        assertGreater(resultBlacklist.getRegionProbability(RegionType.FRAUD), 0.3, 
                      "Blacklist flag should increase FRAUD probability");
        assertGreater(resultTOR.getRegionProbability(RegionType.FRAUD), 0.3, 
                      "TOR flag should increase FRAUD probability");
    }

    /**
     * Test 7: Probability normalization
     * Expected: Sum of probabilities = 1.0
     */
    @Test
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
                     "Sum of probabilities should equal 1.0 (±0.01 tolerance)");
    }

    /**
     * Test 8: Region characteristics validation
     * Expected: SAFE < SUSPICIOUS < FRAUD in security risk
     */
    @Test
    public void testRegionCharacteristics() {
        BehaviorFeatureVector safeNode = new BehaviorFeatureVector(
                1, 2, 3, 2, 0, 0.5, false, false, false, false, false, false
        );
        BehaviorFeatureVector suspiciousNode = new BehaviorFeatureVector(
                5, 8, 10, 6, 3, 3.5, true, false, true, false, true, true
        );
        BehaviorFeatureVector fraudNode = new BehaviorFeatureVector(
                15, 20, 25, 18, 8, 10.0, true, true, true, true, true, true
        );

        MultiRegionAnalysisService.RegionAnalysisResult safeResult = 
            multiRegionService.analyzeAgainstRegions(safeNode);
        MultiRegionAnalysisService.RegionAnalysisResult suspiciousResult = 
            multiRegionService.analyzeAgainstRegions(suspiciousNode);
        MultiRegionAnalysisService.RegionAnalysisResult fraudResult = 
            multiRegionService.analyzeAgainstRegions(fraudNode);

        double safeFraudProb = safeResult.getRegionProbability(RegionType.FRAUD);
        double suspiciousFraudProb = suspiciousResult.getRegionProbability(RegionType.FRAUD);
        double fraudFraudProb = fraudResult.getRegionProbability(RegionType.FRAUD);

        assertTrue(safeFraudProb < suspiciousFraudProb, 
                   "Safe node should have lower FRAUD probability than Suspicious node");
        assertTrue(suspiciousFraudProb < fraudFraudProb, 
                   "Suspicious node should have lower FRAUD probability than Fraud node");
    }

    // Helper assertion for greater than
    private void assertGreater(double actual, double expected, String message) {
        assertTrue(actual > expected, message + " (" + actual + " > " + expected + ")");
    }
}
