# HƯỚNG DẪN TRIỂN KHAI HỆ THỐNG PHÁT HIỆN GỌI LỪA HYBRID

## I. KHỞI ĐỘNG DỰ ÁN

### 1. Xây dựng (Build)
```bash
cd NCKHGRAPHDATABASE/complete
mvn clean compile
```

### 2. Chạy ứng dụng
```bash
mvn spring-boot:run
```

---

## II. CÓ CHỈ DỊCH VỤ CHÍNH

### A. FeatureNormalizationUtility

**Mục đích**: Chuẩn hóa vector đặc trưng

**Sử dụng**:
```java
@Autowired
private FeatureNormalizationUtility normalizationUtility;

public void example() {
    // Tạo vector
    BehaviorFeatureVector vector = new BehaviorFeatureVector(
        50,    // ipCount
        100,   // urlCount
        200,   // emailCount
        30,    // domainCount
        5,     // failedLoginCount
        500.0, // requestFrequency
        true, false, true, false, true, false
    );

    // Chuẩn hóa các đặc trưng số
    double[] normalized = normalizationUtility.normalizeNumericFeatures(vector);
    // Kết quả: [0.5, 0.5, 0.4, 0.3, 0.1, 0.5]

    // Chuẩn hóa toàn bộ vector
    double[] fullNormalized = normalizationUtility.normalizeFullVector(vector);
    // Kết quả: [numeric_features] + [boolean_features_as_0_or_1]

    // Cập nhật giới hạn động
    normalizationUtility.updateBounds(listOfHistoricalVectors);
}
```

### B. FeatureWeightsService

**Mục đích**: Tính toán trọng số đặc trưng

**Trọng số mặc định**:
```
TOR Network:         12.0 (cao nhất)
Blacklist:           10.0
Spam Pattern:        8.0
Suspicious URL:      7.0
Failed Login Count:  6.0
VPN:                 5.0
IP Count:            3.0
Request Frequency:   3.0
URL Count:           2.5
Email Count:         2.0
Domain Count:        2.0
Abnormal Access:     4.0
```

**Sử dụng**:
```java
@Autowired
private FeatureWeightsService weightsService;

public void example() {
    BehaviorFeatureVector node = /* ... */;
    BehaviorFeatureVector centerVector = /* ... */;

    // Tính weighted distance
    double weightedDist = weightsService.calculateWeightedDistance(node, centerVector);
    System.out.println("Weighted Distance: " + weightedDist);

    // Tính risk score dựa trên trọng số
    double riskScore = weightsService.calculateRiskScoreByWeights(node);
    System.out.println("Risk Score by Weights: " + riskScore);

    // Lấy danh sách đặc trưng theo mức độ quan trọng
    List<FeatureWeightsService.FeatureImportance> ranked = weightsService.getRankedFeatures();
    for (FeatureWeightsService.FeatureImportance feature : ranked) {
        System.out.println(feature);
        // Output: TOR Network: 12.0
        //         Blacklist: 10.0
        //         ...
    }

    // Cập nhật trọng số riêng
    weightsService.setFeatureWeight("tor", 15.0);
}
```

### C. KNNVotingAndRecallService

**Mục đích**: Bỏ phiếu và tính toán Recall

**Ví dụ bỏ phiếu**:
```java
@Autowired
private KNNVotingAndRecallService votingService;

public void votingExample() {
    // Tạo danh sách neighbors với classification
    List<KNNVotingAndRecallService.KNNNeighbor> neighbors = new ArrayList<>();

    neighbors.add(new KNNVotingAndRecallService.KNNNeighbor(
        vectorA, "FRAUD", 0.5  // distance
    ));
    neighbors.add(new KNNVotingAndRecallService.KNNNeighbor(
        vectorB, "FRAUD", 0.6
    ));
    neighbors.add(new KNNVotingAndRecallService.KNNNeighbor(
        vectorC, "FRAUD", 0.7
    ));
    neighbors.add(new KNNVotingAndRecallService.KNNNeighbor(
        vectorD, "FRAUD", 0.8
    ));
    neighbors.add(new KNNVotingAndRecallService.KNNNeighbor(
        vectorE, "FRAUD", 0.9
    ));
    neighbors.add(new KNNVotingAndRecallService.KNNNeighbor(
        vectorF, "FRAUD", 1.0
    ));
    neighbors.add(new KNNVotingAndRecallService.KNNNeighbor(
        vectorG, "SAFE", 1.2
    ));

    // Bỏ phiếu đơn
    KNNVotingAndRecallService.VotingResult voting = votingService.performVoting(neighbors, 7);
    System.out.println(voting);
    // Output:
    // Voting Result:
    //   FRAUD: 6 votes (85.7%)
    //   SAFE: 1 votes (14.3%)
    // Winner: FRAUD (85.7% confidence)
    // Confidence: 0.71

    // Bỏ phiếu có trọng số (neighbors gần hơn = trọng số cao hơn)
    KNNVotingAndRecallService.VotingResult weightedVoting = votingService.performWeightedVoting(neighbors, 7);
    System.out.println(weightedVoting);
}
```

