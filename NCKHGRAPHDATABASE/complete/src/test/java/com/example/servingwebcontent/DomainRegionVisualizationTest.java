package com.example.servingwebcontent;

import com.example.servingwebcontent.model.*;
import com.example.servingwebcontent.service.DomainRegionVisualizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for Domain Region Visualization
 * 
 * Kiểm tra:
 * 1. Khởi tạo regions
 * 2. Thêm nodes
 * 3. Tính toán KNN
 * 4. Distance metrics
 * 5. Hiển thị visualization
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Domain Region Visualization Tests")
public class DomainRegionVisualizationTest {

    @Autowired
    private DomainRegionVisualizationService visualizationService;

    @BeforeEach
    public void setup() {
        visualizationService.initializeStandardRegions();
    }

    // ============== Test 1: Khởi tạo Regions ==============

    @Test
    @DisplayName("Should initialize 3 standard regions")
    public void testInitializeStandardRegions() {
        assertEquals(3, visualizationService.getRegions().size(),
                   "Should have 3 regions");

        RegionVisualization safeRegion = visualizationService.getRegion(RegionType.SAFE);
        RegionVisualization suspiciousRegion = visualizationService.getRegion(RegionType.SUSPICIOUS);
        RegionVisualization fraudRegion = visualizationService.getRegion(RegionType.FRAUD);

        assertNotNull(safeRegion, "SAFE region should exist");
        assertNotNull(suspiciousRegion, "SUSPICIOUS region should exist");
        assertNotNull(fraudRegion, "FRAUD region should exist");
    }

    @Test
    @DisplayName("Should have correct color schemes for regions")
    public void testColorSchemes() {
        RegionVisualization safeRegion = visualizationService.getRegion(RegionType.SAFE);
        RegionVisualization fraudRegion = visualizationService.getRegion(RegionType.FRAUD);

        // SAFE: xanh lá nhạt
        assertEquals("#90EE90", safeRegion.getColorScheme().getBackgroundColor(),
                   "SAFE background should be light green");
        assertEquals("#228B22", safeRegion.getColorScheme().getBorderColor(),
                   "SAFE border should be dark green");
        assertEquals("#1E90FF", safeRegion.getColorScheme().getNodeColor(),
                   "SAFE nodes should be blue");

        // FRAUD: đỏ nhạt
        assertEquals("#FFB6C1", fraudRegion.getColorScheme().getBackgroundColor(),
                   "FRAUD background should be light red");
        assertEquals("#DC143C", fraudRegion.getColorScheme().getBorderColor(),
                   "FRAUD border should be dark red");
        assertEquals("#8B0000", fraudRegion.getColorScheme().getNodeColor(),
                   "FRAUD nodes should be dark red");
    }

    // ============== Test 2: Tạo và Thêm Nodes ==============

    @Test
    @DisplayName("Should create node with correct attributes")
    public void testCreateNode() {
        NodeVisualization node = visualizationService.createNode(
            "test_node_1", "TestNode", "User",
            new double[]{1.0, 2.0, 3.0},
            0.25,
            100.0, 150.0
        );

        assertNotNull(node, "Node should not be null");
        assertEquals("test_node_1", node.getNodeId());
        assertEquals("TestNode", node.getNodeLabel());
        assertEquals("User", node.getNodeType());
        assertEquals(0.25, node.getRiskScore());
        assertEquals(100.0, node.getX());
        assertEquals(150.0, node.getY());
    }

