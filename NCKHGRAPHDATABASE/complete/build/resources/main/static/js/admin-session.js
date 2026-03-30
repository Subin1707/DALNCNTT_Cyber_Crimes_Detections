/**
 * admin-session.js – FIX FULL
 * - Load session list an toàn
 * - Select session → reload graph
 * - Không crash nếu backend trả format lạ
 */

document.addEventListener("DOMContentLoaded", () => {
    const sessionSelect = document.getElementById("sessionSelect");
    const reloadBtn = document.getElementById("reloadGraphBtn");

    if (!sessionSelect) {
        console.warn("[admin-session] sessionSelect not found");
        return;
    }

    /* ===============================
     * Helpers
     * =============================== */

    function addOption(value, label) {
        const opt = document.createElement("option");
        opt.value = value;
        opt.textContent = label;
        sessionSelect.appendChild(opt);
    }

    function formatSessionLabel(s) {
        if (typeof s === "string") return s;

        const id =
            s.sessionId ||
            s.id ||
            s.value ||
            "";

        const fileName = s.fileName || s.filename || "";
        const totalRows = s.totalRows ?? s.rows ?? "";
        const createdAt = s.createdAt || s.created || s.time || "";

        let parts = [id];

        if (fileName) parts.push(fileName);
        if (totalRows !== "") parts.push(`rows=${totalRows}`);
        if (createdAt) parts.push(createdAt);

        return parts.join(" | ");
    }

    /* ===============================
     * Load sessions from backend
     * =============================== */

    async function loadSessions() {
        const endpoints = [
            "/admin/sessions",
            "/admin/session-list",
            "/admin/session/list",
            "/admin/get-sessions"
        ];

        let sessions = null;

        for (const url of endpoints) {
            try {
                const res = await fetch(url);
                if (!res.ok) continue;

                const data = await res.json();
                sessions = Array.isArray(data)
                    ? data
                    : (data.sessions || data.data || null);

                if (Array.isArray(sessions)) {
                    console.log("[admin-session] Loaded sessions from", url);
                    break;
                }
            } catch (e) {
                // thử endpoint tiếp theo
            }
        }

        // reset dropdown
        sessionSelect.innerHTML = "";
        addOption("", "Tất cả dữ liệu (ALL)");

        if (!Array.isArray(sessions) || sessions.length === 0) {
            console.warn("[admin-session] Session list empty or not found");
            return;
        }

        // sort sessions by createdAt (desc) when possible so first entry is latest
        const sorted = Array.isArray(sessions)
            ? [...sessions].sort((a, b) => {
                const ta = (a && (a.createdAt || a.created || a.time)) ? new Date(a.createdAt || a.created || a.time).getTime() : 0;
                const tb = (b && (b.createdAt || b.created || b.time)) ? new Date(b.createdAt || b.created || b.time).getTime() : 0;
                return tb - ta;
            })
            : [];

        sorted.forEach(s => {
            if (typeof s === "string") {
                addOption(s, s);
                return;
            }

            const id = s.sessionId || s.id || s.value;
            if (!id) return;

            addOption(id, formatSessionLabel(s));
        });

        // Default: select latest session (first in sorted) if available
        let defaultId = "";
        for (const s of sorted) {
            const id = typeof s === "string" ? s : (s.sessionId || s.id || s.value);
            if (id) { defaultId = id; break; }
        }

        if (defaultId) {
            sessionSelect.value = defaultId;
            updateSelectedLabel();
            // trigger initial load
            setTimeout(() => reloadGraphBySession(), 50);
        }
    }

    /* ===============================
     * Reload graph by session
     * =============================== */

    async function reloadGraphBySession() {
        const sessionId = sessionSelect.value || "";

        if (typeof window.fetchGraph !== "function") {
            console.warn("[admin-session] window.fetchGraph not ready");
            return;
        }

        try {
            console.log("[admin-session] Reload graph, session =", sessionId || "ALL");
            await window.fetchGraph(sessionId);
        } catch (e) {
            console.error("❌ Reload graph failed:", e);
            alert("Không thể tải graph theo session!");
        }
    }

    /* ===============================
     * Events
     * =============================== */

    reloadBtn?.addEventListener("click", reloadGraphBySession);

    sessionSelect.addEventListener("change", () => {
        updateSelectedLabel();
        reloadGraphBySession();
    });

    function updateSelectedLabel() {
        const labelEl = document.getElementById("selectedSessionLabel");
        if (!labelEl) return;
        const idx = sessionSelect.selectedIndex;
        if (idx < 0) {
            labelEl.textContent = "Tất cả dữ liệu (ALL)";
            return;
        }
        const opt = sessionSelect.options[idx];
        labelEl.textContent = opt ? opt.textContent : "Tất cả dữ liệu (ALL)";
    }

    /* ===============================
     * Init – chờ fetchGraph sẵn sàng
     * =============================== */

    const waitFetchGraph = setInterval(async () => {
        if (typeof window.fetchGraph === "function") {
            clearInterval(waitFetchGraph);
            await loadSessions();
        }
    }, 100);
});