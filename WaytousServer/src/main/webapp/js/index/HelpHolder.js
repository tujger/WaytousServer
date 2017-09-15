/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 7/23/17.
 */

EVENTS.HELP = "help";

function HelpHolder(main) {

    this.type = "help";
    this.category = "docs";
    this.title = u.lang.help;
    this.menu = u.lang.help;
    this.icon = "help";

    this.start = function() {
        console.log("START HELP");
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.RELOAD:
                if(object != this.type) {
                    break;
                }
            case EVENTS.HELP:
                console.log("INDEX HELP");
                var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
                u.post("/rest/v1/getContent", {resource: "index-help.html", locale: lang}).then(function(xhr){
                    u.byId("content").innerHTML = xhr.response;
                    u.byId("content").classList.add("content-HELP");
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