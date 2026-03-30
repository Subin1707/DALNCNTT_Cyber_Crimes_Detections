package com.example.servingwebcontent.service;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraphRiskService {

    private final Neo4jClient neo4j;

    public GraphRiskService(Neo4jClient neo4j) {
        this.neo4j = neo4j;
    }

    private static final Map<String,Integer> REL_RISK = Map.of(
            "SENT_FROM_IP",15,
            "CONTAINS_URL",10,
            "HOSTED_ON",18
    );

    private static final double HOP_DECAY = 0.55;
    private static final int MAX_DEPTH = 3;

    private static final int MAX_GRAPH_RISK = 80;
    private static final int MIN_GRAPH_RISK = -30;

    private static final int MAX_EVIDENCE_COUNT = 20;

    public GraphRiskResult calculateGraphRisk(String sessionId){

        List<GraphEvidence> evidences = new ArrayList<>();

        int baseScore = collectBaseRisk(sessionId);

        if(baseScore != 0){
            evidences.add(new GraphEvidence(baseScore,1,"BASE_ENGINE"));
        }

        evidences.addAll(collectPropagationEvidence(sessionId));
        evidences.addAll(collectSharedIpEvidence(sessionId));
        evidences.addAll(collectDensityAnomaly(sessionId));
        evidences.addAll(collectClusterAnomaly(sessionId));

        if(evidences.size() > MAX_EVIDENCE_COUNT){
            evidences = evidences.stream()
                    .sorted(Comparator.comparingInt(e -> -Math.abs(e.score)))
                    .limit(MAX_EVIDENCE_COUNT)
                    .collect(Collectors.toList());
        }

        double total = 0;
        List<String> indicators = new ArrayList<>();

        for(GraphEvidence ev : evidences){

            int depth = Math.max(1, Math.min(ev.depth, MAX_DEPTH));

            double decay = Math.pow(HOP_DECAY, depth-1);

            if(depth >=3){
                decay *= 1.15;
            }

            double applied = ev.score * decay;

            total += applied;

            indicators.add(ev.explain(decay));
        }

        total = normalize(total);

        int finalScore = clamp(
                (int)Math.round(total),
                MIN_GRAPH_RISK,
                MAX_GRAPH_RISK
        );

        return GraphRiskResult.fromScore(finalScore, indicators);
    }

    private int collectBaseRisk(String sessionId){

        return neo4j.query("""
            MATCH (s:AnalysisSession {id:$sid})
                  -[r:HAS_EMAIL|HAS_IP|HAS_URL]->()
            RETURN coalesce(sum(r.riskScore),0) AS total
        """)
        .bind(sessionId).to("sid")
        .fetchAs(Integer.class)
        .one()
        .orElse(0);
    }

    private List<GraphEvidence> collectPropagationEvidence(String sessionId){

        Collection<Map<String,Object>> rows = neo4j.query("""
            MATCH (s:AnalysisSession {id:$sid})
                  -[base:HAS_EMAIL|HAS_IP|HAS_URL]->(root)

            MATCH p=(root)-[rels:SENT_FROM_IP|CONTAINS_URL|HOSTED_ON*1..3]->(n)

            WHERE n <> root
              AND ALL(r IN rels WHERE r.sessionId = $sid)

            WITH DISTINCT n,
                 last(rels) AS rel,
                 length(p) AS depth,
                 base.riskScore AS baseRisk

            RETURN baseRisk,type(rel) AS relType,depth
        """)
        .bind(sessionId).to("sid")
        .fetch()
        .all();

        List<GraphEvidence> result = new ArrayList<>();

        for(Map<String,Object> row : rows){

            int depth = ((Number)row.getOrDefault("depth",1)).intValue();

            int baseRisk = ((Number)row.getOrDefault("baseRisk",0)).intValue();

            String relType = String.valueOf(row.getOrDefault("relType",""));

            int relScore = REL_RISK.getOrDefault(relType,0);

            int total = relScore;

            if(baseRisk >= 50){
                total += 20;
            }

            if(total != 0){
                result.add(
                        new GraphEvidence(
                                total,
                                depth,
                                "PROPAGATION via "+relType
                        )
                );
            }
        }

        return result;
    }

    private List<GraphEvidence> collectSharedIpEvidence(String sessionId){

        Collection<Map<String,Object>> rows = neo4j.query("""
            MATCH (s:AnalysisSession {id:$sid})-[r:HAS_EMAIL]->(e:Email)
            MATCH (e)-[rel:SENT_FROM_IP]->(ip:IPAddress)
            WHERE rel.sessionId = $sid
            WITH ip, collect(r.riskScore) AS scores
            WHERE size(scores) >= 2
            RETURN scores
        """)
        .bind(sessionId).to("sid")
        .fetch()
        .all();

        List<GraphEvidence> result = new ArrayList<>();

        for(Map<String,Object> row : rows){

            Object obj = row.get("scores");

            if(!(obj instanceof List<?> scores)) continue;

            long highRisk = scores.stream()
                    .filter(Objects::nonNull)
                    .map(o -> ((Number)o).intValue())
                    .filter(v -> v>=50)
                    .count();

            if(highRisk>=2){
                result.add(new GraphEvidence(
                        45,
                        1,
                        "SHARED_IP high-risk cluster"
                ));
            }
        }

        return result;
    }

    private List<GraphEvidence> collectDensityAnomaly(String sessionId){

        Collection<Map<String,Object>> rows = neo4j.query("""
            MATCH (s:AnalysisSession {id:$sid})
            MATCH (s)-[:HAS_EMAIL]->(e)
            MATCH (e)-[rel:SENT_FROM_IP]->(ip)
            WHERE rel.sessionId = $sid
            WITH ip, count(e) AS c
            WHERE c >= 4
            RETURN c
        """)
        .bind(sessionId).to("sid")
        .fetch()
        .all();

        List<GraphEvidence> result = new ArrayList<>();

        for(Map<String,Object> ignored : rows){

            result.add(new GraphEvidence(
                    30,
                    1,
                    "IP density anomaly"
            ));
        }

        return result;
    }

    private List<GraphEvidence> collectClusterAnomaly(String sessionId){

        Collection<Map<String,Object>> rows = neo4j.query("""
            MATCH (s:AnalysisSession {id:$sid})
                  -[r:HAS_EMAIL]->()
            WHERE r.riskScore >= 50
            RETURN count(r) AS fakeCount
        """)
        .bind(sessionId).to("sid")
        .fetch()
        .all();

        List<GraphEvidence> result = new ArrayList<>();

        for(Map<String,Object> row : rows){

            int fake = ((Number)row.getOrDefault("fakeCount",0)).intValue();

            if(fake>=3){
                result.add(new GraphEvidence(
                        45,
                        1,
                        "MULTI HIGH-RISK cluster"
                ));
            }
        }

        return result;
    }

    private double normalize(double score){
        return score/(1+Math.abs(score)/100.0);
    }

    private int clamp(int v,int min,int max){
        return Math.max(min,Math.min(v,max));
    }

    private static class GraphEvidence{

        final int score;
        final int depth;
        final String reason;

        GraphEvidence(int score,int depth,String reason){
            this.score=score;
            this.depth=depth;
            this.reason=reason;
        }

        String explain(double decay){
            return reason+
                    " | base="+score+
                    " depth="+depth+
                    " decay="+String.format("%.2f",decay)+
                    " final="+String.format("%.1f",score*decay);
        }
    }

    public static class GraphRiskResult{

        public final int score;
        public final String riskLevel;
        public final String verdict;
        public final List<String> indicators;

        private GraphRiskResult(int s,String rl,String v,List<String> i){
            score=s;
            riskLevel=rl;
            verdict=v;
            indicators=i;
        }

        public static GraphRiskResult fromScore(int score,List<String> indicators){

            if(score>=50)
                return new GraphRiskResult(score,"high","BLOCK",indicators);

            if(score>=25)
                return new GraphRiskResult(score,"medium","REVIEW",indicators);

            return new GraphRiskResult(score,"low","ALLOW",indicators);
        }
    }
}