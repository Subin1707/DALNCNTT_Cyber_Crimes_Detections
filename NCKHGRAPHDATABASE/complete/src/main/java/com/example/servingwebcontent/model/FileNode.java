package com.example.servingwebcontent.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Node File trong graph fraud
 * Đại diện file được download từ URL
 */
@Node("File")
public class FileNode {

    /**
     * File hash dùng làm định danh
     */
    @Id
    @Property("hash")
    private String hash;

    /**
     * Tên file
     */
    @Property("fileName")
    private String fileName;

    /**
     * Loại file
     */
    @Property("fileType")
    private String fileType;

    /**
     * Kích thước file
     */
    @Property("size")
    private long size;

    /**
     * Risk score
     */
    @Property("baseRisk")
    private int baseRisk;

    /**
     * Indicators
     */
    @Property("indicators")
    private List<String> indicators;

    // ================= GRAPH RELATIONSHIP =================

    /**
     * URL download file này
     *
     * URL --DOWNLOADS--> File
     */
    @Relationship(type = "DOWNLOADS", direction = Relationship.Direction.INCOMING)
    private List<URL> sourceUrls = new ArrayList<>();

    // ===== CONSTRUCTOR =====

    public FileNode() {
        this.baseRisk = 0;
        this.indicators = new ArrayList<>();
    }

    public FileNode(String hash) {
        this();
        setHash(hash);
    }

    // ===== NORMALIZE =====

    private static String normalizeHash(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        return s.isEmpty() ? null : s;
    }

    private static String normalizeFileName(String raw) {
        if (raw == null) return null;
        return raw.trim();
    }

    // ===== GET / SET =====

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = normalizeHash(hash);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = normalizeFileName(fileName);
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType == null ? null : fileType.toLowerCase();
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = Math.max(0, size);
    }

    public int getBaseRisk() {
        return baseRisk;
    }

    public void setBaseRisk(int baseRisk) {
        this.baseRisk = Math.max(0, Math.min(100, baseRisk));
    }

    public List<String> getIndicators() {
        return indicators;
    }

    public void setIndicators(List<String> indicators) {
        this.indicators = indicators == null ? new ArrayList<>() : indicators;
    }

    public List<URL> getSourceUrls() {
        return sourceUrls;
    }

    public void setSourceUrls(List<URL> sourceUrls) {
        this.sourceUrls = sourceUrls;
    }

    // ===== UTILS =====

    public void addIndicator(String indicator) {

        if (indicator == null) return;

        if (this.indicators == null)
            this.indicators = new ArrayList<>();

        String ind = indicator.trim().toUpperCase();

        if (!this.indicators.contains(ind))
            this.indicators.add(ind);
    }

    public void increaseRisk(int value) {
        setBaseRisk(this.baseRisk + value);
    }

    // ===== EQUALS / HASH =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileNode)) return false;
        FileNode fileNode = (FileNode) o;
        return Objects.equals(hash, fileNode.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}