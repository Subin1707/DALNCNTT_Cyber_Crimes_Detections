package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import com.example.servingwebcontent.model.RegionType;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Visualization Service for Multi-Region Graph
 * 
 * Tạo dữ liệu visualization cho đồ thị liên kết với các miền phân biệt
 * - Miền SAFE (xanh) ở bên trái
 * - Miền FRAUD (đỏ) ở bên phải
 * - Miền SUSPICIOUS (giao nhau) ở giữa
 */
@Service
public class VisualizationService {
    
    /**
     * Node data for visualization
     */
    public static class VisualizationNode {
        public String id;
        public String label;
        public double x;
        public double y;
        public String region;      // SAFE, SUSPICIOUS, FRAUD
        public double riskScore;
        public String color;       // Blue, Red, Orange
        public int size;
        
        public VisualizationNode(String id, String label, double x, double y, 
                               String region, double riskScore, String color) {
            this.id = id;
            this.label = label;
            this.x = x;
            this.y = y;
            this.region = region;
            this.riskScore = riskScore;
            this.color = color;
            this.size = 15 + (int)(riskScore * 20);
        }
    }
    
    /**
     * Edge (relationship) data for visualization
     */
    public static class VisualizationEdge {
        public String source;
        public String target;
        public String type;       // "connection", "related", etc.
        
        public VisualizationEdge(String source, String target, String type) {
            this.source = source;
            this.target = target;
            this.type = type;
        }
    }
    
    /**
     * Region circle for visualization
     */
    public static class RegionCircle {
        public String name;        // SAFE, SUSPICIOUS, FRAUD
        public double centerX;
        public double centerY;
        public double radius;
        public String color;
        public double opacity;
        
        public RegionCircle(String name, double centerX, double centerY, 
                          double radius, String color, double opacity) {
            this.name = name;
            this.centerX = centerX;
            this.centerY = centerY;
            this.radius = radius;
            this.color = color;
            this.opacity = opacity;
        }
    }
    
    /**
     * Complete visualization data
     */
    public static class VisualizationData {
        public List<VisualizationNode> nodes;
        public List<VisualizationEdge> edges;
        public List<RegionCircle> regions;
        public double width;
        public double height;
        
        public VisualizationData() {
            this.nodes = new ArrayList<>();
            this.edges = new ArrayList<>();
            this.regions = new ArrayList<>();
            this.width = 1200;
            this.height = 800;
        }
    }
    
    /**
     * Generate visualization data for multi-region analysis
     * 
     * Layout:
     * - Tâm bên trái (x=250): SAFE region (xanh) - radius=150
     * - Tâm giữa (x=600): SUSPICIOUS region (cam) - radius=100
     * - Tâm bên phải (x=950): FRAUD region (đỏ) - radius=150
     */
    public VisualizationData generateVisualizationData(
            List<String> userIds,
            Map<String, BehaviorFeatureVector> behaviorMap,
            Map<String, RegionType> classificationMap,
            Map<String, Double> riskScoreMap) {
        
        VisualizationData data = new VisualizationData();
        
        // Define region circles
        data.regions.add(new RegionCircle("SAFE", 250, 400, 150, "#3498db", 0.15));      // Blue
        data.regions.add(new RegionCircle("SUSPICIOUS", 600, 400, 120, "#f39c12", 0.15)); // Orange
        data.regions.add(new RegionCircle("FRAUD", 950, 400, 150, "#e74c3c", 0.15));     // Red
        
        // Generate nodes and position them based on risk score
        Random random = new Random(42); // Same seed for consistent layout
        for (String userId : userIds) {
            RegionType region = classificationMap.getOrDefault(userId, RegionType.SAFE);
            double riskScore = riskScoreMap.getOrDefault(userId, 0.0);
            
            // Position node based on region
            double x, y;
            String color;
            
            switch (region) {
                case SAFE:
                    // Position in SAFE region (blue, left)
                    x = 250 + (random.nextDouble() - 0.5) * 200;
                    y = 400 + (random.nextDouble() - 0.5) * 200;
                    color = "#3498db"; // Blue
                    break;
                case FRAUD:
                    // Position in FRAUD region (red, right)
                    x = 950 + (random.nextDouble() - 0.5) * 200;
                    y = 400 + (random.nextDouble() - 0.5) * 200;
                    color = "#e74c3c"; // Red
                    break;
                case SUSPICIOUS:
                default:
                    // Position in SUSPICIOUS region (orange, middle)
                    x = 600 + (random.nextDouble() - 0.5) * 150;
                    y = 400 + (random.nextDouble() - 0.5) * 150;
                    color = "#f39c12"; // Orange
                    break;
            }
            
            VisualizationNode node = new VisualizationNode(
                userId,
                userId,
                x, y,
                region.name(),
                riskScore,
                color
            );
            data.nodes.add(node);
        }
        
        // Generate edges (connections) between related users
        // Giả sử: users trong cùng region có liên kết với nhau
        for (int i = 0; i < data.nodes.size(); i++) {
            for (int j = i + 1; j < data.nodes.size(); j++) {
                VisualizationNode node1 = data.nodes.get(i);
                VisualizationNode node2 = data.nodes.get(j);
                
                // Connect if both in same region (30% probability)
                if (node1.region.equals(node2.region) && random.nextDouble() < 0.3) {
                    data.edges.add(new VisualizationEdge(node1.id, node2.id, "same_region"));
                }
                // Connect if both SUSPICIOUS (40% probability - more connections in boundary)
                else if (node1.region.equals("SUSPICIOUS") && node2.region.equals("SUSPICIOUS") 
                        && random.nextDouble() < 0.4) {
                    data.edges.add(new VisualizationEdge(node1.id, node2.id, "suspicious"));
                }
            }
        }
        
        return data;
    }
    
    /**
     * Calculate node position based on region and risk score
     * 
     * Formula:
     * - Risk score 0.0 → closer to region center
     * - Risk score 1.0 → closer to region edge
     */
    public double[] calculateNodePosition(RegionType region, double riskScore) {
        double x, y;
        double distanceFromCenter = riskScore * 100;
        double angle = Math.random() * 2 * Math.PI;
        
        double baseX = 0, baseY = 400;
        switch (region) {
            case SAFE:
                baseX = 250;
                break;
            case FRAUD:
                baseX = 950;
                break;
            case SUSPICIOUS:
            default:
                baseX = 600;
                break;
        }
        
        x = baseX + Math.cos(angle) * distanceFromCenter;
        y = baseY + Math.sin(angle) * distanceFromCenter;
        
        return new double[]{x, y};
    }
    
    /**
     * Get color based on risk score
     */
    public String getColorByRiskScore(double riskScore) {
        if (riskScore <= 0.25) {
            return "#3498db"; // Blue - SAFE
        } else if (riskScore <= 0.65) {
            return "#f39c12"; // Orange - SUSPICIOUS
        } else {
            return "#e74c3c"; // Red - FRAUD
        }
    }
    
    /**
     * Get region based on risk score
     */
    public RegionType getRegionByRiskScore(double riskScore) {
        if (riskScore <= 0.25) {
            return RegionType.SAFE;
        } else if (riskScore <= 0.65) {
            return RegionType.SUSPICIOUS;
        } else {
            return RegionType.FRAUD;
        }
    }
}
