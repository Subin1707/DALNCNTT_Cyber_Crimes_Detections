package com.example.servingwebcontent.dto;

import java.util.List;

public class HybridRiskScoreDTO {

    private final double ruleScore;
    private final double knnScore;
    private final double probabilityScore;
    private final double finalScore;
    private final String riskLevel;
    private final String verdict;
    private final List<String> indicators;
    private final SessionFeatureVectorDTO features;

    public HybridRiskScoreDTO(double ruleScore,
                              double knnScore,
                              double probabilityScore,
                              double finalScore,
                              String riskLevel,
                              String verdict,
                              List<String> indicators,
                              SessionFeatureVectorDTO features) {

        this.ruleScore = ruleScore;
        this.knnScore = knnScore;
        this.probabilityScore = probabilityScore;
        this.finalScore = finalScore;
        this.riskLevel = riskLevel;
        this.verdict = verdict;
        this.indicators = indicators == null ? List.of() : List.copyOf(indicators);
        this.features = features;
    }

    public double getRuleScore() {
        return ruleScore;
    }

    public double getKnnScore() {
        return knnScore;
    }

    public double getProbabilityScore() {
        return probabilityScore;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public String getVerdict() {
        return verdict;
    }

    public List<String> getIndicators() {
        return indicators;
    }

    public SessionFeatureVectorDTO getFeatures() {
        return features;
    }
}
