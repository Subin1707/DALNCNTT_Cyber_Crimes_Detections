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

    /* ================= UTIL ================= */
    function filterLinks(nodes, links) {
        const ids = new Set(nodes.map(n => n.id));
        return links.filter(l =>
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
            // link labels will be toggled on next render/tick
        });
    }

    /* ================= FETCH GRAPH ================= */
    async function fetchGraph(sessionId = "", highlightValue = null) {
        try {
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
        publishGraphContext(nodes, links);

        ensureLinkLabelToggle();

        svg.selectAll("*").remove();
        if (simulation) simulation.stop();

        // compute actual drawing width/height from rendered SVG element
        const width = svgEl.clientWidth || 1000;
        const height = svgEl.clientHeight || 600;
        svg.attr("viewBox", `0 0 ${width} ${height}`);

        const container = svg.append("g").attr("class", "graph-container");

        // Seed initial positions to avoid node overlap on fresh data (e.g. Excel / tshark updates)
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

        const zoom = d3.zoom().scaleExtent([0.2, 4]).on("zoom", (event) => {
            container.attr("transform", event.transform);
        });
        svg.call(zoom).on("dblclick.zoom", null);

        const link = container.append("g")
            .selectAll("line")
            .data(links)
            .enter().append("line")
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
            .enter().append("circle")
            .attr("r", nodeRadius)
            .attr("fill", d => nodeColor(d))
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

        simulation = d3.forceSimulation(nodes)
            .force("link", d3.forceLink(links).id(d => d.id).distance(120))
            .force("charge", d3.forceManyBody().strength(-500))
            .force("center", d3.forceCenter(width / 2, height / 2))
            .force("collision", d3.forceCollide().radius(nodeRadius + 10).iterations(2))
            .on("tick", () => {
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

        setTimeout(zoomToFit, 600);

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
        const nx = (e.x - t.x) / t.k;
        const ny = (e.y - t.y) / t.k;
        // Allow free dragging without forcing nodes to remain inside a strict rectangle
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
