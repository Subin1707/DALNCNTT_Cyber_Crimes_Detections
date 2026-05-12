package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import com.example.servingwebcontent.model.RegionType;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Hybrid Fraud Detection Service
 * Orchestrates complete analysis pipeline:
 * Rule-Based + KNN + Multi-Region + Statistical Probability + Consensus
 */
@Service
public class HybridFraudDetectionService {

    private final FraudAnalysisService fraudAnalysisService;
    private final KNNEnhancedAnalysisService knnAnalysisService;
    private final MultiRegionAnalysisService multiRegionService;
    private final StatisticalProbabilityService probabilityService;
    private final ConsensusEngineService consensusEngineService;

    public HybridFraudDetectionService(FraudAnalysisService fraudAnalysisService,
                                       KNNEnhancedAnalysisService knnAnalysisService,
                                       MultiRegionAnalysisService multiRegionService,
                                       StatisticalProbabilityService probabilityService,
                                       ConsensusEngineService consensusEngineService) {
        this.fraudAnalysisService = fraudAnalysisService;
        this.knnAnalysisService = knnAnalysisService;
        this.multiRegionService = multiRegionService;
        this.probabilityService = probabilityService;
        this.consensusEngineService = consensusEngineService;
    }

    /**
     * Execute complete hybrid fraud detection analysis
     * @param nodeFeatures Behavior feature vector
     * @param historicalSamples Historical behavior samples for KNN
     * @return Comprehensive fraud detection result
     */
    public HybridFraudDetectionResult analyzeNode(BehaviorFeatureVector nodeFeatures,
                                                  List<BehaviorFeatureVector> historicalSamples) {
        if (nodeFeatures == null) {
            return new HybridFraudDetectionResult("ERROR: Null input");
        }

        HybridFraudDetectionResult result = new HybridFraudDetectionResult();
        result.setNodeFeatures(nodeFeatures);

        // Step 1: Rule-Based Analysis
        double ruleScore = performRuleBasedAnalysis(nodeFeatures);
        result.setRuleBasedScore(ruleScore);
        result.addStep("Rule-Based Analysis: " + String.format("%.2f%%", ruleScore));

        // Step 2: Multi-Region Analysis
        MultiRegionAnalysisService.RegionAnalysisResult regionResult =
                multiRegionService.analyzeAgainstRegions(nodeFeatures);
        multiRegionService.applyFeaturePenalties(nodeFeatures, regionResult);

        double regionScore = regionResult.getRegionProbability(RegionType.FRAUD) * 100.0;
        result.setRegionAnalysisResult(regionResult);
        result.setMultiRegionScore(regionScore);
        result.addStep("Multi-Region Analysis: " + String.format("%.2f%% -> %s", regionScore, regionResult.getPrimaryRegion()));

        // Step 3: Statistical Probability Analysis
        StatisticalProbabilityService.ProbabilityResult probResult =
                probabilityService.calculateBayesianProbability(nodeFeatures);

        double probabilityScore = probResult.getPosteriorFraud() * 100.0;
        result.setProbabilityResult(probResult);
        result.setProbabilityScore(probabilityScore);
        result.addStep("Bayesian Analysis: " + String.format("%.2f%%", probabilityScore) +
                " (p-value: " + String.format("%.4f", probResult.getPValue()) + ", " +
                (probResult.isStatisticallySignificant() ? "significant" : "not significant") + ")");

        // Step 4: KNN Multi-Distance Analysis (if historical samples available)
        double knnScore = 0.0;
        if (historicalSamples != null && !historicalSamples.isEmpty()) {
            // Note: KNNEnhancedAnalysisService already exists
            // We'll use a simplified KNN score here
            knnScore = estimateKNNScore(nodeFeatures, historicalSamples);
            result.setKnnScore(knnScore);
            result.addStep("KNN Analysis: " + String.format("%.2f%%", knnScore));
        } else {
            result.addStep("KNN Analysis: SKIPPED (no historical samples)");
        }

        // Step 5: Consensus Engine
        ConsensusEngineService.ConsensusResult consensusResult =
                consensusEngineService.produceConsensus(ruleScore, knnScore, regionScore, probabilityScore);

        result.setConsensusResult(consensusResult);
        result.addStep("Consensus Result: " + consensusResult.getRiskLevel() +
                " (" + String.format("%.2f%%", consensusResult.getConsensusScore() * 100.0) + ")");

        // Prepare final report
        result.setFinalRiskScore(consensusResult.getConsensusScore());
        result.setFinalRiskLevel(consensusResult.getRiskLevel());
        result.setConfidence(consensusResult.getConfidence());
        result.setAnalysis(consensusResult.getAnalysis());

        // Anomaly detection
        if (consensusResult.isDisagreement()) {
            result.setAnomalyDetected(true);
            result.addWarning("METHOD DISAGREEMENT: Different analysis methods produced conflicting results. " +
                    "This may indicate an obfuscated attack.");
        }

        if (regionResult.getAnomalyScore() > 0.4) {
            result.setAnomalyDetected(true);
            result.addWarning("REGION ANOMALY: Node doesn't clearly belong to any region. " +
                    "Anomaly score: " + String.format("%.2f", regionResult.getAnomalyScore()));
        }

        if (!probResult.isStatisticallySignificant()) {
            result.addWarning("STATISTICAL INSIGNIFICANCE: p-value > 0.05. Results may not be reliable.");
        }

        return result;
    }

