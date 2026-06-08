// Enhanced Node Analysis - Integrates Decision, Chatbot, and Logging
// This file handles the new UI features for displaying decisions, detailed chatbot analysis, and logs

function closeCustomerNodePopup() {
    const nodeInfo = document.getElementById("nodeInfo");
    if (nodeInfo) {
        nodeInfo.style.display = "none";
        nodeInfo.innerHTML = "";
    }

    const detailsPanel = document.getElementById("nodeDetailsPanel");
    if (detailsPanel) {
        detailsPanel.classList.remove("open");
        detailsPanel.setAttribute("aria-hidden", "true");
    }

    window.selectedNodeId = null;
    if (window.__graphContext) {
        window.__graphContext.selectedNodeId = null;
    }
}

document.addEventListener("click", (event) => {
    const closeButton = event.target.closest(
        "#closeNodeInfo, .node-popup-btn-close, #closeNodePanel, .close-panel"
    );
    if (!closeButton) return;

    event.preventDefault();
    event.stopPropagation();
    closeCustomerNodePopup();
}, true);

/**
 * Enhanced showNodeInfo function - replaces the simple version
 * Displays: Decision + Chatbot Analysis + Recommendations
 */
async function enhancedShowNodeInfo(d) {
    const box = document.getElementById("nodeInfo");
    if (!box) return;

    box.style.display = "flex";
    box.innerHTML = '<div style="text-align:center;padding:20px;"><p>⏳ Loading analysis...</p></div>';

    try {
        // Fetch decision
        const decisionRes = await fetch(
            `/customer/node-decision?nodeId=${d.id}&nodeType=${d.type}&nodeValue=${encodeURIComponent(d.value)}&riskLevel=${d.riskLevel}&riskScore=${d.riskScore}`,
            { method: "POST" }
        );
        if (!decisionRes.ok) {
            throw new Error(`Decision request failed: HTTP ${decisionRes.status}`);
        }
        const decision = await decisionRes.json();

        // Fetch chatbot analysis
        const chatbotRes = await fetch(
            `/customer/node-analysis?nodeId=${d.id}&nodeType=${d.type}&nodeValue=${encodeURIComponent(d.value)}&riskLevel=${d.riskLevel}&riskScore=${d.riskScore}`,
            { method: "POST" }
        );
        if (!chatbotRes.ok) {
            throw new Error(`Analysis request failed: HTTP ${chatbotRes.status}`);
        }
        const chatbot = await chatbotRes.json();

        // Render the complete UI
        renderEnhancedNodeInfo(d, decision, chatbot);

    } catch (e) {
        console.error("enhancedShowNodeInfo error:", e);
        box.innerHTML = '<div style="color:red;padding:20px;"><p>❌ Error loading analysis</p></div>';
    }
}

/**
 * Render the enhanced node info panel with all 4 layers + decision
 */
