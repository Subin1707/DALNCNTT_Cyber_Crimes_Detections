package com.example.servingwebcontent.service;

import com.example.servingwebcontent.dto.*;
import com.example.servingwebcontent.repository.AnalysisSessionRepository;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnalysisSessionService {

    private final Neo4jClient neo4j;
    private final FraudAnalysisService fraudAnalysisService;
    private final GraphRiskService graphRiskService;
    private final HybridRiskScoringService hybridRiskScoringService;
    private final AnalysisSessionRepository sessionRepository;
    private final GraphUpdateBroadcaster graphUpdateBroadcaster;

    public AnalysisSessionService(
            Neo4jClient neo4j,
            FraudAnalysisService fraudAnalysisService,
            GraphRiskService graphRiskService,
            HybridRiskScoringService hybridRiskScoringService,
            AnalysisSessionRepository sessionRepository,
            GraphUpdateBroadcaster graphUpdateBroadcaster) {

        this.neo4j = neo4j;
        this.fraudAnalysisService = fraudAnalysisService;
        this.graphRiskService = graphRiskService;
        this.hybridRiskScoringService = hybridRiskScoringService;
        this.sessionRepository = sessionRepository;
        this.graphUpdateBroadcaster = graphUpdateBroadcaster;
    }

    /* =========================================================
       PROCESS SESSION
       ========================================================= */

    @Transactional
    public SessionProcessResult processSession(
            String sessionId,
            List<FraudInputDTO> inputs) {

        sessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "AnalysisSession không tồn tại: " + sessionId));

        List<AnalysisResultDTO> rowResults = new ArrayList<>();

        for (FraudInputDTO dto : inputs) {

            RowAnalysis row = analyzeRow(dto);

            rowResults.add(row.rowResult);

            buildGraph(sessionId, dto, row);
        }

        GraphRiskService.GraphRiskResult graphRisk =
                graphRiskService.calculateGraphRisk(sessionId);

        HybridRiskScoreDTO hybridRisk =
                hybridRiskScoringService.scoreSession(sessionId, graphRisk);

        updateSessionRisk(sessionId, graphRisk, hybridRisk);

        AnalysisResultDTO sessionResult =
                new AnalysisResultDTO(
                        sessionId,
                        "SESSION",
                        "AnalysisSession",
                        hybridRisk.getFinalScore(),
                        hybridRisk.getRiskLevel(),
                        hybridRisk.getVerdict(),
                        hybridRisk.getIndicators() == null
                                ? List.of()
                                : hybridRisk.getIndicators(),
                        hybridRisk.getRuleScore(),
                        hybridRisk.getKnnScore(),
                        hybridRisk.getProbabilityScore(),
                        hybridRisk.getFeatures()
                );

        if (graphUpdateBroadcaster != null) {
            graphUpdateBroadcaster.publish("graph-update", java.util.Map.of(
                    "ts", java.time.Instant.now().toString(),
                    "kind", "session-processed",
                    "sessionId", sessionId
            ));
        }

        return new SessionProcessResult(sessionResult, rowResults);
    }

    /* =========================================================
       ANALYZE ROW
       ========================================================= */

    private RowAnalysis analyzeRow(FraudInputDTO dto) {

        OutputDTO email = null;
        OutputDTO ip = null;
        OutputDTO url = null;
        OutputDTO domain = null;
        OutputDTO fileNode = null;
        OutputDTO fileHash = null;
        OutputDTO victim = null;

        if (notBlank(dto.getEmail())) {
            email = fraudAnalysisService.analyzeSingle(
                    "email", dto.getEmail());
        }

        if (notBlank(dto.getIp())) {
            ip = fraudAnalysisService.analyzeSingle(
                    "ip", dto.getIp());
        }

        if (notBlank(dto.getUrl())) {
            url = fraudAnalysisService.analyzeSingle(
                    "url", dto.getUrl());
        }

        if (notBlank(dto.getDomain())) {
            domain = fraudAnalysisService.analyzeSingle(
                    "domain", dto.getDomain());
        }

        if (notBlank(dto.getFileNode())) {
            fileNode = fraudAnalysisService.analyzeSingle(
                    "filenode", dto.getFileNode());
        }

        if (notBlank(dto.getFileHash())) {
            fileHash = fraudAnalysisService.analyzeSingle(
                    "filehash", dto.getFileHash());
        }

        if (notBlank(dto.getVictimAccount())) {
            victim = fraudAnalysisService.analyzeSingle(
                    "victim", dto.getVictimAccount());
        }

        int maxScore = 0;

        if (email != null) maxScore = Math.max(maxScore, email.getRiskScore());
        if (ip != null) maxScore = Math.max(maxScore, ip.getRiskScore());
        if (url != null) maxScore = Math.max(maxScore, url.getRiskScore());
        if (domain != null) maxScore = Math.max(maxScore, domain.getRiskScore());
        if (fileNode != null) maxScore = Math.max(maxScore, fileNode.getRiskScore());
        if (fileHash != null) maxScore = Math.max(maxScore, fileHash.getRiskScore());
        if (victim != null) maxScore = Math.max(maxScore, victim.getRiskScore());

        String riskLevel;
        String verdict;

        if (maxScore >= 80) {

            riskLevel = "high";
            verdict = "BLOCK";

        } else if (maxScore >= 50) {

            riskLevel = "medium";
            verdict = "REVIEW";

        } else {

            riskLevel = "low";
            verdict = "ALLOW";
        }

        AnalysisResultDTO rowResult =
                new AnalysisResultDTO(
                        null,
                        "ROW",
                        "Transaction",
                        maxScore,
                        riskLevel,
                        verdict,
                        List.of()
                );

        return new RowAnalysis(email, ip, url, domain, fileNode, fileHash, victim, rowResult);
    }

    /* =========================================================
       BUILD GRAPH
       ========================================================= */

    private void buildGraph(
            String sessionId,
            FraudInputDTO dto,
            RowAnalysis row) {

        String emailNodeId = null;
        String ipNodeId = null;
        String urlNodeId = null;
        String domainNodeId = null;
        String fileNodeId = null;
        String fileHashNodeId = null;
        String victimNodeId = null;

        if (row.email != null) {

            emailNodeId = mergeNode(
                    "Email",
                    "email",
                    dto.getEmail(),
                    row.email
            );

            linkSession(
                    sessionId,
                    emailNodeId,
                    "HAS_EMAIL",
                    row.email
            );
        }

        if (row.ip != null) {

            ipNodeId = mergeNode(
                    "IPAddress",
                    "ip",
                    dto.getIp(),
                    row.ip
            );

            linkSession(
                    sessionId,
                    ipNodeId,
                    "HAS_IP",
                    row.ip
            );
        }

        if (row.url != null) {

            urlNodeId = mergeNode(
                    "URL",
                    "url",
                    dto.getUrl(),
                    row.url
            );

            linkSession(
                    sessionId,
                    urlNodeId,
                    "HAS_URL",
                    row.url
            );
        }

        if (row.domain != null) {

            domainNodeId = mergeNode(
                    "Domain",
                    "domain",
                    dto.getDomain(),
                    row.domain
            );

            linkSession(
                    sessionId,
                    domainNodeId,
                    "HAS_DOMAIN",
                    row.domain
            );
        }

        if (row.fileNode != null) {

            fileNodeId = mergeNode(
                    "File",
                    "fileName",
                    dto.getFileNode(),
                    row.fileNode
            );

            linkSession(
                    sessionId,
                    fileNodeId,
                    "HAS_FILE",
                    row.fileNode
            );
        }

        if (row.fileHash != null) {

            fileHashNodeId = mergeNode(
                    "FileHash",
                    "hash",
                    dto.getFileHash(),
                    row.fileHash
            );

            linkSession(
                    sessionId,
                    fileHashNodeId,
                    "HAS_FILEHASH",
                    row.fileHash
            );
        }

        if (row.victim != null) {

            victimNodeId = mergeNode(
                    "VictimAccount",
                    "username",
                    dto.getVictimAccount(),
                    row.victim
            );

            linkSession(
                    sessionId,
                    victimNodeId,
                    "HAS_VICTIM",
                    row.victim
            );
        }

        if (emailNodeId != null && ipNodeId != null) {
            link(emailNodeId, ipNodeId, "SENT_FROM_IP", sessionId);
        }

        if (emailNodeId != null && urlNodeId != null) {
            link(emailNodeId, urlNodeId, "CONTAINS_URL", sessionId);
        }

        if (urlNodeId != null && ipNodeId != null) {
            link(urlNodeId, ipNodeId, "HOSTED_ON", sessionId);
        }

        if (urlNodeId != null && domainNodeId != null) {
            link(urlNodeId, domainNodeId, "HOSTED_ON_DOMAIN", sessionId);
        }

        if (domainNodeId != null && ipNodeId != null) {
            link(domainNodeId, ipNodeId, "RESOLVES_TO", sessionId);
        }

        if (urlNodeId != null && fileNodeId != null) {
            link(urlNodeId, fileNodeId, "DOWNLOADS", sessionId);
        }

        if (fileNodeId != null && fileHashNodeId != null) {
            link(fileNodeId, fileHashNodeId, "HAS_HASH", sessionId);
        }

        if (victimNodeId != null && emailNodeId != null) {
            link(victimNodeId, emailNodeId, "RECEIVED", sessionId);
        }
    }

    /* =========================================================
       SESSION UPDATE
       ========================================================= */

    private void updateSessionRisk(
            String sessionId,
            GraphRiskService.GraphRiskResult graphRisk,
            HybridRiskScoreDTO hybridRisk) {

        neo4j.query("""
            MATCH (s:AnalysisSession {id:$sid})
            SET s.ruleScore = $ruleScore,
                s.knnScore = $knnScore,
                s.probabilityScore = $probabilityScore,
                s.hybridFinalScore = $hybridScore,
                s.hybridRiskLevel = $hybridLevel,
                s.hybridVerdict = $hybridVerdict,
                s.riskScore  = $hybridScore,
                s.riskLevel  = $hybridLevel,
                s.verdict    = $hybridVerdict,
                s.indicators = $hybridIndicators,
                s.featureNumEmails = $featureNumEmails,
                s.featureNumIps = $featureNumIps,
                s.featureNumUrls = $featureNumUrls,
                s.featureNumDomains = $featureNumDomains,
                s.featureNumSharedIps = $featureNumSharedIps,
                s.featureNumRepeatedUrls = $featureNumRepeatedUrls,
                s.featureNumHighRiskNodes = $featureNumHighRiskNodes,
                s.featureNumMediumRiskNodes = $featureNumMediumRiskNodes,
                s.featureNumIndicators = $featureNumIndicators,
                s.graphRuleScore = $graphRuleScore,
                s.graphRuleRiskLevel = $graphRuleRiskLevel,
                s.graphRuleVerdict = $graphRuleVerdict,
                s.graphRuleIndicators = $graphRuleIndicators,
                s.status     = 'DONE',
                s.updatedAt  = datetime()
        """)
                .bind(sessionId).to("sid")
                .bind(hybridRisk.getRuleScore()).to("ruleScore")
                .bind(hybridRisk.getKnnScore()).to("knnScore")
                .bind(hybridRisk.getProbabilityScore()).to("probabilityScore")
                .bind(hybridRisk.getFinalScore()).to("hybridScore")
                .bind(hybridRisk.getRiskLevel()).to("hybridLevel")
                .bind(hybridRisk.getVerdict()).to("hybridVerdict")
                .bind(hybridRisk.getIndicators()).to("hybridIndicators")
                .bind(hybridRisk.getFeatures().getNumEmails()).to("featureNumEmails")
                .bind(hybridRisk.getFeatures().getNumIps()).to("featureNumIps")
                .bind(hybridRisk.getFeatures().getNumUrls()).to("featureNumUrls")
                .bind(hybridRisk.getFeatures().getNumDomains()).to("featureNumDomains")
                .bind(hybridRisk.getFeatures().getNumSharedIps()).to("featureNumSharedIps")
                .bind(hybridRisk.getFeatures().getNumRepeatedUrls()).to("featureNumRepeatedUrls")
                .bind(hybridRisk.getFeatures().getNumHighRiskNodes()).to("featureNumHighRiskNodes")
                .bind(hybridRisk.getFeatures().getNumMediumRiskNodes()).to("featureNumMediumRiskNodes")
                .bind(hybridRisk.getFeatures().getNumIndicators()).to("featureNumIndicators")
                .bind(graphRisk.score).to("graphRuleScore")
                .bind(graphRisk.riskLevel).to("graphRuleRiskLevel")
                .bind(graphRisk.verdict).to("graphRuleVerdict")
                .bind(graphRisk.indicators).to("graphRuleIndicators")
                .run();
    }

    /* =========================================================
       NODE MERGE
       ========================================================= */

    private String mergeNode(
            String label,
            String key,
            String value,
            OutputDTO out) {

        if (value == null) return null;

        return neo4j.query("""
            MERGE (n:%s {%s:$value})
            ON CREATE SET
                n.createdAt = datetime(),
                n.deleted = false
            SET n.lastSeen  = datetime(),
                n.riskScore = $rs,
                n.riskLevel = $rl,
                n.verdict   = $vd,
                n.indicators = $ind
            RETURN elementId(n) AS id
        """.formatted(label, key))
                .bind(value).to("value")
                .bind(out.getRiskScore()).to("rs")
                .bind(out.getRiskLevel()).to("rl")
                .bind(out.getVerdict()).to("vd")
                .bind(out.getIndicators()).to("ind")
                .fetchAs(String.class)
                .one()
                .orElse(null);
    }

    /* =========================================================
       LINK SESSION
       ========================================================= */

    private void linkSession(
            String sessionId,
            String nodeId,
            String rel,
            OutputDTO out) {

        if (nodeId == null) return;

        neo4j.query("""
            MATCH (s:AnalysisSession {id:$sid}), (n)
            WHERE elementId(n) = $nid
            MERGE (s)-[r:%s {sessionId:$sid}]->(n)
            SET r.riskScore = $rs,
                r.riskLevel = $rl,
                r.verdict   = $vd,
                r.indicators = $ind,
                r.lastSeen  = datetime()
        """.formatted(rel))
                .bind(sessionId).to("sid")
                .bind(nodeId).to("nid")
                .bind(out.getRiskScore()).to("rs")
                .bind(out.getRiskLevel()).to("rl")
                .bind(out.getVerdict()).to("vd")
                .bind(out.getIndicators()).to("ind")
                .run();
    }

    /* =========================================================
       LINK NODES
       ========================================================= */

    private void link(
            String fromId,
            String toId,
            String rel,
            String sessionId) {

        if (fromId == null || toId == null) return;

        neo4j.query("""
            MATCH (a),(b)
            WHERE elementId(a)=$a AND elementId(b)=$b
            MERGE (a)-[r:%s {sessionId:$sid}]->(b)
            SET r.lastSeen = datetime()
        """.formatted(rel))
                .bind(fromId).to("a")
                .bind(toId).to("b")
                .bind(sessionId).to("sid")
                .run();
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    /* =========================================================
       INTERNAL MODEL
       ========================================================= */

    private static class RowAnalysis {

        OutputDTO email;
        OutputDTO ip;
        OutputDTO url;
        OutputDTO domain;
        OutputDTO fileNode;
        OutputDTO fileHash;
        OutputDTO victim;

        AnalysisResultDTO rowResult;

        RowAnalysis(
                OutputDTO email,
                OutputDTO ip,
                OutputDTO url,
                OutputDTO domain,
                OutputDTO fileNode,
                OutputDTO fileHash,
                OutputDTO victim,
                AnalysisResultDTO rowResult) {

            this.email = email;
            this.ip = ip;
            this.url = url;
            this.domain = domain;
            this.fileNode = fileNode;
            this.fileHash = fileHash;
            this.victim = victim;
            this.rowResult = rowResult;
        }
    }
}
