package com.example.servingwebcontent.dto;

import java.util.List;

public class ChatbotResponseDTO {
    private String nodeId;
    private String nodeType; // IP, DOMAIN, URL, EMAIL, etc.
    private String nodeValue;

    // Layer 1: Analysis
    private String analysisDescription;
    private String status;

    // Layer 2: Risk Assessment
    private String riskAssessment;
    private int riskScore;
    private String riskLevel;

    // Layer 3: Danger/Threat Explanation
    private String threatExplanation;
    private List<String> specificDangers;
    private List<ThreatDangerDTO> specificDangersWithMetadata;  // ✅ Thêm metadata (icon, color, severity)

    // Layer 4: Specific Actions
    private List<String> recommendedActions;

    // Graph Intelligence
    private List<RelatedNodeDTO> relatedNodes;
    private String graphIntelligence;

    public ChatbotResponseDTO() {}

    public ChatbotResponseDTO(String nodeId, String nodeType, String nodeValue) {
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.nodeValue = nodeValue;
    }

    // Getters and Setters
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }

    public String getNodeValue() { return nodeValue; }
    public void setNodeValue(String nodeValue) { this.nodeValue = nodeValue; }

    public String getAnalysisDescription() { return analysisDescription; }
    public void setAnalysisDescription(String analysisDescription) { this.analysisDescription = analysisDescription; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRiskAssessment() { return riskAssessment; }
    public void setRiskAssessment(String riskAssessment) { this.riskAssessment = riskAssessment; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getThreatExplanation() { return threatExplanation; }
    public void setThreatExplanation(String threatExplanation) { this.threatExplanation = threatExplanation; }

    public List<String> getSpecificDangers() { return specificDangers; }
    public void setSpecificDangers(List<String> specificDangers) { this.specificDangers = specificDangers; }

    public List<ThreatDangerDTO> getSpecificDangersWithMetadata() { return specificDangersWithMetadata; }
    public void setSpecificDangersWithMetadata(List<ThreatDangerDTO> specificDangersWithMetadata) { this.specificDangersWithMetadata = specificDangersWithMetadata; }

    public List<String> getRecommendedActions() { return recommendedActions; }
    public void setRecommendedActions(List<String> recommendedActions) { this.recommendedActions = recommendedActions; }

    public List<RelatedNodeDTO> getRelatedNodes() { return relatedNodes; }
    public void setRelatedNodes(List<RelatedNodeDTO> relatedNodes) { this.relatedNodes = relatedNodes; }

    public String getGraphIntelligence() { return graphIntelligence; }
    public void setGraphIntelligence(String graphIntelligence) { this.graphIntelligence = graphIntelligence; }

    // Inner class for related nodes
    public static class RelatedNodeDTO {
        private String nodeId;
        private String nodeType;
        private String nodeValue;
        private String riskLevel;
        private String relationship;
        private String reason;

        public RelatedNodeDTO() {}

        public RelatedNodeDTO(String nodeId, String nodeType, String nodeValue, String riskLevel, String relationship, String reason) {
            this.nodeId = nodeId;
            this.nodeType = nodeType;
            this.nodeValue = nodeValue;
            this.riskLevel = riskLevel;
            this.relationship = relationship;
            this.reason = reason;
        }

        // Getters and Setters
        public String getNodeId() { return nodeId; }
        public void setNodeId(String nodeId) { this.nodeId = nodeId; }

        public String getNodeType() { return nodeType; }
        public void setNodeType(String nodeType) { this.nodeType = nodeType; }

        public String getNodeValue() { return nodeValue; }
        public void setNodeValue(String nodeValue) { this.nodeValue = nodeValue; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
