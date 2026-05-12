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
     * DEMO 1: Phương pháp miền thực sự - 3 node
     * GET /api/multiregion/demo/three-regions
     * 
     * Trả về:
     * - 3 node (SAFE, SUSPICIOUS, FRAUD)
     * - Khoảng cách tới 3 miền
     * - Xác suất thuộc miền nào
     */
    @GetMapping("/demo/three-regions")
    public ResponseEntity<?> demoThreeRegions() {
        try {
            // In console output
            multiRegionDemoService.demoThreeRegionAnalysis();
            
            // Tạo API response với dữ liệu
            List<Map<String, Object>> results = new ArrayList<>();
            
            // Node 1: SAFE
            BehaviorFeatureVector safeNode = new BehaviorFeatureVector(
                    1, 2, 3, 2, 0, 0.5, false, false, false, false, false, false);
            MultiRegionAnalysisService.RegionAnalysisResult safeResult = 
                multiRegionService.analyzeAgainstRegions(safeNode);
            multiRegionService.applyFeaturePenalties(safeNode, safeResult);
            results.add(buildDemoResponse("SAFE_NODE", safeNode, safeResult));
            
            // Node 2: SUSPICIOUS
            BehaviorFeatureVector suspiciousNode = new BehaviorFeatureVector(
                    5, 8, 10, 6, 3, 3.5, true, false, true, false, true, true);
            MultiRegionAnalysisService.RegionAnalysisResult suspiciousResult = 
                multiRegionService.analyzeAgainstRegions(suspiciousNode);
            multiRegionService.applyFeaturePenalties(suspiciousNode, suspiciousResult);
            results.add(buildDemoResponse("SUSPICIOUS_NODE", suspiciousNode, suspiciousResult));
            
            // Node 3: FRAUD
            BehaviorFeatureVector fraudNode = new BehaviorFeatureVector(
                    15, 20, 25, 18, 8, 10.0, true, true, true, true, true, true);
            MultiRegionAnalysisService.RegionAnalysisResult fraudResult = 
                multiRegionService.analyzeAgainstRegions(fraudNode);
            multiRegionService.applyFeaturePenalties(fraudNode, fraudResult);
            results.add(buildDemoResponse("FRAUD_NODE", fraudNode, fraudResult));
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "SUCCESS");
            response.put("title", "DEMO: 3 LOẠI NODE - PHƯƠNG PHÁP MIỀN THỰC SỰ");
            response.put("description", "Phân tích 3 node với hành vi khác nhau vào 3 miền");
            response.put("results", results);
            response.put("console_output", "Xem terminal/console để xem chi tiết đầy đủ");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DEMO 2: Trường hợp phức tạp - Node mâu thuẫn
     * GET /api/multiregion/demo/complex-case
     * 
     * Node nằm giữa 2 miền (VPN=true nhưng blacklist=false)
     */
    @GetMapping("/demo/complex-case")
    public ResponseEntity<?> demoComplexCase() {
        try {
            multiRegionDemoService.demoComplexCase();
            
            // Node mâu thuẫn: VPN=true (nghi ngờ) nhưng blacklist=false (bình thường)
            BehaviorFeatureVector complexNode = new BehaviorFeatureVector(
                    8, 12, 8, 5, 2, 2.0, true, false, true, false, false, true);
            
            MultiRegionAnalysisService.RegionAnalysisResult result = 
                multiRegionService.analyzeAgainstRegions(complexNode);
            multiRegionService.applyFeaturePenalties(complexNode, result);
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "SUCCESS");
            response.put("title", "DEMO: TRƯỜNG HỢP PHỨC TẠP - MẦU THUẪN");
            response.put("description", "Node có tính năng vừa bình thường vừa nghi ngờ");
            response.put("node", formatNode(complexNode));
            response.put("anomaly", new LinkedHashMap<String, Object>() {{
                put("score", String.format("%.2f", result.getAnomalyScore()));
                put("status", result.getAnomalyScore() > 0.4 ? "🔴 ANOMALY DETECTED!" : "✅ Normal");
                put("reason", "VPN=true nhưng Blacklist=false → Mâu thuẫn trong hành vi");
            }});
            response.put("region_analysis", new LinkedHashMap<String, Object>() {{
                put("primary_region", result.getPrimaryRegion());
                put("fraud_probability", String.format("%.2f%%", result.getRegionProbability(RegionType.FRAUD) * 100));
                put("suspicious_probability", String.format("%.2f%%", result.getRegionProbability(RegionType.SUSPICIOUS) * 100));
                put("safe_probability", String.format("%.2f%%", result.getRegionProbability(RegionType.SAFE) * 100));
            }});
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DEMO 3: So sánh Rule-Based vs Multi-Region
     * GET /api/multiregion/demo/rule-based-vs-region
     */
    @GetMapping("/demo/rule-based-vs-region")
    public ResponseEntity<?> demoRuleBasedVsRegion() {
        try {
            multiRegionDemoService.demoRuleBasedVsRegion();
            
            // Node A: VPN + Spam (rule-based score ~30)
            BehaviorFeatureVector nodeA = new BehaviorFeatureVector(
                    2, 3, 2, 2, 0, 1.0, true, false, false, false, true, false);
            MultiRegionAnalysisService.RegionAnalysisResult resultA = 
                multiRegionService.analyzeAgainstRegions(nodeA);
            multiRegionService.applyFeaturePenalties(nodeA, resultA);
            
            // Node B: HighIpCount + HighUrlCount (rule-based score ~25)
            BehaviorFeatureVector nodeB = new BehaviorFeatureVector(
                    15, 15, 5, 5, 1, 2.0, false, false, false, false, false, false);
            MultiRegionAnalysisService.RegionAnalysisResult resultB = 
                multiRegionService.analyzeAgainstRegions(nodeB);
            multiRegionService.applyFeaturePenalties(nodeB, resultB);
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "SUCCESS");
            response.put("title", "DEMO: RULE-BASED vs MULTI-REGION");
            response.put("description", "Cùng rule-based score nhưng miền khác → Multi-region tốt hơn");
            response.put("comparison", new ArrayList<Map<String, Object>>() {{
                add(new LinkedHashMap<String, Object>() {{
                    put("node", "A: VPN + Spam");
                    put("rule_based_score", "~30");
                    put("multi_region", new LinkedHashMap<String, Object>() {{
                        put("primary_region", resultA.getPrimaryRegion());
                        put("fraud_probability", String.format("%.2f%%", resultA.getRegionProbability(RegionType.FRAUD) * 100));
                    }});
                }});
                add(new LinkedHashMap<String, Object>() {{
                    put("node", "B: HighIpCount + HighUrlCount");
                    put("rule_based_score", "~25");
                    put("multi_region", new LinkedHashMap<String, Object>() {{
                        put("primary_region", resultB.getPrimaryRegion());
                        put("fraud_probability", String.format("%.2f%%", resultB.getRegionProbability(RegionType.FRAUD) * 100));
                    }});
                }});
            }});
            response.put("conclusion", "Mặc dù cùng rule score, Multi-Region phân biệt loại hành vi khác nhau");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DEMO 4: Định nghĩa 3 miền
     * GET /api/multiregion/demo/definitions
     */
    @GetMapping("/demo/definitions")
    public ResponseEntity<?> demoDefinitions() {
        try {
            multiRegionDemoService.printRegionDefinitions();
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "SUCCESS");
            response.put("title", "ĐỊNH NGHĨA 3 MIỀN");
            response.put("regions", new ArrayList<Map<String, Object>>() {{
                add(new LinkedHashMap<String, Object>() {{
                    put("name", "🟢 SAFE REGION");
                    put("score_range", "0.0 - 0.33");
                    put("characteristics", new ArrayList<String>() {{
                        add("VPN: false");
                        add("Blacklist: false");
                        add("TOR: false");
                        add("Spam: false");
                        add("IpCount: 1-2");
                        add("UrlCount: 1-5");
                    }});
                    put("use_case", "Hành vi bình thường, người dùng tin cậy");
                }});
                add(new LinkedHashMap<String, Object>() {{
                    put("name", "🟡 SUSPICIOUS REGION");
                    put("score_range", "0.33 - 0.67");
                    put("characteristics", new ArrayList<String>() {{
                        add("VPN: mixed");
                        add("Blacklist: sometimes");
                        add("IpCount: 5-10");
                        add("UrlCount: 8-15");
                    }});
                    put("use_case", "Hành vi rủi ro vừa phải, cần kiểm tra kỹ");
                }});
                add(new LinkedHashMap<String, Object>() {{
                    put("name", "🔴 FRAUD REGION");
                    put("score_range", "0.67 - 1.0");
                    put("characteristics", new ArrayList<String>() {{
                        add("VPN: true");
                        add("Blacklist: true");
                        add("TOR: true");
                        add("Spam: true");
                        add("IpCount: 15+");
                        add("UrlCount: 20+");
                    }});
                    put("use_case", "Xác định gian lận, cần hành động ngay");
                }});
            }});
            
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
     * GET /api/multiregion/sample-nodes/safe
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

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("nodeType", "SAFE_NODE");
        response.put("node", formatNode(node));
        response.put("primaryRegion", result.getPrimaryRegion());
        response.put("anomalyScore", String.format("%.2f", result.getAnomalyScore()));
        response.put("distances", Map.of(
            "SAFE", String.format("%.2f", result.getRegionDistance(RegionType.SAFE)),
            "SUSPICIOUS", String.format("%.2f", result.getRegionDistance(RegionType.SUSPICIOUS)),
            "FRAUD", String.format("%.2f", result.getRegionDistance(RegionType.FRAUD))
        ));
        response.put("probabilities", Map.of(
            "SAFE", String.format("%.2f%%", result.getRegionProbability(RegionType.SAFE) * 100),
            "SUSPICIOUS", String.format("%.2f%%", result.getRegionProbability(RegionType.SUSPICIOUS) * 100),
            "FRAUD", String.format("%.2f%%", result.getRegionProbability(RegionType.FRAUD) * 100)
        ));
        return ResponseEntity.ok(response);
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

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("nodeType", "SUSPICIOUS_NODE");
        response.put("node", formatNode(node));
        response.put("primaryRegion", result.getPrimaryRegion());
        response.put("anomalyScore", String.format("%.2f", result.getAnomalyScore()));
        response.put("distances", Map.of(
            "SAFE", String.format("%.2f", result.getRegionDistance(RegionType.SAFE)),
            "SUSPICIOUS", String.format("%.2f", result.getRegionDistance(RegionType.SUSPICIOUS)),
            "FRAUD", String.format("%.2f", result.getRegionDistance(RegionType.FRAUD))
        ));
        response.put("probabilities", Map.of(
            "SAFE", String.format("%.2f%%", result.getRegionProbability(RegionType.SAFE) * 100),
            "SUSPICIOUS", String.format("%.2f%%", result.getRegionProbability(RegionType.SUSPICIOUS) * 100),
            "FRAUD", String.format("%.2f%%", result.getRegionProbability(RegionType.FRAUD) * 100)
        ));
        return ResponseEntity.ok(response);
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

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("nodeType", "FRAUD_NODE");
        response.put("node", formatNode(node));
        response.put("primaryRegion", result.getPrimaryRegion());
        response.put("anomalyScore", String.format("%.2f", result.getAnomalyScore()));
        response.put("distances", Map.of(
            "SAFE", String.format("%.2f", result.getRegionDistance(RegionType.SAFE)),
            "SUSPICIOUS", String.format("%.2f", result.getRegionDistance(RegionType.SUSPICIOUS)),
            "FRAUD", String.format("%.2f", result.getRegionDistance(RegionType.FRAUD))
        ));
        response.put("probabilities", Map.of(
            "SAFE", String.format("%.2f%%", result.getRegionProbability(RegionType.SAFE) * 100),
            "SUSPICIOUS", String.format("%.2f%%", result.getRegionProbability(RegionType.SUSPICIOUS) * 100),
            "FRAUD", String.format("%.2f%%", result.getRegionProbability(RegionType.FRAUD) * 100)
        ));
        return ResponseEntity.ok(response);
    }

    /**
     * Helper: Build demo response
     */
    private Map<String, Object> buildDemoResponse(String nodeType, 
                                                   BehaviorFeatureVector node,
                                                   MultiRegionAnalysisService.RegionAnalysisResult result) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("nodeType", nodeType);
        response.put("node", formatNode(node));
        response.put("primaryRegion", result.getPrimaryRegion());
        response.put("anomalyScore", String.format("%.2f", result.getAnomalyScore()));
        
        Map<String, String> distances = new LinkedHashMap<>();
        distances.put("SAFE", String.format("%.2f", result.getRegionDistance(RegionType.SAFE)));
        distances.put("SUSPICIOUS", String.format("%.2f", result.getRegionDistance(RegionType.SUSPICIOUS)));
        distances.put("FRAUD", String.format("%.2f", result.getRegionDistance(RegionType.FRAUD)));
        response.put("distances", distances);
        
        Map<String, String> probabilities = new LinkedHashMap<>();
        probabilities.put("SAFE", String.format("%.2f%%", result.getRegionProbability(RegionType.SAFE) * 100));
        probabilities.put("SUSPICIOUS", String.format("%.2f%%", result.getRegionProbability(RegionType.SUSPICIOUS) * 100));
        probabilities.put("FRAUD", String.format("%.2f%%", result.getRegionProbability(RegionType.FRAUD) * 100));
        response.put("probabilities", probabilities);
        
        return response;
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
