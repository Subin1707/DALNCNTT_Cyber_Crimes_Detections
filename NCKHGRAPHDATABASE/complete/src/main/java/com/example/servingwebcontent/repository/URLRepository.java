package com.example.servingwebcontent.repository;

import com.example.servingwebcontent.model.URL;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository chỉ phục vụ QUERY GRAPH (URL-based)
 * Không normalize
 * Không business logic
 *
 * Graph schema:
 * Email -[:CONTAINS_URL]-> URL
 * URL -[:HOSTED_ON]-> Domain
 * Domain -[:RESOLVES_TO]-> IPAddress
 * URL -[:IMPERSONATES]-> Domain
 */
public interface URLRepository extends Neo4jRepository<URL, String> {

    // =========================
    // 1️⃣ Kiểm tra URL tồn tại
    // =========================
    @Query("""
        MATCH (u:URL {url: $url})
        RETURN count(u) > 0
    """)
    boolean existsByUrl(String url);

    // =========================
    // 2️⃣ Lấy URL node
    // =========================
    Optional<URL> findByUrl(String url);

    // =================================================
    // 3️⃣ URL có xuất hiện trong Email hay không
    // Email -[:CONTAINS_URL]-> URL
    // =================================================
    @Query("""
        MATCH (:Email)-[:CONTAINS_URL]->(u:URL {url: $url})
        RETURN count(*) > 0
    """)
    boolean appearsInEmail(String url);

    // =================================================
    // 4️⃣ URL có được host trên Domain không
    // URL -[:HOSTED_ON]-> Domain
    // =================================================
    @Query("""
        MATCH (u:URL {url: $url})-[:HOSTED_ON]->(:Domain)
        RETURN count(*) > 0
    """)
    boolean isHosted(String url);

    // =================================================
    // 5️⃣ URL giả mạo brand/domain
    // URL -[:IMPERSONATES]-> Domain
    // =================================================
    @Query("""
        MATCH (u:URL {url: $url})-[:IMPERSONATES]->(:Domain)
        RETURN count(u) > 0
    """)
    boolean isImpersonating(String url);

    // =================================================
    // 6️⃣ URL nằm trên domain trỏ tới IP rủi ro
    // URL → Domain → IP
    // =================================================
    @Query("""
        MATCH (u:URL {url: $url})-[:HOSTED_ON]->(:Domain)-[:RESOLVES_TO]->(ip:IPAddress)
        WHERE ip.status IN ['SUSPICIOUS','FAKE']
        RETURN count(ip) > 0
    """)
    boolean hostedOnRiskyIP(String url);

    // =================================================
    // 7️⃣ URL liên quan Email rủi ro
    // Email -CONTAINS_URL-> URL
    // =================================================
    @Query("""
        MATCH (e:Email)-[:CONTAINS_URL]->(u:URL {url: $url})
        WHERE e.status IN ['SUSPICIOUS','FAKE']
        RETURN count(e) > 0
    """)
    boolean connectedToRiskyEmail(String url);

    // =================================================
    // 8️⃣ Danh sách URL rủi ro cao
    // =================================================
    @Query("""
        MATCH (u:URL)
        WHERE u.status IN ['SUSPICIOUS','FAKE']
        RETURN u
    """)
    List<URL> findHighRiskURLs();
}