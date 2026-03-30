package com.example.servingwebcontent.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * AnalysisSession
 * ----------------
 * Node ngữ cảnh cho MỘT lần phân tích gian lận
 *
 * - Gom tất cả node + relation được tạo trong 1 lần analyze
 * - KHÔNG mang risk
 * - KHÔNG verdict
 */
@Node("AnalysisSession")
public class AnalysisSession {

    /**
     * Session ID – định danh duy nhất
     */
    @Id
    @Property("id")
    private String id;

    /**
     * Tên file upload (nếu có)
     */
    @Property("fileName")
    private String fileName;

    /**
     * Thời điểm user nhập dữ liệu (epoch millis)
     */
    @Property("inputTime")
    private long inputTime;

    /**
     * Thời điểm tạo session trong DB (epoch millis)
     */
    @Property("createdAt")
    private long createdAt;

    /**
     * User tạo session (metadata)
     * Không dùng cho logic phân tích
     */
    @Property("createdBy")
    private String createdBy;

    /**
     * Tổng số dòng dữ liệu được phân tích
     */
    @Property("totalRows")
    private int totalRows;

    /**
     * Trạng thái batch
     * PROCESSING | DONE | FAILED
     */
    @Property("status")
    private String status;

    // ================= CONSTRUCTOR =================

    public AnalysisSession() {
        this.id = generateIdIfMissing(null);
        this.createdAt = System.currentTimeMillis();
        this.inputTime = System.currentTimeMillis();
        this.status = "PROCESSING";
        this.createdBy = "SYSTEM";
        this.totalRows = 0;
    }

    public AnalysisSession(String id,
                           String fileName,
                           long inputTime,
                           String createdBy,
                           int totalRows,
                           String status) {

        this.id = generateIdIfMissing(id);
        this.fileName = normalize(fileName);

        this.inputTime = (inputTime > 0) ? inputTime : System.currentTimeMillis();
        this.createdAt = System.currentTimeMillis();

        this.createdBy = normalizeCreatedBy(createdBy);
        this.totalRows = Math.max(0, totalRows);
        this.status = normalizeStatus(status);
    }

    // ================= NORMALIZE =================

    private static String generateIdIfMissing(String raw) {
        String s = normalize(raw);
        return (s == null) ? UUID.randomUUID().toString() : s;
    }

    private static String normalize(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        return s.isEmpty() ? null : s;
    }

    private static String normalizeCreatedBy(String raw) {
        String s = normalize(raw);
        return (s == null) ? "SYSTEM" : s;
    }

    private static String normalizeStatus(String raw) {
        String s = normalize(raw);
        if (s == null) return "PROCESSING";

        s = s.toUpperCase();
        Set<String> allowed = Set.of("PROCESSING", "DONE", "FAILED");

        return allowed.contains(s) ? s : "PROCESSING";
    }

    // ================= GET / SET =================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = generateIdIfMissing(id);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = normalize(fileName);
    }

    public long getInputTime() {
        return inputTime;
    }

    public void setInputTime(long inputTime) {
        this.inputTime = (inputTime > 0) ? inputTime : System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = (createdAt > 0) ? createdAt : System.currentTimeMillis();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = normalizeCreatedBy(createdBy);
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = Math.max(0, totalRows);
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
        if (!(o instanceof AnalysisSession)) return false;
        AnalysisSession that = (AnalysisSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}