function detectRoleBasePath() {
    const path = window.location.pathname || "";
    if (path.startsWith("/admin")) return "/admin";
    if (path.startsWith("/staff")) return "/staff";
    return "/customer";
}

function canManualBlock() {
    return detectRoleBasePath() === "/admin";
}

function ensureEnhancedPopupStyles() {
    if (document.getElementById("enhanced-popup-styles")) return;

    const style = document.createElement("style");
    style.id = "enhanced-popup-styles";
    style.textContent = `
        #nodeInfo .node-popup-premium {
            width: min(680px, 94vw);
            max-height: min(88vh, 980px);
            overflow: auto;
            background:
                radial-gradient(circle at top right, rgba(59,130,246,0.10), transparent 26%),
                linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
            border-radius: 28px;
            padding: 30px;
            color: #0f172a;
            border: 1px solid rgba(148,163,184,0.22);
            box-shadow: 0 26px 70px rgba(15,23,42,0.18), 0 8px 24px rgba(59,130,246,0.10);
            display: flex;
            flex-direction: column;
            gap: 18px;
        }
        #nodeInfo .node-popup-premium.low { box-shadow: 0 26px 70px rgba(15,23,42,0.16), 0 8px 24px rgba(16,185,129,0.12); }
        #nodeInfo .node-popup-premium.medium { box-shadow: 0 26px 70px rgba(15,23,42,0.16), 0 8px 24px rgba(245,158,11,0.15); }
        #nodeInfo .node-popup-premium.high { box-shadow: 0 26px 70px rgba(15,23,42,0.16), 0 8px 24px rgba(239,68,68,0.16); }
        #nodeInfo .node-popup-header { display:flex; justify-content:space-between; align-items:flex-start; gap:16px; padding-bottom:16px; border-bottom:1px solid rgba(148,163,184,0.18); }
        #nodeInfo .node-popup-header-left { display:flex; gap:14px; align-items:center; min-width:0; }
        #nodeInfo .node-popup-icon { width:52px; height:52px; border-radius:16px; display:flex; align-items:center; justify-content:center; font-size:24px; background:linear-gradient(135deg, rgba(99,102,241,0.16), rgba(59,130,246,0.12)); border:1px solid rgba(99,102,241,0.22); flex:0 0 auto; }
        #nodeInfo .node-popup-type { font-size:17px; font-weight:800; line-height:1.2; }
        #nodeInfo .node-popup-status { margin-top:5px; font-size:12px; color:#64748b; font-weight:600; }
        #nodeInfo .node-popup-risk-badge { padding:10px 16px; border-radius:999px; font-size:11px; font-weight:900; letter-spacing:1.1px; text-transform:uppercase; }
        #nodeInfo .node-popup-risk-low { background:#dcfce7; color:#166534; border:1px solid #86efac; }
        #nodeInfo .node-popup-risk-medium { background:#fef3c7; color:#92400e; border:1px solid #fcd34d; }
        #nodeInfo .node-popup-risk-high { background:#fee2e2; color:#991b1b; border:1px solid #fca5a5; }
        #nodeInfo .node-popup-content-grid { display:grid; grid-template-columns:repeat(3, minmax(0,1fr)); gap:12px; }
        #nodeInfo .node-popup-content-item,
        #nodeInfo .node-popup-section {
            background:#ffffff;
            border:1px solid rgba(191,219,254,0.9);
            border-radius:16px;
            padding:16px 18px;
            box-shadow: inset 0 1px 0 rgba(255,255,255,0.7);
        }
        #nodeInfo .node-popup-content-item {
            min-height:92px;
            display:flex;
            flex-direction:column;
            justify-content:space-between;
        }
        #nodeInfo .node-popup-label { font-size:11px; font-weight:800; text-transform:uppercase; letter-spacing:1.4px; color:#6b7280; margin-bottom:8px; }
        #nodeInfo .node-popup-value { font-size:15px; font-weight:800; line-height:1.4; word-break:break-word; }
        #nodeInfo .node-popup-id-section {
            background:linear-gradient(180deg, rgba(239,246,255,0.95), rgba(248,250,252,0.95));
            border:1px solid rgba(147,197,253,0.85);
            border-left:4px solid #60a5fa;
            border-radius:16px;
            padding:16px 18px;
        }
        #nodeInfo .node-popup-id-value {
            display:block;
            margin-top:8px;
            background:rgba(255,255,255,0.7);
            border:1px solid rgba(191,219,254,0.85);
            border-radius:10px;
            padding:10px 12px;
            font-size:12px;
            line-height:1.55;
            color:#334155;
            word-break:break-all;
        }
        #nodeInfo .node-popup-indicators { background:#ffffff; border:1px solid rgba(191,219,254,0.9); border-radius:16px; padding:16px 18px; }
        #nodeInfo .node-popup-indicators-list { display:flex; flex-wrap:wrap; gap:10px; margin-top:12px; }
        #nodeInfo .node-popup-badge,
        #nodeInfo .node-popup-badge-empty {
            display:inline-flex;
            align-items:center;
            max-width:100%;
            border-radius:999px;
            padding:8px 12px;
            font-size:12px;
            font-weight:700;
            line-height:1.4;
        }
        #nodeInfo .node-popup-badge { background:linear-gradient(180deg, #fff5f5, #fff1f2); border:1px solid #fda4af; color:#dc2626; }
        #nodeInfo .node-popup-badge-empty { background:#ecfdf5; border:1px solid #86efac; color:#16a34a; }
        #nodeInfo .node-popup-section {
            display:flex;
            flex-direction:column;
            gap:10px;
            position:relative;
            overflow:hidden;
            padding-left:20px;
        }
        #nodeInfo .node-popup-section::before {
            content:"";
            position:absolute;
            left:0;
            top:0;
            bottom:0;
            width:5px;
            border-radius:16px 0 0 16px;
            background:linear-gradient(180deg, #93c5fd, #60a5fa);
        }
        #nodeInfo .node-popup-section.section-low::before {
            background:linear-gradient(180deg, #86efac, #22c55e);
        }
        #nodeInfo .node-popup-section.section-medium::before {
            background:linear-gradient(180deg, #fcd34d, #f59e0b);
        }
        #nodeInfo .node-popup-section.section-high::before {
            background:linear-gradient(180deg, #fda4af, #ef4444);
        }
        #nodeInfo .node-popup-section.section-neutral::before {
            background:linear-gradient(180deg, #cbd5e1, #94a3b8);
        }
        #nodeInfo .node-popup-section-header { display:flex; align-items:center; justify-content:space-between; gap:10px; }
        #nodeInfo .node-popup-section-title { font-size:13px; font-weight:800; color:#0f172a; }
        #nodeInfo .node-popup-section-kicker { font-size:10px; letter-spacing:1.2px; font-weight:800; text-transform:uppercase; color:#64748b; }
        #nodeInfo .node-popup-section-body { font-size:13px; color:#334155; line-height:1.65; white-space:pre-wrap; }
        #nodeInfo .node-popup-list { margin:0; padding-left:18px; color:#334155; }
        #nodeInfo .node-popup-list li { margin:6px 0; line-height:1.55; }
        #nodeInfo .node-popup-footer { display:flex; justify-content:flex-end; gap:12px; padding-top:18px; border-top:1px solid rgba(148,163,184,0.18); }
        #nodeInfo .node-popup-btn-close,
        #nodeInfo .node-popup-btn-block {
            border:none;
            border-radius:14px;
            padding:12px 22px;
            font-size:14px;
            font-weight:800;
            cursor:pointer;
            transition:transform .18s ease, box-shadow .18s ease, background .18s ease;
        }
        #nodeInfo .node-popup-btn-close {
            background:linear-gradient(180deg, #dbeafe, #eff6ff);
            color:#1e293b;
            border:1px solid rgba(96,165,250,0.6);
        }
        #nodeInfo .node-popup-btn-block {
            background:linear-gradient(180deg, #4f46e5, #2563eb);
            color:#ffffff;
            box-shadow:0 12px 24px rgba(37,99,235,0.25);
        }
        #nodeInfo .node-popup-btn-close:hover,
        #nodeInfo .node-popup-btn-block:hover { transform:translateY(-1px); }
        #nodeInfo .node-popup-stack { display:flex; flex-direction:column; gap:12px; }
        #nodeInfo .node-popup-meta { display:flex; flex-wrap:wrap; gap:8px; }
        #nodeInfo .node-popup-mini-chip { display:inline-flex; align-items:center; gap:6px; padding:5px 10px; border-radius:999px; background:#f8fafc; border:1px solid #e2e8f0; font-size:11px; color:#475569; font-weight:700; }
        @media (max-width: 700px) {
            #nodeInfo .node-popup-premium { padding:20px; border-radius:22px; }
            #nodeInfo .node-popup-content-grid { grid-template-columns:1fr; }
            #nodeInfo .node-popup-footer { flex-direction:column-reverse; }
            #nodeInfo .node-popup-btn-close,
            #nodeInfo .node-popup-btn-block { width:100%; }
        }
    `;
    document.head.appendChild(style);
}

