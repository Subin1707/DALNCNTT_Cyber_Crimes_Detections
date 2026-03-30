package com.example.servingwebcontent.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Node Email trong graph fraud
 * - Chỉ phản ánh risk nội tại của email
 * - KHÔNG phải kết luận gian lận cuối cùng
 */
@Node("Email")
public class Email {

    /**
     * Email address – định danh duy nhất
     */
    @Id
    @Property("email")
    private String email;

    /**
     * Domain trích từ email
     */
    @Property("domain")
    private String domain;

    /**
     * VALID | SUSPICIOUS | FAKE
     */
    @Property("status")
    private String status;

    /**
     * Base risk của node Email (0–100)
     */
    @Property("baseRisk")
    private int baseRisk;

    /**
     * Indicator list
     */
    @Property("indicators")
    private List<String> indicators;

    // ================= GRAPH RELATIONSHIPS =================

    /**
     * Email gửi từ IP nào
     *
     * Email --SENT_FROM_IP--> IPAddress
     */
    @Relationship(type = "SENT_FROM_IP")
    private IPAddress sentFrom;

    /**
     * Email chứa các URL
     *
     * Email --CONTAINS_URL--> URL
     */
    @Relationship(type = "CONTAINS_URL")
    private List<URL> urls = new ArrayList<>();

    // ================= CONSTRUCTOR =================

    public Email() {
        this.status = "VALID";
        this.baseRisk = 0;
        this.indicators = new ArrayList<>();
    }

    public Email(String email) {
        this();
        setEmail(email);
    }

    // ================= NORMALIZE =================

    private static String normalizeEmail(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        return s.isEmpty() ? null : s;
    }

    private static String extractDomain(String email) {
        if (email == null) return null;
        int at = email.indexOf("@");
        if (at < 0 || at == email.length() - 1) return null;
        return email.substring(at + 1).trim().toLowerCase();
    }

    private static String normalizeStatus(String raw) {
        if (raw == null) return "VALID";
        String s = raw.trim().toUpperCase();
        return List.of("VALID", "SUSPICIOUS", "FAKE").contains(s)
                ? s
                : "VALID";
    }

    // ================= GET / SET =================

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalizeEmail(email);
        this.domain = extractDomain(this.email);
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

    public IPAddress getSentFrom() {
        return sentFrom;
    }

    public void setSentFrom(IPAddress sentFrom) {
        this.sentFrom = sentFrom;
    }

    public List<URL> getUrls() {
        return urls;
    }

    public void setUrls(List<URL> urls) {
        this.urls = urls;
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

    public void addUrl(URL url) {
        if (url == null) return;

        if (this.urls == null)
            this.urls = new ArrayList<>();

        if (!this.urls.contains(url)) {
            this.urls.add(url);
        }
    }

    // ================= EQUALS / HASH =================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email)) return false;
        Email email1 = (Email) o;
        return Objects.equals(email, email1.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}