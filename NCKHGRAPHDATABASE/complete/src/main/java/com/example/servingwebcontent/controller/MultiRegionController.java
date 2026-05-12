package com.example.servingwebcontent.controller;

import com.example.servingwebcontent.dto.BehaviorFeatureVector;
import com.example.servingwebcontent.model.RegionType;
import com.example.servingwebcontent.service.MultiRegionAnalysisService;
import com.example.servingwebcontent.service.MultiRegionDemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * MultiRegionAPI - API để test phương pháp miền
 * Endpoints để phân tích node với 3 miền: SAFE, SUSPICIOUS, FRAUD
 */
@RestController
@RequestMapping("/api/multiregion")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MultiRegionController {

    private final MultiRegionAnalysisService multiRegionService;
    private final MultiRegionDemoService multiRegionDemoService;

    public MultiRegionController(MultiRegionAnalysisService multiRegionService,
                                  MultiRegionDemoService multiRegionDemoService) {
        this.multiRegionService = multiRegionService;
        this.multiRegionDemoService = multiRegionDemoService;
    }

    /**
     * DEMO 1: Phương pháp miền thực sự
     * GET /api/multiregion/demo/three-regions
     * 
     * Trả về:
     * - 3 node khác nhau (safe, suspicious, fraud)
     * - Khoảng cách tới 3 miền
     * - Xác suất thuộc miền nào
     */
    @GetMapping("/demo/three-regions")
    public ResponseEntity<?> demoThreeRegions() {
        try {
            // In console output
            multiRegionDemoService.demoThreeRegionAnalysis();
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Demo phương pháp miền - Xem console output");
            response.put("description", "3 node được phân tích so với 3 miền (Safe, Suspicious, Fraud)");
            response.put("next_endpoint", "/api/multiregion/demo/complex-case");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DEMO 2: Trường hợp phức tạp
     * Node nằm giữa 2 miền (mâu thuẫn)
     */
    @GetMapping("/demo/complex-case")
    public ResponseEntity<?> demoComplexCase() {
        try {
            multiRegionDemoService.demoComplexCase();
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Demo trường hợp phức tạp - Xem console output");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DEMO 3: So sánh Rule-Based vs Multi-Region
     */
    @GetMapping("/demo/rule-based-vs-region")
    public ResponseEntity<?> demoRuleBasedVsRegion() {
        try {
            multiRegionDemoService.demoRuleBasedVsRegion();
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "So sánh Rule-Based vs Multi-Region - Xem console output");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DEMO 4: Visualization
     */
    @GetMapping("/demo/visualization")
    public ResponseEntity<?> demoVisualization() {
        try {
            multiRegionDemoService.printRegionVisualization();
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "ASCII Visualization - Xem console output");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/multiregion/analyze
     * 
     * Phân tích một node với 12 thuộc tính
     * 
     * Body:
     * {
     *   "ipCount": 8,
     *   "urlCount": 12,
     *   "emailCount": 10,
     *   "domainCount": 6,
     *   "failedLoginCount": 2,
     *   "requestFrequency": 3.0,
     *   "vpn": true,
     *   "blacklist": false,
     *   "suspiciousUrl": true,
     *   "torNetwork": false,
     *   "spamPattern": true,
     *   "abnormalAccessTime": true
     * }
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeNode(@RequestBody BehaviorFeatureVector node) {
        if (node == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Node không được null"));
        }

        try {
            // Phân tích node
            MultiRegionAnalysisService.RegionAnalysisResult result = 
                multiRegionService.analyzeAgainstRegions(node);
            
            // Áp dụng penalties
            multiRegionService.applyFeaturePenalties(node, result);

            // Chuẩn bị response
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "SUCCESS");
            response.put("node", formatNode(node));
            response.put("primary_region", result.getPrimaryRegion());
            response.put("anomaly_score", String.format("%.2f", result.getAnomalyScore()));
            
            // Region probabilities
            Map<String, String> probabilities = new LinkedHashMap<>();
            probabilities.put("SAFE", String.format("%.1f%%", result.getRegionProbability(RegionType.SAFE) * 100));
            probabilities.put("SUSPICIOUS", String.format("%.1f%%", result.getRegionProbability(RegionType.SUSPICIOUS) * 100));
            probabilities.put("FRAUD", String.format("%.1f%%", result.getRegionProbability(RegionType.FRAUD) * 100));
            response.put("probabilities", probabilities);

            // Region distances
            Map<String, String> distances = new LinkedHashMap<>();
            distances.put("SAFE", String.format("%.2f", result.getRegionDistance(RegionType.SAFE)));
            distances.put("SUSPICIOUS", String.format("%.2f", result.getRegionDistance(RegionType.SUSPICIOUS)));
            distances.put("FRAUD", String.format("%.2f", result.getRegionDistance(RegionType.FRAUD)));
            response.put("distances", distances);

            // Details
            response.put("details", result.getDetails());

            // Interpretation
            if (result.getAnomalyScore() > 0.4) {
                response.put("warning", "ANOMALY DETECTED - Node nằm giữa các miền (mâu thuẫn)");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage(), 
                                 "stack_trace", e.getStackTrace()));
        }
    }

    /**
     * GET /api/multiregion/regions
     * Lấy thông tin 3 miền
     */
    @GetMapping("/regions")
    public ResponseEntity<?> getRegions() {
        try {
            Map<String, Object> response = new LinkedHashMap<>();
            
            for (RegionType type : RegionType.values()) {
                Map<String, Object> regionInfo = new LinkedHashMap<>();
                regionInfo.put("name", type.getLabel());
                regionInfo.put("description", type.getDescription());
                regionInfo.put("score_range", type.getMinScore() + " - " + type.getMaxScore());
                
                response.put(type.name(), regionInfo);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/multiregion/safe-node
     * Tạo một node AN TOÀN mẫu
     */
    @GetMapping("/sample-nodes/safe")
    public ResponseEntity<?> getSafeNode() {
        BehaviorFeatureVector node = new BehaviorFeatureVector(
                1, 2, 3, 2, 0, 0.5,
                false, false, false, false, false, false
        );
        
        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(node);
        multiRegionService.applyFeaturePenalties(node, result);

        return buildAnalysisResponse("SAFE_NODE", node, result);
    }

    /**
     * GET /api/multiregion/sample-nodes/suspicious
     * Tạo một node NGHI NGỜ mẫu
     */
    @GetMapping("/sample-nodes/suspicious")
    public ResponseEntity<?> getSuspiciousNode() {
        BehaviorFeatureVector node = new BehaviorFeatureVector(
                5, 8, 10, 6, 3, 3.5,
                true, false, true, false, true, true
        );
        
        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(node);
        multiRegionService.applyFeaturePenalties(node, result);

        return buildAnalysisResponse("SUSPICIOUS_NODE", node, result);
    }

    /**
     * GET /api/multiregion/sample-nodes/fraud
     * Tạo một node VI PHẠM mẫu
     */
    @GetMapping("/sample-nodes/fraud")
    public ResponseEntity<?> getFraudNode() {
        BehaviorFeatureVector node = new BehaviorFeatureVector(
                15, 20, 25, 18, 8, 10.0,
                true, true, true, true, true, true
        );
        
        MultiRegionAnalysisService.RegionAnalysisResult result = 
            multiRegionService.analyzeAgainstRegions(node);
        multiRegionService.applyFeaturePenalties(node, result);

        return buildAnalysisResponse("FRAUD_NODE", node, result);
    }

    /**
     * Helper: Build analysis response
     */
    private ResponseEntity<?> buildAnalysisResponse(String nodeType, 
                                                   BehaviorFeatureVector node,
                                                   MultiRegionAnalysisService.RegionAnalysisResult result) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("type", nodeType);
        response.put("node", formatNode(node));
        response.put("primary_region", result.getPrimaryRegion());
        response.put("anomaly_score", String.format("%.2f", result.getAnomalyScore()));
        
        Map<String, String> probabilities = new LinkedHashMap<>();
        probabilities.put("SAFE", String.format("%.1f%%", result.getRegionProbability(RegionType.SAFE) * 100));
        probabilities.put("SUSPICIOUS", String.format("%.1f%%", result.getRegionProbability(RegionType.SUSPICIOUS) * 100));
        probabilities.put("FRAUD", String.format("%.1f%%", result.getRegionProbability(RegionType.FRAUD) * 100));
        response.put("probabilities", probabilities);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Helper: Format node for display
     */
    private Map<String, Object> formatNode(BehaviorFeatureVector node) {
        Map<String, Object> formatted = new LinkedHashMap<>();
        formatted.put("ipCount", node.getIpCount());
        formatted.put("urlCount", node.getUrlCount());
        formatted.put("emailCount", node.getEmailCount());
        formatted.put("domainCount", node.getDomainCount());
        formatted.put("failedLoginCount", node.getFailedLoginCount());
        formatted.put("requestFrequency", node.getRequestFrequency());
        formatted.put("vpn", node.isVpn());
        formatted.put("blacklist", node.isBlacklist());
        formatted.put("suspiciousUrl", node.isSuspiciousUrl());
        formatted.put("torNetwork", node.isTorNetwork());
        formatted.put("spamPattern", node.isSpamPattern());
        formatted.put("abnormalAccessTime", node.isAbnormalAccessTime());
        return formatted;
    }
}
