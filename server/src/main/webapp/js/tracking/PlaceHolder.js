/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 2/10/17.
 */

EVENTS.SHOW_PLACE = "show_place";
EVENTS.EDIT_PLACE = "edit_place";
EVENTS.HIDE_PLACE = "hide_place";
EVENTS.SEND_PLACE = "send_place";
EVENTS.SHARE_PLACE = "share_place";

REQUEST.CODE_AUTOCOMPLETE_PLACE = 3;

function PlaceHolder(main) {

    var type = "place";
    var drawerItemSearch;
    var searchDialog;
    var searchField;
    var autocomplete;
    var placeEditDialog;
    var placeShareDialog;
    var placeSendDialog;

    function start() {
        // console.log("PLACEHOLDER",main);
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                drawerItemSearch = object.add(DRAWER.SECTION_NAVIGATION, EVENTS.SHOW_PLACE, u.lang.search, "search", function(){
                    if(!searchDialog) {
                        searchDialog = u.dialog({
                            className: "place-search",
                            itemsClassName: "place-search-items",
                            tabindex: -1,
                            items: [
                                { type: HTML.INPUT, className: "place-search-input", label: "keyboard_backspace", labelClassName: "place-search-label" }
                            ],
//                            onblur: function() {
//                                searchDialog.close();
//                            }
                        }, main.right);
                        autocomplete = new google.maps.places.Autocomplete(searchDialog.items[0]);
                        autocomplete.bindTo("bounds", main.map);
                        autocomplete.addListener("place_changed", function() {
                            searchDialog.close();
                            var place = autocomplete.getPlace();
                            if (!place.geometry) {
                                main.toast.show(u.lang.sorry_incorrect_place_has_returned);
                                return;
                            }
                            searchDialog.items[0].value = "";

                            var o = {};
                            o[USER.PROVIDER] = type;
                            o[USER.LATITUDE] = place.geometry.location.lat();
                            o[USER.LONGITUDE] = place.geometry.location.lng();
                            o[USER.ALTITUDE] = 0;
                            o[USER.ACCURACY] = 0;
                            o[USER.BEARING] = 0;
                            o[USER.SPEED] = 0;
                            o[USER.COLOR] = utils.getDecimalColor("#00AAAA");
                            o[USER.NAME] = place.name;
                            o[USER.ADDRESS] = place.formatted_address;
                            o[USER.DESCRIPTION] = "";
                            o["key"] = place.place_id;
                            o[REQUEST.TIMESTAMP] = new Date().getTime();

                            var last = u.loadForContext("place:last") || 0;

                            for(var i = 1; i <= last; i++) {
                                var l = u.loadForContext("place:"+i);
                                if(l && l.key && l.key == o.key) {
                                    main.users.forUser(l.number, function(number,user){
                                        user.fire(EVENTS.SELECT_SINGLE_USER);
                                    });
                                    return;
                                }
                            }

                            last ++;
                            u.saveForContext("place:last", last);

                            o[USER.NUMBER] = REQUEST.CODE_AUTOCOMPLETE_PLACE * 10000 + last;
                            u.saveForContext("place:"+last, o);
                            main.fire(EVENTS.SHOW_PLACE, o);
                        });
                        searchDialog.items[0].addEventListener("blur", function(){ searchDialog.close() });
                    }
                    searchDialog.open();
                });
                drawerItemSearch.disable();
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user.type == type) {
                    object.add(MENU.SECTION_VIEWS, EVENT.HIDE_PLACE, u.lang.hide_place, "location_off", function () {
                        user.fire(EVENTS.HIDE_PLACE);
                    });
                    object.add(MENU.SECTION_VIEWS, EVENT.HIDE_PLACE, u.lang.edit, "mode_edit", function () {
                        user.fire(EVENTS.EDIT_PLACE);
                    });
                    /*if(main.tracking && main.tracking.getStatus() == EVENTS.TRACKING_ACTIVE) {
                        object.add(MENU.SECTION_COMMUNICATION, EVENT.SEND_PLACE, "Send to group", "chat", function () {
                            user.fire(EVENTS.SEND_PLACE);
                        });
                    }
                    object.add(MENU.SECTION_COMMUNICATION, EVENT.SHARE_PLACE, "Share", "share", function () {
                        user.fire(EVENTS.SHARE_PLACE);
                    });*/
                }
                break;
            case EVENTS.MAP_READY:
                drawerItemSearch.enable();
                for(var i = 1; i <= (u.loadForContext("place:last") || 0); i++) {
                    var place = u.loadForContext("place:" + i);
                    if(place && place.number) {
                        main.fire(EVENTS.SHOW_PLACE, place);
                    } else {
                        u.saveForContext("place:" + i);
                    }
                }
                break;
            case EVENTS.SHOW_PLACE:
                if(object) {
                    object.markerIcon = {
                        path: "M0 12 c 0 -11 9 -20 20 -20 c 11 0 20 9 20 20 c 0 11 -9 20 -20 20 c -11 0 -20 -9 -20 -20z m20,-10 c-3.87 0 -7 3.13 -7 7 c0,5.25 7,13 7,13 s7,-7.75 7,-13 c0,-3.87 -3.13,-7 -7,-7z m0,9.5c-1.38,0 -2.5,-1.12 -2.5,-2.5s1.12,-2.5 2.5,-2.5 2.5,1.12 2.5,2.5 -1.12,2.5 -2.5,2.5z",
                        fillColor: "#00AAAA",
                        fillOpacity: 0.7,
                        scale: 1.2,
                        strokeColor: "white",
                        strokeOpacity: 0.6,
                        strokeWeight: 2,
                        anchor: new google.maps.Point(40/2, 40)
                    };
                    object.buttonIcon = "place";
                    object.type = type;

                    var user = main.users.addUser(object);
                    user.type = type;
                    user.origin = object;
                    user.saveable = true;

                    main.users.forUser(user.number, function(number, user){
                        user.fire(EVENTS.MAKE_ACTIVE);
                        user.fire(EVENTS.CHANGE_COLOR, "#00AAAA");
                        main.fire(USER.JOINED, user);
                        user.fire(EVENTS.SELECT_SINGLE_USER);
                        user.fire(EVENTS.SHOW_NAVIGATION);
                    });
                }
                break;
            case EVENTS.HIDE_PLACE:
                var user = this;
                user.removeViews();
                user.fire(EVENTS.MAKE_INACTIVE);
                main.fire(EVENTS.CAMERA_UPDATE);
                u.saveForContext("place:" + (user.origin.number - REQUEST.CODE_AUTOCOMPLETE_PLACE * 10000));

                break;
            case EVENTS.EDIT_PLACE:
                placeEditDialog && placeEditDialog.close();
                placeSendDialog && placeSendDialog.close();
                placeShareDialog && placeShareDialog.close();

                var place = this;
                if(place) {
                    placeEditDialog = placeEditDialog || u.dialog({
                        title: "Edit place",
                        items: [
                            { type: HTML.HIDDEN },
                            { type: HTML.INPUT, label: u.lang.name },
                            { type: "textarea", label: u.lang.description },
                        ],
                        className: "place-edit-dialog",
                        positive: {
                            label: "OK",
                            onclick: function(items) {
                                var number = parseInt(items[0].value);
                                var name = items[1].value || "";
                                var description = items[2].value || "";
                                place.origin.name = name;
                                place.origin.description = description;
                                u.saveForContext("place:"+(place.origin.number - REQUEST.CODE_AUTOCOMPLETE_PLACE * 10000), place.origin);

                                place.fire(EVENTS.CHANGE_NAME, name);
                            }
                        },
                        negative: {
                            label: u.lang.cancel
                        },
                    }, main.right);
                    placeEditDialog.items[0].value = place.number;
                    placeEditDialog.items[1].value = place.origin.name || "";
                    placeEditDialog.items[2].value = place.origin.description || "";
                    placeEditDialog.open();
                }
                break;
            /*case EVENTS.SHARE_PLACE:
                break;
            case EVENTS.SEND_PLACE:
                placeEditDialog && placeEditDialog.close();
                placeSendDialog && placeSendDialog.close();
                placeShareDialog && placeShareDialog.close();

                var place = this;
                if(place) {
                    placeSendDialog = placeSendDialog || u.dialog({
                        title: "Send location",
                        items: [
                            { type: HTML.HIDDEN },
                            { type: HTML.DIV },
                        ],
                        className: "saved-location-send-dialog",
                        positive: {
                            label: "Yes",
                            onclick: function(items) {
                                var number = parseInt(items[0].value);
                                var loc = u.loadForContext("saved_location:"+number);

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
                            label: "No"
                        },
                    }, main.right);

                    placeSendDialog.items[0].value = number;
                    placeSendDialog.items[1].innerHTML = "You're going to send the location " + loc.n + " to all. Continue?";
                    placeSendDialog.open();
                }
                break;*/
            default:
                break;
        }
        return true;
    }

    function createView(user) {
        return {};
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
        createView:createView,
    }
}