// sidebar-toggle.js
(function(){
    function applyState(collapsed){
        const sidebar = document.querySelector('.sidebar-left');
        const container = document.querySelector('.page-index .container');
        if(!sidebar) return;
        if(collapsed){
            sidebar.classList.add('collapsed');
            if(container) container.classList.add('sidebar-collapsed');
        } else {
            sidebar.classList.remove('collapsed');
            if(container) container.classList.remove('sidebar-collapsed');
        }
    }

    function toggle(){
        const current = !!(localStorage.getItem('sidebar-collapsed') === '1');
        const next = !current;
        localStorage.setItem('sidebar-collapsed', next ? '1' : '0');
        applyState(next);
    }

    document.addEventListener('DOMContentLoaded', ()=>{
        const sidebar = document.querySelector('.sidebar-left');
        if(sidebar){
            const btn = document.createElement('button');
            btn.id = 'btnToggleNav';
            btn.className = 'btn btn-small sidebar-toggle-btn';
            btn.type = 'button';
            btn.title = 'Thu/Ẩn thanh điều hướng';
            btn.textContent = '☰';
            const title = sidebar.querySelector('.sidebar-title');
            if(title){
                const topbar = document.createElement('div');
                topbar.className = 'sidebar-topbar';
                sidebar.insertBefore(topbar, title);
                topbar.appendChild(title);
                topbar.appendChild(btn);
            } else {
                sidebar.insertBefore(btn, sidebar.firstChild);
            }
            btn.addEventListener('click', toggle);
        }

        const collapsed = localStorage.getItem('sidebar-collapsed') === '1';
        applyState(collapsed);
    });
})();
