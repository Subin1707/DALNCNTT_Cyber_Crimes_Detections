// staff-session.js - Load session list, select a session, and reload graph.
document.addEventListener("DOMContentLoaded", () => {
    const sessionSelect = document.getElementById("sessionSelect");
    const reloadBtn = document.getElementById("reloadGraphBtn");

    if (!sessionSelect) return;

    function addOption(value, label) {
        const opt = document.createElement("option");
        opt.value = value;
        opt.textContent = label;
        sessionSelect.appendChild(opt);
    }

    function formatSessionLabel(s) {
        const id = s.sessionId || s.id || s.value || "";
        const createdAt = s.createdAt || s.time || s.created || "";
        const keyword = s.keyword || s.query || s.input || "";

        if (typeof s === "string") return s;

        if (keyword && createdAt) return `${id} | ${keyword} | ${createdAt}`;
        if (keyword) return `${id} | ${keyword}`;
        if (createdAt) return `${id} | ${createdAt}`;
        return `${id}`;
    }

    async function loadSessions() {
        try {
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

            sessionSelect.innerHTML = "";
            addOption("", "Tất cả dữ liệu (ALL)");

            if (!data) {
                console.warn("Không tìm thấy API sessions (staff-session.js). Dropdown chỉ có ALL.");
                updateSelectedLabel();
                return;
            }

            const sessions = Array.isArray(data) ? data : (data.sessions || []);

            if (!Array.isArray(sessions) || sessions.length === 0) {
                console.warn("Danh sách session rỗng");
                updateSelectedLabel();
                return;
            }

            const sorted = [...sessions].sort((a, b) => {
                const ta = (a && (a.createdAt || a.time || a.created)) ? new Date(a.createdAt || a.time || a.created).getTime() : 0;
                const tb = (b && (b.createdAt || b.time || b.created)) ? new Date(b.createdAt || b.time || b.created).getTime() : 0;
                return tb - ta;
            });

            sorted.forEach((s) => {
                if (typeof s === "string") {
                    addOption(s, s);
                    return;
                }

                const id = s.sessionId || s.id || s.value;
                if (!id) return;

                addOption(id, formatSessionLabel(s));
            });

            let defaultId = "";
            for (const s of sorted) {
                const id = typeof s === "string" ? s : (s.sessionId || s.id || s.value);
                if (id) {
                    defaultId = id;
                    break;
                }
            }

            if (defaultId) {
                sessionSelect.value = defaultId;
                updateSelectedLabel();
                setTimeout(() => reloadGraphBySession(), 50);
            } else {
                updateSelectedLabel();
            }
        } catch (e) {
            console.error("Load sessions failed:", e);
        }
    }

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

    reloadBtn?.addEventListener("click", reloadGraphBySession);

    sessionSelect.addEventListener("change", () => {
        updateSelectedLabel();
        reloadGraphBySession();
    });

    const wait = setInterval(async () => {
        if (typeof window.fetchGraph === "function") {
            clearInterval(wait);
            await loadSessions();
        }
    }, 100);
});
