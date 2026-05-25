// staff-main.js v5.9.1 – FIX FULL (ALL / Email / IPAddress / URL)
// FEATURES: LIMIT NODE IN BOUNDS + SEARCH + EDIT + HIGHLIGHT

document.addEventListener("DOMContentLoaded", () => {

    /* ================= GLOBAL VIEW MODE ================= */

    // === REALTIME UPDATE WITH SSE ===
    try {
        const evt = new EventSource('/staff/stream/graph');
        evt.onmessage = function (event) {
            // Khi có sự kiện mới từ backend, tự động reload graph
            if (typeof window.fetchGraph === 'function') {
                const sessionSelect = document.getElementById('sessionSelect');
                const sessionId = sessionSelect ? sessionSelect.value : '';
                window.fetchGraph(sessionId);
            }
        };
        evt.onerror = function (e) {
            console.warn('SSE connection error:', e);
        };
    } catch (e) {
        console.warn('SSE not supported:', e);
    }
    window.STAFF_VIEW_TYPE = window.STAFF_VIEW_TYPE || "ALL";
    // ALL | Email | IPAddress | URL

    const nodeRadius = 20;
    const MIN_NODE_GAP = 18;
    const NODE_PACKING_STEP = (nodeRadius * 2) + MIN_NODE_GAP + 8;
    const VISUAL_DOMAIN_BOUNDARY_DISTANCE = 0.55;
    const DENSE_GRAPH_NODE_THRESHOLD = 180;
    const DENSE_GRAPH_LINK_THRESHOLD = 420;
    const MAX_RENDERED_OVERLAP_LINKS_DENSE = 180;
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

    const svgEl = document.getElementById("graphSVG");
    if (!svgEl) {
        console.error("❌ Không tìm thấy #graphSVG");
        return;
    }

    // Make SVG responsive: use CSS for size and update viewBox per render
    const svg = d3.select("#graphSVG").attr("width", "100%").attr("height", "100%").attr("preserveAspectRatio", "xMidYMid meet");

    let allNodes = [], allLinks = [], simulation = null, isEditing = false;
    let selectedNodeId = null;
    let showLinkLabelsAlways = false;
    let visualDomainPos = {};
    let visualDomainRadii = {};
    let lastGraphSignature = null;
    let zoomFitTimer = null;
    const layoutPositions = new Map();
    let currentGraphSessionKey = "";

    /* ================= UTIL ================= */
    function filterLinks(nodes, links) {
        const ids = new Set(nodes.map(n => n.id));
        return links.filter(l =>
            ids.has(l.source?.id || l.source) &&
            ids.has(l.target?.id || l.target)
        );
    }

    function safeId(v) {
        return typeof v === "string" ? v : (v && typeof v === "object" ? v.id : null);
    }

    function graphSignature(nodes, links) {
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
        return `${window.STAFF_VIEW_TYPE || "ALL"}#${nodePart}#${linkPart}`;
    }

    function isSessionNode(node) {
        return String(node?.type || "").toLowerCase() === "analysissession";
    }

    function isBalancedInfluenceNode(node) {
        const zone = String(node?.influenceZone || "").toUpperCase();
        return !!(node?.multiDomainOverlap || node?.bridgeNode || zone === "BRIDGE" || zone === "OVERLAP" || zone === "OUTSIDE_INFLUENCE");
    }

    function visibleLinksForLayout(nodes, links) {
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
    }

    function nearestDomainFromBackend(node) {
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
    }

    function assignDomain(node) {
        if (!node || String(node.type || "").toLowerCase() === "analysissession") return null;
        const nearest = nearestDomainFromBackend(node);
        if (nearest) return nearest;
        const backendDomain = String(node.domainAssignment || "").toLowerCase().trim();
        if (DOMAIN_ORDER.includes(backendDomain)) return backendDomain;
        const risk = String(node.riskLevel || "low").toLowerCase().trim();
        if (risk === "high") return "fraud";
        if (risk === "medium") return "suspicious";
        return "safe";
    }
    function publishGraphContext(nodes, links) {
        window.__graphContext = {
            nodes: Array.isArray(nodes) ? nodes : [],
            links: Array.isArray(links) ? links : [],
            linkTypeLabel
        };
    }

    function normalizeValue(type, value) {
        if (!value) return value;
        value = String(value).trim();
        switch (type) {
            case "IPAddress": return value.replace(/[^0-9.]/g, "");
            case "Email": return value.toLowerCase();
            case "URL": return value.replace(/\s+/g, "");
            default: return value;
        }
    }

    function validateValue(type, value) {
        value = String(value || "").trim();
        switch (type) {
            case "IPAddress":
                return /^(25[0-5]|2[0-4]\d|[01]?\d\d?)(\.(25[0-5]|2[0-4]\d|[01]?\d\d?)){3}$/.test(value);
            case "Email":
                return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
            case "URL":
                return /^(https?:\/\/)?([\w-]+\.)+[\w-]+/.test(value);
            default:
                return true;
        }
    }

    function safeText(v) {
        if (v === null || v === undefined) return "—";
        if (Array.isArray(v)) return v.join(", ");
        return String(v);
    }

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
            // link labels will be toggled on next render/tick
        });
    }

    /* ================= FETCH GRAPH ================= */
    async function fetchGraph(sessionId = "", highlightValue = null) {
        try {
            const sessionKey = String(sessionId || "");
            if (sessionKey !== currentGraphSessionKey) {
                layoutPositions.clear();
                lastGraphSignature = null;
                currentGraphSessionKey = sessionKey;
            }
            let url = "/staff/graph";
            if (sessionId && String(sessionId).trim() !== "") {
                url += "/" + encodeURIComponent(sessionId);
            }
            const res = await fetch(url);
            if (!res.ok) throw new Error("Không load được graph");

            const data = await res.json();

            allNodes = data.nodes || [];
            allLinks = data.links || [];

            window.allNodes = allNodes;
            window.allLinks = allLinks;

            const signature = graphSignature(allNodes, allLinks);
            if (!highlightValue && lastGraphSignature === signature) {
                return;
            }
            lastGraphSignature = signature;

            renderByViewType();

            if (highlightValue) {
                highlightNodeValues([highlightValue]);
            }
        } catch (e) {
            console.error("fetchGraph error:", e);
            alert("Không thể tải graph staff!");
        }
    }
    window.fetchGraph = fetchGraph;

    /* ================= RENDER BY VIEW TYPE ================= */
    function renderByViewType(nodes = allNodes, links = allLinks) {
        let filteredNodes = nodes;
        let filteredLinks = links;

        if (window.STAFF_VIEW_TYPE !== "ALL") {
            filteredNodes = nodes.filter(n => n.type === window.STAFF_VIEW_TYPE);
            filteredLinks = filterLinks(filteredNodes, links);
        }

        render(filteredNodes, filteredLinks);
    }

    /* ================= NODE INFO ================= */
    function showNodeInfo(d) {
        if (isEditing) return;

        const box = document.getElementById("nodeInfo");
        if (!box) return;

        box.style.display = "flex";

        box.innerHTML = `
            <div class="node-popup-card">
                <div class="node-info-header ${safeText(d.riskLevel)}">
                    <span>${safeText(d.type)}</span>
                    <span class="risk-badge ${safeText(d.riskLevel)}">
                        ${safeText(d.riskLevel).toUpperCase()}
                    </span>
                </div>

                <div class="node-info-body">
                    <div><b>ID:</b> ${safeText(d.id)}</div>
                    <div><b>Value:</b> ${safeText(d.value)}</div>
                    <div><b>Status:</b> ${safeText(d.status)}</div>
                    <div><b>Risk Score:</b> ${safeText(d.riskScore)}</div>
                    <div><b>Membership:</b> ${safeText(d.membershipStatus || "IN_REGION")}</div>
                    <div><b>Influence zone:</b> ${safeText(d.influenceZone || "-")} ${d.bridgeNode ? "(bridge)" : ""}</div>
                    <div><b>Community:</b> ${safeText(d.communityId || "none")} ${d.domainRole ? `(${safeText(d.domainRole)})` : ""}</div>
                    <div><b>Multi-domain overlap:</b> ${d.multiDomainOverlap ? "yes" : "no"}</div>
                    <div><b>Domain distances:</b> ${safeText(formatDomainDistances(d.domainDistances))}</div>
                    <div><b>Soft membership:</b> ${safeText(formatDomainDistances(d.softMemberships))}</div>
                    <div><b>Domain influence:</b> ${safeText(formatDomainDistances(d.domainInfluence))}</div>
                    <div><b>Feature vector:</b> ${safeText(formatDomainDistances(d.featureVector))}</div>
                    <div><b>Overlap:</b> raw=${safeText(formatNumber(d.overlapScore))}, weighted=${safeText(formatNumber(d.weightedOverlapScore))}, adjusted=${safeText(formatNumber(d.adjustedOverlapScore))}</div>
                    <div><b>Verdict:</b> ${safeText(d.verdict)}</div>
                    <div><b>Indicators:</b> ${Array.isArray(d.indicators) && d.indicators.length ? d.indicators.join(", ") : "Không có"}</div>
                </div>

                <div class="node-info-actions">
                    <button id="closeNodeInfo">Đóng</button>
                </div>
            </div>
        `;

        const closeBtn = document.getElementById("closeNodeInfo");
        if (closeBtn) {
            closeBtn.onclick = () => {
                box.style.display = "none";
                selectedNodeId = null;
            };
        }
    }

    /* ================= RENDER GRAPH ================= */
    function render(nodes, links) {
        links = visibleLinksForLayout(nodes, links);
        publishGraphContext(nodes, links);

        ensureLinkLabelToggle();
        if (zoomFitTimer) {
            clearTimeout(zoomFitTimer);
            zoomFitTimer = null;
        }

        svg.selectAll("*").remove();
        if (simulation) simulation.stop();

        // compute actual drawing width/height from rendered SVG element
        const width = svgEl.clientWidth || 1000;
        const height = svgEl.clientHeight || 600;
        svg.attr("viewBox", `0 0 ${width} ${height}`);

        const container = svg.append("g").attr("class", "graph-container");

        nodes.forEach(n => {
            n.domainAssignment = assignDomain(n);
            const saved = layoutPositions.get(n.id);
            if (saved) {
                n.x = saved.x;
                n.y = saved.y;
                n.vx = 0;
                n.vy = 0;
            }
        });

        const domainCounts = { safe: 0, suspicious: 0, fraud: 0 };
        nodes.forEach(n => {
            if (n.domainAssignment) {
                domainCounts[n.domainAssignment] = (domainCounts[n.domainAssignment] || 0) + 1;
            }
        });

        const domainVisualOffsets = { safe: 0, suspicious: 0, fraud: 0 };
        const balancedNodes = [];
        nodes.forEach(n => {
            if (!n || isSessionNode(n)) return;
            if (isBalancedInfluenceNode(n)) {
                n._balanceVisualIndex = balancedNodes.length;
                balancedNodes.push(n);
                return;
            }
            const domain = n.domainAssignment || assignDomain(n);
            if (!domain) return;
            n._domainVisualIndex = domainVisualOffsets[domain]++;
            n._domainVisualCount = domainCounts[domain] || 1;
        });
        balancedNodes.forEach(n => {
            n._balanceVisualCount = balancedNodes.length || 1;
        });

        const radiusForCount = count => {
            const c = Math.max(0, count || 0);
            return 210 + (24 * Math.sqrt(c)) + (5.5 * Math.pow(c, 0.72));
        };
        const domainRadii = {};
        DOMAIN_ORDER.forEach(domain => {
            domainRadii[domain] = radiusForCount(domainCounts[domain]);
        });

        const maxDomainRadius = Math.max(...DOMAIN_ORDER.map(domain => domainRadii[domain]));
        const domainGap = Math.max(150, maxDomainRadius * 0.34);
        const maxPairRadius = Math.max(
            domainRadii.safe + domainRadii.suspicious,
            domainRadii.safe + domainRadii.fraud,
            domainRadii.suspicious + domainRadii.fraud
        );
        const triangleArm = Math.max(
            maxDomainRadius + domainGap + 140,
            (maxPairRadius + domainGap) / Math.sqrt(3)
        );
        const neutralSessionPos = {
            x: triangleArm + maxDomainRadius + 180,
            y: triangleArm + maxDomainRadius + 180
        };
        const pointOnTriangle = angleDeg => {
            const angle = angleDeg * Math.PI / 180;
            return {
                x: neutralSessionPos.x + Math.cos(angle) * triangleArm,
                y: neutralSessionPos.y + Math.sin(angle) * triangleArm
            };
        };
        const domainPos = {
            safe: pointOnTriangle(-90),
            suspicious: pointOnTriangle(150),
            fraud: pointOnTriangle(30)
        };
        visualDomainPos = domainPos;
        visualDomainRadii = domainRadii;

        const clamp01 = v => Math.max(0, Math.min(1, v));
        const riskValue = node => Math.max(0, Math.min(100, Number(node?.riskScore) || 0));
        const domainStrength = node => {
            const domain = node?.domainAssignment || assignDomain(node);
            const backendDistance = Number(node?.domainDistances?.[domain]);
            if (Number.isFinite(backendDistance)) {
                return clamp01(1 - (backendDistance / VISUAL_DOMAIN_BOUNDARY_DISTANCE));
            }
            const risk = riskValue(node);
            if (domain === "safe") return clamp01(1 - (risk / 33));
            if (domain === "fraud") return clamp01((risk - 67) / 33);
            if (domain === "suspicious") return clamp01(1 - (Math.abs(risk - 50) / 17));
            return 0;
        };
        const stableAngle = node => {
            const raw = String(node?.id || node?.value || "");
            let hash = 0;
            for (let i = 0; i < raw.length; i++) {
                hash = ((hash << 5) - hash + raw.charCodeAt(i)) | 0;
            }
            return Math.abs(hash % 360) * Math.PI / 180;
        };
        const influenceBalanceTarget = node => {
            const weights = node?.domainInfluence || node?.softMemberships || {};
            let total = 0, x = 0, y = 0;
            DOMAIN_ORDER.forEach(domain => {
                const pos = domainPos[domain];
                const weight = Math.max(0, Number(weights[domain]) || 0);
                if (!pos || weight <= 0) return;
                total += weight;
                x += pos.x * weight;
                y += pos.y * weight;
            });
            if (total <= 0) return { ...neutralSessionPos };
            const index = Number.isFinite(node?._balanceVisualIndex) ? node._balanceVisualIndex : 0;
            const count = Math.max(1, Number(node?._balanceVisualCount) || 1);
            const angle = stableAngle(node) + (index * Math.PI * (3 - Math.sqrt(5)));
            const spread = Math.min(Math.max(44, NODE_PACKING_STEP * Math.sqrt(count) * 0.42), maxDomainRadius * 0.22);
            const radius = Math.min(spread, 18 + NODE_PACKING_STEP * Math.sqrt(index + 0.5) * 0.40);
            return { x: (x / total) + Math.cos(angle) * radius, y: (y / total) + Math.sin(angle) * radius };
        };
        const domainTargetForNode = (node, fallbackIndex = 0) => {
            if (isBalancedInfluenceNode(node)) return influenceBalanceTarget(node);
            const domain = node?.domainAssignment || assignDomain(node);
            const pos = domainPos[domain];
            const radius = domainRadii[domain];
            if (!domain || !pos || !radius) return { ...neutralSessionPos };
            const strength = domainStrength(node);
            const usableRadius = Math.max(20, radius - nodeRadius - 30);
            const minRadius = 12;
            const baseRadialDistance = minRadius + (1 - strength) * (usableRadius - minRadius);
            const index = Number.isFinite(node?._domainVisualIndex) ? node._domainVisualIndex : fallbackIndex;
            const count = Math.max(1, Number(node?._domainVisualCount) || domainCounts[domain] || 1);
            const packedRadialDistance = NODE_PACKING_STEP * Math.sqrt(index + 0.5) * 0.72;
            const maxPackingRadius = Math.max(minRadius, usableRadius - NODE_PACKING_STEP * 0.25);
            const radialDistance = Math.min(usableRadius, Math.max(baseRadialDistance, Math.min(maxPackingRadius, packedRadialDistance)));
            const angle = stableAngle(node)
                + (index * Math.PI * (3 - Math.sqrt(5)))
                + ((index % Math.max(1, Math.ceil(Math.sqrt(count)))) * 0.035);
            return {
                x: pos.x + Math.cos(angle) * radialDistance,
                y: pos.y + Math.sin(angle) * radialDistance
            };
        };

        const domainLayer = container.append("g").attr("class", "domains");
        DOMAIN_ORDER.forEach(domain => {
            const pos = domainPos[domain];
            const group = domainLayer.append("g").attr("class", `domain-group domain-${domain}`);
            group.append("circle")
                .attr("cx", pos.x)
                .attr("cy", pos.y)
                .attr("r", domainRadii[domain])
                .attr("fill", DOMAIN_COLORS[domain])
                .attr("fill-opacity", 0.08)
                .attr("stroke", DOMAIN_COLORS[domain])
                .attr("stroke-width", 2)
                .attr("stroke-opacity", 0.5);
            group.append("text")
                .attr("x", pos.x)
                .attr("y", pos.y - domainRadii[domain] - 14)
                .attr("text-anchor", "middle")
                .attr("font-size", "14px")
                .attr("font-weight", "700")
                .attr("fill", DOMAIN_COLORS[domain])
                .attr("fill-opacity", 0.7)
                .text(`${DOMAIN_LABELS[domain]} (${domainCounts[domain] || 0})`);
            group.append("circle")
                .attr("cx", pos.x)
                .attr("cy", pos.y)
                .attr("r", 7)
                .attr("fill", DOMAIN_COLORS[domain])
                .attr("stroke", "#ffffff")
                .attr("stroke-width", 2);
            group.append("text")
                .attr("x", pos.x)
                .attr("y", pos.y + 22)
                .attr("text-anchor", "middle")
                .attr("font-size", "10px")
                .attr("font-weight", "800")
                .attr("fill", DOMAIN_COLORS[domain])
                .text("CENTER");
        });

        const denseGraph = nodes.length > DENSE_GRAPH_NODE_THRESHOLD || links.length > DENSE_GRAPH_LINK_THRESHOLD;
        const centerDistanceNodes = nodes
            .filter(n => !isSessionNode(n))
            .flatMap(n => {
                if (denseGraph && !isBalancedInfluenceNode(n) && n.id !== selectedNodeId) return [];
                if (isBalancedInfluenceNode(n)) {
                    return DOMAIN_ORDER.map(domain => ({ id: `${n.id}::${domain}`, node: n, domain }));
                }
                const domain = n.domainAssignment || assignDomain(n);
                return domain ? [{ id: n.id, node: n, domain }] : [];
            });

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
        const hasUsablePos = (nodes || []).length > 0 && (nodes || []).every(isAlignedWithDomain);
        if (!hasUsablePos) {
            const goldenAngle = Math.PI * (3 - Math.sqrt(5));
            (nodes || []).forEach((n, i) => {
                if (isAlignedWithDomain(n)) return;
                const target = isSessionNode(n) ? neutralSessionPos : domainTargetForNode(n, i);
                const jitter = Math.min(18, 5 + Math.sqrt(i) * 1.2);
                const a = i * goldenAngle;
                n.x = target.x + jitter * Math.cos(a);
                n.y = target.y + jitter * Math.sin(a);
                n.vx = 0;
                n.vy = 0;
            });
        }

        const zoom = d3.zoom().scaleExtent([0.2, 4]).on("zoom", (event) => {
            container.attr("transform", event.transform);
        });
        svg.call(zoom).on("dblclick.zoom", null);

        const centerDistanceLink = container.append("g")
            .attr("class", "center-distance-links")
            .selectAll("line")
            .data(centerDistanceNodes, d => d.id)
            .enter().append("line")
            .attr("stroke", d => DOMAIN_COLORS[d.domain] || "#94a3b8")
            .attr("stroke-width", d => denseGraph ? 0.7 : 1.15)
            .attr("stroke-opacity", d => denseGraph ? (isBalancedInfluenceNode(d.node) ? 0.28 : 0.10) : (isBalancedInfluenceNode(d.node) ? 0.68 : 0.45))
            .attr("stroke-dasharray", "4 4");

        const centerDistanceLabel = container.append("g")
            .attr("class", "center-distance-labels")
            .selectAll("text")
            .data(centerDistanceNodes, d => d.id)
            .enter().append("text")
            .attr("text-anchor", "middle")
            .attr("dy", "-0.35em")
            .style("display", d => denseGraph && d.node?.id !== selectedNodeId ? "none" : null)
            .text(d => {
                const value = d.node?.domainDistances?.[d.domain];
                return value === undefined ? d.domain : `${d.domain}: d=${formatNumber(value)}`;
            });

        const link = container.append("g")
            .selectAll("line")
            .data(links)
            .enter().append("line")
            .attr("stroke", d => linkColor(d.type))
            .attr("stroke-width", d => linkStrokeWidth(d))
            .attr("opacity", d => linkOpacity(d));

        const linkLabel = container.append("g")
            .attr("class", "link-labels")
            .selectAll("text")
            .data(links)
            .enter().append("text")
            .style("display", showLinkLabelsAlways ? null : "none")
            .text(d => linkTypeLabel(d.type, d));

        const node = container.append("g")
            .selectAll("circle")
            .data(nodes, d => d.id)
            .enter().append("circle")
            .attr("r", nodeRadius)
            .attr("fill", d => nodeColor(d))
            .attr("stroke", d => nodeStroke(d))
            .attr("stroke-width", d => nodeStrokeWidth(d))
            .attr("stroke-dasharray", d => d.membershipStatus === "OUTSIDE" ? "4 3" : null)
            .call(d3.drag()
                .on("start", dragStarted)
                .on("drag", dragged)
                .on("end", dragEnded))
            .on("click", (event, d) => {
                event?.stopPropagation?.();
                selectedNodeId = d?.id || null;
                applyHighlight();
                if (typeof window.enhancedShowNodeInfo === "function") {
                    window.enhancedShowNodeInfo(d);
                } else {
                    showNodeInfo(d);
                }
            });

        const label = container.append("g")
            .selectAll("text")
            .data(nodes)
            .enter().append("text")
            .attr("dy", -25)
            .attr("text-anchor", "middle")
            .style("font-size", "11px")
            .text(d => safeText(d.value));

        const nodeById = new Map(nodes.map(n => [n.id, n]));
        const clampNodeInsideDomain = node => {
            if (!node || isSessionNode(node) || isBalancedInfluenceNode(node)) return;
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
            const activeNodes = nodes.filter(n => n && !isSessionNode(n));
            const minDistance = (nodeRadius * 2) + MIN_NODE_GAP + 6;
            for (let i = 0; i < activeNodes.length; i++) {
                const a = activeNodes[i];
                for (let j = i + 1; j < activeNodes.length; j++) {
                    const b = activeNodes[j];
                    if ((a.domainAssignment || assignDomain(a)) !== (b.domainAssignment || assignDomain(b))) continue;
                    let dx = b.x - a.x;
                    let dy = b.y - a.y;
                    let distance = Math.sqrt(dx * dx + dy * dy);
                    if (distance >= minDistance) continue;
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
                }
            }
        };

        simulation = d3.forceSimulation(nodes)
            .force("link", d3.forceLink(links).id(d => d.id).distance(l => {
                const sourceNode = nodeById.get(safeId(l.source));
                const targetNode = nodeById.get(safeId(l.target));
                const sourceDomain = sourceNode?.domainAssignment || "neutral";
                const targetDomain = targetNode?.domainAssignment || "neutral";
                if (isSessionNode(sourceNode) || isSessionNode(targetNode)) {
                    const entityDomain = isSessionNode(sourceNode) ? targetDomain : sourceDomain;
                    return Math.max(140, (domainRadii[entityDomain] || 180) * 0.62);
                }
                return sourceDomain !== targetDomain ? 165 : 82;
            }).strength(l => {
                const sourceNode = nodeById.get(safeId(l.source));
                const targetNode = nodeById.get(safeId(l.target));
                if (isSessionNode(sourceNode) || isSessionNode(targetNode)) return 0.018;
                const sourceDomain = sourceNode?.domainAssignment || "neutral";
                const targetDomain = targetNode?.domainAssignment || "neutral";
                return sourceDomain !== targetDomain ? 0.012 : 0.045;
            }))
            .force("charge", d3.forceManyBody().strength(-340))
            .force("collision", d3.forceCollide().radius(nodeRadius + MIN_NODE_GAP + 4).strength(0.95).iterations(8))
            .force("cluster", () => {
                nodes.forEach(n => {
                    const target = isSessionNode(n) ? neutralSessionPos : domainTargetForNode(n);
                    n.vx += (target.x - n.x) * (isSessionNode(n) ? 0.12 : 0.045);
                    n.vy += (target.y - n.y) * (isSessionNode(n) ? 0.12 : 0.045);
                });
            })
            .force("boundary", () => {
                nodes.forEach(n => {
                    if (!n || isSessionNode(n) || isBalancedInfluenceNode(n)) return;
                    const domain = n.domainAssignment || assignDomain(n);
                    const pos = domainPos[domain];
                    const radius = domainRadii[domain];
                    if (!pos || !radius) return;
                    const dx = n.x - pos.x;
                    const dy = n.y - pos.y;
                    const dist = Math.sqrt(dx * dx + dy * dy);
                    const maxDist = radius - nodeRadius - 12;
                    if (dist > maxDist) {
                        const ratio = maxDist / Math.max(dist, 0.001);
                        n.vx += (pos.x + dx * ratio - n.x) * 0.75;
                        n.vy += (pos.y + dy * ratio - n.y) * 0.75;
                    }
                });
            })
            .on("tick", () => {
                separateOverlappingNodes();
                nodes.forEach(clampNodeInsideDomain);

                // Do not clamp positions to a strict rectangle — allow force layout to place nodes naturally.
                node
                    .attr("cx", d => d.x)
                    .attr("cy", d => d.y);

                label
                    .attr("x", d => d.x)
                    .attr("y", d => d.y - nodeRadius - 5);

                link
                    .attr("x1", d => d.source.x)
                    .attr("y1", d => d.source.y)
                    .attr("x2", d => d.target.x)
                    .attr("y2", d => d.target.y);

                linkLabel
                    .attr("x", d => (d.source.x + d.target.x) / 2)
                    .attr("y", d => (d.source.y + d.target.y) / 2);

                centerDistanceLink
                    .attr("x1", d => domainPos[d.domain]?.x || d.node.x)
                    .attr("y1", d => domainPos[d.domain]?.y || d.node.y)
                    .attr("x2", d => d.node.x)
                    .attr("y2", d => d.node.y);

                centerDistanceLabel
                    .attr("x", d => {
                        const pos = domainPos[d.domain];
                        return pos ? (pos.x + d.node.x) / 2 : d.node.x;
                    })
                    .attr("y", d => {
                        const pos = domainPos[d.domain];
                        return pos ? (pos.y + d.node.y) / 2 : d.node.y;
                    });
            });

        const renderStablePositions = () => {
            separateOverlappingNodes();
            nodes.forEach(clampNodeInsideDomain);

            node
                .attr("cx", d => d.x)
                .attr("cy", d => d.y);

            label
                .attr("x", d => d.x)
                .attr("y", d => d.y - nodeRadius - 5);

            link
                .attr("x1", d => d.source.x)
                .attr("y1", d => d.source.y)
                .attr("x2", d => d.target.x)
                .attr("y2", d => d.target.y);

            linkLabel
                .attr("x", d => (d.source.x + d.target.x) / 2)
                .attr("y", d => (d.source.y + d.target.y) / 2);

            centerDistanceLink
                .attr("x1", d => domainPos[d.domain]?.x || d.node.x)
                .attr("y1", d => domainPos[d.domain]?.y || d.node.y)
                .attr("x2", d => d.node.x)
                .attr("y2", d => d.node.y);

            centerDistanceLabel
                .attr("x", d => {
                    const pos = domainPos[d.domain];
                    return pos ? (pos.x + d.node.x) / 2 : d.node.x;
                })
                .attr("y", d => {
                    const pos = domainPos[d.domain];
                    return pos ? (pos.y + d.node.y) / 2 : d.node.y;
                });
        };

        simulation.stop();
        const staticTicks = (nodes.length > DENSE_GRAPH_NODE_THRESHOLD || links.length > DENSE_GRAPH_LINK_THRESHOLD) ? 38 : 80;
        for (let i = 0; i < staticTicks; i++) {
            simulation.tick();
        }
        renderStablePositions();
        nodes.forEach(n => {
            n.vx = 0;
            n.vy = 0;
            n.fx = n.x;
            n.fy = n.y;
            layoutPositions.set(n.id, { x: n.x, y: n.y });
        });

        function applyHighlight() {
            const id = selectedNodeId;
            if (!id) {
                node.classed("dim", false).classed("pulse", false).classed("rel-highlight", false);
                link.classed("dim", false).classed("pulse", false).classed("rel-highlight", false);
                linkLabel.style("display", showLinkLabelsAlways ? null : "none").classed("dim", false);
                return;
            }

            const connectedKeys = new Set();
            const neighborIds = new Set([id]);
            const key = l => `${(l.source?.id || l.source)}->${(l.target?.id || l.target)}::${l.type || ""}`;

            links.forEach(l => {
                const s = l.source?.id || l.source;
                const t = l.target?.id || l.target;
                if (s === id || t === id) {
                    connectedKeys.add(key(l));
                    if (s) neighborIds.add(s);
                    if (t) neighborIds.add(t);
                }
            });

            node
                .classed("dim", d => !neighborIds.has(d.id))
                .classed("pulse", d => d.id === id)
                .classed("rel-highlight", d => neighborIds.has(d.id) && d.id !== id);

            link
                .classed("dim", d => !connectedKeys.has(key(d)))
                .classed("pulse", d => connectedKeys.has(key(d)))
                .classed("rel-highlight", d => connectedKeys.has(key(d)));

            linkLabel
                .classed("dim", d => !connectedKeys.has(key(d)))
                .style("display", d => {
                    if (showLinkLabelsAlways) return null;
                    return connectedKeys.has(key(d)) ? null : "none";
                });
        }

        svg.on("click", () => {
            selectedNodeId = null;
            applyHighlight();
        });

        applyHighlight();

        // ✅ Render table theo view hiện tại
        renderTable(nodes);
        // ✅ Render session final table (grouped view)
        renderSessionFinalTable(
            (nodes && nodes.length) ? nodes : allNodes
        );

        // zoom-to-fit
        const zoomToFit = () => {
            if (!nodes || nodes.length === 0) return;
            const xs = nodes.map(n => n.x);
            const ys = nodes.map(n => n.y);
            const minX = Math.min(...xs), maxX = Math.max(...xs);
            const minY = Math.min(...ys), maxY = Math.max(...ys);
            const padding = 40;
            const boxW = Math.max(1, maxX - minX);
            const boxH = Math.max(1, maxY - minY);
            const scale = Math.min(4, Math.max(0.2, Math.min(width / (boxW + padding), height / (boxH + padding))));
            const tx = (width - scale * (minX + maxX)) / 2;
            const ty = (height - scale * (minY + maxY)) / 2;
            const transform = d3.zoomIdentity.translate(tx, ty).scale(scale);
            svg.transition().duration(600).call(zoom.transform, transform);
        };

        zoomFitTimer = setTimeout(zoomToFit, 120);

        svg.on("dblclick", () => svg.transition().duration(400).call(zoom.transform, d3.zoomIdentity));
    }

    function dragStarted(e, d) {
        if (!e.active) simulation.alphaTarget(0.3).restart();
        const t = d3.zoomTransform(svg.node());
        d.fx = (e.x - t.x) / t.k;
        d.fy = (e.y - t.y) / t.k;
    }
    function dragged(e, d) {
        const t = d3.zoomTransform(svg.node());
        let nx = (e.x - t.x) / t.k;
        let ny = (e.y - t.y) / t.k;
        if (!isSessionNode(d) && !isBalancedInfluenceNode(d)) {
            const domain = d.domainAssignment || assignDomain(d);
            const pos = visualDomainPos[domain];
            const radius = visualDomainRadii[domain];
            if (pos && radius) {
                const dx = nx - pos.x;
                const dy = ny - pos.y;
                const dist = Math.sqrt(dx * dx + dy * dy);
                const maxDist = Math.max(12, radius - nodeRadius - 5);
                if (dist > maxDist) {
                    const ratio = maxDist / Math.max(dist, 0.001);
                    nx = pos.x + dx * ratio;
                    ny = pos.y + dy * ratio;
                }
            }
        }
        d.fx = nx;
        d.fy = ny;
    }
    function dragEnded(e, d) {
        if (!e.active) simulation.alphaTarget(0);
        d.fx = null; d.fy = null;
    }

    /* ================= TABLE ================= */
    function renderTable(data) {
        const tbodyEl = document.querySelector("#nodeTable tbody");
        if (!tbodyEl) return;

        const tbody = d3.select("#nodeTable tbody");
        tbody.selectAll("*").remove();

        const rows = tbody.selectAll("tr")
            .data(data, d => d.id)
            .enter().append("tr")
            .each(function(d) {
                const rl = String(d.riskLevel || "low").trim().toLowerCase();
                const el = d3.select(this);
                if (rl === "high") el.classed("node-fraud", true);
                else if (rl === "medium") el.classed("node-suspicious", true);
                else el.classed("node-safe", true);
            });

        rows.append("td").text(d => safeText(d.id));
        rows.append("td").text(d => safeText(d.type));
        rows.append("td").append("span").text(d => safeText(d.value)).attr("class", d => {
            const rl = String(d.riskLevel || "low").trim().toLowerCase();
            return rl === "high" ? "node-fraud" : rl === "medium" ? "node-suspicious" : "node-safe";
        });
        rows.append("td").text(d => safeText(d.status));
        rows.append("td").text(d => safeText(d.riskLevel)).attr("class", d => {
            const rl = String(d.riskLevel || "low").trim().toLowerCase();
            return rl === "high" ? "risk-level node-fraud" : rl === "medium" ? "risk-level node-suspicious" : "risk-level node-safe";
        });

        rows.append("td").text(d => safeText(d.riskScore)).attr("class", d => {
            const score = Number(d.riskScore) || 0;
            return score >= 80 ? 'score-high' : score >= 50 ? 'score-medium' : 'score-low';
        });
        rows.append("td").text(d => safeText(d.verdict));
        rows.append("td").text(d => Array.isArray(d.indicators) ? d.indicators.join(", ") : "");

        const act = rows.append("td");
        act.append("button").text("Sửa").on("click", (_, d) => showEditForm(d));
    }

    /* ================= EDIT ================= */
    function showEditForm(d) {
        isEditing = true;

        const box = document.getElementById("nodeInfo");
        if (!box) return;

        box.style.display = "flex";

        box.innerHTML = `
            <div class="node-edit-card">
                <div class="node-edit-header">✏️ Sửa Node</div>
                <div class="node-edit-body">
                    <label>Type</label>
                    <input value="${safeText(d.type)}" disabled>
                    <label>Value</label>
                    <input id="editValue" value="${safeText(d.value)}">
                </div>
                <div class="node-edit-actions">
                    <button id="cancelEditBtn">Hủy</button>
                    <button id="saveEditBtn">Lưu</button>
                </div>
            </div>
        `;

        document.getElementById("cancelEditBtn").onclick = () => {
            isEditing = false;
            box.style.display = "none";
        };

        document.getElementById("saveEditBtn").onclick = async () => {
            const newValue = normalizeValue(d.type, document.getElementById("editValue").value);

            if (!validateValue(d.type, newValue)) {
                alert("Giá trị không hợp lệ");
                return;
            }

            try {
                const res = await fetch("/staff/node/" + encodeURIComponent(d.id), {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ value: newValue })
                });

                const resp = await res.json();
                if (!resp.success) {
                    alert(resp.message || "Update thất bại");
                    return;
                }

                const idx = allNodes.findIndex(n => n.id === d.id);
                if (idx >= 0) allNodes[idx] = resp.node;

                renderByViewType();
                highlightNodeValues([resp.node.value]);

                isEditing = false;
                box.style.display = "none";

            } catch (e) {
                console.error("saveEdit error:", e);
                alert("Không thể lưu node!");
            }
        };
    }

    /* ================= HIGHLIGHT ================= */
    function highlightNodeValues(values) {
        const normalized = (values || []).map(v => normalizeValue(window.STAFF_VIEW_TYPE, v));

        svg.selectAll("circle")
            .attr("stroke", d => normalized.includes(normalizeValue(d.type, d.value)) ? "#000" : null)
            .attr("stroke-width", d => normalized.includes(normalizeValue(d.type, d.value)) ? 3 : null);
    }
    window.highlightNodeValues = highlightNodeValues;

    function nodeColor(d) {
        if (d.membershipStatus === "OUTSIDE") return "#94a3b8";
        return d.riskLevel === "high" ? "#d62728"
            : d.riskLevel === "medium" ? "#ffdd57"
                : "#1c8ef9";
    }

    function nodeStroke(d) {
        if (d.multiDomainOverlap) return "#7c3aed";
        if ((Number(d.adjustedOverlapScore) || 0) >= 0.3) return "#7c3aed";
        if (d.membershipStatus === "OUTSIDE") return "#64748b";
        return "#fff";
    }

    function nodeStrokeWidth(d) {
        if (d.multiDomainOverlap) return 4;
        if ((Number(d.adjustedOverlapScore) || 0) >= 0.3) return 3;
        return d.membershipStatus === "OUTSIDE" ? 2.5 : 1.2;
    }

    function linkColor(t) {
        return {
            OVERLAP: "#7c3aed",
            CONNECTED_TO: "#8e44ad",
            SUBMITTED_FOR_ANALYSIS: "#1c8ef9",
            CONTAINS_URL: "#ffdd57",
            SENT_FROM_IP: "#d62728",
            HOSTED_ON: "#2ca02c"
        }[t] || "#aaa";
    }

    function linkStrokeWidth(d) {
        if (d.type !== "OVERLAP") return 2;
        return 1.5 + Math.min(7, (Number(d.overlapScore) || 0) * 6);
    }

    function linkOpacity(d) {
        return d.type === "OVERLAP" ? 0.82 : 0.36;
    }

    function formatNumber(value) {
        const n = Number(value);
        return Number.isFinite(n) ? n.toFixed(2) : "0.00";
    }

    function formatDomainDistances(distances) {
        if (!distances || typeof distances !== "object") return "none";
        const ordered = ["safe", "suspicious", "fraud"].filter(key => distances[key] !== undefined);
        const keys = ordered.length ? ordered : Object.keys(distances);
        return keys
            .map(key => `${key}=${formatNumber(distances[key])}`)
            .join(", ") || "none";
    }

    /* ================= SESSION FINAL TABLE ================= */
    function renderSessionFinalTable(nodes) {
        const tbody = document.querySelector("#finalTable tbody");
        if (!tbody) return;

        tbody.innerHTML = "";

        const sessions = {};

        (nodes || []).forEach(n => {
            if (!n.sessionId) return;

            const s = sessions[n.sessionId] ||= {
                sessionId: n.sessionId,
                emails: [],
                ips: [],
                urls: [],
                maxRisk: "low",
                maxScore: 0
            };

            const normalizedRisk = String(n.riskLevel || "low").trim().toLowerCase();
            const entry = { value: n.value, risk: normalizedRisk };

            if (n.type === "Email") s.emails.push(entry);
            if (n.type === "IPAddress") s.ips.push(entry);
            if (n.type === "URL") s.urls.push(entry);

            if (normalizedRisk === "high" && s.maxRisk !== "high") s.maxRisk = "high";
            else if (normalizedRisk === "medium" && s.maxRisk === "low") s.maxRisk = "medium";

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

            const buildColumn = (items) => items.map(i => {
                const risk = String(i.risk || "").trim().toLowerCase();
                let cls = "";
                if (risk === "high") cls = "node-fraud";
                else if (risk === "medium") cls = "node-suspicious";
                else cls = "node-safe";
                return `<span class="${cls}">${i.value}</span>`;
            }).join(", ");

            td(buildColumn(s.emails));
            td(buildColumn(s.ips));
            td(buildColumn(s.urls));

            td(s.maxRisk);

            const scoreClass = s.maxScore >= 80 ? "score-high" : s.maxScore >= 50 ? "score-medium" : "score-low";
            td(`<span class="${scoreClass}">${s.maxScore}</span>`);

            td(s.maxRisk === "high" ? "BLOCK" : s.maxRisk === "medium" ? "ĐÁNG NGHI NGỜ" : "AN TOÀN");

            tbody.appendChild(tr);
        });
    }

    /* ================= SEARCH / RESET ================= */
    document.getElementById("searchNodeBtn")?.addEventListener("click", () => {
        const q = document.getElementById("searchNode")?.value?.trim();
        if (!q) return;

        // lọc theo view hiện tại
        const baseNodes = (window.STAFF_VIEW_TYPE === "ALL")
            ? allNodes
            : allNodes.filter(n => n.type === window.STAFF_VIEW_TYPE);

        const nodes = baseNodes.filter(n =>
            String(n.id).includes(q) || String(n.value).includes(q)
        );

        // ✅ render đúng nodes + links tương ứng
        const links = filterLinks(nodes, allLinks);

        render(nodes, links);
        highlightNodeValues(nodes.map(n => n.value));
    });

    document.getElementById("resetNodeBtn")?.addEventListener("click", () => {
        const inp = document.getElementById("searchNode");
        if (inp) inp.value = "";
        renderByViewType();
    });

    /* ================= ANALYZE RESULT RENDER ================= */
    function renderAnalyzeResult(data, container) {
        if (!container) return;

        container.innerHTML = `
            <div class="analyze-result-card ${safeText(data.verdict)}">
                <h3>Kết quả phân tích</h3>
                <ul>
                    <li><b>Verdict:</b> ${safeText(data.verdict)}</li>
                    <li><b>Risk score:</b> ${safeText(data.riskScore)}</li>
                    <li><b>Risk level:</b> ${safeText(data.riskLevel)}</li>
                    <li><b>Indicators:</b> ${(data.indicators || []).join(", ") || "Không có"}</li>
                </ul>
            </div>
        `;
    }
    window.renderAnalyzeResult = renderAnalyzeResult;

    /* ================= INIT ================= */
    fetchGraph();
    window.render = render;

});