**Tính Recall**:
```java
public void recallExample() {
    // Giả sử sau kiểm thử:
    // TP = 80 (phát hiện đúng fraud)
    // FN = 20 (bỏ lỡ fraud)
    // TN = 500 (phát hiện đúng safe)
    // FP = 30 (false alarm)

    KNNVotingAndRecallService.RecallMetrics metrics = votingService.calculateRecall(80, 20, 500, 30);
    
    System.out.println(metrics);
    // Output:
    // Recall Metrics:
    //   TP (Correct Fraud): 80
    //   FN (Missed Fraud): 20
    //   TN (Correct Safe): 500
    //   FP (False Alarms): 30
    //   Recall: 80.00% ✓  (>= 80% requirement)
    //   Precision: 72.73%
    //   F-Score: 76.19%

    if (metrics.meetsRequirement) {
        System.out.println("✓ Hệ thống đạt yêu cầu Recall >= 80%");
    }
}
```

**Tính P-value**:
```java
public void pvalueExample() {
    // Observed: 85 fraud cases detected, 15 safe
    // Expected: 80 fraud cases, 20 safe

    KNNVotingAndRecallService.StatisticalSignificance stats = 
        votingService.calculatePValue(85, 15, 80, 20);
    
    System.out.println(stats);
    // Output: P-value: 0.2589 (Not significant (p ≥ 0.10)) [n=100]
    
    if (stats.isSignificant) {
        System.out.println("✓ Kết quả có ý nghĩa thống kê (p < 0.05)");
    } else {
        System.out.println("✗ Kết quả không có ý nghĩa thống kê (p >= 0.05)");
    }
}
```

### D. MultiRegionAnalysisService

**Mục đích**: Phân tích node theo 3 miền (SAFE, SUSPICIOUS, FRAUD)

**Sử dụng**:
```java
@Autowired
private MultiRegionAnalysisService multiRegionService;

public void regionExample() {
    BehaviorFeatureVector node = new BehaviorFeatureVector(
        10, 15, 20, 8, 3, 5.5,
        true, true, false, false, true, false
    );

    // Phân tích node so với 3 miền
    MultiRegionAnalysisService.RegionAnalysisResult result = 
        multiRegionService.analyzeAgainstRegions(node);

    System.out.println("Region Analysis:");
    System.out.println("Primary Region: " + result.getPrimaryRegion());
    // Output: Primary Region: FRAUD or SUSPICIOUS

    // Khoảng cách tới từng miền
    System.out.println("Distance to SAFE: " + result.getRegionDistance(RegionType.SAFE));
    System.out.println("Distance to SUSPICIOUS: " + result.getRegionDistance(RegionType.SUSPICIOUS));
    System.out.println("Distance to FRAUD: " + result.getRegionDistance(RegionType.FRAUD));

    // Xác suất từng miền
    System.out.println("Probability SAFE: " + (result.getRegionProbability(RegionType.SAFE) * 100) + "%");
    System.out.println("Probability SUSPICIOUS: " + (result.getRegionProbability(RegionType.SUSPICIOUS) * 100) + "%");
    System.out.println("Probability FRAUD: " + (result.getRegionProbability(RegionType.FRAUD) * 100) + "%");

    // Áp dụng penalty cho đặc trưng nguy hiểm
    multiRegionService.applyFeaturePenalties(node, result);
    // Nếu node có blacklist + TOR => khoảng cách tới FRAUD giảm
}
```

### E. EnhancedConsensusEngineService

**Mục đích**: Tổng hợp kết quả từ tất cả phương pháp