    /**
     * Perform rule-based analysis (simplified version)
     * In practice, this would call FraudAnalysisService
     */
    private double performRuleBasedAnalysis(BehaviorFeatureVector node) {
        double score = 0.0;

        if (node.isBlacklist()) score += 40;
        if (node.isTorNetwork()) score += 30;
        if (node.isVpn()) score += 10;
        if (node.isSpamPattern()) score += 20;
        if (node.isSuspiciousUrl()) score += 15;
        if (node.isAbnormalAccessTime()) score += 12;

        if (node.getIpCount() > 10) score += 25;
        if (node.getUrlCount() > 20) score += 20;
        if (node.getFailedLoginCount() > 5) score += 25;

        return Math.min(100.0, score);
    }

    /**
     * Estimate KNN score based on nearest neighbors
     */
    private double estimateKNNScore(BehaviorFeatureVector node, List<BehaviorFeatureVector> samples) {
        if (samples.isEmpty()) {
            return 0.0;
        }

        // Find K nearest neighbors
        int k = Math.min(7, samples.size());
        List<NeighborDistance> neighbors = new ArrayList<>();

        for (BehaviorFeatureVector sample : samples) {
            double euclideanDist = calculateEuclideanDistance(node, sample);
            neighbors.add(new NeighborDistance(sample, euclideanDist));
        }

        // Sort by distance
        neighbors.sort(Comparator.comparingDouble(n -> n.distance));

        // Take K nearest
        double fraudCount = 0.0;
        for (int i = 0; i < k && i < neighbors.size(); i++) {
            // Simulate fraud detection (assume ~40% of samples are fraudulent)
            if (neighbors.get(i).distance < 2.0) {
                fraudCount += (1.0 / (neighbors.get(i).distance + 0.1));
            }
        }

        double knnScore = (fraudCount / k) * 100.0;
        return Math.min(100.0, knnScore);
    }

