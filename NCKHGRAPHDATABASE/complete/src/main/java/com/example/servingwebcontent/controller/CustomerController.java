package com.example.servingwebcontent.controller;

import com.example.servingwebcontent.dto.FraudInputDTO;
import com.example.servingwebcontent.dto.GraphResponseDTO;
import com.example.servingwebcontent.dto.OutputDTO;
import com.example.servingwebcontent.dto.*;
import com.example.servingwebcontent.service.AnalysisSessionService;
import com.example.servingwebcontent.service.ExcelImportService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.util.*;
import com.example.servingwebcontent.model.User;
import com.example.servingwebcontent.service.FraudAnalysisService;
import com.example.servingwebcontent.service.GraphQueryService;
import com.example.servingwebcontent.repository.AnalysisSessionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/customer")
@CrossOrigin
public class CustomerController {

    private final FraudAnalysisService analysisService;
    private final GraphQueryService graphService;
    private final AnalysisSessionService analysisSessionService;
    private final ExcelImportService excelImportService;
    private final AnalysisSessionRepository sessionRepository;
    private final com.example.servingwebcontent.service.DecisionService decisionService;
    private final com.example.servingwebcontent.service.EnhancedChatbotService chatbotService;
    private final com.example.servingwebcontent.service.AlertLoggingService alertLoggingService;

        public CustomerController(FraudAnalysisService analysisService,
                  GraphQueryService graphService,
                  AnalysisSessionService analysisSessionService,
                  ExcelImportService excelImportService,
                  AnalysisSessionRepository sessionRepository,
                  com.example.servingwebcontent.service.DecisionService decisionService,
                  com.example.servingwebcontent.service.EnhancedChatbotService chatbotService,
                  com.example.servingwebcontent.service.AlertLoggingService alertLoggingService) {
        this.analysisService = analysisService;
        this.graphService = graphService;
        this.analysisSessionService = analysisSessionService;
        this.excelImportService = excelImportService;
        this.sessionRepository = sessionRepository;
        this.decisionService = decisionService;
        this.chatbotService = chatbotService;
        this.alertLoggingService = alertLoggingService;
        }