**Ví dụ Convergence Analysis**:
```java
@Autowired
private EnhancedConsensusEngineService consensusService;

public void convergenceExample() {
    // Ba distance metric cho KNN
    double euclideanScore = 0.90;
    double minkowskiScore = 0.88;
    double hammingScore = 0.95;

    EnhancedConsensusEngineService.ConvergenceAnalysis convergence = 
        new EnhancedConsensusEngineService.ConvergenceAnalysis(
            euclideanScore, minkowskiScore, hammingScore
        );

    System.out.println("Convergence Analysis:");
    System.out.println("Status: " + convergence.status);
    // Output: HIGH_CONVERGENCE - High confidence

    System.out.println("Convergence Ratio: " + (convergence.convergenceRatio * 100) + "%");
    // Output: Convergence Ratio: 85.34%

    for (String detail : convergence.details) {
        System.out.println("  " + detail);
    }
    // Output:
    //   Euclidean: 90.00%
    //   Minkowski: 88.00%
    //   Hamming: 95.00%
    //   Average: 91.00%
    //   Std Dev: 0.0232
    //   Convergence: 85.34%
}
```

**Ví dụ Method Agreement**:
```java
public void methodAgreementExample() {
    double ruleScore = 0.85;
    double knnScore = 0.82;
    double regionScore = 0.88;
    double probabilityScore = 0.80;

    EnhancedConsensusEngineService.MethodAgreementAnalysis agreement = 
        new EnhancedConsensusEngineService.MethodAgreementAnalysis(
            ruleScore, knnScore, regionScore, probabilityScore
        );

    System.out.println(agreement);
    // Output:
    // Method Agreement:
    //   Rule-Based: 85.00% ✓
    //   KNN: 82.00% ✓
    //   Multi-Region: 88.00% ✓
    //   Probability: 80.00% ✓
    //   Average: 83.75%
    //   Agreement: 96.50%
    //   Consensus: HIGH_RISK (Consensus: Fraud)

    if (agreement.hasConsensus) {
        System.out.println("✓ Tất cả phương pháp đồng ý: " + agreement.consensus);
    } else {
        System.out.println("⚠️ Có sự không đồng ý:");
        for (String anomaly : agreement.anomalies) {
            System.out.println("  - " + anomaly);
        }
    }
}
```

**Ví dụ Consensus Cuối Cùng**:
```java
public void consensusExample() {
    double ruleScore = 0.85;
    double knnScoreEuclidean = 0.90;
    double knnScoreMinkowski = 0.88;
    double knnScoreHamming = 0.95;
    double regionScore = 0.88;
    double probabilityScore = 0.80;

    EnhancedConsensusEngineService.ConsensusResultEnhanced result = 
        consensusService.produceEnhancedConsensus(
            ruleScore,
            knnScoreEuclidean,
            knnScoreMinkowski,
            knnScoreHamming,
            regionScore,
            probabilityScore
        );

    System.out.println(result);
    // Output:
    // Enhanced Consensus:
    //   Risk Level: CRITICAL (0.858)
    //   Confidence: 91.5%
    //   Method Agreement: HIGH_RISK (Consensus: Fraud)
    //   Anomalies: None
}
```

### F. HybridFraudDetectionService

**Mục đích**: Orchestrate toàn bộ phân tích hybrid

**Sử dụng**:
```java
@Autowired
private HybridFraudDetectionService hybridService;

public void completeAnalysis() {
    BehaviorFeatureVector nodeFeatures = new BehaviorFeatureVector(
        12, 18, 25, 10, 4, 7.2,
        true, true, true, false, true, false
    );

    // Load lịch sử (historical samples)
    List<BehaviorFeatureVector> historicalSamples = 
        sessionFeatureService.loadHistoricalSamples(null, 100);

    // Chạy phân tích hybrid đầy đủ
    HybridFraudDetectionService.HybridFraudDetectionResult result = 
        hybridService.analyzeNode(nodeFeatures, historicalSamples);

    System.out.println("=== HYBRID FRAUD DETECTION RESULT ===");
    System.out.println("Risk Level: " + result.getFinalRiskLevel());
    System.out.println("Risk Score: " + String.format("%.2f%%", result.getFinalRiskScore() * 100));
    System.out.println("Confidence: " + String.format("%.2f%%", result.getConfidence() * 100));
    
    System.out.println("\nAnalysis Steps:");
    for (String step : result.getAnalysisSteps()) {
        System.out.println("  ✓ " + step);
    }

    System.out.println("\nWarnings:");
    if (result.getWarnings().isEmpty()) {
        System.out.println("  ✓ No warnings");
    } else {
        for (String warning : result.getWarnings()) {
            System.out.println("  ⚠️  " + warning);
        }
    }

    System.out.println("\nDetailed Analysis:");
    System.out.println("  Rule-Based Score: " + String.format("%.2f%%", result.getRuleBasedScore() * 100));
    System.out.println("  KNN Score: " + String.format("%.2f%%", result.getKnnScore() * 100));
    System.out.println("  Multi-Region Score: " + String.format("%.2f%%", result.getMultiRegionScore() * 100));
    System.out.println("  Probability Score: " + String.format("%.2f%%", result.getProbabilityScore() * 100));
}
```