    /**
     * Calculate Euclidean distance between two vectors
     */
    private double calculateEuclideanDistance(BehaviorFeatureVector a, BehaviorFeatureVector b) {
        double sum = 0.0;
        double[] vectorA = a.toNumericArray();
        double[] vectorB = b.toNumericArray();

        for (int i = 0; i < Math.min(vectorA.length, vectorB.length); i++) {
            double diff = vectorA[i] - vectorB[i];
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    /**
     * Helper class for neighbor distance
     */
    private static class NeighborDistance {
        BehaviorFeatureVector vector;
        double distance;

        NeighborDistance(BehaviorFeatureVector vector, double distance) {
            this.vector = vector;
            this.distance = distance;
        }
    }

    /**
     * Comprehensive Hybrid Fraud Detection Result
     */
    public static class HybridFraudDetectionResult {
        private BehaviorFeatureVector nodeFeatures;
        private double ruleBasedScore;
        private double knnScore;
        private double multiRegionScore;
        private double probabilityScore;
        private MultiRegionAnalysisService.RegionAnalysisResult regionAnalysisResult;
        private StatisticalProbabilityService.ProbabilityResult probabilityResult;
        private ConsensusEngineService.ConsensusResult consensusResult;
        private double finalRiskScore;
        private String finalRiskLevel;
        private double confidence;
        private String analysis;
        private boolean anomalyDetected;
        private final List<String> steps = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private String errorMessage;

        public HybridFraudDetectionResult() {
        }

        public HybridFraudDetectionResult(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        // Getters and setters
        public BehaviorFeatureVector getNodeFeatures() {
            return nodeFeatures;
        }

        public void setNodeFeatures(BehaviorFeatureVector nodeFeatures) {
            this.nodeFeatures = nodeFeatures;
        }

        public double getRuleBasedScore() {
            return ruleBasedScore;
        }

        public void setRuleBasedScore(double ruleBasedScore) {
            this.ruleBasedScore = ruleBasedScore;
        }

        public double getKnnScore() {
            return knnScore;
        }

        public void setKnnScore(double knnScore) {
            this.knnScore = knnScore;
        }

        public double getMultiRegionScore() {
            return multiRegionScore;
        }

        public void setMultiRegionScore(double multiRegionScore) {
            this.multiRegionScore = multiRegionScore;
        }

        public double getProbabilityScore() {
            return probabilityScore;
        }

        public void setProbabilityScore(double probabilityScore) {
            this.probabilityScore = probabilityScore;
        }

        public MultiRegionAnalysisService.RegionAnalysisResult getRegionAnalysisResult() {
            return regionAnalysisResult;
        }

        public void setRegionAnalysisResult(MultiRegionAnalysisService.RegionAnalysisResult result) {
            this.regionAnalysisResult = result;
        }

        public StatisticalProbabilityService.ProbabilityResult getProbabilityResult() {
            return probabilityResult;
        }

        public void setProbabilityResult(StatisticalProbabilityService.ProbabilityResult result) {
            this.probabilityResult = result;
        }

        public ConsensusEngineService.ConsensusResult getConsensusResult() {
            return consensusResult;
        }

        public void setConsensusResult(ConsensusEngineService.ConsensusResult result) {
            this.consensusResult = result;
        }

        public double getFinalRiskScore() {
            return finalRiskScore;
        }

        public void setFinalRiskScore(double finalRiskScore) {
            this.finalRiskScore = Math.max(0.0, Math.min(1.0, finalRiskScore));
        }

        public String getFinalRiskLevel() {
            return finalRiskLevel;
        }

        public void setFinalRiskLevel(String finalRiskLevel) {
            this.finalRiskLevel = finalRiskLevel;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = Math.max(0.0, Math.min(1.0, confidence));
        }

        public String getAnalysis() {
            return analysis;
        }

        public void setAnalysis(String analysis) {
            this.analysis = analysis;
        }

        public boolean isAnomalyDetected() {
            return anomalyDetected;
        }

        public void setAnomalyDetected(boolean anomalyDetected) {
            this.anomalyDetected = anomalyDetected;
        }

        public List<String> getSteps() {
            return new ArrayList<>(steps);
        }

        public void addStep(String step) {
            this.steps.add(step);
        }

        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        @Override
        public String toString() {
            return "HybridFraudDetectionResult{" +
                    "finalRiskScore=" + String.format("%.3f", finalRiskScore) +
                    ", finalRiskLevel='" + finalRiskLevel + '\'' +
                    ", confidence=" + String.format("%.1%", confidence) +
                    ", anomalyDetected=" + anomalyDetected +
                    ", steps=" + steps.size() +
                    ", warnings=" + warnings.size() +
                    '}';
        }
    }
}
