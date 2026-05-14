package com.example.servingwebcontent.model;

import java.util.*;

/**
 * NODE - Đối tượng dữ liệu trong không gian miền
 * 
 * Đại diện cho:
 * - Thực thể dữ liệu (User, IP, Domain, etc.)
 * - Điểm trong không gian hành vi
 * - Có vector đặc trưng và mức độ nguy hiểm
 * 
 * Hiển thị:
 * - Hình tròn nhỏ
 * - Màu phụ thuộc vào region
 * - Kích thước phụ thuộc vào mức độ nguy hiểm
 */
public class NodeVisualization {

    private String nodeId;
    private String nodeLabel;
    
    // Vector đặc trưng của node
    private double[] featureVector;
    
    // Mức độ nguy hiểm (0.0 - 1.0)
    private double riskScore;
    
    // Loại node
    private String nodeType; // User, IP, Domain, Email, etc.
    
    // Vùng hành vi mà node thuộc về
    private RegionType regionType;
    private RegionVisualization region;
    
    // Tọa độ hiển thị (2D)
    private double x;
    private double y;
    
    // KNN Neighbors
    private List<NodeVisualization> knnNeighbors = new ArrayList<>();
    private int k = 3;
    
    // Khoảng cách đến center vector của miền
    private double distanceToCenterVector;
    
    // Trọng số của node
    private double weight;
    
    // Trạng thái: NORMAL, WARNING, ANALYZING, DANGEROUS
    private NodeStatus status;

    // ============== ENUM: Trạng thái Node ==============
    public enum NodeStatus {
        NORMAL("Normal", "Node bình thường"),
        WARNING("Warning", "Node cần cảnh báo"),
        ANALYZING("Analyzing", "Node đang phân tích"),
        DANGEROUS("Dangerous", "Node nguy hiểm");

        private final String label;
        private final String description;

        NodeStatus(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() { return label; }
        public String getDescription() { return description; }
    }

    // ============== Constructor ==============

    public NodeVisualization(String nodeId, String nodeLabel, String nodeType,
                            double[] featureVector, double riskScore,
                            double x, double y) {
        this.nodeId = nodeId;
        this.nodeLabel = nodeLabel;
        this.nodeType = nodeType;
        this.featureVector = featureVector;
        this.riskScore = Math.max(0.0, Math.min(1.0, riskScore));
        this.x = x;
        this.y = y;
        this.weight = 1.0;
        this.status = determineStatus(riskScore);
    }

    // ============== Xác định Trạng thái ==============

    private NodeStatus determineStatus(double risk) {
        if (risk < 0.33) return NodeStatus.NORMAL;
        if (risk < 0.67) return NodeStatus.WARNING;
        return NodeStatus.DANGEROUS;
    }

    // ============== Phương thức KNN ==============

    /**
     * Tính toán KNN neighbors dựa trên distance
     * @param candidateNodes Danh sách các node ứng viên
     * @param distanceMetric Thuật toán khoảng cách
     */
    public void computeKNNNeighbors(List<NodeVisualization> candidateNodes, 
                                   DistanceMetric distanceMetric) {
        if (candidateNodes == null || candidateNodes.isEmpty()) {
            return;
        }

        // Tính khoảng cách đến tất cả các node
        List<NodeDistance> distances = new ArrayList<>();
        for (NodeVisualization candidate : candidateNodes) {
            if (!candidate.equals(this)) {
                double distance = distanceMetric.calculate(
                    this.featureVector,
                    candidate.featureVector
                );
                distances.add(new NodeDistance(candidate, distance));
            }
        }

        // Sắp xếp theo khoảng cách
        distances.sort(Comparator.comparingDouble(d -> d.distance));

        // Lấy K neighbors gần nhất
        this.knnNeighbors.clear();
        int limit = Math.min(k, distances.size());
        for (int i = 0; i < limit; i++) {
            this.knnNeighbors.add(distances.get(i).node);
        }
    }