---

## III. REST API ENDPOINTS

### 1. Phân tích Hybrid (tất cả phương pháp)
```http
POST /api/analyze/hybrid
Content-Type: application/json

{
  "ipCount": 12,
  "urlCount": 18,
  "emailCount": 25,
  "domainCount": 10,
  "failedLoginCount": 4,
  "requestFrequency": 7.2,
  "vpn": true,
  "blacklist": true,
  "suspiciousUrl": true,
  "torNetwork": false,
  "spamPattern": true,
  "abnormalAccessTime": false
}

Response:
{
  "riskLevel": "CRITICAL",
  "riskScore": 0.858,
  "confidence": 0.915,
  "ruleScore": 0.85,
  "knnScore": 0.91,
  "regionScore": 0.88,
  "probabilityScore": 0.80,
  "analysis": "High agreement between all methods..."
}
```

### 2. Phân tích Multi-Region
```http
POST /api/analyze/regions
Content-Type: application/json

Response:
{
  "primaryRegion": "FRAUD",
  "distanceToSafe": 15.2,
  "distanceToSuspicious": 8.5,
  "distanceToFraud": 2.1,
  "probabilitySafe": 0.05,
  "probabilitySuspicious": 0.25,
  "probabilityFraud": 0.70
}
```

### 3. Phân tích KNN
```http
POST /api/analyze/knn
Content-Type: application/json

Response:
{
  "euclideanScore": 0.90,
  "minkowskiScore": 0.88,
  "hammingScore": 0.95,
  "finalScore": 0.91,
  "convergence": 0.85,
  "kValue": 7,
  "votingResult": {
    "FRAUD": 6,
    "SAFE": 1
  }
}
```

---

## IV. GIÁM SÁT VÀ LOGGING

### Các Metrics theo dõi
```java
// Recall (phát hiện)
if (metrics.recall >= 0.80) {
    logger.info("✓ Recall >= 80%");
} else {
    logger.warn("✗ Recall < 80%");
}

// Convergence (sự hội tụ)
if (convergence.convergenceRatio > 0.7) {
    logger.info("✓ High convergence: " + convergence.convergenceRatio);
} else {
    logger.warn("⚠️ Low convergence: " + convergence.convergenceRatio);
}

// Method agreement
if (agreement.hasConsensus) {
    logger.info("✓ Method consensus: " + agreement.consensus);
} else {
    logger.warn("⚠️ Method disagreement");
}

// Statistical significance
if (stats.isSignificant) {
    logger.info("✓ Statistically significant (p < 0.05)");
} else {
    logger.warn("⚠️ Not significant (p >= 0.05)");
}
```

---

## V. KIỂM THỬ

### Test Case 1: Clear FRAUD
```java
@Test
public void testClearFraud() {
    BehaviorFeatureVector fraud = new BehaviorFeatureVector(
        30, 50, 100, 25, 15, 50.0,  // High numeric values
        true, true, true, true, true, true  // All flags true
    );
    
    HybridFraudDetectionResult result = hybridService.analyzeNode(fraud, historicalSamples);
    
    assertEquals("CRITICAL", result.getFinalRiskLevel());
    assertTrue(result.getFinalRiskScore() > 0.7);
}
```

### Test Case 2: Clear SAFE
```java
@Test
public void testClearSafe() {
    BehaviorFeatureVector safe = new BehaviorFeatureVector(
        1, 2, 5, 3, 0, 1.0,  // Low numeric values
        false, false, false, false, false, false  // All flags false
    );
    
    HybridFraudDetectionResult result = hybridService.analyzeNode(safe, historicalSamples);
    
    assertEquals("SAFE", result.getFinalRiskLevel());
    assertTrue(result.getFinalRiskScore() < 0.33);
}
```

### Test Case 3: SUSPICIOUS
```java
@Test
public void testSuspicious() {
    BehaviorFeatureVector suspicious = new BehaviorFeatureVector(
        8, 12, 15, 6, 2, 3.5,  // Medium values
        true, false, false, false, true, false
    );
    
    HybridFraudDetectionResult result = hybridService.analyzeNode(suspicious, historicalSamples);
    
    assertEquals("SUSPICIOUS", result.getFinalRiskLevel());
    assertTrue(result.getFinalRiskScore() >= 0.33 && result.getFinalRiskScore() < 0.67);
}
```

---

**Cập nhật**: 2026-05-14  
**Phiên bản**: 1.0
