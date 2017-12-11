/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 3/16/17.
 */

EVENTS.SAVE_LOCATION = "save_location";
EVENTS.SHOW_SAVED_LOCATION = "show_saved_location";
EVENTS.EDIT_SAVED_LOCATION = "edit_saved_locations";
EVENTS.HIDE_SAVED_LOCATION = "hide_saved_location";
EVENTS.DELETE_SAVED_LOCATION = "delete_saved_location";
EVENTS.SHOW_SAVED_LOCATIONS = "show_saved_locations";
EVENTS.SHARE_SAVED_LOCATION = "share_saved_locations";
EVENTS.SEND_SAVED_LOCATION = "send_saved_locations";

function SavedLocationHolder(main) {

    var type = "saved_location";

    var locationSavedDialog;
    var locationEditDialog;
    var locationShareDialog;
    var locationSendDialog;
    var locationDeleteDialog;
    var shareBlockedDialog;
    var locationsDialog;
    var drawerMenuItem;
    var showNavigation = false;

    function start() {
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                drawerMenuItem = object.add(DRAWER.SECTION_NAVIGATION, EVENT.SHOW_SAVED_LOCATIONS, u.lang.saved_locations, "pin_drop", function(){
                    if(locationsDialog && locationsDialog.opened) {
                        locationsDialog.close();
                    } else {
                        main.fire(EVENTS.SHOW_SAVED_LOCATIONS);
                    }
                });
                var last = u.load("saved_location:counter") || 0;
                var exists = false;
                for(var i = 0; i <= last; i++) {
                    if(u.load("saved_location:"+i)) {
                        exists = true;
                        break;
                    }
                }
                if(!exists) drawerMenuItem.hide();
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user && (user.type === "user" || user.saveable) && user.location && !user.saved_location) {
                    object.add(MENU.SECTION_NAVIGATION, EVENT.SAVE_LOCATION, u.lang.save_location, "pin_drop", function () {
                        user.fire(EVENTS.SAVE_LOCATION);
                    });
                }
                if(user.type === type) {
                    object.add(MENU.SECTION_EDIT, EVENT.EDIT_SAVED_LOCATION, u.lang.edit, "mode_edit", function () {
                        main.fire(EVENTS.EDIT_SAVED_LOCATION, user.number - 10000);
                    });
                    object.add(MENU.SECTION_VIEWS, EVENT.HIDE_SAVED_LOCATION, u.lang.hide, "pin_drop", function () {
                        main.fire(EVENTS.HIDE_SAVED_LOCATION, user.number - 10000);
                    });
                    if(main.tracking && main.tracking.getStatus() === EVENTS.TRACKING_ACTIVE) {
                        object.add(MENU.SECTION_COMMUNICATION, EVENT.SEND_SAVED_LOCATION, u.lang.send_to_group, "chat", function () {
                            main.fire(EVENTS.SEND_SAVED_LOCATION, user.number - 10000);
                        });
                    }
                    object.add(MENU.SECTION_COMMUNICATION, EVENT.SHARE_SAVED_LOCATION, u.lang.share, "share", function () {
                        main.fire(EVENTS.SHARE_SAVED_LOCATION, user.number - 10000);
                    });
                }
                break;
            case EVENTS.SAVE_LOCATION:
                user = this;
                if(user) {
                    var loc = {
                        la:user.location.coords.latitude,
                        lo:user.location.coords.longitude,
                        t:user.timestamp || new Date().getTime(),
                        n:user.properties.getDisplayName(),
                        a:user.address || "",
                        d:user.description || "",
                        k:user.key || ""
                    };
                    var last = u.load("saved_location:counter") || 0;
                    last++;
                    u.save("saved_location:counter", last);
                    u.save("saved_location:" + last, loc);
                    drawerMenuItem.show();
                    fetchAddressFor(last);
                    if(locationsDialog && locationsDialog.opened) main.fire(EVENTS.SHOW_SAVED_LOCATIONS);

                    locationSavedDialog = locationSavedDialog || u.dialog({
                        items: [
                            { type: HTML.HIDDEN },
                            { type: HTML.DIV, innerHTML: u.lang.you_have_added_location }
                        ],
                        positive: {
                            label: u.lang.show,
                            onclick: function(items) {
                                main.fire(EVENTS.SHOW_SAVED_LOCATION, items[0].value);
                            }
                        },
                        neutral: {
                            label: u.lang.edit,
                            onclick: function(items) {
                                main.fire(EVENTS.EDIT_SAVED_LOCATION, items[0].value);
                            }
                        },
                        negative: {
                            label: u.lang.maybe_later
                        },
                        timeout: 5000
                    }, main.right);
                    locationSavedDialog.items[0].value = last;
                    locationSavedDialog.open();
                }
                break;
            case EVENTS.SHOW_SAVED_LOCATION:
                var number = parseInt(object);
                if(main.users.users[10000 + number]) {
                    main.users.forUser(10000 + number, function(number, user){
                        user.fire(EVENTS.MAKE_ACTIVE);
                        user.fire(EVENTS.SELECT);
                    });
                } else {
                    var loc = u.load("saved_location:"+number);
                    if(loc) {
                        var o = {};
                        o[USER.PROVIDER] = type;
                        o[USER.LATITUDE] = loc.la;
                        o[USER.LONGITUDE] = loc.lo;
                        o[USER.ALTITUDE] = 0;
                        o[USER.ACCURACY] = 0;
                        o[USER.BEARING] = 0;
                        o[USER.SPEED] = 0;
                        o[USER.NUMBER] = 10000 + number;
                        o[USER.COLOR] = utils.getDecimalColor("#00AA00");
                        o[USER.NAME] = loc.n;
                        o[REQUEST.TIMESTAMP] = loc.t;
                        o.markerIcon = {
                            path: "M0 12 c 0 -11 9 -20 20 -20 c 11 0 20 9 20 20 c 0 11 -9 20 -20 20 c -11 0 -20 -9 -20 -20 m26 -3c0-3.31-2.69-6-6-6s-6 2 -6 6c0 4.5 6 11 6 11s6-6.5 6-11zm-8 0c0-1.1.9-2 2-2s2 .9 2 2-.89 2-2 2c-1.1 0-2-.9-2-2z m-5 12v2h14v-2h-14.5z",
                            fillColor: "green",
                            fillOpacity: 0.7,
                            scale: 1.2,
                            strokeColor: "white",
                            strokeOpacity: 0.6,
                            strokeWeight: 2,
                            anchor: new google.maps.Point(40/2, 40/2)
                        };
                        o.buttonIcon = "pin_drop";
                        o.type = type;

                        var user = main.users.addUser(o);

                        main.users.forUser(10000 + number, function(number, user){
                            user.fire(EVENTS.MAKE_ACTIVE);
                            user.fire(EVENTS.CHANGE_COLOR, "#00AA00");
                            main.fire(USER.JOINED, user);
                            user.fire(EVENTS.SELECT_SINGLE_USER);
                        });
                    }
                }
                break;
            case EVENTS.HIDE_SAVED_LOCATION:
                var number = parseInt(object);
                main.users.forUser(10000 + number, function(number, user){
                    user.removeViews();
                    user.fire(EVENTS.MAKE_INACTIVE);
                    main.fire(EVENTS.CAMERA_UPDATE);
                });
                break;
            case EVENTS.EDIT_SAVED_LOCATION:
                locationEditDialog && locationEditDialog.close();
                locationShareDialog && locationShareDialog.close();
                locationSendDialog && locationSendDialog.close();
                locationDeleteDialog && locationDeleteDialog.close();
                var number = parseInt(object);
                var loc = u.load("saved_location:"+number);
                locationEditDialog = locationEditDialog || u.dialog({
                    title: u.lang.edit_location,
                    items: [
                        { type: HTML.HIDDEN },
                        { type: HTML.INPUT, label: u.lang.name },
                        { type: HTML.TEXTAREA, label: u.lang.description }
                    ],
                    className: "saved-location-edit-dialog",
                    positive: {
                        label: u.lang.ok,
                        onclick: function(items) {
                            var number = parseInt(items[0].value);
                            var name = u.clear(items[1].value || "");
                            var description = u.clear(items[2].value || "");
                            var loc = u.load("saved_location:"+number);
                            loc.n = name;
                            loc.d = description;
                            delete loc[DATABASE.SYNCED];
                            u.save("saved_location:"+number, loc);
                            if(locationsDialog && locationsDialog.opened) main.fire(EVENTS.SHOW_SAVED_LOCATIONS);
                            main.users.forUser(10000 + number, function(number, user){
                                user.fire(EVENTS.CHANGE_NAME, name);
                            });
                            main.fire(EVENTS.SYNC_PROFILE);
                        }
                    },
                    neutral: {
                        label: u.lang.delete,
                        onclick: function(items) {
                            main.fire(EVENTS.DELETE_SAVED_LOCATION, items[0].value)
                        }
                    },
                    negative: {
                        label: u.lang.cancel
                    },
                }, main.right);

                if(loc) {
                    locationEditDialog.items[0].value = number;
                    locationEditDialog.items[1].value = loc.n;
                    locationEditDialog.items[2].value = loc.d;
                    locationEditDialog.open();
                }
                break;
            case EVENTS.SHARE_SAVED_LOCATION:
                locationEditDialog && locationEditDialog.close();
                locationShareDialog && locationShareDialog.close();
                locationSendDialog && locationSendDialog.close();
                locationDeleteDialog && locationDeleteDialog.close();
                var loc = u.load("saved_location:"+parseInt(object));
                if(loc) {
                    /*locationShareDialog = locationShareDialog || u.dialog({
                        items: [
                            {type:HTML.DIV, innerHTML: u.lang.let_your_email_client_compose_the_message_with_link_to_this_location },
                            {type:HTML.INPUT, className: "dialog-item-input-link", readOnly:true },
                        ],
                        positive: {
                            label: u.lang.mail,
                            onclick: function() {
                                var link = locationShareDialog.items[1].value;
                                var popup = window.open("mailto:?subject=Here is that location&body="+encodeURIComponent(encodeURI(link)),"_blank");
                                utils.popupBlockerChecker.check(popup, function() {
                                    shareBlockedDialog = shareBlockedDialog || u.dialog({
                                        items: [
                                            {type:HTML.DIV, innerHTML:u.lang.popup_blocked_dialog_1 },
                                            {type:HTML.DIV, enclosed:true, innerHTML:u.lang.popup_blocked_dialog_2 },
                                            {type:HTML.DIV, innerHTML:u.lang.popup_blocked_dialog_3 },
                                            {type:HTML.DIV, innerHTML:locationShareDialog.items[0].value}
                                        ],
                                        positive: {
                                            label: u.lang.close
                                        },
                                    }, main.right);
                                    shareBlockedDialog.open();
                                });
                            }
                        },
                        neutral: {
                            label: u.lang.copy,
                            dismiss: false,
                            onclick: function(items) {
                                if(u.copyToClipboard(items[1])) {
                                    main.toast.show(u.lang.link_was_copied_into_clipboard, 3000);
                                }
                                locationShareDialog.close();
                            }
                        },
                        negative: {
                            label: u.lang.cancel
                        },
//                        timeout: 20000
                    }, main.right);
                    locationShareDialog.items[1].value = "http://maps.google.com/maps?z=14&q=loc:=" + loc.la + "," + loc.lo;
                    locationShareDialog.open();*/
                    main.fire(EVENTS.SHARE_LINK, "http://maps.google.com/maps?z=14&q=loc:" + loc.la + "," + loc.lo);
                }
                break;
            case EVENTS.SEND_SAVED_LOCATION:
                locationEditDialog && locationEditDialog.close();
                locationShareDialog && locationShareDialog.close();
                locationSendDialog && locationSendDialog.close();
                locationDeleteDialog && locationDeleteDialog.close();

                var number = parseInt(object);
                var loc = u.load("saved_location:"+number);
                locationSendDialog = locationSendDialog || u.dialog({
                    title: u.lang.send_location,
                    items: [
                        { type: HTML.HIDDEN },
                        { type: HTML.DIV }
                    ],
                    positive: {
                        label: u.lang.yes,
                        onclick: function(items) {
                            var number = parseInt(items[0].value);
                            var loc = u.load("saved_location:"+number);

                            main.tracking.put(USER.LATITUDE, loc.la);
                            main.tracking.put(USER.LONGITUDE, loc.lo);
                            main.tracking.put(USER.ADDRESS, loc.a);
                            main.tracking.put(USER.NAME, loc.n);
                            main.tracking.put(USER.DESCRIPTION, loc.d);
                            main.tracking.put(REQUEST.PUSH, true);
                            main.tracking.put(REQUEST.DELIVERY_CONFIRMATION, true);
                            main.tracking.send(REQUEST.SAVED_LOCATION);
                        }
                    },
                    negative: {
                        label: u.lang.no
                    }
                }, main.right);

                if(loc) {
                    locationSendDialog.items[0].value = number;
                    u.lang.updateNode(locationSendDialog.items[1], u.lang.you_re_going_to_send_the_location_s_to_all_continue.format(loc.n));
                    locationSendDialog.open();
                }
                break;
            case EVENTS.DELETE_SAVED_LOCATION:
                locationEditDialog && locationEditDialog.close();
                locationShareDialog && locationShareDialog.close();
                locationSendDialog && locationSendDialog.close();
                locationDeleteDialog && locationDeleteDialog.close();
                var number = parseInt(object);
                var loc = u.load("saved_location:"+number);
                locationDeleteDialog = locationDeleteDialog || u.dialog({
                    title: u.lang.delete_location,
                    items: [
                        { type: HTML.HIDDEN },
                        { type: HTML.DIV, innerHTML: u.lang.delete_this_location }
                    ],
                    className: "saved-location-delete-dialog",
                    positive: {
                        label: u.lang.yes,
                        onclick: function(items) {
                            var number = items[0].value;
                            main.fire(EVENTS.HIDE_SAVED_LOCATION, number);

                            var loc = u.load("saved_location:"+number);
                            var newLoc = {};
                            newLoc[DATABASE.KEYS] = loc[DATABASE.KEYS];

                            u.save("saved_location:"+number, newLoc);
                            if(locationsDialog && locationsDialog.opened) main.fire(EVENTS.SHOW_SAVED_LOCATIONS);
                            main.fire(EVENTS.SYNC_PROFILE);
                        }
                    },
                    negative: {
                        label: u.lang.no
                    }
                }, main.right);

                if(loc) {
                    locationDeleteDialog.items[0].value = number;
                    locationDeleteDialog.open();
                }
                break;
            case EVENTS.SHOW_SAVED_LOCATIONS:
                locationsDialog = locationsDialog || u.dialog({
                    title: {
                        label: u.lang.saved_locations_d.format(0),
                        filter: true,
                    },
                    resizeable: true,
                    items: [],
                    className: "saved-locations-dialog",
                    itemsClassName: "saved-locations-dialog-items",
                    onopen: function(){},
                    onclose: function(){},
                    negative: {
                        onclick: function() {}
                    }
                }, main.right);
                locationsDialog.clearItems();
                var last = u.load("saved_location:counter") || 0;
                for(var i = 1; i <= last; i++) {
                    var loc = u.load("saved_location:"+i);
                    if(loc && loc.la && loc.lo) {
                        var div = locationsDialog.addItem({
                            type: HTML.DIV,
                            className: "saved-location-item",
                        });
                        locationsDialog.setTitle(u.lang.saved_locations_d.format(locationsDialog.items.length || 0));
                        var url = "https://maps.googleapis.com/maps/api/staticmap?center=" +loc.la + "," + loc.lo + "&zoom=15&size=200x200&sensor=false" + "&markers=color:darkgreen|"+loc.la+","+loc.lo + "&key="+data.firebase_config.apiKey;

                        u.create(HTML.IMG, {
                            src: url,
                            className: "saved-locations-dialog-item-image",
                            onload: function(e) {
//                                console.log(e);
                            },
                            onerror: function(e) {
                                console.error(e);
                            },
                            innerHTML:"update"
                        }, div);

                        var content = u.create(HTML.DIV, { className: "saved-locations-dialog-item-text" }, div);
                        u.create(HTML.DIV, { className: "saved-locations-dialog-item-label", innerHTML:loc.n }, content);
                        u.create(HTML.DIV, { className: "saved-locations-dialog-item-timestamp", innerHTML:new Date(loc.t).toLocaleString() }, content);
                        u.create(HTML.DIV, { className: "saved-locations-dialog-item-address", innerHTML:loc.a }, content);
                        if(!loc.a) fetchAddressFor(i);
                        u.create(HTML.DIV, { className: "saved-locations-dialog-item-description", innerHTML:loc.d }, content);
                        u.create(HTML.BUTTON, { className: "saved-locations-dialog-item-button saved-locations-dialog-item-show notranslate", dataNumber: i, innerHTML:"remove_red_eye", title: u.lang.show_location.innerText, onclick:function(){
                            locationsDialog.close();
                            main.fire(EVENTS.SHOW_SAVED_LOCATION, this.dataset.number);
                        } }, content);
                        u.create(HTML.BUTTON, { className: "saved-locations-dialog-item-button saved-locations-dialog-item-navigate icon notranslate", dataNumber: i, innerHTML:"navigation", title:u.lang.show_direction_to_location.innerText, onclick:function(){
                            locationsDialog.close();
                            var number = this.dataset.number;
                            main.fire(EVENTS.SHOW_SAVED_LOCATION, number);
                            setTimeout(function(){
                                main.users.forUser(10000 + number, function(number, user){
                                    user.fire(EVENTS.SHOW_NAVIGATION);
                                });
                            },0);
                        } }, content);
                        u.create(HTML.BUTTON, { className: "saved-locations-dialog-item-button saved-locations-dialog-item-edit notranslate", dataNumber: i, innerHTML:"mode_edit", title:u.lang.edit_location.innerText, onclick:function(){
                            main.fire(EVENTS.EDIT_SAVED_LOCATION, this.dataset.number);
                        } }, content);
                        u.create(HTML.BUTTON, { className: "saved-locations-dialog-item-button saved-locations-dialog-item-share notranslate", dataNumber: i, innerHTML:"share", title:u.lang.share_location.innerText, onclick:function(){
                            main.fire(EVENTS.SHARE_SAVED_LOCATION, this.dataset.number);
                        } }, content);
                        u.create(HTML.BUTTON, { className: "saved-locations-dialog-item-button saved-locations-dialog-item-delete notranslate", dataNumber: i, innerHTML:"clear", title:u.lang.delete_location.innerText, onclick:function(){
                            main.fire(EVENTS.DELETE_SAVED_LOCATION, this.dataset.number);
                        } }, content);
                    }
                }
                locationsDialog.open();
                break;
            case EVENTS.SYNC_PROFILE:
                try {
                    var sync = new utils.sync({
                        type: utils.sync.Type.ACCOUNT_PRIVATE,
                        key: REQUEST.SAVED_LOCATION,
                        onsavelocalvalue: function (key, newLocation, oldValue) {
                            var last = u.load("saved_location:counter") || 0;
                            var number;
                            for(var i = 0; i <= last; i++) {
                                var loc = u.load("saved_location:"+i);
                                if(loc && loc.k && loc.k == key) {
                                    exists = true;
                                    number = i;
                                    break;
                                }
                            }
                            if(number) {
                                u.save("saved_location:" + number, newLocation);
                                drawerMenuItem && drawerMenuItem.show();
                                locationsDialog && locationsDialog.opened && main.fire(EVENTS.SHOW_SAVED_LOCATIONS);
                            } else {
                                last++;
                                u.save("saved_location:counter", last);
                                u.save("saved_location:" + last, newLocation);
                                drawerMenuItem && drawerMenuItem.show();
                                locationsDialog && locationsDialog.opened && main.fire(EVENTS.SHOW_SAVED_LOCATIONS);
                            }
                        },
                        onsaveremotevalue: function (key, newLocation, oldValue) {
                            var last = u.load("saved_location:counter") || 0;
                            var number;
                            for(var i = 0; i <= last; i++) {
                                var loc = u.load("saved_location:"+i);
                                if(loc && loc.k && loc.k == key) {
                                    exists = true;
                                    number = i;
                                    break;
                                }
                            }
                            if(number) {
                                u.save("saved_location:" + number, newLocation);
                                drawerMenuItem && drawerMenuItem.show();
                                locationsDialog && locationsDialog.opened && main.fire(EVENTS.SHOW_SAVED_LOCATIONS);
                            } else {
                                last++;
                                u.save("saved_location:counter", last);
                                u.save("saved_location:" + last, newLocation);
                                drawerMenuItem && drawerMenuItem.show();
                                locationsDialog && locationsDialog.opened && main.fire(EVENTS.SHOW_SAVED_LOCATIONS);
                            }
                        }
                    });

                    if (sync.ready()) {
                        var map = {};
                        var last = u.load("saved_location:counter") || 0;
                        var locs = [];
                        for (var i = 1; i <= last; i++) {
                            var loc = u.load("saved_location:" + i);
                            if (!loc) continue;
                            if(loc.k && map[loc.k]) {
                                u.save("saved_location:" + i);
                                continue;
                            } else if(loc.k) {
                                map[loc.k] = true;
                            } else if(!loc.k) {
                                u.save("saved_location:" + i);
                            }
                            locs.push(loc);
                        }
                        sync.syncValues(locs);
                    } else {
                        console.warn("Not ready for sync.")
                    }
                }catch (e) {console.error(e)}
                break;
            default:
                break;
        }
        return true;
    }

    function createView(user){
        var view = {
            user: user
        };
        return view;
    }

    function fetchAddressFor(number) {
        var self = this;
        self._fetching = self._fetching || {};
        if(self._fetching[number]) return;
        self._fetching[number] = true;

        var loc = u.load("saved_location:"+number);
        if(!loc || loc.a || !loc.la || !loc.lo) return;

        var xhr = new XMLHttpRequest();
        xhr.open("GET", "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + loc.la + "&lon=" + loc.lo + "&zoom=18&addressdetails=1", true);

        xhr.onreadystatechange = function () {
            if (xhr.readyState !== 4) return;
            delete self._fetching[number];
            try {
                var address = JSON.parse(xhr.response);
                if(address["display_name"]) {
                    loc.a = address["display_name"];
                    u.save("saved_location:"+number, loc);
                    console.log("Address was resolved for",loc.n,loc.a);
                    if(locationsDialog && locationsDialog.opened) main.fire(EVENTS.SHOW_SAVED_LOCATIONS);

                    main.fire(EVENTS.SYNC_PROFILE);
                }
            } catch(e) {
                console.log("Address was not resolved for",loc.n);
                main.fire(EVENTS.SYNC_PROFILE);
            }
        };
        try {
            xhr.send();
        } catch(e) {
            console.warn(e);
        }
    }

    function perform(json) {
        var number = u.clear(json[USER.NUMBER]);
        if(main.me.number == number) return;
        var from = main.users.users[number];
        var name = u.clear(json[USER.NAME] || (from ? from.properties.getDisplayName() : "Point"));
        var user = new MyUser(main);
        user.properties = {
           getDisplayName: function(){ return name }
        };
        user.location = {
           coords: {
               latitude: json[USER.LATITUDE],
               longitude: json[USER.LONGITUDE]
           }
        };
        user.description = u.clear(json[USER.DESCRIPTION] || "");
        user.address = u.clear(json[USER.ADDRESS] || "");
        user.timestamp = u.clear(json[REQUEST.TIMESTAMP]);
        user.key = u.clear(json["key"] || "");

        var last = u.load("saved_location:counter") || 0;
        for(var i = 1; i <= last; i++) {
            var saved = u.load("saved_location:"+i);
            if(saved && saved.k == user.key) return;
        }

        locationEditDialog && locationEditDialog.close();
        locationShareDialog && locationShareDialog.close();
        locationSendDialog && locationSendDialog.close();
        locationDeleteDialog && locationDeleteDialog.close();

        u.dialog({
             title: u.lang.add_location,
             queue: true,
             items: [
                 { type: HTML.DIV, innerHTML: u.lang.you_ve_got_the_location_from_s_add_it_to_your_saved_locations_list.format((from ? from.properties.getDisplayName() : number) + ": " + user.properties.getDisplayName()).outerHTML },
                 { type: HTML.DIV, className: "saved-location-receive-dialog-item-second" + (user.address ? "" : " hidden"), innerHTML: user.address ? u.lang.address_s.format(user.address).outerHTML : "" },
                 { type: HTML.DIV, className: "saved-location-receive-dialog-item-second" + (user.description ? "" : " hidden"), innerHTML: user.description ? u.lang.description_s.format(user.description).outerHTML : "" }
             ],
             itemsClassName: "saved-location-receive-dialog-items",
             className: "saved-location-receive-dialog",
             positive: {
                 label: u.lang.yes,
                 onclick: function() {
                     this.options.location.views[type] = createView(this.options.location);
                     this.options.location.fire(EVENTS.SAVE_LOCATION);
                 }
             },
             negative: {
                 label: u.lang.no,
                 onclick: function() {
                     var last = u.load("saved_location:counter") || 0;
                     last++;
                     u.save("saved_location:counter",last);
                     u.save("saved_location:"+last, {k:this.options.location.key});
                 }
             },
             location: user
         }, main.right).open();
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
        createView:createView,
        saveable:true,
        perform:perform,
        loadsaved:-1,
    }
}