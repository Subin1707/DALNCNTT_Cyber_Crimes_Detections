// customer-excel-upload.js – Upload Excel cho Customer
// - Upload file Excel
// - Hiển thị trạng thái + kết quả
// - Reload graph sau khi upload xong

document.addEventListener("DOMContentLoaded", () => {

    const uploadBtn = document.getElementById("uploadExcelBtn");
    const fileInput = document.getElementById("excelFile");
    const resultDiv = document.getElementById("uploadResult");

    if (!uploadBtn || !fileInput) return;

    function showMessage(msg, type = "info") {
        if (!resultDiv) return;

        const bg =
            type === "success" ? "#d1fae5" :
            type === "error" ? "#fee2e2" :
            type === "warning" ? "#fef3c7" :
            "#e5e7eb";

        const border =
            type === "success" ? "#10b981" :
            type === "error" ? "#ef4444" :
            type === "warning" ? "#f59e0b" :
            "#9ca3af";

        resultDiv.innerHTML = `
            <div style="
                padding:12px 14px;
                border-radius:10px;
                background:${bg};
                border:1px solid ${border};
                font-family:sans-serif;
                box-shadow:0 8px 24px rgba(0,0,0,.12);
                max-width:520px;
                word-wrap:break-word;
            ">
                ${msg}
            </div>
        `;
        resultDiv.style.display = "block";
    }

    async function reloadGraph() {
        if (typeof window.fetchGraph === "function") {
            await window.fetchGraph(); // reload full graph
        }
    }

    uploadBtn.addEventListener("click", async () => {

        const file = fileInput.files?.[0];
        if (!file) {
            alert("Vui lòng chọn file Excel (.xlsx / .xls)!");
            return;
        }

        const filename = file.name.toLowerCase();
        if (!filename.endsWith(".xlsx") && !filename.endsWith(".xls")) {
            alert("File không hợp lệ! Chỉ chấp nhận .xlsx hoặc .xls");
            return;
        }

        const formData = new FormData();
        // "file" phải trùng tên @RequestParam("file") bên backend
        formData.append("file", file);

        try {
            uploadBtn.disabled = true;
            showMessage("⏳ Đang upload file Excel...");

            const res = await fetch("/customer/upload-excel", {
                method: "POST",
                body: formData
            });

            if (res.status === 403) {
                alert("Phiên customer đã hết hạn!");
                return;
            }

            if (!res.ok) {
                const txt = await res.text();
                throw new Error(txt || "Upload thất bại!");
            }

            // Backend có thể trả JSON hoặc text
            let data;
            const contentType = res.headers.get("content-type") || "";
            if (contentType.includes("application/json")) {
                data = await res.json();
            } else {
                data = { message: await res.text() };
            }

            // Hiển thị kết quả upload
            const msg =
                data?.message ||
                `✅ Upload thành công: <b>${file.name}</b>`;

            showMessage(`✅ ${msg}`, "success");

            // reload graph sau khi upload
            await reloadGraph();

            // highlight nếu backend trả về giá trị nào đó
            if (Array.isArray(data?.highlightValues) && typeof window.highlightNodeValues === "function") {
                window.highlightNodeValues(data.highlightValues);
            }

            // clear file input (cho phép upload lại cùng 1 file)
            fileInput.value = "";

        } catch (err) {
            console.error(err);
            showMessage(`❌ ${err?.message || "Upload thất bại!"}`, "error");
        } finally {
            uploadBtn.disabled = false;
        }
    });

});