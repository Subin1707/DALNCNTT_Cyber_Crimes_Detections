package com.example.servingwebcontent.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * OutputDTO
 *
 * Output chuẩn cho Fraud Analysis Engine
 * DTO thuần – KHÔNG chứa logic phân tích
 *
 * Dùng cho:
 * - FraudAnalysisService
 * - Controller API response
 * - Graph risk evaluation
 */
public class OutputDTO {

    /**
     * Kết luận cuối
     * AN TOÀN / ĐÁNG NGHI NGỜ / GIAN LẬN
     */
    private String verdict;

    /**
     * loại engine tạo kết quả
     * BASE_ENGINE / GRAPH_RISK_ENGINE
     */
    private String scamType;

    /**
     * tổng điểm rủi ro
     */
    private int riskScore;

    /**
     * mức độ rủi ro
     * low / medium / high
     */
    private String riskLevel;

    /**
     * trạng thái entity
     * valid / suspicious / fake / invalid
     */
    private String status;

    /**
     * các chỉ báo phát hiện
     */
    private List<String> indicators;

    /* ================= CONSTRUCTORS ================= */

    public OutputDTO() {

        this.scamType = "GRAPH_RISK_ENGINE";
        this.riskScore = 0;
        this.riskLevel = "low";
        this.status = "valid";
        this.verdict = "AN TOÀN";
        this.indicators = new ArrayList<>();
    }

    public OutputDTO(String verdict,
                     String scamType,
                     int riskScore,
                     String riskLevel,
                     String status,
                     List<String> indicators) {

        setScamType(scamType);
        setRiskScore(riskScore);
        setRiskLevel(riskLevel);
        setStatus(status);
        setIndicators(indicators);
        setVerdict(verdict);
    }

    /* ================= FACTORY METHODS ================= */

    public static OutputDTO safe() {

        OutputDTO dto = new OutputDTO();

        dto.setScamType("RULE_ENGINE");
        dto.setStatus("valid");
        dto.setRiskScore(0);

        return dto;
    }

    public static OutputDTO suspicious(String indicator) {

        OutputDTO dto = new OutputDTO();

        dto.setScamType("RULE_ENGINE");
        dto.setStatus("suspicious");
        dto.setRiskScore(50);

        dto.addIndicator(indicator);

        return dto;
    }

    public static OutputDTO invalid(String indicator) {

        OutputDTO dto = new OutputDTO();

        dto.setScamType("RULE_ENGINE");
        dto.setStatus("invalid");
        dto.setRiskScore(70);

        dto.addIndicator(indicator);

        return dto;
    }

    /* ================= NORMALIZE HELPERS ================= */

    private static String normalizeText(String raw) {

        if (raw == null) return null;

        String s = raw.trim();

        return s.isEmpty() ? null : s;
    }

    private static String normalizeRiskLevel(String raw) {

        String s = normalizeText(raw);

        if (s == null) return "low";

        s = s.toLowerCase();

        return switch (s) {

            case "low", "medium", "high" -> s;

            default -> "low";
        };
    }

    private static String normalizeStatus(String raw) {

        String s = normalizeText(raw);

        if (s == null) return "valid";

        s = s.toLowerCase();

        return switch (s) {

            case "valid", "suspicious", "fake", "invalid" -> s;

            default -> "valid";
        };
    }

    private static String autoVerdictFromStatus(String status) {

        if (status == null) return "AN TOÀN";

        return switch (status.toLowerCase()) {

            case "fake", "invalid" ->
                    "GIAN LẬN";

            case "suspicious" ->
                    "ĐÁNG NGHI NGỜ";

            default ->
                    "AN TOÀN";
        };
    }

    /* ================= GET / SET ================= */

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {

        String v = normalizeText(verdict);

        this.verdict = (v == null)
                ? autoVerdictFromStatus(this.status)
                : v;
    }

    public String getScamType() {
        return scamType;
    }

    public void setScamType(String scamType) {

        String s = normalizeText(scamType);

        this.scamType = (s == null)
                ? "GRAPH_RISK_ENGINE"
                : s;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {

        this.riskScore = riskScore;

        if (riskScore >= 70) this.riskLevel = "high";
        else if (riskScore >= 40) this.riskLevel = "medium";
        else this.riskLevel = "low";
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {

        this.riskLevel = normalizeRiskLevel(riskLevel);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {

        this.status = normalizeStatus(status);

        this.verdict = autoVerdictFromStatus(this.status);
    }

    public List<String> getIndicators() {

        if (indicators == null)
            indicators = new ArrayList<>();

        return indicators;
    }

    public void setIndicators(List<String> indicators) {

        this.indicators = (indicators == null)
                ? new ArrayList<>()
                : new ArrayList<>(indicators);
    }

    /* ================= HELPER ================= */

    public void addIndicator(String indicator) {

        if (indicator == null) return;

        getIndicators().add(indicator);
    }

    public void merge(OutputDTO other) {

        if (other == null) return;

        this.riskScore += other.riskScore;

        if (this.riskScore > 100)
            this.riskScore = 100;

        this.getIndicators().addAll(other.getIndicators());

        setRiskScore(this.riskScore);
    }
}