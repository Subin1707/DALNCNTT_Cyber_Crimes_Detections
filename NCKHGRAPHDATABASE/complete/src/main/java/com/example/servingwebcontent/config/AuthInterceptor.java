package com.example.servingwebcontent.config;

import com.example.servingwebcontent.model.User;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }

        String uri = request.getRequestURI();
        String role = user.getRole();

        if (uri.startsWith("/dashboard/admin") && !"ADMIN".equals(role)) {
            response.sendRedirect("/dashboard");
            return false;
        }
        if (uri.startsWith("/dashboard/staff") && !"STAFF".equals(role)) {
            response.sendRedirect("/dashboard");
            return false;
        }
        if (uri.startsWith("/dashboard/customer") && !"CUSTOMER".equals(role)) {
            response.sendRedirect("/dashboard");
            return false;
        }

        return true;
    }
}