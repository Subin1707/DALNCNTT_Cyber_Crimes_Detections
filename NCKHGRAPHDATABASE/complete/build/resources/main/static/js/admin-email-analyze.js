// admin-email-analyze.js – FIX FULL (Email page) | refresh đúng session + highlight chuẩn

document.addEventListener("DOMContentLoaded", () => {
    const resultDiv = document.getElementById("analyzeResult");

    function normalizeEmail(raw) {
        if (raw == null) return null;
        const s = String(raw).trim().toLowerCase();
        return s ? s : null;
    }

    const handleAnalyze = async (emailRaw) => {
        const email = normalizeEmail(emailRaw);
        if (!email) return alert("Vui lòng nhập Email");

        try {
            if (resultDiv) resultDiv.innerHTML = "";

            const btn = document.getElementById("analyzeEmailBtn");
            if (btn) btn.disabled = true;

            // ✅ Gọi đúng endpoint backend
            const res = await fetch("/admin/analyze", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email })
            });

            if (res.status === 403) {
                alert("Phiên admin đã hết hạn!");
                return;
            }
            if (!res.ok) throw new Error("Phân tích thất bại!");

            const data = await res.json();

            // ✅ Hiển thị kết quả
            if (resultDiv) {
                resultDiv.innerHTML = `
                    <div style="
                        padding:14px;
                        border-radius:10px;
                        background:#fff;
                        box-shadow:0 8px 24px rgba(0,0,0,.25);
                        max-width:400px;
                        word-wrap:break-word;
                        font-family:sans-serif;
                    ">
                        <div style="display:flex;justify-content:space-between;align-items:center">
                            <b>Kết quả phân tích Email</b>
                            <button id="closeAnalyzeResult"
                                style="background:none;border:none;font-size:16px;cursor:pointer">✖</button>
                        </div>
                        <hr>
                        <b>Verdict:</b> ${data.verdict || "—"}<br>
                        <b>Scam type:</b> ${data.scamType || "—"}<br>
                        <b>Risk score:</b> ${data.riskScore ?? 0}<br>
                        <b>Indicators:</b>
                        ${Array.isArray(data.indicators) && data.indicators.length
                            ? data.indicators.join(", ")
                            : "Không có"}
                    </div>
                `;
                resultDiv.style.display = "block";
                document.getElementById("closeAnalyzeResult").onclick =
                    () => resultDiv.style.display = "none";
            }

            // ✅ Refresh graph đúng session đang chọn (KHÔNG truyền email vào fetchGraph)
            const sessionId = document.getElementById("sessionSelect")?.value || null;

            if (typeof window.fetchGraph === "function") {
                await window.fetchGraph(sessionId);
            }

            // ✅ Highlight node email vừa phân tích
            if (typeof window.highlightNodeValues === "function") {
                window.highlightNodeValues([email]);
            }

        } catch (err) {
            console.error(err);
            alert(err?.message || "Không thể phân tích Email");
        } finally {
            const btn = document.getElementById("analyzeEmailBtn");
            if (btn) btn.disabled = false;
        }
    };

    document.getElementById("analyzeEmailBtn")?.addEventListener("click", () => {
        const val = document.getElementById("analyzeEmail")?.value || "";
        handleAnalyze(val);
    });
});