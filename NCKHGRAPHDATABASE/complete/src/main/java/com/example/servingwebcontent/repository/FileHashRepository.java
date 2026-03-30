package com.example.servingwebcontent.repository;

import com.example.servingwebcontent.model.FileHash;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface FileHashRepository extends Neo4jRepository<FileHash, String> {

    // =========================
    // 1️⃣ Hash tồn tại
    // =========================
    @Query("""
        MATCH (h:FileHash {hash:$hash})
        RETURN count(h) > 0
    """)
    boolean existsByHash(String hash);


    // =========================
    // 2️⃣ Lấy hash node
    // =========================
    Optional<FileHash> findByHash(String hash);


    // =================================================
    // 3️⃣ Hash được file sử dụng
    // FileNode -HAS_HASH-> FileHash
    // =================================================
    @Query("""
        MATCH (:FileNode)-[:HAS_HASH]->(h:FileHash {hash:$hash})
        RETURN count(*) > 0
    """)
    boolean usedByFiles(String hash);


    // =================================================
    // 4️⃣ Hash thuộc malware
    // =================================================
    @Query("""
        MATCH (h:FileHash {hash:$hash})
        WHERE h.status IN ['SUSPICIOUS','FAKE']
        RETURN count(h) > 0
    """)
    boolean isMaliciousHash(String hash);


    // =================================================
    // 5️⃣ Hash malware được download từ URL
    // =================================================
    @Query("""
        MATCH (:URL)-[:DOWNLOADS]->(:FileNode)-[:HAS_HASH]->(h:FileHash {hash:$hash})
        RETURN count(*) > 0
    """)
    boolean downloadedFromURL(String hash);


    // =================================================
    // 6️⃣ Danh sách hash bị gắn cờ
    // =================================================
    @Query("""
        MATCH (h:FileHash)
        WHERE h.status IN ['SUSPICIOUS','FAKE']
        RETURN h
    """)
    List<FileHash> findFlaggedHashes();
}