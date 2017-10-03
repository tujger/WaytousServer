/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 2/12/17.
 */
function MyUser(main) {

    this.locations = [];
    this.views = {};
    this.location = null;
    this.properties = null;

    this.fire = function(EVENT,object) {
        var user = this;
        setTimeout(function(){
            main.eventBus.fire(function(holder){
                if(user.views[holder.type] && holder.onEvent) {
                    return holder.onEvent.call(user, EVENT, object);
                }
            });
        }, 0);
    };

    this.createViews = function() {
        var user = this;
        if(user.number != undefined) {
            main.eventBus.fire(function(holder){
                if (holder.createView && !user.views[holder.type]) {
                    try {
                        var view = holder.createView(user);
                        if (view) user.views[holder.type] = view;
                    } catch (e) {
                        console.error(holder.type,e);
                    }
                }
            });
        }
    };

    this.removeViews = function() {
        var user = this;
        if(user.number != undefined) {
            main.eventBus.fire(function(holder){
                if(holder.removeView) holder.removeView(user);
            });
        }

        clearInterval(this.taskLocationUpdate);
    };

    this.addLocation = function(location) {
        if(!location) return;

        this.locations.push(location);
        this.location = location;
        this.onChangeLocation();
    };

    this.onChangeLocation = function() {
        var user = this;
        main.eventBus.fire(function(holder){
            if(user.views[holder.type] && holder.onChangeLocation) holder.onChangeLocation.call(user, user.location);
        });
    };

}