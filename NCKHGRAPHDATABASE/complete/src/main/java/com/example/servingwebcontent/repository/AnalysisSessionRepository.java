package com.example.servingwebcontent.repository;

import com.example.servingwebcontent.model.AnalysisSession;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý AnalysisSession
 * - Mỗi session = 1 lần phân tích fraud
 * - Lưu metadata: file upload, thời gian, user
 */
public interface AnalysisSessionRepository extends Neo4jRepository<AnalysisSession, String> {

    // ==========================
    // 1️⃣ LỊCH SỬ PHÂN TÍCH
    // ==========================

    /** Lấy tất cả session (mới nhất trước) */
    List<AnalysisSession> findAllByOrderByCreatedAtDesc();

    /** Session mới nhất hệ thống */
    Optional<AnalysisSession> findTopByOrderByCreatedAtDesc();

    // ==========================
    // 2️⃣ THEO USER
    // ==========================

    /** Lịch sử session của user */
    List<AnalysisSession> findAllByCreatedByOrderByCreatedAtDesc(String createdBy);

    /** Session mới nhất của user */
    Optional<AnalysisSession> findTopByCreatedByOrderByCreatedAtDesc(String createdBy);

    // ==========================
    // 3️⃣ THEO TRẠNG THÁI
    // ==========================

    /** Các session đang xử lý */
    List<AnalysisSession> findByStatus(String status);

    // ==========================
    // 4️⃣ TIỆN ÍCH
    // ==========================

    boolean existsById(String id);

    Optional<AnalysisSession> findById(String id);
}