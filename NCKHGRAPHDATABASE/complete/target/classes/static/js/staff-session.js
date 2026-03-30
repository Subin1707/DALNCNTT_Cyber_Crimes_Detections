// staff-session.js – FIX FULL – Load session list + select session + reload graph
document.addEventListener("DOMContentLoaded", () => {
    const sessionSelect = document.getElementById("sessionSelect");
    const reloadBtn = document.getElementById("reloadGraphBtn");

    if (!sessionSelect) return;

    /* ================= UTIL ================= */
    function addOption(value, label) {
        const opt = document.createElement("option");
        opt.value = value;
        opt.textContent = label;
        sessionSelect.appendChild(opt);
    }

    function formatSessionLabel(s) {
        // hỗ trợ nhiều kiểu backend trả về
        const id = s.sessionId || s.id || s.value || "";
        const createdAt = s.createdAt || s.time || s.created || "";
        const keyword = s.keyword || s.query || s.input || "";

        // nếu backend trả về string đơn giản
        if (typeof s === "string") return s;

        if (keyword && createdAt) return `${id} | ${keyword} | ${createdAt}`;
        if (keyword) return `${id} | ${keyword}`;
        if (createdAt) return `${id} | ${createdAt}`;
        return `${id}`;
    }

    /* ================= LOAD SESSIONS ================= */
    async function loadSessions() {
        try {
            // thử nhiều endpoint staff sessions phổ biến
            const endpoints = [
                "/staff/sessions",
                "/staff/session-list",
                "/staff/session/list",
                "/staff/get-sessions"
            ];

            let data = null;

            for (const url of endpoints) {
                try {
                    const res = await fetch(url);
                    if (res.ok) {
                        data = await res.json();
                        break;
                    }
                } catch (e) {}
            }

            // nếu backend chưa có API sessions => không crash
            if (!data) {
                console.warn("Không tìm thấy API sessions (staff-session.js). Dropdown chỉ có ALL.");
                return;
            }

            // reset dropdown (giữ ALL)
            sessionSelect.innerHTML = "";
            addOption("", "Tất cả dữ liệu (ALL)");

            const sessions = Array.isArray(data) ? data : (data.sessions || []);

            if (!Array.isArray(sessions) || sessions.length === 0) {
                console.warn("Danh sách session rỗng");
                return;
            }

            // sort sessions by createdAt desc when present so default is latest
            const sorted = Array.isArray(sessions)
                ? [...sessions].sort((a, b) => {
                    const ta = (a && (a.createdAt || a.time || a.created)) ? new Date(a.createdAt || a.time || a.created).getTime() : 0;
                    const tb = (b && (b.createdAt || b.time || b.created)) ? new Date(b.createdAt || b.time || b.created).getTime() : 0;
                    return tb - ta;
                })
                : [];

            sorted.forEach((s) => {
                if (typeof s === "string") {
                    addOption(s, s);
                    return;
                }

                const id = s.sessionId || s.id || s.value;
                if (!id) return;

                addOption(id, formatSessionLabel(s));
            });

            // default to latest session if possible
            let defaultId = "";
            for (const s of sorted) {
                const id = typeof s === "string" ? s : (s.sessionId || s.id || s.value);
                if (id) { defaultId = id; break; }
            }
            if (defaultId) {
                sessionSelect.value = defaultId;
                // update label and trigger initial load
                updateSelectedLabel();
                setTimeout(() => reloadGraphBySession(), 50);
            }

        } catch (e) {
            console.error("Load sessions failed:", e);
        }
    }

    /* ================= RELOAD GRAPH BY SESSION ================= */
    async function reloadGraphBySession() {
        const sessionId = sessionSelect.value || "";

        if (typeof window.fetchGraph !== "function") {
            console.warn("window.fetchGraph chưa sẵn sàng");
            return;
        }

        try {
            await window.fetchGraph(sessionId);
        } catch (e) {
            console.error("Reload graph failed:", e);
            alert("Không thể tải graph theo session!");
        }
    }

    /* ================= EVENTS ================= */
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

    /* ================= INIT ================= */
    // đợi staff-main.js expose fetchGraph
    const wait = setInterval(async () => {
        if (typeof window.fetchGraph === "function") {
            clearInterval(wait);
            await loadSessions();
        }
    }, 100);
});