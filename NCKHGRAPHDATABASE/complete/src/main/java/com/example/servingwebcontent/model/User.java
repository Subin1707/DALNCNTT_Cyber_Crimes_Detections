package com.example.servingwebcontent.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.List;
import java.util.Objects;

/**
 * User hệ thống
 * - Dùng cho đăng nhập & phân quyền
 * - KHÔNG tham gia graph fraud (Email / IP / URL)
 */
@Node("User")
public class User {

    /**
     * Email dùng làm định danh duy nhất
     */
    @Id
    @Property("email")
    private String email;

    /**
     * Password đã được hash (hash ở Service)
     */
    @Property("password")
    private String password;

    /**
     * ADMIN | STAFF | CUSTOMER
     */
    @Property("role")
    private String role;

    /**
     * ACTIVE | DISABLED
     */
    @Property("status")
    private String status;

    // ================= CONSTRUCTOR =================

    public User() {
        this.role = "CUSTOMER";
        this.status = "ACTIVE";
    }

    public User(String email, String password, String role) {
        this();
        this.email = normalizeEmail(email);
        this.password = password;
        this.role = normalizeRole(role);
    }

    // ================= NORMALIZE =================

    private static String normalizeEmail(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        return s.isEmpty() ? null : s;
    }

    private static String normalizeRole(String raw) {
        if (raw == null) return "CUSTOMER";
        String s = raw.trim().toUpperCase();
        return List.of("ADMIN", "STAFF", "CUSTOMER").contains(s)
                ? s
                : "CUSTOMER";
    }

    private static String normalizeStatus(String raw) {
        if (raw == null) return "ACTIVE";
        String s = raw.trim().toUpperCase();
        return List.of("ACTIVE", "DISABLED").contains(s)
                ? s
                : "ACTIVE";
    }

    // ================= GET / SET =================

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalizeEmail(email);
    }

    public String getPassword() {
        return password;
    }

    /**
     * Không hash ở đây — hash tại Service
     */
    public void setPassword(String password) {
        this.password = (password == null) ? null : password.trim();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = normalizeRole(role);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = normalizeStatus(status);
    }

    // ================= EQUALS / HASH =================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}