package com.example.servingwebcontent.dto;

public class FraudInputDTO {

    private String email;
    private String ip;
    private String url;
    private String domain;
    private String fileNode;
    private String fileHash;
    private String victimAccount;

    private String sessionId;

    public FraudInputDTO() {}

    public FraudInputDTO(String email, String ip, String url) {
        setEmail(email);
        setIp(ip);
        setUrl(url);
    }

    public FraudInputDTO(String email,
                         String ip,
                         String url,
                         String domain,
                         String fileNode,
                         String fileHash,
                         String victimAccount,
                         String sessionId) {

        setEmail(email);
        setIp(ip);
        setUrl(url);
        setDomain(domain);
        setFileNode(fileNode);
        setFileHash(fileHash);
        setVictimAccount(victimAccount);
        setSessionId(sessionId);
    }

    /* ================= GETTERS / SETTERS ================= */

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        String s = normalize(email);
        this.email = (s == null) ? null : s.toLowerCase();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = normalize(ip);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        String s = normalize(url);
        this.url = (s == null) ? null : s.toLowerCase();
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        String s = normalize(domain);
        this.domain = (s == null) ? null : s.toLowerCase();
    }

    public String getFileNode() {
        return fileNode;
    }

    public void setFileNode(String fileNode) {
        this.fileNode = normalize(fileNode);
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        String s = normalize(fileHash);
        this.fileHash = (s == null) ? null : s.toLowerCase();
    }

    public String getVictimAccount() {
        return victimAccount;
    }

    public void setVictimAccount(String victimAccount) {
        this.victimAccount = normalize(victimAccount);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = normalize(sessionId);
    }

    /* ================= UTILS ================= */

    /**
     * Kiểm tra input có rỗng hoàn toàn không
     */
    public boolean isEmpty() {
        return email == null &&
               ip == null &&
               url == null &&
               domain == null &&
               fileNode == null &&
               fileHash == null &&
               victimAccount == null;
    }

    /**
     * Normalize text input
     */
    private static String normalize(String raw) {

        if (raw == null) return null;

        String s = raw.trim();

        if (s.isEmpty()) return null;

        return s;
    }
}
