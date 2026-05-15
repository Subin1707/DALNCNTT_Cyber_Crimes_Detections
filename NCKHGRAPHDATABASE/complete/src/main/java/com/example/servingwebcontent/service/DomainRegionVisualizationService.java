package com.example.servingwebcontent.service;

import com.example.servingwebcontent.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ============================================================
 * DOMAIN REGION VISUALIZATION SERVICE - FIX FULL VERSION
 * ============================================================
 *
 * Chức năng:
 * - Phân miền hành vi
 * - KNN behavior analysis
 * - Distance-based classification
 * - Force clustering
 * - Auto positioning
 * - Region gravity
 * - SVG visualization
 * - Chống node overlap
 *
 * ============================================================
 */

@Service
public class DomainRegionVisualizationService {

    // ============================================================
    // FIELDS
    // ============================================================

    private final List<RegionVisualization> regions =
            new ArrayList<>();

    private DistanceMetric distanceMetric =
            new DistanceMetric.EuclideanDistance();

    private final Random random = new Random();

    // ============================================================
    // INITIALIZE REGIONS
    // ============================================================

    public void initializeStandardRegions() {

        regions.clear();

        // =====================================================
        // SAFE REGION
        // =====================================================

        RegionVisualization safeRegion =
                new RegionVisualization(
                        RegionType.SAFE,
                        new double[]{
                                1.0, 1.0, 1.0,
                                1.0, 0.5, 0.5
                        },
                        170.0,
                        180.0,
                        340.0
                );

        // =====================================================
        // SUSPICIOUS REGION
        // =====================================================

        RegionVisualization suspiciousRegion =
                new RegionVisualization(
                        RegionType.SUSPICIOUS,
                        new double[]{
                                5.0, 8.0, 10.0,
                                6.0, 3.0, 3.5
                        },
                        150.0,
                        450.0,
                        220.0
                );

        // =====================================================
        // FRAUD REGION
        // =====================================================

        RegionVisualization fraudRegion =
                new RegionVisualization(
                        RegionType.FRAUD,
                        new double[]{
                                15.0, 20.0, 25.0,
                                18.0, 8.0, 10.0
                        },
                        170.0,
                        720.0,
                        120.0
                );

        regions.add(safeRegion);
        regions.add(suspiciousRegion);
        regions.add(fraudRegion);
    }

    // ============================================================
    // CREATE NODE
    // ============================================================

    /**
     * Tạo node mới
     * KHÔNG cần truyền x y
     * Hệ thống tự tính toán vị trí
     */

    public NodeVisualization createNode(
            String nodeId,
            String nodeLabel,
            String nodeType,
            double[] featureVector,
            double riskScore
    ) {

        NodeVisualization node =
                new NodeVisualization(
                        nodeId,
                        nodeLabel,
                        nodeType,
                        featureVector,
                        riskScore,
                        0,
                        0
                );

        // =====================================================
        // TÌM REGION GẦN NHẤT
        // =====================================================

        RegionVisualization nearestRegion =
                findNearestRegion(node);

        // =====================================================
        // GÁN REGION
        // =====================================================

        node.setAssignedRegion(
                nearestRegion.getRegionType()
        );

        // =====================================================
        // TÍNH TOÁN POSITION
        // =====================================================

        calculateNodePosition(
                node,
                nearestRegion
        );

        // =====================================================
        // THÊM NODE VÀO REGION
        // =====================================================

        nearestRegion.addNode(node);

        return node;
    }

    /**
     * Backward-compatible overload used by the visualization controller.
     * The node is positioned with the provided coordinates and can be added
     * later through addNodeToAppropriateRegion.
     */
    public NodeVisualization createNode(
            String nodeId,
            String nodeLabel,
            String nodeType,
            double[] featureVector,
            double riskScore,
            double x,
            double y
    ) {

        return new NodeVisualization(
                nodeId,
                nodeLabel,
                nodeType,
                featureVector,
                riskScore,
                x,
                y
        );
    }

