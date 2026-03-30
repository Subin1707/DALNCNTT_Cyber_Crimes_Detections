// customer-session.js – single, robust session loader + reload for customer dashboard
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
        if (typeof s === "string") return s;
        const id = s.sessionId || s.id || s.value || "";
        const raw = s.createdAt || s.time || s.created || "";
        let createdAt = "";
        if (raw !== null && raw !== undefined && raw !== "") {
            if (typeof raw === 'number') createdAt = String(raw);
            else {
                const t = Date.parse(String(raw));
                createdAt = isNaN(t) ? String(raw) : String(t);
            }
        }
        const keyword = s.keyword || s.query || s.input || s.fileName || "";
        const parts = [id];
        if (keyword) parts.push(keyword);
        if (createdAt) parts.push(createdAt);
        return parts.filter(Boolean).join(" | ");
    }

    async function loadSessions() {
        try {
            const endpoints = [
                "/customer/sessions",
                "/customer/session-list",
                "/customer/session/list",
                "/customer/get-sessions"
            ];

            let data = null;
            for (const url of endpoints) {
                try {
                    const res = await fetch(url);
                    if (res.ok) { data = await res.json(); break; }
                } catch (e) { /* ignore */ }
            }

            // keep ALL option even if no endpoint
            sessionSelect.innerHTML = "";
            addOption("", "Tất cả dữ liệu (ALL)");

            if (!data) {
                console.warn("Không tìm thấy API sessions (customer-session.js). Dropdown chỉ có ALL.");
                return;
            }

            const sessions = Array.isArray(data) ? data : (data.sessions || []);
            if (!Array.isArray(sessions) || sessions.length === 0) return;

            const sorted = [...sessions].sort((a, b) => {
                const ta = (a && (a.createdAt || a.time || a.created)) ? new Date(a.createdAt || a.time || a.created).getTime() : 0;
                const tb = (b && (b.createdAt || b.time || b.created)) ? new Date(b.createdAt || b.time || b.created).getTime() : 0;
                return tb - ta;
            });

            sorted.forEach(s => {
                if (typeof s === "string") { addOption(s, s); return; }
                const id = s.sessionId || s.id || s.value;
                if (!id) return;
                addOption(id, formatSessionLabel(s));
            });

            // default to latest session
            let defaultId = "";
            for (const s of sorted) {
                const id = typeof s === "string" ? s : (s.sessionId || s.id || s.value);
                if (id) { defaultId = id; break; }
            }
            if (defaultId) {
                sessionSelect.value = defaultId;
                updateSelectedLabel();
                setTimeout(() => reloadGraphBySession(), 60);
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
        if (idx < 0) { labelEl.textContent = "Tất cả dữ liệu (ALL)"; return; }
        const opt = sessionSelect.options[idx];
        labelEl.textContent = opt ? opt.textContent : "Tất cả dữ liệu (ALL)";
    }

    reloadBtn?.addEventListener("click", reloadGraphBySession);
    sessionSelect.addEventListener("change", () => { updateSelectedLabel(); reloadGraphBySession(); });

    // wait for customer-main.js to expose fetchGraph
    const wait = setInterval(() => {
        if (typeof window.fetchGraph === "function") {
            clearInterval(wait);
            loadSessions();
        }
    }, 100);
});