function renderEnhancedNodeInfo(nodeData, decision, chatbot) {
    const box = document.getElementById("nodeInfo");
    box.innerHTML = "";

    const card = document.createElement("div");
    card.className = "node-popup-card";
    card.style.cssText = "max-height: 80vh; overflow-y: auto; font-size: 13px;";

    // ========== HEADER ==========
    const header = document.createElement("div");
    header.style.cssText = "border-bottom: 2px solid #007bff; padding-bottom: 10px; margin-bottom: 15px;";
    
    const h3 = document.createElement("h3");
    h3.style.cssText = "margin: 0 0 10px 0; color: #0f172a;";
    h3.textContent = `${nodeData.type}: ${nodeData.value}`;
    header.appendChild(h3);

    const idSpan = document.createElement("small");
    idSpan.style.cssText = "color: #64748b;";
    idSpan.textContent = `ID: ${nodeData.id}`;
    header.appendChild(idSpan);
    card.appendChild(header);

    // ========== DECISION SECTION ==========
    if (decision) {
        const decisionSection = document.createElement("div");
        decisionSection.style.cssText = "background: #f0f9ff; border-left: 4px solid #0ea5e9; padding: 10px; margin-bottom: 15px; border-radius: 4px;";

        const decisionTitle = document.createElement("h4");
        decisionTitle.style.cssText = "margin: 0 0 8px 0; color: #0c63e4;";
        
        const decisionBadge = getDecisionBadge(decision.decision);
        decisionTitle.innerHTML = `Decision: ${decisionBadge}`;
        decisionSection.appendChild(decisionTitle);

        const decisionReason = document.createElement("p");
        decisionReason.style.cssText = "margin: 0 0 8px 0; color: #333;";
        decisionReason.textContent = decision.reason;
        decisionSection.appendChild(decisionReason);

        const decisionAction = document.createElement("p");
        decisionAction.style.cssText = "margin: 0; color: #555; font-weight: 500; line-height: 1.5;";
        decisionAction.innerHTML = decision.actionDescription;
        decisionSection.appendChild(decisionAction);

        card.appendChild(decisionSection);
    }

    // ========== LAYER 1: ANALYSIS ==========
    if (chatbot) {
        const section1 = createAnalysisSection(chatbot);
        card.appendChild(section1);

        // ========== LAYER 2: RISK ASSESSMENT ==========
        const section2 = createRiskSection(chatbot);
        card.appendChild(section2);

        // ========== LAYER 3: THREAT EXPLANATION ==========
        const section3 = createThreatSection(chatbot);
        card.appendChild(section3);

        // ========== LAYER 4: RECOMMENDED ACTIONS ==========
        const section4 = createActionsSection(chatbot);
        card.appendChild(section4);

        // ========== GRAPH INTELLIGENCE ==========
        const section5 = createGraphIntelligenceSection(chatbot);
        card.appendChild(section5);
    }

    // ========== CLOSE BUTTON ==========
    const btn = document.createElement("button");
    btn.id = "closeNodeInfo";
    btn.className = "node-popup-btn-close";
    btn.type = "button";
    btn.textContent = "Close";
    btn.style.cssText = "width: 100%; padding: 8px; margin-top: 15px; background: #64748b; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: 600;";
    btn.onclick = closeCustomerNodePopup;
    card.appendChild(btn);

    box.appendChild(card);
}

/**
 * Get badge HTML for decision
 */
function getDecisionBadge(decision) {
    const styles = {
        'BLOCK': { bg: '#ef4444', text: 'white' },
        'MONITOR': { bg: '#f59e0b', text: 'white' },
        'ALLOW': { bg: '#10b981', text: 'white' }
    };
    const style = styles[decision] || styles['ALLOW'];
    return `<span style="background: ${style.bg}; color: ${style.text}; padding: 4px 12px; border-radius: 4px; font-weight: 600;">${decision}</span>`;
}

/**
 * Layer 1: Analysis
 */
function createAnalysisSection(chatbot) {
    const section = document.createElement("div");
    section.style.cssText = "background: #f9fafb; border-left: 4px solid #8b5cf6; padding: 10px; margin-bottom: 12px; border-radius: 4px;";

    const title = document.createElement("h4");
    title.style.cssText = "margin: 0 0 8px 0; color: #6d28d9;";
    title.textContent = "📋 Analysis";
    section.appendChild(title);

    const status = document.createElement("p");
    status.style.cssText = "margin: 0 0 6px 0; font-weight: 600;";
    status.textContent = `Status: ${chatbot.status}`;
    section.appendChild(status);

    const content = document.createElement("p");
    content.style.cssText = "margin: 0; color: #555; line-height: 1.4; white-space: pre-wrap;";
    content.textContent = chatbot.analysisDescription || "No description available";
    section.appendChild(content);

    return section;
}

/**
 * Layer 2: Risk Assessment
 */
function createRiskSection(chatbot) {
    const section = document.createElement("div");
    section.style.cssText = "background: #fffbeb; border-left: 4px solid #f59e0b; padding: 10px; margin-bottom: 12px; border-radius: 4px;";

    const title = document.createElement("h4");
    title.style.cssText = "margin: 0 0 8px 0; color: #b45309;";
    title.textContent = "⚠️ Risk Assessment";
    section.appendChild(title);

    const content = document.createElement("p");
    content.style.cssText = "margin: 0; color: #555; line-height: 1.5; white-space: pre-wrap;";
    content.textContent = chatbot.riskAssessment || "No risk assessment available";
    section.appendChild(content);

    return section;
}

/**
 * Layer 3: Threat Explanation
 */
