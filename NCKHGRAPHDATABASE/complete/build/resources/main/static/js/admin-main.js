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

    /* ================= FETCH SESSION LIST ================= */

    async function fetchSessions() {
        const sel = document.getElementById("sessionSelect");
        if (!sel) return;

        try {
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

        } catch (e) {
            console.warn("Fetch sessions failed", e);
        }
    }

    /* ================= FETCH GRAPH (SAFE) ================= */

/* ================= FETCH GRAPH (ENTERPRISE SAFE) ================= */

async function fetchGraph(sessionId = null) {

    // Chuẩn hóa sessionId
    currentSessionId = sessionId ? String(sessionId) : "ALL";

    // Nếu ALL mode -> gọi endpoint trả toàn bộ graph
    if (currentSessionId === "ALL") {
        const url = "/admin/graph";

        try {
            const res = await fetch(url, { signal: abortController.signal });
            if (!res.ok) throw new Error(`Graph API failed: ${res.status}`);
            const data = await res.json();
            allNodes = Array.isArray(data.nodes) ? data.nodes : [];
            allLinks = normalizeLinks(data.links);

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
            showLoading(false);
            return;
        }
    }

    // Cache hit
    if (graphCache.has(currentSessionId)) {
        const cached = graphCache.get(currentSessionId);
        allNodes = [...cached.nodes];
        allLinks = [...cached.links];
        renderByView();
        return;
    }

    // Abort request cũ
    if (abortController) abortController.abort();
    abortController = new AbortController();

    showLoading(true);

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
        showLoading(false);
    }
}

window.fetchGraph = fetchGraph;

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
            currentSessionId
                ? allNodes.filter(n => n.sessionId === currentSessionId)
                : allNodes
        );

    }, 150);

    /* ================= GRAPH ================= */

    function renderGraph(nodes, links) {

        svg.selectAll("*").remove();

        if (simulation) simulation.stop();

        // container group for zoom/pan
        const container = svg.append("g").attr("class", "graph-container");

        // zoom behavior
        const zoom = d3.zoom()
            .scaleExtent([0.2, 4])
            .on("zoom", (event) => {
                container.attr("transform", event.transform);
            });

        svg.call(zoom).on("dblclick.zoom", null); // keep double-click native disabled

        simulation = d3.forceSimulation(nodes)
            .force("link", d3.forceLink(links).id(d => d.id).distance(120))
            .force("charge", d3.forceManyBody().strength(-350))
            .force("center", d3.forceCenter(width / 2, height / 2));

        const link = container.append("g")
            .selectAll("line")
            .data(links)
            .join("line")
            .attr("stroke-width", 2)
            .attr("stroke", d => ({
                SENT_FROM_IP: "#c62828",
                CONTAINS_URL: "#f9a825",
                HOSTED_ON: "#2e7d32"
            }[d.type] || "#aaa"));

        const node = container.append("g")
            .selectAll("circle")
            .data(nodes, d => d.id)
            .join("circle")
            .attr("r", nodeRadius)
            .attr("fill", d =>
                d.riskLevel === "high" ? "#c62828" :
                d.riskLevel === "medium" ? "#f9a825" : "#1e88e5"
            )
            .on("click", (_, d) => showNodeInfo(d))
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

        const label = container.append("g")
            .selectAll("text")
            .data(nodes)
            .join("text")
            .attr("dy", -22)
            .attr("text-anchor", "middle")
            .style("font-size", "11px")
            .text(d => d.value);

        simulation.on("tick", () => {
            node.attr("cx", d => d.x)
                .attr("cy", d => d.y);

            label.attr("x", d => d.x)
                .attr("y", d => d.y - 22);

            link.attr("x1", d => d.source.x)
                .attr("y1", d => d.source.y)
                .attr("x2", d => d.target.x)
                .attr("y2", d => d.target.y);
        });

        // zoom-to-fit helper
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

        // Give simulation a moment to settle, then fit
        setTimeout(zoomToFit, 600);

        // double-click to reset zoom
        svg.on("dblclick", () => {
            svg.transition().duration(400).call(zoom.transform, d3.zoomIdentity);
        });
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

                <!-- Indicators -->
                <div class="node-popup-indicators">
                    <div class="node-popup-label">Risk Indicators</div>
                    <div class="node-popup-indicators-list">
                        ${indicatorsList}
                    </div>
                </div>

                <!-- Actions -->
                <div class="node-popup-footer">
                    <button class="node-popup-btn-close" type="button">Close</button>
                </div>
            </div>
        `;

        const closeBtn = box.querySelector(".node-popup-btn-close");
        if (closeBtn) {
            closeBtn.onclick = () => {
                box.style.display = "none";
            };
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

});