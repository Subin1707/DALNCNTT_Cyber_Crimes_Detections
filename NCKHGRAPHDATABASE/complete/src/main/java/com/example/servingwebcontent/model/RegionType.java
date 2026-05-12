package com.example.servingwebcontent.model;

/**
 * Security Region Type Enum
 * Represents behavioral zones in fraud detection
 *
 * SAFE: Normal user behavior
 * SUSPICIOUS: Intermediate risk behaviors
 * FRAUD: Clearly fraudulent behaviors
 */
public enum RegionType {
    SAFE("SAFE", "Normal user behavior", 0.0, 0.33),
    SUSPICIOUS("SUSPICIOUS", "Intermediate risk behaviors", 0.33, 0.67),
    FRAUD("FRAUD", "Clearly fraudulent behaviors", 0.67, 1.0);

    private final String label;
    private final String description;
    private final double minScore;
    private final double maxScore;

    RegionType(String label, String description, double minScore, double maxScore) {
        this.label = label;
        this.description = description;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public double getMinScore() {
        return minScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    /**
     * Determine region type from risk score
     * @param riskScore Score between 0.0 and 1.0
     * @return Appropriate RegionType
     */
    public static RegionType fromScore(double riskScore) {
        if (riskScore < 0.33) {
            return SAFE;
        } else if (riskScore < 0.67) {
            return SUSPICIOUS;
        } else {
            return FRAUD;
        }
    }
}
