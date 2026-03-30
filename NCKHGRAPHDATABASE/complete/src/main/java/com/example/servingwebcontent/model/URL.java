package com.example.servingwebcontent.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Node URL – Phishing / Impersonation target
 * Chỉ phản ánh risk nội tại của URL
 */
@Node("URL")
public class URL {

    /**
     * URL đầy đủ – định danh duy nhất
     */
    @Id
    @Property("url")
    private String url;

    /**
     * Domain trích từ URL
     */
    @Property("domain")
    private String domain;

    /**
     * VALID | SUSPICIOUS | FAKE
     */
    @Property("status")
    private String status;

    /**
     * Base risk của node URL (0–100)
     */
    @Property("baseRisk")
    private int baseRisk;

    /**
     * Danh sách indicator
     */
    @Property("indicators")
    private List<String> indicators;

    // ================= GRAPH RELATIONSHIP =================

    /**
     * URL được host trên domain nào
     *
     * URL --HOSTED_ON--> Domain
     */
    @Relationship(type = "HOSTED_ON")
    private Domain domainNode;

    // ================= CONSTRUCTOR =================

    public URL() {
        this.status = "VALID";
        this.baseRisk = 0;
        this.indicators = new ArrayList<>();
    }

    public URL(String url) {
        this();
        setUrl(url);
    }

    // ================= NORMALIZE =================

    private static String normalizeUrl(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        return s.isEmpty() ? null : s;
    }

    private static String normalizeStatus(String raw) {
        if (raw == null) return "VALID";
        String s = raw.trim().toUpperCase();
        return List.of("VALID", "SUSPICIOUS", "FAKE").contains(s)
                ? s
                : "VALID";
    }

    /**
     * Extract domain
     */
    private static String extractDomain(String rawUrl) {
        if (rawUrl == null) return null;
        String u = rawUrl.trim();

        u = u.replaceFirst("^(?i)https?://", "");
        u = u.replaceFirst("^(?i)www\\.", "");

        int cut = u.indexOf("/");
        if (cut != -1) u = u.substring(0, cut);

        cut = u.indexOf("?");
        if (cut != -1) u = u.substring(0, cut);

        cut = u.indexOf("#");
        if (cut != -1) u = u.substring(0, cut);

        return u.trim().toLowerCase();
    }

    // ================= GET / SET =================

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = normalizeUrl(url);
        this.domain = extractDomain(this.url);
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = (domain == null) ? null : domain.trim().toLowerCase();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = normalizeStatus(status);
    }

    public int getBaseRisk() {
        return baseRisk;
    }

    public void setBaseRisk(int baseRisk) {
        this.baseRisk = Math.max(0, Math.min(100, baseRisk));
    }

    public List<String> getIndicators() {
        return indicators;
    }

    public void setIndicators(List<String> indicators) {
        this.indicators = (indicators == null) ? new ArrayList<>() : indicators;
    }

    public Domain getDomainNode() {
        return domainNode;
    }

    public void setDomainNode(Domain domainNode) {
        this.domainNode = domainNode;
    }

    // ================= UTILS =================

    public void addIndicator(String indicator) {
        if (indicator == null) return;

        if (this.indicators == null)
            this.indicators = new ArrayList<>();

        String ind = indicator.trim().toUpperCase();

        if (!this.indicators.contains(ind)) {
            this.indicators.add(ind);
        }
    }

    // ================= EQUALS / HASH =================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof URL)) return false;
        URL that = (URL) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}