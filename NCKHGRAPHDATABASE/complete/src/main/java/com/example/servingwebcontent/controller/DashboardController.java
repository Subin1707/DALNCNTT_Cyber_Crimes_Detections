package com.example.servingwebcontent.controller;

import com.example.servingwebcontent.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        // 🔒 HttpSession trong Spring MVC KHÔNG bao giờ null
        // → chỉ cần check user
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // ✅ truyền user cho view
        model.addAttribute("user", user);

        // ✅ normalize role
        String role = user.getRole() == null
                ? ""
                : user.getRole().trim().toUpperCase();

        // ✅ điều hướng theo role
        switch (role) {
            case "ADMIN":
                return "dashboard/admin";
            case "STAFF":
                return "dashboard/staff";
            case "CUSTOMER":
                return "dashboard/customer";
            default:
                // ⚠️ role không hợp lệ → clear session
                session.invalidate();
                return "redirect:/login";
        }
    }
}