package com.example.servingwebcontent.controller;

import com.example.servingwebcontent.model.User;
import com.example.servingwebcontent.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
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

        User user = userService.authenticate(email, password);
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
                return "redirect:/admin";
            case "STAFF":
                return "redirect:/staff";
            case "CUSTOMER":
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
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    /* ===================== LOGOUT ===================== */

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
}