        /* ================= BULK EXCEL UPLOAD (CUSTOMER) ================= */
        @PostMapping("/upload-excel")
        @ResponseBody
        public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file,
                         HttpSession session) {

        User customer = getCustomer(session);

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "File Excel không hợp lệ")
            );
        }

        try {

            List<FraudInputDTO> inputs =
                excelImportService.parseExcelFile(
                    file,
                    customer.getEmail(),
                    System.currentTimeMillis()
                );

            if (inputs == null || inputs.isEmpty()) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "Không có dữ liệu hợp lệ")
            );
            }

            String sessionId = inputs.get(0).getSessionId();

            SessionProcessResult result =
                analysisSessionService.processSession(sessionId, inputs);

            AnalysisResultDTO sessionResult = result.getSessionResult();
            List<AnalysisResultDTO> rowRisks = result.getRowResults();

            List<RowResultDTO> rows = new ArrayList<>();

            for (int i = 0; i < inputs.size(); i++) {

            FraudInputDTO dto = inputs.get(i);
            AnalysisResultDTO rowRisk = rowRisks.get(i);

            rows.add(new RowResultDTO(
                i + 1,
                dto.getEmail(),
                dto.getIp(),
                dto.getUrl(),
                dto.getDomain(),
                dto.getFileNode(),
                dto.getFileHash(),
                dto.getVictimAccount(),
                rowRisk.getRiskLevel(),
                rowRisk.getVerdict()
            ));
            }

            BulkAnalysisResponseDTO response =
                new BulkAnalysisResponseDTO(
                    sessionId,
                    inputs.size(),
                    inputs.size(),
                    0,
                    rows
                );

            return ResponseEntity.ok(response);

        } catch (IOException e) {

            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "success", false,
                    "message", "Lỗi đọc file Excel: " + e.getMessage()
                ));

        } catch (Exception e) {

            e.printStackTrace();
            String msg = (e.getMessage() == null || e.getMessage().isBlank())
                    ? "Lỗi xử lý hệ thống"
                    : e.getMessage();

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", msg
                ));
        }
        }

    /* ================= DASHBOARD ================= */

    @GetMapping({"", "/"})
    public String dashboard(HttpSession session, Model model) {
        User customer = getCustomer(session);
        model.addAttribute("user", customer);
        return "dashboard/customer";
    }

    /* ================= FRAUD ANALYSIS ================= */

        @PostMapping("/analyze")
        @ResponseBody
        public ResponseEntity<?> analyze(@RequestBody(required = false) FraudInputDTO input,
                         HttpSession session) {

        User customer = getCustomer(session); // chỉ check quyền

        if (input == null) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "Dữ liệu input bị rỗng")
            );
        }

        try {
            String sessionId = UUID.randomUUID().toString();

            com.example.servingwebcontent.model.AnalysisSession s =
                new com.example.servingwebcontent.model.AnalysisSession(
                    sessionId,
                    "MANUAL_INPUT",
                    System.currentTimeMillis(),
                    customer.getEmail(),
                    1,
                    "PROCESSING"
                );

            sessionRepository.save(s);

            input.setSessionId(sessionId);

            com.example.servingwebcontent.dto.SessionProcessResult result =
                analysisSessionService.processSession(sessionId, List.of(input));

            com.example.servingwebcontent.dto.AnalysisResultDTO sessionResult = result.getSessionResult();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "sessionId", sessionResult.getSessionId(),
                "verdict", sessionResult.getVerdict(),
                "riskScore", sessionResult.getRiskScore(),
                "scamType", sessionResult.getRiskLevel(),
                "indicators", sessionResult.getIndicators(),
                "ruleScore", sessionResult.getRuleScore(),
                "knnScore", sessionResult.getKnnScore(),
                "probabilityScore", sessionResult.getProbabilityScore(),
                "features", sessionResult.getFeatures()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
        }

    /* ================= GRAPH VIEW ================= */

    @GetMapping({"/graph", "/email-graph", "/ip-graph", "/url-graph"})
    @ResponseBody
    public GraphResponseDTO getGraph(HttpSession session) {
        var user = getCustomer(session);

        // If the frontend requests the generic customer graph (no specific session),
        // return the merged graph of ALL sessions belonging to this customer.
        try {
            List<com.example.servingwebcontent.model.AnalysisSession> list =
                    sessionRepository.findAllByCreatedByOrderByCreatedAtDesc(user.getEmail());

            if (list == null || list.isEmpty()) {
                return new GraphResponseDTO(List.of(), List.of());
            }

            List<String> ids = new ArrayList<>();
            for (var s : list) ids.add(s.getId());

            return graphService.getGraphBySessionIds(ids);
        } catch (Exception e) {
            return new GraphResponseDTO(List.of(), List.of());
        }
    }

    /* ================ GRAPH BY SESSION (CUSTOMER) ================ */
    @GetMapping("/graph/{sessionId}")
    @ResponseBody
    public GraphResponseDTO getGraphBySession(@PathVariable String sessionId, HttpSession session) {
        var user = getCustomer(session);

        var opt = sessionRepository.findById(sessionId);
        if (opt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session không tồn tại");
        }

        var s = opt.get();
        if (!user.getEmail().equalsIgnoreCase(s.getCreatedBy())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền truy cập session này");
        }

        return graphService.getGraphBySession(sessionId);
    }

    /* ================ SESSIONS LIST (CUSTOMER) ================ */
    @GetMapping({"/sessions", "/session-list", "/session/list", "/get-sessions"})
    @ResponseBody
    public List<Map<String, Object>> getSessions(HttpSession session) {
        var user = getCustomer(session);

        List<Map<String, Object>> out = new ArrayList<>();
        try {
            List<com.example.servingwebcontent.model.AnalysisSession> list =
                    sessionRepository.findAllByCreatedByOrderByCreatedAtDesc(user.getEmail());

            for (var s : list) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", s.getId());
                m.put("createdAt", s.getCreatedAt());
                m.put("fileName", s.getFileName());
                m.put("status", s.getStatus());
                out.add(m);
            }
        } catch (Exception e) {
            // return empty list on error
        }

        return out;
    }

    /* ================= MANAGEMENT PAGES ================= */

    @GetMapping("/customer-email")
    public String emailManagement(HttpSession session, Model model) {
        User customer = getCustomer(session);
        model.addAttribute("user", customer);
        return "customer/customer-email";
    }

    @GetMapping("/customer-ip")
    public String ipManagement(HttpSession session, Model model) {
        User customer = getCustomer(session);
        model.addAttribute("user", customer);
        return "customer/customer-ip";
    }

    @GetMapping("/customer-url")
    public String urlManagement(HttpSession session, Model model) {
        User customer = getCustomer(session);
        model.addAttribute("user", customer);
        return "customer/customer-url";
    }

    @GetMapping("/about")
    public String aboutDeveloper(HttpSession session, Model model) {
        User customer = getCustomer(session);
        model.addAttribute("user", customer);
        return "customer/about";
    }

    /* ================= DECISION API ================= */

    @PostMapping("/node-decision")
    @ResponseBody
    public ResponseEntity<?> getNodeDecision(
            @RequestParam String nodeId,
            @RequestParam String nodeType,
            @RequestParam String nodeValue,
            @RequestParam String riskLevel,
            @RequestParam(defaultValue = "0") int riskScore,
            HttpSession session) {
        try {
            User customer = getCustomer(session);

            DecisionDTO decision = decisionService.makeDecision(riskScore, riskLevel);

            // Log the alert
            alertLoggingService.logAlert(nodeId, nodeType, nodeValue, riskLevel, riskScore, decision.getDecision());

            return ResponseEntity.ok(decision);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error generating decision: " + e.getMessage()
            ));
        }
    }

    /* ================= CHATBOT API (4 Layers) ================= */

    @PostMapping("/node-analysis")
    @ResponseBody
    public ResponseEntity<?> analyzeNode(
            @RequestParam String nodeId,
            @RequestParam String nodeType,
            @RequestParam String nodeValue,
            @RequestParam String riskLevel,
            @RequestParam(defaultValue = "0") int riskScore,
            HttpSession session) {
        try {
            User customer = getCustomer(session);

            List<String> indicators = resolveNodeIndicators(nodeId);
            
            ChatbotResponseDTO chatbotResponse = chatbotService.generateAnalysis(
                    nodeId, nodeType, nodeValue, riskLevel, riskScore, indicators
            );

            // Log detection
            alertLoggingService.logDetection(
                    nodeId,
                    nodeType,
                    nodeValue,
                    riskLevel,
                    chatbotResponse.getAnalysisDescription(),
                    chatbotResponse.getThreatExplanation(),
                    chatbotResponse.getRelatedNodes() != null
                            ? chatbotResponse.getRelatedNodes().stream()
                            .map(ChatbotResponseDTO.RelatedNodeDTO::getNodeId)
                            .toList()
                            : new ArrayList<>()
            );

            return ResponseEntity.ok(chatbotResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error analyzing node: " + e.getMessage()
            ));
        }
    }

    private List<String> resolveNodeIndicators(String nodeId) {
        try {
            GraphResponseDTO graph = graphService.getGraph();
            if (graph == null || graph.getNodes() == null) {
                return new ArrayList<>();
            }

            return graph.getNodes().stream()
                    .filter(node -> nodeId.equals(node.getId()))
                    .findFirst()
                    .map(node -> node.getIndicators() != null ? new ArrayList<>(node.getIndicators()) : new ArrayList<String>())
                    .orElseGet(ArrayList::new);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    /* ================= LOGS API ================= */

    @GetMapping("/alerts")
    @ResponseBody
    public ResponseEntity<?> getAlerts(
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String decision,
            @RequestParam(defaultValue = "10") int limit,
            HttpSession session) {
        try {
            User customer = getCustomer(session);

            List<?> alerts;
            if (riskLevel != null && !riskLevel.isEmpty()) {
                alerts = alertLoggingService.getAlertsByRiskLevel(riskLevel);
            } else if (decision != null && !decision.isEmpty()) {
                alerts = alertLoggingService.getAlertsByDecision(decision);
            } else {
                alerts = alertLoggingService.getRecentAlerts(limit);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", alerts.size(),
                    "alerts", alerts
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error retrieving alerts: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/detections")
    @ResponseBody
    public ResponseEntity<?> getDetections(
            @RequestParam(defaultValue = "10") int limit,
            HttpSession session) {
        try {
            User customer = getCustomer(session);

            List<?> detections = alertLoggingService.getRecentDetections(limit);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", detections.size(),
                    "detections", detections
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error retrieving detections: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/log-statistics")
    @ResponseBody
    public ResponseEntity<?> getLogStatistics(HttpSession session) {
        try {
            User customer = getCustomer(session);

            Map<String, Object> stats = alertLoggingService.getStatistics();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "statistics", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error retrieving statistics: " + e.getMessage()
            ));
        }
    }

    /* ================= LOGOUT ================= */

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }

    /* ================= HELPER ================= */

    private User getCustomer(HttpSession session) {
        if (session == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Customer access required");
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Customer access required");
        }

        String role = user.getRole() == null
                ? ""
                : user.getRole().trim().toUpperCase();

        if (!"CUSTOMER".equals(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Customer access required");
        }

        return user;
    }
}