function createThreatSection(chatbot) {
    const section = document.createElement("div");
    section.style.cssText = "background: #fee2e2; border-left: 4px solid #ef4444; padding: 10px; margin-bottom: 12px; border-radius: 4px;";

    const title = document.createElement("h4");
    title.style.cssText = "margin: 0 0 8px 0; color: #b91c1c;";
    title.textContent = "🔥 Threat Explanation";
    section.appendChild(title);

    const narrative = document.createElement("p");
    narrative.style.cssText = "margin: 0 0 10px 0; color: #555; line-height: 1.4;";
    narrative.textContent = chatbot.threatExplanation || "No threat information";
    section.appendChild(narrative);

    if (chatbot.specificDangers && chatbot.specificDangers.length > 0) {
        const dangerTitle = document.createElement("p");
        dangerTitle.style.cssText = "margin: 8px 0 4px 0; font-weight: 600; color: #333;";
        dangerTitle.textContent = "Specific Dangers:";
        section.appendChild(dangerTitle);

        const list = document.createElement("ul");
        list.style.cssText = "margin: 0; padding-left: 20px; color: #555;";
        chatbot.specificDangers.slice(0, 5).forEach(danger => {
            const li = document.createElement("li");
            li.style.cssText = "margin: 2px 0;";
            li.textContent = danger;
            list.appendChild(li);
        });
        section.appendChild(list);
    }

    return section;
}

/**
 * Layer 4: Recommended Actions
 */
function createActionsSection(chatbot) {
    const section = document.createElement("div");
    section.style.cssText = "background: #dbeafe; border-left: 4px solid #0ea5e9; padding: 10px; margin-bottom: 12px; border-radius: 4px;";

    const title = document.createElement("h4");
    title.style.cssText = "margin: 0 0 8px 0; color: #0369a1;";
    title.textContent = "✅ Recommended Actions";
    section.appendChild(title);

    if (chatbot.recommendedActions && chatbot.recommendedActions.length > 0) {
        const list = document.createElement("ol");
        list.style.cssText = "margin: 0; padding-left: 20px; color: #555;";
        chatbot.recommendedActions.forEach(action => {
            const li = document.createElement("li");
            li.style.cssText = "margin: 4px 0; line-height: 1.4;";
            li.innerHTML = action;
            list.appendChild(li);
        });
        section.appendChild(list);
    }

    return section;
}

/**
 * Graph Intelligence - Related Nodes
 */
function createGraphIntelligenceSection(chatbot) {
    const section = document.createElement("div");
    section.style.cssText = "background: #f0fdf4; border-left: 4px solid #10b981; padding: 10px; margin-bottom: 12px; border-radius: 4px;";

    const title = document.createElement("h4");
    title.style.cssText = "margin: 0 0 8px 0; color: #166534;";
    title.textContent = "🔗 Graph Intelligence";
    section.appendChild(title);

    const intelligence = document.createElement("p");
    intelligence.style.cssText = "margin: 0 0 10px 0; color: #555; line-height: 1.4; white-space: pre-wrap;";
    intelligence.textContent = chatbot.graphIntelligence || "No related nodes detected";
    section.appendChild(intelligence);

    if (chatbot.relatedNodes && chatbot.relatedNodes.length > 0) {
        const relatedTitle = document.createElement("p");
        relatedTitle.style.cssText = "margin: 8px 0 4px 0; font-weight: 600; color: #333;";
        relatedTitle.textContent = `Related Nodes (${chatbot.relatedNodes.length}):`;
        section.appendChild(relatedTitle);

        chatbot.relatedNodes.slice(0, 5).forEach(relNode => {
            const nodeDiv = document.createElement("div");
            nodeDiv.style.cssText = "background: white; border: 1px solid #d1d5db; padding: 6px; margin: 4px 0; border-radius: 3px; font-size: 12px;";

            const nodeInfo = document.createElement("p");
            nodeInfo.style.cssText = "margin: 0 0 2px 0; font-weight: 600;";
            nodeInfo.textContent = `${relNode.nodeType}: ${relNode.nodeValue}`;
            nodeDiv.appendChild(nodeInfo);

            const details = document.createElement("p");
            details.style.cssText = "margin: 0; color: #666; font-size: 11px;";
            details.innerHTML = `Risk: <span style="color: ${getRiskColor(relNode.riskLevel)}">${relNode.riskLevel}</span> | Via: ${relNode.relationship || 'unknown'}<br/>Reason: ${relNode.reason || 'N/A'}`;
            nodeDiv.appendChild(details);

            section.appendChild(nodeDiv);
        });

        if (chatbot.relatedNodes.length > 5) {
            const more = document.createElement("p");
            more.style.cssText = "margin: 4px 0 0 0; color: #888; font-size: 11px;";
            more.textContent = `+${chatbot.relatedNodes.length - 5} more related nodes`;
            section.appendChild(more);
        }
    }

    return section;
}

