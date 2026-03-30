// staff-url-analyze.js – FIX FULL (URL page)
// - Render URL-only graph
// - Analyze URL
// - Reload graph -> render URL-only -> highlight URL
// - Remove setInterval, add normalize + validate

document.addEventListener("DOMContentLoaded", () => {

    function normalizeURL(url) {
        if (!url) return null;
        // bỏ khoảng trắng + lowercase
        let s = String(url).trim().replace(/\s+/g, "").toLowerCase();
        return s.length ? s : null;
    }

    function isValidURL(url) {
        if (!url) return false;
        // check nhẹ (không quá strict)
        return /^(https?:\/\/)?([\w-]+\.)+[\w-]+(\/.*)?$/.test(url);
    }

    function renderUrlOnly() {
        if (!window.allNodes || !window.allLinks || typeof window.render !== "function") return;

        // chỉ lấy URL node
        const urlNodes = window.allNodes.filter(n => n.type === "URL");
        const urlIds = new Set(urlNodes.map(n => n.id));

        // chỉ lấy link URL ↔ URL
        const urlLinks = window.allLinks.filter(l => {
            const sid = l.source?.id || l.source;
            const tid = l.target?.id || l.target;
            return urlIds.has(sid) && urlIds.has(tid);
        });

        window.render(urlNodes, urlLinks);
    }

    async function reloadAndRenderUrlOnly(highlightURL = null) {
        // 1) Reload full graph
        if (typeof window.fetchGraph === "function") {
            await window.fetchGraph();
        }

        // 2) Render URL-only
        renderUrlOnly();

        // 3) Highlight URL if exists
        if (highlightURL && typeof window.highlightNodeValues === "function") {
            const nodes = window.allNodes || [];
            const ok = nodes.some(n => n.type === "URL" && normalizeURL(n.value) === highlightURL);
            if (ok) window.highlightNodeValues([highlightURL]);
        }
    }

    /* ================= INIT ================= */
    setTimeout(() => {
        renderUrlOnly();
    }, 0);

    /* ================= ANALYZE URL ================= */
    const analyzeBtn = document.getElementById("analyzeBtn");
    const resultDiv = document.getElementById("analyzeResult");
    const urlInput = document.getElementById("analyzeURL");

    if (!analyzeBtn || !urlInput) return;

    analyzeBtn.addEventListener("click", async () => {

        const url = normalizeURL(urlInput.value);

        if (!url) {
            alert("Nhập URL để phân tích!");
            return;
        }

        if (!isValidURL(url)) {
            alert("URL không hợp lệ! Ví dụ đúng: https://example.com");
            return;
        }

        try {
            analyzeBtn.disabled = true;

            const res = await fetch("/staff/analyze", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ url })
            });

            if (res.status === 403) {
                alert("Phiên staff đã hết hạn!");
                return;
            }

            if (!res.ok) throw new Error("Phân tích thất bại!");

            const data = await res.json();

            // Render kết quả (chuẩn staff-main.js)
            if (typeof window.renderAnalyzeResult === "function") {
                window.renderAnalyzeResult(data, resultDiv);
            } else if (resultDiv) {
                // fallback nếu thiếu hàm
                resultDiv.innerHTML = `
                    <div style="padding:12px;border-radius:10px;background:#fff;box-shadow:0 8px 24px rgba(0,0,0,.2)">
                        <b>Kết quả phân tích URL</b>
                        <hr>
                        <div><b>Verdict:</b> ${data.verdict ?? "—"}</div>
                        <div><b>Risk score:</b> ${data.riskScore ?? 0}</div>
                        <div><b>Risk level:</b> ${data.riskLevel ?? "—"}</div>
                        <div><b>Indicators:</b> ${(data.indicators || []).join(", ") || "Không có"}</div>
                    </div>
                `;
            }

            // Reload graph -> render URL-only -> highlight URL
            await reloadAndRenderUrlOnly(url);

        } catch (e) {
            console.error(e);
            alert(e?.message || "Phân tích URL thất bại");
        } finally {
            analyzeBtn.disabled = false;
        }
    });

});