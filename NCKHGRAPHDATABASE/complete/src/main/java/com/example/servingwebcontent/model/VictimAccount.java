package com.example.servingwebcontent.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Node VictimAccount
 * Đại diện tài khoản nạn nhân
 */
@Node("VictimAccount")
public class VictimAccount {

    @Id
    @Property("username")
    private String username;

    @Property("email")
    private String email;

    @Property("status")
    private String status;

    @Property("indicators")
    private List<String> indicators;

    /**
     * Victim nhận email
     *
     * VictimAccount --RECEIVED--> Email
     */
    @Relationship(type = "RECEIVED")
    private List<Email> receivedEmails = new ArrayList<>();

    // ================= CONSTRUCTOR =================

    public VictimAccount() {
        this.status = "SAFE";
        this.indicators = new ArrayList<>();
    }

    public VictimAccount(String username) {
        this();
        setUsername(username);
    }

    // ================= NORMALIZE =================

    private static String normalize(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        return s.isEmpty() ? null : s;
    }

    private static String normalizeStatus(String raw) {

        if (raw == null) return "SAFE";

        String s = raw.trim().toUpperCase();

        if (s.equals("SAFE") || s.equals("COMPROMISED"))
            return s;

        return "SAFE";
    }

    // ================= GET / SET =================

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = normalize(username);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalize(email);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = normalizeStatus(status);
    }

    public List<String> getIndicators() {
        return indicators;
    }

    public void setIndicators(List<String> indicators) {
        this.indicators = indicators == null ? new ArrayList<>() : indicators;
    }

    public List<Email> getReceivedEmails() {
        return receivedEmails;
    }

    public void setReceivedEmails(List<Email> receivedEmails) {
        this.receivedEmails = receivedEmails;
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

    public void addEmail(Email email) {

        if (email == null) return;

        if (!this.receivedEmails.contains(email))
            this.receivedEmails.add(email);
    }

    // ================= EQUALS / HASH =================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VictimAccount)) return false;
        VictimAccount that = (VictimAccount) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}