/**
 * Get color for risk level
 */
function getRiskColor(riskLevel) {
    const colors = {
        'HIGH': '#ef4444',
        'MEDIUM': '#f59e0b',
        'LOW': '#10b981'
    };
    return colors[riskLevel?.toUpperCase()] || '#666';
}

/**
 * Display Logs Dashboard
 */
async function displayLogsDashboard() {
    try {
        // Fetch statistics
        const statsRes = await fetch('/customer/log-statistics');
        const statsData = await statsRes.json();

        // Fetch recent alerts
        const alertsRes = await fetch('/customer/alerts?limit=10');
        const alertsData = await alertsRes.json();

        // Fetch recent detections
        const detectionsRes = await fetch('/customer/detections?limit=10');
        const detectionsData = detectionsRes.json();

        // Render logs dashboard
        renderLogsDashboard(statsData.statistics, alertsData.alerts, detectionsData.detections);
    } catch (e) {
        console.error("Error loading logs:", e);
    }
}

/**
 * Render logs dashboard
 */
function renderLogsDashboard(stats, alerts, detections) {
    const container = document.getElementById("logsDashboard");
    if (!container) return;

    container.innerHTML = "";

    // Statistics cards
    const statsDiv = document.createElement("div");
    statsDiv.style.cssText = "display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin-bottom: 20px;";

    const cards = [
        { label: "Total Alerts", value: stats.totalAlerts, color: "#0ea5e9" },
        { label: "Block Actions", value: stats.blockActions, color: "#ef4444" },
        { label: "Monitor Actions", value: stats.monitorActions, color: "#f59e0b" },
        { label: "High Risk", value: stats.highRiskAlerts, color: "#dc2626" }
    ];

    cards.forEach(card => {
        const cardEl = document.createElement("div");
        cardEl.style.cssText = `background: ${card.color}; color: white; padding: 15px; border-radius: 8px; text-align: center;`;
        cardEl.innerHTML = `<h3 style="margin: 0;">${card.value}</h3><p style="margin: 5px 0 0 0; font-size: 12px;">${card.label}</p>`;
        statsDiv.appendChild(cardEl);
    });

    container.appendChild(statsDiv);

    // Recent alerts
    const alertsSection = document.createElement("div");
    alertsSection.style.cssText = "background: #f9fafb; padding: 15px; border-radius: 8px; margin-bottom: 15px;";
    alertsSection.innerHTML = "<h3>Recent Alerts</h3>";

    if (alerts && alerts.length > 0) {
        const table = document.createElement("table");
        table.style.cssText = "width: 100%; font-size: 12px; border-collapse: collapse;";
        table.innerHTML = `<tr style="background: #e5e7eb;"><th style="padding: 8px; text-align: left;">Node</th><th>Type</th><th>Risk</th><th>Decision</th><th>Time</th></tr>`;

        alerts.slice(0, 10).forEach(alert => {
            const row = table.insertRow();
            row.innerHTML = `
                <td style="padding: 8px; border-bottom: 1px solid #d1d5db;">${alert.nodeValue || alert.nodeId}</td>
                <td style="padding: 8px; border-bottom: 1px solid #d1d5db;">${alert.nodeType}</td>
                <td style="padding: 8px; border-bottom: 1px solid #d1d5db; color: ${getRiskColor(alert.riskLevel)}">${alert.riskLevel}</td>
                <td style="padding: 8px; border-bottom: 1px solid #d1d5db;"><span style="background: ${getDecisionColor(alert.decision)}; color: white; padding: 2px 6px; border-radius: 3px;">${alert.decision}</span></td>
                <td style="padding: 8px; border-bottom: 1px solid #d1d5db; color: #888; font-size: 11px;">${new Date(alert.timestamp).toLocaleString()}</td>
            `;
        });

        alertsSection.appendChild(table);
    } else {
        const p = document.createElement("p");
        p.textContent = "No recent alerts";
        alertsSection.appendChild(p);
    }

    container.appendChild(alertsSection);
}

/**
 * Get decision color
 */
function getDecisionColor(decision) {
    const colors = {
        'BLOCK': '#ef4444',
        'MONITOR': '#f59e0b',
        'ALLOW': '#10b981'
    };
    return colors[decision] || '#666';
}

// Export functions for use in HTML
window.enhancedShowNodeInfo = enhancedShowNodeInfo;
window.displayLogsDashboard = displayLogsDashboard;