    /**
     * Lớp hỗ trợ cho KNN
     */
    private static class NodeDistance {
        NodeVisualization node;
        double distance;

        NodeDistance(NodeVisualization node, double distance) {
            this.node = node;
            this.distance = distance;
        }
    }

    // ============== Phương thức Khoảng cách ==============

    /**
     * Tính khoảng cách đến center vector
     * @param centerVector Vector tâm
     * @param distanceMetric Thuật toán khoảng cách
     */
    public void computeDistanceToCenterVector(double[] centerVector,
                                             DistanceMetric distanceMetric) {
        this.distanceToCenterVector = distanceMetric.calculate(
            this.featureVector,
            centerVector
        );
    }

    // ============== Hiển thị ==============

    /**
     * Lấy SVG node
     */
    public String toSVG(String nodeColor) {
        // Kích thước node phụ thuộc vào mức độ nguy hiểm
        double radius = 5 + (riskScore * 10);
        
        String strokeStyle = "";
        if (status == NodeStatus.ANALYZING) {
            strokeStyle = String.format(" stroke=\"white\" stroke-width=\"2\"");
        } else if (status == NodeStatus.DANGEROUS) {
            strokeStyle = " filter=\"url(#glow)\"";
        }

        return String.format(
            """
            <circle cx="%f" cy="%f" r="%f" 
                    fill="%s"%s opacity="0.8"/>
            <title>%s (%s): Risk=%.2f%%</title>
            """,
            x, y, radius,
            nodeColor,
            strokeStyle,
            nodeLabel, nodeType,
            riskScore * 100
        );
    }

    /**
     * Mô tả văn bản chi tiết
     */
    public String getDetailedDescription() {
        StringBuilder desc = new StringBuilder();
        
        desc.append(String.format("""
            ╔════════════════════════════════════════╗
            ║  NODE: %s
            ║  Type: %s
            ║  
            ║  Risk Score: %.2f%%
            ║  Status: %s (%s)
            ║  Region: %s
            ║  Weight: %.2f
            ║  
            ║  Position: (%.2f, %.2f)
            ║  Distance to Center: %.2f
            ║  KNN Neighbors: %d
            ║  
            ║  Feature Vector (dim=%d):
            """,
            nodeLabel, nodeType,
            riskScore * 100,
            status.getLabel(), status.getDescription(),
            regionType != null ? regionType.getLabel() : "UNKNOWN",
            weight,
            x, y,
            distanceToCenterVector,
            knnNeighbors.size(),
            featureVector != null ? featureVector.length : 0
        ));

        if (featureVector != null) {
            for (int i = 0; i < Math.min(featureVector.length, 5); i++) {
                desc.append(String.format("║      [%d] = %.4f%n", i, featureVector[i]));
            }
            if (featureVector.length > 5) {
                desc.append(String.format("║      ... (%d more)%n", featureVector.length - 5));
            }
        }

        desc.append(String.format("╚════════════════════════════════════════╝%n"));

        return desc.toString();
    }

    // ============== Getters / Setters ==============

    public String getNodeId() { return nodeId; }
    public String getNodeLabel() { return nodeLabel; }
    public double[] getFeatureVector() { return featureVector; }
    public double getRiskScore() { return riskScore; }
    public String getNodeType() { return nodeType; }
    public RegionType getRegionType() { return regionType; }
    public RegionVisualization getRegion() { return region; }
    public double getX() { return x; }
    public double getY() { return y; }
    public List<NodeVisualization> getKnnNeighbors() { return new ArrayList<>(knnNeighbors); }
    public double getDistanceToCenterVector() { return distanceToCenterVector; }
    public double getWeight() { return weight; }
    public NodeStatus getStatus() { return status; }

    public void setRegion(RegionVisualization region) {
        this.region = region;
        this.regionType = region.getRegionType();
    }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setStatus(NodeStatus status) { this.status = status; }
    public void setK(int k) { this.k = k; }
}