async function enhancedShowNodeInfo(d) {
    const box = document.getElementById("nodeInfo");
    if (!box) return;
    ensureEnhancedPopupStyles();

    const basePath = detectRoleBasePath();
    box.style.display = "flex";
    box.innerHTML = '<div class="node-popup-premium low"><div class="node-popup-id-section"><div class="node-popup-value">Loading analysis...</div></div></div>';

    try {
        const query = `nodeId=${encodeURIComponent(d.id)}&nodeType=${encodeURIComponent(d.type)}&nodeValue=${encodeURIComponent(d.value)}&riskLevel=${encodeURIComponent(d.riskLevel)}&riskScore=${encodeURIComponent(d.riskScore ?? 0)}`;

        const decisionRes = await fetch(`${basePath}/node-decision?${query}`, { method: "POST" });
        if (!decisionRes.ok) throw new Error(`Decision request failed: HTTP ${decisionRes.status}`);
        const decision = await decisionRes.json();

        const chatbotRes = await fetch(`${basePath}/node-analysis?${query}`, { method: "POST" });
        if (!chatbotRes.ok) throw new Error(`Analysis request failed: HTTP ${chatbotRes.status}`);
        const chatbot = await chatbotRes.json();

        renderEnhancedNodeInfo(d, decision, chatbot);
    } catch (e) {
        console.error("enhancedShowNodeInfo error:", e);
        renderEnhancedNodeInfo(d, null, {
            analysisDescription: "Khong tai duoc phan giai thich node.",
            riskAssessment: e?.message || "Unknown error",
            specificDangers: [],
            recommendedActions: [],
            graphIntelligence: "Khong co du lieu bo sung.",
            relatedNodes: []
        });
    }
}

