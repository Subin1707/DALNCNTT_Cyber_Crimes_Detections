package com.example.servingwebcontent.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Node IPAddress – Infrastructure layer
 * Chỉ phản ánh risk nội tại của IP
 */
@Node("IPAddress")
public class IPAddress {

    /**
     * IP address – định danh duy nhất
     */
    @Id
    @Property("ip")
    private String ip;

    /**
     * VALID | SUSPICIOUS | FAKE
     */
    @Property("status")
    private String status;

    /**
     * Base risk của node IP (0–100)
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
     * Domain nào resolve tới IP này
     *
     * Domain --RESOLVES_TO--> IP
     */
    @Relationship(type = "RESOLVES_TO", direction = Relationship.Direction.INCOMING)
    private List<Domain> domains = new ArrayList<>();

    // ================= CONSTRUCTOR =================

    public IPAddress() {
        this.status = "VALID";
        this.baseRisk = 0;
        this.indicators = new ArrayList<>();
    }

    public IPAddress(String ip) {
        this();
        setIp(ip);
    }

    // ================= NORMALIZE =================

    private static String normalizeIp(String raw) {
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

    // ================= GET / SET =================

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = normalizeIp(ip);
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

    public List<Domain> getDomains() {
        return domains;
    }

    public void setDomains(List<Domain> domains) {
        this.domains = domains;
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

    public void addDomain(Domain domain) {
        if (domain == null) return;

        if (this.domains == null)
            this.domains = new ArrayList<>();

        if (!this.domains.contains(domain)) {
            this.domains.add(domain);
        }
    }

    // ================= EQUALS / HASH =================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IPAddress)) return false;
        IPAddress that = (IPAddress) o;
        return Objects.equals(ip, that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip);
    }
}