package com.example.servingwebcontent.model;

import java.util.*;

/**
 * NODE - Đối tượng dữ liệu trong không gian miền
 *
 * Chức năng:
 * - Đại diện cho thực thể dữ liệu
 * - Hỗ trợ KNN
 * - Hỗ trợ phương pháp miền
 * - Hỗ trợ clustering
 * - Hỗ trợ force-directed visualization
 * - Hỗ trợ phân tích xác suất
 */
public class NodeVisualization {

    // =====================================================
    // BASIC INFO
    // =====================================================

    private String nodeId;

    private String nodeLabel;

    private String nodeType;

    // =====================================================
    // FEATURES
    // =====================================================

    private double[] featureVector;

    private double riskScore;

    // =====================================================
    // REGION
    // =====================================================

    private RegionType regionType;

    private RegionVisualization region;

    private RegionType nearestRegionType;

    private String membershipStatus = "IN_REGION";

    // =====================================================
    // POSITION
    // =====================================================

    private double x;

    private double y;

    // =====================================================
    // MOVEMENT
    // =====================================================

    private double velocityX = 0;

    private double velocityY = 0;

    private double damping = 0.90;

    private double attractionForce = 0.03;

    // =====================================================
    // KNN
    // =====================================================

    private List<NodeVisualization> knnNeighbors =
            new ArrayList<>();

    private int k = 7;

    // =====================================================
    // DISTANCE
    // =====================================================

    private double distanceToCenterVector;

    private Map<RegionType, Double> regionDistances =
            new HashMap<>();

    // =====================================================
    // ANALYSIS
    // =====================================================

    private double confidenceScore;

    private double weight;

    // =====================================================
    // STATUS
    // =====================================================

    private NodeStatus status;

    // =====================================================
    // ENUM
    // =====================================================

    public enum NodeStatus {

        NORMAL(
                "Normal",
                "Node bình thường"
        ),

        WARNING(
                "Warning",
                "Node nghi ngờ"
        ),

        ANALYZING(
                "Analyzing",
                "Node đang phân tích"
        ),

        DANGEROUS(
                "Dangerous",
                "Node nguy hiểm"
        );

        private final String label;

        private final String description;

        NodeStatus(
                String label,
                String description
        ) {

            this.label = label;
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
            return description;
        }
    }

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public NodeVisualization(
            String nodeId,
            String nodeLabel,
            String nodeType,
            double[] featureVector,
            double riskScore,
            double x,
            double y
    ) {

        this.nodeId = nodeId;

        this.nodeLabel = nodeLabel;

        this.nodeType = nodeType;

        this.featureVector = featureVector;

        this.riskScore =
                Math.max(
                        0.0,
                        Math.min(1.0, riskScore)
                );

        this.x = x;

        this.y = y;

        this.weight = 1.0;

        this.status =
                determineStatus(this.riskScore);
    }

    // =====================================================
    // STATUS
    // =====================================================

    private NodeStatus determineStatus(
            double risk
    ) {

        if (risk < 0.33) {
            return NodeStatus.NORMAL;
        }

        if (risk < 0.67) {
            return NodeStatus.WARNING;
        }

        return NodeStatus.DANGEROUS;
    }

    // =====================================================
    // KNN
    // =====================================================

    public void computeKNNNeighbors(
            List<NodeVisualization> candidateNodes,
            DistanceMetric distanceMetric
    ) {
        this.knnNeighbors.clear();

        if (candidateNodes == null ||
                candidateNodes.isEmpty()) {

            return;
        }

        List<NodeDistance> distances =
                new ArrayList<>();

        for (NodeVisualization candidate
                : candidateNodes) {

            if (!candidate.equals(this)
                    && candidate.getRegionType() != null
                    && !candidate.isOutsideAnyRegion()) {

                double distance =
                        calculateComparableDistance(
                                this.featureVector,
                                candidate.featureVector,
                                distanceMetric
                        );

                if (Double.isNaN(distance) || Double.isInfinite(distance) || distance == Double.MAX_VALUE) {
                    continue;
                }

                distances.add(
                        new NodeDistance(
                                candidate,
                                distance
                        )
                );
            }
        }

        distances.sort(
                Comparator.comparingDouble(
                        d -> d.distance
                )
        );

        int limit =
                Math.min(k, distances.size());

        for (int i = 0; i < limit; i++) {

            this.knnNeighbors.add(
                    distances.get(i).node
            );
        }
    }

