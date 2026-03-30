package com.example.servingwebcontent.dto;

public class RowResultDTO {

    private int row;

    private String email;
    private String ip;
    private String url;
    private String domain;
    private String fileNode;
    private String fileHash;
    private String victimAccount;

    private boolean success;
    private String error;

    private String riskLevel;
    private String verdict;

    /* ===== SUCCESS ===== */

    public RowResultDTO(int row,
                        String email,
                        String ip,
                        String url,
                        String domain,
                        String fileNode,
                        String fileHash,
                        String victimAccount,
                        String riskLevel,
                        String verdict) {

        this.row = row;

        this.email = normalize(email);
        this.ip = normalize(ip);
        this.url = normalize(url);
        this.domain = normalize(domain);
        this.fileNode = normalize(fileNode);
        this.fileHash = normalize(fileHash);
        this.victimAccount = normalize(victimAccount);

        this.success = true;
        this.error = "";

        this.riskLevel = riskLevel;
        this.verdict = verdict;
    }

    /* ===== ERROR ===== */

    public RowResultDTO(int row, String error) {

        this.row = row;

        this.success = false;
        this.error = error;

        this.email = null;
        this.ip = null;
        this.url = null;
        this.domain = null;
        this.fileNode = null;
        this.fileHash = null;
        this.victimAccount = null;

        this.riskLevel = null;
        this.verdict = null;
    }

    /* ===== NORMALIZE ===== */

    private static String normalize(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    /* ===== GETTERS ===== */

    public int getRow() { return row; }

    public String getEmail() { return email; }

    public String getIp() { return ip; }

    public String getUrl() { return url; }

    public String getDomain() { return domain; }

    public String getFileNode() { return fileNode; }

    public String getFileHash() { return fileHash; }

    public String getVictimAccount() { return victimAccount; }

    public boolean isSuccess() { return success; }

    public String getError() { return error; }

    public String getRiskLevel() { return riskLevel; }

    public String getVerdict() { return verdict; }
}
