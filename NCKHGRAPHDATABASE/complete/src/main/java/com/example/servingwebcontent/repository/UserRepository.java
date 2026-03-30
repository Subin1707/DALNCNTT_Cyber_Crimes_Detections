package com.example.servingwebcontent.repository;

import com.example.servingwebcontent.model.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

/**
 * Repository quản lý User hệ thống
 * - Không normalize trong Cypher
 * - Email đã được normalize tại model/service
 */
public interface UserRepository extends Neo4jRepository<User, String> {

    // =========================
    // 1️⃣ Lấy User theo email
    // =========================
    Optional<User> findByEmail(String email);

    // =========================
    // 2️⃣ Kiểm tra User tồn tại
    // =========================
    boolean existsByEmail(String email);
}