/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 2/11/17.
 */
EVENTS.HIDE_MENU_SUBTITLE = "hide_menu_subtitle";
EVENTS.SHOW_MENU_SUBTITLE = "show_menu_subtitle";
EVENTS.UPDATE_MENU_SUBTITLE = "update_menu_subtitle";
EVENTS.UPDATE_MENU_SUFFIX = "update_menu_suffix";
EVENTS.UPDATE_MENU_PREFIX = "update_menu_prefix";
EVENTS.EXPAND_MENU = "expand_menu";
EVENTS.COLLAPSE_MENU = "collapse_menu";

MENU = {
    SECTION_PRIMARY: 0,
    SECTION_COMMUNICATION: 2,
    SECTION_VIEWS: 3,
    SECTION_NAVIGATION: 4,
    SECTION_EDIT: 5,
    SECTION_MAP: 8,
    SECTION_LAST: 9
}

function ButtonHolder(main) {

    var type = "button";
    var buttons;
    var contextMenu;
    var sections;
    var contextMenuLayout;
    var delayDismiss;
    var startTime;

    function start() {
        buttons = u.dialog({
            id: "button",
            title: {
                label: "Users",
                className: "user-buttons-title",
                button: {
                    icon: "view_headline",
                    className: "user-buttons-title-button notranslate",
                    onclick: function() {
                        var minimized = u.load("button:minimized");
                        u.save("button:minimized", !minimized);
                        main.fire(minimized ? EVENTS.EXPAND_MENU : EVENTS.COLLAPSE_MENU);
                    }
                }
            },
            className: "user-buttons",
            tabindex: 1,
            resizeable: true,
            items: [],
            itemsClassName: "user-buttons-items"
        }, main.right);

        contextMenuLayout = u.create(HTML.DIV, {
            className: "user-context-menu shadow hidden",
            tabindex: 2,
            onblur: function(){
                contextMenuLayout.scrollTop = 0;
                contextMenuLayout.hide(HIDING.OPACITY);
                setTimeout(function() {
                    contextMenuLayout.classList.remove("user-context-menu-list");
                }, 150);
            }, onmouseleave: function(){
                contextMenuLayout.hide(HIDING.OPACITY);
            }, onmouseenter: function(){
                clearTimeout(delayDismiss);
            }
        }, main.right);
        contextMenu = new ContextMenu();
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.TRACKING_ACTIVE:
                buttons.open();
                break;
            case EVENTS.TRACKING_DISABLED:
                buttons.close();
                break;
            case EVENTS.SELECT_USER:
                this.views.button.button.scrollIntoView({behaviour: "smooth"});
                this.views.button.button.classList.add("user-button-selected");
                break;
            case EVENTS.UNSELECT_USER:
                this.views.button.button.classList.remove("user-button-selected");
                break;
            case EVENTS.CHANGE_NAME:
                this.views.button.title.innerHTML = this.properties.getDisplayName();
                break;
            case EVENTS.CHANGE_NUMBER:
                this.views.button.button.dataset.number = parseInt(object);
                break;
            case EVENTS.MAKE_ACTIVE:
                if(this.views && this.views.button && this.views.button.button){
                    this.views.button.button.show();
                }
                u.lang.updateNode(buttons.titleLayout, u.lang.users_d.format(main.users.getCountActive()));

//                buttons.titleLayout.innerHTML = "Users (" + main.users.getCountActive() +")";
                if(main.users.getCountActive() > 1) {
                    buttons.open();
                } else if(!main.tracking || main.tracking.getStatus() == EVENTS.TRACKING_DISABLED) {
                    buttons.close();
                }
                break;
            case EVENTS.MAKE_INACTIVE:
                if(this.views && this.views.button && this.views.button.button && this.views.button.button.classList) this.views.button.button.hide();
                u.lang.updateNode(buttons.titleLayout, u.lang.users_d.format(main.users.getCountActive()));
                if(main.users.getCountActive() < 2 && (!main.tracking || main.tracking.getStatus() == EVENTS.TRACKING_DISABLED)) {
                    buttons.close();
                }
                break;
//            case EVENTS.UPDATE_ADDRESS:
//                var subtitle = this.views.button.subtitle;
//                if(object) {
//                    subtitle.innerHTML = object;
//                    if(!u.load("button:minimized")) {
//                        subtitle.show();
//                        updateSubtitle.call(this);
//                    }
//                } else {
//                    subtitle.hide();
//                }
//                break;
            case EVENTS.MAKE_ENABLED:
                if(this.views && this.views.button && this.views.button.button && this.views.button.button.classList) {
                    this.views.button.button.classList.remove("user-button-away");
                    this.views.button.button.classList.remove("disabled");
                    if(this != main.me) {
                        var delta = new Date().getTime() - parseInt(object || this.properties.changed);
                        if(delta > 60000) {
                            var text = utils.toDateString(new Date().getTime() - parseInt(object || this.properties.changed));
                            this.fire(EVENTS.UPDATE_MENU_SUFFIX, u.lang.s_ago.format(text).innerHTML);
                        } else {
                            this.fire(EVENTS.UPDATE_MENU_SUFFIX);
                        }
                    }
                }
                break;
            case EVENTS.MAKE_DISABLED:
                if(this.views && this.views.button && this.views.button.button && this.views.button.button.classList) {
                    this.views.button.button.classList.add("user-button-away");
                    if(this != main.me) {
                        var delta = new Date().getTime() - parseInt(object || this.properties.changed);
                        console.log("DELTA",delta)
                        if(delta > 60000) {
                            var text = utils.toDateString(new Date().getTime() - parseInt(object || this.properties.changed));
                            this.fire(EVENTS.UPDATE_MENU_SUFFIX, u.lang.s_ago.format(text).innerHTML);
                        } else {
                            this.fire(EVENTS.UPDATE_MENU_SUFFIX);
                        }
                    }
                }
                break;
            case EVENTS.SHOW_BADGE:
                if(object == EVENTS.INCREASE_BADGE) {
                    var value = parseInt(this.views.button.badge.innerHTML);
                    value = value || 0;
                    this.views.button.badge.innerHTML = ""+(++value);
                    this.views.button.button.scrollIntoView({behaviour: "smooth"});
                } else {
                    this.views.button.badge.innerHTML = object || "";
                }
                if(this.views.button.badge.innerHTML) {
                    this.views.button.badge.show();
                }
                break;
            case EVENTS.HIDE_BADGE:
                this.views.button.badge.hide();
                this.views.button.badge.innerHTML = "";
                break;
            case EVENTS.UPDATE_MENU_PREFIX:
                if(object) {
                    this.views.button.prefix.innerHTML = object;
                    this.views.button.prefix.show(HIDING.SCALE_X_RIGHT);
                } else {
                    this.views.button.prefix.hide(HIDING.SCALE_X_RIGHT);
                }
                break;
            case EVENTS.UPDATE_MENU_SUFFIX:
                if(object) {
                    this.views.button.suffix.innerHTML = object;
                    this.views.button.suffix.show(HIDING.SCALE_X_RIGHT);
                } else {
                    this.views.button.suffix.hide(HIDING.SCALE_X_RIGHT);
                }
                break;
            case EVENTS.MOUSE_OVER:
                this.views.button.button.classList.add("user-button-hover");
                break;
            case EVENTS.MOUSE_OUT:
                this.views.button.button.classList.remove("user-button-hover");
                break;
            case EVENTS.CHANGE_COLOR:
                if(!object && object.constructor === String) {
                    var color = object || "#0000FF";
                    color = utils.getRGBAColor(color, 0.4)
                    this.views.button.button.style.backgroundColor = color;
                } else if(object && object.constructor === Number) {
//                    console.log("TODO NUMERIC")
                }
                break;
            case EVENTS.EXPAND_MENU:
                main.users.forAllUsers(function(number,user){
                    user.views.button.subtitle.show(HIDING.SCALE_XY);
                });
                break;
            case EVENTS.COLLAPSE_MENU:
                main.users.forAllUsers(function(number,user){
                    user.views.button.subtitle.hide(HIDING.SCALE_XY);
                });
                break;
            case EVENTS.UPDATE_MENU_SUBTITLE:
                if(!this.views.button.subtitle.classList.contains("hidden")) {
                    this.views.button.subtitle.innerHTML = object || "";
                }
                break;
            default:
                break;
        }
        return true;
    }

    function createView(user){

//    if(buttons.itemsLayout.children.length ==1 && user != main.me){
//    debugger;
//    }

        if(!user || !user.properties) return;
        var color = user.color || user.properties.color || "#0000FF";
        color = utils.getRGBAColor(color, 0.4);

        var firstClick;
        var b = u.create(HTML.DIV, {
            className:"user-button hidden" + (user.locations && user.locations.length > 0 ? "" : " disabled") + (user.type == "user" ? " user-button-away" : ""),
            dataNumber:user.number,
            style:{backgroundColor:color},
            onclick: function() {
                user.fire(EVENTS.SELECT_SINGLE_USER);
                openContextMenu(user, false);
                var thisClick = new Date().getTime();
                firstClick = firstClick || 0;
                if(thisClick - firstClick < 500) {
                    setTimeout(function(){
                        user.fire(EVENTS.CAMERA_ZOOM);
                        contextMenuLayout.hide(HIDING.OPACITY);
                    },0);
                }
                firstClick = thisClick;
            },
            onlongclick: function() {
                main.toast.show(user.properties.getDisplayName());
                openContextMenu(user, true);
            },
            onmouseenter: function(e) {
                user.fire(EVENTS.MOUSE_OVER,e);
            },
            onmouseleave: function(e) {
                user.fire(EVENTS.MOUSE_OUT,e);
            }
        });
        var icon = (user && user.origin && user.origin.buttonIcon) || "person";
        u.create(HTML.DIV, {className:"icon user-button-icon notranslate", innerHTML:icon}, b);
        var badge = u.create(HTML.DIV, {className:"user-button-badge hidden"}, b);
        var div = u.create(HTML.DIV, {className:"user-button-label"}, b);
        var divText = u.create(HTML.DIV, null, div);
        var prefix = u.create(HTML.DIV, {className:"user-button-prefix hidden", innerHTML:""}, divText);
        var title = u.create(HTML.DIV, {className:"user-button-title", innerHTML:user.properties.getDisplayName()}, divText);
        var suffix = u.create(HTML.DIV, {className:"user-button-suffix hidden", innerHTML:""}, divText);
        var subtitle = u.create(HTML.DIV, {className:"user-button-subtitle hidden", innerHTML:""}, div);

        if(!u.load("button:minimized")) {
            subtitle.show(HIDING.SCALE_Y_TOP);
        }

        buttons.titleLayout.innerHTML = "Users (" + main.users.getCountActive() +")";

        var added = false;
        for(var i = 0; i < buttons.itemsLayout.children.length; i++) {
            var node = buttons.itemsLayout.children[i];
            var number = parseInt(node.dataset.number);
            if(number != main.me.number && number > user.number) {
                buttons.itemsLayout.insertBefore(b, node);
                added = true;
                break;
            }
        }
        if(!added) {
            buttons.itemsLayout.appendChild(b);
        }

        return {
            button: b,
            prefix: prefix,
            title: title,
            suffix: suffix,
            subtitle: subtitle,
            badge:badge
        };
    }

    function removeView(user){
        if(user.views && user.views.button && user.views.button.button) {
            delete buttons.itemsLayout[user.views.button.button];
            user.views.button.button.parentNode.removeChild(user.views.button.button);
            delete user.views.button;
        }
    }

    function openContextMenu(user) {
        u.clear(contextMenuLayout);
        sections = [];
        for(var i = 0; i < 10; i ++) {
            sections[i] = u.create(HTML.DIV, {className:"user-context-menu-section hidden"}, contextMenuLayout);
        }
        user.fire(EVENTS.CREATE_CONTEXT_MENU, contextMenu);

        setTimeout(function(){
            var size = user.views.button.button.getBoundingClientRect();
            contextMenuLayout.show(HIDING.OPACITY);
            contextMenuLayout.style.top = Math.floor(size.top) + "px";
            if(size.left - main.right.offsetLeft - contextMenuLayout.offsetWidth -10 > 0) {
                contextMenuLayout.style.left = Math.floor(size.left - contextMenuLayout.offsetWidth -10) + "px";
            } else {
                contextMenuLayout.style.left = Math.floor(size.right + 10) + "px";
            }
            if(main.right.offsetTop + main.right.offsetHeight < contextMenuLayout.offsetTop + contextMenuLayout.offsetHeight) {
                contextMenuLayout.style.top = (main.right.offsetTop + main.right.offsetHeight - contextMenuLayout.offsetHeight - 5) + "px";
            }

            clearTimeout(delayDismiss);
            delayDismiss = setTimeout(function(){
                contextMenuLayout.hide(HIDING.OPACITY);
            },2000);
        },0);
    }

    function ContextMenu() {
        function add(section,id,name,icon,callback) {
            var th = u.create(HTML.DIV, {
                className:"user-context-menu-item",
                onclick: function() {
                    setTimeout(function(){
                        contextMenuLayout.blur();
                        callback();
                    }, 0);
                },
                onlongclick: function(){
                    clearTimeout(delayDismiss);
                    contextMenuLayout.classList.add("user-context-menu-list");
                }
            }, sections[section]);
            if(icon) {
                if(icon.constructor === String) {
                    u.create(HTML.DIV, { className:"user-context-menu-item-icon notranslate", innerHTML: icon }, th);
                } else {
                    th.appendChild(icon);
                }
            }
            u.create(HTML.DIV, { className:"user-context-menu-item-title", innerHTML: name}, th);
            sections[section].show();
            return th;
        }
        function getContextMenu(){
            console.log("GETCONTEXTMENU:",items);
        }
        return {
            add:add,
            getContextMenu:getContextMenu
        }
    }

    function onChangeLocation(location) {
        //updateSubtitle.call(this);
    }


    return {
        type:type,
        start:start,
        onEvent:onEvent,
        createView:createView,
        removeView:removeView,
        onChangeLocation:onChangeLocation
    }
}