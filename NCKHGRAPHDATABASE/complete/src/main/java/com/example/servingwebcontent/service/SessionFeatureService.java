package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.SessionFeatureVectorDTO;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class SessionFeatureService {

    private final Neo4jClient neo4j;

    public SessionFeatureService(Neo4jClient neo4j) {
        this.neo4j = neo4j;
    }

    public SessionFeatureVectorDTO extractFeatures(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return new SessionFeatureVectorDTO(0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        Map<String, Object> counts = neo4j.query("""
            MATCH (s:AnalysisSession {id:$sid})
            OPTIONAL MATCH (s)-[:HAS_EMAIL]->(e:Email)
            WITH s, count(DISTINCT e) AS numEmails
            OPTIONAL MATCH (s)-[:HAS_IP]->(ip:IPAddress)
            WITH s, numEmails, count(DISTINCT ip) AS numIps
            OPTIONAL MATCH (s)-[:HAS_URL]->(u:URL)
            WITH s, numEmails, numIps, count(DISTINCT u) AS numUrls
            OPTIONAL MATCH (s)-[:HAS_DOMAIN]->(d:Domain)
            RETURN numEmails,
                   numIps,
                   numUrls,
                   count(DISTINCT d) AS numDomains
        """)
        .bind(sessionId).to("sid")
        .fetch()
        .one()
        .orElse(Map.of());

        int numSharedIps = neo4j.query("""
            MATCH (s:AnalysisSession {id:$sid})-[:HAS_IP]->(ip:IPAddress)
            MATCH (other:AnalysisSession)-[:HAS_IP]->(ip)
            WHERE other.id <> $sid
            RETURN count(DISTINCT ip) AS c
        """)
        .bind(sessionId).to("sid")
        .fetchAs(Integer.class)
        .one()
        .orElse(0);

        int numRepeatedUrls = neo4j.query("""
            MATCH (s:AnalysisSession {id:$sid})-[:HAS_URL]->(u:URL)
            MATCH (other:AnalysisSession)-[:HAS_URL]->(u)
            WHERE other.id <> $sid
            RETURN count(DISTINCT u) AS c
        """)
        .bind(sessionId).to("sid")
        .fetchAs(Integer.class)
        .one()
        .orElse(0);

        Map<String, Object> riskCounts = neo4j.query("""
            MATCH (s:AnalysisSession {id:$sid})-[:HAS_EMAIL|HAS_IP|HAS_URL|HAS_DOMAIN|HAS_FILE|HAS_FILEHASH|HAS_VICTIM]->(n)
            WHERE coalesce(n.deleted,false) = false
            RETURN count(CASE WHEN coalesce(n.riskScore,0) >= 60 THEN 1 END) AS highRiskNodes,
                   count(CASE WHEN coalesce(n.riskScore,0) >= 30 AND coalesce(n.riskScore,0) < 60 THEN 1 END) AS mediumRiskNodes
        """)
        .bind(sessionId).to("sid")
        .fetch()
        .one()
        .orElse(Map.of());

        int numIndicators = neo4j.query("""
            MATCH (s:AnalysisSession {id:$sid})
            RETURN size(coalesce(s.indicators,[])) AS c
        """)
        .bind(sessionId).to("sid")
        .fetchAs(Integer.class)
        .one()
        .orElse(0);

        return new SessionFeatureVectorDTO(
                asInt(counts.get("numEmails")),
                asInt(counts.get("numIps")),
                asInt(counts.get("numUrls")),
                asInt(counts.get("numDomains")),
                numSharedIps,
                numRepeatedUrls,
                asInt(riskCounts.get("highRiskNodes")),
                asInt(riskCounts.get("mediumRiskNodes")),
                numIndicators
        );
    }

    public List<HistoricalSessionSample> loadHistoricalSamples(String excludedSessionId, int limit) {
        Collection<Map<String, Object>> rows = neo4j.query("""
            MATCH (s:AnalysisSession)
            WHERE s.id IS NOT NULL
              AND s.id <> $excluded
              AND coalesce(s.status,'') = 'DONE'
              AND s.hybridFinalScore IS NOT NULL
            RETURN s.id AS sessionId,
                   coalesce(s.hybridFinalScore, s.riskScore, 0) AS score,
                   coalesce(s.hybridVerdict, s.verdict, 'ALLOW') AS verdict,
                   coalesce(s.featureNumEmails,0) AS numEmails,
                   coalesce(s.featureNumIps,0) AS numIps,
                   coalesce(s.featureNumUrls,0) AS numUrls,
                   coalesce(s.featureNumDomains,0) AS numDomains,
                   coalesce(s.featureNumSharedIps,0) AS numSharedIps,
                   coalesce(s.featureNumRepeatedUrls,0) AS numRepeatedUrls,
                   coalesce(s.featureNumHighRiskNodes,0) AS numHighRiskNodes,
                   coalesce(s.featureNumMediumRiskNodes,0) AS numMediumRiskNodes,
                   coalesce(s.featureNumIndicators,0) AS numIndicators
            ORDER BY s.createdAt DESC
            LIMIT $limit
        """)
        .bind(excludedSessionId).to("excluded")
        .bind(limit).to("limit")
        .fetch()
        .all();

        List<HistoricalSessionSample> samples = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            SessionFeatureVectorDTO feature = new SessionFeatureVectorDTO(
                    asInt(row.get("numEmails")),
                    asInt(row.get("numIps")),
                    asInt(row.get("numUrls")),
                    asInt(row.get("numDomains")),
                    asInt(row.get("numSharedIps")),
                    asInt(row.get("numRepeatedUrls")),
                    asInt(row.get("numHighRiskNodes")),
                    asInt(row.get("numMediumRiskNodes")),
                    asInt(row.get("numIndicators"))
            );
            String verdict = row.get("verdict") == null ? "ALLOW" : row.get("verdict").toString();
            boolean fraud = "BLOCK".equalsIgnoreCase(verdict) || asInt(row.get("score")) >= 50;
            samples.add(new HistoricalSessionSample(
                    row.get("sessionId") == null ? null : row.get("sessionId").toString(),
                    feature,
                    fraud,
                    asDouble(row.get("score"))
            ));
        }
        return samples;
    }

    private int asInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private double asDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0.0;
    }

    public record HistoricalSessionSample(
            String sessionId,
            SessionFeatureVectorDTO features,
            boolean fraud,
            double score
    ) {}
}
