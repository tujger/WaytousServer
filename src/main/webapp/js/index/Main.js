/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 4/24/17.
 */

function Main() {

    var self = this;
    var holders = {};
    var files = [
        "/js/helpers/Utils.js",
//        "/js/helpers/Constants",
        "/js/index/HomeHolder",
        "/js/index/StartHolder",

        "/js/index/HelpHolder",
        "/js/index/PrivacyPolicyHolder",
//        "/js/index/ApiHolder",

        "/js/index/SupportHolder",
        "/js/index/FeedbackHolder",
        "/js/index/ContactHolder",
//        "/js/index/BlablaHolder",
        "/js/index/AboutHolder"
    ];
    var type = "home";

    self.start = function() {
        var a = document.createElement("script");
        a.setAttribute("src","/js/helpers/Edequate.js");
        a.setAttribute("onload","preloaded()");
        document.head.appendChild(a);
    };

    preloaded = function() {

        window.u = new Edequate({exportConstants:true, origin:"waytous"});

        u.loading("Loading resources...");

        document.head
            .place(HTML.META, {name:"viewport", content:"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"})
            .place(HTML.LINK, {rel:"stylesheet", href:"https://fonts.googleapis.com/icon?family=Material+Icons", async:"", defer:""})
            .place(HTML.LINK, {rel:"stylesheet", href:"/css/edequate.css", async:"", defer:""})
            .place(HTML.LINK, {rel:"stylesheet", href:"/css/index.css", async:"", defer:""})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"192x192", href:"/icons/android-chrome-192x192.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"32x32", href:"/icons/favicon-32x32.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"16x16", href:"/icons/favicon-16x16.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"194x194", href:"/icons/favicon-194x194.png"});

        if(data.google_analytics_tracking_id) {
            document.head.place(HTML.SCRIPT, {src:"https://www.googletagmanager.com/gtag/js?id=" + data.google_analytics_tracking_id, async:true})
                .place(HTML.SCRIPT, {innerHTML: "window.dataLayer = window.dataLayer || [];\n" +
                "        function gtag(){dataLayer.push(arguments)};\n" +
                "        gtag('js', new Date());\n" +
                "        gtag('config', '" + data.google_analytics_tracking_id + "');"});
        }

        u.require("/js/helpers/Constants").then(function(e){

            EVENTS.RELOAD = "reload";

            loadResources("index.json", function() {
                u.eventBus.register(files, {
                    context: self,
                    onprogress: function (loaded) {
                        u.loading(Math.ceil(loaded / files.length * 100) + "%");
                    },
                    onstart: function () {
                        window.utils = new Utils(self);
                    },
                    onsuccess: function () {
                        holders = u.eventBus.holders;
                        resume();
                    },
                    onerror: function (code, origin, error) {
                        console.error(code, origin, error);
                        u.loading.hide();
                        u.lang.updateNode(main.alert.items[1].body, u.lang.error_while_loading_s_code_s.format(origin, code));
                        main.alert.open();
                    }
                });
            });
        });
    };

    function resume() {
        try {

            var path = window.location.pathname.split("/");
            if(path.length > 2) {
                if(holders[path[1]]) {
                    type = path[1];
                //} else if (path[1] == "404.html" || path[1] == "403.html") {
                } else {
                    type = 404;
                    //window.location = "/404.html";
                    //return;
                }
            } else {
                type = "home";
                window.history.pushState({}, null, "/" + type);
            }

            window.addEventListener("load",function() { setTimeout(function(){ // This hides the address bar:
                window.scrollTo(0, 1); }, 0);
            });

            var out = u.create("div", {className:"layout"}, "layout");

            self.actionbar = u.actionBar({
                title: (holders[type] ? holders[type].title : type),
                onbuttonclick: function(){
                    try {
                        self.drawer.open();
                    } catch(e) {
                        console.error(e);
                    }
                }
            }, "actionbar");
            var selectLang = u.create(HTML.SELECT, { className: "actionbar-select-lang changeable", onchange: function(e, event) {
                var lang = (this.value || navigator.language).toLowerCase().slice(0,2);
                u.save("lang", lang);
                loadResources("index.json");
                u.fire.call(EVENTS.RELOAD, type);
            }}, self.actionbar).place(HTML.OPTION, { name: u.lang.loading, value:"" });

            //u.post("/rest/v1/getContent", {resource: "index-contact.html", locale: lang}).then(function(xhr){
            u.getJSON("/rest/v1/getLocales").then(function(json){
                u.clear(selectLang);
                var count = 1;
                selectLang.place(HTML.OPTION, { innerHTML: "Default", value: "en" });
                for(var x in json.locales) {
                    selectLang.place(HTML.OPTION, { innerHTML: json.locales[x], value: x });
                    if(u.load("lang") == x) selectLang.selectedIndex = count;
                    count++;
                }
            });

            var sections = {};
            sections[DRAWER.SECTION_PRIMARY] = u.lang.home;
            sections[DRAWER.SECTION_MAP] = u.lang.docs;
            sections[DRAWER.SECTION_LAST] = u.lang.info;

            self.drawer = new u.drawer({
                title: "${APP_NAME}",
                collapsed: false,
                logo: {
                    src:"/images/logo.svg"
                },
                onprimaryclick: function(){
                    console.log("onprimaryclick");
                },
                footer: {
                    className: "drawer-footer-label",
                    content: u.create(HTML.DIV)
                        .place(HTML.SPAN, { className: "drawer-footer-link", innerHTML: u.lang.privacy, onclick: showPrivacy})
                        .place(HTML.SPAN, { className: "drawer-footer-link" ,innerHTML: u.lang.terms, onclick: showTerms})
                        .place(HTML.SPAN, { className: "drawer-footer-link", innerHTML: "${APP_NAME} &copy;2017 Edeqa", onclick: function(e){
                            dialogAbout.open();
                            e.preventDefault();
                            e.stopPropagation();
                            return false;
                        }})
                },
                sections: sections
            }, "drawer");

            var dialogAbout = utils.dialogAbout();

            var switchFullDrawer = function(){
                if(this.parentNode.scrollTop) {
                    self.drawer.toggleSize(true);
                    self.actionbar.toggleSize(true);
                    self.buttonScrollTop.show(HIDING.OPACITY);
                    clearTimeout(self.buttonScrollTop.hideTimeout);
                    self.buttonScrollTop.hideTimeout = setTimeout(function(){
                        self.buttonScrollTop.hide(HIDING.OPACITY);
                    }, 1500);
                } else {
                    self.drawer.toggleSize(false);
                    self.actionbar.toggleSize(false);
                    self.buttonScrollTop.hide(HIDING.OPACITY);
                }
            };
            self.content = u.create(HTML.DIV, {className: "content", onwheel: switchFullDrawer, ontouchmove: switchFullDrawer }, "content");
            u.create(HTML.DIV, {className:"alert"}, out);
            self.buttonScrollTop = u.create(HTML.BUTTON, {
                className: "button-scroll-top changeable hidden",
                onclick: function() {
                    self.content.scrollIntoView({block:"start", behaviour: "smooth"});
                    switchFullDrawer.call(self.content);
                },
                innerHTML: "keyboard_arrow_up"
            }, out);

            for(var x in holders) {
                if(holders[x] && holders[x].menu) {

                    var categories = {
                        "main": DRAWER.SECTION_PRIMARY,
                        "docs": DRAWER.SECTION_MAP,
                        "info": DRAWER.SECTION_LAST
                    };

                    var item = self.drawer.add(categories[holders[x].category], x, holders[x].menu, holders[x].icon, function(){
                        var holder = holders[this.instance];
                        self.drawer.toggleSize(false);
                        self.actionbar.toggleSize(false);
                        self.actionbar.setTitle(holder.title);
                        //u.lang.updateNode(self.actionbar.titleNode, holder.title);
                        u.lang.updateNode(self.drawer.headerPrimary, holder.title);

                        u.progress.show(u.lang.loading.innerHTML);
                        u.fire(holder.type, function() {
                            u.byId("content").parentNode.scrollTop = 0;
                            u.progress.hide();
                        });
                        window.history.pushState({}, null, "/" + holder.type);

                        type = holder.type;
                        self.drawer.close();
                        return false;
                    });
                    item.instance = x;
                }
            }

            if(type == 404 || type == 403) {

            } else {
                u.lang.updateNode(self.actionbar.titleNode, holders[type].title);
                u.lang.updateNode(self.drawer.headerPrimary, holders[type].title);
                holders[type].start();
            }
            u.fire(type);
            u.loading.hide();
        } catch(e) {
            console.error(e);
        }
    }

    function showPrivacy(e) {
        showPrivacy.dialog = showPrivacy.dialog || u.dialog({
                title: u.lang.privacy_policy,
                items: [
                    { type: HTML.DIV, className: "privacy-dialog-body", innerHTML: u.lang.loading }
                ],
                positive: {
                    label: u.lang.close
                }
            });

        showPrivacy.dialog.open();

        var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
        u.post("/rest/v1/getContent", {resource: "privacy-policy.html", locale: lang}).then(function(xhr){
            showPrivacy.dialog.items[0].innerHTML = xhr.response;
        }).catch(function(error, json) {
            showPrivacy.dialog.items[0].innerHTML = "Error";
        });

        e.preventDefault();
        e.stopPropagation();
        return false;
    }

    function showTerms(e) {
        showTerms.dialog = showTerms.dialog || u.dialog({
                title: u.lang.terms_of_service,
                items: [
                    { type: HTML.DIV, className: "terms-dialog-body", innerHTML: u.lang.loading }
                ],
                positive: {
                    label: u.lang.close
                }
            });

        showTerms.dialog.open();

        var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
        u.post("/rest/v1/getContent", {resource: "terms-of-service.html", locale: lang}).then(function(xhr){
            showTerms.dialog.items[0].innerHTML = xhr.response;
        }).catch(function(error, json) {
            showTerms.dialog.items[0].innerHTML = "Error";
        });

        e.preventDefault();
        e.stopPropagation();
        return false;
    }

    function loadResources(resource, callback) {
        var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
        u.lang.overrideResources({
            "default": "/resources/en/" + resource,
            resources: "/rest/v1/getContent",
            type: "resources",
            resource: resource,
            locale: lang,
            callback: callback
        });
    }

}
//document.addEventListener("DOMContentLoaded", (window.WTU = new Main()).start);
document.addEventListener("readystatechange", function(){if(document.readyState === "complete"){(window.WTU = new Main()).start()}});
