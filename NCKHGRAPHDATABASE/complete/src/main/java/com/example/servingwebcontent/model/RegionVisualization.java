package com.example.servingwebcontent.model;

import java.util.*;

/**
 * MIỀN - Mô hình hành vi trong không gian dữ liệu
 * 
 * Đại diện cho:
 * - Vùng hành vi (behavioral zone)
 * - Khoảng không gian chứa các node có đặc điểm tương tự
 * 
 * Hiển thị:
 * - Hình tròn lớn = miền
 * - Màu riêng cho từng miền
 * - Chứa node và center vector
 */
public class RegionVisualization {

    // ============== ENUM: Màu của Miền ==============
    public enum RegionColorScheme {
        // SAFE REGION: Xanh lá nhạt
        SAFE(
            "SAFE",
            "#90EE90",      // Background: xanh lá nhạt
            "#228B22",      // Border: xanh lá đậm
            "#1E90FF",      // Node: xanh dương
            "An toàn, ổn định, tin cậy"
        ),
        
        // SUSPICIOUS REGION: Vàng nhạt
        SUSPICIOUS(
            "SUSPICIOUS",
            "#FFFFE0",      // Background: vàng nhạt
            "#FF8C00",      // Border: cam
            "#FF8C00",      // Node: cam
            "Bất thường, cần theo dõi"
        ),
        
        // FRAUD REGION: Đỏ nhạt
        FRAUD(
            "FRAUD",
            "#FFB6C1",      // Background: đỏ nhạt
            "#DC143C",      // Border: đỏ đậm
            "#8B0000",      // Node: đỏ đậm
            "Nguy hiểm, gian lận, vi phạm"
        );

        private final String regionType;
        private final String backgroundColor;
        private final String borderColor;
        private final String nodeColor;
        private final String meaning;

        RegionColorScheme(String regionType, String backgroundColor, 
                         String borderColor, String nodeColor, String meaning) {
            this.regionType = regionType;
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            this.nodeColor = nodeColor;
            this.meaning = meaning;
        }

        public String getBackgroundColor() { return backgroundColor; }
        public String getBorderColor() { return borderColor; }
        public String getNodeColor() { return nodeColor; }
        public String getMeaning() { return meaning; }
        public String getRegionType() { return regionType; }

        public static RegionColorScheme fromRegionType(RegionType type) {
            return switch (type) {
                case SAFE -> SAFE;
                case SUSPICIOUS -> SUSPICIOUS;
                case FRAUD -> FRAUD;
            };
        }
    }

    // ============== Thuộc tính của Miền ==============

    private String regionId;
    private RegionType regionType;
    private RegionColorScheme colorScheme;
    
    // Center Vector: đặc trưng trung tâm miền
    private double[] centerVector;
    
    // Các node trong miền
    private List<NodeVisualization> nodes = new ArrayList<>();
    
    // Kích thước miền (bán kính)
    private double radius;
    
    // Tọa độ tâm (trong không gian 2D để hiển thị)
    private double centerX;
    private double centerY;
    
    // Số lượng node, xác suất, mức độ nguy hiểm trung bình
    private int nodeCount;
    private double averageRiskScore;
    private double dominanceProbability;

    // ============== Constructor ==============

    public RegionVisualization(RegionType regionType, double[] centerVector, 
                              double radius, double centerX, double centerY) {
        this.regionId = UUID.randomUUID().toString();
        this.regionType = regionType;
        this.colorScheme = RegionColorScheme.fromRegionType(regionType);
        this.centerVector = centerVector;
        this.radius = radius;
        this.centerX = centerX;
        this.centerY = centerY;
        this.nodeCount = 0;
        this.averageRiskScore = 0.0;
        this.dominanceProbability = 0.0;
    }

    // ============== Phương thức thêm Node ==============

    /**
     * Thêm node vào miền
     * @param node Node cần thêm
     */
    public void addNode(NodeVisualization node) {
        if (node != null) {
            nodes.add(node);
            node.setRegion(this);
            updateMetrics();
        }
    }

    /**
     * Xóa node khỏi miền
     */
    public void removeNode(NodeVisualization node) {
        if (node != null) {
            nodes.remove(node);
            updateMetrics();
        }
    }

    // ============== Cập nhật Metrics ==============

    private void updateMetrics() {
        this.nodeCount = nodes.size();
        
        if (nodes.isEmpty()) {
            this.averageRiskScore = 0.0;
            this.dominanceProbability = 0.0;
            return;
        }

        // Tính trung bình mức độ nguy hiểm
        double totalRisk = nodes.stream()
            .mapToDouble(NodeVisualization::getRiskScore)
            .sum();
        this.averageRiskScore = totalRisk / nodes.size();

        // Tính xác suất chủ đạo
        long nodesInRegion = nodes.stream()
            .filter(n -> n.getRegionType() == this.regionType)
            .count();
        this.dominanceProbability = (double) nodesInRegion / nodes.size();
    }

