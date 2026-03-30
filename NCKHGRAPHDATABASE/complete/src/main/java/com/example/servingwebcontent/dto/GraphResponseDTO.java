package com.example.servingwebcontent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class GraphResponseDTO {

    @JsonProperty("nodes")
    private List<GraphNodeDTO> nodes;

    @JsonProperty("links")
    private List<GraphLinkDTO> links;

    public GraphResponseDTO() {
        this.nodes = new ArrayList<>();
        this.links = new ArrayList<>();
    }

    public GraphResponseDTO(List<GraphNodeDTO> nodes, List<GraphLinkDTO> links) {
        this.nodes = (nodes != null) ? nodes : new ArrayList<>();
        this.links = (links != null) ? links : new ArrayList<>();
    }

    public List<GraphNodeDTO> getNodes() {
        return nodes;
    }

    public List<GraphLinkDTO> getLinks() {
        return links;
    }

    /**
     * Convert raw Neo4j result -> GraphResponseDTO
     */
    public static GraphResponseDTO fromNeo4j(
            Collection<Map<String, Object>> nodesRaw,
            Collection<Map<String, Object>> edgesRaw) {

        Map<String, GraphNodeDTO> nodeMap = new LinkedHashMap<>();
        Set<String> linkKeySet = new LinkedHashSet<>();
        List<GraphLinkDTO> links = new ArrayList<>();

        /* ===================== NODES ===================== */

        if (nodesRaw != null) {

            for (Map<String, Object> row : nodesRaw) {

                if (row == null) continue;

                String id = safeToString(row.get("id"));
                if (id == null) id = safeToString(row.get("elementId"));

                if (id == null) continue;

                String sessionId = safeToString(row.get("sessionId"));
                if (sessionId == null) {
                    sessionId = safeToString(row.get("analysisSessionId"));
                }

                String type = safeToString(row.get("type"));
                if (type == null) {
                    type = safeToString(row.get("label"));
                }

                String value = safeToString(row.get("value"));

                if (value == null) {
                    value = firstNonBlank(
                            safeToString(row.get("email")),
                            safeToString(row.get("ip")),
                            safeToString(row.get("url")),
                            safeToString(row.get("domain")),
                            safeToString(row.get("hash")),
                            safeToString(row.get("file")),
                            safeToString(row.get("account"))
                    );
                }

                GraphNodeDTO node = new GraphNodeDTO(
                        id,
                        sessionId,
                        type,
                        value,
                        safeToString(row.get("status")),
                        safeToString(row.get("riskLevel")),
                        safeToInt(row.get("riskScore")),
                        safeToString(row.get("verdict")),
                        safeToStringList(row.get("indicators")),
                        safeToString(row.get("source"))
                );

                nodeMap.putIfAbsent(id, node);
            }
        }

        /* ===================== LINKS ===================== */

        if (edgesRaw != null) {

            for (Map<String, Object> row : edgesRaw) {

                if (row == null) continue;

                String source = safeToString(row.get("source"));
                String target = safeToString(row.get("target"));

                if (source == null) source = safeToString(row.get("from"));
                if (target == null) target = safeToString(row.get("to"));

                if (source == null || target == null) continue;

                if (source.equals(target)) continue;

                if (!nodeMap.containsKey(source) || !nodeMap.containsKey(target)) {
                    continue;
                }

                String type = safeToString(row.get("type"));
                if (type == null) type = "RELATED";

                String key = source + "->" + target + "::" + type;

                if (!linkKeySet.add(key)) {
                    continue;
                }

                links.add(new GraphLinkDTO(source, target, type));
            }
        }

        return new GraphResponseDTO(new ArrayList<>(nodeMap.values()), links);
    }

    /* ================= HELPERS ================= */

    private static String safeToString(Object o) {

        if (o == null) return null;

        String s = o.toString().trim();

        return s.isEmpty() ? null : s;
    }

    private static Integer safeToInt(Object o) {

        if (o == null) return null;

        if (o instanceof Number n) {
            return n.intValue();
        }

        try {
            return Integer.parseInt(o.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static List<String> safeToStringList(Object o) {

        if (o == null) return new ArrayList<>();

        List<String> out = new ArrayList<>();

        if (o instanceof List<?> list) {

            for (Object x : list) {

                String s = safeToString(x);

                if (s != null) {
                    out.add(s);
                }
            }

        } else {

            String s = safeToString(o);

            if (s != null) {
                out.add(s);
            }
        }

        return out;
    }

    private static String firstNonBlank(String... arr) {

        if (arr == null) return null;

        for (String s : arr) {

            if (s != null && !s.isBlank()) {
                return s;
            }
        }

        return null;
    }
}
