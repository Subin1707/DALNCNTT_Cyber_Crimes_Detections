package com.example.servingwebcontent.dto;

public class GraphLinkDTO {

    private String source;
    private String target;
    private String type;

    public GraphLinkDTO() {}

    public GraphLinkDTO(String source, String target, String type) {
        this.source = normalizeId(source);
        this.target = normalizeId(target);
        this.type = normalizeType(type);
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

            default:
                return "RELATED";
        }
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
}