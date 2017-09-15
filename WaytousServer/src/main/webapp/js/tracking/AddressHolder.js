/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 2/10/17.
 */

EVENTS.UPDATE_ADDRESS = "update_address";

function AddressHolder(main) {

    var type = "address";
    var counter = 0;
    var delayInError = 10000;
    var delayStart;

    function start() {
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.SELECT_USER:
                //onChangeLocation.call(this,this.location);
                break;
            case EVENTS.SELECT_SINGLE_USER:
                updateAddress.call(this);
                break;
            case EVENTS.EXPAND_MENU:
                main.users.forAllUsers(function(number,user){
                    updateAddress.call(user);
                });
                break;
            /*case EVENTS.UPDATE_ACTIONBAR_SUBTITLE:
                updateAddress.call(this, object);
                break;*/
            default:
                break;
        }
        return true;
    }

    function onChangeLocation(location) {
        //return;
        //var user = this;
        updateAddress.call(this);
/*        setTimeout(function(){
            if(location) {
                if(delayStart) {
                    if(new Date().getTime() - (delayStart||0) < delayInError) return;
                    delayStart = 0;
                }

                console.warn("FETCH",location.coords.latitude);
                u.getJSON("https://nominatim.openstreetmap.org/reverse?format=json&lat=" + location.coords.latitude + "&lon=" + location.coords.longitude + "&zoom=18&addressdetails=1")
                    .then(function(json){
                        user.fire.call(user, EVENTS.UPDATE_MENU_SUBTITLE, json["display_name"]);
                    }).catch(function(code, xhr) {
                    user.fire(EVENTS.UPDATE_ADDRESS);
                    delayStart = new Date().getTime();
                });

            }
        }, 0);*/
    }

    function createView(user) {
        return {
            lastRequest: 0,
            lastRequestedCoords: {latitude:0,longitude:0},
        };
    }

    function updateAddress() {
        var user = this;
        if(user.views && user.views.address && user.views.address.lastKnownAddress) {
            user.fire.call(user, EVENTS.UPDATE_MENU_SUBTITLE, user.views.address.lastKnownAddress);
            user.fire.call(user, EVENTS.UPDATE_ACTIONBAR_SUBTITLE, user.views.address.lastKnownAddress);
        } else {
            user.fire.call(user, EVENTS.UPDATE_MENU_SUBTITLE);
            user.fire.call(user, EVENTS.UPDATE_ACTIONBAR_SUBTITLE);
        }

        if(user.location && user.location.coords) {
            var currentTime = new Date().getTime();
            if(currentTime - user.views[type].lastRequest < 5000) return;
            if(user.views[type].lastRequestedCoords.latitude == user.location.coords.latitude && user.views[type].lastRequestedCoords.longitude == user.location.coords.longitude) return;
            if(delayStart) {
                if(currentTime - (delayStart||0) < delayInError) return;
                delayStart = 0;
            }
            user.views[type].lastRequest = currentTime;
            user.views[type].lastRequestedCoords = user.location.coords;

//console.warn(++counter, user.number);
            u.getJSON("https://nominatim.openstreetmap.org/reverse?format=json&lat=" + user.location.coords.latitude + "&lon=" + user.location.coords.longitude + "&zoom=18&addressdetails=1")
                .then(function(json){
                    user.views.address.lastKnownAddress = json["display_name"];
                    user.fire.call(user, EVENTS.UPDATE_MENU_SUBTITLE, user.views.address.lastKnownAddress);
                    user.fire.call(user, EVENTS.UPDATE_ACTIONBAR_SUBTITLE, user.views.address.lastKnownAddress);

                    //node.innerHTML = json["display_name"];
                });
        }
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
        createView:createView,
        onChangeLocation:onChangeLocation,
    }
}