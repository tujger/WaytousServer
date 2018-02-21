/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 2/8/17.
 */
EVENTS.UPDATE_ACTIONBAR_SUBTITLE = "update_actionbar_subtitle";
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

function DrawerHolder(main) {

    var drawer;
    var backButtonAction;
    var actionbar;
    var drawerItemShare;

    var start = function() {
        var dialogAbout = utils.dialogAbout(main.right);

        dialogAbout.addItem({
            enclosed: true,
            label: u.lang.privacy_policy,
            body: u.lang.loading.outerHTML,
            onopen: function(e) {
                var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
                u.post("/rest/content", {resource: "privacy-policy.html", locale: lang}).then(function(xhr){
                    e.body.innerHTML = xhr.response;
                }).catch(function(error, json) {
                    e.body.innerHTML = u.lang.error;
                });
            }
        });
        dialogAbout.addItem({
            enclosed: true,
            label: u.lang.terms_of_service,
            body: u.lang.loading.outerHTML,
            className: "dialog-about-terms",
            onopen: function(e) {
                var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
                u.post("/rest/content", {resource: "terms-of-service.html", locale: lang}).then(function(xhr){
                    e.body.innerHTML = xhr.response;
                }).catch(function(error, json) {
                    e.body.innerHTML = u.lang.error;
                });
            }
        });
        dialogAbout.addItem({
            enclosed: true,
            label: u.lang.third_party_components,
            body: "Third party components",
            className: "dialog-about-third"
        });

        var sections = {};
        sections[DRAWER.SECTION_MAP] = u.lang.map;

        drawer = new u.drawer({
            title: main.appName,
            subtitle: u.lang.be_always_on_the_same_way,
            logo: {
                src:"/images/logo.svg",
                onclick: function(){
                    dialogAbout.open();
                }
            },
            ontogglesize: function() {
                main.fire(EVENTS.CAMERA_UPDATE);
            },
            onprimaryclick: function(){
                main.me.fire(EVENTS.SELECT_SINGLE_USER);
            },
            footer: {
                className: "drawer-footer-label",
                content: u.create(HTML.DIV).place(HTML.SPAN, {className: "drawer-footer-link", innerHTML: "${APP_NAME} &copy;2017-18 Edeqa", onclick: function(e){
                    dialogAbout.open();
                    // e.preventDefault();
                    // e.stopPropagation();
                    return false;
                }}).place(HTML.SPAN, "\nBuild " + data.version)
            },
            sections: sections,
            collapsible: [DRAWER.SECTION_MAP]
        }, document.body);

        actionbar = u.actionBar({
            title: main.appName,
            onbuttonclick: function(){
                 try {
                     drawer.open();
                 } catch(e) {
                     console.error(e);
                 }
             }
        }, main.right);

        setTimeout(function(){
            main.fire(EVENTS.CREATE_DRAWER, drawer);
        },0);

        window.history.pushState(null, document.title, location.href);
        backButtonAction = function (event) {
           window.history.pushState(null, document.title, location.href);
           drawer.toggle();
        }

    };

    var onEvent = function(EVENT,object){
        switch (EVENT){
            case EVENTS.UPDATE_ACTIONBAR_SUBTITLE:
                if(object && main.users.getCountSelected() === 1) {
                    if(this.properties.selected) {
                        actionbar.subtitle.innerHTML = object;
                        actionbar.subtitle.show();
                    }
                } else {
                    actionbar.subtitle.hide();
                }
                break;
            case EVENTS.TRACKING_ACTIVE:
                actionbar.titleNode.innerHTML = main.appName;
                drawer.headerTitle.innerHTML = main.appName;
                drawerItemShare.show();
                break;
            case EVENTS.TRACKING_DISABLED:
                actionbar.titleNode.innerHTML = main.appName;
                drawer.headerTitle.innerHTML = main.appName;
                window.removeEventListener("popstate", backButtonAction);
                drawerItemShare.hide();
                break;
            case EVENTS.TRACKING_CONNECTING:
            case EVENTS.TRACKING_RECONNECTING:
                u.lang.updateNode(actionbar.titleNode, u.lang.connecting);
                u.lang.updateNode(drawer.headerTitle, u.lang.connecting);
                window.addEventListener("popstate", backButtonAction);
                drawerItemShare.show();
                break;
            case EVENTS.CHANGE_NAME:
            case USER.JOINED:
                if(main.me.properties && main.me.properties.getDisplayName) {
                    drawer.headerPrimary.innerHTML = main.me.properties.getDisplayName();
                }
                break;
            case EVENTS.SELECT_SINGLE_USER:
                actionbar.titleNode.innerHTML = this.properties.getDisplayName();
                if(this !== main.me) {
                    var delta = new Date().getTime() - this.properties.changed;
                    if(delta > 60000) {
                        var text = utils.toDateString(delta);
                        u.create(HTML.DIV, {
                            className: "actionbar-title-suffix",
                            innerHTML: u.lang.s_ago.format(text)
                        }, actionbar.titleNode);
                    }
                }
                if(main.tracking && main.tracking.getStatus() === EVENTS.TRACKING_ACTIVE) {
                    actionbar.style.backgroundColor = utils.getRGBAColor(this.properties.color, 0.8);
                }
                break;
            case EVENTS.SELECT_USER:
                if(main.users.getCountSelected() > 1) {
                    actionbar.style.backgroundColor = "";
                    u.lang.updateNode(actionbar.titleNode, u.lang.d_selected.format(main.users.getCountSelected()));
                    actionbar.subtitle.hide();
                }
                break;
            case EVENTS.CREATE_DRAWER:
                drawerItemShare = drawerItemShare || object.add({section: DRAWER.SECTION_COMMUNICATION, id: "share", name: u.lang.share, icon: "share", callback: function(){
                    main.fire(EVENTS.SHARE_LINK, main.tracking.getTrackingUri());
                }});
                drawerItemShare.hide();
                break;
        }
        return true;
    };

    function createView(user) {
        return {};
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
                            id:"drawer:collapsed",
                            itemClassName: "media-hidden",
                            type: HTML.CHECKBOX,
                            label: u.lang.collapsed_drawer,
                            checked: u.load("drawer:collapsed"),
                            onaccept: function(e, event) {
                                drawer.toggleSize(this.checked);
                            }
                        }
                    ]
                }
            ]
        }
    }

    return {
        type:"drawer",
        start:start,
        onEvent:onEvent,
        createView:createView,
        options:options
    }
}

