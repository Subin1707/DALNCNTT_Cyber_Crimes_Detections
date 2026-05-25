// admin-main.js – 10/10 ENTERPRISE VERSION

document.addEventListener("DOMContentLoaded", () => {

    /* ================= GLOBAL ================= */

    window.ADMIN_VIEW_TYPE = window.ADMIN_VIEW_TYPE || "ALL";

    const width = 1000;
    const height = 600;
    const nodeRadius = 18;
    const MIN_NODE_GAP = 18;
    const NODE_PACKING_STEP = (nodeRadius * 2) + MIN_NODE_GAP + 8;
    const NEAREST_K_MIN = 6;
    const NEAREST_K_MAX = 18;
    const DETAIL_LABEL_NODE_LIMIT = 120;
    const DETAIL_LABEL_LINK_LIMIT = 260;
    const DENSE_GRAPH_NODE_THRESHOLD = 180;
    const DENSE_GRAPH_LINK_THRESHOLD = 420;
    const MAX_RENDERED_OVERLAP_LINKS_DENSE = 180;

    const svg = d3.select("#graphSVG")
        .attr("width", width)
        .attr("height", height);

    let allNodes = [];
    let allLinks = [];
    let simulation = null;
    let currentSessionId = null;

    let abortController = null;
    const graphCache = new Map();
    const graphSignatureCache = new Map();
    // SSE will trigger instant updates; keep polling only as a fallback.
    const AUTO_REFRESH_MS = 10000;
    let autoRefreshTimer = null;
    let graphInitialized = false;
    let container = null;
    let linkSel = null;
    let linkLabelSel = null;
    let linkDistanceLabelSel = null;
    let centerDistanceLinkSel = null;
    let centerDistanceLabelSel = null;
    let nodeSel = null;
    let labelSel = null;
    let renderGraphPositions = null;
    let zoom = null;
    let prevAllNodeIds = new Set();
    let didInitialFit = false;
    let autoLockInterval = null;
    let lastRenderedSessionId = null;
    let isFetchingGraph = false;
    let autoRefreshTick = 0;
    let selectedNodeId = null;
    let showLinkLabelsAlways = false;
    let domainCircles = null;
    let domainCircleElements = {};
    let domainPos = {};
    let domainRadii = {};
    let lastLayoutKey = null;
    let zoomFitTimer = null;

    const DOMAIN_ORDER = ["safe", "suspicious", "fraud"];
    const DOMAIN_COLORS = {
        safe: "#0066cc",
        suspicious: "#cc8800",
        fraud: "#cc0033"
    };
    const DOMAIN_LABELS = {
        safe: "MIEN AN TOAN",
        suspicious: "MIEN NGHI NGO",
        fraud: "MIEN GIAN LAN"
    };
    const VISUAL_DOMAIN_BOUNDARY_DISTANCE = 0.55;
    let neutralSessionPos = { x: width * 0.50, y: height * 0.08 };

    /* ================= UTIL ================= */

    const safeText = (el, text) => {
        el.textContent = text ?? "—";
    };

    const safeId = v =>
        typeof v === "string"
            ? v
            : typeof v === "object" && v?.id
                ? v.id
                : null;

    const normalizeLinks = links =>
        (links || [])
            .map(l => ({
                ...l,
                source: safeId(l.source),
                target: safeId(l.target),
                type: l.type || ""
            }))
            .filter(l => l.source && l.target);

    const graphSignature = (nodes, links) => {
        const nodePart = (nodes || [])
            .map(n => [
                n.id,
                n.type,
                n.value,
                n.riskLevel,
                n.riskScore,
                n.status,
                n.membershipStatus,
                n.domainAssignment
            ].join("|"))
            .sort()
            .join(";");
        const linkPart = (links || [])
            .map(l => `${safeId(l.source)}>${safeId(l.target)}>${l.type || ""}>${l.overlapScore || ""}>${l.riskScore || ""}`)
            .sort()
            .join(";");
        return `${nodePart}#${linkPart}`;
    };

    const visibleLinksForLayout = (nodes, links) => {
        const safeNodes = Array.isArray(nodes) ? nodes : [];
        const safeLinks = Array.isArray(links) ? links : [];
        const dense = safeNodes.length > DENSE_GRAPH_NODE_THRESHOLD || safeLinks.length > DENSE_GRAPH_LINK_THRESHOLD;
        if (!dense) return safeLinks;

        let overlapCount = 0;
        return safeLinks.filter(link => {
            if (String(link.type || "").toUpperCase() !== "OVERLAP") return true;
            if (overlapCount >= MAX_RENDERED_OVERLAP_LINKS_DENSE) return false;
            overlapCount++;
            return true;
        });
    };

    const filterLinks = (nodes, links) => {
        const ids = new Set(nodes.map(n => n.id));
        return links.filter(l => ids.has(l.source) && ids.has(l.target));
    };
    const publishGraphContext = (nodes, links) => {
        window.__graphContext = {
            nodes: Array.isArray(nodes) ? nodes : [],
            links: Array.isArray(links) ? links : [],
            linkTypeLabel
        };
    };

    const riskRank = r =>
        ({ low: 1, medium: 2, high: 3 }[String(r).toLowerCase()] || 0);

    const isSessionNode = (node) =>
        String(node?.type || "").toLowerCase() === "analysissession";

    const assignDomain = (node) => {
        if (isSessionNode(node)) {
            return null;
        }

        const nearest = nearestDomainFromBackend(node);
        if (nearest) {
            return nearest;
        }

        const backendDomain = String(node?.domainAssignment || "").toLowerCase().trim();
        if (["safe", "suspicious", "fraud"].includes(backendDomain)) {
            return backendDomain;
        }

        const risk = String(node?.riskLevel || "low").toLowerCase().trim();
        if (risk === "high") return "fraud";
        if (risk === "medium") return "suspicious";
        return "safe";
    };

    const nearestDomainFromBackend = (node) => {
        const distances = node?.domainDistances;
        if (!distances || typeof distances !== "object") return null;

        let bestDomain = null;
        let bestDistance = Number.POSITIVE_INFINITY;
        DOMAIN_ORDER.forEach(domain => {
            const value = Number(distances[domain]);
            if (Number.isFinite(value) && value < bestDistance) {
                bestDomain = domain;
                bestDistance = value;
            }
        });
        return bestDomain;
    };

    const riskValue = (node) => Math.max(0, Math.min(100, Number(node?.riskScore) || 0));
    const clamp01 = (v) => Math.max(0, Math.min(1, v));
    const domainStrength = (node) => {
        const domain = node?.domainAssignment || assignDomain(node);
        const backendDistance = Number(node?.domainDistances?.[domain]);
        if (Number.isFinite(backendDistance)) {
            return clamp01(1 - (backendDistance / VISUAL_DOMAIN_BOUNDARY_DISTANCE));
        }

        const risk = riskValue(node);

        if (domain === "safe") {
            return clamp01(1 - (risk / 33));
        }
        if (domain === "fraud") {
            return clamp01((risk - 67) / 33);
        }
        if (domain === "suspicious") {
            return clamp01(1 - (Math.abs(risk - 50) / 17));
        }
        return 0;
    };
    const distanceToDomainCenter = (node) => {
        if (!node || isSessionNode(node)) return null;
        return (1 - domainStrength(node)) * 100;
    };
    const distanceLabel = (node) => {
        const distance = distanceToDomainCenter(node);
        return distance == null ? "" : `d=${distance.toFixed(2)}`;
    };
    const nodeFeatureDistance = (a, b) => {
        if (!a || !b) return null;

        const domainIndex = { safe: 0, suspicious: 0.5, fraud: 1 };
        const typeIndex = {
            AnalysisSession: 0,
            Email: 0.15,
            IPAddress: 0.3,
            URL: 0.45,
            Domain: 0.6,
            FileNode: 0.75,
            FileHash: 0.85,
            VictimAccount: 1
        };
        const statusIndex = { valid: 0, suspicious: 0.5, fake: 1 };
        const riskLevelIndex = { low: 0, medium: 0.5, high: 1 };

        const vector = (node) => [
            riskValue(node) / 100,
            domainIndex[node.domainAssignment || assignDomain(node) || "safe"] ?? 0,
            typeIndex[node.type] ?? 0.5,
            statusIndex[String(node.status || "valid").toLowerCase()] ?? 0,
            riskLevelIndex[String(node.riskLevel || "low").toLowerCase()] ?? 0,
            node.manualBlocked ? 1 : 0
        ];

        const va = vector(a);
        const vb = vector(b);
        const squared = va.reduce((sum, value, index) => {
            const diff = value - vb[index];
            return sum + diff * diff;
        }, 0);

        return Math.sqrt(squared) * 100;
    };
    const resolveNode = (nodeOrId, nodeMap) => {
        const id = safeId(nodeOrId);
        if (!id) return null;
        if (typeof nodeOrId === "object" && nodeOrId?.id) return nodeOrId;
        return nodeMap?.get(id) || null;
    };
    const nodeDistanceLabel = (link, nodeMap) => {
        const sourceNode = resolveNode(link?.source, nodeMap);
        const targetNode = resolveNode(link?.target, nodeMap);
        const distance = nodeFeatureDistance(sourceNode, targetNode);
        return distance == null ? "" : `d=${distance.toFixed(2)}`;
    };
    const regionDistanceScore = (node, domain) => {
        if (!node) return 1;
        const centers = {
            safe: [0.10, 0.00, 0.00, 0.00],
            suspicious: [0.50, 0.50, 0.50, 0.20],
            fraud: [0.90, 1.00, 1.00, 1.00]
        };
        const statusIndex = { valid: 0, suspicious: 0.5, fake: 1 };
        const riskLevelIndex = { low: 0, medium: 0.5, high: 1 };
        const vector = [
            riskValue(node) / 100,
            statusIndex[String(node.status || "valid").toLowerCase()] ?? 0,
            riskLevelIndex[String(node.riskLevel || "low").toLowerCase()] ?? 0,
            node.manualBlocked ? 1 : 0
        ];
        const center = centers[domain] || centers.safe;
        const squared = vector.reduce((sum, value, index) => {
            const diff = value - center[index];
            return sum + diff * diff;
        }, 0);
        return Math.sqrt(squared);
    };
    const regionProbabilities = (node) => {
        const raw = {};
        DOMAIN_ORDER.forEach(domain => {
            raw[domain] = Math.exp(-regionDistanceScore(node, domain) * 3.2);
        });
        const total = Object.values(raw).reduce((sum, value) => sum + value, 0) || 1;
        return Object.fromEntries(
            DOMAIN_ORDER.map(domain => [domain, (raw[domain] / total) * 100])
        );
    };
    const linkTypeLabel = (t, d = null) => ({
        HAS_EMAIL: "phiên → email",
        HAS_IP: "phiên → IP",
        HAS_URL: "phiên → URL",
        HAS_DOMAIN: "phiên → domain",
        HAS_FILE: "phiên → file",
        HAS_FILEHASH: "phiên → hash",
        HAS_VICTIM: "phiên → victim",
        SENT_FROM_IP: "gửi từ IP",
        CONTAINS_URL: "chứa URL",
        HOSTED_ON: "hosted on",
        HOSTED_ON_DOMAIN: "URL thuộc domain",
        RESOLVES_TO: "phân giải",
        DOWNLOADS: "tải về",
        HAS_HASH: "có hash",
        RECEIVED: "nhận",
        CONNECTS_TO: "kết nối"
        ,OVERLAP: d && Number(d.overlapScore) ? `overlap ${formatNumber(d.overlapScore)}` : "overlap"
    }[String(t || "").toUpperCase()] || String(t || ""));

    const safeTextHtml = (s) =>
        String(s ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;");

    const formatNumber = (value) => {
        const n = Number(value);
        return Number.isFinite(n) ? n.toFixed(2) : "0.00";
    };

    const nodeFill = (d) => {
        if (isSessionNode(d)) return "#64748b";
        if (d.membershipStatus === "OUTSIDE") return "#94a3b8";
        return d.riskLevel === "high" ? "#c62828" :
            d.riskLevel === "medium" ? "#f9a825" : "#1e88e5";
    };

    const nodeStroke = (d) => {
        if (isSessionNode(d)) return "#334155";
        if (isBalancedInfluenceNode(d)) return "#111827";
        if (d.multiDomainOverlap) return "#7c3aed";
        if ((Number(d.adjustedOverlapScore) || 0) >= 0.3) return "#7c3aed";
        if (d.membershipStatus === "OUTSIDE") return "#64748b";
        if (d._isNew) return "#000";
        if (d.source === "WIRESHARK") return "#00acc1";
        return "#fff";
    };

    const nodeStrokeWidth = (d) => {
        if (isSessionNode(d)) return 2.4;
        if (isBalancedInfluenceNode(d)) return 4;
        if (d.multiDomainOverlap) return 4;
        if ((Number(d.adjustedOverlapScore) || 0) >= 0.3) return 3;
        if (d.membershipStatus === "OUTSIDE") return 2.5;
        return d._isNew || d.source === "WIRESHARK" ? 3 : 1.2;
    };

    const linkStrokeWidth = (d) => {
        if (d.type !== "OVERLAP") return 1.25;
        return 0.65 + Math.min(2.8, (Number(d.overlapScore) || 0) * 2.2);
    };

    const linkOpacity = (d) => d.type === "OVERLAP" ? 0.22 : 0.18;

    const isOverlapNode = (node) => !!node?.multiDomainOverlap;
    const isBalancedInfluenceNode = (node) => {
        if (!node || isSessionNode(node)) return false;

        const zone = String(node.influenceZone || node.nodeClassification || "").toUpperCase();
        const explicitOverlap = node.multiDomainOverlap
            || node.bridgeNode
            || ["BRIDGE", "OVERLAP", "OUTSIDE_INFLUENCE"].includes(zone);
        if (!explicitOverlap) return false;

        const influence = node?.domainInfluence || node?.softMemberships;
        if (!influence || typeof influence !== "object") return false;
        const values = DOMAIN_ORDER
            .map(domain => Number(influence[domain]) || 0)
            .filter(value => value > 0);
        if (values.length < 2) return false;
        const max = Math.max(...values);
        const min = Math.min(...values);
        return max >= 0.30 && (max - min) <= 0.18;
    };

    const formatDomainDistances = (distances) => {
        if (!distances || typeof distances !== "object") return "none";
        const ordered = DOMAIN_ORDER.filter(domain => distances[domain] !== undefined);
        const keys = ordered.length ? ordered : Object.keys(distances);
        return keys
            .map(key => `${key}=${formatNumber(distances[key])}`)
            .join(", ") || "none";
    };

    const nodeDisplayValue = (node) => {
        const value = node?.value ?? node?.id ?? "";
        return value === "" ? "—" : String(value);
    };

    const riskClassName = (riskLevel) => {
        const risk = String(riskLevel || "low").toLowerCase().trim();
        if (risk === "high") return "node-fraud";
        if (risk === "medium") return "node-suspicious";
        return "node-safe";
    };

    function ensureLinkLabelToggle() {
        const host = document.querySelector(".graph-controls") || document.querySelector(".toolbar");
        if (!host) return;
        if (document.getElementById("toggleLinkLabels")) return;

        const wrap = document.createElement("label");
        wrap.style.cssText = "display:flex;align-items:center;gap:8px;margin-left:auto;font-weight:800;color:#0f172a;";
        wrap.innerHTML = `
            <input id="toggleLinkLabels" type="checkbox" style="width:16px;height:16px" />
            <span>Chú thích liên kết</span>
        `;
        host.appendChild(wrap);

        const cb = document.getElementById("toggleLinkLabels");
        cb.checked = !!showLinkLabelsAlways;
        cb.addEventListener("change", () => {
            showLinkLabelsAlways = cb.checked;
            applyRelationHighlight(selectedNodeId);
        });
    }

    const debounce = (fn, delay = 200) => {
        let t;
        return (...args) => {
            clearTimeout(t);
            t = setTimeout(() => fn(...args), delay);
        };
    };

    const showLoading = (state) => {
        const loader = document.getElementById("graphLoading");
        if (!loader) return;
        loader.style.display = state ? "block" : "none";
    };

    const markNewNodes = () => {
        const nextIds = new Set((allNodes || []).map(n => n.id));
        (allNodes || []).forEach(n => {
            const isNew = !prevAllNodeIds.has(n.id);
            n._isNew = isNew;
            if (isNew) n._newAt = Date.now();
        });
        prevAllNodeIds = nextIds;
    };

    /* ================= FETCH SESSION LIST ================= */

    async function fetchSessions() {
        const sel = document.getElementById("sessionSelect");
        if (!sel) return;

        try {
            const prevValue = sel.value;
            const res = await fetch("/admin/sessions");
            if (!res.ok) return;

            const sessions = await res.json();
            sel.innerHTML = `<option value="">ALL SESSIONS</option>`;

            sessions.forEach(s => {
                const id = s.sessionId || s.id;
                if (!id) return;

                const opt = document.createElement("option");
                opt.value = id;

                opt.textContent = [
                    id,
                    s.fileName,
                    s.totalRows != null ? `rows:${s.totalRows}` : null,
                    s.createdAt ? new Date(s.createdAt).toLocaleString() : null
                ].filter(Boolean).join(" | ");

                sel.appendChild(opt);
            });

            // keep current selection if still exists
            if (prevValue) {
                const exists = Array.from(sel.options).some(o => o.value === prevValue);
                if (exists) sel.value = prevValue;
            }

        } catch (e) {
            console.warn("Fetch sessions failed", e);
        }
    }

    /* ================= FETCH GRAPH (SAFE) ================= */

/* ================= FETCH GRAPH (ENTERPRISE SAFE) ================= */

async function fetchGraph(sessionId = null, options = {}) {

    const {
        force = false,
        // Auto-refresh should not abort in-flight requests; large graphs can take > interval.
        skipIfBusy = false,
        allowAbort = true
    } = options;

    // Chuẩn hóa sessionId
    currentSessionId = sessionId ? String(sessionId) : "ALL";

    // Khi chuyá»ƒn session -> cáº§n fit láº¡i cho graph má»›i
    if (currentSessionId !== lastRenderedSessionId) {
        didInitialFit = false;
        lastRenderedSessionId = currentSessionId;
    }

    if (skipIfBusy && isFetchingGraph) return;

    // Abort request cũ (nếu có) và tạo controller mới cho request hiện tại
    if (allowAbort && abortController) abortController.abort();
    abortController = new AbortController();

    // Nếu ALL mode -> gọi endpoint trả toàn bộ graph
    if (currentSessionId === "ALL") {
        const url = "/admin/graph";

        try {
            showLoading(true);
            isFetchingGraph = true;
            const res = await fetch(url, { signal: abortController.signal, credentials: 'include' });
            if (!res.ok) throw new Error(`Graph API failed: ${res.status}`);
            const data = await res.json();
            allNodes = Array.isArray(data.nodes) ? data.nodes : [];
            allLinks = normalizeLinks(data.links);
            const signature = graphSignature(allNodes, allLinks);
            if (!force && graphSignatureCache.get(currentSessionId) === signature) {
                return;
            }
            if (force && graphSignatureCache.get(currentSessionId) === signature) {
                return;
            }
            graphSignatureCache.set(currentSessionId, signature);
            markNewNodes();

            graphCache.set(currentSessionId, {
                nodes: [...allNodes],
                links: [...allLinks]
            });

            renderByView();
            return;
        } catch (e) {
            if (e.name !== "AbortError") {
                console.error("Graph load error (ALL):", e);
                alert("Không tải được graph cho chế độ ALL");
            }
            return;
        } finally {
            isFetchingGraph = false;
            showLoading(false);
        }
    }

    // Cache hit
    if (!force && graphCache.has(currentSessionId)) {
        const cached = graphCache.get(currentSessionId);
        allNodes = [...cached.nodes];
        allLinks = [...cached.links];
        renderByView();
        return;
    }

    showLoading(true);
    isFetchingGraph = true;

    try {

        const url = "/admin/graph/" + encodeURIComponent(currentSessionId);

        const res = await fetch(url, {
            signal: abortController.signal,
            credentials: 'include'
        });

        if (!res.ok)
            throw new Error(`Graph API failed: ${res.status}`);

        const data = await res.json();

        allNodes = Array.isArray(data.nodes) ? data.nodes : [];
        allLinks = normalizeLinks(data.links);
        const signature = graphSignature(allNodes, allLinks);
        if (graphSignatureCache.get(currentSessionId) === signature) {
            return;
        }
        graphSignatureCache.set(currentSessionId, signature);
        markNewNodes();

        graphCache.set(currentSessionId, {
            nodes: [...allNodes],
            links: [...allLinks]
        });

        renderByView();

    } catch (e) {

        if (e.name !== "AbortError") {
            console.error("Graph load error:", e);
            alert("Không tải được graph cho session này");
        }

    } finally {
        isFetchingGraph = false;
        showLoading(false);
    }
}

window.fetchGraph = fetchGraph;

    /* ================= AUTO REFRESH ================= */

    function startAutoRefresh() {
        if (autoRefreshTimer) return;
        autoRefreshTimer = setInterval(() => {
            autoRefreshTick++;
            const sid = currentSessionId && currentSessionId !== "ALL"
                ? currentSessionId
                : null;
            fetchGraph(sid, { force: true, skipIfBusy: true, allowAbort: false });

            // refresh session dropdown periodically (e.g. NETWORK_CAPTURE appears after tshark starts)
            if (autoRefreshTick % 10 === 0) fetchSessions();
        }, AUTO_REFRESH_MS);
    }

    function stopAutoRefresh() {
        if (!autoRefreshTimer) return;
        clearInterval(autoRefreshTimer);
        autoRefreshTimer = null;
    }

    function openNodeDetails(node) {
        if (!node) return;
        window.selectedNodeId = node.id || null;
        window.__graphContext = {
            ...(window.__graphContext || {}),
            selectedNodeId: node.id || null
        };

        if (document.getElementById("nodeInfo") && typeof window.enhancedShowNodeInfo === "function") {
            window.enhancedShowNodeInfo(node);
            return;
        }

        showNodeInfo(node);
    }
    window.openNodeDetails = openNodeDetails;

    /* ================= VIEW SWITCH ================= */

    const renderByView = debounce(() => {

        let nodes = allNodes;
        let links = allLinks;

        if (window.ADMIN_VIEW_TYPE !== "ALL") {
            nodes = nodes.filter(n => n.type === window.ADMIN_VIEW_TYPE);
            links = filterLinks(nodes, links);
        }

        renderGraph(nodes, links);
        renderNodeTable(nodes);
        renderSessionFinalTable(
            currentSessionId && currentSessionId !== "ALL"
                ? allNodes.filter(n => n.sessionId === currentSessionId)
                : allNodes
        );

    }, 150);

    /* ================= GRAPH ================= */

    function renderGraph(nodes, links) {
        links = visibleLinksForLayout(nodes, links);
        publishGraphContext(nodes, links);

        ensureLinkLabelToggle();
        if (zoomFitTimer) {
            clearTimeout(zoomFitTimer);
            zoomFitTimer = null;
        }

        const layoutKey = `${currentSessionId || "ALL"}::${window.ADMIN_VIEW_TYPE || "ALL"}`;
        const isNewLayoutContext = layoutKey !== lastLayoutKey;

        nodes.forEach(n => {
            n.domainAssignment = assignDomain(n);
        });

        const domainCounts = { safe: 0, suspicious: 0, fraud: 0 };
        nodes.forEach(n => {
            if (n.domainAssignment) {
                domainCounts[n.domainAssignment] = (domainCounts[n.domainAssignment] || 0) + 1;
            }
        });
        const domainVisualOffsets = { safe: 0, suspicious: 0, fraud: 0 };
        const balanceVisualNodes = [];
        nodes.forEach(n => {
            if (!n || isSessionNode(n)) return;
            if (isBalancedInfluenceNode(n)) {
                n._balanceVisualIndex = balanceVisualNodes.length;
                balanceVisualNodes.push(n);
                return;
            }
            const domain = n.domainAssignment || assignDomain(n);
            if (!domain) return;
            n._domainVisualIndex = domainVisualOffsets[domain]++;
            n._domainVisualCount = domainCounts[domain] || 1;
        });
        balanceVisualNodes.forEach(n => {
            n._balanceVisualCount = balanceVisualNodes.length || 1;
        });

        const radiusForCount = (count) => {
            const c = Math.max(0, count || 0);
            return 250 + (26 * Math.sqrt(c)) + (6.5 * Math.pow(c, 0.72));
        };
        DOMAIN_ORDER.forEach(domain => {
            domainRadii[domain] = radiusForCount(domainCounts[domain]);
        });

        const maxDomainRadius = Math.max(...DOMAIN_ORDER.map(domain => domainRadii[domain]));
        const domainGap = Math.max(160, maxDomainRadius * 0.34);
        const maxPairRadius = Math.max(
            domainRadii.safe + domainRadii.suspicious,
            domainRadii.safe + domainRadii.fraud,
            domainRadii.suspicious + domainRadii.fraud
        );
        const triangleArm = Math.max(
            maxDomainRadius + domainGap + 160,
            (maxPairRadius + domainGap) / Math.sqrt(3)
        );

        neutralSessionPos = {
            x: triangleArm + maxDomainRadius + 220,
            y: triangleArm + maxDomainRadius + 220
        };

        const pointOnTriangle = (angleDeg) => {
            const angle = angleDeg * Math.PI / 180;
            return {
                x: neutralSessionPos.x + Math.cos(angle) * triangleArm,
                y: neutralSessionPos.y + Math.sin(angle) * triangleArm
            };
        };
        const safePoint = pointOnTriangle(-90);
        const suspiciousPoint = pointOnTriangle(150);
        const fraudPoint = pointOnTriangle(30);

        domainPos = {
            safe: { x: safePoint.x, y: safePoint.y, color: DOMAIN_COLORS.safe, label: DOMAIN_LABELS.safe },
            suspicious: { x: suspiciousPoint.x, y: suspiciousPoint.y, color: DOMAIN_COLORS.suspicious, label: DOMAIN_LABELS.suspicious },
            fraud: { x: fraudPoint.x, y: fraudPoint.y, color: DOMAIN_COLORS.fraud, label: DOMAIN_LABELS.fraud }
        };

        const riskValue = (node) => Math.max(0, Math.min(100, Number(node?.riskScore) || 0));
        const clamp01 = (v) => Math.max(0, Math.min(1, v));
        const domainStrength = (node) => {
            const domain = node?.domainAssignment || assignDomain(node);
            const backendDistance = Number(node?.domainDistances?.[domain]);
            if (Number.isFinite(backendDistance)) {
                return clamp01(1 - (backendDistance / VISUAL_DOMAIN_BOUNDARY_DISTANCE));
            }

            const risk = riskValue(node);

            if (domain === "safe") {
                return clamp01(1 - (risk / 33));
            }
            if (domain === "fraud") {
                return clamp01((risk - 67) / 33);
            }
            if (domain === "suspicious") {
                return clamp01(1 - (Math.abs(risk - 50) / 17));
            }
            return 0;
        };
        const stableAngle = (node) => {
            const raw = String(node?.id || node?.value || "");
            let hash = 0;
            for (let i = 0; i < raw.length; i++) {
                hash = ((hash << 5) - hash + raw.charCodeAt(i)) | 0;
            }
            const normalized = Math.abs(hash % 360);
            return normalized * Math.PI / 180;
        };
        const influenceBalanceTarget = (node) => {
            const weights = node?.domainInfluence || node?.softMemberships || {};
            let total = 0;
            let x = 0;
            let y = 0;

            DOMAIN_ORDER.forEach(domain => {
                const pos = domainPos[domain];
                const weight = Math.max(0, Number(weights[domain]) || 0);
                if (!pos || weight <= 0) return;
                total += weight;
                x += pos.x * weight;
                y += pos.y * weight;
            });

            if (total <= 0) {
                return { x: neutralSessionPos.x, y: neutralSessionPos.y };
            }

            const index = Number.isFinite(node?._balanceVisualIndex) ? node._balanceVisualIndex : 0;
            const count = Math.max(1, Number(node?._balanceVisualCount) || 1);
            const angle = stableAngle(node) + (index * Math.PI * (3 - Math.sqrt(5)));
            const spread = Math.min(
                Math.max(44, NODE_PACKING_STEP * Math.sqrt(count) * 0.42),
                maxDomainRadius * 0.22
            );
            const radius = Math.min(spread, 18 + NODE_PACKING_STEP * Math.sqrt(index + 0.5) * 0.40);
            return {
                x: (x / total) + Math.cos(angle) * radius,
                y: (y / total) + Math.sin(angle) * radius
            };
        };
        const domainTargetForNode = (node, fallbackIndex = 0) => {
            if (isBalancedInfluenceNode(node)) {
                return influenceBalanceTarget(node);
            }
            const domain = node?.domainAssignment || assignDomain(node);
            const pos = domainPos[domain];
            const radius = domainRadii[domain];
            if (!domain || !pos || !radius) {
                return { x: neutralSessionPos.x, y: neutralSessionPos.y };
            }

            const strength = domainStrength(node);
            const usableRadius = Math.max(20, radius - nodeRadius - 30);
            const minRadius = 12;
            const baseRadialDistance = minRadius + (1 - strength) * (usableRadius - minRadius);
            const index = Number.isFinite(node?._domainVisualIndex) ? node._domainVisualIndex : fallbackIndex;
            const count = Math.max(1, Number(node?._domainVisualCount) || domainCounts[domain] || 1);
            const packedRadialDistance = NODE_PACKING_STEP * Math.sqrt(index + 0.5) * 0.72;
            const maxPackingRadius = Math.max(minRadius, usableRadius - NODE_PACKING_STEP * 0.25);
            const radialDistance = Math.min(
                usableRadius,
                Math.max(baseRadialDistance, Math.min(maxPackingRadius, packedRadialDistance))
            );
            const angle = stableAngle(node)
                + (index * Math.PI * (3 - Math.sqrt(5)))
                + ((index % Math.max(1, Math.ceil(Math.sqrt(count)))) * 0.035);

            return {
                x: pos.x + Math.cos(angle) * radialDistance,
                y: pos.y + Math.sin(angle) * radialDistance
            };
        };

        if (!graphInitialized) {
            // container group for zoom/pan
            container = svg.append("g").attr("class", "graph-container");

            // zoom behavior
            zoom = d3.zoom()
                .scaleExtent([0.03, 4])
                .on("zoom", (event) => {
                    container.attr("transform", event.transform);
                });

            svg.call(zoom).on("dblclick.zoom", null);

            domainCircles = container.append("g").attr("class", "domains");
            DOMAIN_ORDER.forEach(domain => {
                const domainGroup = domainCircles.append("g")
                    .attr("class", `domain-group domain-${domain}`);

                const circleEl = domainGroup.append("circle")
                    .attr("fill", DOMAIN_COLORS[domain])
                    .attr("fill-opacity", 0.08)
                    .attr("stroke", DOMAIN_COLORS[domain])
                    .attr("stroke-width", 2)
                    .attr("stroke-opacity", 0.5);

                const labelEl = domainGroup.append("text")
                    .attr("text-anchor", "middle")
                    .attr("font-size", "14px")
                    .attr("font-weight", "700")
                    .attr("fill", DOMAIN_COLORS[domain])
                    .attr("fill-opacity", 0.7)
                    .attr("pointer-events", "none")
                    .text(DOMAIN_LABELS[domain]);

                const centerMarkEl = domainGroup.append("circle")
                    .attr("r", 7)
                    .attr("fill", DOMAIN_COLORS[domain])
                    .attr("stroke", "#ffffff")
                    .attr("stroke-width", 2)
                    .attr("pointer-events", "none");

                const centerTextEl = domainGroup.append("text")
                    .attr("text-anchor", "middle")
                    .attr("font-size", "10px")
                    .attr("font-weight", "800")
                    .attr("fill", DOMAIN_COLORS[domain])
                    .attr("pointer-events", "none")
                    .text("CENTER");

                domainCircleElements[domain] = {
                    group: domainGroup,
                    circle: circleEl,
                    label: labelEl,
                    centerMark: centerMarkEl,
                    centerText: centerTextEl
                };
            });

            simulation = d3.forceSimulation()
                .force("link", d3.forceLink().id(d => d.id).distance(l => {
                    const sourceNode = allNodes.find(n => n.id === safeId(l.source));
                    const targetNode = allNodes.find(n => n.id === safeId(l.target));
                    const sourceDomain = sourceNode?.domainAssignment || "neutral";
                    const targetDomain = targetNode?.domainAssignment || "neutral";
                    if (isSessionNode(sourceNode) || isSessionNode(targetNode)) {
                        const entityDomain = isSessionNode(sourceNode) ? targetDomain : sourceDomain;
                        return Math.max(140, (domainRadii[entityDomain] || 180) * 0.62);
                    }
                    return sourceDomain !== targetDomain ? 165 : 82;
                }).strength(l => {
                    const sourceNode = allNodes.find(n => n.id === safeId(l.source));
                    const targetNode = allNodes.find(n => n.id === safeId(l.target));
                    if (isSessionNode(sourceNode) || isSessionNode(targetNode)) return 0.018;
                    const sourceDomain = sourceNode?.domainAssignment || "neutral";
                    const targetDomain = targetNode?.domainAssignment || "neutral";
                    return sourceDomain !== targetDomain ? 0.012 : 0.045;
                }))
                .force("charge", d3.forceManyBody().strength(-340))
                .force("collision", d3.forceCollide().radius(nodeRadius + MIN_NODE_GAP + 4).strength(0.95).iterations(8))
                .force("cluster", () => {
                    (simulation.nodes() || []).forEach(node => {
                        if (isSessionNode(node)) {
                            const dx = neutralSessionPos.x - node.x;
                            const dy = neutralSessionPos.y - node.y;
                            const k = 0.12;
                            node.vx += dx * k;
                            node.vy += dy * k;
                            return;
                        }
                        const target = domainTargetForNode(node);
                        const dx = target.x - node.x;
                        const dy = target.y - node.y;
                        const k = 0.045;
                        node.vx += dx * k;
                        node.vy += dy * k;
                    });
                })
                .force("boundary", () => {
                    (simulation.nodes() || []).forEach(node => {
                        if (isBalancedInfluenceNode(node)) return;
                        const domain = node.domainAssignment || assignDomain(node);
                        const pos = domainPos[domain];
                        const radius = domainRadii[domain];
                        if (!pos || !radius) return;
                        const dx = node.x - pos.x;
                        const dy = node.y - pos.y;
                        const dist = Math.sqrt(dx * dx + dy * dy);
                        const maxDist = radius - nodeRadius - 12;
                        
                        if (dist > maxDist) {
                            const ratio = maxDist / Math.max(dist, 0.001);
                            node.vx += (pos.x + dx * ratio - node.x) * 0.75;
                            node.vy += (pos.y + dy * ratio - node.y) * 0.75;
                        }
                    });
                });

            centerDistanceLinkSel = container.append("g").attr("class", "center-distance-links").selectAll("line");
            linkSel = container.append("g").selectAll("line");
            centerDistanceLabelSel = container.append("g").attr("class", "center-distance-labels").selectAll("text");
            linkLabelSel = container.append("g").attr("class", "link-labels").selectAll("text");
            linkDistanceLabelSel = container.append("g").attr("class", "link-distance-labels").selectAll("text");
            nodeSel = container.append("g").selectAll("circle");
            labelSel = container.append("g").selectAll("text");

            const clampNodeInsideDomain = (node) => {
                if (!node || isSessionNode(node)) return;
                if (isBalancedInfluenceNode(node)) return;

                const domain = node.domainAssignment || assignDomain(node);
                const pos = domainPos[domain];
                const radius = domainRadii[domain];
                if (!pos || !radius) return;

                const dx = node.x - pos.x;
                const dy = node.y - pos.y;
                const dist = Math.sqrt(dx * dx + dy * dy);
                const maxDist = Math.max(12, radius - nodeRadius - 14);
                if (dist <= maxDist) return;

                const ratio = maxDist / Math.max(dist, 0.001);
                node.x = pos.x + dx * ratio;
                node.y = pos.y + dy * ratio;
                node.vx *= 0.15;
                node.vy *= 0.15;
            };

            const separateOverlappingNodes = () => {
                const activeNodes = (simulation.nodes() || [])
                    .filter(node => node && !isSessionNode(node));
                const minDistance = (nodeRadius * 2) + MIN_NODE_GAP + 6;

                for (let i = 0; i < activeNodes.length; i++) {
                    const a = activeNodes[i];

                    for (let j = i + 1; j < activeNodes.length; j++) {
                        const b = activeNodes[j];

                        if ((a.domainAssignment || assignDomain(a)) !== (b.domainAssignment || assignDomain(b))) {
                            continue;
                        }

                        let dx = b.x - a.x;
                        let dy = b.y - a.y;
                        let distance = Math.sqrt(dx * dx + dy * dy);

                        if (distance >= minDistance) {
                            continue;
                        }

                        if (distance < 0.001) {
                            const angle = ((i + j) % 24) / 24 * Math.PI * 2;
                            dx = Math.cos(angle);
                            dy = Math.sin(angle);
                            distance = 1;
                        }

                        const push = (minDistance - distance) * 0.72;
                        const ux = dx / distance;
                        const uy = dy / distance;

                        a.x -= ux * push;
                        a.y -= uy * push;
                        b.x += ux * push;
                        b.y += uy * push;

                        a.vx -= ux * push * 0.02;
                        a.vy -= uy * push * 0.02;
                        b.vx += ux * push * 0.02;
                        b.vy += uy * push * 0.02;
                    }
                }
            };

            renderGraphPositions = () => {
                separateOverlappingNodes();
                (simulation.nodes() || []).forEach(clampNodeInsideDomain);

                nodeSel.attr("cx", d => d.x)
                    .attr("cy", d => d.y);

                labelSel.attr("x", d => d.x)
                    .attr("y", d => d.y - 22);

                linkSel.attr("x1", d => d.source.x)
                    .attr("y1", d => d.source.y)
                    .attr("x2", d => d.target.x)
                    .attr("y2", d => d.target.y);

                linkLabelSel
                    .attr("x", d => (d.source.x + d.target.x) / 2)
                    .attr("y", d => ((d.source.y + d.target.y) / 2) - 8);

                if (linkDistanceLabelSel) {
                    linkDistanceLabelSel
                        .attr("x", d => (d.source.x + d.target.x) / 2)
                        .attr("y", d => ((d.source.y + d.target.y) / 2) + 8);
                }

                if (centerDistanceLinkSel) {
                    centerDistanceLinkSel
                        .attr("x1", d => domainPos[d.domain]?.x || d.node.x)
                        .attr("y1", d => domainPos[d.domain]?.y || d.node.y)
                        .attr("x2", d => d.node.x)
                        .attr("y2", d => d.node.y);
                }

                if (centerDistanceLabelSel) {
                    centerDistanceLabelSel
                        .attr("x", d => {
                            const pos = domainPos[d.domain];
                            return pos ? (pos.x + d.node.x) / 2 : d.node.x;
                        })
                        .attr("y", d => {
                            const pos = domainPos[d.domain];
                            return pos ? (pos.y + d.node.y) / 2 : d.node.y;
                        });
                }
            };

            simulation.on("tick", renderGraphPositions);

            svg.on("dblclick", () => {
                svg.transition().duration(400).call(zoom.transform, d3.zoomIdentity);
            });

            graphInitialized = true;
        }

        DOMAIN_ORDER.forEach(domain => {
            const pos = domainPos[domain];
            const radius = domainRadii[domain];
            const elements = domainCircleElements[domain];
            if (!elements) return;

            elements.circle
                .attr("cx", pos.x)
                .attr("cy", pos.y)
                .attr("r", radius);

            elements.label
                .attr("x", pos.x)
                .attr("y", pos.y - radius - 14)
                .text(`${DOMAIN_LABELS[domain]} (${domainCounts[domain] || 0})`);

            elements.centerMark
                .attr("cx", pos.x)
                .attr("cy", pos.y);

            elements.centerText
                .attr("x", pos.x)
                .attr("y", pos.y + 22);
        });

        const prevPos = new Map();
        (simulation.nodes() || []).forEach(n => {
            prevPos.set(n.id, {
                x: n.x, y: n.y, vx: n.vx, vy: n.vy,
                fx: n.fx, fy: n.fy
            });
        });

        const reusedCount = nodes.reduce((c, n) => c + (prevPos.has(n.id) ? 1 : 0), 0);
        const reusedRatio = reusedCount / Math.max(1, nodes.length);
        let isFreshGraph = isNewLayoutContext || prevPos.size === 0 || reusedRatio < 0.25;
        let isAdditive = !isFreshGraph && nodes.some(n => !prevPos.has(n.id));

        const isAlignedWithDomain = n => {
            if (!n || isSessionNode(n) || isBalancedInfluenceNode(n)) return true;
            const domain = n.domainAssignment || assignDomain(n);
            const pos = domainPos[domain];
            const radius = domainRadii[domain];
            if (!pos || !radius || !Number.isFinite(n.x) || !Number.isFinite(n.y)) return false;
            const dx = n.x - pos.x;
            const dy = n.y - pos.y;
            return Math.sqrt(dx * dx + dy * dy) <= radius * 1.18;
        };
        if (!isFreshGraph && nodes.some(n => !isAlignedWithDomain(n))) {
            isFreshGraph = true;
            isAdditive = false;
        }

        let cx = width / 2;
        let cy = height / 2;
        if (!isFreshGraph && prevPos.size > 0) {
            let sx = 0, sy = 0;
            prevPos.forEach(p => { sx += p.x || 0; sy += p.y || 0; });
            cx = sx / prevPos.size;
            cy = sy / prevPos.size;
        }

        if (isFreshGraph) {
            // Nhiá»u node má»›i (vd: upload Excel / Ä‘á»•i session) -> seed vá»‹ trĂ­ theo spiral Ä‘á»ƒ khĂ´ng chĂ´ng lĂªn nhau
            const goldenAngle = Math.PI * (3 - Math.sqrt(5));
            nodes.forEach((n) => {
                n.fx = undefined;
                n.fy = undefined;
                const domain = n.domainAssignment || assignDomain(n);
                if (!domain) {
                    n.x = neutralSessionPos.x;
                    n.y = neutralSessionPos.y;
                    n.vx = 0;
                    n.vy = 0;
                    return;
                }
                const index = Number.isFinite(n._domainVisualIndex) ? n._domainVisualIndex : 0;
                const target = domainTargetForNode(n, index);
                const jitter = Math.min(18, 5 + Math.sqrt(index) * 1.2);
                const a = index * goldenAngle;
                n.x = target.x + jitter * Math.cos(a);
                n.y = target.y + jitter * Math.sin(a);
                n.vx = 0;
                n.vy = 0;
            });
        } else {
            const jitter = 80;
            const newOnes = [];
            nodes.forEach(n => {
                const p = prevPos.get(n.id);
                if (p) {
                    n.x = p.x;
                    n.y = p.y;
                    n.vx = p.vx;
                    n.vy = p.vy;
                    if (p.fx != null) n.fx = p.fx;
                    if (p.fy != null) n.fy = p.fy;
                } else {
                    const domain = n.domainAssignment || assignDomain(n);
                    if (!domain) {
                        n.x = neutralSessionPos.x + (Math.random() - 0.5) * 40;
                        n.y = neutralSessionPos.y + (Math.random() - 0.5) * 30;
                        newOnes.push(n);
                        return;
                    }
                    const target = domainTargetForNode(n);
                    n.x = target.x + (Math.random() - 0.5) * jitter;
                    n.y = target.y + (Math.random() - 0.5) * jitter;
                    newOnes.push(n);
                }
            });

            // Khi chỉ thêm vài node mới (vd: dữ liệu tshark/ingest) -> đặt theo vòng tròn quanh tâm để tránh đè lên nhau
            if (newOnes.length > 1) {
                const groupedNewNodes = { safe: [], suspicious: [], fraud: [] };
                newOnes.forEach(n => {
                    if (isBalancedInfluenceNode(n)) return;
                    const domain = n.domainAssignment || assignDomain(n);
                    if (domain) {
                        groupedNewNodes[domain].push(n);
                    }
                });
                DOMAIN_ORDER.forEach(domain => {
                    const list = groupedNewNodes[domain];
                    list.forEach((n, i) => {
                        const target = domainTargetForNode(n, i);
                        const radius = Math.max(26, Math.min(domainRadii[domain] * 0.16, 86));
                        const a = (i / Math.max(1, list.length)) * Math.PI * 2;
                        n.x = target.x + radius * Math.cos(a);
                        n.y = target.y + radius * Math.sin(a);
                    });
                });
            }
        }

        const linkKey = l => `${safeId(l.source)}->${safeId(l.target)}::${l.type || ""}`;
        const visibleNodeMap = new Map((nodes || []).map(n => [n.id, n]));
        const denseGraph = nodes.length > DETAIL_LABEL_NODE_LIMIT || links.length > DETAIL_LABEL_LINK_LIMIT;
        const centerDistanceNodes = nodes
            .filter(n => !isSessionNode(n))
            .flatMap(n => {
                if (denseGraph && !isOverlapNode(n) && n.id !== selectedNodeId) {
                    return [];
                }
                if (isOverlapNode(n) || isBalancedInfluenceNode(n)) {
                    return DOMAIN_ORDER.map(domain => ({ id: `${n.id}::${domain}`, node: n, domain }));
                }
                const domain = n.domainAssignment || assignDomain(n);
                return domain ? [{ id: n.id, node: n, domain }] : [];
            });

        if (centerDistanceLinkSel) {
            centerDistanceLinkSel = centerDistanceLinkSel.data(centerDistanceNodes, d => d.id);
            centerDistanceLinkSel.exit().remove();
            const centerDistanceLinkEnter = centerDistanceLinkSel.enter().append("line");
            centerDistanceLinkSel = centerDistanceLinkEnter.merge(centerDistanceLinkSel)
                .attr("stroke", d => DOMAIN_COLORS[d.domain] || "#94a3b8")
                .attr("stroke-width", d => denseGraph ? 0.7 : 1.15)
                .attr("stroke-opacity", d => denseGraph ? (isOverlapNode(d.node) ? 0.28 : 0.10) : (isOverlapNode(d.node) ? 0.68 : 0.45))
                .attr("stroke-dasharray", "4 4");
        }

        if (centerDistanceLabelSel) {
            centerDistanceLabelSel = centerDistanceLabelSel.data(centerDistanceNodes, d => d.id);
            centerDistanceLabelSel.exit().remove();
            const centerDistanceLabelEnter = centerDistanceLabelSel.enter().append("text");
            centerDistanceLabelSel = centerDistanceLabelEnter.merge(centerDistanceLabelSel)
                .attr("text-anchor", "middle")
                .attr("dy", "-0.35em")
                .style("display", d => denseGraph && d.node?.id !== selectedNodeId ? "none" : null)
                .text(d => {
                    const value = d.node?.domainDistances?.[d.domain];
                    return value === undefined ? distanceLabel(d.node) : `${d.domain}: d=${formatNumber(value)}`;
                });
        }

        linkSel = linkSel.data(links, linkKey);
        linkSel.exit().remove();
        const linkEnter = linkSel.enter().append("line");
        linkSel = linkEnter.merge(linkSel)
            .attr("stroke", d => ({
                OVERLAP: "#7c3aed",
                SENT_FROM_IP: "#c62828",
                CONTAINS_URL: "#f9a825",
                HOSTED_ON: "#2e7d32",
                HOSTED_ON_DOMAIN: "#2e7d32",
                RESOLVES_TO: "#6d4c41",
                DOWNLOADS: "#5e35b1",
                HAS_HASH: "#6a1b9a",
                RECEIVED: "#546e7a",
                CONNECTS_TO: "#00838f"
            }[d.type] || "#aaa"))
            .attr("stroke-width", d => linkStrokeWidth(d))
            .attr("opacity", d => linkOpacity(d));

        linkLabelSel = linkLabelSel.data(links, linkKey);
        linkLabelSel.exit().remove();
        const linkLabelEnter = linkLabelSel.enter().append("text")
            .style("display", "none")
            .text(d => linkTypeLabel(d.type, d));
        linkLabelSel = linkLabelEnter.merge(linkLabelSel)
            .style("display", denseGraph && !showLinkLabelsAlways ? "none" : null)
            .text(d => linkTypeLabel(d.type, d));

        linkDistanceLabelSel = linkDistanceLabelSel.data(links, linkKey);
        linkDistanceLabelSel.exit().remove();
        const linkDistanceLabelEnter = linkDistanceLabelSel.enter().append("text")
            .style("display", "none")
            .text(d => nodeDistanceLabel(d, visibleNodeMap));
        linkDistanceLabelSel = linkDistanceLabelEnter.merge(linkDistanceLabelSel)
            .style("display", denseGraph ? "none" : (showLinkLabelsAlways ? null : "none"))
            .text(d => nodeDistanceLabel(d, visibleNodeMap));

        nodeSel = nodeSel.data(nodes, d => d.id);
        nodeSel.exit().remove();
        const nodeEnter = nodeSel.enter().append("circle")
            .attr("r", nodeRadius)
            .on("click", (event, d) => {
                event?.stopPropagation?.();
                selectedNodeId = d?.id || null;
                applyRelationHighlight(selectedNodeId);
                openNodeDetails(d);
            })
            .call(d3.drag()
                .on("start", () => {
                    if (simulation) {
                        simulation.alphaTarget(0).alpha(0).stop();
                    }
                })
                .on("drag", (e, d) => {
                    const t = d3.zoomTransform(svg.node());
                    let newX = (e.x - t.x) / t.k;
                    let newY = (e.y - t.y) / t.k;
                    
                    // Keep node inside its domain circle when dragging
                    if (isBalancedInfluenceNode(d)) {
                        d.x = newX;
                        d.y = newY;
                        d.fx = newX;
                        d.fy = newY;
                        if (renderGraphPositions) {
                            renderGraphPositions();
                        }
                        return;
                    }
                    const domain = d.domainAssignment || assignDomain(d);
                    if (!domain) {
                        d.fx = newX;
                        d.fy = newY;
                        return;
                    }
                    const pos = domainPos[domain];
                    const radius = domainRadii[domain];
                    const dx = newX - pos.x;
                    const dy = newY - pos.y;
                    const dist = Math.sqrt(dx * dx + dy * dy);
                    const maxDist = radius - nodeRadius - 5;
                    
                    if (dist > maxDist) {
                        const ratio = maxDist / Math.max(dist, 0.001);
                        newX = pos.x + dx * ratio;
                        newY = pos.y + dy * ratio;
                    }
                    
                    d.x = newX;
                    d.y = newY;
                    d.fx = newX;
                    d.fy = newY;
                    if (renderGraphPositions) {
                        renderGraphPositions();
                    }
                })
                .on("end", () => {
                    if (simulation) {
                        simulation.alphaTarget(0).alpha(0).stop();
                    }
                })
            );

        nodeSel = nodeEnter.merge(nodeSel)
            .attr("fill", d => nodeFill(d))
            .attr("stroke", d => nodeStroke(d))
            .attr("stroke-width", d => nodeStrokeWidth(d))
            .attr("stroke-dasharray", d => isBalancedInfluenceNode(d) ? "2 2" : (d.membershipStatus === "OUTSIDE" ? "4 3" : null))
            .attr("r", d => {
                const fresh = d._newAt && (Date.now() - d._newAt) < 6000;
                return nodeRadius;
            });

        labelSel = labelSel.data(nodes, d => d.id);
        labelSel.exit().remove();
        const labelEnter = labelSel.enter().append("text")
            .attr("dy", -22)
            .attr("text-anchor", "middle")
            .style("font-size", "11px");
        labelSel = labelEnter.merge(labelSel)
            .style("font-size", nodes.length > 140 ? "9px" : "11px")
            .text(d => {
                if (nodes.length <= 140) return d.value;
                const shouldShow =
                    isSessionNode(d) ||
                    d.id === selectedNodeId ||
                    d._isNew ||
                    d.manualBlocked ||
                    d.riskLevel === "high" ||
                    d.riskLevel === "medium";
                if (!shouldShow) return "";
                const value = String(d.value || "");
                return value.length > 34 ? `${value.slice(0, 31)}...` : value;
            });

        simulation.nodes(nodes);
        simulation.force("link").links(links);
        simulation.alpha(isFreshGraph ? 1.2 : (isAdditive ? 0.9 : 0.5)).stop();

        const denseLayout = nodes.length > DENSE_GRAPH_NODE_THRESHOLD || links.length > DENSE_GRAPH_LINK_THRESHOLD;
        const staticTicks = denseLayout
            ? (isFreshGraph ? 38 : (isAdditive ? 26 : 12))
            : (isFreshGraph ? 90 : (isAdditive ? 60 : 25));
        for (let i = 0; i < staticTicks; i++) {
            simulation.tick();
        }

        if (renderGraphPositions) {
            const relaxPasses = denseLayout
                ? (isFreshGraph ? 7 : (isAdditive ? 5 : 3))
                : (isFreshGraph ? 18 : (isAdditive ? 12 : 8));
            for (let i = 0; i < relaxPasses; i++) {
                renderGraphPositions();
            }
        }
        nodes.forEach(n => {
            n.vx = 0;
            n.vy = 0;
            n.fx = n.x;
            n.fy = n.y;
        });

        simulation.alphaTarget(0).alpha(0).stop();
        lastLayoutKey = layoutKey;

        // Auto-lock sau khi layout dá»«ng Ä‘á»§ lĂ¢u (trĂ¡nh lock quĂ¡ sớm lĂ m node chĂ´ng lĂªn nhau)
        if (autoLockInterval) {
            clearInterval(autoLockInterval);
            autoLockInterval = null;
        }

        const lockStart = Date.now();
        const maxWaitMs = isFreshGraph ? 6000 : (isAdditive ? 4500 : 2500);
        autoLockInterval = setInterval(() => {
            if (!simulation) {
                clearInterval(autoLockInterval);
                autoLockInterval = null;
                return;
            }

            const elapsed = Date.now() - lockStart;
            const alpha = simulation.alpha();
            const shouldLock = alpha < 0.08 || elapsed > maxWaitMs;
            
            if (!shouldLock) return;

            clearInterval(autoLockInterval);
            autoLockInterval = null;

            // FORCE lock tất cả node vào vị trí hiện tại (không check null)
            nodes.forEach(n => {
                n.fx = n.x;
                n.fy = n.y;
            });
            
            // Tắt toàn bộ simulation
            simulation.alphaTarget(0).alpha(0);
            simulation.stop();
        }, 150);

        // Focus view on newest nodes (if any)
        const newNodes = nodes.filter(n => n._isNew);
        if (false && newNodes.length > 0) {
            setTimeout(() => {
                const t = d3.zoomTransform(svg.node());
                const nx = newNodes.reduce((s, n) => s + (n.x || 0), 0) / newNodes.length;
                const ny = newNodes.reduce((s, n) => s + (n.y || 0), 0) / newNodes.length;
                const tx = width / 2 - nx * t.k;
                const ty = height / 2 - ny * t.k;
                const transform = d3.zoomIdentity.translate(tx, ty).scale(t.k);
                svg.transition().duration(400).call(zoom.transform, transform);
            }, 350);

            // Auto-open node info for the newest domain/url
            const newest = newNodes
                .filter(n => n.type === "Domain" || n.type === "URL")
                .sort((a, b) => (b._newAt || 0) - (a._newAt || 0))[0];
            if (newest) {
                setTimeout(() => {
                    openNodeDetails(newest);
                }, 450);
            }
        }

        if (!didInitialFit) {
            const zoomToFit = () => {
                if (!nodes || nodes.length === 0) return;

                const xs = nodes.map(n => n.x);
                const ys = nodes.map(n => n.y);
                DOMAIN_ORDER.forEach(domain => {
                    const pos = domainPos[domain];
                    const radius = domainRadii[domain] || 0;
                    if (!pos) return;
                    xs.push(pos.x - radius, pos.x + radius);
                    ys.push(pos.y - radius - 30, pos.y + radius);
                });
                const minX = Math.min(...xs), maxX = Math.max(...xs);
                const minY = Math.min(...ys), maxY = Math.max(...ys);

                const padding = 40;
                const boxW = Math.max(1, maxX - minX);
                const boxH = Math.max(1, maxY - minY);

                const scale = Math.min(4, Math.max(0.03, Math.min(width / (boxW + padding), height / (boxH + padding))));

                const tx = (width - scale * (minX + maxX)) / 2;
                const ty = (height - scale * (minY + maxY)) / 2;

                const transform = d3.zoomIdentity.translate(tx, ty).scale(scale);
                svg.transition().duration(600).call(zoom.transform, transform);
            };

            zoomFitTimer = setTimeout(zoomToFit, 120);
            didInitialFit = true;
        }

        // re-apply highlighting after re-render
        applyRelationHighlight(selectedNodeId);
    }

    /* ================= SESSION TABLE ================= */

/* ================= SESSION TABLE (UPGRADED COLOR MATCH GRAPH) ================= */

function renderSessionFinalTable(nodes) {

    const tbody = document.querySelector("#finalTable tbody");
    if (!tbody) return;

    tbody.innerHTML = "";

    const sessions = {};

    nodes.forEach(n => {
        if (!n.sessionId) return;

        const s = sessions[n.sessionId] ||= {
            sessionId: n.sessionId,
            emails: [],
            ips: [],
            urls: [],
            maxRisk: "low",
            maxScore: 0
        };

const normalizedRisk = String(n.riskLevel || "low")
                            .trim()
                            .toLowerCase();

const entry = {
    value: n.value,
    risk: normalizedRisk
};

        if (n.type === "Email") s.emails.push(entry);
        if (n.type === "IPAddress") s.ips.push(entry);
        if (n.type === "URL") s.urls.push(entry);

if (riskRank(normalizedRisk) > riskRank(s.maxRisk))
    s.maxRisk = normalizedRisk;

        s.maxScore = Math.max(s.maxScore, Number(n.riskScore) || 0);
    });

    Object.values(sessions).forEach(s => {

        const tr = document.createElement("tr");

        const td = (html) => {
            const c = document.createElement("td");
            c.innerHTML = html;
            tr.appendChild(c);
        };

        td(s.sessionId);

        const buildColumn = (items) => {
            return items.map(i => {

                const risk = String(i.risk || "")
                                .trim()
                                .toLowerCase();

                let cls = "";

                if (risk === "high") cls = "node-fraud";
                else if (risk === "medium") cls = "node-suspicious";
                else if (risk === "low") cls = "node-safe";

                return `<span class="${cls}">${i.value}</span>`;
            }).join(", ");
        };

        td(buildColumn(s.emails));
        td(buildColumn(s.ips));
        td(buildColumn(s.urls));

        td(s.maxRisk);

        const scoreClass =
            s.maxScore >= 80 ? "score-high" :
            s.maxScore >= 50 ? "score-medium" :
            "score-low";

        td(`<span class="${scoreClass}">${s.maxScore}</span>`);

        td(
            s.maxRisk === "high" ? "BLOCK" :
            s.maxRisk === "medium" ? "ĐÁNG NGHI NGỜ" :
            "AN TOÀN"
        );

        tbody.appendChild(tr);
    });
}

    /* ================= NODE POPUP ================= */

    function directRelationCount(node) {
        if (!node?.id) return 0;
        return (allLinks || [])
            .filter(l => safeId(l.source) === node.id || safeId(l.target) === node.id)
            .length;
    }

    function adaptiveNearestK(candidateCount, relationCount = 0) {
        const n = Math.max(0, Number(candidateCount) || 0);
        if (n === 0) return 0;
        if (n <= NEAREST_K_MIN) return n;

        const dataScale = Math.sqrt(n);
        const relationBoost = Math.min(4, Math.sqrt(Math.max(0, relationCount)));
        return Math.min(
            n,
            NEAREST_K_MAX,
            Math.max(NEAREST_K_MIN, Math.round(dataScale + relationBoost))
        );
    }

    function buildNearestFeatureContext(node) {
        if (!node) {
            return { candidates: [], candidateCount: 0, relationCount: 0, k: 0, nearest: [] };
        }
        const candidates = (allNodes || [])
            .filter(n => n?.id && n.id !== node.id && !isSessionNode(n))
            .map(n => ({
                type: "FEATURE_DISTANCE",
                direction: "->",
                nodeId: n.id,
                otherLabel: `${n.type || "Node"}: ${nodeDisplayValue(n)}`,
                distanceValue: nodeFeatureDistance(node, n)
            }))
            .filter(n => Number.isFinite(n.distanceValue))
            .sort((a, b) => a.distanceValue - b.distanceValue);
        const relationCount = directRelationCount(node);
        const k = adaptiveNearestK(candidates.length, relationCount);
        const nearest = candidates.slice(0, k).map(n => ({
                ...n,
                distance: `d=${n.distanceValue.toFixed(2)}`
        }));
        return { candidates, candidateCount: candidates.length, relationCount, k, nearest };
    }

    function buildNearestFeatureNeighbors(node) {
        return buildNearestFeatureContext(node).nearest;
    }

    function buildNodeRelations(node, limit = null) {
        const nodeMap = new Map((allNodes || []).map(n => [n.id, n]));
        const relations = (allLinks || [])
            .filter(l => safeId(l.source) === node.id || safeId(l.target) === node.id)
            .map(l => {
                const sourceId = safeId(l.source);
                const targetId = safeId(l.target);
                const otherId = sourceId === node.id ? targetId : sourceId;
                const other = nodeMap.get(otherId);
                const direction = sourceId === node.id ? "→" : "←";
                const otherLabel = other
                    ? `${other.type || "Node"}: ${nodeDisplayValue(other)}`
                    : (otherId || "Unknown");
                return {
                    type: linkTypeLabel(l.type),
                    direction,
                    otherLabel,
                    distance: nodeDistanceLabel(l, nodeMap)
                };
            });
        return limit == null ? relations : relations.slice(0, limit);
    }

    function renderSideNodeDetails(node) {
        const panel = document.getElementById("nodeDetailsPanel");
        if (!panel || !node) return false;

        const typeEl = document.getElementById("panelNodeType");
        const body = panel.querySelector(".panel-body");
        if (!body) return false;

        const domain = node.domainAssignment || assignDomain(node);
        const centerDistance = distanceToDomainCenter(node);
        const indicators = Array.isArray(node.indicators)
            ? node.indicators.filter(Boolean)
            : (node.indicators ? [String(node.indicators)] : []);
        const relations = buildNodeRelations(node);
        const riskClass = riskClassName(node.riskLevel);
        const score = Number(node.riskScore);
        const scoreText = Number.isFinite(score) ? `${score}/100` : "—";
        const centerText = centerDistance == null ? "—" : `d=${centerDistance.toFixed(2)}`;

        if (typeEl) {
            typeEl.textContent = `${node.type || "Unknown"} · ${node.id || "—"}`;
        }

        const indicatorHtml = indicators.length
            ? indicators.map(ind => `<span class="detail-chip">${safeTextHtml(ind)}</span>`).join("")
            : '<span class="detail-muted">Không có chỉ báo rủi ro</span>';

        const relationsHtml = relations.length
            ? relations.map(rel => `
                <div class="detail-relation">
                    <span class="detail-relation-type">${safeTextHtml(rel.type)}</span>
                    <span>${safeTextHtml(rel.direction)} ${safeTextHtml(rel.otherLabel)}</span>
                    ${rel.distance ? `<b>${safeTextHtml(rel.distance)}</b>` : ""}
                </div>
            `).join("")
            : '<span class="detail-muted">Không có liên kết trong graph hiện tại</span>';

        body.innerHTML = `
            <div class="detail-hero ${riskClass}">
                <div>
                    <div class="detail-hero-type">${safeTextHtml(node.type || "Unknown")}</div>
                    <div class="detail-hero-value">${safeTextHtml(nodeDisplayValue(node))}</div>
                </div>
                <div class="detail-risk-pill ${riskClass}">${safeTextHtml(String(node.riskLevel || "low").toUpperCase())}</div>
            </div>

            <div class="detail-grid">
                <div class="detail-metric">
                    <span>Risk Score</span>
                    <b class="${riskClass}">${safeTextHtml(scoreText)}</b>
                </div>
                <div class="detail-metric">
                    <span>Verdict</span>
                    <b>${safeTextHtml(node.verdict || "—")}</b>
                </div>
                <div class="detail-metric">
                    <span>Status</span>
                    <b>${safeTextHtml(node.status || "—")}</b>
                </div>
                <div class="detail-metric">
                    <span>Miền</span>
                    <b>${safeTextHtml((domain || "—").toUpperCase())}</b>
                </div>
                <div class="detail-metric">
                    <span>Khoảng cách tới tâm miền</span>
                    <b>${safeTextHtml(centerText)}</b>
                </div>
                <div class="detail-metric">
                    <span>Manual block</span>
                    <b>${node.manualBlocked ? "YES" : "NO"}</b>
                </div>
            </div>

            <div class="detail-section">
                <div class="detail-section-title">Indicators</div>
                <div class="detail-chip-list">${indicatorHtml}</div>
            </div>

            <div class="detail-section">
                <div class="detail-section-title">Relations & khoảng cách node-node</div>
                <div class="detail-relation-list">${relationsHtml}</div>
            </div>

            <div class="detail-section">
                <div class="detail-section-title">Node ID</div>
                <code class="detail-code">${safeTextHtml(node.id || "—")}</code>
            </div>
        `;

        panel.classList.add("open");
        panel.setAttribute("aria-hidden", "false");
        renderGraphExplanation(node, relations);
        return true;
    }

    function renderGraphExplanation(node, relations = buildNodeRelations(node)) {
        const panel = document.getElementById("graphExplanationPanel");
        const body = document.getElementById("graphExplanationBody");
        if (!panel || !body || !node) return;

        const domain = node.domainAssignment || assignDomain(node) || "safe";
        const probs = regionProbabilities(node);
        const centerDistance = distanceToDomainCenter(node);
        const centerText = centerDistance == null ? "—" : `d=${centerDistance.toFixed(2)}`;
        const sortedRelations = [...relations].sort((a, b) => {
            const da = Number(String(a.distance || "").replace("d=", "")) || Number.MAX_VALUE;
            const db = Number(String(b.distance || "").replace("d=", "")) || Number.MAX_VALUE;
            return da - db;
        });
        const nearestContext = buildNearestFeatureContext(node);
        const candidateCount = nearestContext.candidateCount;
        const actualK = nearestContext.k;
        const nearest = nearestContext.nearest.length
            ? nearestContext.nearest
            : sortedRelations.slice(0, actualK);
        const probabilityRows = DOMAIN_ORDER.map(key => {
            const pct = probs[key] || 0;
            const active = key === domain ? "active" : "";
            const color = DOMAIN_COLORS[key] || "#2563eb";
            return `
                <div class="prob-row ${active}">
                    <div class="prob-label">
                        <span class="prob-dot ${key}"></span>
                        <b>${safeTextHtml(DOMAIN_LABELS[key] || key)}</b>
                    </div>
                    <div class="prob-bar ${key}">
                        <span style="width:${Math.max(2, Math.min(100, pct)).toFixed(2)}%; background:${color} !important;"></span>
                    </div>
                    <div class="prob-value">${pct.toFixed(2)}%</div>
                </div>
            `;
        }).join("");

        const relationRows = nearest.length
            ? nearest.map(rel => `
                <div class="explain-relation-row">
                    <span>${safeTextHtml(rel.type)} ${safeTextHtml(rel.direction)} ${safeTextHtml(rel.otherLabel)}</span>
                    <b>${safeTextHtml(rel.distance || "—")}</b>
                </div>
            `).join("")
            : '<div class="explain-muted">Node này chưa có liên kết trực tiếp trong graph hiện tại.</div>';

        const risk = riskValue(node);
        const reason = domain === "fraud"
            ? "Điểm rủi ro cao và các đặc trưng trạng thái kéo node về miền gian lận."
            : domain === "suspicious"
                ? "Các đặc trưng nằm giữa an toàn và gian lận nên node được xếp vào miền nghi ngờ."
                : "Điểm rủi ro thấp và trạng thái ít bất thường nên node nằm gần miền an toàn.";

        body.innerHTML = `
            <div class="explain-grid">
                <div class="explain-card">
                    <div class="explain-k">Node đang xét</div>
                    <div class="explain-v">${safeTextHtml(nodeDisplayValue(node))}</div>
                    <div class="explain-meta">${safeTextHtml(node.type || "Unknown")} · risk ${risk}/100</div>
                </div>
                <div class="explain-card">
                    <div class="explain-k">Miền được chọn</div>
                    <div class="explain-v ${riskClassName(node.riskLevel)}">${safeTextHtml(DOMAIN_LABELS[domain] || domain)}</div>
                    <div class="explain-meta">Khoảng cách tới tâm miền: ${safeTextHtml(centerText)}</div>
                </div>
                <div class="explain-card">
                    <div class="explain-k">Lý do chính</div>
                    <div class="explain-text">${safeTextHtml(reason)}</div>
                </div>
                <div class="explain-card">
                    <div class="explain-k">K node gan nhat</div>
                    <div class="explain-v">K = ${actualK}</div>
                    <div class="explain-meta">K thich ung theo N=${candidateCount} node ung vien va ${nearestContext.relationCount} lien ket truc tiep; gioi han ${NEAREST_K_MIN}-${NEAREST_K_MAX}. Highlight node dung theo danh sach K nay.</div>
                </div>
            </div>

            <div class="explain-section">
                <div class="explain-section-title">Xác suất thuộc từng miền</div>
                <div class="prob-list">${probabilityRows}</div>
            </div>

            <div class="explain-section">
                <div class="explain-section-title">Khoảng cách tới các node liên kết gần nhất</div>
                <div class="explain-relation-list">${relationRows}</div>
            </div>
        `;
        panel.classList.add("open");
    }

    function showNodeInfo(d) {

        const box = document.getElementById("nodeInfo");
        if (!box) {
            renderSideNodeDetails(d);
            return;
        }

        box.style.display = "flex";

        const riskClass = (d.riskLevel || "").toLowerCase().replace(/\s+/g, "-");
        const typeIcon = {"Email": "📧", "URL": "🔗", "IPAddress": "🌐"}[d.type] || "📋";
        const indicatorsList = (d.indicators || []).length > 0 
            ? d.indicators.map(ind => `<span class="node-popup-badge">${ind}</span>`).join("")
            : '<span class="node-popup-badge-empty">✓ No Issues</span>';

        // Relationship summary (within current graph view)
        const nodeMap = new Map((allNodes || []).map(n => [n.id, n]));
        const rels = (allLinks || [])
            .filter(l => safeId(l.source) === d.id || safeId(l.target) === d.id)
            .slice(0, 14)
            .map(l => {
                const s = safeId(l.source);
                const t = safeId(l.target);
                const otherId = s === d.id ? t : s;
                const other = nodeMap.get(otherId);
                const otherLabel = other ? `${other.type || ""}: ${other.value || otherId}` : otherId;
                const dir = s === d.id ? "→" : "←";
                const distance = nodeDistanceLabel(l, nodeMap);
                return `<span class="node-popup-badge">${safeTextHtml(linkTypeLabel(l.type))}${distance ? ` (${safeTextHtml(distance)})` : ""} ${dir} ${safeTextHtml(otherLabel)}</span>`;
            })
            .join("");

        box.innerHTML = `
            <div class="node-popup-premium ${riskClass}">
                <!-- Header -->
                <div class="node-popup-header">
                    <div class="node-popup-header-left">
                        <span class="node-popup-icon">${typeIcon}</span>
                        <div>
                            <div class="node-popup-type">${d.type || "Unknown"}</div>
                            <div class="node-popup-status">Status: <b>${d.status || "—"}</b></div>
                        </div>
                    </div>
                    <span class="node-popup-risk-badge node-popup-risk-${riskClass}">
                        ${(d.riskLevel || "Unknown").toUpperCase()}
                    </span>
                </div>

                <!-- Main Content Grid -->
                <div class="node-popup-content-grid">
                    <div class="node-popup-content-item">
                        <div class="node-popup-label">Value</div>
                        <div class="node-popup-value">${d.value || "—"}</div>
                    </div>
                    <div class="node-popup-content-item">
                        <div class="node-popup-label">Verdict</div>
                        <div class="node-popup-value node-popup-verdict-${(d.verdict || "").replace(/\s+/g, "_")}">${d.verdict || "—"}</div>
                    </div>
                    <div class="node-popup-content-item">
                        <div class="node-popup-label">Risk Score</div>
                        <div class="node-popup-value node-popup-score-display">
                            <span class="node-popup-score-num">${d.riskScore || "0"}</span>
                            <span class="node-popup-score-max">/100</span>
                        </div>
                    </div>
                </div>

                <!-- ID Section -->
                <div class="node-popup-id-section">
                    <div class="node-popup-label">Node ID</div>
                    <code class="node-popup-id-value">${d.id || "—"}</code>
                </div>

                <div class="node-popup-id-section">
                    <div class="node-popup-label">Overlap / Domain</div>
                    <div class="node-popup-value">
                        Membership: <b>${safeTextHtml(d.membershipStatus || "IN_REGION")}</b><br>
                        Influence zone: <b>${safeTextHtml(d.influenceZone || "-")}</b> ${d.bridgeNode ? "(bridge)" : ""}<br>
                        Community: <b>${safeTextHtml(d.communityId || "none")}</b> ${d.domainRole ? `(${safeTextHtml(d.domainRole)})` : ""}<br>
                        Multi-domain overlap: <b>${d.multiDomainOverlap ? "yes" : "no"}</b><br>
                        Domain distances: ${safeTextHtml(formatDomainDistances(d.domainDistances))}<br>
                        Soft membership: ${safeTextHtml(formatDomainDistances(d.softMemberships))}<br>
                        Domain influence: ${safeTextHtml(formatDomainDistances(d.domainInfluence))}<br>
                        Feature vector: ${safeTextHtml(formatDomainDistances(d.featureVector))}<br>
                        Overlap: raw=${formatNumber(d.overlapScore)}, weighted=${formatNumber(d.weightedOverlapScore)}, adjusted=${formatNumber(d.adjustedOverlapScore)}
                    </div>
                </div>

                ${d.manualBlocked ? `
                <div class="node-popup-id-section">
                    <div class="node-popup-label">Manual Block</div>
                    <div class="node-popup-value">
                        <b>YES</b>
                        ${d.manualBlockReason ? ` - ${d.manualBlockReason}` : ""}
                        ${d.manualBlockedBy ? ` (${d.manualBlockedBy})` : ""}
                    </div>
                </div>
                ` : ""}

                <!-- Indicators -->
                <div class="node-popup-indicators">
                    <div class="node-popup-label">Risk Indicators</div>
                    <div class="node-popup-indicators-list">
                        ${indicatorsList}
                    </div>
                </div>

                <!-- Relations -->
                <div class="node-popup-indicators">
                    <div class="node-popup-label">Liên kết (highlight trên đồ thị)</div>
                    <div class="node-popup-indicators-list">
                        ${rels || '<span class="node-popup-badge-empty">— Không có liên kết</span>'}
                    </div>
                </div>

                <!-- Actions -->
                <div class="node-popup-footer">
                    ${(String(d.type || "").toLowerCase() !== "analysissession")
                        ? `<button class="node-popup-btn-block" type="button">${d.manualBlocked ? "Bỏ chặn" : "Chặn"}</button>`
                        : ""}
                    <button class="node-popup-btn-close" type="button">Close</button>
                </div>
            </div>
        `;

        const closeBtn = box.querySelector(".node-popup-btn-close");
        if (closeBtn) {
            closeBtn.onclick = () => {
                box.style.display = "none";
                selectedNodeId = null;
                applyRelationHighlight(null);
            };
        }

        const blockBtn = box.querySelector(".node-popup-btn-block");
        if (blockBtn) {
            blockBtn.onclick = async () => {
                const id = d.id;
                if (!id) return;

                const isBlocked = !!d.manualBlocked;

                if (!isBlocked) {
                    const reason = prompt("Lý do chặn (tuỳ chọn):", d.manualBlockReason || "");
                    if (reason === null) return;

                    try {
                        const res = await fetch(`/admin/node/${encodeURIComponent(id)}/block`, {
                            method: "POST",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify({ reason })
                        });

                        const j = await res.json().catch(() => ({}));
                        if (!res.ok) throw new Error(j.message || `HTTP ${res.status}`);

                        if (j.node) updateNodeInCaches(id, () => j.node);
                        box.style.display = "none";
                        await fetchGraph(currentSessionId === "ALL" ? null : currentSessionId, { force: true });
                        alert("Chặn thành công");

                    } catch (e) {
                        console.error("Block node failed:", e);
                        alert("Chặn thất bại: " + (e.message || e));
                        await fetchGraph(currentSessionId === "ALL" ? null : currentSessionId, { force: true });
                    }

                    return;
                }

                if (!confirm("Bỏ chặn node này?")) return;

                try {
                    const res = await fetch(`/admin/node/${encodeURIComponent(id)}/unblock`, {
                        method: "POST"
                    });

                    const j = await res.json().catch(() => ({}));
                    if (!res.ok) throw new Error(j.message || `HTTP ${res.status}`);

                    if (j.node) updateNodeInCaches(id, () => j.node);
                    box.style.display = "none";
                    await fetchGraph(currentSessionId === "ALL" ? null : currentSessionId, { force: true });
                    alert("Bỏ chặn thành công");

                } catch (e) {
                    console.error("Unblock node failed:", e);
                    alert("Bỏ chặn thất bại: " + (e.message || e));
                    await fetchGraph(currentSessionId === "ALL" ? null : currentSessionId, { force: true });
                }
            };
        }
    }

    function applyRelationHighlight(nodeId) {
        if (!nodeSel || !linkSel) return;

        if (!nodeId) {
            nodeSel.classed("dim", false).classed("pulse", false).classed("rel-highlight", false);
            linkSel.classed("dim", false).classed("pulse", false).classed("rel-highlight", false);
            if (linkLabelSel) linkLabelSel.style("display", showLinkLabelsAlways ? null : "none").classed("dim", false);
            if (linkDistanceLabelSel) linkDistanceLabelSel.style("display", "none").classed("dim", false);
            if (centerDistanceLinkSel) centerDistanceLinkSel.classed("dim", false).classed("pulse", false);
            if (centerDistanceLabelSel) centerDistanceLabelSel.style("display", "none").classed("dim", false);
            return;
        }

        const connectedKeys = new Set();
        const neighborIds = new Set([nodeId]);
        const selectedNode = (allNodes || []).find(n => n?.id === nodeId);
        buildNearestFeatureNeighbors(selectedNode)
            .forEach(n => {
                if (n.nodeId) neighborIds.add(n.nodeId);
            });

        const linkKey = l => `${safeId(l.source)}->${safeId(l.target)}::${l.type || ""}`;
        (simulation?.force?.("link")?.links?.() || []).forEach(l => {
            const s = safeId(l.source);
            const t = safeId(l.target);
            if (s === nodeId || t === nodeId) {
                connectedKeys.add(linkKey(l));
            }
        });

        nodeSel
            .classed("dim", d => !neighborIds.has(d.id))
            .classed("pulse", d => d.id === nodeId)
            .classed("rel-highlight", d => neighborIds.has(d.id) && d.id !== nodeId);

        linkSel
            .classed("dim", d => !connectedKeys.has(linkKey(d)))
            .classed("pulse", d => connectedKeys.has(linkKey(d)))
            .classed("rel-highlight", d => connectedKeys.has(linkKey(d)));

        if (linkLabelSel) {
            linkLabelSel
                .classed("dim", d => !connectedKeys.has(linkKey(d)))
                .style("display", d => {
                    if (showLinkLabelsAlways) return null;
                    return connectedKeys.has(linkKey(d)) ? null : "none";
                });
        }

        if (linkDistanceLabelSel) {
            linkDistanceLabelSel
                .classed("dim", d => !connectedKeys.has(linkKey(d)))
                .style("display", d => {
                    if (showLinkLabelsAlways) return null;
                    return connectedKeys.has(linkKey(d)) ? null : "none";
                });
        }

        if (centerDistanceLinkSel) {
            centerDistanceLinkSel
                .classed("dim", d => d.node?.id !== nodeId)
                .classed("pulse", d => d.node?.id === nodeId);
        }

        if (centerDistanceLabelSel) {
            centerDistanceLabelSel
                .classed("dim", d => d.node?.id !== nodeId)
                .style("display", d => d.node?.id === nodeId ? null : "none");
        }
    }

    /* ================= NODE TABLE ================= */
    // Helpers to keep local cache in sync after edits/deletes
    function updateNodeInCaches(nodeId, updater) {
        // update allNodes
        const idx = allNodes.findIndex(x => x.id === nodeId);
        if (idx >= 0) {
            allNodes[idx] = { ...allNodes[idx], ...updater(allNodes[idx]) };
        }

        // update graphCache entries
        for (const [key, val] of graphCache.entries()) {
            const ni = val.nodes.findIndex(x => x.id === nodeId);
            if (ni >= 0) {
                val.nodes[ni] = { ...val.nodes[ni], ...updater(val.nodes[ni]) };
            }
        }
    }

    function removeNodeFromCaches(nodeId) {
        allNodes = allNodes.filter(x => x.id !== nodeId);
        allLinks = allLinks.filter(l => l.source !== nodeId && l.target !== nodeId);

        for (const [key, val] of graphCache.entries()) {
            val.nodes = val.nodes.filter(x => x.id !== nodeId);
            val.links = val.links.filter(l => l.source !== nodeId && l.target !== nodeId);
        }
    }

    function renderNodeTable(nodes) {
        const tbody = document.querySelector("#nodeTable tbody");
        if (!tbody) return;

        tbody.innerHTML = "";

        (nodes || []).forEach(n => {
            const tr = document.createElement("tr");

            // Normalize risk level (some nodes may have uppercase)
            const normalizedRisk = String(n.riskLevel || "low").trim().toLowerCase();

            // Apply row class to match graph coloring
            if (normalizedRisk === "high") tr.classList.add("node-fraud");
            else if (normalizedRisk === "medium") tr.classList.add("node-suspicious");
            else tr.classList.add("node-safe");

            const td = (txt, extraClass) => {
                const c = document.createElement("td");
                if (extraClass) c.classList.add(extraClass);
                if (typeof txt === "string") c.textContent = txt ?? "";
                else c.textContent = String(txt ?? "");
                tr.appendChild(c);
            };

            td(n.id);
            td(n.type);

            // value cell: also add a span with same class for inline highlighting
            const valueCell = document.createElement("td");
            const span = document.createElement("span");
            span.textContent = n.value ?? "";
            if (normalizedRisk === "high") span.classList.add("node-fraud");
            else if (normalizedRisk === "medium") span.classList.add("node-suspicious");
            else span.classList.add("node-safe");
            valueCell.appendChild(span);
            tr.appendChild(valueCell);

            td(n.status);

            // risk level cell: show normalized string and add class
            td(normalizedRisk, "risk-level");

            td(n.riskScore);
            td(n.verdict);
            td(Array.isArray(n.indicators) ? n.indicators.join(", ") : "");

            const act = document.createElement("td");

            const viewBtn = document.createElement("button");
            viewBtn.textContent = "Xem";
            viewBtn.onclick = () => {
                selectedNodeId = n.id;
                applyRelationHighlight(selectedNodeId);
                openNodeDetails(n);
            };
            act.appendChild(viewBtn);

            const editBtn = document.createElement("button");
            editBtn.textContent = "Sửa";
            editBtn.style.marginLeft = "6px";
            editBtn.onclick = () => openNodeEditModal(n);
            act.appendChild(editBtn);

            // Manual block / unblock (not for sessions)
            if (String(n.type || "").toLowerCase() !== "analysissession") {
                const blockBtn = document.createElement("button");
                blockBtn.textContent = n.manualBlocked ? "Bỏ chặn" : "Chặn";
                blockBtn.style.marginLeft = "6px";

                blockBtn.onclick = async () => {
                    const id = n.id;
                    if (!id) return;

                    const isBlocked = !!n.manualBlocked;

                    if (!isBlocked) {
                        const reason = prompt("Lý do chặn (tuỳ chọn):", n.manualBlockReason || "");
                        if (reason === null) return;

                        try {
                            const res = await fetch(`/admin/node/${encodeURIComponent(id)}/block`, {
                                method: "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify({ reason })
                            });

                            const j = await res.json().catch(() => ({}));
                            if (!res.ok) throw new Error(j.message || `HTTP ${res.status}`);

                            if (j.node) updateNodeInCaches(id, () => j.node);
                            await fetchGraph(currentSessionId === "ALL" ? null : currentSessionId, { force: true });
                            alert("Chặn thành công");

                        } catch (e) {
                            console.error("Block node failed:", e);
                            alert("Chặn thất bại: " + (e.message || e));
                            await fetchGraph(currentSessionId === "ALL" ? null : currentSessionId, { force: true });
                        }

                        return;
                    }

                    if (!confirm("Bỏ chặn node này?")) return;

                    try {
                        const res = await fetch(`/admin/node/${encodeURIComponent(id)}/unblock`, {
                            method: "POST"
                        });

                        const j = await res.json().catch(() => ({}));
                        if (!res.ok) throw new Error(j.message || `HTTP ${res.status}`);

                        if (j.node) updateNodeInCaches(id, () => j.node);
                        await fetchGraph(currentSessionId === "ALL" ? null : currentSessionId, { force: true });
                        alert("Bỏ chặn thành công");

                    } catch (e) {
                        console.error("Unblock node failed:", e);
                        alert("Bỏ chặn thất bại: " + (e.message || e));
                        await fetchGraph(currentSessionId === "ALL" ? null : currentSessionId, { force: true });
                    }
                };

                act.appendChild(blockBtn);
            }

            const delBtn = document.createElement("button");
            delBtn.textContent = "Xóa";
            delBtn.style.marginLeft = "6px";
            delBtn.onclick = async () => {
                if (!confirm("Bạn có chắc muốn xóa node này?")) return;

                // optimistic remove from local caches/UI
                const backupNodes = [...allNodes];
                const backupLinks = [...allLinks];
                removeNodeFromCaches(n.id);
                renderByView();

                try {
                    const res = await fetch(`/admin/node/${encodeURIComponent(n.id)}`, {
                        method: "DELETE"
                    });

                    const j = await res.json();
                    if (!res.ok) throw new Error(j.message || "Lỗi server");

                    // synchronize with server to be safe
                    await fetchGraph(currentSessionId === "ALL" ? null : currentSessionId);
                    alert("Xóa thành công");

                } catch (e) {
                    console.error("Delete node failed:", e);
                    alert("Xóa thất bại: " + (e.message || e));
                    // revert optimistic change
                    allNodes = backupNodes;
                    allLinks = backupLinks;
                    // refresh cache by fetching server
                    await fetchGraph(currentSessionId === "ALL" ? null : currentSessionId);
                }
            };
            act.appendChild(delBtn);

            tr.appendChild(act);

            tbody.appendChild(tr);
        });
    }

    /* ================= SEARCH / RESET ================= */
    document.getElementById("searchNodeBtn")?.addEventListener("click", () => {
        const q = document.getElementById("searchNode")?.value?.trim();
        if (!q) return;

        const baseNodes = (window.ADMIN_VIEW_TYPE === "ALL")
            ? allNodes
            : allNodes.filter(n => n.type === window.ADMIN_VIEW_TYPE);

        const nodes = baseNodes.filter(n => String(n.id).includes(q) || String(n.value).includes(q));

        const links = filterLinks(nodes, allLinks);

        renderGraph(nodes, links);
        renderNodeTable(nodes);
    });

    document.getElementById("resetNodeBtn")?.addEventListener("click", () => {
        const inp = document.getElementById("searchNode");
        if (inp) inp.value = "";
        renderByView();
    });

    /* =============== NODE EDIT MODAL =============== */

    // create modal DOM once
    function ensureNodeEditModal() {
        if (document.getElementById('nodeEditModal')) return;

        const modal = document.createElement('div');
        modal.id = 'nodeEditModal';
        modal.style.cssText = 'display:none; position:fixed; inset:0; align-items:center; justify-content:center; background:rgba(0,0,0,0.35); z-index:80;';

        modal.innerHTML = `
            <div style="width:520px; background:white; border-radius:14px; padding:18px 20px; box-shadow:0 24px 80px rgba(0,0,0,0.25);">
                <h3 style="margin-bottom:10px;">Chỉnh sửa Node</h3>
                <form id="nodeEditForm">
                    <div style="margin-bottom:10px;">
                        <label style="display:block;font-size:13px;color:#444;margin-bottom:6px;">Loại</label>
                        <input id="nodeEditType" readonly style="width:100%;padding:10px;border-radius:8px;border:1px solid #eee;background:#f8fafc;" />
                    </div>
                    <div style="margin-bottom:10px;">
                        <label style="display:block;font-size:13px;color:#444;margin-bottom:6px;">Giá trị</label>
                        <input id="nodeEditValue" style="width:100%;padding:10px;border-radius:8px;border:1px solid #ddd;" />
                    </div>
                    <div style="display:flex;justify-content:flex-end;gap:8px;">
                        <button type="button" id="nodeEditCancel" style="background:#f3f4f6;color:#111;padding:8px 12px;border-radius:10px;border:1px solid #e5e7eb;">Huỷ</button>
                        <button type="submit" style="background:linear-gradient(135deg,#2563eb,#4f46e5); color:white; padding:8px 12px; border-radius:10px; border:none;">Lưu</button>
                    </div>
                </form>
            </div>
        `;

        document.body.appendChild(modal);

        // handlers
        document.getElementById('nodeEditCancel').addEventListener('click', () => {
            document.getElementById('nodeEditModal').style.display = 'none';
        });

        document.getElementById('nodeEditForm').addEventListener('submit', async (evt) => {
            evt.preventDefault();
            const modalEl = document.getElementById('nodeEditModal');
            const val = document.getElementById('nodeEditValue').value?.trim();
            const id = modalEl.dataset.nodeId;
            if (!id || !val) { alert('Giá trị không hợp lệ'); return; }

            // optimistic update
            updateNodeInCaches(id, () => ({ value: val }));
            renderByView();

            try {
                const res = await fetch(`/admin/node/${encodeURIComponent(id)}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ value: val })
                });

                const j = await res.json().catch(() => ({}));
                if (!res.ok) throw new Error(j.message || `HTTP ${res.status}`);

                if (j.node && j.node.value) updateNodeInCaches(id, () => ({ value: j.node.value }));

                modalEl.style.display = 'none';
                alert('Cập nhật thành công');

            } catch (e) {
                console.error('Node update failed', e);
                alert('Cập nhật thất bại: ' + (e.message || e));
                // reload to revert
                await fetchGraph(currentSessionId === 'ALL' ? null : currentSessionId);
                modalEl.style.display = 'none';
            }
        });
    }

    function openNodeEditModal(node) {
        ensureNodeEditModal();
        const modal = document.getElementById('nodeEditModal');
        modal.dataset.nodeId = node.id;
        document.getElementById('nodeEditType').value = node.type || '';
        document.getElementById('nodeEditValue').value = node.value || '';
        modal.style.display = 'flex';
        document.getElementById('nodeEditValue').focus();
    }


    /* ================= INIT ================= */

fetchSessions();

// Không tự động load graph nếu không có session
console.log("Admin ready. Waiting for session selection...");

startAutoRefresh();

// Auto-load ALL graph on startup so it keeps updating from Wireshark
fetchGraph(null, { force: true });

// Near real-time updates (Server-Sent Events): refresh immediately when server receives new capture
(() => {
    if (typeof window.EventSource !== "function") return;

    let pending = false;
    let timer = null;

    const scheduleRefresh = () => {
        if (pending) return;
        pending = true;

        clearTimeout(timer);
        timer = setTimeout(() => {
            pending = false;
            const sid = currentSessionId && currentSessionId !== "ALL" ? currentSessionId : null;
            fetchGraph(sid, { force: true, skipIfBusy: true, allowAbort: false });
        }, 250);
    };

    try {
        const es = new EventSource("/admin/stream/graph");
        es.addEventListener("graph-update", scheduleRefresh);
        es.addEventListener("update", scheduleRefresh);
        es.addEventListener("hello", () => {});
        es.onerror = () => {
            // browser will auto-reconnect; keep silent
        };
    } catch (e) {
        console.warn("SSE unavailable", e);
    }
})();

document.addEventListener("visibilitychange", () => {
    if (document.visibilityState === "visible") startAutoRefresh();
    else stopAutoRefresh();
});

});
