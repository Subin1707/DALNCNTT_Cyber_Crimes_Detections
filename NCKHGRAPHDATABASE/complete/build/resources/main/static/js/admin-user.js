/**
 * admin-user.js – FIX FULL
 * - Load / Create / Delete / Search user
 * - An toàn backend + UI
 */

document.addEventListener("DOMContentLoaded", () => {
    const userTableBody   = document.getElementById("userTableBody");
    const userSearchInput = document.getElementById("userSearchInput");
    const reloadUsersBtn  = document.getElementById("reloadUsersBtn");

    const createUserBtn   = document.getElementById("createUserBtn");
    const createUsername  = document.getElementById("createUsername");
    const createEmail     = document.getElementById("createEmail");
    const createPassword  = document.getElementById("createPassword");
    const createRole      = document.getElementById("createRole");

    const userMessage     = document.getElementById("userMessage");

    let allUsers = [];
    let isLoading = false;

    /* =========================
     * UI Helpers
     * ========================= */

    function showMsg(text, type = "info") {
        if (!userMessage) return;

        const map = {
            info:    ["#e3f2fd", "#2196f3", "#1565c0"],
            success: ["#e8f5e9", "#4caf50", "#2e7d32"],
            warn:    ["#fff3e0", "#ff9800", "#e65100"],
            error:   ["#ffebee", "#f44336", "#c62828"]
        };

        const [bg, border, color] = map[type] || map.info;

        userMessage.style.display = "block";
        userMessage.style.background = bg;
        userMessage.style.border = `1px solid ${border}`;
        userMessage.style.color = color;
        userMessage.style.padding = "10px";
        userMessage.style.borderRadius = "8px";
        userMessage.style.margin = "10px 0";
        userMessage.innerHTML = text;
    }

    function clearMsg() {
        if (!userMessage) return;
        userMessage.style.display = "none";
        userMessage.innerHTML = "";
    }

    function escapeHtml(str) {
        if (str === null || str === undefined) return "";
        return String(str)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function normalizeRole(role) {
        if (!role) return "USER";
        const r = String(role).toUpperCase();
        if (r.includes("ADMIN")) return "ADMIN";
        return "USER";
    }

    function formatDate(dt) {
        if (!dt) return "-";
        try {
            return new Date(dt).toLocaleString();
        } catch {
            return String(dt);
        }
    }

    /* =========================
     * Render Table
     * ========================= */

    function renderUsers(users) {
        if (!userTableBody) return;

        if (!Array.isArray(users) || users.length === 0) {
            userTableBody.innerHTML = `
                <tr>
                    <td colspan="7" style="padding:14px;text-align:center;color:#777;">
                        Không có user nào
                    </td>
                </tr>
            `;
            return;
        }

        userTableBody.innerHTML = users.map((u, idx) => {
            const id        = u.id ?? u.userId ?? "";
            const username  = u.username ?? u.name ?? "";
            const email     = u.email ?? "";
            const role      = normalizeRole(u.role ?? u.roles);
            const enabled   = (u.enabled ?? u.status ?? true) !== false;
            const createdAt = u.createdAt ?? u.created ?? u.created_date ?? null;

            const statusBadge = enabled
                ? `<span style="padding:3px 8px;border-radius:999px;background:#e8f5e9;border:1px solid #4caf50;color:#2e7d32;font-size:12px;">ACTIVE</span>`
                : `<span style="padding:3px 8px;border-radius:999px;background:#ffebee;border:1px solid #f44336;color:#c62828;font-size:12px;">DISABLED</span>`;

            return `
                <tr>
                    <td>${idx + 1}</td>
                    <td>
                        <b>${escapeHtml(username)}</b>
                        <div style="font-size:11px;color:#777;">ID: ${escapeHtml(id)}</div>
                    </td>
                    <td>${escapeHtml(email)}</td>
                    <td>
                        <span style="padding:3px 8px;border-radius:999px;background:#f3e5f5;border:1px solid #9c27b0;color:#6a1b9a;font-size:12px;">
                            ${role}
                        </span>
                    </td>
                    <td>${statusBadge}</td>
                    <td style="font-size:12px;">${escapeHtml(formatDate(createdAt))}</td>
                    <td>
                        <button class="btnDeleteUser"
                            data-id="${escapeHtml(id)}"
                            data-username="${escapeHtml(username)}"
                            style="padding:6px 10px;border-radius:8px;border:1px solid #f44336;background:#ffebee;color:#c62828;cursor:pointer;">
                            Xoá
                        </button>
                    </td>
                </tr>
            `;
        }).join("");

        document.querySelectorAll(".btnDeleteUser").forEach(btn => {
            btn.addEventListener("click", async () => {
                const id = btn.dataset.id;
                const username = btn.dataset.username;
                if (!id) return;

                if (!confirm(`Bạn chắc chắn muốn xoá user "${username}" ?`)) return;
                await deleteUser(id);
            });
        });
    }

    /* =========================
     * API Calls
     * ========================= */

    async function fetchUsers() {
        if (isLoading) return;
        isLoading = true;
        clearMsg();
        reloadUsersBtn && (reloadUsersBtn.disabled = true);

        try {
            const res = await fetch("/admin/users");

            if (res.status === 403) {
                showMsg("⚠ Phiên admin đã hết hạn. Vui lòng đăng nhập lại!", "warn");
                return;
            }

            if (!res.ok) throw new Error("Không thể tải danh sách user");

            const data = await res.json();
            allUsers = Array.isArray(data) ? data : (data.users || []);
            renderUsers(allUsers);

        } catch (e) {
            console.error(e);
            showMsg("❌ Lỗi tải users: " + e.message, "error");
        } finally {
            reloadUsersBtn && (reloadUsersBtn.disabled = false);
            isLoading = false;
        }
    }

    async function createUser() {
        if (isLoading) return;

        const username = createUsername?.value?.trim();
        const email    = createEmail?.value?.trim();
        const password = createPassword?.value?.trim();
        const role     = normalizeRole(createRole?.value);

        if (!username || !email || !password) {
            showMsg("⚠ Nhập đầy đủ Username, Email, Password", "warn");
            return;
        }

        if (!email.includes("@")) {
            showMsg("⚠ Email không hợp lệ", "warn");
            return;
        }

        isLoading = true;
        createUserBtn && (createUserBtn.disabled = true);

        try {
            const res = await fetch("/admin/users", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, email, password, role })
            });

            const data = await res.json().catch(() => ({}));

            if (!res.ok || data.success === false) {
                throw new Error(data.message || "Tạo user thất bại");
            }

            showMsg("✅ Tạo user thành công", "success");

            createUsername.value = "";
            createEmail.value = "";
            createPassword.value = "";

            await fetchUsers();

        } catch (e) {
            console.error(e);
            showMsg("❌ " + e.message, "error");
        } finally {
            createUserBtn && (createUserBtn.disabled = false);
            isLoading = false;
        }
    }

    async function deleteUser(id) {
        if (isLoading) return;
        isLoading = true;
        clearMsg();

        try {
            const res = await fetch(`/admin/users/${encodeURIComponent(id)}`, {
                method: "DELETE"
            });

            const data = await res.json().catch(() => ({}));

            if (!res.ok || data.success === false) {
                throw new Error(data.message || "Xoá user thất bại");
            }

            showMsg("✅ Xoá user thành công", "success");
            await fetchUsers();

        } catch (e) {
            console.error(e);
            showMsg("❌ " + e.message, "error");
        } finally {
            isLoading = false;
        }
    }

    /* =========================
     * Search
     * ========================= */

    function applySearch(keyword) {
        const key = (keyword || "").toLowerCase().trim();
        if (!key) return renderUsers(allUsers);

        renderUsers(
            allUsers.filter(u =>
                String(u.username ?? "").toLowerCase().includes(key) ||
                String(u.email ?? "").toLowerCase().includes(key) ||
                String(u.role ?? "").toLowerCase().includes(key)
            )
        );
    }

    /* =========================
     * Events
     * ========================= */

    reloadUsersBtn?.addEventListener("click", fetchUsers);
    userSearchInput?.addEventListener("input", e => applySearch(e.target.value));
    createUserBtn?.addEventListener("click", createUser);

    fetchUsers();
});