function renderEnhancedNodeInfo(nodeData, decision, chatbot) {
    const box = document.getElementById("nodeInfo");
    if (!box) return;

    const riskClass = String(nodeData.riskLevel || "low").toLowerCase().replace(/\s+/g, "-");
    const sectionRiskClass = getSectionRiskClass(nodeData.riskLevel);
    const typeIcon = iconForType(nodeData.type);
    const indicators = Array.isArray(nodeData.indicators) ? nodeData.indicators : [];
    const relatedNodes = Array.isArray(chatbot?.relatedNodes) ? chatbot.relatedNodes : [];
    const graphRelations = getGraphRelations(nodeData.id);
    const relationsHtml = graphRelations.length > 0
        ? graphRelations.slice(0, 10).map(item =>
            `<span class="node-popup-badge">${escapeHtml(item.label)}</span>`
        ).join("")
        : (relatedNodes.length > 0
            ? relatedNodes.slice(0, 8).map(node =>
                `<span class="node-popup-badge">${escapeHtml(node.relationship || "RELATED")} → ${escapeHtml(node.nodeType)}: ${escapeHtml(node.nodeValue || node.nodeId || "Unknown")}</span>`
            ).join("")
            : '<span class="node-popup-badge-empty">— Không có liên kết trong graph hiện tại</span>');

    const indicatorHtml = indicators.length > 0
        ? indicators.map(ind => `<span class="node-popup-badge">${escapeHtml(ind)}</span>`).join("")
        : '<span class="node-popup-badge-empty">✓ No Issues</span>';

    const decisionHtml = decision
        ? `
            <div class="node-popup-content-item">
                <div class="node-popup-label">Decision</div>
                <div class="node-popup-value">${escapeHtml(decision.decision || "ALLOW")}</div>
            </div>
        `
        : "";

    box.innerHTML = `
        <div class="node-popup-premium ${riskClass}">
            <div class="node-popup-header">
                <div class="node-popup-header-left">
                    <span class="node-popup-icon">${typeIcon}</span>
                    <div>
                        <div class="node-popup-type">${escapeHtml(nodeData.type || "Unknown")}</div>
                        <div class="node-popup-status">Status: <b>${escapeHtml(nodeData.status || "—")}</b></div>
                    </div>
                </div>
                <span class="node-popup-risk-badge node-popup-risk-${riskClass}">
                    ${escapeHtml(String(nodeData.riskLevel || "Unknown").toUpperCase())}
                </span>
            </div>

            <div class="node-popup-content-grid">
                <div class="node-popup-content-item">
                    <div class="node-popup-label">Value</div>
                    <div class="node-popup-value">${escapeHtml(nodeData.value || "—")}</div>
                </div>
                <div class="node-popup-content-item">
                    <div class="node-popup-label">Verdict</div>
                    <div class="node-popup-value node-popup-verdict-${normalizeVerdictClass(nodeData.verdict)}">${escapeHtml(nodeData.verdict || "—")}</div>
                </div>
                <div class="node-popup-content-item">
                    <div class="node-popup-label">Risk Score</div>
                    <div class="node-popup-value node-popup-score-display">
                        <span class="node-popup-score-num">${escapeHtml(String(nodeData.riskScore ?? 0))}</span>
                        <span class="node-popup-score-max">/100</span>
                    </div>
                </div>
                ${decisionHtml}
            </div>

            <div class="node-popup-id-section">
                <div class="node-popup-label">Node ID</div>
                <code class="node-popup-id-value">${escapeHtml(nodeData.id || "—")}</code>
            </div>

            <div class="node-popup-indicators">
                <div class="node-popup-label">Risk Indicators</div>
                <div class="node-popup-indicators-list">
                    ${indicatorHtml}
                </div>
            </div>

            <div class="node-popup-indicators">
                <div class="node-popup-label">Liên kết (highlight trên đồ thị)</div>
                <div class="node-popup-indicators-list">
                    ${relationsHtml}
                </div>
            </div>

            <div class="node-popup-stack">
            ${renderTextBlock("Phân tích node", chatbot?.analysisDescription, "Analysis", sectionRiskClass)}
            ${renderTextBlock("Đánh giá rủi ro", chatbot?.riskAssessment, "Risk", sectionRiskClass)}
            ${renderDangerBlock(chatbot?.specificDangers, sectionRiskClass)}
            ${renderTextBlock("Giải thích ngữ cảnh graph", chatbot?.graphIntelligence, "Graph", sectionRiskClass)}
            ${renderActionBlock(chatbot?.recommendedActions, sectionRiskClass)}
            ${renderDecisionReasonBlock(decision, sectionRiskClass)}
            </div>

            <div class="node-popup-footer">
                ${canManualBlock() && String(nodeData.type || "").toLowerCase() !== "analysissession"
                    ? `<button class="node-popup-btn-block" type="button">${nodeData.manualBlocked ? "Bỏ chặn" : "Chặn"}</button>`
                    : ""}
                <button class="node-popup-btn-close" type="button">Close</button>
            </div>
        </div>
    `;

    bindPopupActions(box, nodeData);
}