    @Test
    @DisplayName("Should add node to appropriate region based on risk score")
    public void testAddNodeToAppropriateRegion() {
        // SAFE node (risk < 0.33)
        NodeVisualization safeNode = visualizationService.createNode(
            "safe_1", "SafeUser", "User",
            new double[]{1, 2, 3},
            0.10,
            100.0, 350.0
        );
        visualizationService.addNodeToAppropriateRegion(safeNode);

        RegionVisualization safeRegion = visualizationService.getRegion(RegionType.SAFE);
        assertEquals(1, safeRegion.getNodeCount(), "SAFE region should contain 1 node");

        // SUSPICIOUS node (0.33 <= risk < 0.67)
        NodeVisualization suspiciousNode = visualizationService.createNode(
            "susp_1", "SuspiciousUser", "User",
            new double[]{5, 8, 10},
            0.50,
            400.0, 200.0
        );
        visualizationService.addNodeToAppropriateRegion(suspiciousNode);

        RegionVisualization suspiciousRegion = visualizationService.getRegion(RegionType.SUSPICIOUS);
        assertEquals(1, suspiciousRegion.getNodeCount(), "SUSPICIOUS region should contain 1 node");

        // FRAUD node (risk >= 0.67)
        NodeVisualization fraudNode = visualizationService.createNode(
            "fraud_1", "FraudBot", "Bot",
            new double[]{15, 20, 25},
            0.85,
            650.0, 100.0
        );
        visualizationService.addNodeToAppropriateRegion(fraudNode);

        RegionVisualization fraudRegion = visualizationService.getRegion(RegionType.FRAUD);
        assertEquals(1, fraudRegion.getNodeCount(), "FRAUD region should contain 1 node");
    }

    @Test
    @DisplayName("Should add node to fraud region based on risk score")
    public void testAddNodeToSpecificRegion() {
        NodeVisualization node = visualizationService.createNode(
            "test_1", "TestNode", "User",
            new double[]{1, 2, 3},
            0.85,
            100.0, 150.0
        );

        visualizationService.addNodeToAppropriateRegion(node);

        RegionVisualization fraudRegion = visualizationService.getRegion(RegionType.FRAUD);
        assertEquals(1, fraudRegion.getNodeCount(), "FRAUD region should contain 1 node");
        assertEquals(RegionType.FRAUD, node.getRegionType(), "Node should belong to FRAUD region");
    }

    @Test
    @DisplayName("Should keep far node outside without creating a new region")
    public void testOutsideNodeDoesNotCreateNewRegion() {
        NodeVisualization outsideNode = visualizationService.createNode(
            "outside_1", "OutsideNode", "User",
            new double[]{1000, 1000, 1000},
            0.20,
            100.0, 150.0
        );

        visualizationService.addNodeToAppropriateRegion(outsideNode);

        assertEquals(3, visualizationService.getRegions().size(),
                   "OUTSIDE should not be created as a fourth region");
        assertNull(outsideNode.getRegionType(), "Outside node should not be assigned to a RegionType");
        assertEquals("OUTSIDE", outsideNode.getMembershipStatus());
        assertEquals(1, visualizationService.getOutsideNodes().size(),
                   "Outside node should be tracked separately from standard regions");
    }

    // ============== Test 3: Distance Metrics ==============

    @Test
    @DisplayName("Should compute Euclidean distance correctly")
    public void testEuclideanDistance() {
        DistanceMetric metric = new DistanceMetric.EuclideanDistance();

        double[] v1 = {0.0, 0.0};
        double[] v2 = {3.0, 4.0};

        double distance = metric.calculate(v1, v2);
        assertEquals(5.0, distance, 0.001, "Euclidean distance should be 5.0");
    }

    @Test
    @DisplayName("Should compute Manhattan distance correctly")
    public void testManhattanDistance() {
        DistanceMetric metric = new DistanceMetric.ManhattanDistance();

        double[] v1 = {0.0, 0.0};
        double[] v2 = {3.0, 4.0};

        double distance = metric.calculate(v1, v2);
        assertEquals(7.0, distance, 0.001, "Manhattan distance should be 7.0");
    }

    @Test
    @DisplayName("Should compute Hamming distance correctly")
    public void testHammingDistance() {
        DistanceMetric metric = new DistanceMetric.HammingDistance();

        double[] v1 = {1.0, 0.0, 1.0, 0.0};
        double[] v2 = {1.0, 1.0, 1.0, 0.0};

        double distance = metric.calculate(v1, v2);
        assertEquals(1.0, distance, 0.001, "Hamming distance should be 1.0");
    }

