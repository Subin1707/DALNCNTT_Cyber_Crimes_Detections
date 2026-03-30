/**
 * admin-excel-upload.js v5.0 – FULL FIX STABLE
 * - Đồng bộ DTO backend
 * - Fix null safety
 * - Fix highlight
 * - Fix session load
 */

document.addEventListener("DOMContentLoaded", () => {

    const uploadExcelBtn  = document.getElementById("uploadExcelBtn");
    const excelFileInput  = document.getElementById("excelFileInput");
    const excelResult     = document.getElementById("excelResult");
    const sessionSelect   = document.getElementById("sessionSelect");

    if (!uploadExcelBtn) return;

    uploadExcelBtn.addEventListener("click", handleExcelUpload);

    /* ===================== UTIL ===================== */

    function waitForFetchGraph() {
        return new Promise(resolve => {
            const timer = setInterval(() => {
                if (typeof window.fetchGraph === "function") {
                    clearInterval(timer);
                    resolve();
                }
            }, 50);
        });
    }

    function collectHighlightValues(results = []) {
        const set = new Set();

        results.forEach(r => {
            if (!r.success) return;
            if (r.email) set.add(String(r.email).toLowerCase());
            if (r.ip)    set.add(String(r.ip));
            if (r.url)   set.add(String(r.url).toLowerCase());
        });

        return [...set];
    }

    /* ===================== UPLOAD ===================== */

    async function handleExcelUpload() {
        const file = excelFileInput?.files?.[0];

        if (!file) return showError("Vui lòng chọn file Excel");
        if (!/\.(xlsx|xls)$/i.test(file.name))
            return showError("Chỉ hỗ trợ file .xlsx hoặc .xls");
        if (file.size > 5 * 1024 * 1024)
            return showError("File quá lớn (tối đa 5MB)");

        showLoading();

        try {
            const formData = new FormData();
            formData.append("file", file);

            const response = await fetch("/admin/upload-excel", {
                method: "POST",
                body: formData
            });

            let data;

            try {
                data = await response.json();
            } catch (e) {
                throw new Error("Server không trả JSON hợp lệ");
            }

            if (!response.ok || data.success !== true) {
                return showError(data.message || "Lỗi xử lý file");
            }

            if (!Array.isArray(data.results)) {
                return showError("Server trả về sai format (results null)");
            }

            /* ===== HIỂN THỊ BẢNG ===== */
            showSuccessResults(data);

            /* ===== LOAD GRAPH ===== */
            await waitForFetchGraph();

            const highlightValues = collectHighlightValues(data.results);

            let sessionId = data.sessionId || null;

            const sessions = await reloadSessions();

            if (!sessionId && sessions?.length) {
                const newest = sessions
                    .filter(s => s.createdAt)
                    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))[0];
                sessionId = newest?.id || null;
            }

            if (sessionSelect && sessionId) {
                sessionSelect.value = sessionId;
            }

            if (typeof window.fetchGraph === "function") {
                await window.fetchGraph(sessionId);
            }

            if (typeof window.highlightNodeValues === "function") {
                window.highlightNodeValues(highlightValues);
            }

        } catch (err) {
            console.error("Upload Excel error:", err);
            showError(err.message || "Lỗi kết nối server");
        }
    }

    /* ===================== SESSION ===================== */

    async function reloadSessions() {
        if (!sessionSelect) return [];

        try {
            const res = await fetch("/admin/sessions");
            const sessions = await res.json();

            sessionSelect.innerHTML =
                `<option value="">Tất cả dữ liệu (ALL)</option>`;

            if (Array.isArray(sessions)) {
                sessions.forEach(s => {
                    const opt = document.createElement("option");
                    opt.value = s.id;
                    opt.textContent =
                        `${s.fileName || "Excel"} | rows:${s.totalRows ?? "-"} | ${new Date(s.createdAt).toLocaleString()}`;
                    sessionSelect.appendChild(opt);
                });
            }

            return sessions;

        } catch (e) {
            console.error("Reload session error:", e);
            return [];
        }
    }

    /* ===================== UI ===================== */

    function showSuccessResults(data) {
        const {
            totalRecords,
            successCount,
            errorCount,
            results
        } = data;

        let html = `
            <div style="background:#e8f5e9;border:1px solid #4caf50;
                        border-radius:6px;padding:12px;margin-bottom:16px">
                <b>📊 Kết quả xử lý Excel</b><br>
                Tổng dòng: ${totalRecords ?? "-"} |
                ✓ Thành công: ${successCount ?? "-"} |
                ✗ Lỗi: ${errorCount ?? 0}
            </div>

            <table style="width:100%;border-collapse:collapse;font-size:12px">
                <thead>
                    <tr style="background:#f5f5f5">
                        <th>Dòng</th>
                        <th>Email</th>
                        <th>IP</th>
                        <th>URL</th>
                        <th>Risk</th>
                        <th>Verdict</th>
                        <th>Trạng thái</th>
                    </tr>
                </thead>
                <tbody>
        `;

        results.forEach(r => {

            const success = r.success === true;

            html += `
                <tr>
                    <td>${r.row ?? "-"}</td>
                    <td>${r.email ?? "-"}</td>
                    <td>${r.ip ?? "-"}</td>
                    <td>${r.url ?? "-"}</td>
                    <td>${success ? (r.riskLevel ?? "-") : "-"}</td>
                    <td>${success ? (r.verdict ?? "-") : "-"}</td>
                    <td style="background:${success ? "#c8e6c9" : "#ffcdd2"}">
                        ${success
                            ? "✓ Thành công"
                            : "✗ " + (r.error || "Lỗi")}
                    </td>
                </tr>
            `;
        });

        html += "</tbody></table>";

        excelResult.innerHTML = html;
        excelResult.style.display = "block";
    }

    function showError(message) {
        excelResult.innerHTML =
            `<div style="background:#ffebee;border:1px solid #f44336;
                          padding:12px;color:#c62828">
                ⚠ ${message}
             </div>`;
        excelResult.style.display = "block";
    }

    function showLoading() {
        excelResult.innerHTML =
            `<div style="background:#e3f2fd;border:1px solid #2196f3;
                          padding:12px;color:#1565c0">
                ⏳ Đang xử lý file Excel...
             </div>`;
        excelResult.style.display = "block";
    }

});