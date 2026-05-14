package com.example.servingwebcontent.service;

import com.example.servingwebcontent.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service để quản lý và hiển thị Domain Regions
 * 
 * Chức năng:
 * - Tạo regions (miền)
 * - Thêm nodes vào regions
 * - Tính toán KNN
 * - Hiển thị visualization
 * - Xuất báo cáo
 */
@Service
public class DomainRegionVisualizationService {

    private List<RegionVisualization> regions = new ArrayList<>();
    private DistanceMetric distanceMetric = new DistanceMetric.EuclideanDistance();

    // ============== Khởi tạo Regions ==============

    /**
     * Khởi tạo 3 regions tiêu chuẩn: SAFE, SUSPICIOUS, FRAUD
     * Với tọa độ tâm phù hợp cho hiển thị
     */
    public void initializeStandardRegions() {
        regions.clear();

        // SAFE REGION: Bên trái dưới
        RegionVisualization safeRegion = new RegionVisualization(
            RegionType.SAFE,
            new double[]{1.0, 1.0, 1.0, 1.0, 0.5, 0.5},
            150.0,
            150.0, 350.0
        );
        regions.add(safeRegion);

        // SUSPICIOUS REGION: Giữa
        RegionVisualization suspiciousRegion = new RegionVisualization(
            RegionType.SUSPICIOUS,
            new double[]{5.0, 8.0, 10.0, 6.0, 3.0, 3.5},
            120.0,
            400.0, 200.0
        );
        regions.add(suspiciousRegion);

        // FRAUD REGION: Bên phải trên
        RegionVisualization fraudRegion = new RegionVisualization(
            RegionType.FRAUD,
            new double[]{15.0, 20.0, 25.0, 18.0, 8.0, 10.0},
            150.0,
            650.0, 100.0
        );
        regions.add(fraudRegion);
    }

    // ============== Quản lý Nodes ==============

    /**
     * Tạo node mới
     */
    public NodeVisualization createNode(String nodeId, String nodeLabel, String nodeType,
                                       double[] featureVector, double riskScore,
                                       double x, double y) {
        return new NodeVisualization(
            nodeId, nodeLabel, nodeType,
            featureVector, riskScore,
            x, y
        );
    }

    /**
     * Thêm node vào region phù hợp dựa trên risk score
     */
    public void addNodeToAppropriateRegion(NodeVisualization node) {
        RegionType regionType = RegionType.fromScore(node.getRiskScore());
        
        RegionVisualization targetRegion = regions.stream()
            .filter(r -> r.getRegionType() == regionType)
            .findFirst()
            .orElse(null);

        if (targetRegion != null) {
            targetRegion.addNode(node);
        }
    }

    /**
     * Thêm node vào region cụ thể
     */
    public void addNodeToRegion(NodeVisualization node, RegionType regionType) {
        RegionVisualization targetRegion = regions.stream()
            .filter(r -> r.getRegionType() == regionType)
            .findFirst()
            .orElse(null);

        if (targetRegion != null) {
            targetRegion.addNode(node);
        }
    }

    // ============== Tính toán KNN ==============

    /**
     * Tính toán KNN cho tất cả nodes trong tất cả regions
     */
    public void computeKNNForAllNodes() {
        List<NodeVisualization> allNodes = getAllNodes();

        for (RegionVisualization region : regions) {
            for (NodeVisualization node : region.getNodes()) {
                node.computeKNNNeighbors(allNodes, distanceMetric);
                
                // Tính khoảng cách đến center vector của region
                node.computeDistanceToCenterVector(
                    region.getCenterVector(),
                    distanceMetric
                );
            }
        }
    }

    /**
     * Lấy tất cả nodes từ tất cả regions
     */
    private List<NodeVisualization> getAllNodes() {
        List<NodeVisualization> allNodes = new ArrayList<>();
        for (RegionVisualization region : regions) {
            allNodes.addAll(region.getNodes());
        }
        return allNodes;
    }

    // ============== Đặt Distance Metric ==============

    public void setDistanceMetric(String metricName) {
        this.distanceMetric = DistanceMetric.getMetric(metricName);
    }

    public void setDistanceMetric(DistanceMetric metric) {
        this.distanceMetric = metric;
    }

    // ============== Hiển thị ==============