    private double calculateComparableDistance(
            double[] vector1,
            double[] vector2,
            DistanceMetric distanceMetric
    ) {

        if (vector1 == null || vector2 == null || vector1.length == 0 || vector2.length == 0 || distanceMetric == null) {
            return Double.MAX_VALUE;
        }

        if (vector1.length == vector2.length) {
            return distanceMetric.calculate(vector1, vector2);
        }

        int comparableLength = Math.min(vector1.length, vector2.length);
        return distanceMetric.calculate(
                Arrays.copyOf(vector1, comparableLength),
                Arrays.copyOf(vector2, comparableLength)
        );
    }

    // =====================================================
    // KNN VOTING
    // =====================================================

    public Map<RegionType, Double> analyzeKNNVoting() {

        Map<RegionType, Integer> counts =
                new HashMap<>();

        for (NodeVisualization neighbor
                : knnNeighbors) {

            if (neighbor.getRegionType() != null) {

                counts.put(
                        neighbor.getRegionType(),
                        counts.getOrDefault(
                                neighbor.getRegionType(),
                                0
                        ) + 1
                );
            }
        }

        Map<RegionType, Double> percentages =
                new HashMap<>();

        int validVoters = counts.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

        if (validVoters == 0) {
            return percentages;
        }

        for (Map.Entry<RegionType, Integer> entry
                : counts.entrySet()) {

            percentages.put(
                    entry.getKey(),
                    entry.getValue() * 100.0
                            / validVoters
            );
        }

        return percentages;
    }

    // =====================================================
    // REGION DISTANCES
    // =====================================================

    public void calculateRegionDistances(
            List<RegionVisualization> regions,
            DistanceMetric metric
    ) {

        regionDistances.clear();

        for (RegionVisualization region
                : regions) {

            double distance =
                    metric.calculate(
                            this.featureVector,
                            region.getCenterVector()
                    );

            regionDistances.put(
                    region.getRegionType(),
                    distance
            );
        }
    }

    // =====================================================
    // FIND NEAREST REGION
    // =====================================================

    public RegionType findNearestRegion() {

        if (regionDistances.isEmpty()) {
            return null;
        }

        return regionDistances.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .get()
                .getKey();
    }

    // =====================================================
    // CENTER DISTANCE
    // =====================================================

    public void computeDistanceToCenterVector(
            double[] centerVector,
            DistanceMetric distanceMetric
    ) {

        this.distanceToCenterVector =
                distanceMetric.calculate(
                        this.featureVector,
                        centerVector
                );
    }

    // =====================================================
    // CONFIDENCE
    // =====================================================

    public void calculateConfidenceScore() {

        if (regionDistances.isEmpty()) {

            confidenceScore = 0;

            return;
        }

        double minDistance =
                Collections.min(
                        regionDistances.values()
                );

        confidenceScore =
                1.0 / (1.0 + minDistance);
    }

    // =====================================================
    // MOVEMENT
    // =====================================================

    public void moveTowardRegion(
            RegionVisualization targetRegion
    ) {

        if (targetRegion == null) {
            return;
        }

        double dx =
                targetRegion.getCenterX() - this.x;

        double dy =
                targetRegion.getCenterY() - this.y;

        velocityX += dx * attractionForce;

        velocityY += dy * attractionForce;

        velocityX *= damping;

        velocityY *= damping;

        this.x += velocityX;

        this.y += velocityY;
    }

    // =====================================================
    // SVG
    // =====================================================

    public String toSVG(
            String nodeColor
    ) {

        double radius =
                6 + (riskScore * 12);

        String extraStyle = "";

        if (status == NodeStatus.ANALYZING) {

            extraStyle =
                    """
                    stroke="white"
                    stroke-width="2"
                    """;
        }

        if (status == NodeStatus.DANGEROUS) {

            extraStyle =
                    """
                    stroke="#ff0000"
                    stroke-width="2"
                    filter="url(#glow)"
                    """;
        }

        return String.format(
                """
                <g class="node-group">

                    <circle
                        cx="%f"
                        cy="%f"
                        r="%f"
                        fill="%s"
                        opacity="0.85"
                        %s
                    />

                    <text
                        x="%f"
                        y="%f"
                        font-size="10"
                        text-anchor="middle"
                        fill="#333"
                    >
                        %s
                    </text>

                    <title>
                        Node: %s
                        Type: %s
                        Risk: %.2f%%
                        Confidence: %.2f%%
                    </title>

                </g>
                """,

                x,
                y,
                radius,
                nodeColor,
                extraStyle,

                x,
                y - radius - 5,
                nodeLabel,

                nodeLabel,
                nodeType,
                riskScore * 100,
                confidenceScore * 100
        );
    }