    @Test
    @DisplayName("Should compute Minkowski distance correctly")
    public void testMinkowskiDistance() {
        DistanceMetric metric = DistanceMetric.createMinkowski(2.0); // p=2 = Euclidean

        double[] v1 = {0.0, 0.0};
        double[] v2 = {3.0, 4.0};

        double distance = metric.calculate(v1, v2);
        assertEquals(5.0, distance, 0.001, "Minkowski(p=2) should equal Euclidean");
    }

    @Test
    @DisplayName("Should compute Cosine distance correctly")
    public void testCosineDistance() {
        DistanceMetric metric = new DistanceMetric.CosineDistance();

        double[] v1 = {1.0, 0.0};
        double[] v2 = {1.0, 0.0};

        double distance = metric.calculate(v1, v2);
        assertEquals(0.0, distance, 0.001, "Cosine distance should be 0.0 for identical vectors");
    }

    // ============== Test 4: KNN Computation ==============

    @Test
    @DisplayName("Should compute KNN neighbors")
    public void testComputeKNNNeighbors() {
        visualizationService.setDistanceMetric("euclidean");

        // Tạo 5 nodes
        NodeVisualization node1 = visualizationService.createNode(
            "n1", "Node1", "User",
            new double[]{0.0, 0.0},
            0.10, 100.0, 100.0
        );
        visualizationService.addNodeToAppropriateRegion(node1);

        NodeVisualization node2 = visualizationService.createNode(
            "n2", "Node2", "User",
            new double[]{1.0, 1.0},
            0.15, 110.0, 110.0
        );
        visualizationService.addNodeToAppropriateRegion(node2);

        NodeVisualization node3 = visualizationService.createNode(
            "n3", "Node3", "User",
            new double[]{2.0, 2.0},
            0.20, 120.0, 120.0
        );
        visualizationService.addNodeToAppropriateRegion(node3);

        NodeVisualization node4 = visualizationService.createNode(
            "n4", "Node4", "User",
            new double[]{10.0, 10.0},
            0.25, 200.0, 200.0
        );
        visualizationService.addNodeToAppropriateRegion(node4);

        // Tính KNN
        visualizationService.computeKNNForAllNodes();

        // Kiểm tra: node1 có 3 KNN gần nhất
        assertEquals(3, node1.getKnnNeighbors().size(),
                   "Node1 should have 3 KNN neighbors");

        // node2, node3 phải là neighbors của node1 (vì gần hơn node4)
        assertTrue(node1.getKnnNeighbors().contains(node2),
                  "Node2 should be neighbor of Node1");
        assertTrue(node1.getKnnNeighbors().contains(node3),
                  "Node3 should be neighbor of Node1");
    }

    @Test
    @DisplayName("Should not count outside nodes in KNN neighbors")
    public void testKnnShouldIgnoreOutsideNodes() {
        visualizationService.setDistanceMetric("euclidean");

        NodeVisualization node1 = visualizationService.createNode(
            "in_1", "InRegion1", "User",
            new double[]{0.0, 0.0},
            0.10, 100.0, 100.0
        );
        visualizationService.addNodeToAppropriateRegion(node1);

        NodeVisualization node2 = visualizationService.createNode(
            "in_2", "InRegion2", "User",
            new double[]{1.0, 1.0},
            0.15, 110.0, 110.0
        );
        visualizationService.addNodeToAppropriateRegion(node2);

        NodeVisualization node3 = visualizationService.createNode(
            "in_3", "InRegion3", "User",
            new double[]{2.0, 2.0},
            0.20, 120.0, 120.0
        );
        visualizationService.addNodeToAppropriateRegion(node3);

        NodeVisualization outsideNode = visualizationService.createNode(
            "outside_knn", "OutsideKnn", "User",
            new double[]{1000.0, 1000.0},
            0.10, 130.0, 130.0
        );
        visualizationService.addNodeToAppropriateRegion(outsideNode);

        visualizationService.computeKNNForAllNodes();

        assertEquals("OUTSIDE", outsideNode.getMembershipStatus());
        assertEquals(2, node1.getKnnNeighbors().size(),
                   "K should count only valid in-region neighbors");
        assertFalse(node1.getKnnNeighbors().contains(outsideNode),
                   "OUTSIDE node should not be used as a KNN neighbor");
    }

