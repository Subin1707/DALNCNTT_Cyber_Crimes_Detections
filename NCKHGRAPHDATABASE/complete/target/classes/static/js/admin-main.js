// admin-main.js – 10/10 ENTERPRISE VERSION

document.addEventListener("DOMContentLoaded", () => {

    /* ================= GLOBAL ================= */

    window.ADMIN_VIEW_TYPE = window.ADMIN_VIEW_TYPE || "ALL";

    const width = 1000;
    const height = 600;
    const nodeRadius = 18;

    const svg = d3.select("#graphSVG")
        .attr("width", width)
        .attr("height", height);

    let allNodes = [];
    let allLinks = [];
    let simulation = null;
    let currentSessionId = null;

    let abortController = null;
    const graphCache = new Map();
    // SSE will trigger instant updates; keep polling only as a fallback.
    const AUTO_REFRESH_MS = 10000;
    let autoRefreshTimer = null;
    let graphInitialized = false;
    let container = null;
    let linkSel = null;
    let linkLabelSel = null;
    let nodeSel = null;
    let labelSel = null;
    let zoom = null;
    let prevAllNodeIds = new Set();
    let didInitialFit = false;
    let autoLockInterval = null;
    let lastRenderedSessionId = null;
    let isFetchingGraph = false;
    let autoRefreshTick = 0;
    let selectedNodeId = null;
    let showLinkLabelsAlways = false;

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
                source: safeId(l.source),
                target: safeId(l.target),
                type: l.type || ""
            }))
            .filter(l => l.source && l.target);

    const filterLinks = (nodes, links) => {
        const ids = new Set(nodes.map(n => n.id));
        return links.filter(l => ids.has(l.source) && ids.has(l.target));
    };

    const riskRank = r =>
        ({ low: 1, medium: 2, high: 3 }[String(r).toLowerCase()] || 0);

    const linkTypeLabel = (t) => ({
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
    }[String(t || "").toUpperCase()] || String(t || ""));

    const safeTextHtml = (s) =>
        String(s ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;");

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
            const res = await fetch(url, { signal: abortController.signal });
            if (!res.ok) throw new Error(`Graph API failed: ${res.status}`);
            const data = await res.json();
            allNodes = Array.isArray(data.nodes) ? data.nodes : [];
            allLinks = normalizeLinks(data.links);
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
            signal: abortController.signal
        });

        if (!res.ok)
            throw new Error(`Graph API failed: ${res.status}`);

        const data = await res.json();

        allNodes = Array.isArray(data.nodes) ? data.nodes : [];
        allLinks = normalizeLinks(data.links);
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

        ensureLinkLabelToggle();

        if (!graphInitialized) {
            // container group for zoom/pan
            container = svg.append("g").attr("class", "graph-container");

            // zoom behavior
            zoom = d3.zoom()
                .scaleExtent([0.2, 4])
                .on("zoom", (event) => {
                    container.attr("transform", event.transform);
                });

            svg.call(zoom).on("dblclick.zoom", null);

            simulation = d3.forceSimulation()
                .force("link", d3.forceLink().id(d => d.id).distance(120))
                .force("charge", d3.forceManyBody().strength(-500))
                .force("center", d3.forceCenter(width / 2, height / 2))
                .force("collision", d3.forceCollide().radius(nodeRadius + 10).iterations(2));

            linkSel = container.append("g").selectAll("line");
            linkLabelSel = container.append("g").attr("class", "link-labels").selectAll("text");
            nodeSel = container.append("g").selectAll("circle");
            labelSel = container.append("g").selectAll("text");

            simulation.on("tick", () => {
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
                    .attr("y", d => (d.source.y + d.target.y) / 2);
            });

            svg.on("dblclick", () => {
                svg.transition().duration(400).call(zoom.transform, d3.zoomIdentity);
            });

            graphInitialized = true;
        }

        const prevPos = new Map();
        (simulation.nodes() || []).forEach(n => {
            prevPos.set(n.id, {
                x: n.x, y: n.y, vx: n.vx, vy: n.vy,
                fx: n.fx, fy: n.fy
            });
        });

        const reusedCount = nodes.reduce((c, n) => c + (prevPos.has(n.id) ? 1 : 0), 0);
        const reusedRatio = reusedCount / Math.max(1, nodes.length);
        const isFreshGraph = prevPos.size === 0 || reusedRatio < 0.25;
        const isAdditive = !isFreshGraph && nodes.some(n => !prevPos.has(n.id));

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
            const spacing = nodeRadius * 5.5;

            nodes.forEach((n, i) => {
                n.fx = null;
                n.fy = null;
                const r = Math.sqrt(i) * spacing;
                const a = i * goldenAngle;
                n.x = width / 2 + r * Math.cos(a);
                n.y = height / 2 + r * Math.sin(a);
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
                    n.x = cx + (Math.random() - 0.5) * jitter;
                    n.y = cy + (Math.random() - 0.5) * jitter;
                    newOnes.push(n);
                }
            });

            // Khi chỉ thêm vài node mới (vd: dữ liệu tshark/ingest) -> đặt theo vòng tròn quanh tâm để tránh đè lên nhau
            if (newOnes.length > 1) {
                const r = nodeRadius * 6.5;
                newOnes.forEach((n, i) => {
                    const a = (i / newOnes.length) * Math.PI * 2;
                    n.x = cx + r * Math.cos(a);
                    n.y = cy + r * Math.sin(a);
                });
            }
        }

        const linkKey = l => `${safeId(l.source)}->${safeId(l.target)}::${l.type || ""}`;

        linkSel = linkSel.data(links, linkKey);
        linkSel.exit().remove();
        const linkEnter = linkSel.enter().append("line")
            .attr("stroke-width", 2);
        linkSel = linkEnter.merge(linkSel)
            .attr("stroke", d => ({
                SENT_FROM_IP: "#c62828",
                CONTAINS_URL: "#f9a825",
                HOSTED_ON: "#2e7d32",
                HOSTED_ON_DOMAIN: "#2e7d32",
                RESOLVES_TO: "#6d4c41",
                DOWNLOADS: "#5e35b1",
                HAS_HASH: "#6a1b9a",
                RECEIVED: "#546e7a",
                CONNECTS_TO: "#00838f"
            }[d.type] || "#aaa"));

        linkLabelSel = linkLabelSel.data(links, linkKey);
        linkLabelSel.exit().remove();
        const linkLabelEnter = linkLabelSel.enter().append("text")
            .style("display", "none")
            .text(d => linkTypeLabel(d.type));
        linkLabelSel = linkLabelEnter.merge(linkLabelSel)
            .text(d => linkTypeLabel(d.type));

        nodeSel = nodeSel.data(nodes, d => d.id);
        nodeSel.exit().remove();
        const nodeEnter = nodeSel.enter().append("circle")
            .attr("r", nodeRadius)
            .on("click", (event, d) => {
                event?.stopPropagation?.();
                selectedNodeId = d?.id || null;
                applyRelationHighlight(selectedNodeId);
                showNodeInfo(d);
            })
            .call(d3.drag()
                .on("start", e => {
                    if (!e.active)
                        simulation.alphaTarget(0.3).restart();
                })
                .on("drag", (e, d) => {
                    const t = d3.zoomTransform(svg.node());
                    d.fx = (e.x - t.x) / t.k;
                    d.fy = (e.y - t.y) / t.k;
                })
                .on("end", e => {
                    if (!e.active)
                        simulation.alphaTarget(0);
                })
            );

        nodeSel = nodeEnter.merge(nodeSel)
            .attr("fill", d =>
                d.riskLevel === "high" ? "#c62828" :
                d.riskLevel === "medium" ? "#f9a825" : "#1e88e5"
            )
            .attr("stroke", d =>
                d._isNew ? "#000" :
                d.source === "WIRESHARK" ? "#00acc1" : "#fff"
            )
            .attr("stroke-width", d => d._isNew ? 3 : (d.source === "WIRESHARK" ? 3 : 1.2))
            .attr("stroke-dasharray", d => d._isNew ? null : null)
            .attr("r", d => {
                const fresh = d._newAt && (Date.now() - d._newAt) < 6000;
                return fresh ? nodeRadius + 4 : nodeRadius;
            });

        labelSel = labelSel.data(nodes, d => d.id);
        labelSel.exit().remove();
        const labelEnter = labelSel.enter().append("text")
            .attr("dy", -22)
            .attr("text-anchor", "middle")
            .style("font-size", "11px");
        labelSel = labelEnter.merge(labelSel)
            .text(d => d.value);

        simulation.nodes(nodes);
        simulation.force("link").links(links);
        simulation.alpha(isFreshGraph ? 1 : (isAdditive ? 0.6 : 0.25)).restart();

        // Auto-lock sau khi layout dá»«ng Ä‘á»§ lĂ¢u (trĂ¡nh lock quĂ¡ sớm lĂ m node chĂ´ng lĂªn nhau)
        if (autoLockInterval) {
            clearInterval(autoLockInterval);
            autoLockInterval = null;
        }

        const lockStart = Date.now();
        const maxWaitMs = isFreshGraph ? 3500 : (isAdditive ? 2200 : 1200);
        autoLockInterval = setInterval(() => {
            if (!simulation) {
                clearInterval(autoLockInterval);
                autoLockInterval = null;
                return;
            }

            const elapsed = Date.now() - lockStart;
            if (simulation.alpha() > 0.08 && elapsed < maxWaitMs) return;

            clearInterval(autoLockInterval);
            autoLockInterval = null;

            nodes.forEach(n => {
                if (n.fx == null && n.fy == null) {
                    n.fx = n.x;
                    n.fy = n.y;
                }
            });
            simulation.alphaTarget(0);
        }, 200);

        // Focus view on newest nodes (if any)
        const newNodes = nodes.filter(n => n._isNew);
        if (newNodes.length > 0) {
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
                setTimeout(() => showNodeInfo(newest), 450);
            }
        }

        if (!didInitialFit) {
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

            setTimeout(zoomToFit, 600);
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

    function showNodeInfo(d) {

        const box = document.getElementById("nodeInfo");
        if (!box) return;

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
                return `<span class="node-popup-badge">${safeTextHtml(linkTypeLabel(l.type))} ${dir} ${safeTextHtml(otherLabel)}</span>`;
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
            return;
        }

        const connectedKeys = new Set();
        const neighborIds = new Set([nodeId]);

        const linkKey = l => `${safeId(l.source)}->${safeId(l.target)}::${l.type || ""}`;
        (simulation?.force?.("link")?.links?.() || []).forEach(l => {
            const s = safeId(l.source);
            const t = safeId(l.target);
            if (s === nodeId || t === nodeId) {
                connectedKeys.add(linkKey(l));
                if (s) neighborIds.add(s);
                if (t) neighborIds.add(t);
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
            viewBtn.onclick = () => showNodeInfo(n);
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
            const sid = currentSessionId && currentSessionId !== "ALL"
                ? currentSessionId
                : null;
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
