package com.example.servingwebcontent.controller;

import com.example.servingwebcontent.dto.*;
import com.example.servingwebcontent.model.AnalysisSession;
import com.example.servingwebcontent.model.User;
import com.example.servingwebcontent.repository.AnalysisSessionRepository;
import com.example.servingwebcontent.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AnalysisSessionService analysisSessionService;
    private final GraphQueryService graphService;
    private final ExcelImportService excelImportService;
    private final AnalysisSessionRepository sessionRepository;
    private final UserService userService;   // ✅ THÊM
    private final PacketCaptureService packetCaptureService;
    private final GraphUpdateBroadcaster graphUpdateBroadcaster;

    public AdminController(AnalysisSessionService analysisSessionService,
                           GraphQueryService graphService,
                           ExcelImportService excelImportService,
                           AnalysisSessionRepository sessionRepository,
                           UserService userService,
                           PacketCaptureService packetCaptureService,
                           GraphUpdateBroadcaster graphUpdateBroadcaster) {  // ✅ THÊM

        this.analysisSessionService = analysisSessionService;
        this.graphService = graphService;
        this.excelImportService = excelImportService;
        this.sessionRepository = sessionRepository;
        this.userService = userService;     // ✅ THÊM
        this.packetCaptureService = packetCaptureService;
        this.graphUpdateBroadcaster = graphUpdateBroadcaster;
    }

    /* ================= DASHBOARD ================= */
    @GetMapping({"", "/"})
    public String dashboard(HttpSession session, Model model) {
        User admin = getAdmin(session);
        model.addAttribute("user", admin);
        return "dashboard/admin";
    }

    /* ================= CREATE STAFF ================= */
    @PostMapping("/create-staff")
    public String createStaff(@RequestParam String email,
                              @RequestParam String password,
                              HttpSession session,
                              Model model) {

        User admin = getAdmin(session);

        try {
            userService.createStaff(email, password, admin);
            model.addAttribute("success", "Tạo staff thành công");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        model.addAttribute("user", admin);
        return "dashboard/admin";
    }

    /* ================= SINGLE ANALYZE ================= */
    @PostMapping("/analyze")
    @ResponseBody
    public ResponseEntity<?> analyzeSingle(@RequestBody FraudInputDTO input,
                                           HttpSession session) {

        try {

            String sessionId = UUID.randomUUID().toString();

                AnalysisSession s = new AnalysisSession(
                    sessionId,
                    "MANUAL_INPUT",
                    System.currentTimeMillis(),
                    "admin@system.local",
                    1,
                    "PROCESSING"
                );

            sessionRepository.save(s);

            input.setSessionId(sessionId);

            SessionProcessResult result =
                    analysisSessionService.processSession(sessionId, List.of(input));

            AnalysisResultDTO sessionResult = result.getSessionResult();

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

    /* ================= BULK EXCEL UPLOAD ================= */
    @PostMapping("/upload-excel")
    @ResponseBody
    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file,
                                         HttpSession session) {

        User admin = getAdmin(session);

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "File Excel không hợp lệ")
            );
        }

        try {

            List<FraudInputDTO> inputs =
                    excelImportService.parseExcelFile(
                            file,
                            admin.getEmail(),
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

    /* ================= GRAPH ================= */
    @GetMapping("/graph/{sessionId}")
    @ResponseBody
    public GraphResponseDTO getGraph(@PathVariable String sessionId,
                                     HttpSession session) {
        getAdmin(session);
        return graphService.getGraphBySession(sessionId);
    }

    @GetMapping("/graph")
    @ResponseBody
    public GraphResponseDTO getGraphAll(HttpSession session) {
        getAdmin(session);
        return graphService.getGraphAll();
    }

    /* ================= SESSIONS ================= */
    @GetMapping("/sessions")
    @ResponseBody
    public List<Map<String, Object>> getSessions(HttpSession session) {
        getAdmin(session);
        return graphService.getAllSessions();
    }

    /* ================= WIRESHARK STATUS ================= */
    @GetMapping("/wireshark-status")
    @ResponseBody
    public Map<String, Object> getWiresharkStatus(HttpSession session) {
        getAdmin(session);
        return Map.of(
                "totalLines", packetCaptureService.getTotalLines(),
                "totalRecords", packetCaptureService.getTotalRecords(),
                "totalSaved", packetCaptureService.getTotalSaved(),
                "lastSavedAt", packetCaptureService.getLastSavedAt(),
                "recentEvents", packetCaptureService.getRecentEvents()
        );
    }

    @GetMapping("/wireshark-events")
    @ResponseBody
    public Object getWiresharkEvents(HttpSession session) {
        getAdmin(session);
        return packetCaptureService.getRecentEvents();
    }

    /* ================= REALTIME UPDATES (SSE) ================= */
    @GetMapping(value = "/stream/graph", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter streamGraphUpdates(HttpSession session) {
        getAdmin(session);
        return graphUpdateBroadcaster.subscribe();
    }

    /* ================= USER API (ADMIN) ================= */

    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<?> listUsers(HttpSession session) {
        getAdmin(session);
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/users")
    @ResponseBody
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body,
                                        HttpSession session) {
        User admin = getAdmin(session);

        String username = body.getOrDefault("username", null);
        String email = body.getOrDefault("email", null);
        String password = body.getOrDefault("password", null);
        String role = body.getOrDefault("role", "CUSTOMER");

        try {
            // reuse service: allow admin to create staff or generic users
            if ("STAFF".equalsIgnoreCase(role)) {
                userService.createStaff(email, password, admin);
            } else {
                userService.registerCustomer(email, password);
                // optionally set role if not default CUSTOMER
                if (!"CUSTOMER".equalsIgnoreCase(role)) {
                    userService.updateUser(email, role, null, null);
                }
            }

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/users/{id}")
    @ResponseBody
    public ResponseEntity<?> updateUser(@PathVariable("id") String id,
                                        @RequestBody Map<String, String> body,
                                        HttpSession session) {
        getAdmin(session);

        String role = body.get("role");
        String status = body.get("status");
        String password = body.get("password");

        try {
            var u = userService.updateUser(id, role, status, password);
            return ResponseEntity.ok(Map.of("success", true, "user", u));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping({"/users/{id}", "/user/{email}"})
    @ResponseBody
    public ResponseEntity<?> deleteUserApi(@PathVariable(required = false, name = "id") String id,
                                           @PathVariable(required = false, name = "email") String email,
                                           HttpSession session) {
        getAdmin(session);

        String target = id != null ? id : email;
        if (target == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing id/email"));
        }

        try {
            userService.deleteUser(target);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /* ================= NODE EDIT / DELETE (ADMIN) ================= */

    @PutMapping("/node/{nodeId}")
    @ResponseBody
    public ResponseEntity<?> updateNode(@PathVariable String nodeId,
                                        @RequestBody Map<String, String> body,
                                        HttpSession session) {

        User admin = getAdmin(session);

        String newValue = body != null ? body.get("value") : null;
        if (newValue == null || newValue.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Giá trị mới không hợp lệ"
            ));
        }

        try {
            var node = graphService.updateNode(nodeId, newValue, admin.getEmail());
            return ResponseEntity.ok(Map.of("success", true, "node", node));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/node/{nodeId}")
    @ResponseBody
    public ResponseEntity<?> deleteNode(@PathVariable String nodeId,
                                        HttpSession session) {

        User admin = getAdmin(session);

        try {
            graphService.deleteNode(nodeId, admin.getEmail());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/node/{nodeId}/block")
    @ResponseBody
    public ResponseEntity<?> blockNode(@PathVariable String nodeId,
                                       @RequestBody(required = false) Map<String, String> body,
                                       HttpSession session) {

        User admin = getAdmin(session);
        String reason = body != null ? body.get("reason") : null;

        try {
            var node = graphService.blockNode(nodeId, reason, admin.getEmail());
            return ResponseEntity.ok(Map.of("success", true, "node", node));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/node/{nodeId}/unblock")
    @ResponseBody
    public ResponseEntity<?> unblockNode(@PathVariable String nodeId,
                                         HttpSession session) {

        User admin = getAdmin(session);

        try {
            var node = graphService.unblockNode(nodeId, admin.getEmail());
            return ResponseEntity.ok(Map.of("success", true, "node", node));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /* ================= HELPER ================= */
    private User getAdmin(HttpSession session) {
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return user;
    }

    /* ================= PAGES ================= */
    @GetMapping("/admin-email")
    public String adminEmail(HttpSession session, Model model) {
        User admin = getAdmin(session);
        model.addAttribute("user", admin);
        return "admin/admin-email";
    }

    @GetMapping("/admin-ip")
    public String adminIp(HttpSession session, Model model) {
        User admin = getAdmin(session);
        model.addAttribute("user", admin);
        return "admin/admin-ip";
    }

    @GetMapping("/admin-url")
    public String adminUrl(HttpSession session, Model model) {
        User admin = getAdmin(session);
        model.addAttribute("user", admin);
        return "admin/admin-url";
    }

    @GetMapping("/admin-domain")
    public String adminDomain(HttpSession session, Model model) {
        User admin = getAdmin(session);
        model.addAttribute("user", admin);
        return "admin/admin-domain";
    }

    @GetMapping("/admin-file")
    public String adminFile(HttpSession session, Model model) {
        User admin = getAdmin(session);
        model.addAttribute("user", admin);
        return "admin/admin-file";
    }

    @GetMapping("/admin-filehash")
    public String adminFileHash(HttpSession session, Model model) {
        User admin = getAdmin(session);
        model.addAttribute("user", admin);
        return "admin/admin-filehash";
    }

    @GetMapping("/admin-victim")
    public String adminVictim(HttpSession session, Model model) {
        User admin = getAdmin(session);
        model.addAttribute("user", admin);
        return "admin/admin-victim";
    }

    @GetMapping("/user-management")
    public String userManagement(HttpSession session, Model model) {
        User admin = getAdmin(session);
        model.addAttribute("user", admin);
        model.addAttribute("users", userService.getAllUsers());
        return "admin/user-management";
    }

    @GetMapping("/about")
    public String about(HttpSession session, Model model) {
        User admin = getAdmin(session);
        model.addAttribute("user", admin);
        return "admin/about";
    }

@PostMapping("/logout")
public String logout(HttpSession session) {
    if (session != null) session.invalidate();
    return "redirect:/login";
}}
