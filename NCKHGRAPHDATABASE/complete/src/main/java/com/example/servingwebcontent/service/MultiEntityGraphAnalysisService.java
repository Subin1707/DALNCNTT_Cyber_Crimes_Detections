package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.GraphLinkDTO;
import com.example.servingwebcontent.dto.GraphNodeDTO;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MultiEntityGraphAnalysisService {

    private static final int SUPER_NODE_THRESHOLD = 35;
    private static final int MAX_SHARED_FEATURE_PAIRING = 250;
    private static final int MAX_VISUAL_OVERLAP_LINKS = 360;
    private static final int MAX_PROPAGATION_DEPTH = 4;
    private static final double PROPAGATION_ALPHA = 0.58;
    private static final double EPSILON = 1.0e-9;
    private static final double DOMAIN_OVERLAP_THRESHOLD = 0.30;
    private static final double BOUNDARY_OVERLAP_THRESHOLD = 0.16;
    private static final double CORE_DOMAIN_DISTANCE_THRESHOLD = 0.30;
    private static final double BOUNDARY_DOMAIN_DISTANCE_THRESHOLD = 0.44;
    private static final double MULTI_DOMAIN_DISTANCE_GAP = 0.18;
    private static final double OUTLIER_INFLUENCE_THRESHOLD = 0.42;
    private static final double OVERLAP_INFLUENCE_GAP = 0.16;
    private static final List<String> RISK_DOMAINS = List.of("safe", "suspicious", "fraud");
    private static final Set<String> ANALYZABLE_TYPES = Set.of(
            "email",
            "ipaddress",
            "url",
            "domain",
            "filenode",
            "filehash",
            "victimaccount"
    );
    private static final Set<String> CONTEXT_ONLY_TYPES = Set.of(
            "ipaddress",
            "domain",
            "url"
    );
    private static final Map<String, Double> ENTITY_OVERLAP_WEIGHTS = Map.ofEntries(
            Map.entry("device", 0.95),
            Map.entry("email", 0.80),
            Map.entry("victimaccount", 0.75),
            Map.entry("phone", 0.75),
            Map.entry("ipaddress", 0.50),
            Map.entry("url", 0.40),
            Map.entry("domain", 0.30),
            Map.entry("filenode", 0.70),
            Map.entry("filehash", 0.85),
            Map.entry("analysissession", 0.20)
    );
    private static final Map<String, Double> COMMON_FEATURE_WEIGHTS = Map.ofEntries(
            Map.entry("risk", 1.00),
            Map.entry("status", 0.90),
            Map.entry("riskLevel", 0.90),
            Map.entry("overlap", 0.75),
            Map.entry("relation", 0.70),
            Map.entry("density", 0.45),
            Map.entry("propagation", 0.65),
            Map.entry("centrality", 0.35),
            Map.entry("bridge", 0.55),
            Map.entry("manualBlock", 1.00)
    );
    private static final Map<String, Map<String, Double>> TYPE_FEATURE_WEIGHTS = Map.ofEntries(
            Map.entry("email", Map.ofEntries(
                    Map.entry("identityEvidence", 0.90),
                    Map.entry("networkArtifact", 0.25),
                    Map.entry("contentArtifact", 0.60),
                    Map.entry("accountArtifact", 0.45),
                    Map.entry("fileArtifact", 0.20)
            )),
            Map.entry("victimaccount", Map.ofEntries(
                    Map.entry("identityEvidence", 0.80),
                    Map.entry("accountArtifact", 0.95),
                    Map.entry("networkArtifact", 0.30),
                    Map.entry("contentArtifact", 0.35),
                    Map.entry("fileArtifact", 0.20)
            )),
            Map.entry("ipaddress", Map.ofEntries(
                    Map.entry("identityEvidence", 0.25),
                    Map.entry("accountArtifact", 0.20),
                    Map.entry("networkArtifact", 1.00),
                    Map.entry("contentArtifact", 0.45),
                    Map.entry("fileArtifact", 0.25),
                    Map.entry("centrality", 0.75)
            )),
            Map.entry("domain", Map.ofEntries(
                    Map.entry("identityEvidence", 0.25),
                    Map.entry("accountArtifact", 0.20),
                    Map.entry("networkArtifact", 0.95),
                    Map.entry("contentArtifact", 0.65),
                    Map.entry("fileArtifact", 0.25),
                    Map.entry("centrality", 0.65)
            )),
            Map.entry("url", Map.ofEntries(
                    Map.entry("identityEvidence", 0.25),
                    Map.entry("accountArtifact", 0.20),
                    Map.entry("networkArtifact", 0.75),
                    Map.entry("contentArtifact", 0.95),
                    Map.entry("fileArtifact", 0.45)
            )),
            Map.entry("filenode", Map.ofEntries(
                    Map.entry("identityEvidence", 0.20),
                    Map.entry("accountArtifact", 0.15),
                    Map.entry("networkArtifact", 0.35),
                    Map.entry("contentArtifact", 0.45),
                    Map.entry("fileArtifact", 1.00)
            )),
            Map.entry("filehash", Map.ofEntries(
                    Map.entry("identityEvidence", 0.25),
                    Map.entry("accountArtifact", 0.15),
                    Map.entry("networkArtifact", 0.25),
                    Map.entry("contentArtifact", 0.35),
                    Map.entry("fileArtifact", 1.00),
                    Map.entry("manualBlock", 1.20)
            ))
    );

    public void enrich(List<GraphNodeDTO> nodes, List<GraphLinkDTO> links) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        List<GraphLinkDTO> safeLinks = links == null ? new ArrayList<>() : new ArrayList<>(links);
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
        Map<String, Set<String>> neighborSignatures = buildNeighborSignatures(nodeById, adjacency);
        Map<String, List<OverlapEdge>> relationGraph = buildRelationGraph(nodeById, neighborSignatures);
        Map<String, CommunityInfo> communities = detectCommunities(nodeById, relationGraph);
        appendOverlapLinks(safeLinks, relationGraph);
        copyEnrichedLinks(links, safeLinks);

        for (GraphNodeDTO node : nodes) {
            if (node == null || node.getId() == null) {
                continue;
            }

            List<String> reasons = new ArrayList<>();
            List<GraphLinkDTO> nodeLinks = adjacency.getOrDefault(node.getId(), List.of());
            OverlapSummary overlap = summarizeOverlap(node.getId(), relationGraph);
            CommunityInfo community = communities.get(node.getId());
            int degree = nodeLinks.size();
            int relationGraphDegree = overlap.relationDegree();
            double centerDistance = centerDistance(node);
            double centerRisk = 1.0 / (centerDistance + 1.0);
            double relationStrength = averageRelationStrength(nodeLinks);
            double density = densityCache.getOrDefault(node.getId(), 0.0);
            boolean superNode = degree > SUPER_NODE_THRESHOLD
                    || relationGraphDegree > SUPER_NODE_THRESHOLD
                    || isKnownSuperNode(node);
            boolean unsupportedType = hasUnsupportedType(node);
            boolean missingData = hasMissingData(node, degree);
            boolean weakContextOnlyEvidence = hasWeakContextOnlyEvidence(node, degree, relationStrength);
            int depth = fraudDepth.getOrDefault(node.getId(), Integer.MAX_VALUE);
            double propagation = depth == Integer.MAX_VALUE ? 0.0 : Math.pow(PROPAGATION_ALPHA, depth);
            double degreeNormalizer = superNode ? Math.log(degree + 1.0) : 1.0;
            double internalRisk = internalRisk(node, community, overlap);
            double externalRisk = externalRisk(node, community, overlap);
            boolean bridgeNode = isBridgeNode(node, relationGraph, nodeById);
            Map<String, Double> featureVector = extractFeatureVector(node, degree, relationStrength, density, propagation, overlap, bridgeNode);
            int evidenceFeatureCount = countEvidenceFeatures(featureVector);

            double rawRisk = (
                    node.getRiskScore() / 100.0 * 0.34
                            + centerRisk * 0.18
                            + relationStrength * 0.12
                            + overlap.adjustedWeightedOverlap() * 0.18
                            + density * 0.08
                            + propagation * 0.16
                            + contradictorySignal(node) * 0.04
                            + internalRisk * 0.06
                            + externalRisk * 0.04
            ) / Math.max(EPSILON, degreeNormalizer);

            double confidence = confidence(node, degree, relationStrength, overlap.adjustedWeightedOverlap(), missingData, superNode);
            Map<String, Double> domainDistances = calculateDomainDistances(featureVector, node);
            DomainFit domainFit = nearestDomain(domainDistances);
            String membership = membership(centerDistance, relationStrength, overlap.adjustedWeightedOverlap(),
                    degree, missingData, unsupportedType, weakContextOnlyEvidence, community, domainFit);
            Map<String, Double> domainInfluence = calculateDomainInfluence(domainDistances, featureVector, membership);
            Map<String, Double> softMemberships = calculateSoftMemberships(domainInfluence);
            boolean multiDomainOverlap = isMultiDomainOverlap(node, relationGraph, nodeById, overlap, domainDistances, domainInfluence, membership);
            boolean outlierNode = isOutlierNode(domainInfluence);
            String influenceZone = determineInfluenceZone(superNode, bridgeNode, multiDomainOverlap, outlierNode, domainInfluence, membership);
            String action = action(rawRisk, confidence, membership, superNode, missingData, unsupportedType);

            if (centerDistance < 0.35) reasons.add("Gan tam mien: distance=" + format(centerDistance));
            if (centerDistance >= 0.75 && !"OUTSIDE".equals(membership)) reasons.add("Xa tam nhung van trong mien, da ap dung decay theo distance");
            reasons.add("Feature profile theo loai node=" + safe(node.getType()) + ", khong dung mot vector chung cho moi entity");
            reasons.add("Mien gan nhat theo dac trung node/domain=" + domainFit.domain() + ", distance=" + format(domainFit.distance()));
            if (relationStrength > 0.60) reasons.add("Lien ket manh voi relation weight=" + format(relationStrength));
            if (overlap.rawOverlap() > 0) reasons.add("Overlap raw=" + format(overlap.rawOverlap()) + ", weighted=" + format(overlap.weightedOverlap()) + ", adjusted=" + format(overlap.adjustedWeightedOverlap()));
            if (multiDomainOverlap) reasons.add("Continuous overlap influence: node chiu anh huong nhieu mien, khong gan label cung");
            if (bridgeNode) reasons.add("Bridge influence: ket noi tu hai mien tro len, danh gia influence rieng tung mien");
            if (outlierNode) reasons.add("Outlier influence: xa tat ca tam mien, khong ep vao domain");
            if (community != null && community.size() > 1) reasons.add("Community/domain=" + community.communityId() + ", center=" + community.centerId() + ", size=" + community.size());
            if (density > 0.45) reasons.add("Mat do cluster cao=" + format(density));
            if (propagation > 0.0) reasons.add("Co lan truyen anh huong fraud depth=" + depth + ", propagation=" + format(propagation));
            if (superNode) reasons.add("Super node degree=" + degree + ", da degree-normalization");
            if (unsupportedType) reasons.add("Loai node chua duoc ho tro phan tich mien: " + node.getType());
            if (missingData) reasons.add("Thieu du lieu, confidence bi giam");
            if (degree == 0 && "OUTSIDE".equals(membership)) reasons.add("Node co lap va khong khop du dac trung mien nen dat ngoai mien");
            if (degree == 0 && !"OUTSIDE".equals(membership)) reasons.add("Node co lap nhung dac trung rieng khop mien " + domainFit.domain() + ", khong ep OUTSIDE chi vi thieu canh graph");
            if (weakContextOnlyEvidence) reasons.add("Node IP/Domain/URL chi la bang chung ngu canh yeu, chua du feature de dua vao mien");
            if ("BOUNDARY".equals(membership)) reasons.add("Node gan bien mien, uu tien monitoring/manual review");
            if ("OUTSIDE".equals(membership)) reasons.add("Node ngoai mien hoac co lap, khong block tu dong");
            if ("OUTSIDE".equals(membership)) reasons.add("Outside influence chi duoc tinh nhu anh huong ngu canh, khong so sanh ngang voi node trong mien");
            if (reasons.isEmpty()) reasons.add("Graph signal on dinh, xu ly theo risk va confidence");

            node.setDegree(degree);
            node.setCenterDistance(centerDistance);
            node.setRelationStrength(relationStrength);
            node.setClusterDensity(density);
            node.setSuperNode(superNode);
            node.setMissingData(missingData || unsupportedType || weakContextOnlyEvidence);
            node.setPropagationScore(propagation);
            node.setGraphRiskScore(rawRisk * 100.0);
            node.setConfidence(confidence);
            node.setMembershipStatus(membership);
            node.setRecommendedAction(action);
            node.setGraphReasons(reasons);
            node.setRelationGraphDegree(relationGraphDegree);
            node.setOverlapNeighborCount(overlap.overlapNeighborCount());
            node.setOverlapScore(overlap.rawOverlap());
            node.setWeightedOverlapScore(overlap.weightedOverlap());
            node.setAdjustedOverlapScore(overlap.adjustedWeightedOverlap());
            node.setCommunityId(community == null ? null : community.communityId());
            node.setDomainCenterId(community == null ? null : community.centerId());
            node.setDomainRole(community != null && node.getId().equals(community.centerId()) ? "CENTER" : "MEMBER");
            node.setMultiDomainOverlap(multiDomainOverlap);
            node.setDomainDistances(domainDistances);
            node.setDomainAffinities(softMemberships);
            node.setSoftMemberships(softMemberships);
            node.setDomainInfluence(domainInfluence);
            node.setFeatureVector(featureVector);
            node.setBridgeNode(bridgeNode);
            node.setOutlierNode(outlierNode);
            node.setEvidenceFeatureCount(evidenceFeatureCount);
            node.setInfluenceZone(influenceZone);
            node.setNodeClassification(influenceZone);
            node.setInternalRisk(internalRisk);
            node.setExternalRisk(externalRisk);
        }
    }

    private Map<String, Set<String>> buildNeighborSignatures(Map<String, GraphNodeDTO> nodeById,
                                                             Map<String, List<GraphLinkDTO>> adjacency) {
        Map<String, Set<String>> signatures = new HashMap<>();
        for (String nodeId : nodeById.keySet()) {
            Set<String> signature = new LinkedHashSet<>();
            for (GraphLinkDTO link : adjacency.getOrDefault(nodeId, List.of())) {
                String otherId = nodeId.equals(link.getSource()) ? link.getTarget() : link.getSource();
                GraphNodeDTO other = nodeById.get(otherId);
                if (other == null) {
                    continue;
                }
                // Session is only the data container. Counting it as overlap would
                // make every entity in NETWORK_CAPTURE overlap with every other
                // entity and can explode the visualization.
                if ("analysissession".equals(safe(other.getType()))) {
                    continue;
                }
                signature.add(safe(other.getType()) + ":" + other.getId());
            }
            signatures.put(nodeId, signature);
        }
        return signatures;
    }

    private Map<String, List<OverlapEdge>> buildRelationGraph(Map<String, GraphNodeDTO> nodeById,
                                                              Map<String, Set<String>> neighborSignatures) {
        Map<String, List<OverlapEdge>> relationGraph = new HashMap<>();
        Map<String, List<String>> nodesBySharedFeature = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry : neighborSignatures.entrySet()) {
            GraphNodeDTO node = nodeById.get(entry.getKey());
            if (!isAnalyzableEntity(node)) {
                continue;
            }
            for (String feature : entry.getValue()) {
                nodesBySharedFeature.computeIfAbsent(feature, key -> new ArrayList<>()).add(entry.getKey());
            }
        }

        Map<String, PairAccumulator> pairAccumulators = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : nodesBySharedFeature.entrySet()) {
            List<String> ids = entry.getValue();
            if (ids.size() < 2) {
                continue;
            }
            if (ids.size() > MAX_SHARED_FEATURE_PAIRING) {
                continue;
            }
            double weight = sharedEntityWeight(entry.getKey());
            for (int i = 0; i < ids.size(); i++) {
                for (int j = i + 1; j < ids.size(); j++) {
                    String leftId = ids.get(i);
                    String rightId = ids.get(j);
                    String key = edgeKey(leftId, rightId);
                    pairAccumulators
                            .computeIfAbsent(key, ignored -> new PairAccumulator(leftId, rightId))
                            .add(weight);
                }
            }
        }

        for (PairAccumulator pair : pairAccumulators.values()) {
            Set<String> leftNeighbors = neighborSignatures.getOrDefault(pair.leftId(), Set.of());
            Set<String> rightNeighbors = neighborSignatures.getOrDefault(pair.rightId(), Set.of());
            int unionSize = leftNeighbors.size() + rightNeighbors.size() - pair.rawOverlap();
            if (unionSize <= 0) {
                continue;
            }
            double jaccard = pair.rawOverlap() / (double) unionSize;
            int degreeNormalizer = Math.max(leftNeighbors.size(), rightNeighbors.size());
            double adjustedOverlap = pair.weightedOverlap() / Math.max(1.0, Math.log(degreeNormalizer + 1.0));
            double score = clamp((jaccard * 0.45) + (Math.min(1.0, adjustedOverlap) * 0.55));

            OverlapEdge edge = new OverlapEdge(pair.leftId(), pair.rightId(), pair.rawOverlap(), pair.weightedOverlap(), adjustedOverlap, jaccard, score);
            relationGraph.computeIfAbsent(pair.leftId(), k -> new ArrayList<>()).add(edge);
            relationGraph.computeIfAbsent(pair.rightId(), k -> new ArrayList<>()).add(edge);
        }

        return relationGraph;
    }

    private Map<String, CommunityInfo> detectCommunities(Map<String, GraphNodeDTO> nodeById,
                                                         Map<String, List<OverlapEdge>> relationGraph) {
        Map<String, CommunityInfo> result = new HashMap<>();
        Set<String> visited = new HashSet<>();
        int index = 1;

        for (String nodeId : nodeById.keySet()) {
            if (visited.contains(nodeId) || hasUnsupportedType(nodeById.get(nodeId))) {
                continue;
            }

            Set<String> component = new LinkedHashSet<>();
            Queue<String> queue = new ArrayDeque<>();
            queue.add(nodeId);
            visited.add(nodeId);

            while (!queue.isEmpty()) {
                String current = queue.poll();
                component.add(current);
                for (OverlapEdge edge : relationGraph.getOrDefault(current, List.of())) {
                    if (edge.score() < DOMAIN_OVERLAP_THRESHOLD) {
                        continue;
                    }
                    String next = edge.other(current);
                    if (visited.add(next)) {
                        queue.add(next);
                    }
                }
            }

            String centerId = findCommunityCenter(component, relationGraph);
            String communityId = "D" + index++;
            CommunityInfo community = new CommunityInfo(communityId, centerId, component.size());
            for (String member : component) {
                result.put(member, community);
            }
        }

        return result;
    }

    private void appendOverlapLinks(List<GraphLinkDTO> links, Map<String, List<OverlapEdge>> relationGraph) {
        if (links == null || relationGraph.isEmpty()) {
            return;
        }

        Set<String> existing = new HashSet<>();
        for (GraphLinkDTO link : links) {
            if (link == null || link.getSource() == null || link.getTarget() == null) {
                continue;
            }
            existing.add(edgeKey(link.getSource(), link.getTarget()) + "::" + link.getType());
        }

        Set<String> emitted = new HashSet<>();
        for (List<OverlapEdge> edges : relationGraph.values()) {
            for (OverlapEdge edge : edges) {
                if (emitted.size() >= MAX_VISUAL_OVERLAP_LINKS) {
                    return;
                }
                if (edge.score() < BOUNDARY_OVERLAP_THRESHOLD) {
                    continue;
                }

                String pairKey = edgeKey(edge.leftId(), edge.rightId());
                String key = pairKey + "::OVERLAP";
                if (!emitted.add(pairKey) || existing.contains(key)) {
                    continue;
                }

                links.add(GraphLinkDTO.overlap(
                        edge.leftId(),
                        edge.rightId(),
                        edge.weightedOverlap(),
                        edge.score(),
                        edge.rawOverlap(),
                        edge.adjustedOverlap()
                ));
            }
        }
    }

    private void copyEnrichedLinks(List<GraphLinkDTO> target, List<GraphLinkDTO> enriched) {
        if (target == null) {
            return;
        }
        try {
            target.clear();
            target.addAll(enriched);
        } catch (UnsupportedOperationException ignored) {
            // Some callers pass immutable lists in unit tests. Node enrichment is
            // still valid; overlap edges are only appended when the caller list
            // supports mutation.
        }
    }

    private String findCommunityCenter(Set<String> component, Map<String, List<OverlapEdge>> relationGraph) {
        return component.stream()
                .max(Comparator.comparingDouble(nodeId -> relationGraph.getOrDefault(nodeId, List.of()).stream()
                        .filter(edge -> component.contains(edge.other(nodeId)))
                        .mapToDouble(OverlapEdge::adjustedOverlap)
                        .sum()))
                .orElse(null);
    }

    private OverlapSummary summarizeOverlap(String nodeId, Map<String, List<OverlapEdge>> relationGraph) {
        List<OverlapEdge> edges = relationGraph.getOrDefault(nodeId, List.of());
        if (edges.isEmpty()) {
            return new OverlapSummary(0, 0, 0, 0, 0);
        }

        double raw = edges.stream().mapToDouble(OverlapEdge::rawOverlap).sum();
        double weighted = edges.stream().mapToDouble(OverlapEdge::weightedOverlap).sum();
        double adjusted = edges.stream().mapToDouble(OverlapEdge::adjustedOverlap).sum();
        long overlapNeighbors = edges.stream()
                .filter(edge -> edge.score() >= BOUNDARY_OVERLAP_THRESHOLD)
                .count();
        return new OverlapSummary(raw, weighted, adjusted, (int) overlapNeighbors, edges.size());
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

    private boolean hasUnsupportedType(GraphNodeDTO node) {
        return !ANALYZABLE_TYPES.contains(safe(node.getType()));
    }

    private boolean isAnalyzableEntity(GraphNodeDTO node) {
        return node != null && !hasUnsupportedType(node);
    }

    private double sharedEntityWeight(String signature) {
        if (signature == null || signature.isBlank()) {
            return 0.10;
        }
        String type = signature;
        int separator = signature.indexOf(':');
        if (separator >= 0) {
            type = signature.substring(0, separator);
        }
        return ENTITY_OVERLAP_WEIGHTS.getOrDefault(safe(type), 0.35);
    }

    private boolean isKnownSuperNode(GraphNodeDTO node) {
        String value = safe(node.getValue());
        return Set.of(
                "google.com",
                "www.google.com",
                "cloudflare.com",
                "8.8.8.8",
                "8.8.4.4",
                "1.1.1.1",
                "1.0.0.1"
        ).contains(value);
    }

    private boolean hasWeakContextOnlyEvidence(GraphNodeDTO node, int degree, double relationStrength) {
        String type = safe(node.getType());
        if (!CONTEXT_ONLY_TYPES.contains(type)) return false;

        boolean lowRisk = node.getRiskScore() <= 25;
        boolean hasDangerSignal = hasDangerIndicator(node.getIndicators());
        boolean weakRelationship = degree <= 1 || relationStrength < 0.55;

        // IP/Domain/URL can be plain context artifacts. Keep weak, low-risk
        // artifacts outside regions until they have real danger evidence.
        return lowRisk && !hasDangerSignal && weakRelationship;
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

    private double internalRisk(GraphNodeDTO node, CommunityInfo community, OverlapSummary overlap) {
        if (community == null || community.size() <= 1) {
            return 0.0;
        }
        double centerDistance = node.getId().equals(community.centerId()) ? 0.0 : 1.0 / (overlap.adjustedWeightedOverlap() + 1.0);
        return clamp(1.0 / (centerDistance + 1.0));
    }

    private double externalRisk(GraphNodeDTO node, CommunityInfo community, OverlapSummary overlap) {
        if (community != null && community.size() > 1) {
            return 0.0;
        }
        double boundaryDistance = Math.max(0.0, BOUNDARY_OVERLAP_THRESHOLD - overlap.adjustedWeightedOverlap());
        return clamp(overlap.adjustedWeightedOverlap() / (boundaryDistance + 1.0));
    }

    private Map<String, Double> extractFeatureVector(GraphNodeDTO node,
                                                     int degree,
                                                     double relationStrength,
                                                     double density,
                                                     double propagation,
                                                     OverlapSummary overlap,
                                                     boolean bridgeNode) {
        Map<String, Double> vector = new LinkedHashMap<>();
        vector.put("risk", clamp(node.getRiskScore() / 100.0));
        vector.put("status", switch (safe(node.getStatus())) {
            case "fake" -> 1.0;
            case "suspicious" -> 0.5;
            default -> 0.0;
        });
        vector.put("riskLevel", switch (safe(node.getRiskLevel())) {
            case "high" -> 1.0;
            case "medium" -> 0.5;
            default -> 0.0;
        });
        vector.put("overlap", clamp(overlap.adjustedWeightedOverlap()));
        vector.put("relation", clamp(relationStrength));
        vector.put("density", clamp(density));
        vector.put("propagation", clamp(propagation));
        vector.put("centrality", clamp(Math.log(degree + 1.0) / Math.log(SUPER_NODE_THRESHOLD + 1.0)));
        vector.put("bridge", bridgeNode ? 1.0 : 0.0);
        vector.put("manualBlock", node.isManualBlocked() ? 1.0 : 0.0);
        vector.put("identityEvidence", identityEvidence(node));
        vector.put("accountArtifact", accountArtifactEvidence(node));
        vector.put("networkArtifact", networkArtifactEvidence(node));
        vector.put("contentArtifact", contentArtifactEvidence(node));
        vector.put("fileArtifact", fileArtifactEvidence(node));
        return vector;
    }

    private double identityEvidence(GraphNodeDTO node) {
        String type = safe(node.getType());
        double base = switch (type) {
            case "email", "victimaccount" -> 0.18;
            case "filehash" -> 0.14;
            case "ipaddress", "domain", "url" -> 0.10;
            default -> 0.08;
        };
        if (node.getValue() == null || node.getValue().isBlank()) {
            base *= 0.25;
        }
        if ("suspicious".equals(safe(node.getStatus()))) {
            base += 0.25;
        }
        if ("fake".equals(safe(node.getStatus()))) {
            base += 0.55;
        }
        base += clamp(node.getRiskScore() / 100.0) * 0.25;
        if (hasDangerIndicator(node.getIndicators())) {
            base += 0.30;
        }
        return clamp(base);
    }

    private double accountArtifactEvidence(GraphNodeDTO node) {
        String type = safe(node.getType());
        double base = switch (type) {
            case "victimaccount" -> 0.20;
            case "email" -> 0.16;
            default -> 0.05;
        };
        base += clamp(node.getRiskScore() / 100.0) * 0.25;
        if ("suspicious".equals(safe(node.getStatus()))) {
            base += 0.20;
        }
        if ("fake".equals(safe(node.getStatus()))) {
            base += 0.50;
        }
        if (hasDangerIndicator(node.getIndicators())) {
            base += 0.25;
        }
        return clamp(base);
    }

    private double networkArtifactEvidence(GraphNodeDTO node) {
        String type = safe(node.getType());
        double base = switch (type) {
            case "ipaddress", "domain" -> 0.18;
            case "url" -> 0.16;
            default -> 0.06;
        };
        base += clamp(node.getRiskScore() / 100.0) * 0.22;
        if ("suspicious".equals(safe(node.getStatus()))) {
            base += 0.18;
        }
        if ("fake".equals(safe(node.getStatus()))) {
            base += 0.42;
        }
        if (hasDangerIndicator(node.getIndicators())) {
            base += 0.36;
        }
        return clamp(base);
    }

    private double contentArtifactEvidence(GraphNodeDTO node) {
        String type = safe(node.getType());
        double base = switch (type) {
            case "url" -> 0.20;
            case "domain", "email" -> 0.16;
            case "filenode" -> 0.12;
            default -> 0.06;
        };
        base += clamp(node.getRiskScore() / 100.0) * 0.22;
        if ("suspicious".equals(safe(node.getStatus()))) {
            base += 0.20;
        }
        if ("fake".equals(safe(node.getStatus()))) {
            base += 0.42;
        }
        if (hasDangerIndicator(node.getIndicators())) {
            base += 0.34;
        }
        return clamp(base);
    }

    private double fileArtifactEvidence(GraphNodeDTO node) {
        String type = safe(node.getType());
        double base = switch (type) {
            case "filehash" -> 0.18;
            case "filenode" -> 0.16;
            case "url" -> 0.08;
            default -> 0.04;
        };
        base += clamp(node.getRiskScore() / 100.0) * 0.20;
        if ("suspicious".equals(safe(node.getStatus()))) {
            base += 0.18;
        }
        if ("fake".equals(safe(node.getStatus()))) {
            base += 0.40;
        }
        if (hasDangerIndicator(node.getIndicators())) {
            base += 0.38;
        }
        if (node.isManualBlocked()) {
            base += 0.35;
        }
        return clamp(base);
    }

    private int countEvidenceFeatures(Map<String, Double> featureVector) {
        int count = 0;
        for (double value : featureVector.values()) {
            if (value >= 0.20) {
                count++;
            }
        }
        return count;
    }

    private Map<String, Double> calculateDomainDistances(Map<String, Double> featureVector, GraphNodeDTO node) {
        Map<String, Double> distances = new LinkedHashMap<>();
        for (String domain : RISK_DOMAINS) {
            distances.put(domain, regionDistanceScore(featureVector, domain, node));
        }
        return distances;
    }

    private DomainFit nearestDomain(Map<String, Double> domainDistances) {
        String bestDomain = "safe";
        double bestDistance = Double.MAX_VALUE;
        for (Map.Entry<String, Double> entry : domainDistances.entrySet()) {
            if (entry.getValue() < bestDistance) {
                bestDomain = entry.getKey();
                bestDistance = entry.getValue();
            }
        }
        return new DomainFit(bestDomain, bestDistance == Double.MAX_VALUE ? 1.0 : bestDistance);
    }

    private Map<String, Double> calculateDomainInfluence(Map<String, Double> distances,
                                                         Map<String, Double> featureVector,
                                                         String membership) {
        Map<String, Double> influence = new LinkedHashMap<>();
        double outsideGate = outsideInfluenceGate(featureVector, membership);
        for (String domain : RISK_DOMAINS) {
            double value = 1.0 / (distances.getOrDefault(domain, 1.0) + 1.0);
            influence.put(domain, "OUTSIDE".equals(membership) ? value * outsideGate : value);
        }
        return influence;
    }

    private double outsideInfluenceGate(Map<String, Double> featureVector, String membership) {
        if (!"OUTSIDE".equals(membership)) {
            return 1.0;
        }
        double relation = featureVector.getOrDefault("relation", 0.0);
        double overlap = featureVector.getOrDefault("overlap", 0.0);
        double propagation = featureVector.getOrDefault("propagation", 0.0);
        double bridge = featureVector.getOrDefault("bridge", 0.0);
        return clamp(0.12 + relation * 0.18 + overlap * 0.30 + propagation * 0.25 + bridge * 0.15);
    }

    private Map<String, Double> calculateSoftMemberships(Map<String, Double> domainInfluence) {
        Map<String, Double> memberships = new LinkedHashMap<>();
        double total = domainInfluence.values().stream().mapToDouble(Double::doubleValue).sum();
        for (String domain : RISK_DOMAINS) {
            memberships.put(domain, total <= EPSILON ? 0.0 : domainInfluence.getOrDefault(domain, 0.0) / total);
        }
        return memberships;
    }

    private boolean isBridgeNode(GraphNodeDTO node,
                                 Map<String, List<OverlapEdge>> relationGraph,
                                 Map<String, GraphNodeDTO> nodeById) {
        return touchedDomains(node, relationGraph, nodeById).size() >= 2;
    }

    private Set<String> touchedDomains(GraphNodeDTO node,
                                       Map<String, List<OverlapEdge>> relationGraph,
                                       Map<String, GraphNodeDTO> nodeById) {
        Set<String> domains = new HashSet<>();
        if (node == null || node.getId() == null) {
            return domains;
        }
        domains.add(assignRiskDomain(node));
        for (OverlapEdge edge : relationGraph.getOrDefault(node.getId(), List.of())) {
            if (edge.score() < BOUNDARY_OVERLAP_THRESHOLD) {
                continue;
            }
            GraphNodeDTO other = nodeById.get(edge.other(node.getId()));
            if (other != null) {
                domains.add(assignRiskDomain(other));
            }
        }
        return domains;
    }

    private boolean isOutlierNode(Map<String, Double> domainInfluence) {
        return domainInfluence.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0) < OUTLIER_INFLUENCE_THRESHOLD;
    }

    private String determineInfluenceZone(boolean superNode,
                                          boolean bridgeNode,
                                          boolean multiDomainOverlap,
                                          boolean outlierNode,
                                          Map<String, Double> domainInfluence,
                                          String membership) {
        if (superNode) return "SUPER_INFLUENCE";
        if ("OUTSIDE".equals(membership)) return bridgeNode || multiDomainOverlap ? "OUTSIDE_INFLUENCE" : "OUTSIDE_NODE";
        if (outlierNode) return "OUTLIER";
        if (bridgeNode) return "BRIDGE";
        if (multiDomainOverlap || hasCompetingInfluence(domainInfluence)) return "OVERLAP";
        double maxInfluence = domainInfluence.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
        return maxInfluence >= 0.72 ? "CORE" : "BORDER";
    }

    private boolean hasCompetingInfluence(Map<String, Double> domainInfluence) {
        List<Double> sorted = domainInfluence.values().stream()
                .sorted(Comparator.reverseOrder())
                .toList();
        return sorted.size() >= 2
                && sorted.get(0) >= OUTLIER_INFLUENCE_THRESHOLD
                && sorted.get(0) - sorted.get(1) <= OVERLAP_INFLUENCE_GAP;
    }

    private boolean isMultiDomainOverlap(GraphNodeDTO node,
                                         Map<String, List<OverlapEdge>> relationGraph,
                                          Map<String, GraphNodeDTO> nodeById,
                                          OverlapSummary overlap,
                                          Map<String, Double> domainDistances,
                                         Map<String, Double> domainInfluence,
                                         String membership) {
        if ("OUTSIDE".equals(membership) && overlap.adjustedWeightedOverlap() < BOUNDARY_OVERLAP_THRESHOLD) {
            return false;
        }

        if (node == null || overlap.adjustedWeightedOverlap() < BOUNDARY_OVERLAP_THRESHOLD) {
            return hasCompetingInfluence(domainInfluence);
        }

        if (touchedDomains(node, relationGraph, nodeById).size() >= 2) {
            return true;
        }

        List<Double> sortedDistances = domainDistances.values().stream()
                .sorted()
                .toList();
        return sortedDistances.size() >= 2
                && sortedDistances.get(1) - sortedDistances.get(0) <= MULTI_DOMAIN_DISTANCE_GAP;
    }

    private double regionDistanceScore(Map<String, Double> vector, String domain, GraphNodeDTO node) {
        Map<String, Double> center = switch (domain) {
            case "fraud" -> Map.ofEntries(
                    Map.entry("risk", 0.92),
                    Map.entry("status", 1.00),
                    Map.entry("riskLevel", 1.00),
                    Map.entry("overlap", 0.90),
                    Map.entry("relation", 0.85),
                    Map.entry("density", 0.80),
                    Map.entry("propagation", 0.75),
                    Map.entry("centrality", 0.70),
                    Map.entry("bridge", 0.60),
                    Map.entry("manualBlock", 1.00),
                    Map.entry("identityEvidence", 0.80),
                    Map.entry("accountArtifact", 0.75),
                    Map.entry("networkArtifact", 0.85),
                    Map.entry("contentArtifact", 0.85),
                    Map.entry("fileArtifact", 0.90)
            );
            case "suspicious" -> Map.ofEntries(
                    Map.entry("risk", 0.52),
                    Map.entry("status", 0.50),
                    Map.entry("riskLevel", 0.50),
                    Map.entry("overlap", 0.45),
                    Map.entry("relation", 0.55),
                    Map.entry("density", 0.45),
                    Map.entry("propagation", 0.35),
                    Map.entry("centrality", 0.45),
                    Map.entry("bridge", 0.50),
                    Map.entry("manualBlock", 0.20),
                    Map.entry("identityEvidence", 0.55),
                    Map.entry("accountArtifact", 0.50),
                    Map.entry("networkArtifact", 0.60),
                    Map.entry("contentArtifact", 0.60),
                    Map.entry("fileArtifact", 0.55)
            );
            default -> Map.ofEntries(
                    Map.entry("risk", 0.08),
                    Map.entry("status", 0.00),
                    Map.entry("riskLevel", 0.00),
                    Map.entry("overlap", 0.05),
                    Map.entry("relation", 0.15),
                    Map.entry("density", 0.10),
                    Map.entry("propagation", 0.00),
                    Map.entry("centrality", 0.15),
                    Map.entry("bridge", 0.00),
                    Map.entry("manualBlock", 0.00),
                    Map.entry("identityEvidence", 0.25),
                    Map.entry("accountArtifact", 0.15),
                    Map.entry("networkArtifact", 0.20),
                    Map.entry("contentArtifact", 0.20),
                    Map.entry("fileArtifact", 0.15)
            );
        };

        double squared = 0.0;
        double weightTotal = 0.0;
        Map<String, Double> featureWeights = featureWeightsFor(node);
        for (Map.Entry<String, Double> entry : vector.entrySet()) {
            double diff = entry.getValue() - center.getOrDefault(entry.getKey(), 0.0);
            double weight = featureWeights.getOrDefault(entry.getKey(), 0.35);
            squared += weight * diff * diff;
            weightTotal += weight;
        }
        return Math.sqrt(squared / Math.max(EPSILON, weightTotal));
    }

    private Map<String, Double> featureWeightsFor(GraphNodeDTO node) {
        Map<String, Double> weights = new LinkedHashMap<>(COMMON_FEATURE_WEIGHTS);
        weights.putAll(TYPE_FEATURE_WEIGHTS.getOrDefault(safe(node == null ? null : node.getType()), Map.of()));
        return weights;
    }

    private String assignRiskDomain(GraphNodeDTO node) {
        String domain = safe(node.getDomainAssignment());
        if (RISK_DOMAINS.contains(domain)) {
            return domain;
        }
        return switch (safe(node.getRiskLevel())) {
            case "high" -> "fraud";
            case "medium" -> "suspicious";
            default -> "safe";
        };
    }

    private double confidence(GraphNodeDTO node,
                              int degree,
                              double relationStrength,
                              double adjustedOverlap,
                              boolean missingData,
                              boolean superNode) {
        double confidence = 0.30;
        if (node.getValue() != null && !node.getValue().isBlank()) confidence += 0.15;
        if (node.getIndicators() != null && !node.getIndicators().isEmpty()) confidence += 0.15;
        confidence += Math.min(0.22, degree * 0.035);
        confidence += relationStrength * 0.12;
        confidence += Math.min(0.20, adjustedOverlap * 0.20);
        if (missingData) confidence *= 0.55;
        if (superNode) confidence *= 0.75;
        return clamp(confidence);
    }

    private String membership(double centerDistance,
                              double relationStrength,
                              double adjustedOverlap,
                              int degree,
                              boolean missingData,
                              boolean unsupportedType,
                              boolean weakContextOnlyEvidence,
                              CommunityInfo community,
                              DomainFit domainFit) {
        if (unsupportedType) return "OUTSIDE";

        boolean closeToDomain = domainFit.distance() <= CORE_DOMAIN_DISTANCE_THRESHOLD;
        boolean nearDomainBoundary = domainFit.distance() <= BOUNDARY_DOMAIN_DISTANCE_THRESHOLD;
        boolean closeToSafeDomain = "safe".equals(domainFit.domain()) && closeToDomain;
        boolean strongGraphEvidence = relationStrength >= 0.55 || adjustedOverlap >= BOUNDARY_OVERLAP_THRESHOLD;

        if (degree == 0 && closeToSafeDomain) return "IN_REGION";
        if (degree == 0 && nearDomainBoundary) return "BOUNDARY";
        if (degree == 0) return "OUTSIDE";

        if (weakContextOnlyEvidence && closeToSafeDomain) return "IN_REGION";
        if (weakContextOnlyEvidence && nearDomainBoundary) return "BOUNDARY";
        if (weakContextOnlyEvidence) return "OUTSIDE";

        if (missingData && degree <= 1 && closeToSafeDomain) return "IN_REGION";
        if (missingData && degree <= 1 && nearDomainBoundary && strongGraphEvidence) return "BOUNDARY";
        if (missingData && degree <= 1) return "OUTSIDE";
        if (missingData && relationStrength < 0.45 && adjustedOverlap < BOUNDARY_OVERLAP_THRESHOLD) return "OUTSIDE";
        if (community != null && community.size() > 1 && adjustedOverlap >= DOMAIN_OVERLAP_THRESHOLD) return "IN_REGION";
        if (closeToDomain && strongGraphEvidence) return "IN_REGION";
        if (closeToSafeDomain) return "IN_REGION";
        if (centerDistance <= 0.22 && adjustedOverlap >= BOUNDARY_OVERLAP_THRESHOLD) return "IN_REGION";
        if (nearDomainBoundary || centerDistance <= 0.45 || strongGraphEvidence) return "BOUNDARY";
        return "OUTSIDE";
    }

    private String action(double risk, double confidence, String membership, boolean superNode, boolean missingData, boolean unsupportedType) {
        if (unsupportedType) return "collect_more_data";
        if (superNode) return "sampling";
        if ("BOUNDARY".equals(membership) && risk >= 0.45) return "manual_review";
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

    private record OverlapEdge(String leftId,
                               String rightId,
                               double rawOverlap,
                               double weightedOverlap,
                               double adjustedOverlap,
                               double jaccard,
                               double score) {
        private String other(String nodeId) {
            return leftId.equals(nodeId) ? rightId : leftId;
        }
    }

    private static final class PairAccumulator {
        private final String leftId;
        private final String rightId;
        private int rawOverlap;
        private double weightedOverlap;

        private PairAccumulator(String leftId, String rightId) {
            this.leftId = leftId;
            this.rightId = rightId;
        }

        private void add(double weight) {
            rawOverlap++;
            weightedOverlap += weight;
        }

        private String leftId() {
            return leftId;
        }

        private String rightId() {
            return rightId;
        }

        private int rawOverlap() {
            return rawOverlap;
        }

        private double weightedOverlap() {
            return weightedOverlap;
        }
    }

    private record OverlapSummary(double rawOverlap,
                                  double weightedOverlap,
                                  double adjustedWeightedOverlap,
                                  int overlapNeighborCount,
                                  int relationDegree) {
    }

    private record CommunityInfo(String communityId,
                                 String centerId,
                                 int size) {
    }

    private record DomainFit(String domain,
                             double distance) {
    }
}
