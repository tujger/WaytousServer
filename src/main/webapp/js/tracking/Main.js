/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 1/20/17.
 */

function Main() {
    var firebaseVersion = "4.6.2";
    var users;
    var me;
    var main = window.Waytous = this;

    if (!data.is_debug_mode && "serviceWorker" in navigator) {
        window.addEventListener("load", function() {
            navigator.serviceWorker.register("/sw.js")
            .then(function(registration) {
                console.log("ServiceWorker registration successful with scope:", registration);
            })
            .catch(function(err) {
                if(err.code == 18) {
                    console.warn(err);
                    return;
                }
                console.error("ServiceWorker registration failed:", err);
                throw new Error("ServiceWorker error:",err);
            });
        });
    }

    function start() {
        var a = document.createElement("script");
        a.setAttribute("src","/js/Edequate.js");
        a.setAttribute("defer","");
        a.setAttribute("async","");
        a.setAttribute("onload","preloaded()");
        document.head.appendChild(a);
    }

    preloaded = function(){
        window.u = new Edequate({exportConstants:true, origin:"waytous"});

        main.right = main.layout = u.create(HTML.DIV, {className:"layout changeable"}, document.body);
        main.appName = "${APP_NAME}";

        u.loading("0%");
        u.require("/js/helpers/Constants").then(function(){
            loadResources("tracking.json", function() {
                initializeHeader();
                initializeProperties();
                loadScripts();
            });
        });

        window.addEventListener("load", function() { window. scrollTo(0, 0); });
//        document.addEventListener("touchmove", function(e) { e.preventDefault() });

        //addConsoleLayer(main.right);

    };

    function initializeHeader() {

        document.head
            .place(HTML.META, {name:"viewport", content:"width=device-width, initial-scale=1, maximum-scale=5, user-scalable=no"})
            .place(HTML.STYLE, {innerHTML: "@import url('/css/edequate.css');@import url('/css/tracking.css');@import url('https://fonts.googleapis.com/icon?family=Material+Icons');"})
//            .place(HTML.LINK, {rel:HTML.STYLESHEET, href:"/css/edequate.css"})
//            .place(HTML.LINK, {rel:HTML.STYLESHEET, href:"/css/tracking.css"})
//            .place(HTML.LINK, {rel:HTML.STYLESHEET, href:"https://fonts.googleapis.com/icon?family=Material+Icons"})
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
            .place(HTML.LINK, {rel:"manifest", href:"/icons/manifest.json"})
            .place(HTML.LINK, {rel:"mask-icon", href:"/icons/safari-pinned-tab.svg", color:"#00aaaa"})
            .place(HTML.LINK, {rel:"icon",type:"image/x-icon", href:"/icons/favicon.ico"})
            .place(HTML.LINK, {rel:"shortcut icon", href:"/icons/favicon.ico"})
            .place(HTML.LINK, {rel:"apple-touch-startup-image", href: "/icons/apple-touch-icon.png"})
            .place(HTML.META, {name:"apple-mobile-web-app-title", content:main.appName})
            .place(HTML.META, {name:"apple-mobile-web-app-capable", content:"yes"})
            .place(HTML.META, {name:"apple-mobile-web-app-status-bar-style", content:"black-translucent"})
            .place(HTML.META, {name:"mobile-web-app-capable", content:"yes"})
            .place(HTML.META, {name:"application-name", content:main.appName})
            .place(HTML.META, {name:"application-shortName", content:main.appName})
            .place(HTML.META, {name:"msapplication-TileColor", content:"#00aaaa"})
            .place(HTML.META, {name:"msapplication-TileImage", content:"/icons/mstile-144x144.png"})
            .place(HTML.META, {name:"msapplication-config", content:"/icons/browserconfig.xml"})
            .place(HTML.META, {name:"theme-color", content:"#aaeeee"});
            //.place(HTML.SCRIPT, {innerHTML: "window.ga=window.ga||function(){(ga.q=ga.q||[]).push(arguments)};ga.l=+new Date;\n" +
            //"ga('create', 'UA-104070698-1', 'auto');\n" +
            //"ga('send', 'pageview');"})
            //.place(HTML.SCRIPT, {src: "https://www.google-analytics.com/analytics.js", async: true});

        if(data && data.google_analytics_tracking_id) {
            document.head.place(HTML.SCRIPT, {src:"https://www.googletagmanager.com/gtag/js?id=" + data.google_analytics_tracking_id, async:""})
                .place(HTML.SCRIPT, {innerHTML: "window.dataLayer = window.dataLayer || [];\n" +
                "function gtag(){dataLayer.push(arguments)};\n" +
                "gtag('js', new Date());\n" +
                "gtag('config', '" + data.google_analytics_tracking_id + "');"});
        }

    }

    function initializeProperties() {
        main.help = help;
        main.options = options;
        main.me = me;
        main.initialize = initialize;
        main.eventBus = u.eventBus;
        main.fire = u.fire;
        main.toast = u.toast;
        main.right.appendChild(main.toast);
        main.alert = main.alert || u.dialog({
             queue: true,
             className: "alert-dialog",
             items: [
                 { type: HTML.DIV, label: u.lang.error_while_loading_service },
                 { type: HTML.DIV, enclosed: true, body: "" },
             ],
             positive: {
                 label: u.create(HTML.SPAN, "Reload"),
                 onclick: function(){
                     window.location.reload();
                 }
             },
             help: function() {
                main.fire(EVENTS.SHOW_HELP, {module: main, article: 1});
             }
         }, document.body);
    }

    function loadScripts(){
        var files = data.is_debug_mode ? [
            // "https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-app.js", // https://firebase.google.com/docs/web/setup
            // "https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-auth.js",
            // "https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-database.js",
            "https://cdnjs.cloudflare.com/ajax/libs/fingerprintjs2/1.5.1/fingerprint2.min.js", // https://cdnjs.com/libraries/fingerprintjs2
            "/js/helpers/Utils.js",
            "/js/helpers/MyUser",
            "/js/helpers/MyUsers",
            "/js/helpers/NoSleep.js",
            "/js/tracking/PropertiesHolder", // must be first of holders
            "/js/tracking/AddressHolder",
            "/js/tracking/GpsHolder",
            "/js/tracking/ButtonHolder",
            "/js/tracking/CameraHolder",
            "/js/tracking/DistanceHolder",
            "/js/tracking/DrawerHolder",
            // "/js/tracking/FabHolder",
            "/js/tracking/HelpHolder",
            "/js/tracking/MapHolder",
            "/js/tracking/MarkerHolder",
            "/js/tracking/MessageHolder",
            "/js/tracking/NavigationHolder",
            "/js/tracking/PlaceHolder",
            "/js/tracking/SavedLocationHolder",
            "/js/tracking/ShareHolder",
            "/js/tracking/StreetViewHolder",
            "/js/tracking/TrackingHolder",
            "/js/tracking/TrackHolder",
            "/js/tracking/UserProfileHolder",
            "/js/tracking/SettingHolder"
//            "/js/tracking/WelcomeHolder",
//             "/js/tracking/SampleHolder",
        ] : [
            // "https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-app.js", // https://firebase.google.com/docs/web/setup
            // "https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-auth.js",
            // "https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-database.js",
            "https://cdnjs.cloudflare.com/ajax/libs/fingerprintjs2/1.5.1/fingerprint2.min.js", // https://cdnjs.com/libraries/fingerprintjs2
            "/js/all.js"
        ];
        var modules = data.is_debug_mode ? null : [
            "MyUser",
            "MyUsers",
//            "NoSleep",
            "PropertiesHolder", // must be first of holders
            "AddressHolder",
            "GpsHolder",
            "ButtonHolder",
            "CameraHolder",
            "DistanceHolder",
            "DrawerHolder",
            "HelpHolder",
            "MapHolder",
            "MarkerHolder",
            "MessageHolder",
            "NavigationHolder",
            "SettingHolder",
            "PlaceHolder",
            "SavedLocationHolder",
            "ShareHolder",
            "StreetViewHolder",
            "TrackingHolder",
            "TrackHolder",
            "UserProfileHolder"
        ];

        u.eventBus.register(files, {
            context: main,
            modules: modules,
            onprogress: function(loaded) {
                u.loading(Math.ceil(loaded / files.length * 100) + "%");
            },
            onstart: function() {
                window.utils = new Utils(main);
            },
            onsuccess: function() {
                utils.getUuid(initialize);
            },
            onerror: function(code, origin, error) {
                console.error(code, origin, error);
                u.loading.hide();
                u.lang.updateNode(main.alert.items[1].body, u.lang.error_while_loading_s_code_s.format(origin,code));
                main.alert.open();
            }
        });

        // main.right.webkitRequestFullScreen();
        /*window.addEventListener("load",function() { setTimeout(function(){ // This hides the address bar:
            window.scrollTo(0, 1); }, 0);
        });*/

    }

    function initialize() {

//        if(!firebase || !firebase.database || !firebase.auth) {
//            console.error("Failed firebase loading, trying again...");
////debugger;
//            var files = [];
//            if(!firebase) files.push("https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-app.js");
//            if(!firebase.database) files.push("https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-database.js");
//            if(!firebase.auth) files.push("https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-auth.js");
//
//            var loaded = 0;
//            var failed = false;
//            for(var i in files) {
//                var file = files[i];
//                u.require(file, main).then(function() {
//                    if(failed) return;
//                    loaded++;
//                    u.loading(Math.ceil(loaded / files.length * 100) + "%");
//                    if(loaded === u.keys(files).length) {
//                        initialize.call(main);
//                    }
//                }).catch(function(code, moduleName, event) {
//                    console.log(code, moduleName, event.srcElement.src);
//                    if(failed) return;
//                    failed = true;
//
//                    u.lang.updateNode(main.alert.items[1].body, u.lang.error_while_loading_s_code_s.format(moduleName,code));
//                    main.alert.open();
//                });
//            }
//
//            return;
//        }
        // firebase.initializeApp(data.firebase_config);
        database = firebase.database();

        firebase.auth().onAuthStateChanged(function(user) {
            main.fire(EVENTS.FIREBASE_READY);
        });

        u.loading.hide();

        setTimeout(function(){
            main.users = users = new MyUsers(main);

            if(!me){
                main.me = me = new MyUser(main);
                me.user = true;
                me.color = "#0000FF";
                me.number = 0;
                me.active = true;
                me.selected = true;

                if(u.load("properties:name")){
                    me.name = u.load("properties:name");
                }
            }
            users.setMe();
        },0);

        window.addEventListener("load", hideAddressBar );
        window.addEventListener("orientationchange", hideAddressBar );

    }

    function hideAddressBar() {
        if(!window.location.hash) {
            if(document.height < window.outerHeight) {
                document.body.style.height = (window.outerHeight + 50) + 'px';
            }
            setTimeout( function(){ window.scrollTo(0, 1); }, 50 );
        }
    }

    function help(){
        return {
            title: u.lang.general,
            1: {
                title: "Abcdef",
                body: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras pellentesque aliquam tellus, quis finibus odio faucibus sed. Nunc nec dictum ipsum, a efficitur sem. Nullam suscipit quis neque in cursus. Etiam tempus imperdiet scelerisque. Integer ut nisi at est varius rutrum quis eget urna. "
            }
        }
    }

    function options(){
        return {
            id: "general",
            title: u.lang.general,
            categories: [
                {
                    id: "general:main",
                    title: u.lang.main,
                    items: [
                        {
                            id:"main:lang",
                            type: HTML.SELECT,
                            label: u.lang.language,
                            default: u.load("lang") || "",
                            onaccept: function(e, event) {
                                var lang = (this.value || navigator.language).toLowerCase().slice(0,2);
                                u.save("lang", lang);
                                loadResources("tracking.json");
                            },
                            values: {"": u.lang.default, "en": u.lang.english, "ru": u.lang.russian }
                        },
                        {
                            id:"main:reset_dialogs",
                            type: HTML.BUTTON,
                            label: u.lang.dialogs_positions,
                            itemClassName: "media-hidden",
                            className: "dialog-button",
                            innerHTML: u.lang.reset,
                            onclick: function(e, event) {
                                for(var x in localStorage) {
                                    if(x.indexOf(u.origin + ":dialog:") === 0) {
                                        delete localStorage[x];
                                    }
                                }
                                var items = document.getElementsByClassName("modal");
                                for(var i in items) {
                                    items[i].style = "";
                                }
                                this.dialog.close();
                            }
                        }
                        /*{
                            id:"main:force_reload",
                            type: HTML.BUTTON,
                            label: u.lang.force_reload,
                            className: "dialog-button",
                            innerHTML: u.lang.go,
                            onclick: function(e, event) {
                                window.location.reload(true);
                            }
                        }*/
                    ]
                },
                {
                    id: "general:notifications",
                    title: u.lang.notifications,
                    items: [
                        {
                            id:"notification:disable",
                            type: HTML.CHECKBOX,
                            label: u.lang.onscreen_notifications,
                            checked: !u.load("main:disable_notification"),
                            onaccept: function(e, event) {
                                u.save("main:disable_notification", !this.checked);
                            },
                            onshow: function(e) {
                                if (!("Notification" in window) || Notification.permission.toLowerCase() === 'denied') {
                                    e.parentNode.hide();
                                }
                            }
                        }
                    ]
                }
            ]
        }
    }

    function loadResources(resource, locale, callback) {
        if(locale && locale.constructor !== String) {
            callback = locale;
            locale = null;
        }
        var lang = (locale || u.load("lang") || navigator.language).toLowerCase().slice(0,2);
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
        main:main,
//        fire:fire,
        initialize:initialize,
        // help:help,
        options:options
    }
}