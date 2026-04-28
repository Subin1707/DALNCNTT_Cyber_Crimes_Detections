// customer-main.js v5.7 – FULL FIX – LIMIT NODE IN BOUNDS + SEARCH + HIGHLIGHT (READ ONLY)
document.addEventListener("DOMContentLoaded", () => {

    /* ================= CONFIG ================= */

    // === REALTIME UPDATE WITH SSE ===
    try {
        const evt = new EventSource('/customer/stream/graph');
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
    const nodeRadius = 20;

    const svgEl = document.getElementById("graphSVG");
    if (!svgEl) {
        console.error("❌ Không tìm thấy #graphSVG");
        return;
    }

    const svg = d3.select("#graphSVG")
        .attr("width", "100%")
        .attr("height", "100%")
        .attr("preserveAspectRatio", "xMidYMid meet");

    let allNodes = [];
    let allLinks = [];
    let simulation = null;
    let selectedNodeId = null;
    let showLinkLabelsAlways = false;

    /* ================= UTIL ================= */
    function filterLinks(nodes, links) {
        const ids = new Set(nodes.map(n => n.id));
        return (links || []).filter(l =>
            ids.has(l.source?.id || l.source) &&
            ids.has(l.target?.id || l.target)
        );
    }
    function publishGraphContext(nodes, links) {
        window.__graphContext = {
            nodes: Array.isArray(nodes) ? nodes : [],
            links: Array.isArray(links) ? links : [],
            linkTypeLabel
        };
    }

    function safeStr(v) {
        return (v ?? "").toString();
    }
    function safeText(el, value) {
        el.textContent = value ?? "";
    }

    function normalizeSearch(s) {
        return safeStr(s).trim().toLowerCase();
    }

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
        });
    }


    /* ================= FETCH GRAPH (WITH OPTIONAL SESSION) ================= */
    async function fetchGraph(sessionId = "", highlightValue = null) {
        try {
            let url = "/customer/graph";
            if (sessionId && String(sessionId).trim() !== "") {
                // try session-specific endpoint first, fall back to generic
                const tryUrl = "/customer/graph/" + encodeURIComponent(sessionId);
                let res = await fetch(tryUrl);
                if (res.ok) {
                    const data = await res.json();
                    allNodes = data.nodes || [];
                    allLinks = data.links || [];
                } else {
                    res = await fetch(url);
                    if (!res.ok) throw new Error("Không thể tải graph!");
                    const data = await res.json();
                    allNodes = data.nodes || [];
                    allLinks = data.links || [];
                }
            } else {
                const res = await fetch(url);
                if (!res.ok) throw new Error("Không thể tải graph!");
                const data = await res.json();
                allNodes = data.nodes || [];
                allLinks = data.links || [];
            }

            window.allNodes = allNodes;
            window.allLinks = allLinks;

            render(allNodes, filterLinks(allNodes, allLinks));

            if (highlightValue) {
                highlightNodeValues([highlightValue]);
            }

            // try to render sessions table (if nodes contain sessionId)
            if (typeof renderSessionFinalTable === "function") {
                renderSessionFinalTable(allNodes);
            }

        } catch (e) {
            console.error("fetchGraph error:", e);
            alert(e?.message || "Lỗi tải graph!");
        }
    }
    window.fetchGraph = fetchGraph;
