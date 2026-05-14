package com.example.servingwebcontent.controller;

import com.example.servingwebcontent.model.*;
import com.example.servingwebcontent.service.DomainRegionVisualizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller để hiển thị Domain Region Visualization
 * 
 * Endpoint:
 * GET /domain-visualization - Trang chính
 * GET /domain-visualization/html - Lấy HTML visualization
 * GET /domain-visualization/report - Lấy báo cáo chi tiết
 * POST /domain-visualization/demo - Tạo demo với sample data
 */
@Controller
@RequestMapping("/domain-visualization")
public class DomainRegionVisualizationController {

    @Autowired
    private DomainRegionVisualizationService visualizationService;

    // ============== Web Pages ==============

    /**
     * Trang chính - hiển thị visualization
     */
    @GetMapping("")
    public String showVisualization() {
        return "domain-region-visualization";
    }

    // ============== REST API ==============

    /**
     * Lấy HTML visualization
     */
    @GetMapping("/html")
    @ResponseBody
    public String getVisualizationHTML() {
        return visualizationService.generateVisualizationHTML();
    }

    /**
     * Lấy báo cáo chi tiết
     */
    @GetMapping("/report")
    @ResponseBody
    public String getDetailedReport() {
        return "<pre>" + visualizationService.generateDetailedReport() + "</pre>";
    }

    /**
     * Tạo demo với sample data
     */
    @PostMapping("/demo")
    @ResponseBody
    public Map<String, Object> createDemo(@RequestParam(defaultValue = "euclidean") String metric) {
        visualizationService.initializeStandardRegions();
        visualizationService.setDistanceMetric(metric);

        // Tạo sample nodes
        createSampleNodes();

        // Tính toán KNN
        visualizationService.computeKNNForAllNodes();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Demo created successfully");
        response.put("totalNodes", visualizationService.getTotalNodeCount());
        response.put("regions", visualizationService.getRegions().size());
        response.put("distanceMetric", visualizationService.getDistanceMetric().getName());

        return response;
    }

    /**
     * Tạo sample nodes cho demo
     */
    private void createSampleNodes() {
        // SAFE Region nodes
        NodeVisualization node1 = visualizationService.createNode(
            "user_safe_1", "UserA", "User",
            new double[]{1, 2, 3, 2, 0, 0.5},
            0.1,
            100.0, 320.0
        );
        visualizationService.addNodeToAppropriateRegion(node1);

        NodeVisualization node2 = visualizationService.createNode(
            "user_safe_2", "UserB", "User",
            new double[]{2, 1, 4, 1, 0, 0.3},
            0.15,
            150.0, 380.0
        );
        visualizationService.addNodeToAppropriateRegion(node2);

        NodeVisualization node3 = visualizationService.createNode(
            "ip_safe_1", "IPSafe", "IP",
            new double[]{1, 3, 2, 3, 0, 0.2},
            0.05,
            200.0, 340.0
        );
        visualizationService.addNodeToAppropriateRegion(node3);

        // SUSPICIOUS Region nodes
        NodeVisualization node4 = visualizationService.createNode(
            "user_susp_1", "UserC", "User",
            new double[]{5, 8, 10, 6, 3, 3.5},
            0.45,
            350.0, 150.0
        );
        visualizationService.addNodeToAppropriateRegion(node4);

        NodeVisualization node5 = visualizationService.createNode(
            "ip_susp_1", "IPSuspicious", "IP",
            new double[]{6, 7, 9, 7, 2, 3.2},
            0.50,
            420.0, 200.0
        );
        visualizationService.addNodeToAppropriateRegion(node5);

        NodeVisualization node6 = visualizationService.createNode(
            "domain_susp_1", "DomainX", "Domain",
            new double[]{4, 9, 8, 5, 4, 4.0},
            0.55,
            450.0, 250.0
        );
        visualizationService.addNodeToAppropriateRegion(node6);

        // FRAUD Region nodes
        NodeVisualization node7 = visualizationService.createNode(
            "bot_fraud_1", "BotA", "Bot",
            new double[]{15, 20, 25, 18, 8, 10.0},
            0.85,
            600.0, 50.0
        );
        visualizationService.addNodeToAppropriateRegion(node7);

        NodeVisualization node8 = visualizationService.createNode(
            "bot_fraud_2", "BotB", "Bot",
            new double[]{16, 21, 24, 19, 9, 9.5},
            0.88,
            680.0, 100.0
        );
        visualizationService.addNodeToAppropriateRegion(node8);

        NodeVisualization node9 = visualizationService.createNode(
            "malware_fraud_1", "MalwareX", "Malware",
            new double[]{14, 19, 26, 17, 7, 11.0},
            0.92,
            700.0, 60.0
        );
        visualizationService.addNodeToAppropriateRegion(node9);

        NodeVisualization node10 = visualizationService.createNode(
            "spam_fraud_1", "SpamNodeY", "Spam",
            new double[]{17, 22, 23, 20, 10, 8.5},
            0.90,
            630.0, 130.0
        );
        visualizationService.addNodeToAppropriateRegion(node10);
    }

    /**
     * Lấy danh sách các distance metrics có sẵn
     */
    @GetMapping("/metrics")
    @ResponseBody
    public Map<String, String> getAvailableMetrics() {
        Map<String, String> metrics = new HashMap<>();
        metrics.put("euclidean", "Khoảng cách Euclid (L2 norm)");
        metrics.put("manhattan", "Khoảng cách Manhattan (L1 norm)");
        metrics.put("minkowski", "Khoảng cách Minkowski (Lp norm)");
        metrics.put("hamming", "Khoảng cách Hamming (dữ liệu boolean)");
        metrics.put("cosine", "Khoảng cách Cosine (so sánh hướng)");
        return metrics;
    }

    /**
     * Lấy thông tin chi tiết về distance metric hiện tại
     */
    @GetMapping("/metric-info")
    @ResponseBody
    public Map<String, String> getMetricInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("name", visualizationService.getDistanceMetric().getName());
        info.put("description", visualizationService.getDistanceMetric().getDescription());
        return info;
    }
}
