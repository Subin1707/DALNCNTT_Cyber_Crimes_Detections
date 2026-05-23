package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.GraphLinkDTO;
import com.example.servingwebcontent.dto.GraphNodeDTO;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MultiEntityGraphAnalysisService {

    private static final int SUPER_NODE_THRESHOLD = 35;
    private static final int MAX_PROPAGATION_DEPTH = 4;
    private static final double PROPAGATION_ALPHA = 0.58;
    private static final double EPSILON = 1.0e-9;

    public void enrich(List<GraphNodeDTO> nodes, List<GraphLinkDTO> links) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        List<GraphLinkDTO> safeLinks = links == null ? List.of() : links;
        Map<String, GraphNodeDTO> nodeById = new LinkedHashMap<>();
        Map<String, List<GraphLinkDTO>> adjacency = new HashMap<>();
        Map<String, Integer> fraudDepth = calculateFraudDepth(nodes, safeLinks);

        for (GraphNodeDTO node : nodes) {
            if (node == null || node.getId() == null) {
                continue;
            }
            nodeById.put(node.getId(), node);
            adjacency.putIfAbsent(node.getId(), new ArrayList<>());
        }

        for (GraphLinkDTO link : safeLinks) {
            if (link == null || link.getSource() == null || link.getTarget() == null) {
                continue;
            }
            adjacency.computeIfAbsent(link.getSource(), k -> new ArrayList<>()).add(link);
            adjacency.computeIfAbsent(link.getTarget(), k -> new ArrayList<>()).add(link);
        }

        Map<String, Double> densityCache = calculateClusterDensity(nodeById, adjacency);

        for (GraphNodeDTO node : nodes) {
            if (node == null || node.getId() == null) {
                continue;
            }

            List<String> reasons = new ArrayList<>();
            List<GraphLinkDTO> nodeLinks = adjacency.getOrDefault(node.getId(), List.of());
            int degree = nodeLinks.size();
            double centerDistance = centerDistance(node);
            double centerRisk = 1.0 / (centerDistance + 1.0);
            double relationStrength = averageRelationStrength(nodeLinks);
            double density = densityCache.getOrDefault(node.getId(), 0.0);
            boolean superNode = degree > SUPER_NODE_THRESHOLD;
            boolean missingData = hasMissingData(node, degree);
            boolean weakNetworkCaptureEvidence = hasWeakNetworkCaptureEvidence(node, degree);
            int depth = fraudDepth.getOrDefault(node.getId(), Integer.MAX_VALUE);
            double propagation = depth == Integer.MAX_VALUE ? 0.0 : Math.pow(PROPAGATION_ALPHA, depth);
            double degreeNormalizer = superNode ? Math.log(degree + 1.0) : 1.0;

            double rawRisk = (
                    node.getRiskScore() / 100.0 * 0.34
                            + centerRisk * 0.18
                            + relationStrength * 0.16
                            + density * 0.10
                            + propagation * 0.16
                            + contradictorySignal(node) * 0.06
            ) / Math.max(EPSILON, degreeNormalizer);

            double confidence = confidence(node, degree, relationStrength, missingData, superNode);
            String membership = membership(centerDistance, relationStrength, degree, missingData, weakNetworkCaptureEvidence);
            String action = action(rawRisk, confidence, membership, superNode, missingData);

            if (centerDistance < 0.35) reasons.add("Gan tam mien: distance=" + format(centerDistance));
            if (centerDistance >= 0.75 && !"OUTSIDE".equals(membership)) reasons.add("Xa tam nhung van trong mien, da ap dung decay theo distance");
            if (relationStrength > 0.60) reasons.add("Lien ket manh voi relation weight=" + format(relationStrength));
            if (density > 0.45) reasons.add("Mat do cluster cao=" + format(density));
            if (propagation > 0.0) reasons.add("Co lan truyen anh huong fraud depth=" + depth + ", propagation=" + format(propagation));
            if (superNode) reasons.add("Super node degree=" + degree + ", da degree-normalization");
            if (missingData) reasons.add("Thieu du lieu, confidence bi giam");
            if (degree == 0) reasons.add("Node co lap, khong co relationship nen dat ngoai mien va can thu thap them du lieu");
            if (weakNetworkCaptureEvidence) reasons.add("Network capture chi co bang chung quan sat IP/Domain/URL, chua du feature de dua vao mien");
            if ("BOUNDARY".equals(membership)) reasons.add("Node gan bien mien, uu tien monitoring/manual review");
            if ("OUTSIDE".equals(membership)) reasons.add("Node ngoai mien hoac co lap, khong block tu dong");
            if (reasons.isEmpty()) reasons.add("Graph signal on dinh, xu ly theo risk va confidence");

            node.setDegree(degree);
            node.setCenterDistance(centerDistance);
            node.setRelationStrength(relationStrength);
            node.setClusterDensity(density);
            node.setSuperNode(superNode);
            node.setMissingData(missingData);
            node.setPropagationScore(propagation);
            node.setGraphRiskScore(rawRisk * 100.0);
            node.setConfidence(confidence);
            node.setMembershipStatus(membership);
            node.setRecommendedAction(action);
            node.setGraphReasons(reasons);
        }
    }

    private Map<String, Integer> calculateFraudDepth(List<GraphNodeDTO> nodes, List<GraphLinkDTO> links) {
        Map<String, GraphNodeDTO> nodeById = new HashMap<>();
        Map<String, Set<String>> neighbors = new HashMap<>();
        Queue<String> queue = new ArrayDeque<>();
        Map<String, Integer> depth = new HashMap<>();

        for (GraphNodeDTO node : nodes) {
            if (node == null || node.getId() == null) continue;
            nodeById.put(node.getId(), node);
            neighbors.putIfAbsent(node.getId(), new LinkedHashSet<>());
            if ("high".equalsIgnoreCase(node.getRiskLevel()) || node.getRiskScore() >= 70) {
                queue.add(node.getId());
                depth.put(node.getId(), 0);
            }
        }

        for (GraphLinkDTO link : links) {
            if (link == null || link.getSource() == null || link.getTarget() == null) continue;
            neighbors.computeIfAbsent(link.getSource(), k -> new LinkedHashSet<>()).add(link.getTarget());
            neighbors.computeIfAbsent(link.getTarget(), k -> new LinkedHashSet<>()).add(link.getSource());
        }

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentDepth = depth.getOrDefault(current, 0);
            if (currentDepth >= MAX_PROPAGATION_DEPTH) continue;

            for (String next : neighbors.getOrDefault(current, Set.of())) {
                if (!nodeById.containsKey(next) || depth.containsKey(next)) continue;
                depth.put(next, currentDepth + 1);
                queue.add(next);
            }
        }

        return depth;
    }

    private Map<String, Double> calculateClusterDensity(Map<String, GraphNodeDTO> nodeById,
                                                        Map<String, List<GraphLinkDTO>> adjacency) {
        Map<String, Double> result = new HashMap<>();
        Set<String> edgeSet = undirectedEdgeSet(adjacency);
        for (String nodeId : nodeById.keySet()) {
            Set<String> neighbors = new LinkedHashSet<>();
            for (GraphLinkDTO link : adjacency.getOrDefault(nodeId, List.of())) {
                String other = nodeId.equals(link.getSource()) ? link.getTarget() : link.getSource();
                if (other != null && nodeById.containsKey(other)) {
                    neighbors.add(other);
                }
            }
            int k = neighbors.size();
            if (k < 2) {
                result.put(nodeId, 0.0);
                continue;
            }

            int linksBetweenNeighbors = 0;
            List<String> list = new ArrayList<>(neighbors);
            for (int i = 0; i < list.size(); i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    if (edgeSet.contains(edgeKey(list.get(i), list.get(j)))) {
                        linksBetweenNeighbors++;
                    }
                }
            }
            result.put(nodeId, clamp((2.0 * linksBetweenNeighbors) / (k * (k - 1.0))));
        }
        return result;
    }

    private Set<String> undirectedEdgeSet(Map<String, List<GraphLinkDTO>> adjacency) {
        Set<String> edges = new HashSet<>();
        for (List<GraphLinkDTO> links : adjacency.values()) {
            for (GraphLinkDTO link : links) {
                if (link.getSource() != null && link.getTarget() != null) {
                    edges.add(edgeKey(link.getSource(), link.getTarget()));
                }
            }
        }
        return edges;
    }

    private String edgeKey(String a, String b) {
        return a.compareTo(b) <= 0 ? a + "::" + b : b + "::" + a;
    }

    private double centerDistance(GraphNodeDTO node) {
        double risk = clamp(node.getRiskScore() / 100.0);
        double center = switch (safe(node.getDomainAssignment())) {
            case "fraud" -> 0.90;
            case "suspicious" -> 0.50;
            default -> 0.10;
        };
        return Math.abs(risk - center);
    }

    private double averageRelationStrength(List<GraphLinkDTO> links) {
        if (links == null || links.isEmpty()) return 0.0;
        double sum = 0.0;
        for (GraphLinkDTO link : links) {
            sum += relationWeight(link.getType());
        }
        return clamp(sum / links.size());
    }

    private double relationWeight(String type) {
        return switch (safe(type).toUpperCase(Locale.ROOT)) {
            case "HAS_IP", "SENT_FROM_IP", "CONNECTS_TO" -> 0.85;
            case "HAS_URL", "CONTAINS_URL", "HOSTED_ON", "HOSTED_ON_DOMAIN", "RESOLVES_TO" -> 0.78;
            case "HAS_FILE", "DOWNLOADS", "DOWNLOADS_FILE", "HAS_HASH", "HAS_FILE_HASH" -> 0.90;
            case "HAS_EMAIL", "RECEIVED", "HAS_ACCOUNT", "HAS_VICTIM" -> 0.62;
            default -> 0.42;
        };
    }

    private boolean hasMissingData(GraphNodeDTO node, int degree) {
        if (node.getValue() == null || node.getValue().isBlank()) return true;
        if (degree == 0) return true;
        return node.getIndicators() == null || node.getIndicators().isEmpty();
    }

    private boolean hasWeakNetworkCaptureEvidence(GraphNodeDTO node, int degree) {
        if (!"wireshark".equals(safe(node.getSource()))) return false;

        String type = safe(node.getType());
        boolean captureOnlyType = Set.of("ipaddress", "domain", "url").contains(type);
        if (!captureOnlyType) return false;

        boolean lowRisk = node.getRiskScore() <= 25;
        boolean hasDangerSignal = hasDangerIndicator(node.getIndicators());

        // Network capture nodes can have many observation edges but still lack
        // strong fraud evidence. Keep low-risk observed traffic outside regions
        // until a real dangerous signal appears.
        return lowRisk && !hasDangerSignal;
    }

    private boolean hasDangerIndicator(List<String> indicators) {
        if (indicators == null || indicators.isEmpty()) return false;
        for (String indicator : indicators) {
            String value = safe(indicator);
            if (value.contains("blacklist")
                    || value.contains("fraud")
                    || value.contains("tor")
                    || value.contains("vpn")
                    || value.contains("spam")
                    || value.contains("phishing")
                    || value.contains("malware")
                    || value.contains("suspicious")
                    || value.contains("failed")
                    || value.contains("invalid")
                    || value.contains("manual_block")) {
                return true;
            }
        }
        return false;
    }

    private double contradictorySignal(GraphNodeDTO node) {
        String status = safe(node.getStatus());
        int risk = node.getRiskScore();
        if ("valid".equals(status) && risk >= 60) return 1.0;
        if ("fake".equals(status) && risk <= 40) return 1.0;
        if ("suspicious".equals(status) && (risk < 25 || risk > 85)) return 0.6;
        return 0.0;
    }

    private double confidence(GraphNodeDTO node, int degree, double relationStrength, boolean missingData, boolean superNode) {
        double confidence = 0.30;
        if (node.getValue() != null && !node.getValue().isBlank()) confidence += 0.15;
        if (node.getIndicators() != null && !node.getIndicators().isEmpty()) confidence += 0.15;
        confidence += Math.min(0.22, degree * 0.035);
        confidence += relationStrength * 0.18;
        if (missingData) confidence *= 0.55;
        if (superNode) confidence *= 0.75;
        return clamp(confidence);
    }

    private String membership(double centerDistance,
                              double relationStrength,
                              int degree,
                              boolean missingData,
                              boolean weakNetworkCaptureEvidence) {
        if (degree == 0) return "OUTSIDE";
        if (weakNetworkCaptureEvidence) return "OUTSIDE";
        if (missingData && degree <= 1) return "OUTSIDE";
        if (missingData && relationStrength < 0.45) return "OUTSIDE";
        if (centerDistance <= 0.22) return "IN_REGION";
        if (centerDistance <= 0.45 || relationStrength >= 0.55) return "BOUNDARY";
        return "OUTSIDE";
    }

    private String action(double risk, double confidence, String membership, boolean superNode, boolean missingData) {
        if (superNode) return "sampling";
        if (missingData || confidence < 0.30) return "collect_more_data";
        if ("OUTSIDE".equals(membership)) return "log_only";
        if (risk >= 0.72 && confidence >= 0.60) return "block";
        if (risk >= 0.45 || "BOUNDARY".equals(membership)) return "manual_review";
        return "monitor";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return 0.0;
        return Math.max(0.0, Math.min(1.0, value));
    }

    private String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
