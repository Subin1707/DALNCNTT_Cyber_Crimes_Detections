package com.example.servingwebcontent.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Node FileHash
 * Dùng để lưu hash malware
 */
@Node("FileHash")
public class FileHash {

    @Id
    @Property("hash")
    private String hash;

    /**
     * md5 / sha1 / sha256
     */
    @Property("algorithm")
    private String algorithm;

    /**
     * malicious score (0–1)
     */
    @Property("maliciousScore")
    private double maliciousScore;

    // ================= GRAPH RELATIONSHIP =================

    /**
     * File nào có hash này
     *
     * File --HAS_HASH--> FileHash
     */
    @Relationship(type = "HAS_HASH", direction = Relationship.Direction.INCOMING)
    private List<FileNode> files = new ArrayList<>();

    // ===== CONSTRUCTOR =====

    public FileHash() {}

    public FileHash(String hash) {
        setHash(hash);
    }

    // ===== NORMALIZE =====

    private static String normalizeHash(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        return s.isEmpty() ? null : s;
    }

    // ===== GET / SET =====

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = normalizeHash(hash);
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm == null ? null : algorithm.toLowerCase();
    }

    public double getMaliciousScore() {
        return maliciousScore;
    }

    public void setMaliciousScore(double maliciousScore) {
        this.maliciousScore = Math.max(0, Math.min(1, maliciousScore));
    }

    public List<FileNode> getFiles() {
        return files;
    }

    public void setFiles(List<FileNode> files) {
        this.files = files;
    }

    // ===== UTILS =====

    public void addFile(FileNode file) {

        if (file == null) return;

        if (this.files == null)
            this.files = new ArrayList<>();

        if (!this.files.contains(file))
            this.files.add(file);
    }

    // ===== EQUALS / HASH =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileHash)) return false;
        FileHash fileHash = (FileHash) o;
        return Objects.equals(hash, fileHash.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}