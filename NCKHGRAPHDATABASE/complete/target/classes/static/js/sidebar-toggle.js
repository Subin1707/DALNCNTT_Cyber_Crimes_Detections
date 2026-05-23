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
        // inject toggle button into header (dashboard-header or .header)
        const header = document.querySelector('.dashboard-header') || document.querySelector('.header');
        if(header){
            const btn = document.createElement('button');
            btn.id = 'btnToggleNav';
            btn.className = 'btn btn-small';
            btn.type = 'button';
            btn.title = 'Thu/Ẩn thanh điều hướng';
            btn.style.marginRight = '8px';
            btn.textContent = '☰';
            // Insert at start of header actions if present
            const headerActions = header.querySelector('.header-actions');
            if(headerActions){ headerActions.insertBefore(btn, headerActions.firstChild); }
            else { header.appendChild(btn); }
            btn.addEventListener('click', toggle);
        }

        const collapsed = localStorage.getItem('sidebar-collapsed') === '1';
        applyState(collapsed);
    });
})();