function showNodeInfo(d) {

    const box = document.getElementById("nodeInfo");
    if (!box) return;

    box.style.display = "flex";
    box.innerHTML = "";

    const card = document.createElement("div");
    card.className = "node-popup-card";

    const addLine = (label, value) => {
        const p = document.createElement("p");
        p.innerHTML = `<b>${label}:</b> `;
        const span = document.createElement("span");
        safeText(span, value);
        p.appendChild(span);
        card.appendChild(p);
    };

    const h3 = document.createElement("h3");
    safeText(h3, d.type);
    card.appendChild(h3);

    addLine("Value", d.value);
    addLine("Risk", d.riskLevel);
    addLine("Score", d.riskScore);
    addLine("Verdict", d.verdict);

    const btn = document.createElement("button");
    btn.textContent = "Đóng";
    btn.onclick = () => {
        box.style.display = "none";
        selectedNodeId = null;
    };

    card.appendChild(btn);
    box.appendChild(card);
}
    /* ================= RENDER GRAPH ================= */
    function render(nodes, links) {
        svg.selectAll("*").remove();
        if (simulation) simulation.stop();

        nodes = nodes || [];
        links = links || [];
        publishGraphContext(nodes, links);

        // compute actual drawing width/height from rendered SVG element
        const width = svgEl.clientWidth || 1000;
        const height = svgEl.clientHeight || 600;

        const container = svg.append("g").attr("class", "graph-container");

        ensureLinkLabelToggle();

        // Seed initial positions to avoid overlap when nodes come without x/y (fresh fetch)
        const hasAnyPos = (nodes || []).some(n => Number.isFinite(n?.x) && Number.isFinite(n?.y));
        if (!hasAnyPos) {
            const goldenAngle = Math.PI * (3 - Math.sqrt(5));
            const spacing = nodeRadius * 5.5;
            (nodes || []).forEach((n, i) => {
                const r = Math.sqrt(i) * spacing;
                const a = i * goldenAngle;
                n.x = width / 2 + r * Math.cos(a);
                n.y = height / 2 + r * Math.sin(a);
            });
        }

const zoom = d3.zoom()
    .scaleExtent([0.2, 4])
    .filter(event => {
        // Không zoom khi click hoặc drag vào node
        if (event.type === "mousedown" && event.target.tagName === "circle") {
            return false;
        }
        return true;
    })
    .on("zoom", (event) => {
        container.attr("transform", event.transform);
    });        
    svg.call(zoom).on("dblclick.zoom", null);

        const link = container.append("g")
            .selectAll("line")
            .data(links)
            .enter()
            .append("line")
            .attr("stroke", d => linkColor(d.type))
            .attr("stroke-width", 2);

        const linkLabel = container.append("g")
            .attr("class", "link-labels")
            .selectAll("text")
            .data(links)
            .enter().append("text")
            .style("display", showLinkLabelsAlways ? null : "none")
            .text(d => linkTypeLabel(d.type));

        const node = container.append("g")
            .selectAll("circle")
            .data(nodes, d => d.id)
            .enter()
            .append("circle")
            .attr("r", nodeRadius)
            .attr("fill", d => nodeColor(d))
            .call(
                d3.drag()
                    .on("start", dragStarted)
                    .on("drag", dragged)
                    .on("end", dragEnded)
            )         
            .on("click", function(event, d) {
                event.stopPropagation();   // ngăn zoom bắt click
                selectedNodeId = d?.id || null;
                applyHighlight();
                // Use enhanced analysis instead of simple showNodeInfo
                if (typeof window.enhancedShowNodeInfo === 'function') {
                    window.enhancedShowNodeInfo(d);
                } else {
                    showNodeInfo(d);
                }
            });
            node.on("mousedown.zoom", null);
        const label = container.append("g")
            .selectAll("text")
            .data(nodes)
            .enter()
            .append("text")
            .attr("dy", -25)
            .attr("text-anchor", "middle")
            .style("font-size", "12px")
            .style("pointer-events", "none")
            .text(d => safeStr(d.value));

        simulation = d3.forceSimulation(nodes)
            .force("link", d3.forceLink(links).id(d => d.id).distance(120))
            .force("charge", d3.forceManyBody().strength(-500))
            .force("center", d3.forceCenter(width / 2, height / 2))
            .force("collision", d3.forceCollide().radius(nodeRadius + 10).iterations(2))
            .on("tick", () => {
                // Không ép tọa độ vào khung chữ nhật cố định — cho phép layout đặt node tự nhiên
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

        renderTable(nodes);
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
        setTimeout(zoomToFit, 600);
        svg.on("dblclick", () => svg.transition().duration(400).call(zoom.transform, d3.zoomIdentity));
    }
    window.render = render;

    function dragStarted(e, d) {
        if (!e.active) simulation.alphaTarget(0.3).restart();
        const t = d3.zoomTransform(svg.node());
        d.fx = (e.x - t.x) / t.k;
        d.fy = (e.y - t.y) / t.k;
    }

    function dragged(e, d) {
        const t = d3.zoomTransform(svg.node());
        const nx = (e.x - t.x) / t.k;
        const ny = (e.y - t.y) / t.k;
        // Cho phép kéo tự do mà không ép vào khung chữ nhật
        d.fx = nx;
        d.fy = ny;
    }

    function dragEnded(e, d) {
        if (!e.active) simulation.alphaTarget(0);
        d.fx = null;
        d.fy = null;
    }

    /* ================= TABLE (READ ONLY) ================= */
    function renderTable(data) {
        const tbody = d3.select("#nodeTable tbody");
        if (tbody.empty()) return;

        tbody.selectAll("*").remove();

        const rows = tbody.selectAll("tr")
            .data(data || [], d => d.id)
            .enter()
            .append("tr")
            .each(function(d) {
                const rl = String(d.riskLevel || "low").trim().toLowerCase();
                const el = d3.select(this);
                if (rl === "high") el.classed("node-fraud", true);
                else if (rl === "medium") el.classed("node-suspicious", true);
                else el.classed("node-safe", true);
            });

        rows.append("td").text(d => safeStr(d.id));
        rows.append("td").text(d => safeStr(d.type));
        rows.append("td").append("span").text(d => safeStr(d.value)).attr("class", d => {
            const rl = String(d.riskLevel || "low").trim().toLowerCase();
            return rl === "high" ? "node-fraud" : rl === "medium" ? "node-suspicious" : "node-safe";
        });
        rows.append("td").text(d => safeStr(d.status));
        rows.append("td").text(d => safeStr(d.riskLevel)).attr("class", d => {
            const rl = String(d.riskLevel || "low").trim().toLowerCase();
            return rl === "high" ? "risk-level node-fraud" : rl === "medium" ? "risk-level node-suspicious" : "risk-level node-safe";
        });
        rows.append("td").text(d => safeStr(d.riskScore)).attr("class", d => {
            const score = Number(d.riskScore) || 0;
            return score >= 80 ? 'score-high' : score >= 50 ? 'score-medium' : 'score-low';
        });
        rows.append("td").text(d => safeStr(d.verdict));
        rows.append("td").text(d => (d.indicators || []).join(", "));
    }

    /* ================= HIGHLIGHT ================= */
    function highlightNodeValues(values) {
        values = (values || []).map(v => safeStr(v));

        svg.selectAll("circle")
            .attr("stroke", d => values.includes(safeStr(d.value)) ? "#000" : null)
            .attr("stroke-width", d => values.includes(safeStr(d.value)) ? 3 : null);
    }
    window.highlightNodeValues = highlightNodeValues;

    function nodeColor(d) {
        return d.riskLevel === "high" ? "#d62728"
            : d.riskLevel === "medium" ? "#ffdd57"
                : "#1c8ef9";
    }

    function linkColor(t) {
        return {
            CONNECTED_TO: "#8e44ad",
            SUBMITTED_FOR_ANALYSIS: "#1c8ef9",
            CONTAINS_URL: "#ffdd57",
            SENT_FROM_IP: "#d62728",
            HOSTED_ON: "#2ca02c"
        }[t] || "#aaa";
    }

    /* ================= SEARCH / RESET ================= */
    const searchBtn = document.getElementById("searchNodeBtn");
    const resetBtn = document.getElementById("resetNodeBtn");
    const searchInput = document.getElementById("searchNode");

    if (searchBtn) {
        searchBtn.onclick = () => {
            const q = normalizeSearch(searchInput?.value);
            if (!q) return;

            const nodes = allNodes.filter(n => {
                const id = normalizeSearch(n.id);
                const value = normalizeSearch(n.value);
                return id.includes(q) || value.includes(q);
            });

            render(nodes, filterLinks(nodes, allLinks));
            highlightNodeValues(nodes.map(n => n.value));
        };
    }

    if (resetBtn) {
        resetBtn.onclick = () => {
            if (searchInput) searchInput.value = "";
            render(allNodes, filterLinks(allNodes, allLinks));
        };
    }

    /* ================= ANALYZE RESULT RENDER ================= */
    function renderAnalyzeResult(data, container) {
        if (!container) return;

        container.innerHTML = `
            <div class="analyze-result-card ${safeStr(data.verdict)}">
                <h3>Kết quả phân tích</h3>
                <ul>
                    <li><b>Verdict:</b> ${safeStr(data.verdict)}</li>
                    <li><b>Risk score:</b> ${safeStr(data.riskScore)}</li>
                    <li><b>Risk level:</b> ${safeStr(data.riskLevel)}</li>
                    <li><b>Indicators:</b> ${(data.indicators || []).join(", ") || "Không có"}</li>
                </ul>
            </div>
        `;
    }
    window.renderAnalyzeResult = renderAnalyzeResult;

    /* ================= INIT ================= */
    // initial load: fetch graph (customer-session.js will handle session dropdown / reload)
    fetchGraph();

});
