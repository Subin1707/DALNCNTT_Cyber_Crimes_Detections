package com.example.servingwebcontent.dto;

import java.util.ArrayList;
import java.util.List;

public class UpdateNodeRequest {

    private String value;
    private String status;
    private String riskLevel;
    private Integer riskScore;
    private String verdict;
    private List<String> indicators;

    public UpdateNodeRequest() {
        this.indicators = new ArrayList<>();
    }

    public UpdateNodeRequest(String value,
                             String status,
                             String riskLevel,
                             Integer riskScore,
                             String verdict,
                             List<String> indicators) {

        setValue(value);
        setStatus(status);
        setRiskLevel(riskLevel);
        setRiskScore(riskScore);
        setVerdict(verdict);
        setIndicators(indicators);
    }

    /* ================= NORMALIZE ================= */

    private static String normalizeText(String raw) {

        if (raw == null) return null;

        String s = raw.trim();

        return s.isEmpty() ? null : s;
    }

    private static String normalizeStatus(String raw) {

        String s = normalizeText(raw);

        if (s == null) return null;

        s = s.toLowerCase();

        return switch (s) {

            case "valid",
                 "suspicious",
                 "fake" -> s;

            case "malicious" -> "fake";

            default -> null;
        };
    }

    private static String normalizeRiskLevel(String raw) {

        String s = normalizeText(raw);

        if (s == null) return null;

        s = s.toLowerCase();

        return switch (s) {

            case "low",
                 "medium",
                 "high" -> s;

            case "1" -> "low";
            case "2" -> "medium";
            case "3" -> "high";

            default -> null;
        };
    }

    /* ================= GET / SET ================= */

    public String getValue() {
        return value;
    }

    public void setValue(String value) {

        String s = normalizeText(value);

        if (s == null) {
            this.value = null;
            return;
        }

        // email / url -> lowercase
        if (s.contains("@") || s.contains("http") || s.contains("www")) {
            this.value = s.toLowerCase();
        } else {
            this.value = s; // IP giữ nguyên
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = normalizeStatus(status);
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = normalizeRiskLevel(riskLevel);
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = normalizeText(verdict);
    }

    public List<String> getIndicators() {

        if (indicators == null) {
            indicators = new ArrayList<>();
        }

        return indicators;
    }

    public void setIndicators(List<String> indicators) {

        this.indicators =
                (indicators == null)
                        ? new ArrayList<>()
                        : new ArrayList<>(indicators);
    }
}