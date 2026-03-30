// customer-main.js v5.7 – FULL FIX – LIMIT NODE IN BOUNDS + SEARCH + HIGHLIGHT (READ ONLY)
document.addEventListener("DOMContentLoaded", () => {

    /* ================= CONFIG ================= */
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

    /* ================= UTIL ================= */
    function filterLinks(nodes, links) {
        const ids = new Set(nodes.map(n => n.id));
        return (links || []).filter(l =>
            ids.has(l.source?.id || l.source) &&
            ids.has(l.target?.id || l.target)
        );
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
    btn.onclick = () => box.style.display = "none";

    card.appendChild(btn);
    box.appendChild(card);
}
    /* ================= RENDER GRAPH ================= */
    function render(nodes, links) {
        svg.selectAll("*").remove();
        if (simulation) simulation.stop();

        nodes = nodes || [];
        links = links || [];

        // compute actual drawing width/height from rendered SVG element
        const width = svgEl.clientWidth || 1000;
        const height = svgEl.clientHeight || 600;

        const container = svg.append("g").attr("class", "graph-container");

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
                showNodeInfo(d);
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
            .force("collision", d3.forceCollide().radius(nodeRadius + 5))
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
            });

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