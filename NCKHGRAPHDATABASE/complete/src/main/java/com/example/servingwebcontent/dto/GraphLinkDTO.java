package com.example.servingwebcontent.dto;

public class GraphLinkDTO {

    private String source;
    private String target;
    private String type;
    private double weight;
    private double relationWeight;
    private double overlapScore;
    private double rawOverlap;
    private double adjustedOverlap;
    private String relationKind;

    public GraphLinkDTO() {}

    public GraphLinkDTO(String source, String target, String type) {
        this.source = normalizeId(source);
        this.target = normalizeId(target);
        this.type = normalizeType(type);
        this.weight = defaultWeight(this.type);
        this.relationWeight = this.weight;
        this.relationKind = "SESSION_GRAPH";
    }

    public static GraphLinkDTO overlap(String source,
                                       String target,
                                       double weight,
                                       double overlapScore,
                                       double rawOverlap,
                                       double adjustedOverlap) {
        GraphLinkDTO link = new GraphLinkDTO(source, target, "OVERLAP");
        link.weight = weight;
        link.relationWeight = weight;
        link.overlapScore = overlapScore;
        link.rawOverlap = rawOverlap;
        link.adjustedOverlap = adjustedOverlap;
        link.relationKind = "RELATION_GRAPH";
        return link;
    }

    /* ================= NORMALIZE ================= */

    private static String normalizeId(String raw) {
        if (raw == null) return null;

        String s = raw.trim();
        return s.isEmpty() ? null : s;
    }

    private static String normalizeType(String raw) {

        if (raw == null) return "RELATED";

        String s = raw.trim().toUpperCase();

        switch (s) {

            /* SESSION → ENTITY */

            case "HAS_EMAIL":
            case "EMAIL":
                return "HAS_EMAIL";

            case "HAS_IP":
            case "IP":
                return "HAS_IP";

            case "HAS_URL":
            case "URL":
                return "HAS_URL";

            case "HAS_DOMAIN":
            case "DOMAIN":
                return "HAS_DOMAIN";

            case "HAS_FILE":
            case "FILE":
                return "HAS_FILE";

            case "HAS_HASH":
            case "HASH":
                return "HAS_HASH";

            case "HAS_ACCOUNT":
            case "ACCOUNT":
            case "VICTIM_ACCOUNT":
                return "HAS_ACCOUNT";


            /* EMAIL / URL / INFRA */

            case "SENT_FROM_IP":
            case "ACCESS_FROM":
            case "ACCESS":
                return "SENT_FROM_IP";

            case "CONTAINS_URL":
            case "CONTAINS":
            case "LINKS_TO":
                return "CONTAINS_URL";

            case "HOSTED_ON":
            case "HOST_ON":
            case "HOSTED":
                return "HOSTED_ON";

            case "BELONGS_TO":
            case "DOMAIN_OF":
                return "BELONGS_TO";

            case "DOWNLOADS_FILE":
            case "DOWNLOADS":
                return "DOWNLOADS_FILE";

            case "FILE_HASH":
            case "HAS_FILE_HASH":
                return "HAS_FILE_HASH";

            case "VISITS":
            case "ACCESSES_URL":
                return "VISITS";

            case "OVERLAP":
            case "WEIGHTED_OVERLAP":
                return "OVERLAP";

            case "CO_OCCURS":
            case "CO_OCCURRENCE":
                return "CO_OCCURS";

            case "SAME_DEVICE":
                return "SAME_DEVICE";

            case "SAME_EMAIL":
                return "SAME_EMAIL";

            case "SAME_PHONE":
                return "SAME_PHONE";

            case "SAME_IP":
                return "SAME_IP";

            case "SAME_URL":
                return "SAME_URL";

            case "SAME_DOMAIN":
                return "SAME_DOMAIN";

            default:
                return "RELATED";
        }
    }

    private static double defaultWeight(String type) {
        if (type == null) return 0.35;
        return switch (type) {
            case "HAS_EMAIL", "HAS_ACCOUNT" -> 0.62;
            case "HAS_IP", "SENT_FROM_IP" -> 0.50;
            case "HAS_URL", "CONTAINS_URL", "VISITS" -> 0.40;
            case "HAS_DOMAIN", "HOSTED_ON", "BELONGS_TO" -> 0.30;
            case "HAS_FILE", "DOWNLOADS_FILE" -> 0.70;
            case "HAS_HASH", "HAS_FILE_HASH" -> 0.85;
            case "SAME_DEVICE" -> 0.95;
            case "SAME_EMAIL" -> 0.80;
            case "SAME_PHONE" -> 0.75;
            case "SAME_IP" -> 0.50;
            case "SAME_URL" -> 0.40;
            case "SAME_DOMAIN" -> 0.30;
            case "CO_OCCURS" -> 0.35;
            case "OVERLAP" -> 0.0;
            default -> 0.35;
        };
    }

    /* ================= GETTERS ================= */

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public String getType() {
        return type;
    }

    public double getWeight() {
        return weight;
    }

    public double getRelationWeight() {
        return relationWeight;
    }

    public double getOverlapScore() {
        return overlapScore;
    }

    public double getRawOverlap() {
        return rawOverlap;
    }

    public double getAdjustedOverlap() {
        return adjustedOverlap;
    }

    public String getRelationKind() {
        return relationKind;
    }
}