    // =====================================================
    // DESCRIPTION
    // =====================================================

    public String getDetailedDescription() {

        StringBuilder desc =
                new StringBuilder();

        desc.append("""
                ╔══════════════════════════════════════╗
                """);

        desc.append("\n");

        desc.append("NODE: ")
                .append(nodeLabel)
                .append("\n");

        desc.append("TYPE: ")
                .append(nodeType)
                .append("\n");

        desc.append("REGION: ")
                .append(regionType)
                .append("\n");

        desc.append("MEMBERSHIP: ")
                .append(membershipStatus)
                .append("\n");

        if (nearestRegionType != null) {
            desc.append("NEAREST REGION: ")
                    .append(nearestRegionType)
                    .append("\n");
        }

        desc.append("RISK SCORE: ")
                .append(riskScore * 100)
                .append("%\n");

        desc.append("CONFIDENCE: ")
                .append(confidenceScore * 100)
                .append("%\n");

        desc.append("POSITION: (")
                .append(x)
                .append(", ")
                .append(y)
                .append(")\n");

        desc.append("DISTANCE TO CENTER: ")
                .append(distanceToCenterVector)
                .append("\n");

        desc.append("KNN NEIGHBORS: ")
                .append(knnNeighbors.size())
                .append("\n");

        desc.append("\nREGION DISTANCES:\n");

        for (Map.Entry<RegionType, Double> entry
                : regionDistances.entrySet()) {

            desc.append("- ")
                    .append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\n");
        }

        desc.append("""
                ╚══════════════════════════════════════╝
                """);

        return desc.toString();
    }

    // =====================================================
    // NODE DISTANCE
    // =====================================================

    private static class NodeDistance {

        NodeVisualization node;

        double distance;

        NodeDistance(
                NodeVisualization node,
                double distance
        ) {

            this.node = node;
            this.distance = distance;
        }
    }

    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public double[] getFeatureVector() {
        return featureVector;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public String getNodeType() {
        return nodeType;
    }

    public RegionType getRegionType() {
        return regionType;
    }

    public RegionVisualization getRegion() {
        return region;
    }

    public RegionType getNearestRegionType() {
        return nearestRegionType;
    }

    public String getMembershipStatus() {
        return membershipStatus;
    }

    public boolean isOutsideAnyRegion() {
        return "OUTSIDE".equals(membershipStatus);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public List<NodeVisualization> getKnnNeighbors() {
        return new ArrayList<>(knnNeighbors);
    }

    public double getDistanceToCenterVector() {
        return distanceToCenterVector;
    }

    public double getWeight() {
        return weight;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public Map<RegionType, Double> getRegionDistances() {
        return regionDistances;
    }

    public void setAssignedRegion(RegionType regionType) {
        this.regionType = regionType;
        if (regionType != null) {
            this.nearestRegionType = regionType;
        }
        this.membershipStatus = regionType == null ? "OUTSIDE" : "IN_REGION";
    }

    public void setNearestRegionType(RegionType nearestRegionType) {
        this.nearestRegionType = nearestRegionType;
    }

    public void setMembershipStatus(String membershipStatus) {
        if (membershipStatus == null || membershipStatus.isBlank()) {
            this.membershipStatus = "IN_REGION";
            return;
        }
        this.membershipStatus = membershipStatus;
    }

    public void addRegionDistance(RegionType regionType, double distance) {
        if (regionType == null) {
            return;
        }
        regionDistances.put(regionType, distance);
    }

    public void setRegion(
            RegionVisualization region
    ) {

        this.region = region;

        this.regionType =
                region.getRegionType();

        this.nearestRegionType =
                region.getRegionType();

        this.membershipStatus = "IN_REGION";
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public void setK(int k) {
        this.k = Math.max(1, k);
    }

    public int getK() {
        return k;
    }
}
