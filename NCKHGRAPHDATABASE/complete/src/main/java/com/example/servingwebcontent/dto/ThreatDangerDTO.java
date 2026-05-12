package com.example.servingwebcontent.dto;

public class ThreatDangerDTO {
    private String danger;          // Mô tả mối đe dọa
    private String icon;            // Biểu tượng emoji
    private String color;           // Màu sắc hex
    private String severity;        // CRITICAL, HIGH, MEDIUM, LOW
    private String category;        // PHISHING, C2_COMMAND, MALWARE, v.v.

    public ThreatDangerDTO() {}

    public ThreatDangerDTO(String danger, String icon, String color, String severity, String category) {
        this.danger = danger;
        this.icon = icon;
        this.color = color;
        this.severity = severity;
        this.category = category;
    }

    // Getters and Setters
    public String getDanger() { return danger; }
    public void setDanger(String danger) { this.danger = danger; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
