document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("registerForm");
    if (!form) return;

    form.addEventListener("submit", (e) => {
        const emailInput = form.querySelector('input[name="email"]');
        const passInput  = form.querySelector('input[name="password"]');

        const email = emailInput ? emailInput.value.trim() : "";
        const password = passInput ? passInput.value.trim() : "";

        /* ===== CHECK RỖNG ===== */
        if (!email || !password) {
            e.preventDefault();
            alert("Vui lòng nhập email và mật khẩu");
            (!email ? emailInput : passInput)?.focus();
            return;
        }

        /* ===== CHECK EMAIL ===== */
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            e.preventDefault();
            alert("Email không đúng định dạng!");
            emailInput.focus();
            return;
        }

        /* ===== CHECK PASSWORD ===== */
        if (password.length < 4) {
            e.preventDefault();
            alert("Mật khẩu phải có ít nhất 4 ký tự!");
            passInput.focus();
            return;
        }

        // ✅ hợp lệ → submit form
        // backend UserService.registerCustomer sẽ xử lý trùng email
    });
});