function bindPopupActions(box, nodeData) {
    const closeBtn = box.querySelector(".node-popup-btn-close");
    if (closeBtn) {
        closeBtn.onclick = () => {
            box.style.display = "none";
        };
    }

    const blockBtn = box.querySelector(".node-popup-btn-block");
    if (!blockBtn) return;

    blockBtn.onclick = async () => {
        const id = nodeData.id;
        if (!id) return;

        const isBlocked = !!nodeData.manualBlocked;
        try {
            if (!isBlocked) {
                const reason = prompt("Ly do chan (tuy chon):", nodeData.manualBlockReason || "");
                if (reason === null) return;

                const res = await fetch(`/admin/node/${encodeURIComponent(id)}/block`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ reason })
                });
                const data = await res.json().catch(() => ({}));
                if (!res.ok) throw new Error(data.message || `HTTP ${res.status}`);
                alert("Chan thanh cong");
            } else {
                if (!confirm("Bo chan node nay?")) return;
                const res = await fetch(`/admin/node/${encodeURIComponent(id)}/unblock`, { method: "POST" });
                const data = await res.json().catch(() => ({}));
                if (!res.ok) throw new Error(data.message || `HTTP ${res.status}`);
                alert("Bo chan thanh cong");
            }
            box.style.display = "none";
            if (typeof window.fetchGraph === "function") {
                window.fetchGraph(null, { force: true });
            }
        } catch (e) {
            console.error("Block/unblock failed:", e);
            alert(`That bai: ${e.message || e}`);
        }
    };
}

