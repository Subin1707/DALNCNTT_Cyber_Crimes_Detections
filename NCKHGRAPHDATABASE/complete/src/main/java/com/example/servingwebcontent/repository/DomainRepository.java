package com.example.servingwebcontent.repository;

import com.example.servingwebcontent.model.Domain;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface DomainRepository extends Neo4jRepository<Domain, String> {

    // =========================
    // 1️⃣ Domain tồn tại
    // =========================
    @Query("""
        MATCH (d:Domain {domain:$domain})
        RETURN count(d) > 0
    """)
    boolean existsByDomain(String domain);


    // =========================
    // 2️⃣ Lấy Domain node
    // =========================
    Optional<Domain> findByDomain(String domain);


    // =================================================
    // 3️⃣ Domain host URL
    // URL -HOSTED_ON-> Domain
    // =================================================
    @Query("""
        MATCH (:URL)-[:HOSTED_ON]->(d:Domain {domain:$domain})
        RETURN count(*) > 0
    """)
    boolean hostsURLs(String domain);


    // =================================================
    // 4️⃣ Domain host URL rủi ro
    // =================================================
    @Query("""
        MATCH (u:URL)-[:HOSTED_ON]->(d:Domain {domain:$domain})
        WHERE u.status IN ['SUSPICIOUS','FAKE']
        RETURN count(u) > 0
    """)
    boolean hostsRiskyURLs(String domain);


    // =================================================
    // 5️⃣ Domain resolve tới IP rủi ro
    // Domain -RESOLVES_TO-> IP
    // =================================================
    @Query("""
        MATCH (d:Domain {domain:$domain})-[:RESOLVES_TO]->(ip:IPAddress)
        WHERE ip.status IN ['SUSPICIOUS','FAKE']
        RETURN count(ip) > 0
    """)
    boolean resolvesToRiskyIP(String domain);


    // =================================================
    // 6️⃣ Danh sách Domain bị gắn cờ
    // =================================================
    @Query("""
        MATCH (d:Domain)
        WHERE d.status IN ['SUSPICIOUS','FAKE']
        RETURN d
    """)
    List<Domain> findFlaggedDomains();
}