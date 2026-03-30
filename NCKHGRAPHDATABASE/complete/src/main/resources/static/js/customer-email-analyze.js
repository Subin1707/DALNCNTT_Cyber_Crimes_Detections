// customer-email-analyze.js – v1.1 – FIX FULL (Email Only + Analyze + Refresh + Highlight)
document.addEventListener("DOMContentLoaded", () => {

    /* ================= EMAIL ONLY GRAPH ================= */
    function renderEmailOnly() {
        if (!window.allNodes || !window.allLinks || !window.render) return;

        const emailNodes = window.allNodes.filter(n => n.type === "Email");
        const emailIds = new Set(emailNodes.map(n => n.id));

        // chỉ lấy link giữa Email ↔ Email
        const emailLinks = window.allLinks.filter(l =>
            emailIds.has(l.source?.id || l.source) &&
            emailIds.has(l.target?.id || l.target)
        );

        window.render(emailNodes, emailLinks);
    }

    // đợi graph load từ customer-main.js
    const waitGraph = setInterval(() => {
        if (window.allNodes && window.allLinks && window.render) {
            clearInterval(waitGraph);
            renderEmailOnly();
        }
    }, 100);

    /* ================= ANALYZE EMAIL ================= */
    const analyzeBtn  = document.getElementById("analyzeBtn");
    const resultDiv   = document.getElementById("analyzeResult");
    const emailInput  = document.getElementById("analyzeEmail");

    if (!analyzeBtn || !emailInput) return;

    analyzeBtn.addEventListener("click", async () => {
        const email = (emailInput.value || "").trim().toLowerCase();

        if (!email) {
            alert("Vui lòng nhập Email");
            return;
        }

        try {
            analyzeBtn.disabled = true;
            if (resultDiv) resultDiv.innerHTML = "";

            const res = await fetch("/customer/analyze", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email })
            });

            if (res.status === 403) {
                alert("Phiên customer đã hết hạn!");
                return;
            }

            if (!res.ok) {
                let msg = "Analyze failed";
                try { msg = await res.text(); } catch (e) {}
                throw new Error(msg);
            }

            const data = await res.json();

            // ✅ render kết quả
            if (typeof window.renderAnalyzeResult === "function") {
                window.renderAnalyzeResult(data, resultDiv);
            }

            // ✅ reload graph + render lại email-only
            if (typeof window.fetchGraph === "function") {
                await window.fetchGraph(); // reload full graph
            }

            renderEmailOnly();

            // ✅ highlight email
            if (typeof window.highlightNodeValues === "function") {
                window.highlightNodeValues([email]);
            }

        } catch (e) {
            console.error(e);
            alert(e?.message || "Không thể phân tích Email");
        } finally {
            analyzeBtn.disabled = false;
        }
    });
});