    // ============== Phương thức Hiển thị ==============

    /**
     * Lấy HTML/SVG để hiển thị miền
     */
    public String toSVG(int svgWidth, int svgHeight) {
        StringBuilder svg = new StringBuilder();
        
        // Vẽ hình tròn miền
        svg.append(String.format(
            """
            <circle cx="%f" cy="%f" r="%f" 
                    fill="%s" stroke="%s" stroke-width="2" opacity="0.7"/>
            """,
            centerX, centerY, radius,
            colorScheme.getBackgroundColor(),
            colorScheme.getBorderColor()
        ));

        // Vẽ center vector (dấu ★)
        svg.append(String.format(
            """
            <text x="%f" y="%f" font-size="24" text-anchor="middle" 
                  fill="%s" font-weight="bold">★</text>
            """,
            centerX, centerY,
            colorScheme.getBorderColor()
        ));

        // Vẽ các node
        for (NodeVisualization node : nodes) {
            svg.append(node.toSVG(colorScheme.getNodeColor()));
        }

        // Vẽ đường KNN (nối các node gần nhau)
        svg.append(drawKNNConnections());

        return svg.toString();
    }

    /**
     * Vẽ đường kết nối KNN
     */
    private String drawKNNConnections() {
        StringBuilder connections = new StringBuilder();
        
        for (NodeVisualization node : nodes) {
            for (NodeVisualization neighbor : node.getKnnNeighbors()) {
                if (nodes.contains(neighbor)) {
                    connections.append(String.format(
                        """
                        <line x1="%f" y1="%f" x2="%f" y2="%f" 
                              stroke="%s" stroke-width="1" opacity="0.5"/>
                        """,
                        node.getX(), node.getY(),
                        neighbor.getX(), neighbor.getY(),
                        colorScheme.getBorderColor()
                    ));
                }
            }
        }
        
        return connections.toString();
    }

    /**
     * Mô tả văn bản của miền (dễ đọc)
     */
    public String getTextualDescription() {
        return String.format("""
            ╔════════════════════════════════════════╗
            ║  REGION: %s
            ║  Color Scheme: %s
            ║  Meaning: %s
            ║  
            ║  Nodes: %d
            ║  Average Risk: %.2f%%
            ║  Dominance: %.2f%%
            ║  
            ║  Center: (%.2f, %.2f)
            ║  Radius: %.2f
            ╚════════════════════════════════════════╝
            """,
            regionType.getLabel(),
            String.format("Background: %s, Border: %s, Node: %s",
                colorScheme.getBackgroundColor(),
                colorScheme.getBorderColor(),
                colorScheme.getNodeColor()),
            colorScheme.getMeaning(),
            nodeCount,
            averageRiskScore * 100,
            dominanceProbability * 100,
            centerX, centerY,
            radius
        );
    }

    /**
     * Mô hình chuẩn cho báo cáo
     */
    public String getStandardReportModel() {
        StringBuilder report = new StringBuilder();
        
        report.append(String.format("╔══════════════════════════════════════╗%n"));
        report.append(String.format("║     %s REGION%n", regionType.getLabel()));
        report.append(String.format("║     (Miền hành vi %s)%n", colorScheme.getMeaning()));
        report.append(String.format("╚══════════════════════════════════════╝%n"));
        report.append(String.format("%n"));

        // Vẽ miền với các node
        int gridSize = 7;
        for (int row = 0; row < gridSize; row++) {
            report.append("        ");
            for (int col = 0; col < gridSize; col++) {
                if (row == gridSize / 2 && col == gridSize / 2) {
                    // Center vector
                    report.append("★ ");
                } else if (Math.random() < 0.4) {
                    // Node ngẫu nhiên
                    report.append("● ");
                } else {
                    report.append("  ");
                }
            }
            report.append(String.format("%n"));
        }

        report.append(String.format("%nNode Count: %d%n", nodeCount));
        report.append(String.format("Average Risk: %.2f%%%n", averageRiskScore * 100));
        report.append(String.format("Dominance: %.2f%%%n", dominanceProbability * 100));

        return report.toString();
    }

    // ============== Getters / Setters ==============

    public String getRegionId() { return regionId; }
    public RegionType getRegionType() { return regionType; }
    public RegionColorScheme getColorScheme() { return colorScheme; }
    public double[] getCenterVector() { return centerVector; }
    public List<NodeVisualization> getNodes() { return new ArrayList<>(nodes); }
    public double getRadius() { return radius; }
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }
    public int getNodeCount() { return nodeCount; }
    public double getAverageRiskScore() { return averageRiskScore; }
    public double getDominanceProbability() { return dominanceProbability; }

    public void setCenterX(double centerX) { this.centerX = centerX; }
    public void setCenterY(double centerY) { this.centerY = centerY; }
    public void setRadius(double radius) { this.radius = radius; }
}
