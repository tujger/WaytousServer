/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 1/20/17.
 */

function Main() {
//    var firebaseVersion = "4.6.2";
    var drawer;
    var layout;
    var actionbar;

    var holders = {};
    var holderFiles = [
//        "https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-app.js",
//        "https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-auth.js",
//        "https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-database.js",
        "/js/helpers/Utils.js",
        "/js/helpers/Constants",
        "Home",
        "Create",
        "User",
        "Group",
        "Groups",
        "Account",
        "Accounts",
        "Chat",
        "Logs",
        "Statistics"
//        "Settings",
//        "Help",
    ];

    function start() {
        var a = document.createElement("script");
        a.setAttribute("src","/js/Edequate.js");
        a.setAttribute("onload","preloaded()");
        document.head.appendChild(a);
    }


    preloaded = function() {
        var self = this;
        if(window.location.pathname === "/admin" || window.location.pathname === "/admin/") {
            window.location.href = "/admin/home";
            return;
        }

        window.u = new Edequate({exportConstants:true, origin:"waytous"});

        DRAWER = {
            SECTION_PRIMARY: 0,
            SECTION_COMMUNICATION: 2,
            SECTION_SHARE: 3,
            SECTION_NAVIGATION: 5,
            SECTION_VIEWS: 6,
            SECTION_MAP: 7,
            SECTION_MISCELLANEOUS: 8,
            SECTION_LAST: 9
        };

        document.title = u.create(HTML.DIV, "${APP_NAME} - Admin").innerHTML;
        u.loading("Loading resources...");

        document.head
            .place(HTML.META, {name:"viewport", content:"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"})
            .place(HTML.LINK, {rel:HTML.STYLESHEET, href:"/css/edequate.css"})
            .place(HTML.LINK, {rel:HTML.STYLESHEET, href:"/css/admin.css"})
            .place(HTML.LINK, {rel:HTML.STYLESHEET, href:"https://fonts.googleapis.com/icon?family=Material+Icons"})
            .place(HTML.LINK, {rel:"apple-touch-icon", href:"/icons/apple-touch-icon.png"})
            .place(HTML.LINK, {rel:"apple-touch-icon", sizes:"60x60", href:"/icons/apple-touch-icon-60x60.png"})
            .place(HTML.LINK, {rel:"apple-touch-icon", sizes:"76x76", href:"/icons/apple-touch-icon-76x76.png"})
            .place(HTML.LINK, {rel:"apple-touch-icon", sizes:"120x120", href:"/icons/apple-touch-icon-120x120.png"})
            .place(HTML.LINK, {rel:"apple-touch-icon", sizes:"152x152", href:"/icons/apple-touch-icon-152x152.png"})
            .place(HTML.LINK, {rel:"apple-touch-icon", sizes:"180x180", href:"/icons/apple-touch-icon.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"192x192", href:"/icons/android-chrome-192x192.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"32x32", href:"/icons/favicon-32x32.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"16x16", href:"/icons/favicon-16x16.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"194x194", href:"/icons/favicon-194x194.png"})
            .place(HTML.LINK, {rel:"mask-icon", href:"/icons/safari-pinned-tab.svg", color:"#00aaaa"})
            .place(HTML.LINK, {rel:"icon",type:"image/x-icon", href:"/icons/favicon.ico"})
            .place(HTML.LINK, {rel:"shortcut icon", href:"/icons/favicon.ico"})
            .place(HTML.LINK, {rel:"apple-touch-startup-image", href: "/icons/apple-touch-icon.png"})
            .place(HTML.META, {name:"msapplication-TileColor", content:"#00aaaa"})
            .place(HTML.META, {name:"msapplication-TileImage", content:"/icons/mstile-144x144.png"})
            .place(HTML.META, {name:"msapplication-config", content:"/icons/browserconfig.xml"})
            .place(HTML.META, {name:"theme-color", content:"#aaeeee"});


        u.require("/js/helpers/Constants").then(function(){
            loadResources("admin.json", function(){
                var loaded = 0;
                for(var i in holderFiles) {
                    var file = holderFiles[i];
                    if(!file.match(/^(https?:)|\//i)) file = "/js/admin/"+file;
                    u.require(file).then(function(e) {
                        loaded++;
                        if(e && e.moduleName) {
                            holders[e.moduleName.toLowerCase()] = e;
                        }
                        if(loaded === u.keys(holderFiles).length) {
                            console.log("Preload finished: "+loaded+" files done.");
                            window.utils = new Utils(self);

                            initialize();

                        }
                    }).catch(function(){
                        u.dialog({
                            title: u.lang.alert,
                            items: [
                                { type:HTML.DIV, innerHTML: "Error loading service."}
                            ],
                            positive: {
                                label: u.lang.reload,
                                onclick: function(){
                                    window.location = window.location.href;
                                }
                            }
                        }).open();
                    });
                }
            });
        });
    };

    function initialize() {

//        if(!firebase || !firebase.database || !firebase.auth) {
//            console.error("Failed firebase loading, trying again...");
//
//            var files = [];
//            if(!firebase) files.push("https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-app.js");
//            if(!firebase.database) files.push("https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-database.js");
//            if(!firebase.auth) files.push("https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-auth.js");
//
//            var loaded = 0;
//            var failed = false;
//            for(var i in files) {
//                var file = files[i];
//                u.require(file).then(function() {
//                    if(failed) return;
//                    loaded++;
//                    //progress.innerHTML = Math.ceil(loaded / files.length * 100) + "%";
//                    if(loaded === u.keys(files).length) {
//                        initialize();
//                    }
//                }).catch(function(){
//                    u.dialog({
//                        title: "Alert",
//                        items: [
//                            { type:HTML.DIV, innerHTML: "Error loading service."}
//                        ],
//                        positive: {
//                            label: "Reload",
//                            onclick: function(){
//                                window.location = window.location.href;
//                            }
//                        }
//                    }).open();
//                });
//            }
//
//            return;
//        }

        try {
//            firebase.initializeApp(data.firebase_config);
            database = firebase.database();
        } catch(e) {
            console.error(e);
        }

        resign(resume);

    }

    function resign(callback){

        u.loading(u.lang.signing_in);
        firebase.auth().signInWithCustomToken(data.sign).then(function(){
            callback();
        }).catch(function() {
            window.location = window.location.href;
        });
    }

    function resume() {
        try {
            window.addEventListener("load",function() { setTimeout(function(){ // This hides the address bar:
                window.scrollTo(0, 1); }, 0);
            });
            var page = window.location.pathname.split("/")[2];

            layout = u.create("div", {className:"layout"}, document.body);
//            content = u.create("div", {className:"content"}, layout);

            drawer = new u.drawer({
                title: "${APP_NAME}",
                subtitle: u.lang.admin,
                collapsed: "admin:drawer:collapsed",
                logo: {
                    src:"/images/logo.svg",
                    onclick: function() {
                        dialogAbout.open();
                    }
                },
                onprimaryclick: function(){

                },
                footer: {
                    className: "drawer-footer-label",
                    content: u.create(HTML.DIV).place(HTML.SPAN, {className: "drawer-footer-link", innerHTML: "${APP_NAME} &copy;2017-18 Edeqa", onclick: function(e){
                        dialogAbout.open();
                        e.preventDefault();
                        e.stopPropagation();
                        return false;
                    }}).place(HTML.SPAN, "\nBuild " + data.version)
                }
            }, document.body);

            var dialogAbout = utils.dialogAbout();

            actionbar = u.actionBar({
                title: holders[page].title,
                onbuttonclick: function(){
                    try {
                        drawer.open();
                    } catch(e) {
                        console.error(e);
                    }
                }
            }, document.body);
            u.create({className:"alert"}, document.body);

            for(var i in holderFiles) {
                var x = holderFiles[i].toLowerCase();
                if(holders[x] && holders[x].menu) {
                    var item = drawer.add(holders[x].move ? DRAWER.SECTION_PRIMARY : DRAWER.SECTION_VIEWS, x, holders[x].menu, holders[x].icon, function(){
                        switchTo("/admin/" + holders[this.instance].page);
                        return false;
                    });
                    item.instance = x;
                }
            }

            drawer.add(DRAWER.SECTION_LAST, "exit", "Log out", "exit_to_app", logout);

            actionbar.titleNode.innerHTML = holders[page].title;
            drawer.headerPrimary.innerHTML = holders[page].title;
            holders[page].start();
            u.loading.hide();
        } catch(e) {
            console.error(e);
            window.location = "/admin/home";
        }
    }

    var logout = function() {
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
    };

    var switchTo = function(to) {
        var parts = to.split("/");
        if(parts[1] === "admin") {
            if(holders[parts[2]].move) {
                u.clear(layout);
                actionbar.titleNode.innerHTML = holders[parts[2]].title;
                drawer.headerPrimary.innerHTML = holders[parts[2]].title;
            }
            holders[parts[2]].start(parts);
            if(holders[parts[2]].move) {
                window.history.pushState({}, null, "/admin/" + holders[parts[2]].page);
            }
        }
    };

    function loadResources(resource, callback) {
        var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
        u.lang.overrideResources({
            "default": "/resources/en/" + resource,
            resources: "/rest/resources",
            resource: resource,
            locale: lang,
            callback: callback
        });
    }


    return {
        start: start,
        switchTo: switchTo,
        resign: resign
    }
}
document.addEventListener("readystatechange", function(){if(document.readyState === "complete"){(window.WTU = new Main()).start()}});
