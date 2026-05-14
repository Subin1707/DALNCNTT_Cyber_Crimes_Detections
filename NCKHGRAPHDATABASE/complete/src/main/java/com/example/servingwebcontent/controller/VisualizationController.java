package com.example.servingwebcontent.controller;

import com.example.servingwebcontent.service.VisualizationService;
import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import com.example.servingwebcontent.model.RegionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Visualization Controller
 * 
 * Cung cấp API endpoints cho visualization đồ thị liên kết với các miền
 * - GET /api/visualization/graph - Lấy dữ liệu đồ thị
 * - POST /api/visualization/update - Cập nhật dữ liệu visualization
 */
@RestController
@RequestMapping("/api/visualization")
@CrossOrigin(origins = "*")
public class VisualizationController {
    
    @Autowired
    private VisualizationService visualizationService;
    
    /**
     * Get visualization data for graph
     * 
     * Request: POST /api/visualization/graph
     * Body: {
     *   "userIds": ["user1", "user2", ...],
     *   "behaviors": {
     *     "user1": {...},
     *     "user2": {...}
     *   },
     *   "classifications": {
     *     "user1": "SAFE",
     *     "user2": "FRAUD"
     *   },
     *   "riskScores": {
     *     "user1": 0.15,
     *     "user2": 0.85
     *   }
     * }
     */
    @PostMapping("/graph")
    public VisualizationService.VisualizationData getGraphVisualization(
            @RequestBody Map<String, Object> request) {
        
        @SuppressWarnings("unchecked")
        List<String> userIds = (List<String>) request.get("userIds");
        
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> behaviors = 
            (Map<String, Map<String, Object>>) request.get("behaviors");
        
        @SuppressWarnings("unchecked")
        Map<String, String> classifications = 
            (Map<String, String>) request.get("classifications");
        
        @SuppressWarnings("unchecked")
        Map<String, Double> riskScores = 
            (Map<String, Double>) request.get("riskScores");
        
        // Convert classifications to RegionType
        Map<String, RegionType> classificationMap = new HashMap<>();
        classifications.forEach((userId, region) -> {
            classificationMap.put(userId, RegionType.valueOf(region.toUpperCase()));
        });
        
        // Convert behaviors to BehaviorFeatureVector (optional for now)
        Map<String, BehaviorFeatureVector> behaviorMap = new HashMap<>();
        
        return visualizationService.generateVisualizationData(
            userIds, behaviorMap, classificationMap, riskScores
        );
    }
    
    /**
     * Get example test data
     */
    @GetMapping("/example")
    public VisualizationService.VisualizationData getExampleData() {
        List<String> userIds = Arrays.asList(
            "user1", "user2", "user3", "user4", "user5",
            "user6", "user7", "user8", "user9", "user10",
            "user11", "user12", "user13", "user14", "user15"
        );
        
        Map<String, RegionType> classificationMap = new HashMap<>();
        Map<String, Double> riskScoreMap = new HashMap<>();
        
        // SAFE users (left)
        for (int i = 1; i <= 5; i++) {
            classificationMap.put("user" + i, RegionType.SAFE);
            riskScoreMap.put("user" + i, Math.random() * 0.25);
        }
        
        // SUSPICIOUS users (middle)
        for (int i = 6; i <= 10; i++) {
            classificationMap.put("user" + i, RegionType.SUSPICIOUS);
            riskScoreMap.put("user" + i, 0.25 + Math.random() * 0.4);
        }
        
        // FRAUD users (right)
        for (int i = 11; i <= 15; i++) {
            classificationMap.put("user" + i, RegionType.FRAUD);
            riskScoreMap.put("user" + i, 0.65 + Math.random() * 0.35);
        }
        
        return visualizationService.generateVisualizationData(
            userIds, new HashMap<>(), classificationMap, riskScoreMap
        );
    }
    
    /**
     * Get region definition
     */
    @GetMapping("/regions")
    public Map<String, Object> getRegions() {
        Map<String, Object> response = new HashMap<>();
        
        List<Map<String, Object>> regions = Arrays.asList(
            createRegionInfo("SAFE", "#3498db", 250, 400, 150, 
                "Hành động hợp lệ, ổn định"),
            createRegionInfo("SUSPICIOUS", "#f39c12", 600, 400, 120, 
                "Hành động bất thường nhưng chưa xác định"),
            createRegionInfo("FRAUD", "#e74c3c", 950, 400, 150, 
                "Dấu hiệu rõ ràng của gian lận")
        );
        
        response.put("regions", regions);
        response.put("width", 1200);
        response.put("height", 800);
        
        return response;
    }
    
    private Map<String, Object> createRegionInfo(String name, String color, 
                                                  double x, double y, double radius, 
                                                  String description) {
        Map<String, Object> info = new HashMap<>();
        info.put("name", name);
        info.put("color", color);
        info.put("centerX", x);
        info.put("centerY", y);
        info.put("radius", radius);
        info.put("description", description);
        return info;
    }
}
