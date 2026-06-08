package com.example.servingwebcontent.controller;

import com.example.servingwebcontent.model.User;
import com.example.servingwebcontent.service.PacketCaptureService;
import com.example.servingwebcontent.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;
    private final PacketCaptureService packetCaptureService;

    public AuthController(UserService userService, PacketCaptureService packetCaptureService) {
        this.userService = userService;
        this.packetCaptureService = packetCaptureService;
    }

    /* ===================== LOGIN ===================== */

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            HttpSession session,
            Model model
    ) {

        // 🔒 validate input
        if (email == null || email.isBlank() ||
            password == null || password.isBlank()) {

            model.addAttribute("error", "Vui lòng nhập đầy đủ Email và Password");
            return "login";
        }

        email = email.trim().toLowerCase();
        password = password.trim();

        User user;
        try {
            user = userService.authenticate(email, password);
        } catch (RuntimeException e) {
            model.addAttribute("error", databaseConnectionMessage(e));
            return "login";
        }

        if (user == null) {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng");
            return "login";
        }

        // ✅ lưu session
        session.setAttribute("user", user);

        // ✅ normalize role
        String role = user.getRole() == null
                ? ""
                : user.getRole().trim().toUpperCase();

        // ✅ điều hướng theo role
        switch (role) {
            case "ADMIN":
                packetCaptureService.deactivateCustomerCapture(session.getId());
                return "redirect:/admin";
            case "STAFF":
                packetCaptureService.deactivateCustomerCapture(session.getId());
                return "redirect:/staff";
            case "CUSTOMER":
                packetCaptureService.activateCustomerCapture(user.getEmail(), session.getId());
                return "redirect:/customer";
            default:
                return "redirect:/dashboard";
        }
    }

    /* ===================== REGISTER ===================== */

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            Model model
    ) {

        if (email == null || email.isBlank() ||
            password == null || password.isBlank()) {

            model.addAttribute("error", "Vui lòng nhập email và mật khẩu");
            return "register";
        }

        email = email.trim().toLowerCase();
        password = password.trim();

        try {
            // ✅ đúng với UserService hiện tại
            userService.registerCustomer(email, password);

            model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "login";

        } catch (RuntimeException e) {
            model.addAttribute("error", databaseConnectionMessage(e));
            return "register";
        }
    }

    private String databaseConnectionMessage(RuntimeException e) {
        StringBuilder message = new StringBuilder(e.getMessage() == null ? "" : e.getMessage());
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause.getMessage() != null) {
                message.append(' ').append(cause.getMessage());
            }
            cause = cause.getCause();
        }

        String combinedMessage = message.toString();
        if (combinedMessage.contains("Could not open a new Neo4j session")
                || combinedMessage.contains("Driver execution failed")
                || combinedMessage.contains("UnknownHostException")
                || combinedMessage.contains("No such host")) {
            return "Không kết nối được Neo4j. Vui lòng kiểm tra mạng, host Neo4j Aura hoặc cấu hình spring.neo4j.uri trong application.properties.";
        }

        return e.getMessage();
    }

    /* ===================== LOGOUT ===================== */

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            packetCaptureService.deactivateCustomerCapture(session.getId());
            session.invalidate();
        }
        return "redirect:/login";
    }
}
