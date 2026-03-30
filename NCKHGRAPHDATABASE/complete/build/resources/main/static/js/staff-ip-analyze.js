// staff-ip-analyze.js – FIX FULL (IP page)
// - Render IP-only graph
// - Analyze IP
// - Reload graph -> render IP-only -> highlight IP
// - Remove setInterval, add normalize + validate

document.addEventListener("DOMContentLoaded", () => {

    function normalizeIP(ip) {
        if (!ip) return null;
        // giữ lại số + dấu chấm
        const s = String(ip).trim().replace(/[^0-9.]/g, "");
        return s.length ? s : null;
    }

    function isValidIP(ip) {
        if (!ip) return false;
        return /^(25[0-5]|2[0-4]\d|[01]?\d\d?)(\.(25[0-5]|2[0-4]\d|[01]?\d\d?)){3}$/.test(ip);
    }

    function renderIPOnly() {
        if (!window.allNodes || !window.allLinks || typeof window.render !== "function") return;

        // chỉ lấy IP node
        const ipNodes = window.allNodes.filter(n => n.type === "IPAddress");
        const ipIds = new Set(ipNodes.map(n => n.id));

        // chỉ lấy link IP ↔ IP
        const ipLinks = window.allLinks.filter(l => {
            const sid = l.source?.id || l.source;
            const tid = l.target?.id || l.target;
            return ipIds.has(sid) && ipIds.has(tid);
        });

        window.render(ipNodes, ipLinks);
    }

    async function reloadAndRenderIPOnly(highlightIP = null) {
        // 1) Reload full graph data
        if (typeof window.fetchGraph === "function") {
            await window.fetchGraph();
        }

        // 2) Render only IP nodes
        renderIPOnly();

        // 3) Highlight IP if exists
        if (highlightIP && typeof window.highlightNodeValues === "function") {
            const nodes = window.allNodes || [];
            const ok = nodes.some(n => n.type === "IPAddress" && normalizeIP(n.value) === highlightIP);
            if (ok) window.highlightNodeValues([highlightIP]);
        }
    }

    /* ================= INIT ================= */
    // staff-main.js load graph async -> render lại sau 1 nhịp
    setTimeout(() => {
        renderIPOnly();
    }, 0);

    /* ================= ANALYZE IP ================= */
    const analyzeBtn = document.getElementById("analyzeBtn");
    const resultDiv = document.getElementById("analyzeResult");
    const ipInput = document.getElementById("analyzeIP");

    if (!analyzeBtn || !ipInput) return;

    analyzeBtn.addEventListener("click", async () => {

        const ip = normalizeIP(ipInput.value);

        if (!ip) {
            alert("Nhập IP để phân tích!");
            return;
        }

        if (!isValidIP(ip)) {
            alert("IP không hợp lệ! Ví dụ đúng: 192.168.1.1");
            return;
        }

        try {
            analyzeBtn.disabled = true;

            const res = await fetch("/staff/analyze", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ ip })
            });

            if (res.status === 403) {
                alert("Phiên staff đã hết hạn!");
                return;
            }

            if (!res.ok) throw new Error("Phân tích thất bại!");

            const data = await res.json();

            // Render kết quả
            if (typeof window.renderAnalyzeResult === "function") {
                window.renderAnalyzeResult(data, resultDiv);
            } else if (resultDiv) {
                // fallback nếu thiếu hàm
                resultDiv.innerHTML = `
                    <div style="padding:12px;border-radius:10px;background:#fff;box-shadow:0 8px 24px rgba(0,0,0,.2)">
                        <b>Kết quả phân tích IP</b>
                        <hr>
                        <div><b>Verdict:</b> ${data.verdict ?? "—"}</div>
                        <div><b>Risk score:</b> ${data.riskScore ?? 0}</div>
                        <div><b>Risk level:</b> ${data.riskLevel ?? "—"}</div>
                        <div><b>Indicators:</b> ${(data.indicators || []).join(", ") || "Không có"}</div>
                    </div>
                `;
            }

            // Reload graph -> render IP-only -> highlight IP
            await reloadAndRenderIPOnly(ip);

        } catch (e) {
            console.error(e);
            alert(e?.message || "Phân tích IP thất bại");
        } finally {
            analyzeBtn.disabled = false;
        }
    });

});