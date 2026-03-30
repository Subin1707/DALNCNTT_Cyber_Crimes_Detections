package com.example.servingwebcontent.service;

import com.example.servingwebcontent.model.User;
import com.example.servingwebcontent.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    /* ======================
       HELPERS
    ====================== */

    private String normalizeEmail(String email) {
        if (email == null) return null;
        String e = email.trim().toLowerCase();
        return e.isBlank() ? null : e;
    }

    private String normalizeRole(String role) {
        if (role == null) return "";
        return role.trim().toUpperCase();
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new RuntimeException("Mật khẩu không hợp lệ");
        }
        if (password.length() < 6) {
            throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự");
        }
    }

    /* ======================
       LOGIN
    ====================== */

    public User authenticate(String email, String rawPassword) {
        String e = normalizeEmail(email);
        if (e == null || rawPassword == null || rawPassword.isBlank()) {
            return null;
        }

        return repo.findByEmail(e)
                .filter(u ->
                        u.getPassword() != null &&
                        encoder.matches(rawPassword, u.getPassword())
                )
                .orElse(null);
    }

    /* ======================
       CUSTOMER REGISTER
    ====================== */

    public void registerCustomer(String email, String password) {
        String e = normalizeEmail(email);
        if (e == null) {
            throw new RuntimeException("Email không hợp lệ");
        }

        validatePassword(password);

        if (repo.existsByEmail(e)) {
            throw new RuntimeException("Email đã tồn tại");
        }

        repo.save(new User(
                e,
                encoder.encode(password),
                "CUSTOMER"
        ));
    }

    /* ======================
       ADMIN CREATE STAFF
    ====================== */

    public void createStaff(String email, String password, User admin) {
        if (admin == null || !"ADMIN".equals(normalizeRole(admin.getRole()))) {
            throw new RuntimeException("Không có quyền");
        }

        String e = normalizeEmail(email);
        if (e == null) {
            throw new RuntimeException("Email không hợp lệ");
        }

        validatePassword(password);

        if (repo.existsByEmail(e)) {
            throw new RuntimeException("Email đã tồn tại");
        }

        repo.save(new User(
                e,
                encoder.encode(password),
                "STAFF"
        ));
    }

    /* ======================
       GET ALL USERS
    ====================== */

    public List<User> getAllUsers() {
        return repo.findAll();
    }

    /* ======================
       DELETE USER
    ====================== */

    public void deleteUser(String email) {
        String e = normalizeEmail(email);
        if (e == null) {
            throw new RuntimeException("Email không hợp lệ");
        }

        if (!repo.existsByEmail(e)) {
            throw new RuntimeException("User không tồn tại");
        }

        // Prevent deleting ADMIN accounts
        var u = repo.findByEmail(e).orElseThrow(() -> new RuntimeException("User không tồn tại"));
        if ("ADMIN".equalsIgnoreCase(u.getRole())) {
            throw new RuntimeException("Không thể xóa tài khoản ADMIN");
        }

        repo.deleteById(e);
    }

    /* ======================
       UPDATE USER (ADMIN)
    ====================== */

    public User updateUser(String email, String newRole, String newStatus, String newPassword) {
        String e = normalizeEmail(email);
        if (e == null) throw new RuntimeException("Email không hợp lệ");

        User u = repo.findByEmail(e).orElseThrow(() -> new RuntimeException("User không tồn tại"));

        if ("ADMIN".equalsIgnoreCase(u.getRole())) {
            throw new RuntimeException("Không thể chỉnh sửa tài khoản ADMIN");
        }

        if (newRole != null && !newRole.isBlank()) {
            u.setRole(newRole);
        }

        if (newStatus != null && !newStatus.isBlank()) {
            u.setStatus(newStatus);
        }

        if (newPassword != null && !newPassword.isBlank()) {
            validatePassword(newPassword);
            u.setPassword(encoder.encode(newPassword));
        }

        return repo.save(u);
    }
}