package com.example.servingwebcontent.model;

/**
 * REGION TYPE
 * Phân loại miền hành vi trong hệ thống phát hiện gian lận
 *
 * SAFE        : Hành vi an toàn
 * SUSPICIOUS  : Hành vi bất thường
 * FRAUD       : Hành vi gian lận
 */
public enum RegionType {

    SAFE(
        "SAFE",
        "Normal user behavior",
        0.0,
        0.33,
        "#90EE90", // background
        "#228B22", // border
        "#1E90FF", // node
        "🟢"
    ),

    SUSPICIOUS(
        "SUSPICIOUS",
        "Intermediate risk behaviors",
        0.33,
        0.67,
        "#FFF4B3", // background
        "#FF8C00", // border
        "#FFA500", // node
        "🟠"
    ),

    FRAUD(
        "FRAUD",
        "Clearly fraudulent behaviors",
        0.67,
        1.0,
        "#FFC0CB", // background
        "#DC143C", // border
        "#8B0000", // node
        "🔴"
    );

    // =========================
    // ATTRIBUTES
    // =========================

    private final String label;
    private final String description;

    // Risk range
    private final double minScore;
    private final double maxScore;

    // Visualization
    private final String backgroundColor;
    private final String borderColor;
    private final String nodeColor;
    private final String icon;

    // =========================
    // CONSTRUCTOR
    // =========================

    RegionType(String label,
               String description,
               double minScore,
               double maxScore,
               String backgroundColor,
               String borderColor,
               String nodeColor,
               String icon) {

        this.label = label;
        this.description = description;
        this.minScore = minScore;
        this.maxScore = maxScore;

        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.nodeColor = nodeColor;
        this.icon = icon;
    }

    // =========================
    // LOGIC
    // =========================

    /**
     * Xác định miền theo risk score
     */
    public static RegionType fromScore(double riskScore) {

        if (riskScore < 0.33) {
            return SAFE;
        }

        if (riskScore < 0.67) {
            return SUSPICIOUS;
        }

        return FRAUD;
    }

    /**
     * Kiểm tra score có nằm trong miền không
     */
    public boolean contains(double riskScore) {
        return riskScore >= minScore && riskScore < maxScore;
    }

    /**
     * Miền nguy hiểm hơn
     */
    public boolean isMoreDangerousThan(RegionType other) {
        return this.maxScore > other.maxScore;
    }

    /**
     * Miền an toàn?
     */
    public boolean isSafe() {
        return this == SAFE;
    }

    /**
     * Miền nghi ngờ?
     */
    public boolean isSuspicious() {
        return this == SUSPICIOUS;
    }

    /**
     * Miền gian lận?
     */
    public boolean isFraud() {
        return this == FRAUD;
    }

    // =========================
    // GETTERS
    // =========================

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

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public String getNodeColor() {
        return nodeColor;
    }

    public String getIcon() {
        return icon;
    }

    // =========================
    // DISPLAY
    // =========================

    public String getDisplayName() {
        return icon + " " + label;
    }

    public String getColorDescription() {
        return String.format(
            "Background=%s | Border=%s | Node=%s",
            backgroundColor,
            borderColor,
            nodeColor
        );
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}