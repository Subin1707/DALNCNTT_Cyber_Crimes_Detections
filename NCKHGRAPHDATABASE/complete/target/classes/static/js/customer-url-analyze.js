// customer-url-analyze.js – v1.1 – FIX FULL (URL Only + Analyze + Refresh + Highlight)
document.addEventListener("DOMContentLoaded", () => {

    /* ================= URL ONLY GRAPH ================= */
    function renderUrlOnly() {
        if (!window.allNodes || !window.allLinks || !window.render) return;

        // chỉ lấy URL node
        const urlNodes = window.allNodes.filter(n => n.type === "URL");
        const urlIds = new Set(urlNodes.map(n => n.id));

        // chỉ lấy link URL ↔ URL
        const urlLinks = window.allLinks.filter(l =>
            urlIds.has(l.source?.id || l.source) &&
            urlIds.has(l.target?.id || l.target)
        );

        window.render(urlNodes, urlLinks);
    }

    // đợi customer-main.js load graph xong
    const waitGraph = setInterval(() => {
        if (window.allNodes && window.allLinks && window.render) {
            clearInterval(waitGraph);
            renderUrlOnly();
        }
    }, 100);

    /* ================= ANALYZE URL ONLY ================= */
    const analyzeBtn = document.getElementById("analyzeBtn");
    const resultDiv  = document.getElementById("analyzeResult");
    if (!analyzeBtn) return;

    analyzeBtn.addEventListener("click", async () => {

        const url = (document.getElementById("analyzeURL")?.value || "").trim();
        if (!url) {
            alert("Nhập URL để phân tích!");
            return;
        }

        try {
            analyzeBtn.disabled = true;
            if (resultDiv) resultDiv.innerHTML = "";

            const res = await fetch("/customer/analyze", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ url })
            });

            if (res.status === 403) {
                alert("Phiên customer đã hết hạn!");
                return;
            }

            if (!res.ok) {
                let msg = "Phân tích URL thất bại";
                try { msg = await res.text(); } catch (e) {}
                throw new Error(msg);
            }

            const data = await res.json();

            // render kết quả (đúng chuẩn window.)
            if (typeof window.renderAnalyzeResult === "function") {
                window.renderAnalyzeResult(data, resultDiv);
            }

            // reload full graph (đúng chuẩn customer-main.js)
            if (typeof window.fetchGraph === "function") {
                await window.fetchGraph(); // ⚠️ không truyền url để tránh lệch data
            }

            // render lại URL-only
            renderUrlOnly();

            // highlight URL
            if (typeof window.highlightNodeValues === "function") {
                window.highlightNodeValues([url]);
            }

        } catch (e) {
            console.error(e);
            alert(e?.message || "Phân tích URL thất bại");
        } finally {
            analyzeBtn.disabled = false;
        }
    });
});