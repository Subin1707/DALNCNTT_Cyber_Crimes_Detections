package com.example.servingwebcontent.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GraphNodeDTO {

    private String id;
    private String sessionId;
    private String type;
    private String value;

    private String status;
    private String riskLevel;
    private int riskScore;

    private String verdict;
    private List<String> indicators;
    private String source;
    private String domainAssignment;  // "safe", "suspicious", "fraud"

    private boolean manualBlocked;
    private String manualBlockReason;
    private String manualBlockedBy;
    private String manualBlockedAt;

    private int degree;
    private double centerDistance;
    private double relationStrength;
    private double clusterDensity;
    private boolean superNode;
    private boolean missingData;
    private double propagationScore;
    private double graphRiskScore;
    private double confidence;
    private String membershipStatus;
    private String recommendedAction;
    private List<String> graphReasons;
    private int relationGraphDegree;
    private int overlapNeighborCount;
    private double overlapScore;
    private double weightedOverlapScore;
    private double adjustedOverlapScore;
    private String communityId;
    private String domainCenterId;
    private String domainRole;
    private boolean multiDomainOverlap;
    private Map<String, Double> domainDistances;
    private Map<String, Double> domainAffinities;
    private Map<String, Double> softMemberships;
    private Map<String, Double> domainInfluence;
    private Map<String, Double> featureVector;
    private boolean bridgeNode;
    private boolean outlierNode;
    private int evidenceFeatureCount;
    private String influenceZone;
    private String nodeClassification;
    private double internalRisk;
    private double externalRisk;

    public GraphNodeDTO() {
        this.status = "valid";
        this.riskLevel = "low";
        this.riskScore = 0;
        this.verdict = "AN TOÀN";
        this.indicators = new ArrayList<>();
        this.manualBlocked = false;
        this.membershipStatus = "IN_REGION";
        this.recommendedAction = "monitor";
        this.graphReasons = new ArrayList<>();
        this.domainDistances = new LinkedHashMap<>();
        this.domainAffinities = new LinkedHashMap<>();
        this.softMemberships = new LinkedHashMap<>();
        this.domainInfluence = new LinkedHashMap<>();
        this.featureVector = new LinkedHashMap<>();
        this.influenceZone = "OUTLIER";
        this.nodeClassification = "OUTSIDE_NODE";
    }

    public GraphNodeDTO(String id,
                        String sessionId,
                        String type,
                        String value,
                        String status,
                        String riskLevel,
                        Integer riskScore,
                        String verdict,
                        List<String> indicators) {

        this(id, sessionId, type, value, status, riskLevel, riskScore, verdict, indicators, null);
    }

    public GraphNodeDTO(String id,
                        String sessionId,
                        String type,
                        String value,
                        String status,
                        String riskLevel,
                        Integer riskScore,
                        String verdict,
                        List<String> indicators,
                        String source) {

        this.id = normalize(id);
        this.sessionId = normalize(sessionId);
        this.type = normalizeType(type);
        this.value = normalize(value);

        this.status = normalizeStatus(status);
        this.riskLevel = normalizeRiskLevel(riskLevel);
        this.riskScore = (riskScore != null && riskScore >= 0) ? riskScore : 0;

        this.indicators = (indicators != null)
                ? new ArrayList<>(indicators)
                : new ArrayList<>();

        this.verdict = normalizeVerdict(verdict, this.status);

        this.source = normalize(source);

        this.manualBlocked = false;
        this.manualBlockReason = null;
        this.manualBlockedBy = null;
        this.manualBlockedAt = null;
        this.membershipStatus = "IN_REGION";
        this.recommendedAction = "monitor";
        this.graphReasons = new ArrayList<>();
        this.domainDistances = new LinkedHashMap<>();
        this.domainAffinities = new LinkedHashMap<>();
        this.softMemberships = new LinkedHashMap<>();
        this.domainInfluence = new LinkedHashMap<>();
        this.featureVector = new LinkedHashMap<>();
        this.influenceZone = "OUTLIER";
        this.nodeClassification = "OUTSIDE_NODE";
    }

    public GraphNodeDTO(String id,
                        String sessionId,
                        String type,
                        String value,
                        String status,
                        String riskLevel,
                        Integer riskScore,
                        String verdict,
                        List<String> indicators,
                        String source,
                        Boolean manualBlocked,
                        String manualBlockReason,
                        String manualBlockedBy,
                        String manualBlockedAt) {

        this(id, sessionId, type, value, status, riskLevel, riskScore, verdict, indicators, source);

        this.manualBlocked = manualBlocked != null && manualBlocked;
        this.manualBlockReason = normalize(manualBlockReason);
        this.manualBlockedBy = normalize(manualBlockedBy);
        this.manualBlockedAt = normalize(manualBlockedAt);
        this.domainAssignment = calculateDomainAssignment(this.riskLevel);
    }

    public GraphNodeDTO(String id,
                        String sessionId,
                        String type,
                        String value,
                        String status,
                        String riskLevel,
                        Integer riskScore,
                        String verdict,
                        List<String> indicators,
                        String source,
                        String domainAssignment,
                        Boolean manualBlocked,
                        String manualBlockReason,
                        String manualBlockedBy,
                        String manualBlockedAt) {

        this(id, sessionId, type, value, status, riskLevel, riskScore, verdict, indicators, source, manualBlocked, manualBlockReason, manualBlockedBy, manualBlockedAt);
        this.domainAssignment = normalize(domainAssignment);
    }

    /* ================= NORMALIZE ================= */

    private static String normalize(String raw) {
        if (raw == null) return null;

        String s = raw.trim();
        return s.isEmpty() ? null : s;
    }

    private static String normalizeType(String raw) {

        String s = normalize(raw);
        if (s == null) return "Email";

        switch (s.toUpperCase()) {

            case "EMAIL":
                return "Email";

            case "IP":
            case "IPADDRESS":
            case "IP_ADDRESS":
                return "IPAddress";

            case "URL":
                return "URL";

            case "DOMAIN":
                return "Domain";

            case "FILENODE":
            case "FILE":
                return "FileNode";

            case "FILEHASH":
            case "HASH":
                return "FileHash";

            case "VICTIMACCOUNT":
            case "ACCOUNT":
                return "VictimAccount";

            case "ANALYSISSESSION":
                return "AnalysisSession";

            default:
                return s;
        }
    }

    private static String normalizeStatus(String raw) {

        String s = normalize(raw);
        if (s == null) return "valid";

        switch (s.toLowerCase()) {
            case "valid":
            case "suspicious":
            case "fake":
                return s.toLowerCase();
            default:
                return "valid";
        }
    }

    private static String normalizeRiskLevel(String raw) {

        String s = normalize(raw);
        if (s == null) return "low";

        switch (s.toLowerCase()) {
            case "low":
            case "medium":
            case "high":
                return s.toLowerCase();
            default:
                return "low";
        }
    }

    private static String normalizeVerdict(String raw, String status) {

        if (raw != null && !raw.isBlank()) {
            return raw.trim();
        }

        return autoVerdictFromStatus(status);
    }

    private static String autoVerdictFromStatus(String status) {

        if (status == null) return "AN TOÀN";

        switch (status) {

            case "fake":
                return "GIAN LẬN";

            case "suspicious":
                return "ĐÁNG NGHI NGỜ";

            default:
                return "AN TOÀN";
        }
    }

    /* ================= GETTERS ================= */

    public String getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getStatus() {
        return status;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public String getVerdict() {
        return verdict;
    }

    public List<String> getIndicators() {
        return indicators;
    }

    public String getSource() {
        return source;
    }

    public boolean isManualBlocked() {
        return manualBlocked;
    }

    public String getManualBlockReason() {
        return manualBlockReason;
    }

    public String getManualBlockedBy() {
        return manualBlockedBy;
    }

    public String getManualBlockedAt() {
        return manualBlockedAt;
    }

    public String getDomainAssignment() {
        return domainAssignment;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = Math.max(0, degree);
    }

    public double getCenterDistance() {
        return centerDistance;
    }

    public void setCenterDistance(double centerDistance) {
        this.centerDistance = centerDistance;
    }

    public double getRelationStrength() {
        return relationStrength;
    }

    public void setRelationStrength(double relationStrength) {
        this.relationStrength = relationStrength;
    }

    public double getClusterDensity() {
        return clusterDensity;
    }

    public void setClusterDensity(double clusterDensity) {
        this.clusterDensity = clusterDensity;
    }

    public boolean isSuperNode() {
        return superNode;
    }

    public void setSuperNode(boolean superNode) {
        this.superNode = superNode;
    }

    public boolean isMissingData() {
        return missingData;
    }

    public void setMissingData(boolean missingData) {
        this.missingData = missingData;
    }

    public double getPropagationScore() {
        return propagationScore;
    }

    public void setPropagationScore(double propagationScore) {
        this.propagationScore = propagationScore;
    }

    public double getGraphRiskScore() {
        return graphRiskScore;
    }

    public void setGraphRiskScore(double graphRiskScore) {
        this.graphRiskScore = graphRiskScore;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getMembershipStatus() {
        return membershipStatus;
    }

    public void setMembershipStatus(String membershipStatus) {
        this.membershipStatus = normalize(membershipStatus);
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = normalize(recommendedAction);
    }

    public List<String> getGraphReasons() {
        return graphReasons;
    }

    public void setGraphReasons(List<String> graphReasons) {
        this.graphReasons = graphReasons != null ? new ArrayList<>(graphReasons) : new ArrayList<>();
    }

    public int getRelationGraphDegree() {
        return relationGraphDegree;
    }

    public void setRelationGraphDegree(int relationGraphDegree) {
        this.relationGraphDegree = Math.max(0, relationGraphDegree);
    }

    public int getOverlapNeighborCount() {
        return overlapNeighborCount;
    }

    public void setOverlapNeighborCount(int overlapNeighborCount) {
        this.overlapNeighborCount = Math.max(0, overlapNeighborCount);
    }

    public double getOverlapScore() {
        return overlapScore;
    }

    public void setOverlapScore(double overlapScore) {
        this.overlapScore = overlapScore;
    }

    public double getWeightedOverlapScore() {
        return weightedOverlapScore;
    }

    public void setWeightedOverlapScore(double weightedOverlapScore) {
        this.weightedOverlapScore = weightedOverlapScore;
    }

    public double getAdjustedOverlapScore() {
        return adjustedOverlapScore;
    }

    public void setAdjustedOverlapScore(double adjustedOverlapScore) {
        this.adjustedOverlapScore = adjustedOverlapScore;
    }

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = normalize(communityId);
    }

    public String getDomainCenterId() {
        return domainCenterId;
    }

    public void setDomainCenterId(String domainCenterId) {
        this.domainCenterId = normalize(domainCenterId);
    }

    public String getDomainRole() {
        return domainRole;
    }

    public void setDomainRole(String domainRole) {
        this.domainRole = normalize(domainRole);
    }

    public boolean isMultiDomainOverlap() {
        return multiDomainOverlap;
    }

    public void setMultiDomainOverlap(boolean multiDomainOverlap) {
        this.multiDomainOverlap = multiDomainOverlap;
    }

    public Map<String, Double> getDomainDistances() {
        return domainDistances;
    }

    public void setDomainDistances(Map<String, Double> domainDistances) {
        this.domainDistances = domainDistances != null ? new LinkedHashMap<>(domainDistances) : new LinkedHashMap<>();
    }

    public Map<String, Double> getDomainAffinities() {
        return domainAffinities;
    }

    public void setDomainAffinities(Map<String, Double> domainAffinities) {
        this.domainAffinities = domainAffinities != null ? new LinkedHashMap<>(domainAffinities) : new LinkedHashMap<>();
    }

    public Map<String, Double> getSoftMemberships() {
        return softMemberships;
    }

    public void setSoftMemberships(Map<String, Double> softMemberships) {
        this.softMemberships = softMemberships != null ? new LinkedHashMap<>(softMemberships) : new LinkedHashMap<>();
    }

    public Map<String, Double> getDomainInfluence() {
        return domainInfluence;
    }

    public void setDomainInfluence(Map<String, Double> domainInfluence) {
        this.domainInfluence = domainInfluence != null ? new LinkedHashMap<>(domainInfluence) : new LinkedHashMap<>();
    }

    public Map<String, Double> getFeatureVector() {
        return featureVector;
    }

    public void setFeatureVector(Map<String, Double> featureVector) {
        this.featureVector = featureVector != null ? new LinkedHashMap<>(featureVector) : new LinkedHashMap<>();
    }

    public boolean isBridgeNode() {
        return bridgeNode;
    }

    public void setBridgeNode(boolean bridgeNode) {
        this.bridgeNode = bridgeNode;
    }

    public boolean isOutlierNode() {
        return outlierNode;
    }

    public void setOutlierNode(boolean outlierNode) {
        this.outlierNode = outlierNode;
    }

    public int getEvidenceFeatureCount() {
        return evidenceFeatureCount;
    }

    public void setEvidenceFeatureCount(int evidenceFeatureCount) {
        this.evidenceFeatureCount = Math.max(0, evidenceFeatureCount);
    }

    public String getInfluenceZone() {
        return influenceZone;
    }

    public void setInfluenceZone(String influenceZone) {
        this.influenceZone = normalize(influenceZone);
    }

    public String getNodeClassification() {
        return nodeClassification;
    }

    public void setNodeClassification(String nodeClassification) {
        this.nodeClassification = normalize(nodeClassification);
    }

    public double getInternalRisk() {
        return internalRisk;
    }

    public void setInternalRisk(double internalRisk) {
        this.internalRisk = internalRisk;
    }

    public double getExternalRisk() {
        return externalRisk;
    }

    public void setExternalRisk(double externalRisk) {
        this.externalRisk = externalRisk;
    }

    private static String calculateDomainAssignment(String riskLevel) {
        if (riskLevel == null) return "safe";
        String normalized = riskLevel.toLowerCase().trim();
        return switch(normalized) {
            case "high" -> "fraud";
            case "medium" -> "suspicious";
            default -> "safe";
        };
    }

}
