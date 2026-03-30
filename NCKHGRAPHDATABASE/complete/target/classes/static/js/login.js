document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");
    if (!form) return;

    form.addEventListener("submit", (e) => {
        const emailInput = form.querySelector('input[name="email"]');
        const passInput  = form.querySelector('input[name="password"]');

        const email = emailInput ? emailInput.value.trim() : "";
        const password = passInput ? passInput.value.trim() : "";

        /* ===== VALIDATE RỖNG ===== */
        if (!email || !password) {
            e.preventDefault();
            alert("Vui lòng nhập đầy đủ Email và Password");
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
        if (password.length < 4) {
            e.preventDefault();
            alert("Mật khẩu phải có ít nhất 4 ký tự!");
            passInput.focus();
            return;
        }

        // ✅ hợp lệ → cho submit form bình thường
        // backend AuthController sẽ xử lý
    });
});