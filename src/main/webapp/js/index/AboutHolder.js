/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 4/24/17.
 */

EVENTS.ABOUT = "about";

function AboutHolder(main) {

    this.type = "about";
    this.category = "info";
    this.title = u.lang.about;
    this.menu = u.lang.about;
    this.icon = "info_outline";

    this.start = function() {
    };

    this.onEvent = function(event, object) {

        switch(event) {
            case EVENTS.RELOAD:
                if(object !== this.type) {
                    break;
                }
            case EVENTS.ABOUT:
                console.log("INDEX ABOUT");
                var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
                u.post("/rest/v1/getContent", {resource: "index-about.html", locale: lang}).then(function(xhr){
                    u.byId("content").innerHTML = xhr.response;
                    u.byId("content").classList.add("content-about");
                    if(object) object();
                }).catch(function(error, json) {
                    u.byId("content").innerHTML = "Error";
                    u.byId("content").classList.add("content-error");
                    if(object) object();
                });
                break;
        }

    };

}