    /**
     * Tạo HTML page chứa SVG visualization
     */
    public String generateVisualizationHTML() {
        StringBuilder html = new StringBuilder();

        html.append("""
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <title>Domain Region Visualization - Miền Hành Vi</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        margin: 0;
                        padding: 20px;
                    }
                    
                    .container {
                        max-width: 1400px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 15px;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.3);
                        padding: 30px;
                    }
                    
                    h1 {
                        text-align: center;
                        color: #333;
                        margin-bottom: 10px;
                    }
                    
                    .subtitle {
                        text-align: center;
                        color: #666;
                        margin-bottom: 30px;
                        font-size: 14px;
                    }
                    
                    .visualization-grid {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 20px;
                        margin-bottom: 30px;
                    }
                    
                    .region-svg {
                        border: 2px solid #ddd;
                        border-radius: 8px;
                        background: #f9f9f9;
                    }
                    
                    .region-info {
                        background: #f5f5f5;
                        padding: 15px;
                        border-radius: 8px;
                        border-left: 4px solid #667eea;
                    }
                    
                    .full-width {
                        grid-column: 1 / -1;
                    }
                    
                    .legend {
                        display: grid;
                        grid-template-columns: repeat(3, 1fr);
                        gap: 15px;
                        margin: 30px 0;
                    }
                    
                    .legend-item {
                        padding: 15px;
                        border-radius: 8px;
                        text-align: center;
                    }
                    
                    .legend-safe {
                        background: #90EE90;
                        border: 2px solid #228B22;
                    }
                    
                    .legend-suspicious {
                        background: #FFFFE0;
                        border: 2px solid #FF8C00;
                    }
                    
                    .legend-fraud {
                        background: #FFB6C1;
                        border: 2px solid #DC143C;
                    }
                    
                    .legend-text {
                        font-weight: bold;
                        margin-bottom: 5px;
                    }
                    
                    .legend-desc {
                        font-size: 12px;
                        color: #555;
                    }
                    
                    pre {
                        background: #f4f4f4;
                        padding: 15px;
                        border-radius: 8px;
                        overflow-x: auto;
                        font-size: 12px;
                        line-height: 1.4;
                    }
                    
                    .metrics {
                        display: grid;
                        grid-template-columns: repeat(4, 1fr);
                        gap: 15px;
                        margin: 20px 0;
                    }
                    
                    .metric-card {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 20px;
                        border-radius: 8px;
                        text-align: center;
                    }
                    
                    .metric-value {
                        font-size: 28px;
                        font-weight: bold;
                        margin: 10px 0;
                    }
                    
                    .metric-label {
                        font-size: 12px;
                        opacity: 0.9;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🔍 Domain Region Visualization</h1>
                    <p class="subtitle">Trực quan hóa miền hành vi trong không gian dữ liệu gian lận</p>
                    
                    <div class="legend">
                        <div class="legend-item legend-safe">
                            <div class="legend-text">🟢 SAFE REGION</div>
                            <div class="legend-desc">Hành vi an toàn, ổn định</div>
                        </div>
                        <div class="legend-item legend-suspicious">
                            <div class="legend-text">🟠 SUSPICIOUS REGION</div>
                            <div class="legend-desc">Hành vi bất thường, cần theo dõi</div>
                        </div>
                        <div class="legend-item legend-fraud">
                            <div class="legend-text">🔴 FRAUD REGION</div>
                            <div class="legend-desc">Hành vi nguy hiểm, gian lận</div>
                        </div>
                    </div>
                    
                    <div class="visualization-grid">
            """);

        // Tạo SVG cho mỗi region
        int svgWidth = 600;
        int svgHeight = 500;

        for (RegionVisualization region : regions) {
            html.append("""
                <div>
                    <h3 style="text-align: center; color: %s;">%s REGION</h3>
                    <svg width="%d" height="%d" class="region-svg" xmlns="http://www.w3.org/2000/svg">
                        <defs>
                            <filter id="glow">
                                <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
                                <feMerge>
                                    <feMergeNode in="coloredBlur"/>
                                    <feMergeNode in="SourceGraphic"/>
                                </feMerge>
                            </filter>
                        </defs>
                """.formatted(
                    region.getColorScheme().getBorderColor(),
                    region.getRegionType().getLabel(),
                    svgWidth,
                    svgHeight
            ));

            // Vẽ region
            html.append(region.toSVG(svgWidth, svgHeight));

            html.append("</svg>\n");

            // Thông tin region
            html.append("<div class=\"region-info\">\n");
            html.append(region.getTextualDescription().replace("\n", "<br>"));
            html.append("</div>\n");
            html.append("</div>\n");
        }

        html.append("""
                    </div>
                    
                    <h2>📊 Thống kê chung</h2>
                    <div class="metrics">
            """);

        int totalNodes = getAllNodes().size();
        double avgRisk = regions.stream()
            .mapToDouble(RegionVisualization::getAverageRiskScore)
            .average()
            .orElse(0.0);

        html.append(String.format("""
                        <div class="metric-card">
                            <div class="metric-label">Tổng Nodes</div>
                            <div class="metric-value">%d</div>
                        </div>
                        <div class="metric-card">
                            <div class="metric-label">Distance Metric</div>
                            <div class="metric-value">%s</div>
                        </div>
                        <div class="metric-card">
                            <div class="metric-label">Avg Risk Score</div>
                            <div class="metric-value">%.2f%%</div>
                        </div>
                        <div class="metric-card">
                            <div class="metric-label">Regions</div>
                            <div class="metric-value">%d</div>
                        </div>
            """,
            totalNodes,
            distanceMetric.getName(),
            avgRisk * 100,
            regions.size()
        ));

        html.append("""
                    </div>
                    
                    <h2>🎯 Mô hình báo cáo chuẩn</h2>
            """);

        for (RegionVisualization region : regions) {
            html.append("<pre>");
            html.append(region.getStandardReportModel());
            html.append("</pre>\n");
        }

        html.append("""
                </div>
            </body>
            </html>
            """);

        return html.toString();
    }

    /**
     * Xuất báo cáo văn bản chi tiết
     */
    public String generateDetailedReport() {
        StringBuilder report = new StringBuilder();

        report.append("""
            ╔═══════════════════════════════════════════════════════════════╗
            ║                                                               ║
            ║   DOMAIN REGION VISUALIZATION - BÁOCHAO CHI TIẾT             ║
            ║   Trực quan hóa miền hành vi trong không gian dữ liệu        ║
            ║                                                               ║
            ╚═══════════════════════════════════════════════════════════════╝
            
            """);

        // Báo cáo từng region
        for (RegionVisualization region : regions) {
            report.append(region.getTextualDescription()).append("\n\n");

            // Báo cáo từng node
            for (NodeVisualization node : region.getNodes()) {
                report.append(node.getDetailedDescription()).append("\n");
            }
        }

        return report.toString();
    }

    // ============== Getters ==============

    public List<RegionVisualization> getRegions() {
        return new ArrayList<>(regions);
    }

    public RegionVisualization getRegion(RegionType type) {
        return regions.stream()
            .filter(r -> r.getRegionType() == type)
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
}
