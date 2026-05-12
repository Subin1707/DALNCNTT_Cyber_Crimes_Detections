package com.example.servingwebcontent.dto;

import java.io.Serializable;

/**
 * Behavioral Feature Vector for Hybrid Fraud Detection
 * Represents the behavioral characteristics of a user/node
 * Used for multi-distance KNN analysis and region classification
 */
public class BehaviorFeatureVector implements Serializable {

    private static final long serialVersionUID = 1L;

    // Numeric features
    private int ipCount;
    private int urlCount;
    private int emailCount;
    private int domainCount;
    private int failedLoginCount;
    private double requestFrequency;

    // Boolean features (security flags)
    private boolean vpn;
    private boolean blacklist;
    private boolean suspiciousUrl;
    private boolean torNetwork;
    private boolean spamPattern;
    private boolean abnormalAccessTime;

    // Constructor
    public BehaviorFeatureVector() {
    }

    // Full constructor
    public BehaviorFeatureVector(int ipCount, int urlCount, int emailCount, int domainCount,
                                  int failedLoginCount, double requestFrequency,
                                  boolean vpn, boolean blacklist, boolean suspiciousUrl,
                                  boolean torNetwork, boolean spamPattern, boolean abnormalAccessTime) {
        this.ipCount = Math.max(0, ipCount);
        this.urlCount = Math.max(0, urlCount);
        this.emailCount = Math.max(0, emailCount);
        this.domainCount = Math.max(0, domainCount);
        this.failedLoginCount = Math.max(0, failedLoginCount);
        this.requestFrequency = Math.max(0.0, requestFrequency);
        this.vpn = vpn;
        this.blacklist = blacklist;
        this.suspiciousUrl = suspiciousUrl;
        this.torNetwork = torNetwork;
        this.spamPattern = spamPattern;
        this.abnormalAccessTime = abnormalAccessTime;
    }

    // Numeric features getters/setters
    public int getIpCount() {
        return ipCount;
    }

    public void setIpCount(int ipCount) {
        this.ipCount = Math.max(0, ipCount);
    }

    public int getUrlCount() {
        return urlCount;
    }

    public void setUrlCount(int urlCount) {
        this.urlCount = Math.max(0, urlCount);
    }

    public int getEmailCount() {
        return emailCount;
    }

    public void setEmailCount(int emailCount) {
        this.emailCount = Math.max(0, emailCount);
    }

    public int getDomainCount() {
        return domainCount;
    }

    public void setDomainCount(int domainCount) {
        this.domainCount = Math.max(0, domainCount);
    }

    public int getFailedLoginCount() {
        return failedLoginCount;
    }

    public void setFailedLoginCount(int failedLoginCount) {
        this.failedLoginCount = Math.max(0, failedLoginCount);
    }

    public double getRequestFrequency() {
        return requestFrequency;
    }

    public void setRequestFrequency(double requestFrequency) {
        this.requestFrequency = Math.max(0.0, requestFrequency);
    }

    // Boolean features getters/setters
    public boolean isVpn() {
        return vpn;
    }

    public void setVpn(boolean vpn) {
        this.vpn = vpn;
    }

    public boolean isBlacklist() {
        return blacklist;
    }

    public void setBlacklist(boolean blacklist) {
        this.blacklist = blacklist;
    }

    public boolean isSuspiciousUrl() {
        return suspiciousUrl;
    }

    public void setSuspiciousUrl(boolean suspiciousUrl) {
        this.suspiciousUrl = suspiciousUrl;
    }

    public boolean isTorNetwork() {
        return torNetwork;
    }

    public void setTorNetwork(boolean torNetwork) {
        this.torNetwork = torNetwork;
    }

    public boolean isSpamPattern() {
        return spamPattern;
    }

    public void setSpamPattern(boolean spamPattern) {
        this.spamPattern = spamPattern;
    }

    public boolean isAbnormalAccessTime() {
        return abnormalAccessTime;
    }

    public void setAbnormalAccessTime(boolean abnormalAccessTime) {
        this.abnormalAccessTime = abnormalAccessTime;
    }

    // Convert to numeric array for distance calculations
    public double[] toNumericArray() {
        return new double[]{
                ipCount,
                urlCount,
                emailCount,
                domainCount,
                failedLoginCount,
                requestFrequency
        };
    }

    // Convert boolean features to boolean array
    public boolean[] toBooleanArray() {
        return new boolean[]{
                vpn,
                blacklist,
                suspiciousUrl,
                torNetwork,
                spamPattern,
                abnormalAccessTime
        };
    }

    @Override
    public String toString() {
        return "BehaviorFeatureVector{" +
                "ipCount=" + ipCount +
                ", urlCount=" + urlCount +
                ", emailCount=" + emailCount +
                ", domainCount=" + domainCount +
                ", failedLoginCount=" + failedLoginCount +
                ", requestFrequency=" + requestFrequency +
                ", vpn=" + vpn +
                ", blacklist=" + blacklist +
                ", suspiciousUrl=" + suspiciousUrl +
                ", torNetwork=" + torNetwork +
                ", spamPattern=" + spamPattern +
                ", abnormalAccessTime=" + abnormalAccessTime +
                '}';
    }
}
