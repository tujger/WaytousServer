/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 1/19/17.
 */
function LogoutHolder(main) {
    this.category = DRAWER.SECTION_LAST;
    this.type = "logout";
    this.title = "Log out";
    this.menu = "Log out";
    this.icon = "exit_to_app";

    this.start = function() {
        main.drawer.headerTitle.innerHTML = "Waytous";
        main.drawer.headerSubtitle.innerHTML = "Admin";
        main.drawer.footer.lastChild.firstChild.replaceWith(u.create(HTML.DIV).place(HTML.SPAN, {className: "drawer-footer-link", innerHTML: "${APP_NAME} &copy;2017-18 Edeqa", onclick: function(e){
            main.arguments.utils.dialogAbout().open();
            e.stopPropagation();
        }}).place(HTML.SPAN, "\nBuild " + data.version));
    }

    this.resume = function() {
        u.clear(document.body);
        u.create(HTML.DIV, {
            className: "admin-splash-layout"
        }, document.body).place(HTML.IMG, {
            className: "admin-splash-logo",
            src: "/images/waytous-transparent-256.png"
        }).place(HTML.SPAN, {
            className: "admin-splash-title",
            innerHTML: "${APP_NAME} 1.${SERVER_BUILD}"
        }).place(HTML.SPAN, {
            className: "admin-splash-subtitle",
            innerHTML: "Admin"
        });

        setTimeout(function () {
            var xhr = new XMLHttpRequest();
            xhr.open("GET", "/admin", true);
            xhr.setRequestHeader("Authorization", "Digest logout");
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4) {

                    var url = new URL(window.location.href);
                    url = "https://" + url.hostname + (data.HTTPS_PORT === 443 ? "" : ":"+ data.HTTPS_PORT) + "/";
                    window.location = url
                }
            };
            xhr.send();
        }, 0);
    }
}