    @Test
    @DisplayName("Should compute distance to center vector")
    public void testDistanceToCenterVector() {
        visualizationService.setDistanceMetric("euclidean");

        double[] centerVector = {5.0, 5.0};
        RegionVisualization region = new RegionVisualization(
            RegionType.SAFE,
            centerVector,
            100.0,
            200.0, 200.0
        );

        NodeVisualization node = visualizationService.createNode(
            "n1", "Node1", "User",
            new double[]{5.0, 5.0},
            0.10, 200.0, 200.0
        );

        node.computeDistanceToCenterVector(centerVector, 
            visualizationService.getDistanceMetric());

        assertEquals(0.0, node.getDistanceToCenterVector(), 0.001,
                   "Distance should be 0 for identical vectors");
    }

    // ============== Test 5: Hiển thị ==============

    @Test
    @DisplayName("Should generate visualization HTML")
    public void testGenerateVisualizationHTML() {
        // Tạo sample nodes
        for (int i = 0; i < 3; i++) {
            NodeVisualization node = visualizationService.createNode(
                "node_" + i, "Node" + i, "User",
                new double[]{Math.random() * 10, Math.random() * 10},
                Math.random(),
                Math.random() * 400 + 100, Math.random() * 400 + 100
            );
            visualizationService.addNodeToAppropriateRegion(node);
        }

        String html = visualizationService.generateVisualizationHTML();

        assertNotNull(html, "HTML should not be null");
        assertTrue(html.contains("<!DOCTYPE html>"), "Should be valid HTML");
        assertTrue(html.contains("SAFE REGION"), "Should contain SAFE region");
        assertTrue(html.contains("SUSPICIOUS REGION"), "Should contain SUSPICIOUS region");
        assertTrue(html.contains("FRAUD REGION"), "Should contain FRAUD region");
        assertTrue(html.contains("svg"), "Should contain SVG elements");
    }

    @Test
    @DisplayName("Should generate detailed report")
    public void testGenerateDetailedReport() {
        // Tạo sample nodes
        NodeVisualization node1 = visualizationService.createNode(
            "node1", "TestNode", "User",
            new double[]{1.0, 2.0, 3.0},
            0.25,
            100.0, 100.0
        );
        visualizationService.addNodeToAppropriateRegion(node1);

        String report = visualizationService.generateDetailedReport();

        assertNotNull(report, "Report should not be null");
        assertTrue(report.contains("DOMAIN REGION VISUALIZATION"), "Should contain title");
        assertTrue(report.contains("TestNode"), "Should contain node label");
        assertTrue(report.length() > 100, "Report should have substantial content");
    }

    // ============== Test 6: Tổng thể ==============

    @Test
    @DisplayName("Should handle complete workflow")
    public void testCompleteWorkflow() {
        visualizationService.setDistanceMetric("euclidean");

        // 1. Khởi tạo regions
        assertEquals(3, visualizationService.getRegions().size());

        // 2. Tạo nodes
        NodeVisualization safeNode = visualizationService.createNode(
            "safe", "SafeUser", "User",
            new double[]{1, 2, 3},
            0.10, 100.0, 350.0
        );
        visualizationService.addNodeToAppropriateRegion(safeNode);

        // 3. Tính KNN
        visualizationService.computeKNNForAllNodes();

        // 4. Kiểm tra
        assertEquals(1, visualizationService.getTotalNodeCount());
        assertEquals(RegionType.SAFE, safeNode.getRegionType());
    }
}