    public void addNodeToAppropriateRegion(NodeVisualization node) {
        if (node == null) {
            return;
        }

        RegionVisualization nearestRegion =
                findNearestRegion(node);

        if (nearestRegion == null) {
            return;
        }

        node.setAssignedRegion(
                nearestRegion.getRegionType()
        );

        nearestRegion.addNode(node);
    }

    // ============================================================
    // FIND NEAREST REGION
    // ============================================================

    /**
     * Tìm miền gần nhất theo khoảng cách hành vi
     */

    private RegionVisualization findNearestRegion(
            NodeVisualization node
    ) {

        RegionVisualization nearestRegion = null;

        double minDistance = Double.MAX_VALUE;

        for (RegionVisualization region : regions) {

            double distance =
                    distanceMetric.calculate(
                            node.getFeatureVector(),
                            region.getCenterVector()
                    );

            // =================================================
            // LƯU DISTANCE
            // =================================================

            node.addRegionDistance(
                    region.getRegionType(),
                    distance
            );

            // =================================================
            // TÌM MIN DISTANCE
            // =================================================

            if (distance < minDistance) {

                minDistance = distance;

                nearestRegion = region;
            }
        }

        return nearestRegion;
    }

    // ============================================================
    // POSITION ENGINE
    // ============================================================

    /**
     * Tự động tính vị trí node trong region
     */

    private void calculateNodePosition(
            NodeVisualization node,
            RegionVisualization region
    ) {

        // =====================================================
        // RANDOM GÓC
        // =====================================================

        double angle =
                random.nextDouble() * 2 * Math.PI;

        // =====================================================
        // RANDOM BÁN KÍNH
        // =====================================================

        double radius =
                random.nextDouble()
                        * (region.getRadius() * 0.75);

        // =====================================================
        // OFFSET
        // =====================================================

        double offsetX =
                Math.cos(angle) * radius;

        double offsetY =
                Math.sin(angle) * radius;

        // =====================================================
        // FINAL POSITION
        // =====================================================

        double finalX =
                region.getCenterX() + offsetX;

        double finalY =
                region.getCenterY() + offsetY;

        node.setX(finalX);
        node.setY(finalY);
    }

    // ============================================================
    // FORCE CLUSTERING
    // ============================================================

    /**
     * Kéo node về tâm region
     */

    public void applyForceClustering() {

        for (RegionVisualization region : regions) {

            for (NodeVisualization node
                    : region.getNodes()) {

                moveNodeTowardRegion(
                        node,
                        region
                );
            }
        }
    }

    // ============================================================
    // MOVE NODE TOWARD REGION
    // ============================================================

    private void moveNodeTowardRegion(
            NodeVisualization node,
            RegionVisualization region
    ) {

        double dx =
                region.getCenterX() - node.getX();

        double dy =
                region.getCenterY() - node.getY();

        // =====================================================
        // FORCE
        // =====================================================

        double force = 0.05;

        // =====================================================
        // APPLY FORCE
        // =====================================================

        node.setX(
                node.getX() + dx * force
        );

        node.setY(
                node.getY() + dy * force
        );
    }

    // ============================================================
    // AVOID OVERLAP
    // ============================================================

    /**
     * Chống node đè lên nhau
     */

    public void avoidNodeOverlap() {

        List<NodeVisualization> allNodes =
                getAllNodes();

        for (int i = 0; i < allNodes.size(); i++) {

            NodeVisualization nodeA =
                    allNodes.get(i);

            for (int j = i + 1;
                 j < allNodes.size();
                 j++) {

                NodeVisualization nodeB =
                        allNodes.get(j);

                double dx =
                        nodeA.getX() - nodeB.getX();

                double dy =
                        nodeA.getY() - nodeB.getY();

                double distance =
                        Math.sqrt(dx * dx + dy * dy);

                // =================================================
                // NẾU QUÁ GẦN
                // =================================================

                if (distance < 25) {

                    double pushForce = 8;

                    nodeA.setX(
                            nodeA.getX() + pushForce
                    );

                    nodeB.setX(
                            nodeB.getX() - pushForce
                    );
                }
            }
        }
    }

    // ============================================================
    // COMPUTE KNN
    // ============================================================

