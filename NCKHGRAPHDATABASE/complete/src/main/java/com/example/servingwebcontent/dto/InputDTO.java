package com.example.servingwebcontent.dto;

/**
 * InputDTO
 *
 * Dùng cho authentication / user management
 * KHÔNG dùng cho Fraud Analysis
 */
public class InputDTO {

    private String email;
    private String username;
    private String password;

    /**
     * ADMIN | STAFF | CUSTOMER
     */
    private String role;

    public InputDTO() {}

    public InputDTO(String username,
                    String email,
                    String password,
                    String role) {

        this.username = normalize(username);
        this.email = normalizeEmail(email);
        this.password = password;
        this.role = normalizeRole(role);
    }

    /* ================= GET / SET ================= */

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalizeEmail(email);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = normalize(username);
    }

    public String getPassword() {
        return password;
    }

    /**
     * Không xử lý password ở DTO
     * Service sẽ validate + hash
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = normalizeRole(role);
    }

    /* ================= HELPERS ================= */

    private static String normalize(String raw) {

        if (raw == null) return null;

        String s = raw.trim();

        return s.isEmpty() ? null : s;
    }

    private static String normalizeEmail(String raw) {

        String s = normalize(raw);

        return s == null ? null : s.toLowerCase();
    }

    private static String normalizeRole(String raw) {

        String r = normalize(raw);

        if (r == null) return "CUSTOMER";

        return switch (r.toUpperCase()) {

            case "ADMIN" -> "ADMIN";

            case "STAFF" -> "STAFF";

            default -> "CUSTOMER";
        };
    }
}