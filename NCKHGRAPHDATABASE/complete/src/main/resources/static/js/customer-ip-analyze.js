// customer-ip-analyze.js – v1.1 – FIX FULL (IP Only + Analyze + Refresh + Highlight)
document.addEventListener("DOMContentLoaded", () => {

    /* ================= IP ONLY GRAPH ================= */
    function renderIPOnly() {
        if (!window.allNodes || !window.allLinks || !window.render) return;

        // chỉ lấy IP node
        const ipNodes = window.allNodes.filter(n => n.type === "IPAddress");
        const ipIds = new Set(ipNodes.map(n => n.id));

        // chỉ lấy link IP ↔ IP
        const ipLinks = window.allLinks.filter(l =>
            ipIds.has(l.source?.id || l.source) &&
            ipIds.has(l.target?.id || l.target)
        );

        window.render(ipNodes, ipLinks);
    }

    // đợi customer-main.js fetch graph xong
    const waitGraph = setInterval(() => {
        if (window.allNodes && window.allLinks && window.render) {
            clearInterval(waitGraph);
            renderIPOnly();
        }
    }, 100);

    /* ================= ANALYZE IP ================= */
    const analyzeBtn = document.getElementById("analyzeBtn");
    const resultDiv  = document.getElementById("analyzeResult");

    if (!analyzeBtn) return;

    analyzeBtn.addEventListener("click", async () => {

        const ip = (document.getElementById("analyzeIP")?.value || "").trim();
        if (!ip) {
            alert("Nhập IP để phân tích!");
            return;
        }

        try {
            analyzeBtn.disabled = true;
            if (resultDiv) resultDiv.innerHTML = "";

            const res = await fetch("/customer/analyze", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ ip })
            });

            if (res.status === 403) {
                alert("Phiên customer đã hết hạn!");
                return;
            }

            if (!res.ok) {
                let msg = "Phân tích IP thất bại";
                try { msg = await res.text(); } catch (e) {}
                throw new Error(msg);
            }

            const data = await res.json();

            // render kết quả
            if (typeof window.renderAnalyzeResult === "function") {
                window.renderAnalyzeResult(data, resultDiv);
            }

            // reload full graph (đúng chuẩn customer-main.js)
            if (typeof window.fetchGraph === "function") {
                await window.fetchGraph(); // ⚠️ không truyền ip để tránh lọc sai data
            }

            // render lại IP-only
            renderIPOnly();

            // highlight IP
            if (typeof window.highlightNodeValues === "function") {
                window.highlightNodeValues([ip]);
            }

        } catch (e) {
            console.error(e);
            alert(e?.message || "Phân tích IP thất bại");
        } finally {
            analyzeBtn.disabled = false;
        }
    });
});