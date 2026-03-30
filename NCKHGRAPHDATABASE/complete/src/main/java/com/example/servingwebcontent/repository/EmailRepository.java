package com.example.servingwebcontent.repository;

import com.example.servingwebcontent.model.Email;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository chỉ phục vụ QUERY GRAPH
 * Không normalize
 * Không business logic
 */
public interface EmailRepository extends Neo4jRepository<Email, String> {

    // =========================
    // 1️⃣ Kiểm tra email tồn tại
    // =========================
    @Query("""
        MATCH (e:Email {email: $email})
        RETURN count(e) > 0
    """)
    boolean existsByEmail(String email);

    // =========================
    // 2️⃣ Lấy Email node
    // =========================
    Optional<Email> findByEmail(String email);

    // =================================================
    // 3️⃣ Email có chứa URL hay không
    // Email -[:CONTAINS_URL]-> URL
    // =================================================
    @Query("""
        MATCH (e:Email {email: $email})-[:CONTAINS_URL]->(:URL)
        RETURN count(*) > 0
    """)
    boolean hasURLs(String email);

    // =================================================
    // 4️⃣ Email liên quan URL giả mạo
    // Email → URL → IMPERSONATES
    // =================================================
    @Query("""
        MATCH (e:Email {email: $email})-[:CONTAINS_URL]->(u:URL)
        MATCH (u)-[:IMPERSONATES]->(:Domain)
        RETURN count(u) > 0
    """)
    boolean involvedInImpersonation(String email);

    // =================================================
    // 5️⃣ Email liên quan IP rủi ro
    // Flow:
    // Email -[:SENT_FROM_IP]-> IP
    // OR
    // Email -[:CONTAINS_URL]-> URL -[:HOSTED_ON]-> Domain -[:RESOLVES_TO]-> IP
    // =================================================
    @Query("""
        MATCH (e:Email {email: $email})
        OPTIONAL MATCH (e)-[:SENT_FROM_IP]->(ip1:IPAddress)
        OPTIONAL MATCH (e)-[:CONTAINS_URL]->(:URL)-[:HOSTED_ON]->(:Domain)-[:RESOLVES_TO]->(ip2:IPAddress)
        WITH collect(DISTINCT ip1) + collect(DISTINCT ip2) AS ips
        UNWIND ips AS ip
        WITH ip
        WHERE ip IS NOT NULL AND ip.status IN ['SUSPICIOUS','FAKE']
        RETURN count(ip) > 0
    """)
    boolean connectedToRiskyIP(String email);

    // =================================================
    // 6️⃣ Danh sách Email bị gắn cờ
    // =================================================
    @Query("""
        MATCH (e:Email)
        WHERE e.status IN ['SUSPICIOUS','FAKE']
        RETURN e
    """)
    List<Email> findFlaggedEmails();
}