    public void computeKNNForAllNodes() {

        List<NodeVisualization> allNodes =
                getAllNodes();

        for (RegionVisualization region : regions) {

            for (NodeVisualization node
                    : region.getNodes()) {

                node.computeKNNNeighbors(
                        allNodes,
                        distanceMetric
                );

                node.computeDistanceToCenterVector(
                        region.getCenterVector(),
                        distanceMetric
                );
            }
        }
    }

    // ============================================================
    // UPDATE VISUALIZATION ENGINE
    // ============================================================

    /**
     * Update toàn bộ visualization
     */

    public void updateVisualization() {

        applyForceClustering();

        avoidNodeOverlap();

        computeKNNForAllNodes();
    }

    public String generateVisualizationHTML() {
        if (regions.isEmpty()) {
            initializeStandardRegions();
        }

        updateVisualization();

        int svgWidth = 960;
        int svgHeight = 560;

        StringBuilder html = new StringBuilder();
        html.append("""
                <div class="domain-region-visualization">
                    <svg width="960" height="560" viewBox="0 0 960 560" xmlns="http://www.w3.org/2000/svg">
                        <defs>
                            <filter id="glow">
                                <feGaussianBlur stdDeviation="3.5" result="coloredBlur"/>
                                <feMerge>
                                    <feMergeNode in="coloredBlur"/>
                                    <feMergeNode in="SourceGraphic"/>
                                </feMerge>
                            </filter>
                        </defs>
                        <rect width="960" height="560" fill="#f8fafc"/>
                """);

        for (RegionVisualization region : regions) {
            html.append(region.toSVG(svgWidth, svgHeight));
        }

        html.append("""
                    </svg>
                </div>
                """);

        return html.toString();
    }

    public String generateDetailedReport() {
        if (regions.isEmpty()) {
            initializeStandardRegions();
        }

        StringBuilder report = new StringBuilder();
        report.append("========== DOMAIN REGION VISUALIZATION REPORT ==========\n");
        report.append("Distance metric: ")
                .append(distanceMetric.getName())
                .append("\n");
        report.append("Total nodes: ")
                .append(getTotalNodeCount())
                .append("\n\n");

        for (RegionVisualization region : regions) {
            report.append(region.getStandardReportModel())
                    .append("\n");

            for (NodeVisualization node : region.getNodes()) {
                report.append(node.getDetailedDescription())
                        .append("\n");
            }
        }

        return report.toString();
    }

    // ============================================================
    // GET ALL NODES
    // ============================================================

    private List<NodeVisualization> getAllNodes() {

        List<NodeVisualization> allNodes =
                new ArrayList<>();

        for (RegionVisualization region : regions) {

            allNodes.addAll(region.getNodes());
        }

        return allNodes;
    }

    // ============================================================
    // DISTANCE METRIC
    // ============================================================

    public void setDistanceMetric(
            String metricName
    ) {

        this.distanceMetric =
                DistanceMetric.getMetric(
                        metricName
                );
    }

    public void setDistanceMetric(
            DistanceMetric metric
    ) {

        this.distanceMetric = metric;
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public List<RegionVisualization> getRegions() {

        return new ArrayList<>(regions);
    }

    public RegionVisualization getRegion(
            RegionType type
    ) {

        return regions.stream()
                .filter(r ->
                        r.getRegionType() == type
                )
                .findFirst()
                .orElse(null);
    }

    public int getTotalNodeCount() {

        return getAllNodes().size();
    }

    public DistanceMetric getDistanceMetric() {

        return distanceMetric;
    }

    public String getDistanceMetricDescription() {

        return distanceMetric.getDescription();
    }

    // ============================================================
    // DEBUG REPORT
    // ============================================================

    public void printRegionStatistics() {

        System.out.println(
                "\n========== REGION STATISTICS =========="
        );

        for (RegionVisualization region : regions) {

            System.out.println(
                    "\nRegion: "
                            + region.getRegionType()
            );

            System.out.println(
                    "Total Nodes: "
                            + region.getNodes().size()
            );

            System.out.println(
                    "Average Risk: "
                            + region.getAverageRiskScore()
            );
        }
    }
}
