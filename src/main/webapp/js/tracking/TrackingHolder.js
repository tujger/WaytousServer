/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 2/9/17.
 */

function TrackingHolder(main) {

    var type ="tracking";

    var TRACKING_URI = "uri";
    var progress;
    var progressTitle;
    var drawerItemNew;
    var drawerItemExit;
    var noSleep;
    var wakeLockEnabled;
    var sound;
    var sounds;
    var joinSound;
    var defaultSound = "oringz-w427.mp3";
    var agreementDialog;

    function start(){

        progress = u.dialog({
            queue: true,
            className: "progress-dialog",
            items: [
                { type: HTML.DIV, className: "progress-dialog-circle" },
                { type: HTML.DIV, className: "progress-dialog-title" }
            ]
        }, main.right);

        joinSound = u.load("tracking:sound_on_join") || defaultSound;
        sound = u.create(HTML.AUDIO, {className:"hidden", preload:"", src:"/sounds/"+joinSound, last:0, playButLast:function(){
            var current = new Date().getTime();
            if(current - this.last > 10) {
                this.last = current;
                this.play();
            }
        }}, main.right);
        progressTitle = progress.items[1];
        noSleep = new NoSleep();
        wakeLockEnabled = false;

    }

    function perform(json){
        var loc = utils.jsonToLocation(json);
        var number = u.clear(json[USER.NUMBER]);
        main.users.forUser(number, function(number,user){
            user.addLocation(loc);
        });
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                drawerItemNew = object.add(DRAWER.SECTION_PRIMARY,EVENTS.TRACKING_NEW, u.lang.create_group,
                    u.create(HTML.IMG, {
                        src: "/images/navigation_twinks.svg",
                        className: "icon drawer-menu-item-icon"
                    }),function(){
                        main.fire(EVENTS.TRACKING_NEW);
                    });
                drawerItemNew.hide();
                drawerItemExit = object.add(DRAWER.SECTION_LAST,EVENTS.TRACKING_STOP, u.lang.exit_group,"exit_to_app",function(){
                    main.fire(EVENTS.TRACKING_STOP);
                });
                drawerItemExit.hide();
                break;
            case EVENTS.MAP_READY:
                drawerItemNew.show();
                var path = window.location.pathname.split("/");
                var group = path[2];
//                var groupOld = u.load("group");
                if(group) {
                    if(group.toUpperCase() === "NEW") {
                        window.history.pushState({}, null, path[0] + "/" + path[1]);
                        main.fire(EVENTS.TRACKING_NEW);
                    } else {
                        u.context = group;
                        startTracking(group);
                        //
                        //setTimeout(function () {
                        //    u.require("/js/helpers/TrackingFB.js").then(startTracking.bind(self));
                        //}, 0);
                    }
                } else {
                    //if(!u.load("tracking:terms_of_service_confirmed")) {
                    showAgreementDialog();
//                    main.fire(EVENTS.TRACKING_NEW);
                    //} else {
                    //
                    //}
                }
                break;
            case EVENTS.TRACKING_NEW:
                agreementDialog && agreementDialog.close();
                startTracking();
                break;
            case EVENTS.TRACKING_JOIN:
                path = window.location.pathname.split("/");
                var token = object || path[2];
                path[2] = token;
                path = path.join("/");
                window.history.pushState({}, null, path);
                startTracking(token);
                break;
            case EVENTS.TRACKING_ACTIVE:
                u.context = main.tracking.getToken();
                document.title = u.lang.s_s.format(main.appName, main.tracking.getToken()).innerHTML;
                u.notification({
                    title: u.lang.waytous_online.innerText,
                    body: u.lang.you_have_joined_to_the_group_s.format(main.tracking.getToken()).innerText,
                    icon: "/images/waytous-transparent-256.png",
                    duration: 10000,
                    onclick: function(e){
                        console.log(this,e)
                    }
                });

                /*if (!wakeLockEnabled && /android/ig.test(navigator.userAgent)) {
                    noSleepDialog = noSleepDialog || u.dialog({
                        queue: true,
                        items: [
                            { type: HTML.DIV, innerHTML: u.lang.do_you_want_to_keep_screen_on_during_group_is_active }
                        ],
                        positive: {
                            label: u.lang.yes,
                            onclick: function(){
                                noSleep.enable(); // keep the screen on!
                                wakeLockEnabled = true;
                            }
                        },
                        negative: {
                            label: u.lang.no
                        },
                        timeout: 3000
                    });
                    noSleepDialog.open();
                }*/
                break;
            case EVENTS.TRACKING_CONNECTING:
//                window.onbeforeunload = beforeunload;
//                agreementDialog.close();
                document.title = u.lang.connecting_s.format(main.appName).innerHTML;
                drawerItemNew.hide();
                drawerItemExit.show();
                break;
            case EVENTS.TRACKING_RECONNECTING:
//                window.onbeforeunload = beforeunload;

                document.title = u.lang.connecting_s.format(main.appName).innerHTML;
                drawerItemNew.hide();
                drawerItemExit.show();
                break;
            case EVENTS.TRACKING_DISABLED:
                window.onbeforeunload = null;

                document.title = main.appName;
                drawerItemExit.hide();
                if (wakeLockEnabled) {
                    noSleep.disable(); // let the screen turn off.
                    wakeLockEnabled = false;
                }
                break;
            case EVENTS.TRACKING_STOP:
                if(main.tracking.getStatus() !== EVENTS.TRACKING_DISABLED) {
                    main.users.forAllUsersExceptMe(function (number, user) {
                        user.removeViews();
                    });
                    main.tracking && main.tracking.stop();
                    u.save("group");
                }
                break;
            case EVENTS.SYNC_PROFILE:
                synchronizeTosConfirmation();
                break;
            default:
                break;
        }
        return true;
    }

    function startTracking(group) {
        if(u.load("tracking:terms_of_service_confirmed")) {
            //if(group) {
                //setTimeout(function(){
                u.require("/js/helpers/TrackingFB.js").then(startTrackingReady.bind(self));
                //}, 0);
                //startTrackingReady();
            //} else {
    //        u.load("tracking:terms_of_service_confirmed")
    //            showAgreementDialog();
    //        }
        } else {
            showAgreementDialog(group);
        }
    }

    function showAgreementDialog(group) {
        agreementDialog = agreementDialog || u.dialog({
            title: u.lang.information,
            className: "wizard-dialog",
            items: [
                { type: HTML.DIV, className: "wizard-dialog-item", innerHTML: u.lang.you_may_create_the_group },
                {
                    type: HTML.DIV,
                    enclosed: true,
                    label: u.lang.terms_of_service_click,
                    body: u.lang.loading.outerHTML,
                    className: "wizard-dialog-item",
                    innerHTML: u.lang.loading,
                    onopen: function(e) {
                        if(!e.loaded) {
                            var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0, 2);
                            u.post("/rest/v1/getContent", {
                                resource: "terms-of-service.html",
                                locale: lang
                            }).then(function (xhr) {
                                e.body.innerHTML = xhr.response;
                                e.loaded = true;
                            }).catch(function (error, json) {
                                e.body.innerHTML = u.lang.error;
                            });
                        }
                    }
                },
                {
                    type: HTML.CHECKBOX,
                    itemClassName: "wizard-dialog-item-agree",
                    label: u.lang.i_have_read_and_agree_with_terms_of_service,
                    onclick: function() {
                        if(this.checked) {
                            u.lang.updateNode(agreementDialog.positive, !!agreementDialog.groupId ? u.lang.join_group : u.lang.create_group);
                            agreementDialog.positive.classList.remove("wizard-dialog-button-disabled");
                            agreementDialog.positive.disabled = false;
                        } else {
                            agreementDialog.positive.classList.add("wizard-dialog-button-disabled");
                            agreementDialog.positive.disabled = true;
                        }
                    }
                }
            ],
            positive: {
                label: u.lang.close,
                className: "wizard-dialog-button-create wizard-dialog-button-disabled",
                disabled: true,
                onclick: function(items) {
                    if(items[2].checked) {
                        var sync = new utils.sync({
                            type: utils.sync.Type.ACCOUNT_PRIVATE,
                            key: DATABASE.TERMS_OF_SERVICE_CONFIRMED,
                            log: true,
                            onfinish: function(mode, key, value) {
                                // if(value) {
                                    u.save("tracking:terms_of_service_confirmed", true);
                                    u.require("/js/helpers/TrackingFB.js").then(startTrackingReady.bind(self));
                                // } else {
                                //     console.error("Unable to start tracking");
                                //     main.toast.show("Unable to start tracking");
                                // }
                            },
                            onerror: function(key, error) {
                                console.error(error);
                                main.toast.show(error);
                            }
                        });
                        if(sync.ready()) {
                            sync.overrideRemoteValue(true);
                        } else {
                            u.save("tracking:terms_of_service_confirmed", true);
                            u.require("/js/helpers/TrackingFB.js").then(startTrackingReady.bind(self));
                            //startTracking(agreementDialog.groupId);
                        }
                    } else {
                        window.history.pushState({}, null, "/group/");
                    }
                }
            }
        }, main.right);

        if(u.load("tracking:terms_of_service_confirmed")) {
            agreementDialog.items[2].checked = false;
            agreementDialog.items[2].click();
            agreementDialog.itemsLayout.childNodes[2].hide();
        } else {
            agreementDialog.itemsLayout.childNodes[2].show();
        }
        if(group) {
            u.lang.updateNode(agreementDialog.items[0], u.lang.you_are_joining_the_group);
            u.lang.updateNode(agreementDialog.positive, u.lang.join_group);
        } else {
            u.lang.updateNode(agreementDialog.items[0], u.lang.you_may_create_the_group);
            u.lang.updateNode(agreementDialog.positive, u.lang.create_group);
        }
        agreementDialog.groupId = group;
        agreementDialog.open();
    }

    function startTrackingReady(){
        progress.open();

        this.tracking = main.tracking = new TrackingFB(main);

        var a = window.location.pathname.split("/");
        if(a[2]) {
            a[2] = a[2].toUpperCase();

            window.history.pushState({}, null, a.join("/"));

            main.fire(EVENTS.TRACKING_RECONNECTING);
            this.tracking.setLink(window.location.href);
            u.save("group", a[2]);
        } else {
            progressTitle.innerHTML = u.lang.creating_group;
        }
        this.tracking.setTrackingListener(onTrackingListener);
        this.tracking.start();

    }

    var onTrackingListener = {
        onCreating: function(){
            // console.log("ONCREATING");
            u.lang.updateNode(progressTitle, u.lang.connecting);
            //progressTitle.innerHTML = u.lang.connecting;
            progress.open();

            u.saveForContext(TRACKING_URI, null);
            main.fire(EVENTS.TRACKING_CONNECTING);
        },
        onJoining: function(){
            // console.log("ONJOINING");
            u.lang.updateNode(progressTitle, u.lang.joining_group);
//            progressTitle.innerHTML = u.lang.joining_group;
            progress.open();
            main.fire(EVENTS.TRACKING_RECONNECTING, u.lang.joining_group);
        },
        onReconnecting: function(){
            // console.log("ONRECONNECTING");
            u.lang.updateNode(progressTitle, u.lang.reconnecting);
//            progressTitle.innerHTML = u.lang.reconnecting;
            progress.open();
            main.fire(EVENTS.TRACKING_RECONNECTING, u.lang.reconnecting);
        },
        onClose: function(){
            console.log("ONCLOSE");
        },
        onAccept: function(o){
            try {
                if(main.tracking.getStatus() !== EVENTS.TRACKING_ACTIVE) {
                    main.tracking.setStatus(EVENTS.TRACKING_ACTIVE);
                    main.fire(EVENTS.TRACKING_ACTIVE);
                }
                if (o[RESPONSE.TOKEN]) {
                    var token = o[RESPONSE.TOKEN];
                    main.fire(EVENTS.TOKEN_CREATED, token);
                    u.save("group", token);
                    window.history.pushState({}, null, "/group/" + token);
                    main.fire(EVENTS.SHOW_HELP, {module: main.eventBus.holders.tracking, article: 1});
                    main.me.fire(EVENTS.SELECT_USER);
                }
                if (o[REQUEST.WELCOME_MESSAGE]) {
                    main.fire(EVENTS.WELCOME_MESSAGE, o[RESPONSE.WELCOME_MESSAGE]);
                }
                if (o[RESPONSE.NUMBER] != undefined) {
                    main.users.forMe(function (number, user) {
                        user.createViews();
                        progress.close();
                    })
                }
//                if (o[RESPONSE.INITIAL]) {
//                    main.users.forAllUsersExceptMe(function (number, user) {
//                        user.createViews();
//                    })
//                }
            } catch(e) {
                console.error(e);
            }
        },
        onReject: function(reason){
            console.error("ONREJECT",reason);
            u.saveForContext(TRACKING_URI);
            main.fire(EVENTS.TRACKING_DISABLED);
            main.fire(EVENTS.TRACKING_ERROR, reason);

            progress.close();
            u.save("group");

            u.dialog({
                queue: true,
                className: "alert-dialog",
                modal: true,
                items: [
                    { type: HTML.DIV, innerHTML: reason || u.lang.sorry_you_have_requested_the_expired_group },
                    { type: HTML.DIV, enclosed:true, body: u.lang.expired_explanation },
                ],
                positive: {
                    label: u.lang.ok
                },
                onclose: function() {
                    window.location = "/group/";
                }
            }, main.right).open();

//            window.history.pushState({}, null, "/group/" );

        },
        onStop: function(){
            console.log("ONSTOP");
            u.saveForContext(TRACKING_URI);
            main.fire(EVENTS.TRACKING_DISABLED);
        },
        onMessage: function(o){
            // console.log("ONMESSAGE",o);
            try {
                var response = o[RESPONSE.STATUS];
                switch (response) {
                    case RESPONSE.STATUS_UPDATED:
                        if (o[USER.DISMISSED] != undefined) {
                            var number = o[USER.DISMISSED];
                            // console.log("DISMISSED",number);
                            var user = main.users.users[number];
                            //user.createViews();
                            //user.removeViews();
                            if(user.properties && user.properties.active) {
                                user.fire(EVENTS.MAKE_INACTIVE);
                                main.fire(USER.DISMISSED, user);
                            }
                        } else if (o[USER.JOINED] != undefined) {
                            number = o[USER.JOINED];
                            user = main.users.users[number];
                            progress.close();
                            //user.createViews();

                            if(user.properties && !user.properties.active) {
                                if(!user.changed || new Date().getTime() - 15 * 60 * 1000 > user.changed) {
                                    main.toast.show(u.lang.user_s_has_joined.format(user.properties.getDisplayName()), 10000);
                                    sound.playButLast();
                                }
                            }
                            user.fire(EVENTS.MAKE_ACTIVE);
                            main.fire(USER.JOINED, user);
                            if(user.properties && user.properties.active) {
                                var timestamp = o[REQUEST.TIMESTAMP];
                                if(utils.isEnabledTime(timestamp) && !user.properties.enabled) {
                                    user.fire(EVENTS.MAKE_ENABLED, timestamp);
                                } else if (!utils.isEnabledTime(timestamp) && user.properties.enabled) {
                                    user.fire(EVENTS.MAKE_DISABLED, timestamp);
                                }
                            }
                        } else {
                            number = o[RESPONSE.NUMBER];
                            user = main.users.users[number];

                            if(user.properties && user.properties.active) {
                                timestamp = o[REQUEST.TIMESTAMP];
                                if(utils.isEnabledTime(timestamp) && !user.properties.enabled) {
                                    user.fire(EVENTS.MAKE_ENABLED, timestamp);
                                } else if (!utils.isEnabledTime(timestamp) && user.properties.enabled) {
                                    user.fire(EVENTS.MAKE_DISABLED, timestamp);
                                }
                            }
                        }
                        break;
                    case RESPONSE.LEAVE:
                        if (o[USER.NUMBER]) {
                            console.log("LEAVE", o[USER.NUMBER]);
                        }
                        break;
                    case RESPONSE.CHANGE_NAME:
                        if (o[USER.NAME]) {
                            console.log("CHANGENAME", o[USER.NUMBER], o[USER.NAME]);
                        }
                        break;
                    default:
                        // console.log(type,response,o);
                        var holder = main.eventBus.holders[response];
                        if (holder && holder.perform) {
                            holder.perform(o);
                        }
                        break;
                }
            } catch(e) {
                console.error(e);
            }
        }
    };

    function beforeunload(evt) {
        return u.lang.beforeunload;
    }

    function synchronizeTosConfirmation() {
        var sync = new utils.sync({
            type: utils.sync.Type.ACCOUNT_PRIVATE,
            key: "tos-confirmed",
            log: true,
            onsavelocalvalue: function(key, newValue, oldValue) {
                console.log("LOCAL", key, newValue, oldValue)
                if(newValue !== undefined && newValue !== null && newValue.constructor === Boolean) {
                    u.save("tracking:terms_of_service_confirmed", newValue);
                    if(newValue) window.location.reload();
                }
            },
            onsaveremotevalue: function(key, newValue, oldValue) {
                console.log("REMOTE", key, newValue, oldValue)
            }
        });
        if(sync.ready()) {
            sync.syncValue(u.load("tracking:terms_of_service_confirmed"))
        }
    }



    function help(){
        return {
            title: u.lang.tracking_help_title,
            1: {
                ignore: true,
                title: u.lang.tracking_help_title_1,
                body: u.lang.tracking_help_body_1
            }
        }
    }

    function options(){
        return {
            id: "general",
            title: u.lang.general,
            categories: [
                {
                    id: "general:notifications",
                    title: u.lang.notifications,
                    items: [
                        {
                            id:"tracking:sound_on_join",
                            type: HTML.SELECT,
                            label: u.lang.sound_on_join,
                            default: u.load("tracking:sound_on_join") || defaultSound,
                            onaccept: function(e, event) {
                                u.save("tracking:sound_on_join", this.value);
                                sound.src = "/sounds/" + this.value;
                            },
                            onchange: function(e, event) {
                                var sample = u.create(HTML.AUDIO, {className:"hidden", preload:true, src:"/sounds/"+this.value}, main.right);
                                sample.addEventListener("load", function() {
                                    sample.play();
                                }, {passive: true});
                                sample.play();
                            },
                            onshow: function(e) {
                                if(sounds) {
                                } else {
                                    u.getJSON("/rest/v1/getSounds").then(function(json){
                                        sounds = {};
                                        u.clear(e);
                                        var selected = 0;
                                        for(var i in json.files) {
                                            var file = json.files[i];
                                            var name = (file.replace(/\..*$/,"").replace(/[\-_]/g," ")).toUpperCaseFirst();
                                            sounds[file] = name;
                                            u.create(HTML.OPTION, {value:file, innerHTML:name}, e);
                                            if((joinSound || defaultSound) === file) selected = i;
                                        }
                                        e.selectedIndex = selected;
                                    });
                                }
                            },
                            values: {"":u.lang.loading.innerText}
                        }
                    ]
                }
            ]
        }
    }


    return {
        type:type,
        start:start,
        onEvent:onEvent,
        perform:perform,
        saveable:true,
        help:help,
        options:options
    }
}
