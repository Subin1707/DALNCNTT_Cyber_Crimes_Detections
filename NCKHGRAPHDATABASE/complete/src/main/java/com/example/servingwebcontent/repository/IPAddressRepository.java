package com.example.servingwebcontent.repository;

import com.example.servingwebcontent.model.IPAddress;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository chỉ phục vụ QUERY GRAPH (IP-based)
 * Không normalize
 * Không business logic
 */
public interface IPAddressRepository extends Neo4jRepository<IPAddress, String> {

    // =========================
    // 1️⃣ Kiểm tra IP tồn tại
    // =========================
    @Query("""
        MATCH (ip:IPAddress {ip: $ip})
        RETURN count(ip) > 0
    """)
    boolean existsByIp(String ip);

    // =========================
    // 2️⃣ Lấy IP node
    // =========================
    Optional<IPAddress> findByIp(String ip);

    // =================================================
    // 3️⃣ IP có được dùng làm nguồn gửi email không
    // Email -[:SENT_FROM_IP]-> IP
    // =================================================
    @Query("""
        MATCH (:Email)-[:SENT_FROM_IP]->(ip:IPAddress {ip: $ip})
        RETURN count(*) > 0
    """)
    boolean usedToSendEmail(String ip);

    // =================================================
    // 4️⃣ IP host domain có URL phishing
    // URL -HOSTED_ON-> Domain -RESOLVES_TO-> IP
    // =================================================
    @Query("""
        MATCH (u:URL)-[:HOSTED_ON]->(:Domain)-[:RESOLVES_TO]->(ip:IPAddress {ip: $ip})
        MATCH (u)-[:IMPERSONATES]->(:Domain)
        RETURN count(u) > 0
    """)
    boolean hostsImpersonationURL(String ip);

    // =================================================
    // 5️⃣ Danh sách IP bị gắn cờ
    // =================================================
    @Query("""
        MATCH (ip:IPAddress)
        WHERE ip.status IN ['SUSPICIOUS','FAKE']
        RETURN ip
    """)
    List<IPAddress> findFlaggedIPs();

    // =================================================
    // 6️⃣ IP phục vụ nhiều domain
    // (hạ tầng scam / shared hosting)
    // =================================================
    @Query("""
        MATCH (:URL)-[:HOSTED_ON]->(d:Domain)-[:RESOLVES_TO]->(ip:IPAddress)
        WITH ip, count(DISTINCT d) AS domainCount
        WHERE domainCount >= 3
        RETURN ip
    """)
    List<IPAddress> findSharedIPs();

    // =================================================
    // 7️⃣ IP liên quan Email rủi ro
    // =================================================
    @Query("""
        MATCH (e:Email)-[:SENT_FROM_IP]->(ip:IPAddress {ip: $ip})
        WHERE e.status IN ['SUSPICIOUS','FAKE']
        RETURN count(e) > 0
    """)
    boolean connectedToRiskyEmail(String ip);
}