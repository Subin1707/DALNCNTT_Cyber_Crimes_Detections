package com.example.servingwebcontent.repository;

import com.example.servingwebcontent.model.FileNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface FileNodeRepository extends Neo4jRepository<FileNode, String> {

    // =========================
    // 1️⃣ File tồn tại
    // =========================
    @Query("""
        MATCH (f:FileNode {fileName:$fileName})
        RETURN count(f) > 0
    """)
    boolean existsByFileName(String fileName);


    // =========================
    // 2️⃣ Lấy File node
    // =========================
    Optional<FileNode> findByFileName(String fileName);


    // =================================================
    // 3️⃣ File có hash hay không
    // FileNode -HAS_HASH-> FileHash
    // =================================================
    @Query("""
        MATCH (f:FileNode {fileName:$fileName})-[:HAS_HASH]->(:FileHash)
        RETURN count(*) > 0
    """)
    boolean hasHash(String fileName);


    // =================================================
    // 4️⃣ File được download từ URL
    // URL -DOWNLOADS-> FileNode
    // =================================================
    @Query("""
        MATCH (:URL)-[:DOWNLOADS]->(f:FileNode {fileName:$fileName})
        RETURN count(*) > 0
    """)
    boolean downloadedFromURL(String fileName);


    // =================================================
    // 5️⃣ File được download từ URL rủi ro
    // =================================================
    @Query("""
        MATCH (u:URL)-[:DOWNLOADS]->(f:FileNode {fileName:$fileName})
        WHERE u.status IN ['SUSPICIOUS','FAKE']
        RETURN count(u) > 0
    """)
    boolean downloadedFromRiskyURL(String fileName);


    // =================================================
    // 6️⃣ Danh sách File bị gắn cờ
    // =================================================
    @Query("""
        MATCH (f:FileNode)
        WHERE f.status IN ['SUSPICIOUS','FAKE']
        RETURN f
    """)
    List<FileNode> findFlaggedFiles();
}