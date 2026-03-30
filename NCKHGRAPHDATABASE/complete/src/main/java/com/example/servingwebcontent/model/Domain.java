package com.example.servingwebcontent.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Node Domain trong graph fraud
 * - Phản ánh độ rủi ro của domain
 * - Không phải kết luận gian lận cuối cùng
 */
@Node("Domain")
public class Domain {

    /**
     * Domain name – định danh duy nhất
     */
    @Id
    @Property("domain")
    private String domain;

    /**
     * Top level domain
     */
    @Property("tld")
    private String tld;

    /**
     * VALID | SUSPICIOUS | MALICIOUS
     */
    @Property("status")
    private String status;

    /**
     * Base risk (0–100)
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
     * URL được host trên domain này
     *
     * URL --HOSTED_ON--> Domain
     */
    @Relationship(type = "HOSTED_ON", direction = Relationship.Direction.INCOMING)
    private List<URL> urls = new ArrayList<>();

    /**
     * Domain resolve tới IP
     *
     * Domain --RESOLVES_TO--> IP
     */
    @Relationship(type = "RESOLVES_TO")
    private IPAddress resolvedIp;

    // ================= CONSTRUCTOR =================

    public Domain() {
        this.status = "VALID";
        this.baseRisk = 0;
        this.indicators = new ArrayList<>();
    }

    public Domain(String domain) {
        this();
        setDomain(domain);
    }

    // ================= NORMALIZE =================

    private static String normalizeDomain(String raw) {

        if (raw == null) return null;

        String s = raw.trim().toLowerCase();

        if (s.startsWith("http://"))
            s = s.substring(7);

        if (s.startsWith("https://"))
            s = s.substring(8);

        int slash = s.indexOf("/");
        if (slash > 0)
            s = s.substring(0, slash);

        return s.isEmpty() ? null : s;
    }

    private static String extractTLD(String domain) {

        if (domain == null) return null;

        int lastDot = domain.lastIndexOf(".");

        if (lastDot < 0 || lastDot == domain.length() - 1)
            return null;

        return domain.substring(lastDot + 1);
    }

    private static String normalizeStatus(String raw) {

        if (raw == null) return "VALID";

        String s = raw.trim().toUpperCase();

        if (s.equals("VALID") || s.equals("SUSPICIOUS") || s.equals("MALICIOUS"))
            return s;

        return "VALID";
    }

    // ================= GET / SET =================

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = normalizeDomain(domain);
        this.tld = extractTLD(this.domain);
    }

    public String getTld() {
        return tld;
    }

    public void setTld(String tld) {
        this.tld = (tld == null) ? null : tld.trim().toLowerCase();
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

    public List<URL> getUrls() {
        return urls;
    }

    public void setUrls(List<URL> urls) {
        this.urls = urls;
    }

    public IPAddress getResolvedIp() {
        return resolvedIp;
    }

    public void setResolvedIp(IPAddress resolvedIp) {
        this.resolvedIp = resolvedIp;
    }

    // ================= UTILS =================

    public void addIndicator(String indicator) {

        if (indicator == null) return;

        if (this.indicators == null)
            this.indicators = new ArrayList<>();

        String ind = indicator.trim().toUpperCase();

        if (!this.indicators.contains(ind))
            this.indicators.add(ind);
    }

    public void addUrl(URL url) {

        if (url == null) return;

        if (this.urls == null)
            this.urls = new ArrayList<>();

        if (!this.urls.contains(url))
            this.urls.add(url);
    }

    public void increaseRisk(int value) {
        setBaseRisk(this.baseRisk + value);
    }

    // ================= EQUALS / HASH =================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Domain)) return false;
        Domain domain1 = (Domain) o;
        return Objects.equals(domain, domain1.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain);
    }
}