/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 7/23/17.
 */

EVENTS.SUPPORT = "support";

function SupportHolder(main) {

    this.type = "support";
    this.category = "info";
    this.title = u.lang.support;
    this.menu = u.lang.support;
    this.icon = "live_help";

    this.start = function() {
        console.log("START SUPPORT");
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.RELOAD:
                if(object != this.type) {
                    break;
                }
            case EVENTS.SUPPORT:
                console.log("INDEX SUPPORT");
                var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
                u.post("/rest/v1/getContent", {resource: "index-support.html", locale: lang}).then(function(xhr){
                    u.byId("content").innerHTML = xhr.response;
                    u.byId("content").classList.add("content-support");
                    if(object) object();
                }).catch(function(error, json) {
                    u.byId("content").innerHTML = "Error";
                    u.byId("content").classList.add("content-error");
                    if(object) object();
                });
                break;
        }
        return true;
    }

}