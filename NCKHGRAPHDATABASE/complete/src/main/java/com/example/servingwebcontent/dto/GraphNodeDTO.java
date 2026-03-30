package com.example.servingwebcontent.dto;

import java.util.ArrayList;
import java.util.List;

public class GraphNodeDTO {

    private String id;
    private String sessionId;
    private String type;
    private String value;

    private String status;
    private String riskLevel;
    private int riskScore;

    private String verdict;
    private List<String> indicators;
    private String source;

    public GraphNodeDTO() {
        this.status = "valid";
        this.riskLevel = "low";
        this.riskScore = 0;
        this.verdict = "AN TOÀN";
        this.indicators = new ArrayList<>();
    }

    public GraphNodeDTO(String id,
                        String sessionId,
                        String type,
                        String value,
                        String status,
                        String riskLevel,
                        Integer riskScore,
                        String verdict,
                        List<String> indicators) {

        this(id, sessionId, type, value, status, riskLevel, riskScore, verdict, indicators, null);
    }

    public GraphNodeDTO(String id,
                        String sessionId,
                        String type,
                        String value,
                        String status,
                        String riskLevel,
                        Integer riskScore,
                        String verdict,
                        List<String> indicators,
                        String source) {

        this.id = normalize(id);
        this.sessionId = normalize(sessionId);
        this.type = normalizeType(type);
        this.value = normalize(value);

        this.status = normalizeStatus(status);
        this.riskLevel = normalizeRiskLevel(riskLevel);
        this.riskScore = (riskScore != null && riskScore >= 0) ? riskScore : 0;

        this.indicators = (indicators != null)
                ? new ArrayList<>(indicators)
                : new ArrayList<>();

        this.verdict = normalizeVerdict(verdict, this.status);

        this.source = normalize(source);
    }

    /* ================= NORMALIZE ================= */

    private static String normalize(String raw) {
        if (raw == null) return null;

        String s = raw.trim();
        return s.isEmpty() ? null : s;
    }

    private static String normalizeType(String raw) {

        String s = normalize(raw);
        if (s == null) return "Email";

        switch (s.toUpperCase()) {

            case "EMAIL":
                return "Email";

            case "IP":
            case "IPADDRESS":
            case "IP_ADDRESS":
                return "IPAddress";

            case "URL":
                return "URL";

            case "DOMAIN":
                return "Domain";

            case "FILENODE":
            case "FILE":
                return "FileNode";

            case "FILEHASH":
            case "HASH":
                return "FileHash";

            case "VICTIMACCOUNT":
            case "ACCOUNT":
                return "VictimAccount";

            case "ANALYSISSESSION":
                return "AnalysisSession";

            default:
                return "Email";
        }
    }

    private static String normalizeStatus(String raw) {

        String s = normalize(raw);
        if (s == null) return "valid";

        switch (s.toLowerCase()) {
            case "valid":
            case "suspicious":
            case "fake":
                return s.toLowerCase();
            default:
                return "valid";
        }
    }

    private static String normalizeRiskLevel(String raw) {

        String s = normalize(raw);
        if (s == null) return "low";

        switch (s.toLowerCase()) {
            case "low":
            case "medium":
            case "high":
                return s.toLowerCase();
            default:
                return "low";
        }
    }

    private static String normalizeVerdict(String raw, String status) {

        if (raw != null && !raw.isBlank()) {
            return raw.trim();
        }

        return autoVerdictFromStatus(status);
    }

    private static String autoVerdictFromStatus(String status) {

        if (status == null) return "AN TOÀN";

        switch (status) {

            case "fake":
                return "GIAN LẬN";

            case "suspicious":
                return "ĐÁNG NGHI NGỜ";

            default:
                return "AN TOÀN";
        }
    }

    /* ================= GETTERS ================= */

    public String getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getStatus() {
        return status;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public String getVerdict() {
        return verdict;
    }

    public List<String> getIndicators() {
        return indicators;
    }

    public String getSource() {
        return source;
    }
}
