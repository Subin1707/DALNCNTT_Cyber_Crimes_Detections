package com.example.servingwebcontent.model;

import java.util.*;

/**
 * REGION VISUALIZATION
 * Miền hành vi trong hệ thống phát hiện gian lận
 */
public class RegionVisualization {

    // =========================================================
    // ENUM COLOR SCHEME
    // =========================================================

    public enum RegionColorScheme {

        SAFE(
                "#22c55e",
                "rgba(34,197,94,0.12)",
                "#16a34a",
                "Miền an toàn"
        ),

        SUSPICIOUS(
                "#f59e0b",
                "rgba(245,158,11,0.15)",
                "#d97706",
                "Miền nghi ngờ"
        ),

        FRAUD(
                "#ef4444",
                "rgba(239,68,68,0.15)",
                "#dc2626",
                "Miền gian lận"
        );

        private final String nodeColor;
        private final String backgroundColor;
        private final String borderColor;
        private final String meaning;

        RegionColorScheme(
                String nodeColor,
                String backgroundColor,
                String borderColor,
                String meaning
        ) {
            this.nodeColor = nodeColor;
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            this.meaning = meaning;
        }

        public String getNodeColor() {
            return nodeColor;
        }

        public String getBackgroundColor() {
            return backgroundColor;
        }

        public String getBorderColor() {
            return borderColor;
        }

        public String getMeaning() {
            return meaning;
        }

        public static RegionColorScheme fromRegionType(RegionType type) {

            return switch (type) {
                case SAFE -> SAFE;
                case SUSPICIOUS -> SUSPICIOUS;
                case FRAUD -> FRAUD;
            };
        }
    }

    // =========================================================
    // THUỘC TÍNH
    // =========================================================

    private String regionId;

    private RegionType regionType;

    private RegionColorScheme colorScheme;

    // vector trung tâm đặc trưng
    private double[] centerVector;

    // danh sách node
    private List<NodeVisualization> nodes = new ArrayList<>();

    // bán kính miền
    private double radius;

    // tọa độ tâm
    private double centerX;
    private double centerY;

    // thống kê
    private int nodeCount;
    private double averageRiskScore;
    private double dominanceProbability;

    private Random random = new Random();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public RegionVisualization(
            RegionType regionType,
            double[] centerVector,
            double radius,
            double centerX,
            double centerY
    ) {

        this.regionId = UUID.randomUUID().toString();

        this.regionType = regionType;

        this.colorScheme =
                RegionColorScheme.fromRegionType(regionType);

        this.centerVector = centerVector;

        this.radius = radius;

        this.centerX = centerX;
        this.centerY = centerY;

        this.nodeCount = 0;
        this.averageRiskScore = 0;
        this.dominanceProbability = 0;
    }

    // =========================================================
    // THÊM NODE
    // =========================================================

    public void addNode(NodeVisualization node) {

        if (node == null) {
            return;
        }

        node.setRegion(this);

        autoMoveNodeInsideRegion(node);

        nodes.add(node);

        updateMetrics();
    }

    // =========================================================
    // KÉO NODE VÀO TRONG MIỀN
    // =========================================================

    private void autoMoveNodeInsideRegion(NodeVisualization node) {

        double angle = random.nextDouble() * 2 * Math.PI;

        double distance =
                random.nextDouble() * (radius * 0.72);

        double x =
                centerX + Math.cos(angle) * distance;

        double y =
                centerY + Math.sin(angle) * distance;

        node.setX(x);
        node.setY(y);
    }

    // =========================================================
    // KIỂM TRA NODE CÓ TRONG MIỀN KHÔNG
    // =========================================================

    public boolean isInsideRegion(NodeVisualization node) {

        double dx = node.getX() - centerX;
        double dy = node.getY() - centerY;

        double distance =
                Math.sqrt(dx * dx + dy * dy);

        return distance <= radius;
    }

    // =========================================================
    // FORCE NODE VỀ ĐÚNG MIỀN
    // =========================================================

    public void validateAllNodesPosition() {

        for (NodeVisualization node : nodes) {

            if (!isInsideRegion(node)) {

                autoMoveNodeInsideRegion(node);
            }
        }
    }

    // =========================================================
    // UPDATE METRICS
    // =========================================================

    private void updateMetrics() {

        nodeCount = nodes.size();

        if (nodes.isEmpty()) {

            averageRiskScore = 0;
            dominanceProbability = 0;

            return;
        }

        double totalRisk = 0;

        int correctRegionCount = 0;

        for (NodeVisualization node : nodes) {

            totalRisk += node.getRiskScore();

            if (node.getRegionType() == regionType) {
                correctRegionCount++;
            }
        }

        averageRiskScore =
                totalRisk / nodes.size();

        dominanceProbability =
                (double) correctRegionCount / nodes.size();
    }

