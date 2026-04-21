document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");
    if (!form) return;

    const toggle = form.querySelector("[data-toggle-password]");
    const passwordInput = form.querySelector('input[name="password"]');

    if (toggle && passwordInput) {
        toggle.addEventListener("click", () => {
            const isHidden = passwordInput.type === "password";
            passwordInput.type = isHidden ? "text" : "password";
            toggle.setAttribute("aria-label", isHidden ? "Ẩn mật khẩu" : "Hiện mật khẩu");
            const icon = toggle.querySelector("i");
            if (icon) {
                icon.classList.toggle("fa-eye", !isHidden);
                icon.classList.toggle("fa-eye-slash", isHidden);
            }
        });
    }

    form.addEventListener("submit", (e) => {
        const emailInput = form.querySelector('input[name="email"]');
        const passInput = form.querySelector('input[name="password"]');

        const email = emailInput ? emailInput.value.trim() : "";
        const password = passInput ? passInput.value.trim() : "";

        /* ===== VALIDATE RỖNG ===== */
        if (!email || !password) {
            e.preventDefault();
            alert("Vui lòng nhập đầy đủ email và mật khẩu");
            (!email ? emailInput : passInput)?.focus();
            return;
        }

        /* ===== VALIDATE EMAIL ===== */
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            e.preventDefault();
            alert("Email không đúng định dạng!");
            emailInput.focus();
            return;
        }

        /* ===== VALIDATE PASSWORD ===== */
        if (password.length < 6) {
            e.preventDefault();
            alert("Mật khẩu phải có ít nhất 6 ký tự!");
            passInput.focus();
            return;
        }

        // ✅ hợp lệ → cho submit form bình thường
        // backend AuthController sẽ xử lý
    });
});