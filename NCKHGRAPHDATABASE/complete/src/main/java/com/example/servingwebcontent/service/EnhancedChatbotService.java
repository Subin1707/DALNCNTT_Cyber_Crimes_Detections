package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.ChatbotResponseDTO;
import com.example.servingwebcontent.dto.ChatbotResponseDTO.RelatedNodeDTO;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class EnhancedChatbotService {

    private final Neo4jClient neo4j;
    private final AlertLoggingService alertLoggingService;

    public EnhancedChatbotService(Neo4jClient neo4j, AlertLoggingService alertLoggingService) {
        this.neo4j = neo4j;
        this.alertLoggingService = alertLoggingService;
    }

    private enum ThreatPattern {
        PHISHING("Phishing", "Mimics legitimate services to steal credentials"),
        C2_COMMAND("C2 Communication", "Command-and-control communication with an attacker"),
        LATERAL_MOVEMENT("Lateral Movement", "Attempts to move within the internal network"),
        BOTNET("Botnet Activity", "Part of botnet infrastructure"),
        MALWARE_HOSTING("Malware Hosting", "Hosts or distributes malware"),
        DDoS_SOURCE("DDoS Source", "Used as a source for DDoS attacks"),
        DATA_EXFILTRATION("Data Exfiltration", "Attempts to steal sensitive data"),
        NONE("Clean", "No suspicious patterns detected");

        private final String displayName;
        private final String description;

        ThreatPattern(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    public ChatbotResponseDTO generateAnalysis(
            String nodeId,
            String nodeType,
            String nodeValue,
            String riskLevel,
            int riskScore,
            List<String> indicators
    ) {
        ChatbotResponseDTO response = new ChatbotResponseDTO(nodeId, nodeType, nodeValue);
        response.setRiskLevel(riskLevel);
        response.setRiskScore(riskScore);

        ThreatPattern pattern = detectThreatPattern(nodeType, nodeValue, riskScore);
        List<RelatedNodeDTO> relatedNodes = findRelatedNodes(nodeId, nodeType);

        response.setStatus(getNodeStatus(riskScore));
        response.setAnalysisDescription(generateAnalysisLayer(nodeType, nodeValue, riskScore, pattern, indicators, relatedNodes));
        response.setRiskAssessment(generateRiskAssessment(riskLevel, riskScore, indicators, pattern));
        response.setSpecificDangers(generateThreatExplanation(nodeType, riskLevel, pattern));
        response.setThreatExplanation(generateThreatNarrative(nodeType, nodeValue, riskLevel, pattern, indicators, relatedNodes));
        response.setRecommendedActions(generateRecommendedActions(riskLevel, nodeType, pattern));
        response.setRelatedNodes(relatedNodes);
        response.setGraphIntelligence(generateGraphIntelligence(nodeType, nodeValue, relatedNodes, riskScore, indicators));

        return response;
    }

    private ThreatPattern detectThreatPattern(String nodeType, String nodeValue, int riskScore) {
        if (riskScore < 20) {
            return ThreatPattern.NONE;
        }

        String safeType = safeUpper(nodeType);
        String lowerValue = nodeValue == null ? "" : nodeValue.toLowerCase();

        switch (safeType) {
            case "URL":
                if (containsAny(lowerValue, "login", "secure", "verify", "confirm", "account", "password")) {
                    return ThreatPattern.PHISHING;
                }
                if (containsAny(lowerValue, "payload", "malware", "exploit", "dropper")) {
                    return ThreatPattern.MALWARE_HOSTING;
                }
                break;
            case "IP":
                if (isInternalIP(nodeValue) && riskScore >= 50) {
                    return ThreatPattern.LATERAL_MOVEMENT;
                }
                if (riskScore >= 70) {
                    return ThreatPattern.C2_COMMAND;
                }
                if (riskScore >= 60) {
                    return ThreatPattern.DDoS_SOURCE;
                }
                break;
            case "DOMAIN":
                if (containsAny(lowerValue, "c2", "command", "control")) {
                    return ThreatPattern.C2_COMMAND;
                }
                if (riskScore >= 70) {
                    return ThreatPattern.MALWARE_HOSTING;
                }
                break;
            case "EMAIL":
                if (riskScore >= 70 || containsAny(lowerValue, "support", "security", "billing", "verify")) {
                    return ThreatPattern.PHISHING;
                }
                break;
            case "FILE":
                if (riskScore >= 70) {
                    return ThreatPattern.MALWARE_HOSTING;
                }
                break;
            default:
                break;
        }

        if (riskScore >= 70) {
            return ThreatPattern.MALWARE_HOSTING;
        }
        if (riskScore >= 50) {
            return ThreatPattern.BOTNET;
        }
        return ThreatPattern.NONE;
    }

    private String generateAnalysisLayer(
            String nodeType,
            String nodeValue,
            int riskScore,
            ThreatPattern pattern,
            List<String> indicators,
            List<RelatedNodeDTO> relatedNodes
    ) {
        String normalizedType = safeUpper(nodeType);
        String summary = switch (normalizedType) {
            case "IP", "IPADDRESS" -> "IP address observed in the fraud graph. Classification: "
                    + (isInternalIP(nodeValue) ? "internal/private" : "external/public") + ".";
            case "DOMAIN" -> "Domain observed in the fraud graph and linked to other entities.";
            case "URL" -> "URL observed in messages, browsing activity, or network telemetry.";
            case "EMAIL" -> "Email address observed in the fraud graph and linked to communications.";
            case "FILE", "FILENODE", "FILEHASH" -> "File-related artifact observed in the fraud graph and linked to system activity.";
            case "ANALYSISSESSION" -> "Analysis session node representing an ingestion or investigation context.";
            default -> "Entity observed in the fraud graph.";
        };

        StringBuilder builder = new StringBuilder();
        builder.append("Loại node: ").append(nodeType).append('\n');
        builder.append("Giá trị: ").append(nodeValue).append('\n');
        builder.append("Mô tả: ").append(summary).append('\n');
        builder.append("Risk Score: ").append(riskScore).append("/100\n");
        builder.append("Pattern phát hiện: ").append(pattern.getDisplayName()).append('\n');
        builder.append("Ý nghĩa pattern: ").append(pattern.getDescription()).append("\n\n");
        builder.append("Lý do node xuất hiện trong graph:\n");
        builder.append(buildPresenceExplanation(nodeType, nodeValue, indicators, relatedNodes));

        return builder.toString();
    }

    private String generateRiskAssessment(String riskLevel, int riskScore, List<String> indicators, ThreatPattern pattern) {
        StringBuilder builder = new StringBuilder();
        builder.append("Risk Level: ").append(riskLevel).append('\n');
        builder.append("Risk Score: ").append(riskScore).append("/100\n");
        builder.append("Threat Pattern: ").append(pattern.getDisplayName()).append('\n');

        if (indicators != null && !indicators.isEmpty()) {
            builder.append("Indicators:\n");
            indicators.stream().limit(4).forEach(indicator -> builder.append("- ").append(indicator).append('\n'));
        }

        builder.append("Assessment: ");
        if (riskScore >= 70) {
            builder.append("High-confidence malicious behavior. Immediate containment is appropriate.");
        } else if (riskScore >= 40) {
            builder.append("Suspicious behavior. Investigate context and related nodes before taking broad action.");
        } else if (riskScore >= 20) {
            builder.append("Low to moderate concern. Monitor for escalation or additional corroborating signals.");
        } else {
            builder.append("Low concern based on current evidence.");
        }

        return builder.toString();
    }

    private List<String> generateThreatExplanation(String nodeType, String riskLevel, ThreatPattern pattern) {
        List<String> dangers = new ArrayList<>();

        if ("HIGH".equalsIgnoreCase(riskLevel)) {
            dangers.add("This entity is strongly associated with malicious activity.");
        } else if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
            dangers.add("This entity has suspicious characteristics that require review.");
        } else {
            dangers.add("Current evidence does not show strong malicious behavior.");
        }

        switch (pattern) {
            case PHISHING -> {
                dangers.add("May steal credentials or payment information.");
                dangers.add("Often impersonates a trusted service or workflow.");
            }
            case C2_COMMAND -> {
                dangers.add("May coordinate compromised hosts or malware.");
                dangers.add("Can support persistence, tasking, and data theft.");
            }
            case LATERAL_MOVEMENT -> {
                dangers.add("May indicate an internal host is probing or spreading.");
                dangers.add("Can expose additional systems through trusted network paths.");
            }
            case MALWARE_HOSTING -> {
                dangers.add("May deliver or host malicious payloads.");
                dangers.add("Systems interacting with it may be at risk of compromise.");
            }
            case DDoS_SOURCE -> {
                dangers.add("May be used to generate disruptive traffic.");
                dangers.add("Can affect service availability or hide parallel activity.");
            }
            case BOTNET -> {
                dangers.add("May be part of coordinated malicious infrastructure.");
                dangers.add("Behavior may escalate quickly if attacker tasking changes.");
            }
            default -> dangers.add("Review linked entities for additional context.");
        }

        return dangers;
    }

    private String generateThreatNarrative(
            String nodeType,
            String nodeValue,
            String riskLevel,
            ThreatPattern pattern,
            List<String> indicators,
            List<RelatedNodeDTO> relatedNodes
    ) {
        if ("HIGH".equalsIgnoreCase(riskLevel)) {
            return "Node " + nodeType + " `" + nodeValue + "` đang ở mức HIGH và khớp với mẫu "
                    + pattern.getDisplayName() + ". "
                    + buildThreatContext(indicators, relatedNodes, true);
        }
        if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
            return "Node " + nodeType + " `" + nodeValue + "` có dấu hiệu đáng ngờ và cần xem cùng ngữ cảnh graph. "
                    + "Pattern hiện tại là " + pattern.getDisplayName() + ". "
                    + buildThreatContext(indicators, relatedNodes, false);
        }
        return "Node " + nodeType + " `" + nodeValue + "` hiện có mức rủi ro thấp. "
                + "Tuy nhiên vẫn nên theo dõi các liên kết trong graph vì thay đổi ở node liên quan có thể làm ngữ cảnh rủi ro thay đổi.";
    }

    private List<String> generateRecommendedActions(String riskLevel, String nodeType, ThreatPattern pattern) {
        List<String> actions = new ArrayList<>();

        if ("HIGH".equalsIgnoreCase(riskLevel)) {
            actions.add("Block or isolate this " + nodeType.toLowerCase() + " where operationally possible.");
            actions.add("Review all related nodes and recent activity for the same pattern.");
            actions.add("Notify the security team and open an incident for containment.");
        } else if ("MEDIUM".equalsIgnoreCase(riskLevel)) {
            actions.add("Enable enhanced monitoring for this " + nodeType.toLowerCase() + ".");
            actions.add("Review related nodes, event logs, and enrichment sources.");
            actions.add("Prepare containment steps if risk score or indicators increase.");
        } else {
            actions.add("Continue routine monitoring.");
            actions.add("Alert on meaningful reputation or behavior changes.");
        }

        switch (pattern) {
            case PHISHING -> actions.add("Check for impacted users, inboxes, and credential exposure.");
            case C2_COMMAND -> actions.add("Review outbound connections and inspect potentially compromised hosts.");
            case LATERAL_MOVEMENT -> actions.add("Inspect east-west traffic and validate internal account usage.");
            case MALWARE_HOSTING -> actions.add("Scan systems that accessed the related resource.");
            case DDoS_SOURCE -> actions.add("Review traffic volume, target patterns, and upstream controls.");
            case BOTNET -> actions.add("Look for recurring beaconing or coordinated connections.");
            default -> { }
        }

        return actions;
    }

    @SuppressWarnings("unchecked")
    private List<RelatedNodeDTO> findRelatedNodes(String nodeId, String nodeType) {
        List<RelatedNodeDTO> relatedNodes = new ArrayList<>();

        try {
            String query = """
                    MATCH (n)-[r]-(related)
                    WHERE elementId(n) = $nodeId
                    RETURN elementId(related) AS id,
                           labels(related)[0] AS type,
                           coalesce(
                               related.email,
                               related.ip,
                               related.url,
                               related.domain,
                               related.fileName,
                               related.hash,
                               related.username,
                               related.account,
                               related.fileName,
                               related.id,
                               'UNKNOWN'
                           ) AS value,
                           coalesce(related.riskLevel, 'UNKNOWN') AS risk,
                           type(r) AS rel
                    LIMIT 8
                    """;

            Collection<Map<String, Object>> results = (Collection<Map<String, Object>>) (Collection<?>) neo4j.query(query)
                    .bind(nodeId).to("nodeId")
                    .fetchAs(Map.class)
                    .all();

            for (Map<String, Object> row : results) {
                String relatedId = (String) row.get("id");
                String relatedType = (String) row.get("type");
                String relatedValue = (String) row.get("value");
                String riskLevel = (String) row.get("risk");
                String relationship = (String) row.get("rel");

                relatedNodes.add(new RelatedNodeDTO(
                        relatedId,
                        relatedType,
                        relatedValue,
                        riskLevel != null ? riskLevel : "UNKNOWN",
                        relationship,
                        generateRelationshipReason(nodeType, relatedType, relationship)
                ));
            }
        } catch (Exception ignored) {
            // Keep chatbot responses available even when graph enrichment fails.
        }

        return relatedNodes;
    }

    private String generateRelationshipReason(String fromType, String relatedType, String relationship) {
        if (relationship == null || relationship.isBlank()) {
            return "Có liên kết trực tiếp trong graph.";
        }

        return switch (relationship.toUpperCase()) {
            case "HAS_EMAIL", "RECEIVED" -> "Node này xuất hiện trong cùng phiên/phân tích email liên quan.";
            case "HAS_IP", "SENT_FROM_IP", "CONNECTS_TO", "RESOLVES_TO" -> "Node này liên quan đến hạ tầng IP đang giao tiếp hoặc phân giải tới.";
            case "HAS_URL", "CONTAINS_URL" -> "Node này được nhắc tới hoặc nhúng trong một thực thể khác trong graph.";
            case "HAS_DOMAIN", "BELONGS_TO_DOMAIN", "HOSTED_ON_DOMAIN" -> "Node này gắn với domain/hạ tầng tên miền liên quan.";
            case "HAS_FILE", "HAS_FILEHASH", "DOWNLOADS", "HAS_HASH" -> "Node này liên quan đến tệp hoặc dấu vết tải xuống trong cùng chuỗi sự kiện.";
            case "HOSTED_ON" -> "Node này được đặt trên cùng hạ tầng lưu trữ hoặc máy chủ.";
            default -> "Liên kết `" + relationship + "` cho thấy " + fromType + " có quan hệ trực tiếp với " + relatedType + ".";
        };
    }

    private String generateGraphIntelligence(
            String nodeType,
            String nodeValue,
            List<RelatedNodeDTO> relatedNodes,
            int nodeRiskScore,
            List<String> indicators
    ) {
        if (relatedNodes == null || relatedNodes.isEmpty()) {
            return "Chưa thấy liên kết trực tiếp nào từ node này trong graph. Điều đó có thể nghĩa là node đang khá cô lập, hoặc dữ liệu phiên hiện tại chưa đủ để nối nó vào chuỗi hành vi lớn hơn.";
        }

        long highRiskCount = relatedNodes.stream()
                .filter(node -> "HIGH".equalsIgnoreCase(node.getRiskLevel()))
                .count();
        long mediumRiskCount = relatedNodes.stream()
                .filter(node -> "MEDIUM".equalsIgnoreCase(node.getRiskLevel()))
                .count();

        StringBuilder builder = new StringBuilder();
        builder.append("Node ").append(nodeType).append(" `").append(nodeValue).append("` đang có ")
                .append(relatedNodes.size()).append(" liên kết trực tiếp trong graph.\n");
        builder.append("Số node liên quan mức HIGH: ").append(highRiskCount).append('\n');
        builder.append("Số node liên quan mức MEDIUM: ").append(mediumRiskCount).append('\n');

        List<String> relationHighlights = relatedNodes.stream()
                .limit(4)
                .map(node -> "- " + node.getNodeType() + " `" + node.getNodeValue() + "` qua quan hệ `"
                        + (node.getRelationship() == null ? "UNKNOWN" : node.getRelationship()) + "`")
                .toList();
        if (!relationHighlights.isEmpty()) {
            builder.append("Các liên kết đáng chú ý:\n");
            relationHighlights.forEach(line -> builder.append(line).append('\n'));
        }

        if (nodeRiskScore >= 70 && highRiskCount > 0) {
            builder.append("Kết luận ngữ cảnh: node này không chỉ rủi ro tự thân mà còn nằm trong một cụm node rủi ro cao. Đây là dấu hiệu mạnh cho thấy nó thuộc cùng chuỗi hành vi tấn công.");
        } else if (nodeRiskScore < 20 && highRiskCount > 0) {
            builder.append("Kết luận ngữ cảnh: bản thân node có thể an toàn, nhưng việc nó nối với node rủi ro cao khiến nó trở thành điểm cần theo dõi trong điều tra.");
        } else {
            builder.append("Kết luận ngữ cảnh: cần đọc node này cùng các liên kết của nó để hiểu đúng mức độ ưu tiên điều tra.");
        }

        if (indicators != null && !indicators.isEmpty()) {
            builder.append("\nIndicator hỗ trợ kết luận:\n");
            indicators.stream().limit(3).forEach(indicator -> builder.append("- ").append(indicator).append('\n'));
        }

        return builder.toString();
    }

    private String buildPresenceExplanation(
            String nodeType,
            String nodeValue,
            List<String> indicators,
            List<RelatedNodeDTO> relatedNodes
    ) {
        StringBuilder builder = new StringBuilder();
        if (relatedNodes == null || relatedNodes.isEmpty()) {
            builder.append("- Hệ thống đã ghi nhận ").append(nodeType).append(" `").append(nodeValue)
                    .append("` trong dữ liệu phân tích, nhưng hiện chưa truy ra nhiều liên kết trực tiếp.\n");
        } else {
            RelatedNodeDTO sessionNode = relatedNodes.stream()
                    .filter(node -> "AnalysisSession".equalsIgnoreCase(node.getNodeType()))
                    .findFirst()
                    .orElse(null);
            if (sessionNode != null) {
                builder.append("- Node này được đưa vào graph từ phiên phân tích `")
                        .append(sessionNode.getNodeValue()).append("`.\n");
            } else {
                builder.append("- Node này xuất hiện vì nó có liên kết trực tiếp với các thực thể khác trong graph.\n");
            }

            relatedNodes.stream().limit(4).forEach(node -> builder.append("- Liên kết tới ")
                    .append(node.getNodeType()).append(" `").append(node.getNodeValue()).append("` qua quan hệ `")
                    .append(node.getRelationship() == null ? "UNKNOWN" : node.getRelationship()).append("`: ")
                    .append(node.getReason()).append('\n'));
        }

        if (indicators != null && !indicators.isEmpty()) {
            builder.append("- Các indicator khiến node này được giữ lại để phân tích:\n");
            indicators.stream().limit(4).forEach(indicator -> builder.append("  * ").append(indicator).append('\n'));
        }

        return builder.toString().trim();
    }

    private String buildThreatContext(List<String> indicators, List<RelatedNodeDTO> relatedNodes, boolean highRisk) {
        StringBuilder builder = new StringBuilder();
        if (indicators != null && !indicators.isEmpty()) {
            builder.append("Các indicator hiện có đang củng cố nhận định này");
            if (highRisk) {
                builder.append(", đặc biệt khi nhiều dấu hiệu xuất hiện đồng thời");
            }
            builder.append(". ");
        }
        if (relatedNodes != null && !relatedNodes.isEmpty()) {
            long suspiciousNeighbors = relatedNodes.stream()
                    .filter(node -> "HIGH".equalsIgnoreCase(node.getRiskLevel()) || "MEDIUM".equalsIgnoreCase(node.getRiskLevel()))
                    .count();
            builder.append("Trong graph, node này nối với ").append(relatedNodes.size()).append(" thực thể khác");
            if (suspiciousNeighbors > 0) {
                builder.append(", trong đó ").append(suspiciousNeighbors).append(" thực thể đã có mức rủi ro từ MEDIUM trở lên");
            }
            builder.append(".");
        }
        return builder.toString().trim();
    }

    private String getNodeStatus(int riskScore) {
        if (riskScore >= 70) {
            return "MALICIOUS";
        }
        if (riskScore >= 40) {
            return "SUSPICIOUS";
        }
        if (riskScore >= 20) {
            return "CAUTION";
        }
        return "SAFE";
    }

    private boolean isInternalIP(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        return ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || ip.startsWith("172.16.")
                || ip.startsWith("172.17.")
                || ip.startsWith("172.18.")
                || ip.startsWith("172.19.")
                || ip.startsWith("172.2")
                || ip.startsWith("172.30.")
                || ip.startsWith("172.31.")
                || ip.startsWith("127.")
                || ip.equals("::1")
                || ip.startsWith("fe80:");
    }

    private boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private String safeUpper(String value) {
        return value == null ? "" : value.toUpperCase();
    }
}
