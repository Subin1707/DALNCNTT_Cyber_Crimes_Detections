package com.example.servingwebcontent.controller;

import com.example.servingwebcontent.model.User;
import com.example.servingwebcontent.service.AlgorithmComparisonService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AlgorithmComparisonController {

    private final AlgorithmComparisonService comparisonService;

    public AlgorithmComparisonController(AlgorithmComparisonService comparisonService) {
        this.comparisonService = comparisonService;
    }

    @GetMapping("/algorithm-comparison")
    public String algorithmComparison(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        model.addAttribute("role", user != null ? user.getRole() : "GUEST");
        return "algorithm-comparison";
    }

    @ResponseBody
    @GetMapping("/api/algorithm-comparison")
    public ResponseEntity<AlgorithmComparisonService.ComparisonReport> getComparison(
            @RequestParam(defaultValue = "5") int k,
            @RequestParam(defaultValue = "30") int repeatCount) {
        return ResponseEntity.ok(comparisonService.compareAlgorithms(k, repeatCount));
    }
}
