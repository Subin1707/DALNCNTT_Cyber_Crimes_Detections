package com.example.servingwebcontent.repository;

import com.example.servingwebcontent.model.VictimAccount;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.Optional;

public interface VictimAccountRepository extends Neo4jRepository<VictimAccount, String> {

    // =========================
    // 1️⃣ Account tồn tại
    // =========================
    @Query("""
        MATCH (v:VictimAccount {username:$username})
        RETURN count(v) > 0
    """)
    boolean existsByUsername(String username);


    // =========================
    // 2️⃣ Lấy account
    // =========================
    Optional<VictimAccount> findByUsername(String username);


    // =================================================
    // 3️⃣ Account nhận email
    // VictimAccount -RECEIVED-> Email
    // =================================================
    @Query("""
        MATCH (v:VictimAccount {username:$username})-[:RECEIVED]->(:Email)
        RETURN count(*) > 0
    """)
    boolean receivedEmails(String username);


    // =================================================
    // 4️⃣ Account nhận email phishing
    // =================================================
    @Query("""
        MATCH (v:VictimAccount {username:$username})-[:RECEIVED]->(e:Email)
        WHERE e.status IN ['SUSPICIOUS','FAKE']
        RETURN count(e) > 0
    """)
    boolean targetedByPhishing(String username);


    // =================================================
    // 5️⃣ Danh sách account bị compromise
    // =================================================
    @Query("""
        MATCH (v:VictimAccount)
        WHERE v.status = 'COMPROMISED'
        RETURN v
    """)
    List<VictimAccount> findCompromisedAccounts();
}