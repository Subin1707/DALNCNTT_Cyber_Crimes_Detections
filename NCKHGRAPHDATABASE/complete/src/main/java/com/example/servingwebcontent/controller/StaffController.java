package com.example.servingwebcontent.controller;

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
import com.example.servingwebcontent.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/staff")
@CrossOrigin
public class StaffController {

    private final FraudAnalysisService fraudAnalysisService;
    private final GraphQueryService graphService;
    private final UserService userService;
    private final AnalysisSessionService analysisSessionService;
    private final ExcelImportService excelImportService;

        public StaffController(FraudAnalysisService fraudAnalysisService,
                   GraphQueryService graphService,
                   UserService userService,
                   AnalysisSessionService analysisSessionService,
                   ExcelImportService excelImportService) {

        this.fraudAnalysisService = fraudAnalysisService;
        this.graphService = graphService;
        this.userService = userService;
        this.analysisSessionService = analysisSessionService;
        this.excelImportService = excelImportService;
        }

        /* ================= BULK EXCEL UPLOAD (STAFF) ================= */
        @PostMapping("/upload-excel")
        @ResponseBody
        public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file,
                         HttpSession session) {

        User staff = getStaff(session);

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "File Excel không hợp lệ")
            );
        }

        try {

            List<FraudInputDTO> inputs =
                excelImportService.parseExcelFile(
                    file,
                    staff.getEmail(),
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
        User staff = getStaff(session);
        model.addAttribute("user", staff);
        return "dashboard/staff";
    }

    /* ================= PREVIEW FRAUD (NO SESSION) ================= */
    @PostMapping("/analyze")
    @ResponseBody
    public OutputDTO analyzePreview(@RequestBody FraudInputDTO input,
                                    HttpSession session) {
        getStaff(session);
        return fraudAnalysisService.analyzePreview(input);
    }

    /* ================= GRAPH BY SESSION ================= */
    @GetMapping("/graph/{sessionId}")
    @ResponseBody
    public GraphResponseDTO getGraph(@PathVariable String sessionId,
                                     HttpSession session) {
        getStaff(session);

        if (sessionId == null || sessionId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SessionId không hợp lệ");
        }

        return graphService.getGraphBySession(sessionId);
    }

    @GetMapping("/graph")
    @ResponseBody
    public GraphResponseDTO getGraphAll(HttpSession session) {
        getStaff(session);
        return graphService.getGraphAll();
    }

    /* ================= UPDATE NODE ================= */
    @PutMapping("/node/{id}")
    @ResponseBody
    public Map<String, Object> editNode(@PathVariable String id,
                                        @RequestBody UpdateNodeRequest payload,
                                        HttpSession session) {

        User staff = getStaff(session);

        if (id == null || id.isBlank()) {
            return Map.of(
                    "success", false,
                    "code", "INVALID_ID",
                    "message", "Node id không hợp lệ"
            );
        }

        if (payload == null || payload.getValue() == null || payload.getValue().isBlank()) {
            return Map.of(
                    "success", false,
                    "code", "INVALID_VALUE",
                    "message", "Value không được để trống"
            );
        }

        try {
            GraphNodeDTO updated =
                    graphService.updateNode(id, payload.getValue(), staff.getEmail());

            return Map.of(
                    "success", true,
                    "message", "Cập nhật node thành công",
                    "node", updated
            );

        } catch (IllegalArgumentException e) {
            return Map.of(
                    "success", false,
                    "code", "INVALID_VALUE",
                    "message", e.getMessage()
            );

        } catch (IllegalStateException e) {
            return Map.of(
                    "success", false,
                    "code", "CONFLICT",
                    "message", e.getMessage()
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "success", false,
                    "code", "SERVER_ERROR",
                    "message", "Server error"
            );
        }
    }

    /* ================= USER MANAGEMENT ================= */
    @GetMapping("/user-management")
    public String manageUser(HttpSession session, Model model) {
        User staff = getStaff(session);
        model.addAttribute("user", staff);
        model.addAttribute("users", userService.getAllUsers());
        return "staff/user-management";
    }

    /* ================= PAGES ================= */
    @GetMapping("/staff-email")
    public String staffEmail(HttpSession session, Model model) {
        User staff = getStaff(session);
        model.addAttribute("user", staff);
        return "staff/staff-email";
    }

    @GetMapping("/staff-ip")
    public String staffIp(HttpSession session, Model model) {
        User staff = getStaff(session);
        model.addAttribute("user", staff);
        return "staff/staff-ip";
    }

    @GetMapping("/staff-url")
    public String staffUrl(HttpSession session, Model model) {
        User staff = getStaff(session);
        model.addAttribute("user", staff);
        return "staff/staff-url";
    }

    @DeleteMapping("/user/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email,
                                        HttpSession session) {

        getStaff(session);

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email không hợp lệ");
        }

        try {
            userService.deleteUser(email);
            return ResponseEntity.ok("Đã xóa user");

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi xóa user: " + e.getMessage());
        }
    }

    /* ================= SESSIONS ================= */
    @GetMapping({"/sessions", "/session-list", "/session/list", "/get-sessions"})
    @ResponseBody
    public List<Map<String, Object>> getSessions(HttpSession session) {
        getStaff(session);
        return graphService.getAllSessions();
    }

    /* ================= LOGOUT ================= */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) session.invalidate();
        return "redirect:/login";
    }

    /* ================= HELPER ================= */
    private User getStaff(HttpSession session) {
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (!"STAFF".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return user;
    }
}
