// staff-email-analyze.js – FIX FULL (Email page)
// - Render Email-only graph
// - Analyze Email
// - Reload graph -> render Email-only -> highlight email

document.addEventListener("DOMContentLoaded", () => {

    function normalizeEmail(email) {
        if (!email) return null;
        const s = String(email).trim().toLowerCase();
        return s.length ? s : null;
    }

    function renderEmailOnly() {
        if (!window.allNodes || !window.allLinks || typeof window.render !== "function") return;

        const emailNodes = window.allNodes.filter(n => n.type === "Email");
        const emailIds = new Set(emailNodes.map(n => n.id));

        const emailLinks = window.allLinks.filter(l => {
            const sid = l.source?.id || l.source;
            const tid = l.target?.id || l.target;
            return emailIds.has(sid) && emailIds.has(tid);
        });

        window.render(emailNodes, emailLinks);
    }

    async function reloadAndRenderEmailOnly(highlightEmail = null) {
        // 1) Reload full graph data
        if (typeof window.fetchGraph === "function") {
            await window.fetchGraph();
        }

        // 2) Render only Email nodes
        renderEmailOnly();

        // 3) Highlight email if exists
        if (highlightEmail && typeof window.highlightNodeValues === "function") {
            const nodes = window.allNodes || [];
            const ok = nodes.some(n => n.type === "Email" && normalizeEmail(n.value) === highlightEmail);
            if (ok) window.highlightNodeValues([highlightEmail]);
        }
    }

    /* ================= INIT: render Email-only after main graph loaded ================= */
    // Nếu staff-main.js load graph async, ta chờ 1 nhịp rồi render
    setTimeout(() => {
        renderEmailOnly();
    }, 0);

    /* ================= ANALYZE EMAIL ================= */
    const analyzeBtn = document.getElementById("analyzeBtn");
    const resultDiv = document.getElementById("analyzeResult");
    const emailInput = document.getElementById("analyzeEmail");

    if (!analyzeBtn || !emailInput) return;

    analyzeBtn.addEventListener("click", async () => {
        const email = normalizeEmail(emailInput.value);

        if (!email) {
            alert("Vui lòng nhập Email");
            return;
        }

        try {
            analyzeBtn.disabled = true;

            const res = await fetch("/staff/analyze", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email })
            });

            if (res.status === 403) {
                alert("Phiên staff đã hết hạn!");
                return;
            }

            if (!res.ok) throw new Error("Analyze failed");

            const data = await res.json();

            // Hiển thị kết quả (nếu staff-main.js có hàm renderAnalyzeResult)
            if (typeof window.renderAnalyzeResult === "function") {
                window.renderAnalyzeResult(data, resultDiv);
            } else if (resultDiv) {
                // fallback nếu thiếu hàm
                resultDiv.innerHTML = `
                    <div style="padding:12px;border-radius:10px;background:#fff;box-shadow:0 8px 24px rgba(0,0,0,.2)">
                        <b>Kết quả phân tích Email</b>
                        <hr>
                        <div><b>Verdict:</b> ${data.verdict ?? "—"}</div>
                        <div><b>Risk score:</b> ${data.riskScore ?? 0}</div>
                        <div><b>Risk level:</b> ${data.riskLevel ?? "—"}</div>
                        <div><b>Indicators:</b> ${(data.indicators || []).join(", ") || "Không có"}</div>
                    </div>
                `;
            }

            // Reload graph -> render Email-only -> highlight email
            await reloadAndRenderEmailOnly(email);

        } catch (e) {
            console.error(e);
            alert(e?.message || "Không thể phân tích Email");
        } finally {
            analyzeBtn.disabled = false;
        }
    });

});