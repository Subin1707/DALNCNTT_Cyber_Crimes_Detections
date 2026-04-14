package com.example.servingwebcontent.dto;

import java.util.List;

public class AnalysisResultDTO {

    private String sessionId;

    // Object được phân tích
    private String target;
    private String targetType;

    private double riskScore;
    private String riskLevel;
    private String verdict;

    private List<String> indicators;
    private Double ruleScore;
    private Double knnScore;
    private Double probabilityScore;
    private SessionFeatureVectorDTO features;

    // Constructor rỗng (bắt buộc cho Jackson)
    public AnalysisResultDTO() {}

    // Constructor đầy đủ
    public AnalysisResultDTO(
            String sessionId,
            String target,
            String targetType,
            double riskScore,
            String riskLevel,
            String verdict,
            List<String> indicators) {

        this(
                sessionId,
                target,
                targetType,
                riskScore,
                riskLevel,
                verdict,
                indicators,
                null,
                null,
                null,
                null
        );
    }

    public AnalysisResultDTO(
            String sessionId,
            String target,
            String targetType,
            double riskScore,
            String riskLevel,
            String verdict,
            List<String> indicators,
            Double ruleScore,
            Double knnScore,
            Double probabilityScore,
            SessionFeatureVectorDTO features) {

        this.sessionId = sessionId;
        this.target = target;
        this.targetType = targetType;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.verdict = verdict;
        this.indicators = indicators;
        this.ruleScore = ruleScore;
        this.knnScore = knnScore;
        this.probabilityScore = probabilityScore;
        this.features = features;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getTarget() {
        return target;
    }

    public String getTargetType() {
        return targetType;
    }

    public double getRiskScore() {
        return riskScore;
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

    public Double getRuleScore() {
        return ruleScore;
    }

    public Double getKnnScore() {
        return knnScore;
    }

    public Double getProbabilityScore() {
        return probabilityScore;
    }

    public SessionFeatureVectorDTO getFeatures() {
        return features;
    }
}