    // =========================================================
    // SVG
    // =========================================================

    public String toSVG(int svgWidth, int svgHeight) {

        validateAllNodesPosition();

        StringBuilder svg = new StringBuilder();

        // ========================
        // VẼ MIỀN
        // ========================

        svg.append(String.format("""
            <circle
                cx="%f"
                cy="%f"
                r="%f"
                fill="%s"
                stroke="%s"
                stroke-width="4"
                opacity="0.92"
            />
            """,
                centerX,
                centerY,
                radius,
                colorScheme.getBackgroundColor(),
                colorScheme.getBorderColor()
        ));

        // ========================
        // GLOW EFFECT
        // ========================

        svg.append(String.format("""
            <circle
                cx="%f"
                cy="%f"
                r="%f"
                fill="none"
                stroke="%s"
                stroke-width="10"
                opacity="0.12"
            />
            """,
                centerX,
                centerY,
                radius + 8,
                colorScheme.getBorderColor()
        ));

        // ========================
        // LABEL
        // ========================

        svg.append(String.format("""
            <text
                x="%f"
                y="%f"
                text-anchor="middle"
                font-size="22"
                font-weight="bold"
                fill="%s">
                %s
            </text>
            """,
                centerX,
                centerY - radius - 20,
                colorScheme.getBorderColor(),
                regionType.getLabel()
        ));

        // ========================
        // CENTER STAR
        // ========================

        svg.append(String.format("""
            <text
                x="%f"
                y="%f"
                text-anchor="middle"
                font-size="26"
                fill="%s">
                ★
            </text>
            """,
                centerX,
                centerY + 8,
                colorScheme.getBorderColor()
        ));

        // ========================
        // KNN CONNECTIONS
        // ========================

        svg.append(drawKNNConnections());

        // ========================
        // VẼ NODE
        // ========================

        for (NodeVisualization node : nodes) {

            svg.append(
                    node.toSVG(
                            colorScheme.getNodeColor()
                    )
            );
        }

        return svg.toString();
    }

    // =========================================================
    // VẼ KNN
    // =========================================================

    private String drawKNNConnections() {

        StringBuilder line = new StringBuilder();

        for (NodeVisualization node : nodes) {

            for (NodeVisualization neighbor :
                    node.getKnnNeighbors()) {

                if (nodes.contains(neighbor)) {

                    line.append(String.format("""
                        <line
                            x1="%f"
                            y1="%f"
                            x2="%f"
                            y2="%f"
                            stroke="%s"
                            stroke-width="1.2"
                            opacity="0.25"
                        />
                        """,
                            node.getX(),
                            node.getY(),
                            neighbor.getX(),
                            neighbor.getY(),
                            colorScheme.getBorderColor()
                    ));
                }
            }
        }

        return line.toString();
    }

    // =========================================================
    // TEXT DESCRIPTION
    // =========================================================

    public String getTextualDescription() {

        return String.format("""
            ════════════════════════════════════════
            REGION: %s

            Ý nghĩa:
            %s

            Tổng node:
            %d

            Risk trung bình:
            %.2f%%

            Xác suất chủ đạo:
            %.2f%%

            Tâm miền:
            (%.2f , %.2f)

            Bán kính:
            %.2f

            Miền chứa:
            - vector đặc trưng
            - node tương đồng
            - node cùng hành vi
            - KNN lân cận
            - phân tích thống kê
            ════════════════════════════════════════
            """,

                regionType.getLabel(),

                colorScheme.getMeaning(),

                nodeCount,

                averageRiskScore * 100,

                dominanceProbability * 100,

                centerX,
                centerY,

                radius
        );
    }

    // =========================================================
    // REPORT
    // =========================================================

    public String getStandardReportModel() {

        return String.format("""
            [DOMAIN REGION REPORT]

            Region:
            %s

            Meaning:
            %s

            Total Nodes:
            %d

            Average Risk:
            %.2f%%

            Dominance:
            %.2f%%

            Center:
            X = %.2f
            Y = %.2f

            Radius:
            %.2f
            """,

                regionType.getLabel(),

                colorScheme.getMeaning(),

                nodeCount,

                averageRiskScore * 100,

                dominanceProbability * 100,

                centerX,
                centerY,

                radius
        );
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public String getRegionId() {
        return regionId;
    }

    public RegionType getRegionType() {
        return regionType;
    }

    public RegionColorScheme getColorScheme() {
        return colorScheme;
    }

    public double[] getCenterVector() {
        return centerVector;
    }

    public List<NodeVisualization> getNodes() {
        return nodes;
    }

    public double getRadius() {
        return radius;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public double getAverageRiskScore() {
        return averageRiskScore;
    }

    public double getDominanceProbability() {
        return dominanceProbability;
    }

    // =========================================================
    // SETTERS
    // =========================================================

    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}