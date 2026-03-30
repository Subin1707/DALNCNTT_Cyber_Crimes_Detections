package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.GraphLinkDTO;
import com.example.servingwebcontent.dto.GraphNodeDTO;
import com.example.servingwebcontent.dto.GraphResponseDTO;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class GraphQueryService {

    private final Neo4jClient neo4j;

    public GraphQueryService(Neo4jClient neo4j) {
        this.neo4j = neo4j;
    }

    /* ===================================================== */
    /* PUBLIC API                                            */
    /* ===================================================== */

    public GraphResponseDTO getGraph() {
        return getLatestSessionGraph();
    }

    /**
     * Return a graph containing all sessions and all entity nodes and relationships.
     * This is used for the "ALL" view in admin UI.
     */
    public GraphResponseDTO getGraphAll() {

        Map<String, GraphNodeDTO> nodeMap = new LinkedHashMap<>();
        Set<String> linkKeySet = new LinkedHashSet<>();
        List<GraphLinkDTO> links = new ArrayList<>();

        // ---------- SESSION NODES ----------
        neo4j.query("""
            MATCH (s:AnalysisSession)
            RETURN elementId(s) AS id,
                   'AnalysisSession' AS type,
                   coalesce(s.fileName,'SESSION') AS value,
                   coalesce(s.status,'valid') AS status,
                   coalesce(s.riskLevel,'low') AS riskLevel,
                   coalesce(s.riskScore,0) AS riskScore,
                   coalesce(s.verdict,'AN TOÀN') AS verdict,
                   coalesce(s.indicators,[]) AS indicators,
                   '' AS source,
                   s.id AS sessionId
            ORDER BY s.createdAt DESC
        """)
        .fetch().all()
        .forEach(m -> {
            GraphNodeDTO n = mapNode(m);
            nodeMap.put(n.getId(), n);
        });

        // ---------- ENTITY NODES (Email/IP/URL/Domain/File/FileHash/VictimAccount) ----------
        neo4j.query("""
            MATCH (n)
            WHERE labels(n)[0] IN ['Email','IPAddress','URL','Domain','File','FileHash','VictimAccount']
              AND coalesce(n.deleted,false) = false
            RETURN elementId(n) AS id,
                   labels(n)[0] AS type,
                   coalesce(n.email,n.ip,n.url,n.domain,n.fileName,n.hash,n.username,n.account) AS value,
                   coalesce(n.status,'valid') AS status,
                   coalesce(n.riskLevel,'low') AS riskLevel,
                   coalesce(n.riskScore,0) AS riskScore,
                   coalesce(n.verdict,'AN TOÀN') AS verdict,
                   coalesce(n.indicators,[]) AS indicators,
                   coalesce(n.source,'') AS source,
                   '' AS sessionId
        """)
        .fetch().all()
        .forEach(m -> {
            GraphNodeDTO n = mapNode(m);
            nodeMap.put(n.getId(), n);
        });

        // ---------- HAS_* LINKS (session -> entity) ----------
        neo4j.query("""
            MATCH (s:AnalysisSession)-[r:HAS_EMAIL|HAS_IP|HAS_URL|HAS_DOMAIN|HAS_FILE|HAS_FILEHASH|HAS_VICTIM]->(n)
            WHERE coalesce(n.deleted,false) = false
            RETURN elementId(s) AS source,
                   elementId(n) AS target,
                   type(r) AS type
        """)
        .fetch().all()
        .forEach(m -> addLink(m, links, linkKeySet));

        // ---------- INTER-ENTITY LINKS (SENT_FROM_IP / CONTAINS_URL / HOSTED_ON / EXTRA) ----------
        neo4j.query("""
            MATCH (a)-[r:SENT_FROM_IP|CONTAINS_URL|HOSTED_ON|HOSTED_ON_DOMAIN|RESOLVES_TO|DOWNLOADS|HAS_HASH|RECEIVED|CONNECTS_TO]->(b)
            WHERE coalesce(a.deleted,false) = false
              AND coalesce(b.deleted,false) = false
            RETURN DISTINCT elementId(a) AS source,
                            elementId(b) AS target,
                            type(r) AS type
        """)
        .fetch().all()
        .forEach(m -> addLink(m, links, linkKeySet));

        return new GraphResponseDTO(
                new ArrayList<>(nodeMap.values()),
                links
        );
    }

    /* ===================================================== */
    /* SESSION LIST                                          */
    /* ===================================================== */

    public List<Map<String, Object>> getAllSessions() {
        return new ArrayList<>(neo4j.query("""
            MATCH (s:AnalysisSession)
            RETURN s.id AS id,
                   s.createdAt AS createdAt,
                   s.fileName AS fileName,
                   s.status AS status
            ORDER BY s.createdAt DESC
        """).fetch().all());
    }

    public GraphResponseDTO getLatestSessionGraph() {
        String sid = getLatestSessionIdOrNull();
        if (sid == null) {
            return new GraphResponseDTO(List.of(), List.of());
        }
        return getGraphBySession(sid);
    }

    /**
     * Return a graph containing nodes/links from the provided session IDs (merge of sessions).
     * Useful for customer "ALL" view where we only want sessions belonging to that customer.
     */
    public GraphResponseDTO getGraphBySessionIds(List<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return new GraphResponseDTO(List.of(), List.of());
        }

        Map<String, GraphNodeDTO> nodeMap = new LinkedHashMap<>();
        Set<String> linkKeySet = new LinkedHashSet<>();
        List<GraphLinkDTO> links = new ArrayList<>();

        // ---------- SESSION NODES (only provided ids) ----------
        neo4j.query("""
            MATCH (s:AnalysisSession)
            WHERE s.id IN $sids
            RETURN elementId(s) AS id,
                   'AnalysisSession' AS type,
                   coalesce(s.fileName,'SESSION') AS value,
                   coalesce(s.status,'valid') AS status,
                   coalesce(s.riskLevel,'low') AS riskLevel,
                   coalesce(s.riskScore,0) AS riskScore,
                   coalesce(s.verdict,'AN TOÀN') AS verdict,
                   coalesce(s.indicators,[]) AS indicators,
                   '' AS source,
                   s.id AS sessionId
            ORDER BY s.createdAt DESC
        """)
        .bind(sessionIds).to("sids")
        .fetch().all()
        .forEach(m -> {
            GraphNodeDTO n = mapNode(m);
            nodeMap.put(n.getId(), n);
        });

        // ---------- ENTITY NODES linked to those sessions ----------
        neo4j.query("""
            MATCH (s:AnalysisSession)-[:HAS_EMAIL|:HAS_IP|:HAS_URL|:HAS_DOMAIN|:HAS_FILE|:HAS_FILEHASH|:HAS_VICTIM]->(n)
            WHERE s.id IN $sids AND coalesce(n.deleted,false) = false
            RETURN elementId(n) AS id,
                   labels(n)[0] AS type,
                   coalesce(n.email,n.ip,n.url,n.domain,n.fileName,n.hash,n.username,n.account) AS value,
                   coalesce(n.status,'valid') AS status,
                   coalesce(n.riskLevel,'low') AS riskLevel,
                   coalesce(n.riskScore,0) AS riskScore,
                   coalesce(n.verdict,'AN TOÀN') AS verdict,
                   coalesce(n.indicators,[]) AS indicators,
                   coalesce(n.source,'') AS source,
                   s.id AS sessionId
        """)
        .bind(sessionIds).to("sids")
        .fetch().all()
        .forEach(m -> {
            GraphNodeDTO n = mapNode(m);
            nodeMap.put(n.getId(), n);
        });

        // ---------- SESSION -> ENTITY links ----------
        neo4j.query("""
            MATCH (s:AnalysisSession)-[r:HAS_EMAIL|HAS_IP|HAS_URL|HAS_DOMAIN|HAS_FILE|HAS_FILEHASH|HAS_VICTIM]->(n)
            WHERE s.id IN $sids AND coalesce(n.deleted,false) = false
            RETURN elementId(s) AS source,
                   elementId(n) AS target,
                   type(r) AS type
        """)
        .bind(sessionIds).to("sids")
        .fetch().all()
        .forEach(m -> addLink(m, links, linkKeySet));

        // ---------- INTER-ENTITY LINKS within those sessions (sessionId on rel) ----------
        neo4j.query("""
            MATCH (s:AnalysisSession)-[:HAS_EMAIL|:HAS_IP|:HAS_URL|:HAS_DOMAIN|:HAS_FILE|:HAS_FILEHASH|:HAS_VICTIM]->(a)
            MATCH (a)-[r:SENT_FROM_IP|CONTAINS_URL|HOSTED_ON|HOSTED_ON_DOMAIN|RESOLVES_TO|DOWNLOADS|HAS_HASH|RECEIVED|CONNECTS_TO]->(b)
            WHERE s.id IN $sids
              AND coalesce(a.deleted,false) = false
              AND coalesce(b.deleted,false) = false
              AND r.sessionId IN $sids
            RETURN DISTINCT elementId(a) AS source,
                            elementId(b) AS target,
                            type(r) AS type
        """)
        .bind(sessionIds).to("sids")
        .fetch().all()
        .forEach(m -> addLink(m, links, linkKeySet));

        return new GraphResponseDTO(new ArrayList<>(nodeMap.values()), links);
    }

    /* ===================================================== */
    /* GRAPH BY SESSION (FIXED VERSION)                     */
    /* ===================================================== */

    public GraphResponseDTO getGraphBySession(String sessionId) {

        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId không hợp lệ");
        }

        Map<String, GraphNodeDTO> nodeMap = new LinkedHashMap<>();
        Set<String> linkKeySet = new LinkedHashSet<>();
        List<GraphLinkDTO> links = new ArrayList<>();

        /* ---------- SESSION NODE ---------- */
        Map<String, Object> sessionRow = neo4j.query("""
            MATCH (s:AnalysisSession {id: $sid})
            RETURN elementId(s) AS id,
                   'AnalysisSession' AS type,
                   coalesce(s.fileName,'SESSION') AS value,
                   coalesce(s.status,'valid') AS status,
                   coalesce(s.riskLevel,'low') AS riskLevel,
                   coalesce(s.riskScore,0) AS riskScore,
                   coalesce(s.verdict,'AN TOÀN') AS verdict,
                   coalesce(s.indicators,[]) AS indicators,
                   '' AS source,
                   s.id AS sessionId
        """)
        .bind(sessionId).to("sid")
        .fetch()
        .one()
        .orElseThrow(() ->
                new IllegalArgumentException("Session không tồn tại")
        );

        GraphNodeDTO sessionNode = mapNode(sessionRow);
        nodeMap.put(sessionNode.getId(), sessionNode);

        /* ---------- ENTITY NODES ---------- */
        neo4j.query("""
            MATCH (s:AnalysisSession {id: $sid})
                  -[:HAS_EMAIL|HAS_IP|HAS_URL|HAS_DOMAIN|HAS_FILE|HAS_FILEHASH|HAS_VICTIM]->(n)
            WHERE coalesce(n.deleted,false) = false
            RETURN elementId(n) AS id,
                   labels(n)[0] AS type,
                   coalesce(n.email,n.ip,n.url,n.domain,n.fileName,n.hash,n.username,n.account) AS value,
                   coalesce(n.status,'valid') AS status,
                   coalesce(n.riskLevel,'low') AS riskLevel,
                   coalesce(n.riskScore,0) AS riskScore,
                   coalesce(n.verdict,'AN TOÀN') AS verdict,
                   coalesce(n.indicators,[]) AS indicators,
                   coalesce(n.source,'') AS source,
                   s.id AS sessionId
        """)
        .bind(sessionId).to("sid")
        .fetch().all()
        .forEach(m -> {
            GraphNodeDTO n = mapNode(m);
            nodeMap.put(n.getId(), n);
        });

        /* ---------- SESSION → ENTITY LINKS ---------- */
        neo4j.query("""
            MATCH (s:AnalysisSession {id: $sid})
                  -[r:HAS_EMAIL|HAS_IP|HAS_URL|HAS_DOMAIN|HAS_FILE|HAS_FILEHASH|HAS_VICTIM]->(n)
            WHERE coalesce(n.deleted,false) = false
            RETURN elementId(s) AS source,
                   elementId(n) AS target,
                   type(r) AS type
        """)
        .bind(sessionId).to("sid")
        .fetch().all()
        .forEach(m -> addLink(m, links, linkKeySet));

        /* ===================================================== */
        /* ⭐⭐⭐ FIX QUAN TRỌNG NHẤT Ở ĐÂY ⭐⭐⭐                  */
        /* Chỉ lấy relationship thuộc đúng session             */
        /* ===================================================== */

        neo4j.query("""
            MATCH (s:AnalysisSession {id: $sid})
                  -[:HAS_EMAIL|HAS_IP|HAS_URL|HAS_DOMAIN|HAS_FILE|HAS_FILEHASH|HAS_VICTIM]->(a)

            MATCH (a)-[r:SENT_FROM_IP|CONTAINS_URL|HOSTED_ON|HOSTED_ON_DOMAIN|RESOLVES_TO|DOWNLOADS|HAS_HASH|RECEIVED|CONNECTS_TO]->(b)

            WHERE coalesce(a.deleted,false) = false
              AND coalesce(b.deleted,false) = false
              AND r.sessionId = $sid

            RETURN DISTINCT elementId(a) AS source,
                            elementId(b) AS target,
                            type(r) AS type
        """)
        .bind(sessionId).to("sid")
        .fetch().all()
        .forEach(m -> addLink(m, links, linkKeySet));

        return new GraphResponseDTO(
                new ArrayList<>(nodeMap.values()),
                links
        );
    }

    /* ===================================================== */
    /* UPDATE NODE                                           */
    /* ===================================================== */

    @Transactional("transactionManager")
    public GraphNodeDTO updateNode(String nodeId,
                                   String newValue,
                                   String updatedBy) {

        Map<String, Object> result = neo4j.query("""
            MATCH (n)
            WHERE elementId(n) = $id
              AND coalesce(n.deleted,false) = false
            SET n.email = CASE WHEN labels(n)[0] = 'Email' THEN $val ELSE n.email END,
                n.ip    = CASE WHEN labels(n)[0] = 'IPAddress' THEN $val ELSE n.ip END,
                n.url   = CASE WHEN labels(n)[0] = 'URL' THEN $val ELSE n.url END,
                n.domain = CASE WHEN labels(n)[0] = 'Domain' THEN $val ELSE n.domain END,
                n.fileName = CASE WHEN labels(n)[0] = 'File' THEN $val ELSE n.fileName END,
                n.hash = CASE WHEN labels(n)[0] = 'FileHash' THEN $val ELSE n.hash END,
                n.username = CASE WHEN labels(n)[0] = 'VictimAccount' THEN $val ELSE n.username END,
                n.updatedAt = datetime(),
                n.updatedBy = $by
            RETURN elementId(n) AS id,
                   labels(n)[0] AS type,
                   coalesce(n.email,n.ip,n.url,n.domain,n.fileName,n.hash,n.username,n.account) AS value,
                   coalesce(n.status,'valid') AS status,
                   coalesce(n.riskLevel,'low') AS riskLevel,
                   coalesce(n.riskScore,0) AS riskScore,
                   coalesce(n.verdict,'AN TOÀN') AS verdict,
                   coalesce(n.indicators,[]) AS indicators,
                   coalesce(n.source,'') AS source,
                   '' AS sessionId
        """)
        .bind(nodeId).to("id")
        .bind(newValue).to("val")
        .bind(updatedBy).to("by")
        .fetch()
        .one()
        .orElseThrow(() ->
                new IllegalStateException("Node không tồn tại hoặc đã bị xóa")
        );

        return mapNode(result);
    }

    @Transactional("transactionManager")
    public void deleteNode(String nodeId, String deletedBy) {

        // Perform a hard delete (remove node and its relationships) from the database.
        // We first capture the elementId(n) then detach delete the node.
        Map<String, Object> result = neo4j.query("""
            MATCH (n)
            WHERE elementId(n) = $id
            WITH elementId(n) AS id, n
            CALL {
              WITH n
              DETACH DELETE n
            }
            RETURN id
        """)
        .bind(nodeId).to("id")
        .fetch()
        .one()
        .orElseThrow(() -> new IllegalStateException("Node không tồn tại hoặc đã bị xóa"));

        // deletion executed; nothing to return
    }

    /* ===================================================== */
    /* HELPERS                                               */
    /* ===================================================== */

    private String getLatestSessionIdOrNull() {
        return neo4j.query("""
            MATCH (s:AnalysisSession)
            WHERE s.createdAt IS NOT NULL
            RETURN s.id
            ORDER BY s.createdAt DESC
            LIMIT 1
        """)
        .fetchAs(String.class)
        .one()
        .orElse(null);
    }

    private GraphNodeDTO mapNode(Map<String, Object> m) {
        return new GraphNodeDTO(
                safeStr(m.get("id")),
                safeStr(m.get("sessionId")),
                safeStr(m.get("type")),
                safeStr(m.get("value")),
                safeStr(m.get("status")),
                safeStr(m.get("riskLevel")),
                safeInt(m.get("riskScore")),
                safeStr(m.get("verdict")),
                castToStringList(m.get("indicators")),
                safeStr(m.get("source"))
        );
    }

    private void addLink(Map<String, Object> m,
                         List<GraphLinkDTO> links,
                         Set<String> keySet) {

        String src = safeStr(m.get("source"));
        String tgt = safeStr(m.get("target"));
        String type = safeStr(m.get("type"));

        if (src == null || tgt == null || src.equals(tgt)) return;

        String key = src + "->" + tgt + "::" + type;
        if (keySet.contains(key)) return;

        keySet.add(key);
        links.add(new GraphLinkDTO(src, tgt, type));
    }

    private static String safeStr(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private static int safeInt(Object o) {
        if (o instanceof Number n) return n.intValue();
        return 0;
    }

    private static List<String> castToStringList(Object obj) {
        List<String> list = new ArrayList<>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                if (o != null) list.add(o.toString());
            }
        }
        return list;
    }
}