function renderTextBlock(label, text, kicker = "Details", sectionRiskClass = "section-neutral") {
    if (!text) return "";
    return `
        <div class="node-popup-section ${sectionRiskClass}">
            <div class="node-popup-section-header">
                <div class="node-popup-section-kicker">${escapeHtml(kicker)}</div>
                <div class="node-popup-section-title">${escapeHtml(label)}</div>
            </div>
            <div class="node-popup-section-body">${escapeHtml(text)}</div>
        </div>
    `;
}

function renderDangerBlock(items, sectionRiskClass = "section-neutral") {
    if (!Array.isArray(items) || items.length === 0) return "";
    return `
        <div class="node-popup-section ${sectionRiskClass}">
            <div class="node-popup-section-header">
                <div class="node-popup-section-kicker">Threats</div>
                <div class="node-popup-section-title">Nguy cơ chính</div>
            </div>
            <div class="node-popup-indicators-list">
                ${items.slice(0, 6).map(item => `<span class="node-popup-badge">${escapeHtml(item)}</span>`).join("")}
            </div>
        </div>
    `;
}

function renderActionBlock(items, sectionRiskClass = "section-neutral") {
    if (!Array.isArray(items) || items.length === 0) return "";
    return `
        <div class="node-popup-section ${sectionRiskClass}">
            <div class="node-popup-section-header">
                <div class="node-popup-section-kicker">Actions</div>
                <div class="node-popup-section-title">Khuyến nghị</div>
            </div>
            <ol class="node-popup-list">
                ${items.slice(0, 6).map(item => `<li>${escapeHtml(item)}</li>`).join("")}
            </ol>
        </div>
    `;
}

function renderDecisionReasonBlock(decision, sectionRiskClass = "section-neutral") {
    if (!decision) return "";
    const lines = [];
    if (decision.reason) lines.push(decision.reason);
    if (decision.actionDescription) lines.push(stripHtml(decision.actionDescription));
    if (lines.length === 0) return "";
    return renderTextBlock("Quyết định hệ thống", lines.join("\n"), "Decision", sectionRiskClass);
}

function getSectionRiskClass(riskLevel) {
    const normalized = String(riskLevel || "").toLowerCase();
    if (normalized === "high") return "section-high";
    if (normalized === "medium") return "section-medium";
    if (normalized === "low") return "section-low";
    return "section-neutral";
}

function getGraphRelations(nodeId) {
    const context = window.__graphContext;
    if (!context || !Array.isArray(context.links) || !Array.isArray(context.nodes)) {
        return [];
    }

    const nodeMap = new Map(context.nodes.map(node => [node.id, node]));
    const linkLabel = typeof context.linkTypeLabel === "function"
        ? context.linkTypeLabel
        : (type => type || "RELATED");

    return context.links
        .filter(link => safeGraphId(link.source) === nodeId || safeGraphId(link.target) === nodeId)
        .map(link => {
            const sourceId = safeGraphId(link.source);
            const targetId = safeGraphId(link.target);
            const otherId = sourceId === nodeId ? targetId : sourceId;
            const otherNode = nodeMap.get(otherId);
            const direction = sourceId === nodeId ? "→" : "←";
            const otherLabel = otherNode
                ? `${otherNode.type || "Node"}: ${otherNode.value || otherNode.id || otherId}`
                : (otherId || "Unknown");

            return {
                label: `${linkLabel(link.type)} ${direction} ${otherLabel}`
            };
        });
}

function safeGraphId(value) {
    if (!value) return null;
    if (typeof value === "string") return value;
    if (typeof value === "object" && value.id) return value.id;
    return null;
}

function normalizeVerdictClass(verdict) {
    return String(verdict || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .replace(/\s+/g, "_")
        .toUpperCase();
}

function iconForType(type) {
    return {
        Email: "📧",
        URL: "🔗",
        IPAddress: "🌐",
        Domain: "📋",
        FileNode: "📄",
        FileHash: "🔐",
        VictimAccount: "👤",
        AnalysisSession: "🧭"
    }[type] || "📋";
}

function escapeHtml(value) {
    return String(value ?? "")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function stripHtml(value) {
    const div = document.createElement("div");
    div.innerHTML = value || "";
    return div.textContent || div.innerText || "";
}

window.enhancedShowNodeInfo = enhancedShowNodeInfo;
